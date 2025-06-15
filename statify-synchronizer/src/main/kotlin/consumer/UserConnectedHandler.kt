package org.danila.consumer

import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import event.UserConnectedEvent
import event.UserLibraryStatus
import event.UserSpotifyLibraryStatusUpdatedEvent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.danila.metrics.coroutine.CoroutineMetricsInterceptor
import org.danila.services.RedisStateService
import org.danila.services.spotify.SpotifyService
import org.danila.util.reactive.kafka.defaultRetry
import org.danila.util.reactive.kafka.sendToDlt
import org.danila.util.reactive.kafka.sendToDltOnError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverRecord

@Component
class UserConnectedHandler @Autowired constructor(
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val metricsInterceptor: CoroutineMetricsInterceptor,
    private val spotifyService: SpotifyService,
    private val redisStateService: RedisStateService,
) {

    fun handle(rec: ReceiverRecord<String, UserConnectedEvent>): Mono<Void> {
        val evt = rec.value()

        return mono(Dispatchers.IO + metricsInterceptor + CoroutineName("fetch_spotify_data")) {
            redisStateService.putTokenCredentials(evt.userId, evt.tokenCredentials)
            kafkaTemplate.send(
                USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
                UserSpotifyLibraryStatusUpdatedEvent(
                    id = evt.userSpotifyLibraryId,
                    status = UserLibraryStatus.IN_PROGRESS
                )
            ).sendToDltOnError(rec, kafkaTemplate).awaitSingleOrNull()
        }.then(
            mono(Dispatchers.Default) {
                spotifyService.fetchSpotifyData(evt)
                rec.receiverOffset().acknowledge()
            }.retryWhen(defaultRetry())
                .onErrorResume { it.printStackTrace(); kafkaTemplate.sendToDlt(rec) }
        ).onErrorResume {
            it.printStackTrace()
            mono {
                redisStateService.deleteTokenCredentials(evt.userId)
            }.then(kafkaTemplate.sendToDlt(rec))
        }.then()
    }

}