package org.danila.services.api.spotify.auth

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import event.TokenCredentials
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.danila.services.RedisStateService
import org.danila.util.UserIdKey
import org.springframework.stereotype.Component
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

@Component
class SpotifyAuthRetryHelper(
    private val spotifyAuthService: SpotifyAuthService,
    private val redisStateService: RedisStateService
) {

    private val userTokenMutexCache: Cache<UUID, Mutex> = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build()

    suspend fun <T> withAuthRetry(
        block: suspend (authHeader: String) -> T
    ): T {
        val userId = coroutineContext[UserIdKey]?.userId ?: error("No userId found")
        var creds = redisStateService.getTokenCredentials(userId)
        val initial = creds.accessToken

        return try {
            block("Bearer $initial")
        } catch (e: HttpException) {
            if (e.code() != 401) throw e

            val userTokenMutex = userTokenMutexCache.get(userId) { Mutex() }

            val newToken = userTokenMutex.withLock {
                creds = redisStateService.getTokenCredentials(userId)

                if (creds.accessToken == initial) {
                    val fresh = spotifyAuthService.refreshAccessToken(creds.refreshToken)

                    redisStateService.putTokenCredentials(userId, TokenCredentials(fresh, creds.refreshToken))

                    fresh
                } else creds.accessToken
            }

            block("Bearer $newToken")
        }
    }

}