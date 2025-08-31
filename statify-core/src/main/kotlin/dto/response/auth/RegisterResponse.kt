package org.danila.dto.response.auth

data class RegisterResponse(val accessToken: String, val user: AuthResponse)
