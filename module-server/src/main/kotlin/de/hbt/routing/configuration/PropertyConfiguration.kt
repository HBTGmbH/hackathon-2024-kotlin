package de.hbt.routing.configuration

import de.hbt.routing.property.ServiceProperties
import de.hbt.support.property.HealthCheckProperties
import de.hbt.support.property.RestTemplateTimeoutProperties
import de.hbt.support.property.StatusProbeProperties
import de.hbt.support.property.TimeProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(RestTemplateTimeoutProperties::class,
        StatusProbeProperties::class, TimeProperties::class,
        HealthCheckProperties::class, ServiceProperties::class)
open class PropertyConfiguration
