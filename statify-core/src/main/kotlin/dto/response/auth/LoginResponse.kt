package org.danila.dto.response.auth

data class LoginResponse(val accessToken: String, val user: AuthResponse)