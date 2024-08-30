package de.hbt.support.controller

import de.hbt.support.property.HealthCheckProperties
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/routing"])
class HealthCheckController(private val properties: HealthCheckProperties) {


    @Hidden
    @GetMapping("/healthCheck")
    fun healthCheck(message: String): String {
        return properties.greeting.format(message)
    }
}