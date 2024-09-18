package de.hbt.routing.openai.dto

import lombok.Builder
import lombok.extern.jackson.Jacksonized

data class Message(val role: String, val content: String)