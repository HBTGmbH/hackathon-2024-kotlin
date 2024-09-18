package de.hbt.routing.controller

import de.hbt.routing.exception.ChatException
import de.hbt.routing.service.ConversationCache
import de.hbt.routing.service.GTIOrchestrationService
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
class AppController(private val routingParametersService: RoutingParametersService,
                    private val gtiOrchestrationService: GTIOrchestrationService) {

    data class Message<T>(val requestId: String, val content: T, val role: String)
    data class RouteRequest(val message: Message<String>)
    data class RouteSuggestion(val message: Message<RoutingParametersService.RoutingParameters>)
    data class ErrorMessage(val message: Message<String>)
    data class RoutingParameters(val start: String, val destination: String, val time: String)
    data class CalculationRequest(val message: Message<RoutingParameters>)
    data class CalculationResult(val message: Message<String>)

    data class ConversationResponse(val requestId: String, val dialogues: List<ConversationCache.PromptAndAnswer>)

    @Operation(
            summary = "Submit route request",
            responses = [
                ApiResponse(
                        responseCode = "200",
                        description = "Route request successful",
                        content = [Content(
                                schema = Schema(implementation = RouteSuggestion::class)
                        )]
                ),
                ApiResponse(
                        responseCode = "500",
                        description = "Internal server error during processing",
                        content = [Content(
                                schema = Schema(implementation = ErrorMessage::class),
                        )]
                ),
            ]
    )
    @PostMapping("/route")
    fun submitRouteRequest(@RequestBody request: RouteRequest): ResponseEntity<RouteSuggestion> {
        val uuid = request.message.requestId.ifBlank { UUID.randomUUID().toString() }
        return try {
            val response = routingParametersService.getRoutingParameters(requestId = uuid,
                    prompt = request.message.content)
            val successMessage = Message(requestId = uuid, content = response,
                    role = "assistant")
            ResponseEntity.ok(RouteSuggestion(message = successMessage))
        } catch (e: Exception) {
            throw ChatException(uuid, HttpStatus.INTERNAL_SERVER_ERROR,
                    e.localizedMessage, e)
        }
    }

    @Operation(
            summary = "Submit calculation request",
            responses = [
                ApiResponse(
                        responseCode = "200",
                        description = "Calculation request successful",
                        content = [Content(
                                schema = Schema(implementation = CalculationResult::class)
                        )]
                ),
                ApiResponse(
                        responseCode = "500",
                        description = "Internal server error during processing",
                        content = [Content(
                                schema = Schema(implementation = ErrorMessage::class),
                        )]
                ),
            ]
    )
    @PostMapping("/calculation")
    fun submitCalculationRequest(@RequestBody request: CalculationRequest): ResponseEntity<CalculationResult> {
        return try {
            val routeParameters = request.message.content
            val parsedInfo = GTIOrchestrationService.ParsedInfo(routeParameters.start,
                    routeParameters.destination, routeParameters.time)
            val response = gtiOrchestrationService.orchestrate(parsedInfo)
            val successMessage = Message(requestId = request.message.requestId, content = response,
                    role = "assistant")
            ResponseEntity.ok(CalculationResult(message = successMessage))
        } catch (e: Exception) {
            throw ChatException(request.message.requestId, HttpStatus.INTERNAL_SERVER_ERROR,
                    e.localizedMessage, e)
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