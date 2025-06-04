package org.danila.configuration.constants.spotify

object SpotifyApiConstants {

    // --- Spotify pagination and batching constants ---

    /**
     * Maximum number of items to fetch per page when retrieving a user’s saved tracks from Spotify.
     */
    const val MAX_SAVED_TRACKS_PAGE_SIZE = 50

    /**
     * Maximum number of track IDs to request in a single `getSeveralTracks` Spotify API call.
     */
    const val MAX_TRACKS_PER_MULTI_FETCH = 50

    /**
     * Maximum number of items to fetch per page when retrieving the list of artists a user follows on Spotify.
     */
    const val MAX_FOLLOWED_ARTISTS_PAGE_SIZE = 50

    /**
     * Maximum number of artist IDs to request in a single `getSeveralArtists` Spotify API call.
     */
    const val MAX_ARTISTS_PER_MULTI_FETCH = 50

    /**
     * Maximum number of items to fetch per page when retrieving a user’s saved albums from Spotify.
     */
    const val MAX_SAVED_ALBUMS_PAGE_SIZE = 50

    /**
     * Maximum number of album IDs to request in a single `getSeveralAlbums` Spotify API call.
     */
    const val MAX_ALBUMS_PER_MULTI_FETCH = 20

}