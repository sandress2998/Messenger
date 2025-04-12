package ru.mephi.chatservice.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class TransactionConfig {
    @Bean
    fun transactionalOperator(r2dbcTxManager: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(r2dbcTxManager)
    }

    @Bean
    fun r2dbcTxManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}