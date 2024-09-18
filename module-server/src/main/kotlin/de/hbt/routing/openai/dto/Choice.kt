package de.hbt.routing.openai.dto

import lombok.Builder
import lombok.extern.jackson.Jacksonized

data class Choice(val index: Int, val message: Message)