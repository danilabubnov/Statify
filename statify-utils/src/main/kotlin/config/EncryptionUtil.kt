package config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component

@Component
class EncryptionUtil {

    lateinit var encryptor: TextEncryptor

    @Value("\${encryption.password}")
    private lateinit var password: String

    @Value("\${encryption.salt}")
    private lateinit var salt: String

    @PostConstruct
    fun init() {
        encryptor = Encryptors.text(password, salt)
    }

}