package org.danila.dto.request.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    @field:Size(min = 5, max = 128, message = "Email must be between 5 and 128 characters long")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(max = 512, message = "Password must be less than 512 characters long")
    val password: String

) {

    fun normalize(): LoginRequest = copy(
        email = email.trim().lowercase(),
        password = password.trim()
    )

}