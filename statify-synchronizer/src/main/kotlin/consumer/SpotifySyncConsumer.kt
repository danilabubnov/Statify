package org.danila.consumer

import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.danila.event.EnrichAlbumEvent
import org.danila.event.EnrichArtistEvent
import org.danila.event.EnrichEvent
import org.danila.event.EnrichTrackEvent
import org.danila.metrics.coroutine.CoroutineMetricsInterceptor
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

@Component
class SpotifySyncConsumer @Autowired constructor(
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val coroutineMetricsInterceptor: CoroutineMetricsInterceptor,
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
                mono(Dispatchers.Default + coroutineMetricsInterceptor + CoroutineName("consume_user_connected")) {
                    val evt = rec.value()

                    println("UserConnectedEvent ${evt.eventId}")

                    spotifyService.fetchSpotifyData(evt)
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
                                val functionName = when (evt) {
                                    is EnrichArtistEvent -> "enrich_artists"
                                    is EnrichTrackEvent  -> "enrich_tracks"
                                    is EnrichAlbumEvent  -> "enrich_albums"
                                    else                 -> "enrich_unknown"
                                }

                                mono(Dispatchers.Default + coroutineMetricsInterceptor + CoroutineName(functionName)) {
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

}