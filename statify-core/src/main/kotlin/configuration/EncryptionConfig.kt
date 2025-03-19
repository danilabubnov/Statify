package org.danila.configuration

import config.EncryptionUtil
import jakarta.annotation.PostConstruct
import org.danila.converter.CryptoConverter
import org.springframework.context.annotation.Configuration

@Configuration
class EncryptionConfig(
    private val encryptionUtil: EncryptionUtil
) {

    @PostConstruct
    fun init() {
        CryptoConverter.EncryptionHolder.encryptor = encryptionUtil.encryptor
    }

}