package org.danila.scheduler

import constants.kafka.KafkaTopics.ALBUM_RG_LOOKUP_BY_BARCODE_TOPIC
import constants.kafka.KafkaTopics.ALBUM_RG_LOOKUP_BY_NAME_TOPIC
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import logging.logger
import org.danila.event.scheduled.albums.AlbumReleaseGroupBatchEvent
import org.danila.event.scheduled.albums.LookupType
import org.danila.services.model.spotify.storage.AlbumStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Component
class AlbumReleaseGroupLookupScheduler @Autowired constructor(
    private val scheduleCoroutine: CoroutineScope,
    private val albumStorageService: AlbumStorageService,

    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) {

    private val logger by logger()

    private val publishPendingLookupsRunning = AtomicBoolean(false)
    private val publishNameFallbackLookupsRunning = AtomicBoolean(false)

    @Scheduled(fixedDelay = 200_000)
    fun publishPendingLookups() {
        if (!publishPendingLookupsRunning.compareAndSet(false, true)) {
            logger.debug { "publishPendingLookups(): already running, skipping this tick" }
            return
        }

        scheduleCoroutine.launch(CoroutineName("publish_pending_lookups")) {
            logger.info { "publishPendingLookups(): started draining PENDING albums" }

            try {
                albumStorageService.claimPendingAlbums()
                    .collect { ids ->
                        if (ids.isNotEmpty()) {
                            val event = AlbumReleaseGroupBatchEvent(eventId = UUID.randomUUID(), ids = ids, lookupType = LookupType.BY_BARCODE)

                            kafkaTemplate.send(ALBUM_RG_LOOKUP_BY_BARCODE_TOPIC, event)
                                .doOnError { ex -> logger.error(ex) { "Failed to publish pending albums event: $event" } }
                                .awaitSingleOrNull()
                        }
                    }
            } finally {
                publishPendingLookupsRunning.set(false)
            }
        }
    }

    @Scheduled(fixedDelay = 200_000)
    fun publishNameFallbackLookups() {
        if (!publishNameFallbackLookupsRunning.compareAndSet(false, true)) {
            logger.debug { "publishNameFallbackLookups(): already running, skipping this tick" }
            return
        }

        scheduleCoroutine.launch(CoroutineName("publish_name_fallback_lookups")) {
            logger.info { "publishNameFallbackLookups(): started draining BARCODE_NOT_FOUND  albums" }

            try {
                albumStorageService.claimBarcodeNotFoundAlbums()
                    .collect { ids ->
                        if (ids.isNotEmpty()) {
                            val event = AlbumReleaseGroupBatchEvent(eventId = UUID.randomUUID(), ids = ids, lookupType = LookupType.BY_NAME)

                            kafkaTemplate.send(ALBUM_RG_LOOKUP_BY_NAME_TOPIC, event)
                                .doOnError { ex -> logger.error(ex) { "Failed to publish pending albums event: $event" } }
                                .awaitSingleOrNull()
                        }
                    }
            } finally {
                publishNameFallbackLookupsRunning.set(false)
            }
        }
    }

}