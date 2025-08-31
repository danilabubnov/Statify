package org.danila.service.model.user

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
    fun create(email: String, password: String): User {
        if (userRepository.existsByEmail(email)) error("Email already exists")

        return userRepository.save(
            User(
                id = idGeneratorService.uuid,
                email = email,
                password = passwordEncoder.encode(password)
            )
        )
    }

}