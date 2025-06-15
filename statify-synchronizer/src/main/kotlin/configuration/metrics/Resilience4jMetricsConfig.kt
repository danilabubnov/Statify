package org.danila.configuration.metrics

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import org.danila.metrics.Metrics
import org.springframework.context.annotation.Configuration

@Configuration
class Resilience4jMetricsConfig(
    retryRegistry: RetryRegistry,
    circuitBreakerRegistry: CircuitBreakerRegistry,
    private val metrics: Metrics
) {

    init {
        retryRegistry
            .retry("spotifyServerErrorRetry")
            .eventPublisher
            .onError { _ ->
                metrics.spotifyServerErrorFallbackCounter.increment()
            }

        circuitBreakerRegistry
            .circuitBreaker("spotifyCircuitBreaker")
            .eventPublisher
            .onCallNotPermitted { _ ->
                metrics.spotifyCircuitBreakerFallbackCounter.increment()
            }
    }

}