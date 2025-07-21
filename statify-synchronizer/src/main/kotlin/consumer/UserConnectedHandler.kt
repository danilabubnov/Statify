package org.danila.consumer

import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import event.UserConnectedEvent
import event.UserLibraryStatus
import event.UserSpotifyLibraryStatusUpdatedEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import logging.logger
import org.danila.services.RedisStateService
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component

@Component
class UserConnectedHandler @Autowired constructor(
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val spotifyService: SpotifyService,
    private val redisStateService: RedisStateService,
) {

    private val logger by logger()

    suspend fun handle(evt: UserConnectedEvent) {
        logger.info { "Handling UserConnectedEvent: userId=${evt.userId}, spotifyLibraryId=${evt.userSpotifyLibraryId}" }

        try {
            withContext(Dispatchers.IO) {
                coroutineScope {
                    val job1 = launch {
                        redisStateService.putTokenCredentials(evt.userId, evt.tokenCredentials)
                    }

                    val job2 = launch {
                        kafkaTemplate.send(
                            USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
                            UserSpotifyLibraryStatusUpdatedEvent(
                                id = evt.userSpotifyLibraryId,
                                status = UserLibraryStatus.IN_PROGRESS
                            )
                        ).awaitSingleOrNull()
                    }

                    joinAll(job1, job2)
                }
            }

            logger.debug { "Stored credentials and published IN_PROGRESS event for userId=${evt.userId}" }

            spotifyService.fetchSpotifyData(evt)

            logger.info { "Completed UserConnectedEvent for userId=${evt.userId}" }
        } catch (ex: Exception) {
            withContext(Dispatchers.IO) {
                redisStateService.deleteTokenCredentials(evt.userId)
            }

            throw ex
        }
    }

}