package de.hbt.routing.gti

import de.hbt.geofox.gti.model.InitResponse
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/gti"])
@Tag(name = "Gti", description = "all requests that proxy GTI")
class GtiProxyController(private val gtiService: GTIService) {

    @Operation(
            summary = "Perform GTI init request",
            responses = [ApiResponse(
                    responseCode = "200",
                    description = "Init request successful",
                    content = [Content(
                            schema = Schema(implementation = InitResponse::class)
                    )]
            )]
    )
    @Hidden
    @GetMapping("/init")
    fun init(): ResponseEntity<InitResponse> {
        val initResponse = gtiService.init()
        return ResponseEntity.ok(initResponse)
    }
}