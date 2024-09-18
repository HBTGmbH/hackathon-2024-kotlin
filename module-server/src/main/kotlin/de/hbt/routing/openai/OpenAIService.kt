package de.hbt.routing.openai

import de.hbt.routing.openai.dto.ChatRequest
import de.hbt.routing.openai.dto.ChatResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OpenAIService(@Qualifier("openaiRestTemplate") private val restTemplate: RestTemplate) {

    fun chat(prompt: String): String {
        // create a request
        val request = ChatRequest("gpt-3.5-turbo", prompt)

        // call the API
        val response = restTemplate.postForObject("https://api.openai.com/v1/chat/completions", request, ChatResponse::class.java)

        if (response?.choices == null || response.choices.isEmpty()) {
            return "No response"
        }

        // return the first response
        return response.choices.first().message.content
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}