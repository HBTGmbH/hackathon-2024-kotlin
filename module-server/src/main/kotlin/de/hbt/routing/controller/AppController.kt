package de.hbt.routing.controller

import de.hbt.routing.exception.ChatException
import de.hbt.routing.service.ConversationCache
import de.hbt.routing.service.GTIOrchestrationService
import de.hbt.routing.service.RoutingParametersService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestClientResponseException
import java.time.OffsetDateTime
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/chat")
@Tag(name = "Route Chat", description = "all requests that support sending route requests")
class AppController(private val routingParametersService: RoutingParametersService, private val gtiOrchestrationService: GTIOrchestrationService) {

    data class GtiErrorMessage(val returnCode: String, val errorText: String, val errorDevInfo: String?)
    data class RouteRequest(val requestId: String, val content: String)
    data class RouteSuggestion(val requestId: String, val content: RoutingParametersService.RoutingParameters)
    data class ErrorMessage(val requestId: String, val content: String)
    data class RoutingParameters(val start: String, val destination: String, val time: OffsetDateTime)
    data class CalculationRequest(val requestId: String, val content: RoutingParameters, val localeId: String?)
    data class CalculationResult(val requestId: String, val content: String)

    data class ConversationResponse(val requestId: String, val dialogues: List<ConversationCache.PromptAndAnswer>)

    @Operation(summary = "Submit route request", responses = [
        ApiResponse(responseCode = "200", description = "Route request successful", content = [Content(schema = Schema(implementation = RouteSuggestion::class))]),
        ApiResponse(responseCode = "500", description = "Internal server error during processing", content = [Content(
                schema = Schema(implementation = ErrorMessage::class),
        )]),
    ])
    @SecurityRequirement(name = "oauth2")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/route")
    fun submitRouteRequest(@RequestBody request: RouteRequest): ResponseEntity<RouteSuggestion> {
        val uuid = request.requestId.ifBlank { UUID.randomUUID().toString() }
        return try {
            val response = routingParametersService.getRoutingParameters(requestId = uuid, prompt = request.content)
            val successMessage = RouteSuggestion(requestId = uuid, content = response)
            ResponseEntity.ok(successMessage)
        } catch (e: Exception) {
            throw ChatException(uuid, HttpStatus.INTERNAL_SERVER_ERROR, e.localizedMessage, e)
        }
    }

    @Operation(summary = "Submit calculation request", responses = [
        ApiResponse(responseCode = "200", description = "Calculation request successful", content = [Content(schema = Schema(implementation = CalculationResult::class))]),
        ApiResponse(responseCode = "400", description = "Request was wrong", content = [Content(schema = Schema(implementation = ErrorMessage::class))]),
        ApiResponse(responseCode = "500", description = "Internal server error during processing", content = [Content(
                schema = Schema(implementation = ErrorMessage::class),
        )]),
    ])
    @SecurityRequirement(name = "oauth2")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/calculation")
    fun submitCalculationRequest(@RequestBody request: CalculationRequest): ResponseEntity<CalculationResult> {
        return try {
            val routeParameters = request.content
            val parsedInfo = GTIOrchestrationService.ParsedInfo(routeParameters.start, routeParameters.destination, routeParameters.time, locale(request))
            val response = gtiOrchestrationService.orchestrate(parsedInfo)
            val successMessage = CalculationResult(requestId = request.requestId, content = response)
            ResponseEntity.ok(successMessage)
        } catch (e: RestClientResponseException) {
            throw ChatException(request.requestId, e.statusCode, e.getResponseBodyAs(GtiErrorMessage::class.java)!!.errorText, e)
        } catch (e: Exception) {
            throw ChatException(request.requestId, HttpStatus.INTERNAL_SERVER_ERROR, e.localizedMessage, e)
        }
    }

    private fun locale(request: CalculationRequest): Locale {
        if (request.localeId == null) return Locale.GERMAN
        return Locale.of(request.localeId) ?: Locale.GERMAN
    }


    @Operation(summary = "Retrieve conversation", responses = [ApiResponse(responseCode = "200", description = "Conversation existed and is returned", content = [Content(schema = Schema(implementation = ConversationResponse::class))])])
    @SecurityRequirement(name = "oauth2")
    @SecurityRequirement(name = "bearerAuth")
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