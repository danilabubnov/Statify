package org.danila.util.retry

import io.r2dbc.spi.R2dbcException
import kotlinx.coroutines.delay
import org.danila.metrics.StatifySynchronizerMetrics
import org.danila.metrics.recordSuspend
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

@Component
class DatabaseTransactionRetryHelperImpl(
    private val metrics: StatifySynchronizerMetrics
) : DatabaseTransactionRetryHelper {

    override suspend fun <T> executeWithRetry(
        maxAttempts: Int,
        initialDelay: Duration,
        block: suspend () -> T
    ): T {
        metrics.databaseWriteTotalCounter.increment()

        return metrics.databaseWriteDurationTimer.recordSuspend {
            var attempt = 1
            var nextDelay = initialDelay

            while (true) {
                try {
                    val result = block()
                    metrics.databaseWriteSuccessCounter.increment()
                    return@recordSuspend result
                } catch (exc: Throwable) {
                    if (exc is R2dbcException) {
                        val sqlState = exc.sqlState
                        val isDeadlock = sqlState == "40001" || sqlState == "40P01"

                        if (isDeadlock && attempt < maxAttempts) {
                            metrics.databaseDeadlockRetryCounter.increment()
                            val jitter = ThreadLocalRandom.current().nextDouble(0.4, 1.1)
                            val delayMillis = (nextDelay.toMillis() * jitter).toLong()

                            delay(delayMillis)

                            attempt++
                            nextDelay = nextDelay.multipliedBy(2)
                            continue
                        } else if (isDeadlock) metrics.databaseDeadlockFailCounter.increment()
                    }

                    throw exc
                }
            }

            throw IllegalStateException("Unreachable code")
        }
    }

}