package de.hbt.routing.service

import com.nimbusds.jose.shaded.gson.Gson
import de.hbt.geofox.gti.model.GRResponse
import de.hbt.geofox.gti.model.GTITime
import de.hbt.geofox.gti.model.SDName
import de.hbt.routing.openai.OpenAIService
import de.hbt.routing.openai.dto.ChatRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class GTIOrchestrationService(private val gtiService: GTIService,
                              private val openAIService: OpenAIService) {

    companion object {
        private val log = KotlinLogging.logger {}
        private const val SYSTEM_PROMPT = """
You are a smart assistant that helps users with parsing some routing information.
Your goal is to receive a json object and convert the information to natural language.
        """
    }

    data class ParsedInfo(val from: String, val to: String, val time: OffsetDateTime)

    fun orchestrate(input: ParsedInfo): String {
        val grResponse = doTheGRRequest(input)
        val gson = Gson()
        val jsonString = gson.toJson(grResponse)
        val openAIResponse = openAIService.chat(ChatRequest.chatRequest(SYSTEM_PROMPT, jsonString))
        if (openAIResponse.choices.isEmpty()) {
            return "Empty response"
        }

        // return the first response
        val content = openAIResponse.choices.first().message.content
        return content
    }

    fun doTheGRRequest(parsedJson: ParsedInfo): GRResponse {
        val start = SDName(name = parsedJson.from, type = SDName.Type.uNKNOWN)
        val dest = SDName(name = parsedJson.to, type = SDName.Type.uNKNOWN)
        val time = GTITime(date = parsedJson.time.toLocalDate().toString(), time = parsedJson.time.toLocalTime().toString())
        val gtiResponse = gtiService.getRoute(start, dest, time)
        return gtiResponse
    }
}