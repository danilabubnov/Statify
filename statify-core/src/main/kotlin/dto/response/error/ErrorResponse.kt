package org.danila.dto.response.error

import java.time.Instant

data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val message: String,
    val errors: Map<String, String>? = null
)