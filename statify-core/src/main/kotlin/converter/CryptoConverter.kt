package org.danila.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.security.crypto.encrypt.TextEncryptor

@Converter
class CryptoConverter : AttributeConverter<String?, String?> {

    override fun convertToDatabaseColumn(attribute: String?): String? {
        return attribute?.let { encryptor.encrypt(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        return dbData?.let { encryptor.decrypt(it) }
    }

    companion object EncryptionHolder {
        lateinit var encryptor: TextEncryptor
    }

}