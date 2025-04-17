package org.danila.consumer

import event.AccessTokenUpdatedEvent
import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactor.mono
import org.danila.configuration.USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC
import org.danila.event.EnrichEvent
import org.danila.services.api.spotify.SpotifyAuthService
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.GroupedFlux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverRecord
import java.time.Instant
import java.util.*

@Component
class SpotifySyncConsumer @Autowired constructor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val spotifyAuthService: SpotifyAuthService,
    private val spotifyService: SpotifyService,

    private val userConnectedConsumer: ReactiveKafkaConsumerTemplate<String, UserConnectedEvent>,
    private val enrichConsumer: ReactiveKafkaConsumerTemplate<String, Any>,
) {

    @PostConstruct
    fun startConsumers() {
        consumeUserConnected()
        consumeEnrichWaves()
    }

    private fun consumeUserConnected() {
        userConnectedConsumer
            .receive()
            .flatMap<Void>({ rec ->
                mono {
                    val evt = rec.value()

                    println("${Instant.now()}: UserConnectedEvent ${evt.eventId}")

                    val token = getAccessToken(evt)

                    spotifyService.fetchSpotifyData(evt.copy(metadata = evt.metadata.copy(accessToken = token)))
                }.then(Mono.fromRunnable { rec.receiverOffset().acknowledge() })
            }, 3)
            .onErrorContinue { err, _ -> err.printStackTrace() }
            .subscribe()
    }

    private fun consumeEnrichWaves() {
        enrichConsumer
            .receive()
            .map { rec -> (rec.value() as EnrichEvent) to rec }
            .groupBy { (evt, _) -> evt.metadata.correlationId }
            .flatMapSequential({ group: GroupedFlux<String, Pair<EnrichEvent, ReceiverRecord<String, Any>>> ->
                group
                    .windowUntilChanged { (evt, _) -> evt.metadata.generation }
                    .concatMap { wave ->
                        wave
                            .flatMap<Void> { (evt, rec) ->
                                mono {
                                    println("${Instant.now()}: EnrichEvent ${evt.eventId} (gen=${evt.metadata.generation})")

                                    spotifyService.enrich(evt)
                                }.then(Mono.fromRunnable { rec.receiverOffset().acknowledge() })
                            }
                            .then()
                    }
            }, 4)
            .onErrorContinue { err, _ -> err.printStackTrace() }
            .subscribe()
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

}