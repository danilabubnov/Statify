package org.danila.dto.auth

data class LoginResponse(val accessToken: String, val refreshToken: String)