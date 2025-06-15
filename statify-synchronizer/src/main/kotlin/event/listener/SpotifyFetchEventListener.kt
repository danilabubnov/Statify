package org.danila.event.listener

import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import event.UserLibraryStatus
import event.UserSpotifyLibraryStatusUpdatedEvent
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.event.spotifyfetch.SpotifyFetchFailedEvent
import org.danila.event.spotifyfetch.SpotifyFetchCompletedEvent
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @PreDestroy
    fun stop() {
        scope.cancel()
    }

    @EventListener
    fun onSpotifyFetchCompleted(event: SpotifyFetchCompletedEvent) {
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

        if (status != null)
            kafkaTemplate.send(
                USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
                UserSpotifyLibraryStatusUpdatedEvent(id = libraryId, status = status)
            ).doOnError { println("Failed to send enrich event ${it.message}") }
                .awaitSingleOrNull()
    }

    private suspend fun updateLibraryStatusOnFailure(generation: Int, libraryId: UUID, correlationId: String) {
        if (generation > 1) return
        if (generation == 1) redisStateService.deletePendingGen1(correlationId)

        kafkaTemplate.send(
            USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
            UserSpotifyLibraryStatusUpdatedEvent(id = libraryId, status = UserLibraryStatus.FAILED)
        ).doOnError { println("Failed to send enrich event ${it.message}") }
            .awaitSingleOrNull()
    }

}