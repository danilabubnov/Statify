package org.danila

// --- Spotify pagination and batching constants ---

/**
 * Maximum number of items to fetch per page when retrieving a user’s saved tracks from Spotify.
 */
const val MAX_SAVED_TRACKS_PAGE_SIZE = 50

/**
 * Capacity of the in-memory buffer for the Flow of saved tracks before batching.
 */
const val SAVED_TRACKS_FLOW_BUFFER_CAPACITY = MAX_SAVED_TRACKS_PAGE_SIZE * 4

/**
 * Number of saved tracks to accumulate before writing a batch to the database.
 */
const val SAVED_TRACKS_BATCH_SIZE = SAVED_TRACKS_FLOW_BUFFER_CAPACITY / 2

/**
 * Maximum number of track IDs to request in a single `getSeveralTracks` Spotify API call.
 */
const val MAX_TRACKS_PER_MULTI_FETCH = 50

/**
 * Capacity of the in-memory buffer for the Flow of “several tracks” results before batching.
 */
const val MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY = MAX_TRACKS_PER_MULTI_FETCH * 4

/**
 * Number of tracks from multi-fetch calls to accumulate before writing a batch to the database.
 */
const val MULTI_FETCH_TRACKS_BATCH_SIZE = MULTI_FETCH_TRACKS_FLOW_BUFFER_CAPACITY / 2

/**
 * Maximum number of items to fetch per page when retrieving the list of artists a user follows on Spotify.
 */
const val MAX_FOLLOWED_ARTISTS_PAGE_SIZE = 50

/**
 * Capacity of the in-memory buffer for the Flow of followed-artist items before batching.
 */
const val FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY = MAX_FOLLOWED_ARTISTS_PAGE_SIZE * 4

/**
 * Number of followed artists to accumulate before writing a batch to the database.
 */
const val FOLLOWED_ARTISTS_BATCH_SIZE = FOLLOWED_ARTISTS_FLOW_BUFFER_CAPACITY / 2

/**
 * Maximum number of artist IDs to request in a single `getSeveralArtists` Spotify API call.
 */
const val MAX_ARTISTS_PER_MULTI_FETCH = 50

/**
 * Capacity of the in-memory buffer for the Flow of “several artists” results before batching.
 */
const val MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY = MAX_ARTISTS_PER_MULTI_FETCH * 4

/**
 * Number of artists from multi-fetch calls to accumulate before writing a batch to the database.
 */
const val MULTI_FETCH_ARTISTS_BATCH_SIZE = MULTI_FETCH_ARTISTS_FLOW_BUFFER_CAPACITY / 2

/**
 * Maximum number of items to fetch per page when retrieving a user’s saved albums from Spotify.
 */
const val MAX_SAVED_ALBUMS_PAGE_SIZE = 50

/**
 * Capacity of the in-memory buffer for the Flow of saved-album items before batching.
 */
const val SAVED_ALBUMS_FLOW_BUFFER_CAPACITY = MAX_SAVED_ALBUMS_PAGE_SIZE * 4

/**
 * Number of saved albums to accumulate before writing a batch to the database.
 */
const val SAVED_ALBUMS_BATCH_SIZE = SAVED_ALBUMS_FLOW_BUFFER_CAPACITY / 2

/**
 * Maximum number of album IDs to request in a single `getSeveralAlbums` Spotify API call.
 */
const val MAX_ALBUMS_PER_MULTI_FETCH = 20

/**
 * Capacity of the in-memory buffer for the Flow of “several albums” results before batching.
 */
const val MULTI_FETCH_ALBUMS_FLOW_BUFFER_CAPACITY = MAX_ALBUMS_PER_MULTI_FETCH * 4

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

// --- Kafka topic constants ---

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

const val SEVEN_DAYS_IN_MS = 7 * 24 * 60 * 60 * 1000L
const val FOURTEEN_DAYS_IN_MS = 14 * 24 * 60 * 60 * 1000L