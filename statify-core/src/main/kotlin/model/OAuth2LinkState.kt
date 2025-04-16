package org.danila.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.persistence.*
import org.danila.model.users.User
import org.hibernate.annotations.CreationTimestamp
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType
import java.time.Instant
import java.util.*

@Entity
@Table(name = "oauth2_link_state")
data class OAuth2LinkState(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = OAuth2AuthorizationRequestConverter::class)
    val authorizationRequest: OAuth2AuthorizationRequest,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null

)

@Converter
class OAuth2AuthorizationRequestConverter() : AttributeConverter<OAuth2AuthorizationRequest, String> {

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerModule(SimpleModule().apply {
            addSerializer(OAuth2AuthorizationResponseType::class.java, OAuth2AuthorizationResponseTypeSerializer())
            addDeserializer(OAuth2AuthorizationResponseType::class.java, OAuth2AuthorizationResponseTypeDeserializer())
        })
    }

    override fun convertToDatabaseColumn(attribute: OAuth2AuthorizationRequest): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): OAuth2AuthorizationRequest {
        return objectMapper.readValue(dbData, OAuth2AuthorizationRequest::class.java)
    }

}

class OAuth2AuthorizationResponseTypeSerializer : StdSerializer<OAuth2AuthorizationResponseType>(
    OAuth2AuthorizationResponseType::class.java
) {
    override fun serialize(
        value: OAuth2AuthorizationResponseType,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        gen.writeString(value.value)
    }
}

class OAuth2AuthorizationResponseTypeDeserializer : StdDeserializer<OAuth2AuthorizationResponseType>(
    OAuth2AuthorizationResponseType::class.java
) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OAuth2AuthorizationResponseType {
        val value = p.text
        return OAuth2AuthorizationResponseType(value)
    }
}