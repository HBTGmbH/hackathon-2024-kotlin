package de.hbt.routing.controller

import de.hbt.geofox.gti.model.InitResponse
import de.hbt.routing.service.ChatService
import de.hbt.routing.service.helper.ConversationCache
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/chat")
@Tag(name = "Route Chat", description = "all requests that support sending route requests")
class AppController(private val chatService: ChatService) {

    data class ChatRequest(val requestId: String, val prompt: String)
    data class ChatResponse(val requestId: String, val answer: String)
    data class ConversationResponse(val requestId: String, val dialogues: List<ConversationCache.PromptAndAnswer>)

    @Operation(
            summary = "Send route request",
            responses = [ApiResponse(
                    responseCode = "200",
                    description = "Route request successful",
                    content = [Content(
                            schema = Schema(implementation = ChatResponse::class)
                    )]
            )]
    )
    @PostMapping("/send")
    fun sendPrompt(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        return try {
            val response = chatService.processPrompt(requestId = request.requestId, prompt = request.prompt)
            ResponseEntity.ok(ChatResponse(response.requestId, response.response))
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while processing request" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ChatResponse(request.requestId, "Error: ${e.message}"))
        }
    }

    @Operation(
            summary = "Retrieve conversation",
            responses = [ApiResponse(
                    responseCode = "200",
                    description = "Conversation existed and is returned",
                    content = [Content(
                            schema = Schema(implementation = ConversationResponse::class)
                    )]
            )]
    )
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