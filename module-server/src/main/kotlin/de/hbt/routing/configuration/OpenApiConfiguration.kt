package de.hbt.routing.configuration

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = OAuthFlows(
                implicit = OAuthFlow(
                        authorizationUrl = "https://id.2martens.de/realms/2martens/protocol/openid-connect/auth",
                        tokenUrl = "https://id.2martens.de/realms/2martens/protocol/openid-connect/token"
                )
        )
)
@Configuration
open class OpenApiConfiguration {
    @Bean
    open fun customOpenAPI(
            @Value("\${openapi.description}") apiDescription: String,
            @Value("\${openapi.version}") apiVersion: String, @Value("\${openapi.title}") apiTitle: String
    ): OpenAPI {
        return OpenAPI()
                .info(
                        Info()
                                .title(apiTitle)
                                .version(apiVersion)
                                .description(apiDescription)
                )
    }
}
