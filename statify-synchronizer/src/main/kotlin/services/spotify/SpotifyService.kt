package org.danila.services.spotify

import constants.kafka.KafkaTopics.ALBUM_ENRICH_TOPIC
import constants.kafka.KafkaTopics.ARTIST_ENRICH_TOPIC
import constants.kafka.KafkaTopics.TRACK_ENRICH_TOPIC
import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import event.UserConnectedEvent
import event.UserLibraryStatus
import event.UserSpotifyLibraryStatusUpdatedEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactor.awaitSingle
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.BATCH_TIMEOUT_MS
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.FOLLOWED_ARTISTS_BATCH_SIZE
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MULTI_FETCH_ALBUMS_BATCH_SIZE
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MULTI_FETCH_ALBUMS_FLOW_BUFFER_CAPACITY
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MULTI_FETCH_ARTISTS_BATCH_SIZE
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MULTI_FETCH_TRACKS_BATCH_SIZE
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.SAVED_ALBUMS_BATCH_SIZE
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.SAVED_ALBUMS_FLOW_BUFFER_CAPACITY
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.SAVED_TRACKS_BATCH_SIZE
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.SAVED_TRACKS_FLOW_BUFFER_CAPACITY
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.SavedAlbumItemDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.track.SavedTrackItemDTO
import org.danila.dto.track.TrackDTO
import org.danila.event.*
import org.danila.metrics.Metrics
import org.danila.metrics.batchEmits
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
import org.danila.services.RedisStateService
import org.danila.services.api.spotify.client.SpotifyAlbumsClient
import org.danila.services.api.spotify.client.SpotifyArtistsClient
import org.danila.services.api.spotify.client.SpotifyTracksClient
import org.danila.services.model.spotify.storage.*
import org.danila.util.*
import org.danila.util.reactive.batchWithTimeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext

@Service
class SpotifyService @Autowired constructor(
    private val userFavoriteAlbumStorageService: UserFavoriteAlbumStorageService,
    private val userFavoriteTrackStorageService: UserFavoriteTrackStorageService,
    private val trackArtistStorageService: TrackArtistStorageService,
    private val albumArtistStorageService: AlbumArtistStorageService,
    private val artistGenreStorageService: ArtistGenreStorageService,
    private val artistImageStorageService: ArtistImageStorageService,
    private val albumImageStorageService: AlbumImageStorageService,
    private val artistStorageService: ArtistStorageService,
    private val albumStorageService: AlbumStorageService,
    private val trackStorageService: TrackStorageService,

    private val spotifyAlbumsClient: SpotifyAlbumsClient,
    private val spotifyArtistsClient: SpotifyArtistsClient,
    private val spotifyTracksClient: SpotifyTracksClient,

    private val spotifyDataProcessor: SpotifyDataProcessor,
    private val metrics: Metrics,
    private val redisStateService: RedisStateService,

    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) {

    suspend fun fetchSpotifyData(event: UserConnectedEvent) {
        val enrichmentMetadata = EnrichMetadata(tokenCredentials = event.tokenCredentials, correlationId = event.eventId.toString(), generation = 0)

        withContext(Dispatchers.Default + UserIdElement(event.userId) + EnrichmentMetadataElement(enrichmentMetadata) + UserSpotifyLibraryElement(event.userSpotifyLibraryId)) {
            coroutineScope {
                launch {
                    spotifyArtistsClient.getAllFollowedArtists()
                        .flowOn(Dispatchers.IO)
                        .buffer(FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY)
                        .batchWithTimeout(FOLLOWED_ARTISTS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                        .batchEmits(totalCounter = metrics.followedArtistsTotalCounter, timeoutCounter = metrics.followedArtistsTimeoutCounter)
                        .collect {
                            processInitialSpotifyData(
                                artistDTOs = it.result,
                                trackDTOs = emptyList(),
                                albumDTOs = emptyList()
                            )
                        }
                }

                launch {
                    spotifyTracksClient.getAllSavedTracks()
                        .flowOn(Dispatchers.IO)
                        .buffer(SAVED_TRACKS_FLOW_BUFFER_CAPACITY)
                        .batchWithTimeout(SAVED_TRACKS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                        .batchEmits(totalCounter = metrics.savedTracksTotalCounter, timeoutCounter = metrics.savedTracksTimeoutCounter)
                        .collect {
                            processInitialSpotifyData(
                                artistDTOs = emptyList(),
                                trackDTOs = it.result,
                                albumDTOs = emptyList()
                            )
                        }
                }

                launch {
                    spotifyAlbumsClient.getAllSavedAlbums()
                        .flowOn(Dispatchers.IO)
                        .buffer(SAVED_ALBUMS_FLOW_BUFFER_CAPACITY)
                        .batchWithTimeout(SAVED_ALBUMS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                        .batchEmits(totalCounter = metrics.savedAlbumsTotalCounter, timeoutCounter = metrics.savedAlbumsTimeoutCounter)
                        .collect {
                            processInitialSpotifyData(
                                artistDTOs = emptyList(),
                                trackDTOs = emptyList(),
                                albumDTOs = it.result
                            )
                        }
                }
            }
        }
    }

    suspend fun enrich(event: EnrichEvent) {
        val enrichmentMetadata = event.metadata.copy(generation = event.metadata.generation + 1)

        withContext(Dispatchers.Default + UserIdElement(event.userId) + EnrichmentMetadataElement(enrichmentMetadata)) {
            when (event) {
                is EnrichArtistEvent -> {
                    launch {
                        spotifyArtistsClient.getSeveralArtists(artistIds = event.artistIds)
                            .flowOn(Dispatchers.IO)
                            .buffer(MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY)
                            .batchWithTimeout(MULTI_FETCH_ARTISTS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                            .batchEmits(totalCounter = metrics.multiFetchArtistsTotalCounter, timeoutCounter = metrics.multiFetchArtistsTimeoutCounter)
                            .collect {
                                processEnrichmentSpotifyData(
                                    artistDTOs = it.result,
                                    trackDTOs = emptyList(),
                                    albumDTOs = emptyList()
                                )
                            }
                    }
                }

                is EnrichTrackEvent -> {
                    launch {
                        spotifyTracksClient.getSeveralTracks(trackIds = event.trackIds)
                            .flowOn(Dispatchers.IO)
                            .buffer(MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY)
                            .batchWithTimeout(MULTI_FETCH_TRACKS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                            .batchEmits(totalCounter = metrics.multiFetchTracksTotalCounter, timeoutCounter = metrics.multiFetchTracksTimeoutCounter)
                            .collect {
                                processEnrichmentSpotifyData(
                                    artistDTOs = emptyList(),
                                    trackDTOs = it.result,
                                    albumDTOs = emptyList()
                                )
                            }
                    }
                }

                is EnrichAlbumEvent -> {
                    launch {
                        spotifyAlbumsClient.getSeveralAlbums(albumIds = event.albumIds)
                            .flowOn(Dispatchers.IO)
                            .buffer(MULTI_FETCH_ALBUMS_FLOW_BUFFER_CAPACITY)
                            .batchWithTimeout(MULTI_FETCH_ALBUMS_BATCH_SIZE, BATCH_TIMEOUT_MS)
                            .batchEmits(totalCounter = metrics.multiFetchAlbumsTotalCounter, timeoutCounter = metrics.multiFetchAlbumsTimeoutCounter)
                            .collect {
                                processEnrichmentSpotifyData(
                                    artistDTOs = emptyList(),
                                    trackDTOs = emptyList(),
                                    albumDTOs = it.result
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
        val userId = coroutineContext[UserIdKey]?.userId ?: error("No userId found")

        val existingData = fetchExistingData(userId = userId, artistDTOs = artistDTOs, trackDTOs = trackDTOs.map { it.track }, albumDTOs = albumDTOs.map { it.album })
        val saveCollections = spotifyDataProcessor.processData(userId = userId, artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs, existingData = existingData)

        val simpleEntities = saveDataAndReturnSimpleEntities(saveCollections = saveCollections)

        if (simpleEntities.artists.isEmpty() && simpleEntities.tracks.isEmpty() && simpleEntities.albums.isEmpty())
            updateUserLibraryStatus(isFurtherEnrichmentRequired = false)
        else {
            sendEnrichEvents(simpleAlbums = simpleEntities.albums, simpleArtists = simpleEntities.artists, simpleTracks = simpleEntities.tracks)
            updateUserLibraryStatus(isFurtherEnrichmentRequired = true)
        }
    }

    private suspend fun processEnrichmentSpotifyData(
        artistDTOs: List<ArtistDTO>,
        trackDTOs: List<TrackDTO>,
        albumDTOs: List<AlbumDTO>
    ) {
        val enrichment = requireNotNull(coroutineContext[EnrichmentMetadataKey])
        val existingData = fetchExistingData(userId = null, artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs)
        val saveCollections = spotifyDataProcessor.processData(artistDTOs = artistDTOs, trackDTOs = trackDTOs, albumDTOs = albumDTOs, existingData = existingData)

        val simpleEntities = saveDataAndReturnSimpleEntities(saveCollections = saveCollections)

        if (enrichment.metadata.generation == 1)
            redisStateService.decrementCounterAndCheckIfDeleted(correlationId = enrichment.metadata.correlationId)

        if (simpleEntities.artists.isEmpty() && simpleEntities.tracks.isEmpty() && simpleEntities.albums.isEmpty() && enrichment.metadata.generation <= 1) {
            updateUserLibraryStatus(isFurtherEnrichmentRequired = false)
        }
        else {
            sendEnrichEvents(simpleAlbums = simpleEntities.albums, simpleArtists = simpleEntities.artists, simpleTracks = simpleEntities.tracks)
            if (enrichment.metadata.generation <= 1) updateUserLibraryStatus(isFurtherEnrichmentRequired = true)
        }
    }

    private suspend fun fetchExistingData(userId: UUID?, artistDTOs: List<ArtistDTO>, trackDTOs: List<TrackDTO>, albumDTOs: List<AlbumDTO>): ExistingData = coroutineScope {
        val artistsDeferred = async {
            artistStorageService.findExistingArtists(
                artistDTOs.map { it.id }.toSet() +
                        albumDTOs.flatMap { it.artists.map { it.id } + it.tracks.items.flatMap { it.artists.map { it.id } } }.toSet() +
                        trackDTOs.flatMap { it.album.artists.map { it.id } + it.artists.map { it.id } }.toSet()
            )
        }
        val tracksDeferred = async {
            trackStorageService.findExistingTracks(trackDTOs.map { it.id }.toSet() + albumDTOs.flatMap { it.tracks.items.map { it.id } }.toSet())
        }
        val albumsDeferred = async {
            albumStorageService.findExistingAlbum(albumDTOs.map { it.id }.toSet() + trackDTOs.map { it.album.id }.toSet())
        }
        val albumArtistsDeferred = async {
            albumArtistStorageService.findExistingAlbumArtists(
                albumDTOs.flatMap { album -> album.artists.map { artist -> album.id to artist.id } }.toSet() +
                        trackDTOs.flatMap { trackDTO -> trackDTO.album.artists.map { artist -> trackDTO.album.id to artist.id } }.toSet()
            )
        }
        val trackArtistsDeferred = async {
            trackArtistStorageService.findExistingTrackArtists(
                trackDTOs.flatMap { track -> track.artists.map { artist -> track.id to artist.id } }.toSet() +
                        albumDTOs.flatMap { album -> album.tracks.items.flatMap { track -> track.artists.map { artist -> track.id to artist.id } } }.toSet()
            )
        }
        val artistImagesDeferred = async {
            artistImageStorageService.findExistingArtistImages(artistDTOs.map { it.id to it.images }.toSet())
        }
        val artistGenresDeferred = async {
            artistGenreStorageService.findExistingArtistGenres(artistDTOs.map { it.id to it.genres }.toSet())
        }
        val albumImagesDeferred = async {
            albumImageStorageService.findExistingAlbumImages(
                albumDTOs.map { it.id to it.images }.toSet() + trackDTOs.map { track -> track.album.id to track.album.images }.toSet()
            )
        }
        val userFavoriteTracksDeferred = async { if (userId != null) userFavoriteTrackStorageService.findExistingUserFavoriteTracks(userId) else emptyList() }
        val userFavoriteAlbumsDeferred = async { if (userId != null) userFavoriteAlbumStorageService.findExistingUserFavoriteAlbums(userId) else emptyList() }

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

    private suspend fun saveDataAndReturnSimpleEntities(saveCollections: ConcurrentSaveCollections): SimplePersistedEntities {
        return coroutineScope {
            val (albums, artists) = awaitAll(
                async { albumStorageService.upsertAndReturnSimpleAlbums(saveCollections.albums) },
                async { artistStorageService.upsertAndReturnSimpleArtists(saveCollections.artists) }
            )

            val tracks = trackStorageService.upsertAndReturnSimpleTracks(saveCollections.tracks)

            val jobs = listOf(
                launch { albumImageStorageService.persistAlbumImages(saveCollections.albumImages) },
                launch { artistImageStorageService.persistArtistImage(saveCollections.artistImages) },
                launch { artistGenreStorageService.persistArtistGenres(saveCollections.artistGenres) },
                launch { albumArtistStorageService.persistAlbumArtists(saveCollections.albumArtists) },
                launch { trackArtistStorageService.persistTrackArtists(saveCollections.trackArtists) },
                launch { userFavoriteTrackStorageService.persistUserFavoriteTracks(saveCollections.userFavoriteTracks) },
                launch { userFavoriteAlbumStorageService.persistUserFavoriteAlbums(saveCollections.userFavoriteAlbums) }
            )

            jobs.joinAll()

            SimplePersistedEntities(
                albums = albums,
                artists = artists,
                tracks = tracks
            )
        }
    }

    private suspend fun sendEnrichEvents(
        simpleAlbums: Collection<String>,
        simpleArtists: Collection<String>,
        simpleTracks: Collection<String>
    ) {
        val userId = requireNotNull(coroutineContext[UserIdKey]).userId
        val enrichmentMetadata by lazy { suspend { requireNotNull(coroutineContext[EnrichmentMetadataKey]).metadata } }

        val jobs = mutableListOf<Deferred<Any>>()

        supervisorScope {
            withContext(Dispatchers.IO) {
                val initialGenEventsCount = AtomicLong(0)

                if (simpleAlbums.isNotEmpty()) {
                    val enrichmentMetadata = enrichmentMetadata()

                    jobs += async {
                        kafkaTemplate.send(
                            ALBUM_ENRICH_TOPIC,
                            EnrichAlbumEvent(eventId = UUID.randomUUID(), albumIds = simpleAlbums.toSet(), metadata = enrichmentMetadata, userId = userId)
                        ).doOnError { println("Failed to send enrich event ${it.message}") }
                            .doOnSuccess { if (enrichmentMetadata.generation == 0) initialGenEventsCount.incrementAndGet() }
                            .awaitSingle()
                    }
                }

                if (simpleArtists.isNotEmpty()) {
                    val enrichmentMetadata = enrichmentMetadata()

                    jobs += async {
                        kafkaTemplate.send(
                            ARTIST_ENRICH_TOPIC,
                            EnrichArtistEvent(eventId = UUID.randomUUID(), artistIds = simpleArtists.toSet(), metadata = enrichmentMetadata, userId = userId)
                        ).doOnError { println("Failed to send enrich event ${it.message}") }
                            .doOnSuccess { if (enrichmentMetadata.generation == 0) initialGenEventsCount.incrementAndGet() }
                            .awaitSingle()
                    }
                }

                if (simpleTracks.isNotEmpty()) {
                    val enrichmentMetadata = enrichmentMetadata()

                    jobs += async {
                        kafkaTemplate.send(
                            TRACK_ENRICH_TOPIC,
                            EnrichTrackEvent(eventId = UUID.randomUUID(), trackIds = simpleTracks.toSet(), metadata = enrichmentMetadata, userId = userId)
                        ).doOnError { println("Failed to send enrich event ${it.message}") }
                            .doOnSuccess { if (enrichmentMetadata.generation == 0) initialGenEventsCount.incrementAndGet() }
                            .awaitSingle()
                    }
                }

                jobs.awaitAll()

                redisStateService.incrementPendingGen1(correlationId = enrichmentMetadata().correlationId, delta = initialGenEventsCount.get())
            }
        }
    }

    private suspend fun updateUserLibraryStatus(isFurtherEnrichmentRequired: Boolean) {
        val metadata = requireNotNull(coroutineContext[EnrichmentMetadataKey]).metadata

        if (metadata.generation > 1) return

        val libraryId = requireNotNull(coroutineContext[UserSpotifyLibraryKey]).id

        val status = when {
            isFurtherEnrichmentRequired && metadata.generation == 0 -> UserLibraryStatus.PARTIALLY_SYNCED
            !isFurtherEnrichmentRequired && metadata.generation == 0 -> UserLibraryStatus.COMPLETED
            metadata.generation == 1 && redisStateService.getPendingGen1(metadata.correlationId) == 0L -> UserLibraryStatus.COMPLETED
            else -> null
        }

        if (status != null)
            kafkaTemplate.send(
                USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
                UserSpotifyLibraryStatusUpdatedEvent(id = libraryId, status = status)
            ).doOnError { println("Failed to send enrich event ${it.message}") }
                .awaitSingle()
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

data class SimplePersistedEntities(
    val albums: Collection<String>,
    val artists: Collection<String>,
    val tracks: Collection<String>
)