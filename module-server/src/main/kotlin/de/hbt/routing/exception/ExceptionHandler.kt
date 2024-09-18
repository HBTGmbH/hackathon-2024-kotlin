package de.hbt.routing.exception

import de.hbt.routing.controller.AppController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler
    fun handleStatusException(e: RouteRequestException): ResponseEntity<AppController.ErrorMessage> {
        val errorMessage = AppController.ErrorMessage(AppController.Message(requestId = e.requestId,
                content = e.localizedMessage, role = "assistant"))

        return ResponseEntity(errorMessage, e.status)
    }
}