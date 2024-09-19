package de.hbt.routing.exception

import de.hbt.routing.controller.AppController
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(0)
class ExceptionHandler {
    @ExceptionHandler(ChatException::class)
    fun handleChatException(e: ChatException): ResponseEntity<AppController.ErrorMessage> {
        val errorMessage = AppController.ErrorMessage(requestId = e.requestId,
                content = e.localizedMessage)

        return ResponseEntity(errorMessage, e.status)
    }

    @ExceptionHandler(GtiException::class)
    fun handleGtiException(e: GtiException): ResponseEntity<AppController.ErrorMessage> {
        val errorMessage = AppController.ErrorMessage(e.requestId, e.gtiErrorMessage.errorText)

        return ResponseEntity(errorMessage, e.statusCode)
    }
}