package org.danila.exception

import org.springframework.security.core.AuthenticationException

class MissingBearerTokenException : AuthenticationException("No bearer token presented")
class InvalidBearerTokenException : AuthenticationException("Invalid bearer token")