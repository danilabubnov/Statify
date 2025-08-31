package org.danila.dto.response.auth

import org.danila.model.users.User
import java.util.*

data class AuthResponse(val id: UUID, val email: String) {
    companion object {
        fun fromUser(user: User) = AuthResponse(id = user.id, email = user.email)
    }
}