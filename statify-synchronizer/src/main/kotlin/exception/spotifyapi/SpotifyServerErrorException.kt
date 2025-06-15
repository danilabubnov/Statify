package exception.spotifyapi

class SpotifyServerErrorException(
    message: String,
    cause: Throwable,
) : SpotifyApiException(message = message, cause = cause)