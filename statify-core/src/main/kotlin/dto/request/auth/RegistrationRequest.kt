package org.danila.dto.request.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.danila.validation.NoEdgeSpaces

data class RegistrationRequest(

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
        message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter"
    )
    @field:NoEdgeSpaces(message = "Password cannot start or end with spaces")
    val password: String

) {

    fun normalize(): RegistrationRequest = copy(
        email = email.trim(),
        password = password.trim()
    )

}