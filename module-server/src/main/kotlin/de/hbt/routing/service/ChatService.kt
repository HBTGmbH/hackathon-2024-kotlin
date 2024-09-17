package de.hbt.routing.service

import de.hbt.routing.service.helper.ConversationCache

interface ChatService {
    data class Response(val requestId: String, val response: String)
    fun processPrompt(requestId: String, prompt: String): Response
    fun getConversation(requestId: String): List<ConversationCache.PromptAndAnswer>
}