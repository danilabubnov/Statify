package org.danila.configuration.constants.kafka

object KafkaTopics {

    /**
     * Kafka topic for signaling that a user has connected their Spotify account.
     */
    const val USER_SPOTIFY_CONNECTED_TOPIC = "user.spotify.connected.v1"

    /**
     * Kafka topic for requesting enrichment of artist data.
     */
    const val ARTIST_ENRICH_TOPIC = "statify.artist.enrich.v1"

    /**
     * Dead-letter topic for artist enrichment errors.
     */
    const val ARTIST_ENRICH_DLT = "$ARTIST_ENRICH_TOPIC.DLT"

    /**
     * Kafka topic for requesting enrichment of album data.
     */
    const val ALBUM_ENRICH_TOPIC = "statify.album.enrich.v1"

    /**
     * Dead-letter topic for album enrichment errors.
     */
    const val ALBUM_ENRICH_DLT = "$ALBUM_ENRICH_TOPIC.DLT"

    /**
     * Kafka topic for requesting enrichment of track data.
     */
    const val TRACK_ENRICH_TOPIC = "statify.track.enrich.v1"

    /**
     * Dead-letter topic for track enrichment errors.
     */
    const val TRACK_ENRICH_DLT = "$TRACK_ENRICH_TOPIC.DLT"

}