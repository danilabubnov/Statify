package org.danila.util.retry

import java.time.Duration

interface DatabaseTransactionRetryHelper {

    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelay: Duration = Duration.ofMillis(100),
        block: suspend () -> T
    ): T

}