package org.danila.consumer

import event.AccessTokenUpdatedEvent
import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.future.await
import org.danila.configuration.USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC
import org.danila.services.api.spotify.SpotifyAuthService
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

    private val userConnectedConsumerTemplate: ReactiveKafkaConsumerTemplate<String, UserConnectedEvent>
) {

    suspend fun handleUserConnectedEvent(event: UserConnectedEvent) {
        println("${Instant.now()}: Получено событие: ${event.eventId}") // TODO: logs

        var accessToken = event.metadata.accessToken

        if (spotifyAuthService.isAccessTokenExpired(event.metadata.expiresAt))
            accessToken = getAccessToken(event = event)

        // spotifyService.fetchSpotifyData(accessToken = accessToken)
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

}