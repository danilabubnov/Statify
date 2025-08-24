package org.danila.dto.auth

import java.util.*

data class AuthResponse(val id: UUID, val firstName: String, val lastName: String, val email: String, val username: String)