package org.danila.exception

class UsernameNotFoundException(message: String = "User not found") : RuntimeException(message)