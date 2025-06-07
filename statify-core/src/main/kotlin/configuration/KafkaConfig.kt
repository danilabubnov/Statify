package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import constants.CommonDurations.FOURTEEN_DAYS_IN_MS
import constants.CommonDurations.SEVEN_DAYS_IN_MS
import constants.kafka.KafkaTopics.USER_SPOTIFY_CONNECTED_DLT
import constants.kafka.KafkaTopics.USER_SPOTIFY_CONNECTED_TOPIC
import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_DLT
import constants.kafka.KafkaTopics.USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC
import event.UserSpotifyLibraryStatusUpdatedEvent
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig(

    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,

    ) {

    // ------------------ CONSUMER CONFIG ------------------

    @Bean
    @DependsOn("kafkaAdmin")
    fun consumerFactory(): ConsumerFactory<String, UserSpotifyLibraryStatusUpdatedEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.TYPE_MAPPINGS to "UserSpotifyLibraryStatusUpdatedEvent:event.UserSpotifyLibraryStatusUpdatedEvent"
        )

        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    @DependsOn("kafkaAdmin")
    fun deadLetterPublishingRecoverer(kafkaTemplate: KafkaTemplate<String, Any>): DeadLetterPublishingRecoverer {
        return DeadLetterPublishingRecoverer(kafkaTemplate) { consumerRecord, exception ->
            TopicPartition("${consumerRecord.topic()}.DLT", consumerRecord.partition())
        }
    }

    @Bean
    @DependsOn("kafkaAdmin")
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, UserSpotifyLibraryStatusUpdatedEvent>,
        kafkaTemplate: KafkaTemplate<String, Any>
    ): ConcurrentKafkaListenerContainerFactory<String, UserSpotifyLibraryStatusUpdatedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserSpotifyLibraryStatusUpdatedEvent>()

        val recoverer = deadLetterPublishingRecoverer(kafkaTemplate)
        val errorHandler = DefaultErrorHandler(recoverer, FixedBackOff(5000, 3))

        errorHandler.setRetryListeners(
            { record, ex, deliveryAttempt ->
                println("Failed record: ${record.key()}, attempt: $deliveryAttempt") // TODO: logs
            }
        )

        factory.consumerFactory = consumerFactory
        factory.setCommonErrorHandler(errorHandler)

        return factory
    }

    // ------------------ PRODUCER CONFIG ------------------

    @Bean
    fun producerFactory(@Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper): ProducerFactory<String, Any> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            JsonSerializer.TYPE_MAPPINGS to """
                UserConnectedEvent:event.UserConnectedEvent,
                UserSpotifyLibraryStatusUpdatedEvent:event.UserSpotifyLibraryStatusUpdatedEvent
            """.trimIndent(),
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

    // ------------------ TOPICS ------------------

    @Bean
    fun kafkaAdmin(): KafkaAdmin = KafkaAdmin(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)).also { it.setAutoCreate(true) }

    @Bean
    fun topics(): KafkaAdmin.NewTopics = KafkaAdmin.NewTopics(
        TopicBuilder.name(USER_SPOTIFY_CONNECTED_TOPIC)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, SEVEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(USER_SPOTIFY_CONNECTED_DLT)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, FOURTEEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(USER_SPOTIFY_LIBRARY_STATUS_UPDATED_TOPIC)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, SEVEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(USER_SPOTIFY_LIBRARY_STATUS_UPDATED_DLT)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, FOURTEEN_DAYS_IN_MS.toString())
            .build(),
    )

}