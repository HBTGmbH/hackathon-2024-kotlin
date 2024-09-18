package de.hbt.routing.service

import de.hbt.routing.openai.dto.ChatRequest
import de.hbt.routing.openai.dto.ChatResponse
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ConversationCache {

    data class PromptAndAnswer(val prompt: ChatRequest, val answer: ChatResponse)

    private val conversations = ConcurrentHashMap<String, MutableList<PromptAndAnswer>>()

    fun addToConversation(requestId: String, dialogue: PromptAndAnswer) {
        conversations.computeIfAbsent(requestId) { mutableListOf() }.add(dialogue)
    }

    fun getConversation(requestId: String): List<PromptAndAnswer> {
        return conversations[requestId] ?: emptyList()
    }
}
