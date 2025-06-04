package org.danila.services.api.spotify.retry

import kotlinx.coroutines.delay
import org.danila.metrics.Metrics
import org.springframework.stereotype.Component
import retrofit2.HttpException
import java.time.Duration

@Component
class SpotifyRateLimitRetryHelperImpl(
    private val metrics: Metrics
) : SpotifyRateLimitRetryHelper {

    override suspend fun <T> withRetryAfter(block: suspend () -> T): T {
        val maxRetries = 3
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: HttpException) {
                if (e.code() != 429) throw e

                metrics.spotifyRateLimitRetryCounter.increment()

                val retryAfterSec = e.response()
                    ?.headers()
                    ?.get("Retry-After")
                    ?.toLongOrNull()
                    ?: 5L

                metrics.spotifyRateLimitDelayTimer.record(Duration.ofSeconds(retryAfterSec))

                delay(retryAfterSec * 1_000L)
            }
        }

        return block()
    }

}