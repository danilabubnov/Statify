package org.danila.services.model.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.danila.util.retry.DatabaseTransactionRetryHelper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Component
class DatabaseExecutionContext(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val databaseTransactionRetryHelper: DatabaseTransactionRetryHelper
) {

    suspend fun <T> withRead(
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        readSemaphore.withPermit {
            block()
        }
    }

    suspend fun <T> withWriteTransactionRetry(
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        writeSemaphore.withPermit {
            databaseTransactionRetryHelper.executeWithRetry {
                transactionalOperator.executeAndAwait {
                    block()
                }
            }
        }
    }

}