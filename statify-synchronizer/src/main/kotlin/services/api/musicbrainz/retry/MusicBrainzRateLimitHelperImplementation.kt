package org.danila.services.api.musicbrainz.retry

import io.github.resilience4j.kotlin.ratelimiter.decorateSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MusicBrainzRateLimitHelperImplementation : MusicBrainzRateLimitHelper {

    private val rateLimiter: RateLimiter = RateLimiterRegistry.of(
        RateLimiterConfig.custom()
            .limitForPeriod(1)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(1))
            .build()
    ).rateLimiter("musicBrainzRateLimiter")

    override suspend fun <T> withMusicBrainzRateLimit(block: suspend () -> T): T {
        return rateLimiter.decorateSuspendFunction(block).invoke()
    }

}