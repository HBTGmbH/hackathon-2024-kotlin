package de.hbt.routing.openai

import de.hbt.routing.openai.dto.ChatRequest
import de.hbt.routing.openai.dto.ChatResponse
import de.hbt.routing.openai.dto.Message
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class OpenAIService(@Qualifier("openaiRestTemplate") private val restTemplate: RestTemplate) {

    fun chat(prompt: String): String {
        // create a request
        val request = ChatRequest(MODEL, prompt, "user")
        request.messages.addFirst(SYSTEM_PROMPT_MESSAGE)

        // call the API
        val response = restTemplate.postForObject(COMPLETIONS_API, request, ChatResponse::class.java)

        if (response?.choices == null || response.choices.isEmpty()) {
            return "No response"
        }

        // return the first response
        return response.choices.first().message.content
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val MODEL = "gpt-4o"
        private const val COMPLETIONS_API = "https://api.openai.com/v1/chat/completions"
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
        private val SYSTEM_PROMPT_MESSAGE = Message("system", SYSTEM_PROMPT)
    }
}