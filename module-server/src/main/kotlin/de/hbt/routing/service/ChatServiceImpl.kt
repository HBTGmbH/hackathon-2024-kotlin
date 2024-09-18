package de.hbt.routing.service

import de.hbt.routing.openai.OpenAIService
import de.hbt.routing.service.helper.ConversationCache
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatServiceImpl(private val conversationCache: ConversationCache, private val openAIService: OpenAIService) : ChatService {

    override fun processPrompt(requestId: String, prompt: String): ChatService.Response {
        val uuid = requestId.ifBlank { UUID.randomUUID().toString() }
        val openAIResponse = openAIService.chat(prompt)
        val response = ChatService.Response(uuid, openAIResponse)

        conversationCache.addToConversation(uuid, ConversationCache.PromptAndAnswer(prompt, openAIResponse))

        return response
    }

    override fun getConversation(requestId: String): List<ConversationCache.PromptAndAnswer> {
        return conversationCache.getConversation(requestId)
    }
}