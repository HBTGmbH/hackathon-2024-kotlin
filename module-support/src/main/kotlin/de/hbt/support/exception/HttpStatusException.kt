package de.hbt.support.exception

import org.springframework.http.HttpStatus

class HttpStatusException(val status: HttpStatus, message: String, cause: Throwable?) :
        RuntimeException(message, cause)