package org.danila.services.model.spotify

import io.r2dbc.spi.R2dbcException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

object DatabaseExecutionContext {

    suspend inline fun <T> withRead(
        readSemaphore: Semaphore,
        crossinline block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        readSemaphore.withPermit {
            block()
        }
    }

    suspend inline fun <T> withWriteTransactionRetry(
        writeSemaphore: Semaphore,
        transactionalOperator: TransactionalOperator,
        crossinline block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        writeSemaphore.withPermit {
            onDeadlock {
                transactionalOperator.executeAndAwait {
                    block()
                }
            }
        }
    }

    suspend fun <T> onDeadlock(
        maxAttempts: Int = 3,
        initialDelay: Duration = Duration.ofMillis(100),
        block: suspend () -> T
    ): T {
        var attempt = 1
        var nextDelay = initialDelay

        while (true) {
            try {
                return block()
            } catch (exc: R2dbcException) {
                val isTransient = exc.sqlState in setOf("40001", "40P01")

                if (!isTransient || attempt >= maxAttempts) throw exc

                val jitter = ThreadLocalRandom.current().nextDouble(0.4, 1.1)

                println("Deadlock detected (sqlState=$${exc.sqlState}), attempt $attempt/$maxAttempts") // TODO: logs
                delay((nextDelay.toMillis() * jitter).toLong())

                attempt++
                nextDelay = nextDelay.multipliedBy(2)
            }
        }
    }

}