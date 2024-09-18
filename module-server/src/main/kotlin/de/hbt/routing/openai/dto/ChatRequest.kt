package de.hbt.routing.openai.dto

data class ChatRequest(val model: String, val n: Int, val temperature: Double, val messages: MutableList<Message?>) {
    constructor(model: String, prompt: String, role: String) : this(model, 1, 0.0, ArrayList<Message?>()) {
        messages.add(Message(role, prompt))
    }
}