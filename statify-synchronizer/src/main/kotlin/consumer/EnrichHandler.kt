package org.danila.consumer

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.danila.event.enrich.EnrichEvent
import org.danila.event.enrich.EnrichExtensions.functionName
import org.danila.metrics.coroutine.CoroutineMetricsInterceptor
import org.danila.services.spotify.SpotifyService
import org.danila.util.reactive.kafka.defaultRetry
import org.danila.util.reactive.kafka.sendToDlt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverRecord

@Component
class EnrichHandler @Autowired constructor(
    private val metricsInterceptor: CoroutineMetricsInterceptor,
    private val spotifyService: SpotifyService,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
) {

    fun handle(evt: EnrichEvent, rec: ReceiverRecord<String, *>): Mono<Void> {
        return mono(Dispatchers.Default + metricsInterceptor + CoroutineName(evt.functionName())) {
            spotifyService.enrich(evt)
        }.retryWhen(defaultRetry())
            .onErrorResume { it.printStackTrace(); kafkaTemplate.sendToDlt(rec) }
            .doOnSuccess { rec.receiverOffset().acknowledge() }
            .then()
    }

}