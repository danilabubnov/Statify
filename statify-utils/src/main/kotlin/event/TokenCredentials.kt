package event

data class TokenCredentials(
    var accessToken: String,
    val refreshToken: String,
)