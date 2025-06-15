package org.danila.exception.spotifyApi

sealed class SpotifyApiException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)