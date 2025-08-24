package org.danila.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegistrationRequest(

    @field:NotBlank(message = "First Name must not be blank")
    @field:Size(min = 3, max = 24, message = "First Name must contain between 3 and 24 characters")
    val firstName: String,

    @field:NotBlank(message = "Last Name must not be blank")
    @field:Size(min = 3, max = 24, message = "Last Name must contain between 3 and 24 characters")
    val lastName: String,

    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 3, max = 24, message = "Username must contain between 3 and 24 characters")
    val username: String,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 128, message = "Password must contain between 8 and 128 characters")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
        message = "Password must contain at least one digit, one lowercase letter, and one uppercase letter"
    )
    val password: String

)