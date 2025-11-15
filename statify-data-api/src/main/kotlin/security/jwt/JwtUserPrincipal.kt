package org.danila.security.jwt

import java.security.Principal
import java.util.*

data class JwtUserPrincipal(
    val userId: UUID
) : Principal {
    override fun getName(): String = userId.toString()
}
