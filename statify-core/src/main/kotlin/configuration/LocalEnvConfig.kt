package org.danila.configuration

import io.github.cdimascio.dotenv.DotenvBuilder
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
class LocalEnvConfig {

    @PostConstruct
    fun loadDotEnv() {
        DotenvBuilder()
            .systemProperties()
            .ignoreIfMissing()
            .load()
    }

}