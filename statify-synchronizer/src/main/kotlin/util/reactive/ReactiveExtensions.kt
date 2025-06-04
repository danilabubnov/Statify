package org.danila.util.reactive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.awaitSingle
import reactor.core.publisher.Flux

suspend fun <T> Flux<T>.awaitList(): List<T> = this.collectList().awaitSingle()

fun <T> Flow<T>.batchWithTimeout(
    size: Int,
    maxWaitMs: Long
): Flow<Batch<T>> = flow {
    val buffer = mutableListOf<T>()
    var lastEmit = System.currentTimeMillis()

    suspend fun tryEmit(force: Boolean = false) {
        val now = System.currentTimeMillis()

        if (buffer.size >= size) {
            emit(Batch(buffer.toList(), EmitReason.SIZE))
            buffer.clear()
            lastEmit = now
            return
        }

        if (!force && buffer.isNotEmpty() && now - lastEmit >= maxWaitMs) {
            emit(Batch(buffer.toList(), EmitReason.TIMEOUT))
            buffer.clear()
            lastEmit = now
            return
        }

        if (force && buffer.isNotEmpty()) {
            emit(Batch(buffer.toList(), EmitReason.FINAL))
            buffer.clear()
            lastEmit = now
        }
    }

    collect { element ->
        buffer += element
        tryEmit()
    }

    tryEmit(force = true)
}

data class Batch<T>(val result: List<T>, val reason: EmitReason)

enum class EmitReason {
    TIMEOUT,
    SIZE,
    FINAL
}