package org.danila.service

import jakarta.transaction.Transactional
import org.danila.model.users.User
import org.danila.repository.UserRepository
import org.danila.service.utils.IdGeneratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService @Autowired constructor(
    private val idGeneratorService: IdGeneratorService,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(firstName: String, lastName: String, email: String, username: String, password: String): User {
        if (userRepository.existsByUsername(username)) throw IllegalArgumentException("Username already exists")
        if (userRepository.existsByEmail(email)) throw IllegalArgumentException("Email already exists")

        return userRepository.save(
            User(
                id = idGeneratorService.uuid,
                firstName = firstName,
                lastName = lastName,
                email = email,
                username = username,
                password = passwordEncoder.encode(password)
            )
        )
    }

    @Transactional
    fun update(user: User) = userRepository.save(user)

}