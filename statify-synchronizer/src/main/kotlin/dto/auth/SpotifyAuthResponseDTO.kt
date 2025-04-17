package org.danila.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class SpotifyAuthResponseDTO(

    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Long

)