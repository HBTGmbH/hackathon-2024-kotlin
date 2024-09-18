package de.hbt.routing.service

import com.nimbusds.jose.shaded.gson.Gson
import de.hbt.geofox.gti.model.GRResponse
import de.hbt.geofox.gti.model.GTITime
import de.hbt.geofox.gti.model.SDName
import de.hbt.routing.openai.dto.ChatRequest
import de.hbt.routing.openai.dto.ChatResponse
import de.hbt.routing.openai.dto.Message
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GTIOrchestrationService(private val gtiService: GTIService,
                              @Qualifier("openaiRestTemplate") private val restTemplate: RestTemplate) {

    data class ParsedInfo(val from: String, val to: String, val time: String)

    fun orchestrate(input: ParsedInfo): String {
        val grResponse = doTheGRRequest(input)
        val gson = Gson()
        val response = chat(gson.toJson(grResponse))
        return response
    }

    fun doTheGRRequest(parsedJson: ParsedInfo): GRResponse {
        val start = SDName(name = parsedJson.from, type = SDName.Type.uNKNOWN)
        val dest = SDName(name = parsedJson.to, type = SDName.Type.uNKNOWN)
        val time = GTITime(date = "heute", time = parsedJson.time)
        val gtiResponse = gtiService.getRoute(start, dest, time)
        return gtiResponse
    }

    fun chat(prompt: String): String {
        // create a request
        val request = ChatRequest(MODEL, prompt, "user")
        request.messages.addFirst(SYSTEM_PROMPT_MESSAGE)

        // call the API
        val response = restTemplate.postForObject(COMPLETIONS_API, request, ChatResponse::class.java)

        if (response?.choices == null || response.choices.isEmpty()) {
            return "No response"
        }

        // return the first response
        return response.choices.first().message.content
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val MODEL = "gpt-4o"
        private const val COMPLETIONS_API = "https://api.openai.com/v1/chat/completions"
        private const val SYSTEM_PROMPT = """
You are a smart assistant that helps users with parsing some routing information.
Your goal is to receive a json object and convert the information to natural language.
        """
        private val SYSTEM_PROMPT_MESSAGE = Message("system", SYSTEM_PROMPT)
    }
}