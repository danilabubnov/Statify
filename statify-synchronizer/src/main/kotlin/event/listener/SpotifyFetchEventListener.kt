package org.danila.event.listener

import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import event.UserLibraryStatus
import event.UserSpotifyLibraryStatusUpdatedEvent
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import logging.logger
import org.danila.event.spotifyfetch.SpotifyFetchCompletedEvent
import org.danila.event.spotifyfetch.SpotifyFetchFailedEvent
import org.danila.services.RedisStateService
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class SpotifyFetchEventListener(
    private val redisStateService: RedisStateService,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) {

    private val logger by logger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @PreDestroy
    fun stop() {
        scope.cancel()
    }

    @EventListener
    fun onSpotifyFetchCompleted(event: SpotifyFetchCompletedEvent) {
        logger.debug {
            "Spotify fetch completed: libraryId=${event.spotifyFetchContext.libraryId}, " +
                    "correlationId=${event.spotifyFetchContext.correlationId}, " +
                    "enrichmentRequired=${event.enrichmentRequired}, generation=${event.spotifyFetchContext.generation}"
        }

        scope.launch {
            updateLibraryStatusOnSuccess(
                enrichmentRequired = event.enrichmentRequired,
                generation = event.spotifyFetchContext.generation,
                libraryId = event.spotifyFetchContext.libraryId,
                correlationId = event.spotifyFetchContext.correlationId,
            )
        }
    }

    @EventListener
    fun onSpotifyFetchFailed(event: SpotifyFetchFailedEvent) {
        logger.debug {
            "Spotify fetch failed: libraryId=${event.spotifyFetchContext.libraryId}, " +
                    "correlationId=${event.spotifyFetchContext.correlationId}, " +
                    "generation=${event.spotifyFetchContext.generation}"
        }

        scope.launch {
            updateLibraryStatusOnFailure(
                generation = event.spotifyFetchContext.generation,
                libraryId = event.spotifyFetchContext.libraryId,
                correlationId = event.spotifyFetchContext.correlationId,
            )
        }
    }

    private suspend fun updateLibraryStatusOnSuccess(enrichmentRequired: Boolean, generation: Int, libraryId: UUID, correlationId: String) {
        if (generation > 1) return

        val status = when {
            enrichmentRequired && generation == 0 -> UserLibraryStatus.PARTIALLY_SYNCED
            !enrichmentRequired && generation == 0 -> UserLibraryStatus.COMPLETED
            generation == 1 && redisStateService.getPendingGen1(correlationId) == 0L -> UserLibraryStatus.COMPLETED
            else -> null
        }

        if (status != null) {
            logger.info { "Updating library status to $status for libraryId=$libraryId, correlationId=$correlationId" }

            kafkaTemplate.send(
                USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
                UserSpotifyLibraryStatusUpdatedEvent(id = libraryId, status = status)
            )
                .doOnError { ex ->
                    logger.error(ex) {
                        "Failed to send library status update (success) for " +
                                "libraryId=$libraryId, correlationId=$correlationId"
                    }
                }
                .awaitSingleOrNull()
        } else logger.debug { "No status update needed for libraryId=$libraryId, generation=$generation" }
    }

    private suspend fun updateLibraryStatusOnFailure(generation: Int, libraryId: UUID, correlationId: String) {
        if (generation > 1) return
        if (generation == 1) {
            logger.debug { "Clearing pending gen1 counter for correlationId=$correlationId due to failure" }
            redisStateService.deletePendingGen1(correlationId)
        }

        logger.info { "Updating library status to FAILED for libraryId=$libraryId, correlationId=$correlationId" }

        kafkaTemplate.send(
            USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
            UserSpotifyLibraryStatusUpdatedEvent(id = libraryId, status = UserLibraryStatus.FAILED)
        )
            .doOnError { ex ->
                logger.error(ex) {
                    "Failed to send library status update (failure) for " +
                            "libraryId=$libraryId, correlationId=$correlationId"
                }
            }
            .awaitSingleOrNull()
    }

}