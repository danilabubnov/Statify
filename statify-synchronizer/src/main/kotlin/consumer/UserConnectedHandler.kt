package org.danila.consumer

import event.UserConnectedEvent
import event.UserLibraryStatus
import event.UserSpotifyLibraryStatusUpdatedEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.danila.configuration.constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import org.danila.services.spotify.SpotifyService
import org.danila.services.spotify.TokenStore
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
    private val spotifyService: SpotifyService,
    private val tokenStore: TokenStore,
){

    fun handle(rec: ReceiverRecord<String, UserConnectedEvent>): Mono<Void> {
        val evt = rec.value()

        return mono(Dispatchers.IO) {
            tokenStore.initInFlightCounter(evt.eventId.toString())
            tokenStore.put(evt.userId, evt.tokenCredentials)
            kafkaTemplate.send(
                USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC,
                UserSpotifyLibraryStatusUpdatedEvent(
                    id = evt.userSpotifyLibraryId,
                    status = UserLibraryStatus.IN_PROGRESS
                )
            ).sendToDltOnError(rec, kafkaTemplate).awaitSingle()
        }.then(
            mono(Dispatchers.Default) {
                spotifyService.fetchSpotifyData(evt)
            }.retryWhen(defaultRetry())
                .onErrorResume { kafkaTemplate.sendToDlt(rec) }
        ).onErrorResume {
            mono {
                tokenStore.delete(evt.userId)
                tokenStore.deleteInFlightCounter(evt.eventId.toString())
            }.then(kafkaTemplate.sendToDlt(rec))
        }.doOnSuccess { rec.receiverOffset().acknowledge() }
            .then()
    }

}