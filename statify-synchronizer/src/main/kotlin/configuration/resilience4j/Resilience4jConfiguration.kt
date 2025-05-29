package org.danila.configuration.resilience4j

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.function.Predicate

@Configuration
class Resilience4jConfiguration {

    /**
     * This configuration is used instead of application.properties because Spring Boot does not support
     * SpEL expressions like ${bean} or #{@bean} in .properties files.
     */
    @Bean
    fun spotifyServerErrorRetry(spotifyServerErrorPredicate: Predicate<Throwable>): Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(4)
            .waitDuration(Duration.ofSeconds(2))
            .retryOnException(spotifyServerErrorPredicate::test)
            .build()

        return Retry.of("spotifyServerErrorRetry", config)
    }

}
