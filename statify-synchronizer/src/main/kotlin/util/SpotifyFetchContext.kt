package org.danila.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.time.withTimeout
import java.time.Duration
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

object SpotifyFetchContextKey : CoroutineContext.Key<SpotifyFetchContext>

data class SpotifyFetchContext(
    private var _trackLimit: Int = DEFAULT_TRACK_LIMIT,
    private var _trackOffset: Int? = null,
    private var _trackTotal: Int? = null,

    private var _albumLimit: Int = DEFAULT_ALBUM_LIMIT,
    private var _albumOffset: Int? = null,
    private var _albumTotal: Int? = null,

    private var _artistLimit: Int = DEFAULT_ARTIST_LIMIT,
    private var _artistAfter: String? = null,
    private var _artistTotal: Int? = null,
) : AbstractCoroutineContextElement(SpotifyFetchContextKey) {

    val trackLimit: Int
        get() = _trackLimit
    val trackOffset: Int?
        get() = _trackOffset
    val trackTotal: Int?
        get() = _trackTotal

    val albumLimit: Int
        get() = _albumLimit
    val albumOffset: Int?
        get() = _albumOffset
    val albumTotal: Int?
        get() = _albumTotal

    val artistLimit: Int
        get() = _artistLimit
    val artistAfter: String?
        get() = _artistAfter
    val artistTotal: Int?
        get() = _artistTotal

    private companion object {
        private const val TOTAL_MEMORY_LIMIT = 4048

        private const val TRACK_MEMORY_UNIT = 7.0
        private const val ALBUM_MEMORY_UNIT = 6.5
        private const val ARTIST_MEMORY_UNIT = 4.5

        private const val TOTAL_MEMORY_UNIT_WEIGHT = TRACK_MEMORY_UNIT + ALBUM_MEMORY_UNIT + ARTIST_MEMORY_UNIT

        private const val TRACK_MEMORY_SHARE = TRACK_MEMORY_UNIT / TOTAL_MEMORY_UNIT_WEIGHT
        private const val ALBUM_MEMORY_SHARE = ALBUM_MEMORY_UNIT / TOTAL_MEMORY_UNIT_WEIGHT
        private const val ARTIST_MEMORY_SHARE = ARTIST_MEMORY_UNIT / TOTAL_MEMORY_UNIT_WEIGHT

        private const val DEFAULT_TRACK_LIMIT = (TOTAL_MEMORY_LIMIT * TRACK_MEMORY_SHARE / TRACK_MEMORY_UNIT).toInt()
        private const val DEFAULT_ALBUM_LIMIT = (TOTAL_MEMORY_LIMIT * ALBUM_MEMORY_SHARE / ALBUM_MEMORY_UNIT).toInt()
        private const val DEFAULT_ARTIST_LIMIT = (TOTAL_MEMORY_LIMIT * ARTIST_MEMORY_SHARE / ARTIST_MEMORY_UNIT).toInt()
    }

    private val limitsMutex = Mutex()

    private var trackDeferred = CompletableDeferred<Unit>()
    private var albumDeferred = CompletableDeferred<Unit>()
    private var artistDeferred = CompletableDeferred<Unit>()

    suspend fun updateFetchOptions(
        trackOffset: Int? = null,
        trackTotal: Int? = null,
        albumOffset: Int? = null,
        albumTotal: Int? = null,
        artistAfter: String? = null,
        artistTotal: Int? = null
    ) {
        limitsMutex.withLock {
            if (trackOffset != null || trackTotal != null) {
                this._trackOffset = trackOffset
                this._trackTotal = trackTotal

                if (!trackDeferred.isCompleted) trackDeferred.complete(Unit)
            }

            if (albumOffset != null || albumTotal != null) {
                this._albumOffset = albumOffset
                this._albumTotal = albumTotal

                if (!albumDeferred.isCompleted) albumDeferred.complete(Unit)
            }

            if (artistAfter != null || artistTotal != null) {
                this._artistAfter = artistAfter
                this._artistTotal = artistTotal

                if (!artistDeferred.isCompleted) artistDeferred.complete(Unit)
            }
        }

        rebalanceLimits()
    }

    private suspend fun rebalanceLimits() {
        try {
            withTimeout(Duration.ofSeconds(150)) { awaitAll(trackDeferred, albumDeferred, artistDeferred) }

            val trackConsumed = _trackOffset ?: 0
            val albumConsumed = _albumOffset ?: 0

            val trackRemaining  = (_trackTotal ?: throw IllegalStateException("TrackTotal must be initialized")) - trackConsumed
            val albumRemaining  = (_albumTotal ?: throw IllegalStateException("AlbumTotal must be initialized")) - albumConsumed
            val artistRemaining = _artistTotal ?: throw IllegalStateException("AlbumTotal must be initialized")

            var trackLimit = minOf(DEFAULT_TRACK_LIMIT, trackRemaining)
            var albumLimit = minOf(DEFAULT_ALBUM_LIMIT, albumRemaining)
            var artistLimit = minOf(DEFAULT_ARTIST_LIMIT, artistRemaining)

            val usedMemory = trackLimit * TRACK_MEMORY_UNIT + albumLimit * ALBUM_MEMORY_UNIT + artistLimit * ARTIST_MEMORY_UNIT
            var leftoverMemory = TOTAL_MEMORY_LIMIT - usedMemory

            while (true) {
                var grew = false

                if (trackLimit < trackRemaining && leftoverMemory >= TRACK_MEMORY_UNIT) {
                    trackLimit++;  leftoverMemory -= TRACK_MEMORY_UNIT;  grew = true
                }
                if (albumLimit < albumRemaining && leftoverMemory >= ALBUM_MEMORY_UNIT) {
                    albumLimit++;  leftoverMemory -= ALBUM_MEMORY_UNIT;  grew = true
                }
                if (artistLimit < artistRemaining && leftoverMemory >= ARTIST_MEMORY_UNIT) {
                    artistLimit++; leftoverMemory -= ARTIST_MEMORY_UNIT; grew = true
                }

                if (!grew) break
            }

            _trackLimit  = trackLimit
            _albumLimit  = albumLimit
            _artistLimit = artistLimit
        } catch (e: TimeoutCancellationException) {
            e.printStackTrace() // TODO: logs
        } finally {
            resetDeferreds()
        }
    }

    private fun resetDeferreds() {
        trackDeferred = CompletableDeferred()
        albumDeferred = CompletableDeferred()
        artistDeferred = CompletableDeferred()
    }

}