package org.danila.metrics.coroutine

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import org.danila.metrics.StatifySynchronizerMetrics
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

@Component
class CoroutineMetricsInterceptor(
    private val metrics: StatifySynchronizerMetrics
) : ContinuationInterceptor {

    override val key: CoroutineContext.Key<*>
        get() = ContinuationInterceptor.Key

    private data class JobMetric(var startTimeNanos: Long, var activeNanos: Long)

    private companion object {
        private val totalContext = ConcurrentHashMap<Job, JobMetric>()
    }

    override fun <T> interceptContinuation(
        continuation: Continuation<T>
    ): Continuation<T> {
        val job: Job? = continuation.context[Job.Key]

        if (job != null) {
            totalContext.computeIfAbsent(job) { _ ->
                val now = System.nanoTime()
                val jm = JobMetric(startTimeNanos = now, activeNanos = 0L)

                job.invokeOnCompletion {
                    val finishedAt = System.nanoTime()
                    val totalNanos = finishedAt - jm.startTimeNanos

                    val coroutineName = continuation.context[CoroutineName.Key]?.name
                        ?: "unnamed"

                    metrics.recordCoroutineTotal(coroutineName, totalNanos)
                    metrics.recordCoroutineActive(coroutineName, jm.activeNanos)

                    totalContext.remove(job)
                }

                jm
            }
        }

        return TimingContinuation(delegate = continuation)
    }

    private class TimingContinuation<T>(
        private val delegate: Continuation<T>
    ) : Continuation<T> {

        override val context: CoroutineContext
            get() = delegate.context

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun resumeWith(result: Result<T>) {
            val activeStart = System.nanoTime()

            try {
                delegate.resumeWith(result)
            } finally {
                val activeEnd = System.nanoTime()
                val delta = activeEnd - activeStart

                var currentJob: Job? = context[Job.Key]

                while (currentJob != null) {
                    totalContext[currentJob]?.let { jm ->
                        jm.activeNanos += delta
                    }

                    currentJob = currentJob.parent
                }
            }
        }
    }

}