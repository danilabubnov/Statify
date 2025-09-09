package org.danila.consumer

import constants.kafka.KafkaTopics.ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC
import constants.kafka.KafkaTopics.USER_SPOTIFY_CONNECTED_TOPIC
import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import logging.logger
import org.danila.event.enrich.EnrichEvent
import org.danila.event.enrich.EnrichExtensions.functionName
import org.danila.event.scheduled.albums.PendingAlbumBatchEvent
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
    private val pendingAlbumConsumer: ReactiveKafkaConsumerTemplate<String, PendingAlbumBatchEvent>,
    private val enrichConsumer: ReactiveKafkaConsumerTemplate<String, Any>,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val metricsInterceptor: CoroutineMetricsInterceptor,
    private val userConnectedHandler: UserConnectedHandler,
    private val pendingAlbumsHandler: PendingAlbumsHandler,
    private val enrichHandler: EnrichHandler
) {

    private val logger by logger()

    @PostConstruct
    fun startConsumers() {
        logger.info { "Starting consumers..." }
        consumeUserConnected()
        consumeEnrichWaves()
        consumePendingAlbums()
    }

    private fun consumeUserConnected() {
        logger.info { "consumeUserConnected(): subscribing to topic=$USER_SPOTIFY_CONNECTED_TOPIC" }

        userConnectedConsumer
            .receive()
            .flatMap(
                { rec ->
                    logger.debug { "Received UserConnectedEvent: userId=${rec.value().userId}" }

                    mono(Dispatchers.Default + metricsInterceptor + CoroutineName("consume_user_connected")) {
                        userConnectedHandler.handle(rec.value())
                    }
                        .retryWhen(defaultRetry())
                        .onErrorResume { ex ->
                            logger.error(ex) { "Failed to process UserConnectedEvent: userId=${rec.value().userId}" }
                            kafkaTemplate.sendToDlt(rec)
                        }
                        .then(rec.receiverOffset().commit())
                },
                3
            )
            .subscribe(
                {},
                { ex -> logger.error(ex) { "Fatal error in consumer stream for topic=$USER_SPOTIFY_CONNECTED_TOPIC" } }
            )
    }

    private fun consumeEnrichWaves() {
        logger.info { "consumeEnrichWaves(): subscribing to topic=*_ENRICH_TOPIC" }

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
                                logger.debug { "Received EnrichEvent: correlationId=${evt.metadata.correlationId}, generation=${evt.metadata.generation}" }

                                mono(Dispatchers.Default + metricsInterceptor + CoroutineName(evt.functionName())) {
                                    enrichHandler.handle(rec.value() as EnrichEvent)
                                }
                                    .retryWhen(defaultRetry())
                                    .onErrorResume { ex ->
                                        logger.error(ex) { "Failed to process EnrichEvent: correlationId=${evt.metadata.correlationId}" }
                                        kafkaTemplate.sendToDlt(rec)
                                    }
                                    .then(rec.receiverOffset().commit())
                            }
                        }
                },
                4
            )
            .subscribe(
                {},
                { ex -> logger.error(ex) { "Fatal error in consumer stream for topic=*_ENRICH_TOPIC" } }
            )
    }

    private fun consumePendingAlbums() {
        logger.info { "consumePendingAlbums(): subscribing to topic=$ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC" }

        pendingAlbumConsumer
            .receive()
            .flatMap(
                { rec ->
                    logger.debug { "Received PendingAlbumBatchEvent: eventId=${rec.value().eventId}, size=${rec.value().ids.size}" }

                    mono(Dispatchers.Default + metricsInterceptor + CoroutineName("consume_pending_albums")) {
                        pendingAlbumsHandler.handle(rec.value())
                    }
                        .retryWhen(defaultRetry())
                        .onErrorResume { ex ->
                            logger.error(ex) { "Failed to process PendingAlbumBatchEvent: eventId=${rec.value().eventId}" }
                            kafkaTemplate.sendToDlt(rec)
                        }
                        .then(rec.receiverOffset().commit())
                },
                1
            )
            .subscribe(
                {},
                { ex -> logger.error(ex) { "Fatal error in consumer stream for topic=$ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC" } }
            )
    }

}