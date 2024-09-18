package de.hbt.routing.exception

import org.springframework.http.HttpStatus

class RouteRequestException(val requestId: String, val status: HttpStatus, message: String,
                            e: Exception): RuntimeException(message, e)