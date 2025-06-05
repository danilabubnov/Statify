package org.danila.consumer

import event.UserSpotifyLibraryStatusUpdatedEvent
import org.danila.configuration.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import org.danila.service.model.spotify.UserSpotifyLibraryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class StatifyCoreConsumer @Autowired constructor(
    private val userSpotifyLibraryService: UserSpotifyLibraryService
){

    @KafkaListener(topics = [USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC])
    fun handleUserSpotifyLibraryStatusUpdated(event: UserSpotifyLibraryStatusUpdatedEvent) {
        userSpotifyLibraryService.updateStatus(id = event.id, status = event.status)
    }

}