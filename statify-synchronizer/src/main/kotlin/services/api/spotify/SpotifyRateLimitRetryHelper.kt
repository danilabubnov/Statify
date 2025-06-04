package org.danila.services.api.spotify

interface SpotifyRateLimitRetryHelper {

    suspend fun <T> withRetryAfter(block: suspend () -> T): T

}