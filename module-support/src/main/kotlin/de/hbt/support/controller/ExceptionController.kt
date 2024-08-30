package de.hbt.support.controller

import de.hbt.support.exception.HttpStatusException
import de.hbt.support.model.dto.ErrorMessage
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice(annotations = [RestController::class])
class ExceptionController : ResponseEntityExceptionHandler() {
    @ExceptionHandler(HttpStatusException::class)
    fun handleException(e: HttpStatusException): ResponseEntity<ErrorMessage> {
        if (e.cause != null) {
            log.info(e.cause.toString(), e.cause)
        } else {
            log.info(e.toString())
        }
        return ResponseEntity.status(e.status)
                .body(ErrorMessage(e.message!!))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ErrorMessage> {
        log.error("unexpected exception occurred", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorMessage(message = e.message!!))
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}