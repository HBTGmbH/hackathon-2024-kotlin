package de.hbt.routing.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.hbt.geofox.gti.model.Coordinate
import de.hbt.geofox.gti.model.GRResponse
import de.hbt.geofox.gti.model.GTITime
import de.hbt.geofox.gti.model.SDName
import de.hbt.routing.openai.OpenAIService
import de.hbt.routing.openai.dto.ChatRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.regex.Pattern

@Service
class GTIOrchestrationService(private val gtiService: GTIService,
                              private val openAIService: OpenAIService,
                              private val objectMapper: ObjectMapper) {


    companion object {
        private val log = KotlinLogging.logger {}
        val GTI_ZONE: ZoneId = ZoneId.of("Europe/Berlin")
        val GTI_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.GERMAN).withZone(GTI_ZONE)
        val GTI_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.GERMAN).withZone(GTI_ZONE)
        val WGS84_LAT_LON_PATTERN: Pattern = Pattern.compile("lat:\\s*[+]?(-?\\d*[,.]?\\d+)\\s+lon:\\s*[+]?(-?\\d*[,.]?\\d+)", Pattern.CASE_INSENSITIVE)
    }

    data class ParsedInfo(val from: String, val to: String, val time: OffsetDateTime, val locale: Locale)

    fun orchestrate(input: ParsedInfo): String {
        val grResponse = doTheGRRequest(input)
        val jsonString = objectMapper.writeValueAsString(grResponse)
        return gpt(jsonString, input.locale)
    }

    private fun gpt(jsonString: String, locale: Locale): String {
        val openAIResponse = openAIService.chat(ChatRequest.chatRequest(
                generateSystemPrompt().replace("{{LOCALE}}", locale.getDisplayLanguage(Locale.ENGLISH)),
                jsonString))
        if (openAIResponse.choices.isEmpty()) {
            return "Empty response"
        }

        // return the first response
        val content = openAIResponse.choices.first().message.content
        return content
    }

    private fun generateSystemPrompt(): String {
        return """
You are a smart assistant that helps users with routing requests. Your goal is to give the user an advise which route to take based on the API response of a routing service.
Give a short and precise advise in {{LOCALE}}. It should include which exact transport line to take (Bus, U-Bahn, S-Bahn), where to change and when they arrive at the destination.
        """
    }

    private fun doTheGRRequest(parsedJson: ParsedInfo): GRResponse {

        val start = buildSDName(parsedJson.from)
        val dest = buildSDName(parsedJson.to)
        val date = parsedJson.time.format(GTI_DATE_FORMATTER)
        val time = parsedJson.time.format(GTI_TIME_FORMATTER)
        val gtiTime = GTITime(date = date, time = time)
        val gtiResponse = gtiService.getRoute(start, dest, gtiTime)
        return gtiResponse
    }

    private fun buildSDName(value: String): SDName {
        val matcher = WGS84_LAT_LON_PATTERN.matcher(value)
        return if (matcher.matches()) {
            SDName(coordinate = Coordinate(y = matcher.group(1).toDouble(), x = matcher.group(2).toDouble()), type = SDName.Type.cOORDINATE)
        } else {
            SDName(name = value, type = SDName.Type.uNKNOWN)
        }
    }
}