package org.danila.configuration.constants.spotify

object SpotifyBatchConfig {

    /**
     * Capacity of the in-memory buffer for the Flow of saved tracks before batching.
     */
    const val SAVED_TRACKS_FLOW_BUFFER_CAPACITY = SpotifyApiConstants.MAX_SAVED_TRACKS_PAGE_SIZE * 4

    /**
     * Number of saved tracks to accumulate before writing a batch to the database.
     */
    const val SAVED_TRACKS_BATCH_SIZE = SAVED_TRACKS_FLOW_BUFFER_CAPACITY / 2

    /**
     * Capacity of the in-memory buffer for the Flow of “several tracks” results before batching.
     */
    const val MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY = SpotifyApiConstants.MAX_TRACKS_PER_MULTI_FETCH * 4

    /**
     * Number of tracks from multi-fetch calls to accumulate before writing a batch to the database.
     */
    const val MULTI_FETCH_TRACKS_BATCH_SIZE = MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY / 2

    /**
     * Capacity of the in-memory buffer for the Flow of followed-artist items before batching.
     */
    const val FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY = SpotifyApiConstants.MAX_FOLLOWED_ARTISTS_PAGE_SIZE * 4

    /**
     * Number of followed artists to accumulate before writing a batch to the database.
     */
    const val FOLLOWED_ARTISTS_BATCH_SIZE = FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY / 2

    /**
     * Capacity of the in-memory buffer for the Flow of “several artists” results before batching.
     */
    const val MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY = SpotifyApiConstants.MAX_ARTISTS_PER_MULTI_FETCH * 4

    /**
     * Number of artists from multi-fetch calls to accumulate before writing a batch to the database.
     */
    const val MULTI_FETCH_ARTISTS_BATCH_SIZE = MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY / 2

    /**
     * Capacity of the in-memory buffer for the Flow of saved-album items before batching.
     */
    const val SAVED_ALBUMS_FLOW_BUFFER_CAPACITY = SpotifyApiConstants.MAX_SAVED_ALBUMS_PAGE_SIZE * 4

    /**
     * Number of saved albums to accumulate before writing a batch to the database.
     */
    const val SAVED_ALBUMS_BATCH_SIZE = SAVED_ALBUMS_FLOW_BUFFER_CAPACITY / 2

    /**
     * Capacity of the in-memory buffer for the Flow of “several albums” results before batching.
     */
    const val MULTI_FETCH_ALBUMS_FLOW_BUFFER_CAPACITY = SpotifyApiConstants.MAX_ALBUMS_PER_MULTI_FETCH * 4

    /**
     * Number of albums from multi-fetch calls to accumulate before writing a batch to the database.
     */
    const val MULTI_FETCH_ALBUMS_BATCH_SIZE = MULTI_FETCH_ALBUMS_FLOW_BUFFER_CAPACITY / 2

    /**
     * Maximum time to wait (in milliseconds) before flushing any incomplete batch, regardless of size.
     */
    const val BATCH_TIMEOUT_MS = 1_000L

    /**
     * Maximum number of entities to process in a single chunk when upserting or fetching data.
     */
    const val MAX_SAVED_ENTITIES_CHUNK_SIZE = 50

}