package org.danila.web.handler

import org.danila.exception.InvalidBearerTokenException
import org.danila.exception.MissingBearerTokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class WebExceptionHandler {

    @ExceptionHandler(MissingBearerTokenException::class)
    fun handleMissingToken(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("Authentication required"))
    }

    @ExceptionHandler(InvalidBearerTokenException::class)
    fun handleInvalidFormat(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("Invalid authorization header format"))
    }

}

data class ErrorResponse(val message: String)