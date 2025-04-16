package org.danila.consumer

import event.AccessTokenUpdatedEvent
import org.danila.configuration.USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC
import org.danila.service.SpotifyInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class UserSpotifyAccessTokenUpdatedListener @Autowired constructor(
    private val spotifyInfoService: SpotifyInfoService
){

    @KafkaListener(topics = [USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC])
    fun handleUserSpotifyAccessTokenUpdatedEvent(event: AccessTokenUpdatedEvent) {
        println("Получено событие: ${event.eventId}") // TODO: logs

        val spotifyInfo = spotifyInfoService.findBySpotifyIdOrNull(event.spotifyId) ?: throw IllegalArgumentException("SpotifyInfo with id ${event.spotifyId} not found")

        spotifyInfoService.update(spotifyInfo.copy(accessToken = event.accessToken))
    }

}