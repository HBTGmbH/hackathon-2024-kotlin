package de.hbt.routing.exception

import de.hbt.routing.controller.AppController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {
    @ExceptionHandler
    fun handleChatException(e: ChatException): ResponseEntity<AppController.ErrorMessage> {
        val errorMessage = AppController.ErrorMessage(requestId = e.requestId,
                content = e.localizedMessage)

        return ResponseEntity(errorMessage, e.status)
    }
}