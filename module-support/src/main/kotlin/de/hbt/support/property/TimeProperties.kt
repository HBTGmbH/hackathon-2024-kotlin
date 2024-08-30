package de.hbt.support.property

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.cloud.context.config.annotation.RefreshScope
import java.time.ZoneId

@RefreshScope
@ConfigurationProperties(prefix = "time")
@Schema(description = "Properties to configure time")
data class TimeProperties @ConstructorBinding constructor(
        val defaultTimeZone: ZoneId
)
