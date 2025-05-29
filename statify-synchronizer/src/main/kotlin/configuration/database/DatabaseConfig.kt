package org.danila.configuration.database

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.sync.Semaphore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class DatabaseConfig(
    @Value("\${database.concurrent-write-permits}") private val writePermits: Int,
    @Value("\${database.concurrent-read-permits}") private val readPermits: Int
){

    @Bean
    fun transactionalOperator(connectionFactory: ConnectionFactory): TransactionalOperator {
        val transactionManager = R2dbcTransactionManager(connectionFactory)
        return TransactionalOperator.create(transactionManager)
    }

    @Bean
    fun databaseWriteSemaphore(): Semaphore = Semaphore(permits = writePermits)

    @Bean
    fun databaseReadSemaphore(): Semaphore = Semaphore(permits = readPermits)

}