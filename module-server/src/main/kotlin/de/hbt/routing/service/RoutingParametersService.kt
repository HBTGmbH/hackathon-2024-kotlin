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
        val request = chatRequest(prompt, SYSTEM_PROMPT)
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

    fun getConversation(requestId: String): List<ConversationCache.PromptAndAnswer> {
        return conversationCache.getConversation(requestId)
    }

    companion object {
        private val ZONE = ZoneId.of("Europe/Berlin")
        private val NOW = LocalDateTime.now().atZone(ZONE)
        private val TEST_TIME = NOW.withHour(21).withMinute(0).withSecond(0).withNano(0)
        private val SYSTEM_PROMPT = """
You are a smart assistant that helps users with routing requests. Your goal is to extract three key parameters from each user query: the start location, destination location, and time. When a user provides a routing request, parse the following:

Start: The location where the journey begins (e.g., city, point of interest, address).
Destination: The location where the user wants to go (e.g., city, point of interest, address).
Time: When the user wants to travel (e.g., specific time, day, or general time frame).
Your task is to always output a structured JSON object with these three fields. If a parameter is missing or unclear, indicate it as null. Make sure to handle a variety of natural language inputs, including informal and conversational language.

For example:

Input: How do I get from New York to Boston at 9 PM?
Output: {"start": "New York", "destination": "Boston", "time": "${TEST_TIME.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"}
Assume that the user means the next AM or PM time and adjust the time by adding 12 hours if necessary.
Always format the time as a UTC timestamp.
If the user asks for the current time (now, today, jetzt, heute), use ${NOW.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}.
If only a day is given, assume the current time at that day.
If no time can be deducted, mark it as null.

If the input is ambiguous, do your best to infer the meaning and provide the most likely interpretation.

Be concise, clear, and precise in your extraction. Return a plain unformatted json (no markdown).
        """
    }
}