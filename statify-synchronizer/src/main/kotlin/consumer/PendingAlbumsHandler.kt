package org.danila.consumer

import logging.logger
import org.danila.event.scheduled.albums.PendingAlbumBatchEvent
import org.danila.services.musicbrainz.MusicBrainzService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PendingAlbumsHandler @Autowired constructor(
    private val musicBrainzService: MusicBrainzService
) {

    private val logger by logger()

    suspend fun handle(evt: PendingAlbumBatchEvent) {
        logger.info { "Handling PendingAlbumBatchEvent: eventId=${evt.eventId}, size=${evt.ids.size}" }

        musicBrainzService.resolveReleaseGroupsForAlbums(evt.ids)

        logger.debug { "Handled PendingAlbumBatchEvent: eventId=${evt.eventId}" }
    }

}