package org.danila.configuration

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class DatabaseConfig {

    @Bean
    fun transactionalOperator(connectionFactory: ConnectionFactory): TransactionalOperator {
        val transactionManager = R2dbcTransactionManager(connectionFactory)
        return TransactionalOperator.create(transactionManager)
    }

}
