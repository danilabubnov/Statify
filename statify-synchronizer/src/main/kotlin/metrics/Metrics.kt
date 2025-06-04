package org.danila.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class Metrics(private val meterRegistry: MeterRegistry) {

    val followedArtistsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.followed_artists.total",
        "entity", "followed_artists",
        "reason", "total",
        "description", "Total number of batch emits for followed artists"
    )

    val followedArtistsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.followed_artists.timeout",
        "entity", "followed_artists",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for followed artists"
    )

    val savedTracksTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_tracks.total",
        "entity", "saved_tracks",
        "reason", "total",
        "description", "Total number of batch emits for saved tracks"
    )

    val savedTracksTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_tracks.timeout",
        "entity", "saved_tracks",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for saved tracks"
    )

    val savedAlbumsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_albums.total",
        "entity", "saved_albums",
        "reason", "total",
        "description", "Total number of batch emits for saved albums"
    )

    val savedAlbumsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_albums.timeout",
        "entity", "saved_albums",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for saved albums"
    )

    val multiFetchArtistsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_artists.total",
        "entity", "multi_fetch_artists",
        "reason", "total",
        "description", "Total number of batch emits for multi-fetch artists during enrichment"
    )

    val multiFetchArtistsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_artists.timeout",
        "entity", "multi_fetch_artists",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for multi-fetch artists during enrichment"
    )

    val multiFetchTracksTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_tracks.total",
        "entity", "multi_fetch_tracks",
        "reason", "total",
        "description", "Total number of batch emits for multi-fetch tracks during enrichment"
    )

    val multiFetchTracksTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_tracks.timeout",
        "entity", "multi_fetch_tracks",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for multi-fetch tracks during enrichment"
    )

    val multiFetchAlbumsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_albums.total",
        "entity", "multi_fetch_albums",
        "reason", "total",
        "description", "Total number of batch emits for multi-fetch albums during enrichment"
    )

    val multiFetchAlbumsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_albums.timeout",
        "entity", "multi_fetch_albums",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for multi-fetch albums during enrichment"
    )

    private val coroutineTotalTimers: ConcurrentHashMap<String, Timer> = ConcurrentHashMap()
    private val coroutineActiveTimers: ConcurrentHashMap<String, Timer> = ConcurrentHashMap()

    fun recordCoroutineTotal(coroutineName: String, totalNanos: Long) {
        val timer = coroutineTotalTimers.computeIfAbsent(coroutineName) { name ->
            meterRegistry.timer(
                "coroutine.execution.duration",
                "coroutine_name", name,
                "phase", "total",
                "description", "Total wall-clock time of coroutine \"$name\""
            )
        }
        timer.record(totalNanos, TimeUnit.NANOSECONDS)
    }

    fun recordCoroutineActive(coroutineName: String, activeNanos: Long) {
        val timer = coroutineActiveTimers.computeIfAbsent(coroutineName) { name ->
            meterRegistry.timer(
                "coroutine.execution.duration",
                "coroutine_name", name,
                "phase", "active",
                "description", "Active (CPU) time of coroutine \"$name\" without suspension"
            )
        }
        timer.record(activeNanos, TimeUnit.NANOSECONDS)
    }

    val databaseWriteTotalCounter: Counter = meterRegistry.counter(
        "database.transaction.write.total",
        "operation", "write",
        "description", "Total number of write transactions executed"
    )

    val databaseWriteSuccessCounter: Counter = meterRegistry.counter(
        "database.transaction.write.success",
        "operation", "write",
        "description", "Number of successfully committed write transactions"
    )

    val databaseDeadlockRetryCounter: Counter = meterRegistry.counter(
        "database.transaction.write.deadlock.retry",
        "operation", "write",
        "description", "Number of retries due to deadlock in write transactions"
    )

    val databaseDeadlockFailCounter: Counter = meterRegistry.counter(
        "database.transaction.write.deadlock.fail",
        "operation", "write",
        "description", "Number of write transactions that failed after max deadlock retries"
    )

    val databaseWriteDurationTimer: Timer = meterRegistry.timer(
        "database.transaction.write.duration",
        "operation", "write",
        "description", "Total duration of write transactions (including retries on deadlock)"
    )

    val spotifyRateLimitRetryCounter: Counter = meterRegistry.counter(
        "spotify_rate_limit_retry_total",
        "description", "Number of retries due to HTTP 429 (rate limit) from Spotify"
    )

    val spotifyRateLimitDelayTimer: Timer = meterRegistry.timer(
        "spotify_rate_limit_delay_seconds",
        "description", "Total time spent delaying because of HTTP 429 (Retry-After) from Spotify"
    )

    val spotifyApiRequestsTotal: Counter = meterRegistry.counter(
        "spotify_api_requests_total",
        "description", "Total number of HTTP calls to Spotify API"
    )

    val spotifyApiCallDurationTimer: Timer = meterRegistry.timer(
        "spotify_api_call_duration_seconds",
        "description", "Duration of Spotify API HTTP call (without retries)"
    )

}