package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import event.AccessTokenUpdatedEvent
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.RetryListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

const val USER_SPOTIFY_CONNECTED_TOPIC = "user.spotify.connected.v1"
const val USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC = "user.spotify.access.token.updated.v1"

@Configuration
class KafkaConfig(

    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String

) {

    @Bean
    fun producerFactory(@Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            JsonSerializer.TYPE_MAPPINGS to "UserConnectedEvent:event.UserConnectedEvent"
        )

        return DefaultKafkaProducerFactory(
            config,
            StringSerializer(),
            JsonSerializer<Any>(objectMapper)
        )
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>) = KafkaTemplate(producerFactory)

    @Bean
    fun kafkaAdmin(): KafkaAdmin = KafkaAdmin(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers))

    @Bean
    fun topics(): Collection<NewTopic> = listOf(
        TopicBuilder.name(USER_SPOTIFY_CONNECTED_TOPIC)
            .partitions(3)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .build(),
        TopicBuilder.name(USER_SPOTIFY_ACCESS_TOKEN_UPDATED_TOPIC)
            .partitions(3)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 дней
            .build()
    )

    @Bean
    fun consumerFactory(): ConsumerFactory<String, AccessTokenUpdatedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.TYPE_MAPPINGS to "AccessTokenUpdatedEvent:event.AccessTokenUpdatedEvent"
        )

        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, AccessTokenUpdatedEvent>): ConcurrentKafkaListenerContainerFactory<String, AccessTokenUpdatedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, AccessTokenUpdatedEvent>()

        factory.consumerFactory = consumerFactory
        factory.setCommonErrorHandler(DefaultErrorHandler().apply {
            setRetryListeners(
                RetryListener { record, ex, deliveryAttempt ->
                    println("Failed record: ${record.key()}, attempt: $deliveryAttempt") // TODO: logs
                }
            )
        })

        return factory
    }

}