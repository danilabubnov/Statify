package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

const val USER_SPOTIFY_CONNECTED_TOPIC = "user.spotify.connected.v1"
const val USER_SPOTIFY_CONNECTED_DLT = "$USER_SPOTIFY_CONNECTED_TOPIC.DLT"

@Configuration
class KafkaConfig(

    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,

) {

    @Bean
    fun producerFactory(@Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            JsonSerializer.TYPE_MAPPINGS to "UserConnectedEvent:event.UserConnectedEvent",
            ProducerConfig.ACKS_CONFIG to "all"
        )

        return DefaultKafkaProducerFactory(
            config,
            StringSerializer(),
            JsonSerializer(objectMapper)
        )
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>) = KafkaTemplate(producerFactory)

    @Bean
    fun kafkaAdmin(): KafkaAdmin = KafkaAdmin(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)).also { it.setAutoCreate(true) }

    @Bean
    fun topics(): KafkaAdmin.NewTopics = KafkaAdmin.NewTopics(
        TopicBuilder.name(USER_SPOTIFY_CONNECTED_TOPIC)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .build(),
        TopicBuilder.name(USER_SPOTIFY_CONNECTED_DLT)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "1209600000") // 14 days
            .build(),
    )

}