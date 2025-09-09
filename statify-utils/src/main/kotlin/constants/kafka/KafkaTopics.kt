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
     * Kafka topic for albums that have a null release group and need to be resolved
     * via external metadata providers (e.g., MusicBrainz).
     */
    const val ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC = "statify.album.mb-release-group.resolve.v1"

    /**
     * Dead-letter topic for album release group resolution errors.
     */
    const val ALBUM_MB_RELEASE_GROUP_RESOLVE_DLT = "$ALBUM_MB_RELEASE_GROUP_RESOLVE_TOPIC.DLT"

}