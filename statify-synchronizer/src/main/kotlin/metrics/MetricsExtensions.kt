package org.danila.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.danila.util.reactive.Batch
import org.danila.util.reactive.EmitReason
import java.util.concurrent.TimeUnit

fun <T> Flow<Batch<T>>.batchEmits(
    totalCounter: Counter,
    timeoutCounter: Counter
): Flow<Batch<T>> = this.onEach { batch ->
    totalCounter.increment()
    if (batch.reason == EmitReason.TIMEOUT) timeoutCounter.increment()
}

suspend fun <T> Timer.recordSuspend(block: suspend () -> T): T {
    val start = System.nanoTime()

    try {
        return block()
    } finally {
        val elapsed = System.nanoTime() - start
        this.record(elapsed, TimeUnit.NANOSECONDS)
    }
}