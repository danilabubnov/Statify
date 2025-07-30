package org.danila.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/graphql")
                    .allowedOrigins(
                        "https://studio.apollographql.com",
                        "http://localhost:8081"
                    )
                    .allowedMethods("POST", "OPTIONS")
                    .allowCredentials(true)
                    .allowedHeaders("*")
            }
        }
    }

}
