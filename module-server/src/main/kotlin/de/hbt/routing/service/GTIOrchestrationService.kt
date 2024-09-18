package de.hbt.routing.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.hbt.geofox.gti.model.GRResponse
import de.hbt.geofox.gti.model.GTITime
import de.hbt.geofox.gti.model.SDName
import de.hbt.routing.openai.OpenAIService
import de.hbt.routing.openai.dto.ChatRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Service
class GTIOrchestrationService(private val gtiService: GTIService,
                              private val openAIService: OpenAIService,
                              private val objectMapper: ObjectMapper) {

    companion object {
        private val log = KotlinLogging.logger {}
        private const val SYSTEM_PROMPT = """
You are a smart assistant that helps users with routing requests. Your goal is to give the user an advise which route to take based on the API response of a routing service.
Give a short and precise advise in {{LOCALE}}. It should include which exact transport line to take (Bus, U-Bahn, S-Bahn), where to change and when they arrive at the destination.
        """
    }

    data class ParsedInfo(val from: String, val to: String, val time: OffsetDateTime, val locale: Locale)

    fun orchestrate(input: ParsedInfo): String {
        val grResponse = doTheGRRequest(input)
        val jsonString = objectMapper.writeValueAsString(grResponse)
        return gpt(jsonString, input.locale)
    }

    fun gpt(jsonString: String, locale: Locale): String {
        val openAIResponse = openAIService.chat(ChatRequest.chatRequest(
                SYSTEM_PROMPT.replace("{{LOCALE}}", locale.getDisplayLanguage(Locale.ENGLISH)),
                jsonString))
        if (openAIResponse.choices.isEmpty()) {
            return "Empty response"
        }

        // return the first response
        val content = openAIResponse.choices.first().message.content
        return content
    }

    private fun doTheGRRequest(parsedJson: ParsedInfo): GRResponse {
        val start = SDName(name = parsedJson.from, type = SDName.Type.uNKNOWN)
        val dest = SDName(name = parsedJson.to, type = SDName.Type.uNKNOWN)
        val time = GTITime(date = parsedJson.time.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)), time = parsedJson.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
        val gtiResponse = gtiService.getRoute(start, dest, time)
        return gtiResponse
    }
}