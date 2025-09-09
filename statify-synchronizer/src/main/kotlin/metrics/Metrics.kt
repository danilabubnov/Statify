package org.danila.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class Metrics(private val meterRegistry: MeterRegistry) {

    // ─── Batch emit metrics ─────────────────────────────────────────────────────

    val followedArtistsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.followed_artists.timeout",
        "entity", "followed_artists",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for followed artists"
    )
    val followedArtistsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.followed_artists.total",
        "entity", "followed_artists",
        "reason", "total",
        "description", "Total number of batch emits for followed artists"
    )

    val multiFetchAlbumsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_albums.timeout",
        "entity", "multi_fetch_albums",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for multi-fetch albums during enrichment"
    )
    val multiFetchAlbumsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_albums.total",
        "entity", "multi_fetch_albums",
        "reason", "total",
        "description", "Total number of batch emits for multi-fetch albums during enrichment"
    )

    val multiFetchArtistsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_artists.timeout",
        "entity", "multi_fetch_artists",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for multi-fetch artists during enrichment"
    )
    val multiFetchArtistsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_artists.total",
        "entity", "multi_fetch_artists",
        "reason", "total",
        "description", "Total number of batch emits for multi-fetch artists during enrichment"
    )

    val multiFetchTracksTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_tracks.timeout",
        "entity", "multi_fetch_tracks",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for multi-fetch tracks during enrichment"
    )
    val multiFetchTracksTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.multi_fetch_tracks.total",
        "entity", "multi_fetch_tracks",
        "reason", "total",
        "description", "Total number of batch emits for multi-fetch tracks during enrichment"
    )

    val savedAlbumsTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_albums.timeout",
        "entity", "saved_albums",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for saved albums"
    )
    val savedAlbumsTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_albums.total",
        "entity", "saved_albums",
        "reason", "total",
        "description", "Total number of batch emits for saved albums"
    )

    val savedTracksTimeoutCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_tracks.timeout",
        "entity", "saved_tracks",
        "reason", "timeout",
        "description", "Number of batch emits triggered by timeout for saved tracks"
    )
    val savedTracksTotalCounter: Counter = meterRegistry.counter(
        "spotify.batch.emit.saved_tracks.total",
        "entity", "saved_tracks",
        "reason", "total",
        "description", "Total number of batch emits for saved tracks"
    )

    // ─── Coroutine execution timers ────────────────────────────────────────────────

    private val coroutineActiveTimers: ConcurrentHashMap<String, Timer> = ConcurrentHashMap()
    private val coroutineTotalTimers: ConcurrentHashMap<String, Timer> = ConcurrentHashMap()

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

    // ─── Database transaction metrics ─────────────────────────────────────────────

    val databaseDeadlockFailCounter: Counter = meterRegistry.counter(
        "database.transaction.write.deadlock.fail",
        "operation", "write",
        "description", "Number of write transactions that failed after max deadlock retries"
    )
    val databaseDeadlockRetryCounter: Counter = meterRegistry.counter(
        "database.transaction.write.deadlock.retry",
        "operation", "write",
        "description", "Number of retries due to deadlock in write transactions"
    )
    val databaseWriteDurationTimer: Timer = meterRegistry.timer(
        "database.transaction.write.duration",
        "operation", "write",
        "description", "Total duration of write transactions (including retries on deadlock)"
    )
    val databaseWriteSuccessCounter: Counter = meterRegistry.counter(
        "database.transaction.write.success",
        "operation", "write",
        "description", "Number of successfully committed write transactions"
    )
    val databaseWriteTotalCounter: Counter = meterRegistry.counter(
        "database.transaction.write.total",
        "operation", "write",
        "description", "Total number of write transactions executed"
    )

    // ─── Spotify API metrics ─────────────────────────────────────────────────────

    val spotifyApiCallDurationTimer: Timer = meterRegistry.timer(
        "spotify_api_call_duration_seconds",
        "description", "Duration of Spotify API HTTP call (without retries)"
    )
    val spotifyApiRequestsTotal: Counter = meterRegistry.counter(
        "spotify_api_requests_total",
        "description", "Total number of HTTP calls to Spotify API"
    )
    val spotifyRateLimitDelayTimer: Timer = meterRegistry.timer(
        "spotify_rate_limit_delay_seconds",
        "description", "Total time spent delaying because of HTTP 429 (Retry-After) from Spotify"
    )
    val spotifyRateLimitRetryCounter: Counter = meterRegistry.counter(
        "spotify_rate_limit_retry_total",
        "description", "Number of retries due to HTTP 429 (rate limit) from Spotify"
    )
    val spotifyServerErrorFallbackCounter: Counter = meterRegistry.counter(
        "spotify_server_error_fallback_total",
        "description", "Number of fallbacks executed due to HTTP 5xx errors after retry attempts"
    )
    val spotifyCircuitBreakerFallbackCounter: Counter = meterRegistry.counter(
        "spotify_circuit_breaker_fallback_total",
        "description", "Number of fallbacks executed because the circuit breaker is open"
    )
    val spotifyFetchErrorCounter: Counter = meterRegistry.counter(
        "spotify_fetch_error_total",
        "description", "Total number of exceptions thrown during Spotify API fetch calls"
    )

    // ─── Music Brainz API metrics ─────────────────────────────────────────────────────

    val musicBrainzApiCallDurationTimer: Timer = meterRegistry.timer(
        "music_brainz_api_call_duration_seconds",
        "description", "Duration of Music Brainz API HTTP call (without retries)"
    )
    val musicBrainzApiRequestsTotal: Counter = meterRegistry.counter(
        "music_brainz_api_requests_total",
        "description", "Total number of HTTP calls to Music Brainz API"
    )

}