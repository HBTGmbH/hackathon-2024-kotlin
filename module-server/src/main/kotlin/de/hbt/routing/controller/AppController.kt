package de.hbt.routing.controller

import de.hbt.routing.service.ChatService
import de.hbt.routing.service.helper.ConversationCache
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/chat")
class AppController(private val chatService: ChatService) {

    data class ChatRequest(val requestId: String, val prompt: String)
    data class ChatResponse(val requestId: String, val answer: String)
    data class ConversationResponse(val requestId: String, val dialogues: List<ConversationCache.PromptAndAnswer>)

    @PostMapping("/send")
    fun sendPrompt(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        return try {
            val response = chatService.processPrompt(requestId = request.requestId, prompt = request.prompt)
            ResponseEntity.ok(ChatResponse(response.requestId, response.response))
        } catch (e: Exception) {
            logger.error(e) { "Exception occured while processing request" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ChatResponse(request.requestId, "Error: ${e.message}"))
        }
    }

    @GetMapping("/conversation/{requestId}")
    fun getConversation(@PathVariable requestId: String): ResponseEntity<ConversationResponse> {
        return try {
            val dialogues = chatService.getConversation(requestId)
            ResponseEntity.ok(ConversationResponse(requestId, dialogues))
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while retrieving conversation for requestId: $requestId" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ConversationResponse(requestId, emptyList()))
        }
    }
}