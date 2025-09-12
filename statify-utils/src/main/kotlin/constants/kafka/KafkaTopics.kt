package constants.kafka

object KafkaTopics {

    /**
     * Kafka topic for signaling that a user has connected their Spotify account.
     */
    const val USER_SPOTIFY_CONNECTED_TOPIC = "user.spotify.connected.v1"

    /**
     * Dead-letter topic for user spotify connected errors.
     */
    const val USER_SPOTIFY_CONNECTED_DLT = "$USER_SPOTIFY_CONNECTED_TOPIC.DLT"

    /**
     * Kafka topic for updating the synchronization status of a user's Spotify library.
     */
    const val USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC = "user.spotify.library.status.updated.v1"

    /**
     * Dead-letter topic for user spotify library status errors.
     */
    const val USER_SPOTIFY_LIBRARY_STATUS_UPDATED_DLT = "$USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC.DLT"

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

    /**
     * Kafka topic that carries batches of album IDs which require MusicBrainz
     * release-group with barcode resolution.
     */
    const val ALBUM_RG_LOOKUP_BY_BARCODE_TOPIC = "statify.album.mb-release-group.resolve-with-barcode.v1"

    /**
     * Dead-letter topic for failures while processing PendingAlbumBatchEvent
     */
    const val ALBUM_RG_LOOKUP_BY_BARCODE_DLT = "$ALBUM_RG_LOOKUP_BY_BARCODE_TOPIC.DLT"

    /**
     * Kafka topic that carries batches of album IDs which require MusicBrainz
     * release-group with name resolution.
     */
    const val ALBUM_RG_LOOKUP_BY_NAME_TOPIC = "statify.album.mb-release-group.resolve-with-name.v1"

    /**
     * Dead-letter topic for failures while processing PendingAlbumBatchEvent
     */
    const val ALBUM_RG_LOOKUP_BY_NAME_DLT = "$ALBUM_RG_LOOKUP_BY_BARCODE_TOPIC.DLT"

}