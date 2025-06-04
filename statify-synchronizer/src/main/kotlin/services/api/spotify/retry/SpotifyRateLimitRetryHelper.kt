package org.danila.services.api.spotify.retry

interface SpotifyRateLimitRetryHelper {

    suspend fun <T> withRetryAfter(block: suspend () -> T): T

}