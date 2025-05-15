package org.danila.configuration

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(factory: RedisConnectionFactory): RedisCacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(15))
            .disableCachingNullValues()

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .build()
    }
}