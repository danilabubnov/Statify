package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import event.TokenCredentials
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class CacheConfig {

    @Bean
    fun reactiveRedisTemplate(@Qualifier("redisObjectMapper") redisObjectMapper: ObjectMapper, factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, TokenCredentials> {
        val serializer = Jackson2JsonRedisSerializer(redisObjectMapper, TokenCredentials::class.java)

        val ctx = RedisSerializationContext
            .newSerializationContext<String, TokenCredentials>(StringRedisSerializer())
            .value(serializer)
            .build()

        return ReactiveRedisTemplate(factory, ctx)
    }

}