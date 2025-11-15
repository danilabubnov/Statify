package org.danila.security.user

import org.danila.exception.EmailNotFoundException
import org.danila.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import utils.trimToNull
import java.util.*

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails? = UserDetailsImpl(
        user = userRepository.findByEmail(
            username?.trimToNull() ?: error("Email is null")
        ) ?: throw EmailNotFoundException()
    )

    fun loadUserById(userId: UUID): UserDetails = UserDetailsImpl(
        user = userRepository.findById(userId)
            .orElseThrow { error("User not found with id: $userId") }
    )

}