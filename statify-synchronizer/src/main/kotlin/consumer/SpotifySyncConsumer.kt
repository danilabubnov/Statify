package org.danila.consumer

import event.UserConnectedEvent
import jakarta.annotation.PostConstruct
import org.danila.event.enrich.EnrichEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component

@Component
class SpotifySyncConsumer @Autowired constructor(
    private val userConnectedConsumer: ReactiveKafkaConsumerTemplate<String, UserConnectedEvent>,
    private val enrichConsumer: ReactiveKafkaConsumerTemplate<String, Any>,
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
            .flatMap({ rec ->
                println("event consumed: ${rec.value().eventId}")
                userConnectedHandler.handle(rec)
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
                        wave.flatMap { (evt, rec) ->
                            enrichHandler.handle(evt, rec)
                        }
                    }
            }, 4)
            .subscribe()
    }

}