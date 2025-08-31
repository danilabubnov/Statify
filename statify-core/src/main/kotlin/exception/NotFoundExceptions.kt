package org.danila.exception

class EmailNotFoundException(message: String = "Email not found") : RuntimeException(message)