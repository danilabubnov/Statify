package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import event.UserConnectedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.receiver.ReceiverOptions

const val USER_SPOTIFY_CONNECTED_TOPIC = "user.spotify.connected.v1"
const val USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC = "user.spotify.access.token.updated.v1"

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String
) {

    @Bean
    fun receiverOptions(): ReceiverOptions<String, UserConnectedEvent> {
        val props = mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.TYPE_MAPPINGS to "UserConnectedEvent:event.UserConnectedEvent"
        )

        return ReceiverOptions.create<String, UserConnectedEvent>(props)
            .subscription(listOf(USER_SPOTIFY_CONNECTED_TOPIC))
    }

    @Bean
    fun producerFactory(@Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            JsonSerializer.TYPE_MAPPINGS to """
                AccessTokenUpdatedEvent:event.AccessTokenUpdatedEvent
            """.trimIndent()
        )

        return DefaultKafkaProducerFactory(
            config,
            StringSerializer(),
            JsonSerializer<Any>(objectMapper)
        )
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>) = KafkaTemplate(producerFactory)

}