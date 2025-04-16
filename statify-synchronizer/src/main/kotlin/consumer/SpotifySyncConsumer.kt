package org.danila.consumer

import event.AccessTokenUpdatedEvent
import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.future.await
import org.danila.configuration.USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC
import org.danila.event.EnrichAlbumEvent
import org.danila.event.EnrichArtistEvent
import org.danila.event.EnrichTrackEvent
import org.danila.services.api.spotify.SpotifyAuthService
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class SpotifySyncConsumer @Autowired constructor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val spotifyAuthService: SpotifyAuthService,
    private val spotifyService: SpotifyService,

    private val userConnectedConsumerTemplate: ReactiveKafkaConsumerTemplate<String, UserConnectedEvent>,
    private val artistEnrichConsumerTemplate: ReactiveKafkaConsumerTemplate<String, EnrichArtistEvent>,
    private val albumEnrichConsumerTemplate: ReactiveKafkaConsumerTemplate<String, EnrichAlbumEvent>,
    private val trackEnrichConsumerTemplate: ReactiveKafkaConsumerTemplate<String, EnrichTrackEvent>
) {

    suspend fun handleUserConnectedEvent(event: UserConnectedEvent) {
        println("${Instant.now()}: Получено событие: ${event.eventId}") // TODO: logs

        var accessToken = event.metadata.accessToken

        if (spotifyAuthService.isAccessTokenExpired(event.metadata.expiresAt))
            accessToken = getAccessToken(event = event)

        spotifyService.fetchSpotifyData(accessToken = accessToken)
    }

    suspend fun handleArtistEnrichEvent(event: EnrichArtistEvent) {
        println("${Instant.now()}: Получено событие: ${event.eventId} для обогащения артиста") // TODO: logs

        val accessToken = event.metadata.accessToken

        spotifyService.enrichArtists(accessToken = accessToken, artistIds = event.artistIds)
    }

    suspend fun handleAlbumEnrichEvent(event: EnrichAlbumEvent) {
        println("${Instant.now()}: Получено событие: ${event.eventId} для обогащения альбома") // TODO: logs

        val accessToken = event.metadata.accessToken

        spotifyService.enrichAlbums(accessToken = accessToken, albumIds = event.albumIds)
    }

    suspend fun handleTrackEnrichEvent(event: EnrichTrackEvent) {
        println("${Instant.now()}: Получено событие: ${event.eventId} для обогащения трека") // TODO: logs

        val accessToken = event.metadata.accessToken

        spotifyService.enrichTracks(accessToken = accessToken, trackIds = event.trackIds)
    }

    private suspend fun getAccessToken(event: UserConnectedEvent): String =
        if (spotifyAuthService.isAccessTokenExpired(event.metadata.expiresAt))
            spotifyAuthService.refreshAccessToken(event.metadata.refreshToken).also { accessToken ->
                sendTokenUpdateEvent(accessToken = accessToken, spotifyId = event.metadata.spotifyId)
            }
        else event.metadata.accessToken

    private suspend fun sendTokenUpdateEvent(accessToken: String, spotifyId: String) {
        kafkaTemplate.send(
            USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC,
            AccessTokenUpdatedEvent(eventId = UUID.randomUUID(), accessToken = accessToken, spotifyId = spotifyId)
        ).await()
    }

    @PostConstruct
    fun consumeUserConnectedEvents() {
        userConnectedConsumerTemplate
            .receive()
            .concatMap { record ->
                kotlinx.coroutines.reactor.mono {
                    handleUserConnectedEvent(record.value())
                        .also { record.receiverOffset().acknowledge() }
                }
            }
            .onErrorContinue { throwable, _ ->
                throwable.printStackTrace() // TODO: logs
            }
            .subscribe()
    }

    @PostConstruct
    fun consumeArtistEnrichEvents() {
        artistEnrichConsumerTemplate
            .receive()
            .concatMap { record ->
                kotlinx.coroutines.reactor.mono {
                    handleArtistEnrichEvent(record.value())
                        .also { record.receiverOffset().acknowledge() }
                }
            }
            .onErrorContinue { throwable, _ ->
                throwable.printStackTrace() // TODO: logs
            }
            .subscribe()
    }

    @PostConstruct
    fun consumeAlbumEnrichEvents() {
        albumEnrichConsumerTemplate
            .receive()
            .concatMap { record ->
                kotlinx.coroutines.reactor.mono {
                    handleAlbumEnrichEvent(record.value())
                        .also { record.receiverOffset().acknowledge() }
                }
            }
            .onErrorContinue { throwable, _ ->
                throwable.printStackTrace() // TODO: logs
            }
            .subscribe()
    }

    @PostConstruct
    fun consumeTrackEnrichEvents() {
        trackEnrichConsumerTemplate
            .receive()
            .concatMap { record ->
                kotlinx.coroutines.reactor.mono {
                    handleTrackEnrichEvent(record.value())
                        .also { record.receiverOffset().acknowledge() }
                }
            }
            .onErrorContinue { throwable, _ ->
                throwable.printStackTrace() // TODO: logs
            }
            .subscribe()
    }

}