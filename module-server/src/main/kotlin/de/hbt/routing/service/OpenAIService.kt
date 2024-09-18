package de.hbt.routing.service

import de.hbt.routing.property.ServiceProperties
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OpenAIService(private val serviceProperties: ServiceProperties) {

    fun call() {
        log.info("Hallo, ich rufe bei OpenAI an")
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}