package de.hbt.routing.openai

import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OpenAIService {
    private val apiKey: String by lazy { System.getenv("OPENAI_API_TOKEN") }

    fun call() {
        log.info("Hallo, ich rufe bei OpenAI an")
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}