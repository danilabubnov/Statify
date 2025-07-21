package org.danila.util.reactive.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverRecord
import reactor.util.retry.Retry
import java.time.Duration

private val logger = KotlinLogging.logger("statify.synchronizer.util.reactive.kafka.ReactiveKafkaExtensions")

fun defaultRetry(): Retry = Retry
    .backoff(3, Duration.ofSeconds(8))
    .doAfterRetry { retrySignal ->
        val delay = retrySignal.retryContextView().getOrDefault<Long>("backoffDelay", 0L)
        logger.warn {
            "Retry #${retrySignal.totalRetries()} (backoff=${delay}s) — " +
                    "last error: ${retrySignal.failure()?.message}"
        }
    }

fun <T> ReactiveKafkaProducerTemplate<String, Any>.sendToDlt(
    record: ReceiverRecord<String, T>
): Mono<Unit> {
    val dltTopic = "${record.topic()}.DLT"
    return this
        .send(dltTopic, record.partition(), record.key(), record.value())
        .doOnSuccess {
            logger.info {
                "Successfully sent to DLT: topic=$dltTopic, " +
                        "partition=${record.partition()}, key=${record.key()}"
            }
        }
        .doOnError { ex ->
            logger.error(ex) {
                "Failed to send to DLT: topic=$dltTopic, " +
                        "partition=${record.partition()}, key=${record.key()}"
            }
        }
        .thenReturn(Unit)
}