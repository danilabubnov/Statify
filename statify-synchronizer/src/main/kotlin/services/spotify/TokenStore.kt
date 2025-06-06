package org.danila.services.spotify

import event.TokenCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class TokenStore @Autowired constructor(
    private val redisTemplate: ReactiveRedisTemplate<String, TokenCredentials>,
    @Qualifier("inFlightCounterRedisTemplate") private val counterRedisTemplate: ReactiveRedisTemplate<String, Long>,
) {

    /**
     * Currently using a manual wrapper around ReactiveRedisTemplate
     * because Spring Boot 3.4.x (Spring Data Redis 3.6.x) does not provide
     * a ReactiveRedisCacheManager implementation.
     *
     * Once you upgrade to Spring Boot 3.5+ (Spring Data Redis 3.7+),
     * you can switch to ReactiveRedisCacheManager and re-enable
     * Spring Cache annotations.
     */

    suspend fun get(userId: UUID): TokenCredentials = withContext(Dispatchers.IO) {
        redisTemplate.opsForValue().get(userId.toString()).awaitSingleOrNull()
            ?: throw NoSuchElementException("No creds for $userId")
    }

    suspend fun put(userId: UUID, creds: TokenCredentials): TokenCredentials = withContext(Dispatchers.IO) {
        redisTemplate.opsForValue().set(userId.toString(), creds, Duration.ofMinutes(15)).awaitSingle()

        creds
    }

    suspend fun delete(userId: UUID): Unit = withContext(Dispatchers.IO) {
        redisTemplate.opsForValue().delete(userId.toString()).awaitSingleOrNull() ?: Unit
    }

    private fun counterKey(correlationId: String) = "inflight:$correlationId"

    suspend fun initInFlightCounter(correlationId: String): Long = withContext(Dispatchers.IO) {
        counterRedisTemplate
            .opsForValue()
            .set(counterKey(correlationId), 0L)
            .then(counterRedisTemplate.opsForValue().get(counterKey(correlationId)))
            .awaitSingle() ?: 0L
    }

    suspend fun deleteInFlightCounter(correlationId: String): Unit = withContext(Dispatchers.IO) {
        counterRedisTemplate
            .opsForValue()
            .delete(counterKey(correlationId))
            .awaitSingleOrNull() ?: Unit
    }

    suspend fun incrementInFlight(correlationId: String, delta: Long = 1): Long = withContext(Dispatchers.IO) {
        counterRedisTemplate
            .opsForValue()
            .increment(counterKey(correlationId), delta)
            .awaitSingle()
    }

    suspend fun decrementInFlight(correlationId: String, delta: Long = 1): Long = withContext(Dispatchers.IO) {
        val newVal = counterRedisTemplate
            .opsForValue()
            .decrement(counterKey(correlationId), delta)
            .awaitSingle()

        if (newVal < 0) {
            counterRedisTemplate
                .opsForValue()
                .set(counterKey(correlationId), 0L)
                .awaitSingle()

            0L
        } else newVal
    }

    suspend fun getInFlight(correlationId: String): Long = withContext(Dispatchers.IO) {
        counterRedisTemplate
            .opsForValue()
            .get(counterKey(correlationId))
            .awaitSingleOrNull()
            ?: 0L
    }

}