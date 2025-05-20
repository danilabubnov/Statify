package org.danila

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Flux

suspend fun <T> Flux<T>.awaitList(): List<T> = this.collectList().awaitSingle()

fun <T> Flow<T>.batchWithTimeout(
    size: Int,
    maxWaitMs: Long
): Flow<List<T>> = flow {
    val buffer = mutableListOf<T>()
    var lastEmit = System.currentTimeMillis()

    suspend fun tryEmit(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (buffer.size >= size
            || (force && buffer.isNotEmpty())
            || (buffer.isNotEmpty() && now - lastEmit >= maxWaitMs)
        ) {
            emit(buffer.toList())
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