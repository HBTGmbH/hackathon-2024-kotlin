package de.hbt.routing.service

import de.hbt.routing.service.helper.ConversationCache
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatServiceImpl(private val conversationCache: ConversationCache) : ChatService {

    override fun processPrompt(requestId: String, prompt: String): ChatService.Response {
        val uuid = if (requestId.isBlank()) UUID.randomUUID().toString() else requestId
        val mockResponse = "This is a mock response for prompt: $prompt"
        val response = ChatService.Response(uuid, mockResponse)

        conversationCache.addToConversation(uuid, ConversationCache.PromptAndAnswer(prompt, mockResponse))

        return response
    }
}