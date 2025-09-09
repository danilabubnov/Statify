package org.danila.services.api.musicbrainz.retry

interface MusicBrainzRateLimitHelper {

    suspend fun <T> withMusicBrainzRateLimit(block: suspend () -> T): T

}