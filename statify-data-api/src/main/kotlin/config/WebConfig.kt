package org.danila.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/graphql")
            .allowedOrigins(
                "https://studio.apollographql.com",
                "http://localhost:8081",
                "http://localhost:5173",
                "https://localhost",
                "http://localhost"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true)
    }

}
