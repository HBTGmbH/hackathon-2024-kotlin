package de.hbt.routing.controller

import de.hbt.routing.service.ChatService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/chat")
class AppController(private val chatService: ChatService) {

    data class ChatRequest(val requestId: String, val prompt: String)
    data class ChatResponse(val requestId: String, val answer: String)

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
}