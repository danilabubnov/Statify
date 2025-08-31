package org.danila.web.handler

import org.danila.dto.response.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@ControllerAdvice
class WebExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: Exception): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.BAD_REQUEST, ex.message ?: ex.localizedMessage)

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: Exception): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.BAD_REQUEST, ex.message ?: ex.localizedMessage)

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuth(ex: AuthenticationException): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password")

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.valueOf(ex.statusCode.value()), ex.reason ?: "Error")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: Exception): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: Exception): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.FORBIDDEN, "Forbidden")

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> =
        buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        errors: Map<String, String>? = null
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(
                timestamp = Instant.now(),
                message = message,
                errors = errors
            )
        )

}