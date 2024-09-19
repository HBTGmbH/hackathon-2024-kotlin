package de.hbt.routing.exception

import org.springframework.http.HttpStatusCode

class ChatException(val requestId: String, val status: HttpStatusCode, message: String,
                    e: Exception): RuntimeException(message, e)