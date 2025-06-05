package org.danila.util.reactive.kafka

import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverRecord
import reactor.kafka.sender.SenderResult
import reactor.util.retry.Retry
import java.time.Duration

fun defaultRetry(): Retry = Retry
    .backoff(3, Duration.ofSeconds(8))
    .doAfterRetry { r ->
        val delay = r.retryContextView().getOrDefault<Long>("backoffDelay", 0L)
        println("Retry #${r.totalRetries()}, delay: ${delay}s")
        println(r.failure().message)
    }

fun <T> ReactiveKafkaProducerTemplate<String, Any>.sendToDlt(
    record: ReceiverRecord<String, T>
): Mono<Unit> {
    return this.send("${record.topic()}.DLT", record.partition(), record.key(), record.value())
        .doOnError { println("Failed to send to DLT: ${it.message}") }
        .thenReturn(Unit)
}

fun <T, V> Mono<SenderResult<V>>.sendToDltOnError(
    record: ReceiverRecord<String, T>,
    kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
): Mono<Void> {
    return this.then()
        .onErrorResume {
            kafkaTemplate
                .send("${record.topic()}.DLT", record.partition(), record.key(), record.value())
                .doOnError { e -> println("Failed to send to DLT ${e.message}") }
                .then()
        }
}