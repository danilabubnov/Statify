package org.danila.consumer

import logging.logger
import org.danila.event.enrich.EnrichEvent
import org.danila.event.enrich.EnrichExtensions.functionName
import org.danila.services.spotify.SpotifyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EnrichHandler @Autowired constructor(
    private val spotifyService: SpotifyService,
) {

    private val logger by logger()

    suspend fun handle(evt: EnrichEvent) {
        logger.info { "Enriching event: eventId=${evt.eventId}, function=${evt.functionName()}" }

        try {
            spotifyService.enrich(evt)
            logger.info { "Enrich completed: eventId=${evt.eventId}" }
        } catch (ex: Exception) {
            throw ex
        }
    }

}