package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import event.UserConnectedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.RetryListener
import org.springframework.kafka.support.serializer.JsonDeserializer

const val USER_SPOTIFY_CONNECTED_TOPIC = "user.spotify.connected.v1"

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String
) {

    @Bean
    fun consumerFactory(@Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper): ConsumerFactory<String, UserConnectedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.TYPE_MAPPINGS to "UserConnectedEvent:event.UserConnectedEvent"
        )

        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, UserConnectedEvent>
    ): ConcurrentKafkaListenerContainerFactory<String, UserConnectedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserConnectedEvent>()
        factory.consumerFactory = consumerFactory

        factory.setCommonErrorHandler(DefaultErrorHandler().apply {
            setRetryListeners(
                RetryListener { record, ex, deliveryAttempt ->  // Явное указание SAM-типа
                    println("Failed record: ${record.key()}, attempt: $deliveryAttempt")
                }
            )
        })
        return factory
    }

}