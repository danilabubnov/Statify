package org.danila.services.spotify

import event.TokenCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TokenStore {

    @Cacheable("tokens", key = "#userId")
    suspend fun get(userId: UUID): TokenCredentials = withContext(Dispatchers.IO) { throw NoSuchElementException("No creds for $userId") }

    @CachePut("tokens", key = "#userId")
    suspend fun put(userId: UUID, creds: TokenCredentials): TokenCredentials = withContext(Dispatchers.IO) { creds }

}