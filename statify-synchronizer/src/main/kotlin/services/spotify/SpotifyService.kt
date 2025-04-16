package org.danila.services.spotify

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.danila.event.EnrichAlbumEvent
import org.danila.event.EnrichArtistEvent
import org.danila.event.EnrichMetadata
import org.danila.event.EnrichTrackEvent
import org.danila.model.spotify.AlbumArtist
import org.danila.model.spotify.TrackArtist
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumImage
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.model.spotify.artist.ArtistImage
import org.danila.model.spotify.track.Track
import org.danila.services.api.spotify.SpotifyApiClient
import org.danila.services.model.spotify.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class SpotifyService @Autowired constructor(
    private val trackArtistService: TrackArtistService,
    private val albumArtistService: AlbumArtistService,
    private val artistGenreService: ArtistGenreService,
    private val artistImageService: ArtistImageService,
    private val albumImageService: AlbumImageService,
    private val artistService: ArtistService,
    private val albumService: AlbumService,
    private val trackService: TrackService,

    private val spotifyDataProcessor: SpotifyDataProcessor,
    private val spotifyApiClient: SpotifyApiClient,

    private val eventPublisher: ApplicationEventPublisher
) {

    suspend fun fetchSpotifyData(accessToken: String) {
        val authHeader = "Bearer $accessToken"
        val (artistDTOs, savedTracksDTOs, savedAlbumsDTOs) = fetchSpotifyDTOs(authHeader)

        processSpotifyData(
            artistDTOs = artistDTOs,
            trackDTOs = savedTracksDTOs.map { it.track },
            albumDTOs = savedAlbumsDTOs.map { it.album },
            accessToken = accessToken,
            enrich = false
        )
    }

    suspend fun enrichArtists(artistIds: Set<String>, accessToken: String) {
        val authHeader = "Bearer $accessToken"
        val artistDTOs = spotifyApiClient.getSeveralArtists(artistIds = artistIds, authHeader = authHeader)

        processSpotifyData(
            artistDTOs = artistDTOs,
            trackDTOs = emptyList(),
            albumDTOs = emptyList(),
            accessToken = accessToken,
            enrich = true
        )
    }

    suspend fun enrichAlbums(albumIds: Set<String>, accessToken: String) {
        val authHeader = "Bearer $accessToken"
        val albumDTOs = spotifyApiClient.getSeveralAlbums(albumIds = albumIds, authHeader = authHeader)

        processSpotifyData(
            artistDTOs = emptyList(),
            trackDTOs = emptyList(),
            albumDTOs = albumDTOs,
            accessToken = accessToken,
            enrich = true
        )
    }

    suspend fun enrichTracks(trackIds: Set<String>, accessToken: String) {
        val authHeader = "Bearer $accessToken"
        val trackDTOs = spotifyApiClient.getSeveralTracks(trackIds = trackIds, authHeader = authHeader)

        processSpotifyData(
            artistDTOs = emptyList(),
            trackDTOs = trackDTOs,
            albumDTOs = emptyList(),
            accessToken = accessToken,
            enrich = true
        )
    }

    private suspend fun fetchSpotifyDTOs(authHeader: String): Triple<List<ArtistDTO>, List<SavedTrackItemDTO>, List<SavedAlbumItemDTO>> =
        coroutineScope {
            val artistsDeferred = async { spotifyApiClient.getAllFollowedArtists(authHeader) }
            val tracksDeferred = async { spotifyApiClient.getAllSavedTracks(authHeader) }
            val albumsDeferred = async { spotifyApiClient.getAllSavedAlbums(authHeader) }

            Triple(artistsDeferred.await(), tracksDeferred.await(), albumsDeferred.await())
        }

    private suspend fun processSpotifyData(artistDTOs: List<ArtistDTO>, trackDTOs: List<TrackDTO>, albumDTOs: List<AlbumDTO>, accessToken: String, enrich: Boolean) {
        val existingData = fetchExistingData(artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs)
        val saveCollections = spotifyDataProcessor.processData(artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs, existingData = existingData)

        val savedCollections = saveData(saveCollections = saveCollections)

        publishEvents(savedCollections = savedCollections, accessToken = accessToken)
    }

    private suspend fun fetchExistingData(artistDTOs: List<ArtistDTO>, trackDTOs: List<TrackDTO>, albumDTOs: List<AlbumDTO>): ExistingData = coroutineScope {
        val artistsDeferred = async {
            artistService.findExistingArtists(
                artistDTOs.map { it.id }.toSet() +
                        albumDTOs.flatMap { it.artists.map { it.id } + it.tracks.items.flatMap { it.artists.map { it.id } } }.toSet() +
                        trackDTOs.flatMap { it.album.artists.map { it.id } + it.artists.map { it.id } }.toSet()
            )
        }
        val tracksDeferred = async {
            trackService.findExistingTracks(trackDTOs.map { it.id }.toSet() + albumDTOs.flatMap { it.tracks.items.map { it.id } }.toSet())
        }
        val albumsDeferred = async {
            albumService.findExistingAlbum(albumDTOs.map { it.id }.toSet() + trackDTOs.map { it.album.id }.toSet())
        }
        val albumArtistsDeferred = async {
            albumArtistService.findExistingAlbumArtists(
                albumDTOs.flatMap { album -> album.artists.map { artist -> album.id to artist.id } }.toSet() +
                    trackDTOs.flatMap { trackDTO -> trackDTO.album.artists.map { artist -> trackDTO.album.id to artist.id } }.toSet()
            )
        }
        val trackArtistsDeferred = async {
            trackArtistService.findExistingTrackArtists(
                trackDTOs.flatMap { track -> track.artists.map { artist -> track.id to artist.id } }.toSet() +
                    albumDTOs.flatMap { album -> album.tracks.items.flatMap { track -> track.artists.map { artist -> track.id to artist.id } } }.toSet()
            )
        }
        val artistImagesDeferred = async {
            artistImageService.findExistingArtistImages(artistDTOs.map { it.id to it.images }.toSet())
        }
        val artistGenresDeferred = async {
            artistGenreService.findExistingArtistGenres(artistDTOs.map { it.id to it.genres }.toSet())
        }
        val albumImagesDeferred = async {
            albumImageService.findExistingAlbumImages(
                albumDTOs.map { it.id to it.images }.toSet() + trackDTOs.map { track -> track.album.id to track.album.images }.toSet()
            )
        }

        ExistingData(
            artists = artistsDeferred.await().toSet(),
            tracks = tracksDeferred.await().toSet(),
            albums = albumsDeferred.await().toSet(),
            albumArtists = albumArtistsDeferred.await().toSet(),
            trackArtists = trackArtistsDeferred.await().toSet(),
            artistImages = artistImagesDeferred.await().toSet(),
            artistGenres = artistGenresDeferred.await().toSet(),
            albumImages = albumImagesDeferred.await().toSet()
        )
    }

    private suspend fun saveData(saveCollections: SaveCollections): SaveCollections = coroutineScope {
        awaitAll(
            async { albumService.upsertAlbums(saveCollections.albums) },
            async { artistService.upsertArtists(saveCollections.artists) }
        )

        trackService.upsertTracks(saveCollections.tracks)

        awaitAll(
            async { albumImageService.persistAlbumImages(saveCollections.albumImages) },
            async { artistImageService.persistArtistImage(saveCollections.artistImages) },
            async { artistGenreService.persistArtistGenres(saveCollections.artistGenres) },
            async { albumArtistService.persistAlbumArtists(saveCollections.albumArtists) },
            async { trackArtistService.persistTrackArtists(saveCollections.trackArtists) }
        )

        saveCollections
    }

    private fun publishEvents(savedCollections: SaveCollections, accessToken: String) {
        eventPublisher.publishEvent(savedCollections.albums.filter { it.isSimpleAlbum() }.map { it.spotifyId }
            .let { EnrichAlbumEvent(eventId = UUID.randomUUID(), albumIds = it.toSet(), metadata = EnrichMetadata(accessToken = accessToken)) })
        eventPublisher.publishEvent(savedCollections.artists.filter { it.isSimpleArtist() }.map { it.spotifyId }
            .let { EnrichArtistEvent(eventId = UUID.randomUUID(), artistIds = it.toSet(), metadata = EnrichMetadata(accessToken = accessToken)) })
        eventPublisher.publishEvent(savedCollections.tracks.filter { it.isSimpleTrack() }.map { it.spotifyId }
            .let { EnrichTrackEvent(eventId = UUID.randomUUID(), trackIds = it.toSet(), metadata = EnrichMetadata(accessToken = accessToken)) })
    }

}

data class ExistingData(
    val artists: Set<Artist>,
    val tracks: Set<Track>,
    val albums: Set<Album>,
    val albumArtists: Set<AlbumArtist>,
    val trackArtists: Set<TrackArtist>,
    val artistImages: Set<ArtistImage>,
    val artistGenres: Set<ArtistGenre>,
    val albumImages: Set<AlbumImage>
)