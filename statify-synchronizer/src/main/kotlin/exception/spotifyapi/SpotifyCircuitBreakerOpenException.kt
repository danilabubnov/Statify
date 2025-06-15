package org.danila.exception.spotifyApi

class SpotifyCircuitBreakerOpenException(
    message: String,
    cause: Throwable,
) : SpotifyApiException(message = message, cause = cause)