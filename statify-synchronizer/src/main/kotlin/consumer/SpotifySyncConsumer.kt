package org.danila.consumer

import event.AccessTokenUpdatedEvent
import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.danila.configuration.USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC
import org.danila.event.EnrichEvent
import org.danila.services.api.spotify.SpotifyAuthService
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.util.*

@Component
class SpotifySyncConsumer @Autowired constructor(
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
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
            .flatMap({ rec ->
                mono {
                    val evt = rec.value()

                    println("UserConnectedEvent ${evt.eventId}")

                    val token = getAccessToken(evt)

                    spotifyService.fetchSpotifyData(evt.copy(metadata = evt.metadata.copy(accessToken = token)))
                }
                    .then<Void>(Mono.fromRunnable { rec.receiverOffset().acknowledge() })
                    .retryWhen(
                        Retry.fixedDelay(3, Duration.ofSeconds(5))
                            .doAfterRetry { r ->
                                println(r.failure().message)
                                println(r.failure().stackTrace)
                                println("Retry #${r.totalRetries()} for ${rec.topic()}")
                            })
                    .onErrorResume { ex ->
                        kafkaTemplate
                            .send("${rec.topic()}.DLT", rec.partition(), rec.key(), rec.value())
                            .doOnError { println("Failed to send to DLT ${it.message}") }
                            .then(Mono.fromRunnable { rec.receiverOffset().acknowledge() })
                    }
            }, 3)
            .subscribe()
    }

    private fun consumeEnrichWaves() {
        enrichConsumer
            .receive()
            .map { rec -> (rec.value() as EnrichEvent) to rec }
            .groupBy { (evt, _) -> evt.metadata.correlationId }
            .flatMapSequential({ group ->
                group
                    .windowUntilChanged { (evt, _) -> evt.metadata.generation }
                    .concatMap { wave ->
                        wave
                            .flatMap { (evt, rec) ->
                                mono {
                                    println("EnrichEvent ${evt.eventId} gen=${evt.metadata.generation}")

                                    spotifyService.enrich(evt)
                                }
                                    .then<Void>(Mono.fromRunnable { rec.receiverOffset().acknowledge() })
                                    .retryWhen(
                                        Retry.fixedDelay(3, Duration.ofSeconds(5))
                                            .doAfterRetry { r ->
                                                println(r.failure().message)
                                                println(r.failure().stackTrace)
                                                println("Retry #${r.totalRetries()} for ${rec.topic()}")
                                            })
                                    .onErrorResume { ex ->
                                        kafkaTemplate
                                            .send("${rec.topic()}.DLT", rec.partition(), rec.key(), rec.value())
                                            .doOnError { println("Failed to send to DLT ${it.message}") }
                                            .then(Mono.fromRunnable { rec.receiverOffset().acknowledge() })
                                    }
                            }
                            .then()
                    }
            }, 4)
            .subscribe()
    }

    private suspend fun getAccessToken(event: UserConnectedEvent): String =
        if (spotifyAuthService.isAccessTokenExpired(event.metadata.expiresAt)) {
            val newToken = spotifyAuthService.refreshAccessToken(event.metadata.refreshToken)

            sendTokenUpdateEvent(newToken, event.metadata.spotifyId)

            newToken
        } else event.metadata.accessToken

    private suspend fun sendTokenUpdateEvent(accessToken: String, spotifyId: String) {
        kafkaTemplate.send(
            USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC, AccessTokenUpdatedEvent(
                eventId = UUID.randomUUID(),
                accessToken = accessToken,
                spotifyId = spotifyId
            )
        ).doOnError { println("Failed to send token update ${it.message}") }
            .awaitSingle()
    }

}