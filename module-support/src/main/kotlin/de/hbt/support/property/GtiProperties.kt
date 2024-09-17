package de.hbt.support.property

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "de.hbt.support.gti")
@Schema(description = "Properties, to configure GTI service")
data class GtiProperties @ConstructorBinding constructor(
    val user: String,
    val secret: String
)
