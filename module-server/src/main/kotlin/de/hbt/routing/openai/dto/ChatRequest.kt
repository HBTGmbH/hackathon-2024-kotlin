package de.hbt.routing.openai.dto

data class ChatRequest(val model: String, val n: Int, val temperature: Double, val messages: MutableList<Message>) {

    companion object {
        private const val MODEL = "gpt-4o"
        fun chatRequest(systemPrompt: String, userPrompt: String): ChatRequest {
            val messages = ArrayList<Message>()
            messages.add(Message("system", systemPrompt))
            messages.add(Message("user", userPrompt))
            return ChatRequest(MODEL, 1, 0.0, messages)
        }
    }
}