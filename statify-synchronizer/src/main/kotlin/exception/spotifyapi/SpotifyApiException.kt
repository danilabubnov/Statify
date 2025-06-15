package exception.spotifyapi

sealed class SpotifyApiException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)