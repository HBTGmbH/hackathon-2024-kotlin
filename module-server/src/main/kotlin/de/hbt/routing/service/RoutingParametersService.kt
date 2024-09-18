package de.hbt.routing.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.hbt.routing.openai.OpenAIService
import de.hbt.routing.openai.dto.ChatRequest.Companion.chatRequest
import org.springframework.stereotype.Service

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
        private const val SYSTEM_PROMPT = """
You are a smart assistant that helps users with routing requests. Your goal is to extract three key parameters from each user query: the start location, destination location, and time. When a user provides a routing request, parse the following:

Start: The location where the journey begins (e.g., city, point of interest, address).
Destination: The location where the user wants to go (e.g., city, point of interest, address).
Time: When the user wants to travel (e.g., specific time, day, or general time frame).
Your task is to always output a structured JSON object with these three fields. If a parameter is missing or unclear, indicate it as null. Make sure to handle a variety of natural language inputs, including informal and conversational language.

For example:

Input: 'How do I get from New York to Boston at 9 AM?'
Output: {"start": "New York", "destination": "Boston", "time": "9 AM"}
If the user does not specify a time, mark it as null.

If the input is ambiguous, do your best to infer the meaning and provide the most likely interpretation.

Be concise, clear, and precise in your extraction. Return a plain unformatted json (no markdown)."
        """
    }
}