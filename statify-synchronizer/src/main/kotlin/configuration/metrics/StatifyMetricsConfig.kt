package org.danila.configuration.metrics

import okhttp3.Interceptor
import org.danila.metrics.Metrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class StatifyMetricsConfig {

    @Bean
    fun spotifyMetricsInterceptor(
        metrics: Metrics
    ): Interceptor {
        return Interceptor { chain ->
            metrics.spotifyApiRequestsTotal.increment()

            val start = System.nanoTime()
            val response = chain.proceed(chain.request())
            val elapsed = System.nanoTime() - start

            metrics.spotifyApiCallDurationTimer.record(elapsed, TimeUnit.NANOSECONDS)

            response
        }
    }

}