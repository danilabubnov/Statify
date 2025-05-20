package org.danila.services.spotify

import event.UserConnectedEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactor.awaitSingle
import org.danila.*
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.danila.event.*
import org.danila.model.spotify.AlbumArtist
import org.danila.model.spotify.TrackArtist
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumImage
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.model.spotify.artist.ArtistImage
import org.danila.model.spotify.track.Track
import org.danila.model.spotify.track.UserFavoriteTrack
import org.danila.services.api.spotify.SpotifyApiClient
import org.danila.services.model.spotify.*
import org.danila.util.EnrichmentMetadataElement
import org.danila.util.EnrichmentMetadataKey
import org.danila.util.UserIdElement
import org.danila.util.UserIdKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
class SpotifyService @Autowired constructor(
    private val userFavoriteAlbumService: UserFavoriteAlbumService,
    private val userFavoriteTrackService: UserFavoriteTrackService,
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
    private val tokenStore: TokenStore,

    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) {

    suspend fun fetchSpotifyData(event: UserConnectedEvent) {
        val enrichmentMetadata = EnrichMetadata(tokenCredentials = event.tokenCredentials, correlationId = event.eventId.toString(), generation = 0)

        tokenStore.put(userId = event.userId, creds = event.tokenCredentials)

        withContext(UserIdElement(event.userId) + EnrichmentMetadataElement(enrichmentMetadata)) {
            coroutineScope {
                launch {
                    spotifyApiClient.getAllFollowedArtists()
                        .flowOn(Dispatchers.IO)
                        .buffer(FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY)
                        .batchWithTimeout(FOLLOWED_ARTISTS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                        .collect {
                            processInitialSpotifyData(
                                artistDTOs = it,
                                trackDTOs = emptyList(),
                                albumDTOs = emptyList()
                            )
                        }
                }

                launch {
                    spotifyApiClient.getAllSavedTracks()
                        .flowOn(Dispatchers.IO)
                        .buffer(SAVED_TRACKS_FLOW_BUFFER_CAPACITY)
                        .batchWithTimeout(SAVED_TRACKS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                        .collect {
                            processInitialSpotifyData(
                                artistDTOs = emptyList(),
                                trackDTOs = it,
                                albumDTOs = emptyList()
                            )
                        }
                }

                launch {
                    spotifyApiClient.getAllSavedAlbums()
                        .flowOn(Dispatchers.IO)
                        .buffer(SAVED_ALBUMS_FLOW_BUFFER_CAPACITY)
                        .batchWithTimeout(SAVED_ALBUMS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                        .collect {
                            processInitialSpotifyData(
                                artistDTOs = emptyList(),
                                trackDTOs = emptyList(),
                                albumDTOs = it
                            )
                        }
                }
            }
        }
    }

    suspend fun enrich(event: EnrichEvent) {
        val enrichmentMetadata = event.metadata.copy(generation = event.metadata.generation + 1)

        withContext(UserIdElement(event.userId) + EnrichmentMetadataElement(enrichmentMetadata)) {
            when (event) {
                is EnrichArtistEvent -> {
                    launch {
                        spotifyApiClient.getSeveralArtists(artistIds = event.artistIds)
                            .flowOn(Dispatchers.IO)
                            .buffer(MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY)
                            .batchWithTimeout(MULTI_FETCH_ARTISTS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                            .collect {
                                processEnrichmentSpotifyData(
                                    artistDTOs = it,
                                    trackDTOs = emptyList(),
                                    albumDTOs = emptyList()
                                )
                            }
                    }
                }

                is EnrichTrackEvent -> {
                    launch {
                        spotifyApiClient.getSeveralTracks(trackIds = event.trackIds)
                            .flowOn(Dispatchers.IO)
                            .buffer(MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY)
                            .batchWithTimeout(MULTI_FETCH_TRACKS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                            .collect {
                                processEnrichmentSpotifyData(
                                    artistDTOs = emptyList(),
                                    trackDTOs = it,
                                    albumDTOs = emptyList()
                                )
                            }
                    }
                }

                is EnrichAlbumEvent -> {
                    launch {
                        spotifyApiClient.getSeveralAlbums(albumIds = event.albumIds)
                            .flowOn(Dispatchers.IO)
                            .buffer(MULTI_FETCH_ALBUMS_FLOW_BUFFER_CAPACITY)
                            .batchWithTimeout(MULTI_FETCH_ALBUMS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                            .collect {
                                processEnrichmentSpotifyData(
                                    artistDTOs = emptyList(),
                                    trackDTOs = emptyList(),
                                    albumDTOs = it
                                )
                            }
                    }
                }
            }
        }
    }

    private suspend fun processInitialSpotifyData(
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<SavedTrackItemDTO>,
        albumDTOs: List<SavedAlbumItemDTO>
    ) {
        val userId = coroutineContext[UserIdKey]?.userId ?: throw IllegalStateException("No userId found")

        val existingData = fetchExistingData(userId = userId, artistDTOs = artistDTOs, trackDTOs = trackDTOs.map { it.track }, albumDTOs = albumDTOs.map { it.album })
        val saveCollections = withContext(Dispatchers.Default) {
            spotifyDataProcessor.processData(userId = userId, artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs, existingData = existingData)
        }

        saveData(saveCollections = saveCollections)
    }

    private suspend fun processEnrichmentSpotifyData(
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<TrackDTO>,
        albumDTOs: List<AlbumDTO>
    ) {
        val existingData = fetchExistingData(userId = null, artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs)
        val saveCollections = spotifyDataProcessor.processData(artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs, existingData = existingData)

        saveData(saveCollections = saveCollections)
    }

    private suspend fun fetchExistingData(userId: UUID?, artistDTOs: List<ArtistDTO>, trackDTOs: List<TrackDTO>, albumDTOs: List<AlbumDTO>): ExistingData = coroutineScope {
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
        val userFavoriteTracksDeferred = async { if (userId != null) userFavoriteTrackService.findExistingUserFavoriteTracks(userId) else emptyList() }
        val userFavoriteAlbumsDeferred = async { if (userId != null) userFavoriteAlbumService.findExistingUserFavoriteAlbums(userId) else emptyList() }

        ExistingData(
            artists = artistsDeferred.await().toSet(),
            tracks = tracksDeferred.await().toSet(),
            albums = albumsDeferred.await().toSet(),
            albumArtists = albumArtistsDeferred.await().toSet(),
            trackArtists = trackArtistsDeferred.await().toSet(),
            artistImages = artistImagesDeferred.await().toSet(),
            artistGenres = artistGenresDeferred.await().toSet(),
            albumImages = albumImagesDeferred.await().toSet(),
            userFavoriteTracks = userFavoriteTracksDeferred.await().toSet(),
            userFavoriteAlbums = userFavoriteAlbumsDeferred.await().toSet()
        )
    }

    private suspend fun saveData(saveCollections: SaveCollections) {
        coroutineScope {
            val (albums, artists) = awaitAll(
                async { albumService.upsertAndReturnSimpleAlbums(saveCollections.albums) },
                async { artistService.upsertAndReturnSimpleArtists(saveCollections.artists) }
            )

            val tracks = trackService.upsertAndReturnSimpleTracks(saveCollections.tracks)

            val jobs = listOf(
                launch { albumImageService.persistAlbumImages(saveCollections.albumImages) },
                launch { artistImageService.persistArtistImage(saveCollections.artistImages) },
                launch { artistGenreService.persistArtistGenres(saveCollections.artistGenres) },
                launch { albumArtistService.persistAlbumArtists(saveCollections.albumArtists) },
                launch { trackArtistService.persistTrackArtists(saveCollections.trackArtists) },
                launch { userFavoriteTrackService.persistUserFavoriteTracks(saveCollections.userFavoriteTracks) },
                launch { userFavoriteAlbumService.persistUserFavoriteAlbums(saveCollections.userFavoriteAlbums) }
            )

            jobs.joinAll()

            sendEnrichEvents(
                simpleAlbums = albums,
                simpleArtists = artists,
                simpleTracks = tracks
            )
        }
    }

    private suspend fun sendEnrichEvents(
        simpleAlbums: Collection<String>,
        simpleArtists: Collection<String>,
        simpleTracks: Collection<String>
    ) {
        val userId = coroutineContext[UserIdKey]?.userId ?: throw IllegalStateException("No userId found")
        val enrichmentMetadata = coroutineContext[EnrichmentMetadataKey]?.metadata ?: throw IllegalStateException("No enrichment metadata found")

        val jobs = mutableListOf<Deferred<Any>>()

        coroutineScope {
            if (simpleAlbums.isNotEmpty())
                jobs += async {
                    kafkaTemplate.send(
                        ALBUM_ENRICH_TOPIC,
                        EnrichAlbumEvent(eventId = UUID.randomUUID(), albumIds = simpleAlbums.toSet(), metadata = enrichmentMetadata, userId = userId)
                    ).doOnError { println("Failed to send enrich event ${it.message}") }
                        .awaitSingle()
                }

            if (simpleArtists.isNotEmpty())
                jobs += async {
                    kafkaTemplate.send(
                        ARTIST_ENRICH_TOPIC,
                        EnrichArtistEvent(eventId = UUID.randomUUID(), artistIds = simpleArtists.toSet(), metadata = enrichmentMetadata, userId = userId)
                    ).doOnError { println("Failed to send enrich event ${it.message}") }
                        .awaitSingle()
                }

            if (simpleTracks.isNotEmpty())
                jobs += async {
                    kafkaTemplate.send(
                        TRACK_ENRICH_TOPIC,
                        EnrichTrackEvent(eventId = UUID.randomUUID(), trackIds = simpleTracks.toSet(), metadata = enrichmentMetadata, userId = userId)
                    ).doOnError { println("Failed to send enrich event ${it.message}") }
                        .awaitSingle()
                }

            jobs.awaitAll()
        }
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
    val albumImages: Set<AlbumImage>,
    val userFavoriteTracks: Set<UserFavoriteTrack>,
    val userFavoriteAlbums: Set<UserFavoriteAlbum>
)