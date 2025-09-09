package org.danila.configuration.variables

import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_ALBUMS_PER_MULTI_FETCH
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_ARTISTS_PER_MULTI_FETCH
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_FOLLOWED_ARTISTS_PAGE_SIZE
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_SAVED_ALBUMS_PAGE_SIZE
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_SAVED_TRACKS_PAGE_SIZE
import org.danila.configuration.constants.spotify.SpotifyApiConstants.MAX_TRACKS_PER_MULTI_FETCH

// Using UPPER_SNAKE_CASE for val to stay consistent with const val config style.

object SpotifyChunkSizes {

    /**
     * Maximum number of tracks to process in a single chunk when upserting or fetching data.
     * Calculated as the maximum of Spotify's saved tracks page size and the multi-track fetch limit.
     * Ensures efficient and API-compliant batch processing of saved tracks.
     */
    val MAX_SAVED_TRACKS_CHUNK_SIZE = maxOf(MAX_SAVED_TRACKS_PAGE_SIZE, MAX_TRACKS_PER_MULTI_FETCH)

    /**
     * Maximum number of albums to process in a single chunk when upserting or fetching data.
     * Calculated as the maximum of Spotify's saved albums page size and the multi-album fetch limit.
     * Helps maintain consistency and performance when dealing with saved albums.
     */
    val MAX_SAVED_ALBUMS_CHUNK_SIZE = maxOf(MAX_SAVED_ALBUMS_PAGE_SIZE, MAX_ALBUMS_PER_MULTI_FETCH)

    /**
     * Maximum number of artists to process in a single chunk when upserting or fetching data.
     * Based on the greater of followed artists page size and multi-artist fetch limit.
     * Ensures robust and optimal handling of followed artists.
     */
    val MAX_SAVED_ARTISTS_CHUNK_SIZE = maxOf(MAX_FOLLOWED_ARTISTS_PAGE_SIZE, MAX_ARTISTS_PER_MULTI_FETCH)

    const val MAX_PENDING_ALBUMS_FETCH_CHUNK_SIZE = 50

}