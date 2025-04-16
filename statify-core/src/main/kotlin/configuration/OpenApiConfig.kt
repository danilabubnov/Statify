package org.danila.configuration

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenApiCustomizer? {
        return OpenApiCustomizer { openApi ->
            openApi.addSecurityItem(SecurityRequirement().addList("bearerAuth"))
        }
    }

}