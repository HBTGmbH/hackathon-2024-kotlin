package de.hbt.routing.openai.dto

import lombok.Builder
import lombok.extern.jackson.Jacksonized

data class ChatResponse(val choices: List<Choice>)