package de.hbt.support.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

@ConfigurationProperties(prefix = "resttemplate.timeout")
data class RestTemplateTimeoutProperties @ConstructorBinding constructor(
        @DurationUnit(ChronoUnit.MILLIS) val readTimeoutRestHealthIndicatorInMillis: Duration,
        @DurationUnit(ChronoUnit.MILLIS) val connectionRestHealthIndicatorTimeoutInMillis: Duration,
        @DurationUnit(ChronoUnit.MILLIS) val readTimeoutRestTemplateInMillis: Duration,
        @DurationUnit(ChronoUnit.MILLIS) val connectionRestTemplateTimeoutInMillis: Duration
)