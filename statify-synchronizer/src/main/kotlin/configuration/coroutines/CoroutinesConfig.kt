package org.danila.configuration.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import logging.logger
import org.danila.metrics.coroutine.CoroutineMetricsInterceptor
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.coroutines.CoroutineContext

@Configuration
class CoroutinesConfig(
    private val metricsInterceptor: CoroutineMetricsInterceptor
) {

    private val logger by logger()

    @Bean
    fun scheduleCoroutine(): CoroutineScope = object : CoroutineScope, DisposableBean {

        private val job = SupervisorJob()
        private val handler = CoroutineExceptionHandler { _, exception ->
            logger.error(exception) { "Unhandled exception in schedule coroutine" }
        }

        override val coroutineContext: CoroutineContext = Dispatchers.Default + job + handler + metricsInterceptor

        override fun destroy() {
            logger.info { "Shutting down scheduling CoroutineScope" }
            job.cancel()
        }

    }

}