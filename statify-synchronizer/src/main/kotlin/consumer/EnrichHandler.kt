package org.danila.consumer

import org.danila.event.enrich.EnrichEvent
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EnrichHandler @Autowired constructor(
    private val spotifyService: SpotifyService,
) {

    suspend fun handle(evt: EnrichEvent) {
        spotifyService.enrich(evt)
    }

}