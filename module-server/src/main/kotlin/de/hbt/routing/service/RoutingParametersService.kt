package de.hbt.routing.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.hbt.routing.openai.OpenAIService
import de.hbt.routing.openai.dto.ChatRequest.Companion.chatRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class RoutingParametersService(private val conversationCache: ConversationCache, private val openAIService: OpenAIService, private val objectMapper: ObjectMapper) {

    data class RoutingParameters(val start: String?, val destination: String?, val time: String?) {}

    fun getRoutingParameters(requestId: String, prompt: String): RoutingParameters {
        val request = chatRequest(generateSystemPrompt(), prompt)
        val openAIResponse = openAIService.chat(request)
        if (openAIResponse.choices.isEmpty()) {
            return RoutingParameters("", "", "")
        }
        conversationCache.addToConversation(requestId, ConversationCache.PromptAndAnswer(request, openAIResponse))

        // return the first response as JSON
        val content = openAIResponse.choices.first().message.content
        val routingParameters = objectMapper.readValue(content, RoutingParameters::class.java)
        return routingParameters
    }

    private fun generateSystemPrompt(): String {
        val zone = ZoneId.of("Europe/Berlin")
        val now = LocalDateTime.now().atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return """
You are a smart assistant that extracts three key parameters from user queries: start location, destination, and time. Always return a structured JSON with these fields:

Start: Where the journey begins.
Destination: Where the user wants to go.
Time: When the user wants to travel.
If start or destination are missing, set it to null. Handle informal language and adjust time to UTC.
Use the next AM/PM if applicable, and for "now" use $now.
If only a day is given, assume the current time on that day. If no time can be inferred, set it to now.

Return a plain unformatted json (no markdown).
Example output: {"start": "New York", "destination": "Boston", "time": "$now"}
        """
    }

    fun getConversation(requestId: String): List<ConversationCache.PromptAndAnswer> {
        return conversationCache.getConversation(requestId)
    }
}