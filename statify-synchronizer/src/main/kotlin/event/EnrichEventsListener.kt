package org.danila.event

import org.danila.configuration.ALBUM_ENRICH_TOPIC
import org.danila.configuration.ARTIST_ENRICH_TOPIC
import org.danila.configuration.TRACK_ENRICH_TOPIC
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EnrichEventsListener(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    @EventListener
    fun handleEnrichAlbumEvent(event: EnrichAlbumEvent) {
        kafkaTemplate.send(ALBUM_ENRICH_TOPIC, event)
    }

    @EventListener
    fun handleEnrichArtistEvent(event: EnrichArtistEvent) {
        kafkaTemplate.send(ARTIST_ENRICH_TOPIC, event)
    }

    @EventListener
    fun handleEnrichTrackEvent(event: EnrichTrackEvent) {
        kafkaTemplate.send(TRACK_ENRICH_TOPIC, event)
    }

}