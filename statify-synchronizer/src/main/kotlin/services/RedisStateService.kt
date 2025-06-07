package org.danila.services

import event.TokenCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ResourceLoader
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.NoSuchElementException
import java.util.UUID

@Service
class RedisStateService @Autowired constructor(
    @Qualifier("inFlightCounterRedisTemplate") private val counterRedisTemplate: ReactiveRedisTemplate<String, Long>,
    @Qualifier("reactiveRedisTemplate") private val redisTemplate: ReactiveRedisTemplate<String, TokenCredentials>,
    private val resourceLoader: ResourceLoader
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

    suspend fun getTokenCredentials(userId: UUID): TokenCredentials = withContext(Dispatchers.IO) {
        redisTemplate.opsForValue().get(userId.toString()).awaitSingleOrNull()
            ?: throw NoSuchElementException("No creds for $userId")
    }

    suspend fun putTokenCredentials(userId: UUID, creds: TokenCredentials): TokenCredentials = withContext(Dispatchers.IO) {
        redisTemplate.opsForValue().set(userId.toString(), creds, Duration.ofMinutes(15)).awaitSingle()

        creds
    }

    suspend fun deleteTokenCredentials(userId: UUID): Unit = withContext(Dispatchers.IO) {
        redisTemplate.opsForValue().delete(userId.toString()).awaitSingleOrNull() ?: Unit
    }

    private fun pendingGen1Key(correlationId: String) = "pendingGen1:$correlationId"

    suspend fun getPendingGen1(correlationId: String): Long = withContext(Dispatchers.IO) {
        counterRedisTemplate
            .opsForValue()
            .get(pendingGen1Key(correlationId))
            .awaitSingleOrNull()
            ?: 0L
    }

    suspend fun incrementPendingGen1(correlationId: String, delta: Long = 1): Long = withContext(Dispatchers.IO) {
        counterRedisTemplate
            .opsForValue()
            .increment(pendingGen1Key(correlationId), delta)
            .awaitSingle()
    }

    private val cleanupCounterScript: DefaultRedisScript<Long> by lazy {
        DefaultRedisScript<Long>().apply {
            setLocation(resourceLoader.getResource("classpath:scripts/decrement_and_check_zero.lua"))
            resultType = Long::class.java
        }
    }

    suspend fun decrementCounterAndCheckIfDeleted(correlationId: String): Boolean = withContext(Dispatchers.IO) {
        val result = counterRedisTemplate
            .execute(
                cleanupCounterScript,
                listOf(correlationId),
                1
            )
            .awaitSingle()

        result == 1L
    }

}