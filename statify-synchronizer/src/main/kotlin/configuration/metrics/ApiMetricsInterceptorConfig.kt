package org.danila.configuration.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import okhttp3.Interceptor
import org.danila.metrics.Metrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class ApiMetricsInterceptorConfig {

    @Bean
    fun spotifyMetricsInterceptor(metrics: Metrics): Interceptor =
        metricsInterceptor(
            metrics.spotifyApiRequestsTotal,
            metrics.spotifyApiCallDurationTimer
        )

    @Bean
    fun musicBrainzMetricsInterceptor(metrics: Metrics): Interceptor =
        metricsInterceptor(
            metrics.musicBrainzApiRequestsTotal,
            metrics.musicBrainzApiCallDurationTimer
        )

    private fun metricsInterceptor(
        requestCounter: Counter,
        callTimer: Timer
    ): Interceptor {
        return Interceptor { chain ->
            requestCounter.increment()
            val start = System.nanoTime()

            val response = chain.proceed(chain.request())

            val elapsed = System.nanoTime() - start
            callTimer.record(elapsed, TimeUnit.NANOSECONDS)

            response
        }
    }

}