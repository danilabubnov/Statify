package org.danila.consumer

import logging.logger
import org.danila.event.scheduled.albums.AlbumReleaseGroupBatchEvent
import org.danila.event.scheduled.albums.LookupType
import org.danila.services.musicbrainz.MusicBrainzService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AlbumReleaseGroupBatchHandler @Autowired constructor(
    private val musicBrainzService: MusicBrainzService
) {

    private val logger by logger()

    suspend fun handle(evt: AlbumReleaseGroupBatchEvent) {
        logger.info { "Handling AlbumReleaseGroupBatchEvent: eventId=${evt.eventId}, size=${evt.ids.size}, type=${evt.lookupType.name}" }

        if (evt.lookupType == LookupType.BY_BARCODE)
            musicBrainzService.resolveReleaseGroupsForAlbumsByBarcode(albumIds = evt.ids)
        else musicBrainzService.resolveReleaseGroupsForAlbumsByName(albumIds = evt.ids)

        logger.debug { "Handled AlbumReleaseGroupBatchEvent: eventId=${evt.eventId}" }
    }

}