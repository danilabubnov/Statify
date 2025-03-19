package config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonConfiguration {

    @Bean
    open fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(
            Hibernate5JakartaModule().configure(
                Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false
            )
        )
        .registerModule(JavaTimeModule())
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

}
