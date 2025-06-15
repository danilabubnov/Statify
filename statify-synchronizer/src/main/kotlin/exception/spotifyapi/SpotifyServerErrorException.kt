package org.danila.exception.spotifyApi

class SpotifyServerErrorException(
    message: String,
    cause: Throwable,
) : SpotifyApiException(message = message, cause = cause)