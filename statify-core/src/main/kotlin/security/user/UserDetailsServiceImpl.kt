package org.danila.security.user

import org.danila.exception.UsernameNotFoundException
import org.danila.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import utils.trimToNull

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails? = UserDetailsImpl(
        user = userRepository.findByUsername(
            username?.trimToNull() ?: throw IllegalArgumentException("Username is null")
        ) ?: throw UsernameNotFoundException()
    )

}