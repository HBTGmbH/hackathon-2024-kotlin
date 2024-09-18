package de.hbt.routing.controller

import de.hbt.routing.service.ConversationCache
import de.hbt.routing.service.RoutingParametersService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/chat")
@Tag(name = "Route Chat", description = "all requests that support sending route requests")
class AppController(private val routingParametersService: RoutingParametersService) {

    data class RoutingParametersRequest(val requestId: String, val prompt: String)
    data class RoutingParametersResponse(val requestId: String, val routingParameters: RoutingParametersService.RoutingParameters?)
    data class ConversationResponse(val requestId: String, val dialogues: List<ConversationCache.PromptAndAnswer>)

    @Operation(
            summary = "Send route request",
            responses = [ApiResponse(
                    responseCode = "200",
                    description = "Route request successful",
                    content = [Content(
                            schema = Schema(implementation = RoutingParametersResponse::class)
                    )]
            )]
    )
    @PostMapping("/send")
    fun routingParametersRequest(@RequestBody request: RoutingParametersRequest): ResponseEntity<RoutingParametersResponse> {
        return try {
            val uuid = request.requestId.ifBlank { UUID.randomUUID().toString() }
            val response = routingParametersService.getRoutingParameters(requestId = uuid, prompt = request.prompt)
            ResponseEntity.ok(RoutingParametersResponse(uuid, response))
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while processing request" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RoutingParametersResponse(request.requestId, null))
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
            val dialogues = routingParametersService.getConversation(requestId)
            ResponseEntity.ok(ConversationResponse(requestId, dialogues))
        } catch (e: Exception) {
            logger.error(e) { "Exception occurred while retrieving conversation for requestId: $requestId" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ConversationResponse(requestId, emptyList()))
        }
    }
}