package org.danila.scheduler

import constants.kafka.KafkaTopics.ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import logging.logger
import org.danila.event.scheduled.albums.PendingAlbumBatchEvent
import org.danila.services.model.spotify.storage.AlbumStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Component
class AlbumEnrichmentScheduler @Autowired constructor(
    private val scheduleCoroutine: CoroutineScope,
    private val albumStorageService: AlbumStorageService,

    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) {

    private val logger by logger()

    private val running = AtomicBoolean(false)

    @Scheduled(fixedDelay = 200_000)
    fun publishPendingAlbums() {
        if (!running.compareAndSet(false, true)) {
            logger.debug { "publishPendingAlbums(): already running, skipping this tick" }
            return
        }

        scheduleCoroutine.launch(CoroutineName("publish_pending_albums")) {
            logger.info { "publishPendingAlbums(): started draining PENDING albums" }

            try {
                albumStorageService.claimPendingBatch()
                    .collect { ids ->
                        if (ids.isNotEmpty()) {
                            val event = PendingAlbumBatchEvent(eventId = UUID.randomUUID(), ids = ids)

                            kafkaTemplate.send(ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC, event)
                                .doOnError { ex -> logger.error(ex) { "Failed to publish pending albums event: $event" } }
                                .awaitSingleOrNull()
                        }
                    }
            } finally {
                running.set(false)
            }
        }
    }

}