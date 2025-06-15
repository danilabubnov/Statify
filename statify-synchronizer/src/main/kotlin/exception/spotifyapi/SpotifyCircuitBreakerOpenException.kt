package exception.spotifyapi

class SpotifyCircuitBreakerOpenException(
    message: String,
    cause: Throwable,
) : SpotifyApiException(message = message, cause = cause)