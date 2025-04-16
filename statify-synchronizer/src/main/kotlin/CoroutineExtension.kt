package org.danila

import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Flux

suspend fun <T> Flux<T>.awaitList(): List<T> = this.collectList().awaitSingle()