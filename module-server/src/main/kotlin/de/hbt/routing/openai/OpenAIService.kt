package de.hbt.routing.openai

import de.hbt.routing.openai.dto.ChatRequest
import de.hbt.routing.openai.dto.ChatResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class OpenAIService(@Qualifier("openaiRestTemplate") private val restTemplate: RestTemplate) {

    companion object {
        private val log = KotlinLogging.logger {}
        private const val COMPLETIONS_API = "https://api.openai.com/v1/chat/completions"
    }

    fun chat(request: ChatRequest): ChatResponse {
        val response = restTemplate.postForObject(COMPLETIONS_API, request, ChatResponse::class.java)
        return response ?: throw IllegalStateException("Invalid response from OpenAI")
    }
}