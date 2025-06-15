package org.danila.consumer

import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.danila.event.enrich.EnrichEvent
import org.danila.event.enrich.EnrichExtensions.functionName
import org.danila.metrics.coroutine.CoroutineMetricsInterceptor
import org.danila.util.reactive.kafka.defaultRetry
import org.danila.util.reactive.kafka.sendToDlt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component

@Component
class SpotifySyncConsumer @Autowired constructor(
    private val userConnectedConsumer: ReactiveKafkaConsumerTemplate<String, UserConnectedEvent>,
    private val enrichConsumer: ReactiveKafkaConsumerTemplate<String, Any>,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val metricsInterceptor: CoroutineMetricsInterceptor,
    private val userConnectedHandler: UserConnectedHandler,
    private val enrichHandler: EnrichHandler
) {

    @PostConstruct
    fun startConsumers() {
        consumeUserConnected()
        consumeEnrichWaves()
    }

    private fun consumeUserConnected() {
        userConnectedConsumer
            .receive()
            .flatMap(
                { rec ->
                    mono(Dispatchers.Default + metricsInterceptor + CoroutineName("consume_user_connected")) {
                        userConnectedHandler.handle(rec.value())
                    }
                        .retryWhen(defaultRetry())
                        .onErrorResume { ex ->
                            ex.printStackTrace()
                            kafkaTemplate.sendToDlt(rec)
                        }
                        .then(rec.receiverOffset().commit())
                },
                3
            )
            .subscribe()
    }

    private fun consumeEnrichWaves() {
        enrichConsumer
            .receive()
            .map { rec -> rec.value() as EnrichEvent to rec }
            .groupBy { (evt, _) -> evt.metadata.correlationId }
            .flatMapSequential(
                { group ->
                    group
                        .windowUntilChanged { (evt, _) -> evt.metadata.generation }
                        .concatMap { wave ->
                            wave.flatMap { (evt, rec) ->
                                mono(Dispatchers.Default + metricsInterceptor + CoroutineName(evt.functionName())) {
                                    enrichHandler.handle(rec.value() as EnrichEvent)
                                }
                                    .retryWhen(defaultRetry())
                                    .onErrorResume { ex ->
                                        ex.printStackTrace()
                                        kafkaTemplate.sendToDlt(rec)
                                    }
                                    .then(rec.receiverOffset().commit())
                            }
                        }
                },
                4
            )
            .subscribe()
    }

}