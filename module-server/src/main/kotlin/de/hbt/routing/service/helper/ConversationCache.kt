package de.hbt.routing.service.helper

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ConversationCache {

    data class PromptAndAnswer(val prompt: String, val answer: String)

    private val conversations = ConcurrentHashMap<String, MutableList<PromptAndAnswer>>()

    fun addToConversation(requestId: String, dialogue: PromptAndAnswer) {
        conversations.computeIfAbsent(requestId) { mutableListOf() }.add(dialogue)
    }

    fun getConversation(requestId: String): List<PromptAndAnswer> {
        return conversations[requestId] ?: emptyList()
    }
}
