package org.danila.configuration.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import event.UserConnectedEvent
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.danila.configuration.constants.CommonDurations.FOURTEEN_DAYS_IN_MS
import org.danila.configuration.constants.CommonDurations.SEVEN_DAYS_IN_MS
import org.danila.configuration.constants.kafka.KafkaTopics.ALBUM_ENRICH_DLT
import org.danila.configuration.constants.kafka.KafkaTopics.ALBUM_ENRICH_TOPIC
import org.danila.configuration.constants.kafka.KafkaTopics.ARTIST_ENRICH_DLT
import org.danila.configuration.constants.kafka.KafkaTopics.ARTIST_ENRICH_TOPIC
import org.danila.configuration.constants.kafka.KafkaTopics.TRACK_ENRICH_DLT
import org.danila.configuration.constants.kafka.KafkaTopics.TRACK_ENRICH_TOPIC
import org.danila.configuration.constants.kafka.KafkaTopics.USER_SPOTIFY_CONNECTED_TOPIC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) {

    // ------------------ CONSUMER CONFIG ------------------

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
    @DependsOn("kafkaAdmin")
    fun reactiveKafkaConsumer(
        receiverOptions: ReceiverOptions<String, UserConnectedEvent>,
    ): ReactiveKafkaConsumerTemplate<String, UserConnectedEvent> {
        return ReactiveKafkaConsumerTemplate(receiverOptions)
    }

    @Bean
    fun allEnrichReceiverOptions(): ReceiverOptions<String, Any> {
        val props = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "$groupId‑all‑enrich",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.TYPE_MAPPINGS to """
                EnrichArtistEvent:org.danila.event.EnrichArtistEvent,
                EnrichAlbumEvent:org.danila.event.EnrichAlbumEvent,
                EnrichTrackEvent:org.danila.event.EnrichTrackEvent
            """.trimIndent()
        )

        return ReceiverOptions.create<String, Any>(props)
            .subscription(listOf(ARTIST_ENRICH_TOPIC, ALBUM_ENRICH_TOPIC, TRACK_ENRICH_TOPIC))
    }

    @Bean
    @DependsOn("kafkaAdmin")
    fun enrichConsumer(
        allEnrichReceiverOptions: ReceiverOptions<String, Any>,
    ): ReactiveKafkaConsumerTemplate<String, Any> {
        return ReactiveKafkaConsumerTemplate(allEnrichReceiverOptions)
    }

    // ------------------ PRODUCER CONFIG ------------------

    @Bean
    fun reactiveSenderOptions(@Qualifier("kafkaObjectMapper") objectMapper: ObjectMapper): SenderOptions<String, Any> =
        SenderOptions.create<String, Any>(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG   to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                JsonSerializer.TYPE_MAPPINGS                to """
                    EnrichArtistEvent:org.danila.event.EnrichArtistEvent,
                    EnrichAlbumEvent:org.danila.event.EnrichAlbumEvent,
                    EnrichTrackEvent:org.danila.event.EnrichTrackEvent
                """.trimIndent()
            )
        )
            .withKeySerializer(StringSerializer())
            .withValueSerializer(JsonSerializer(objectMapper))

    @Bean
    fun reactiveKafkaProducerTemplate(senderOptions: SenderOptions<String, Any>): ReactiveKafkaProducerTemplate<String, Any> =
        ReactiveKafkaProducerTemplate(senderOptions)

    // ------------------ TOPICS ------------------

    @Bean
    fun kafkaAdmin(): KafkaAdmin = KafkaAdmin(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)).also { it.setAutoCreate(true) }

    @Bean
    fun topics(): KafkaAdmin.NewTopics = KafkaAdmin.NewTopics(
        TopicBuilder.name(ARTIST_ENRICH_TOPIC)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, SEVEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(ARTIST_ENRICH_DLT)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, FOURTEEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(ALBUM_ENRICH_TOPIC)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, SEVEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(ALBUM_ENRICH_DLT)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, FOURTEEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(TRACK_ENRICH_TOPIC)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, SEVEN_DAYS_IN_MS.toString())
            .build(),
        TopicBuilder.name(TRACK_ENRICH_DLT)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, FOURTEEN_DAYS_IN_MS.toString())
            .build()
    )

}