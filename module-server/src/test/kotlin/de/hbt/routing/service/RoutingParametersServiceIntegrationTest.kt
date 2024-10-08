package de.hbt.routing.service

import de.hbt.routing.configuration.OpenAIRestTemplateConfig
import de.hbt.routing.openai.OpenAIService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.stream.Stream


@SpringBootTest(classes = [RoutingParametersService::class, ConversationCache::class, OpenAIService::class, OpenAIRestTemplateConfig::class])
@Disabled("manual")
class RoutingParametersServiceIntegrationTest {

    private val REQUEST_ID = "123"

    @Autowired
    var routingParametersService: RoutingParametersService? = null

    @ParameterizedTest
    @MethodSource("data")
    fun getRoutingParameters(prompt: String, result: RoutingParametersService.RoutingParameters) {
        //given
        assertThat(routingParametersService).isNotNull

        //when
        val routingParameters = routingParametersService?.getRoutingParameters(REQUEST_ID, prompt)

        //then
        assertThat(routingParameters)
                .isNotNull
                .satisfies(timeIsCloseToExpected(result.time!!))
                .satisfies({ assertThat(it?.start).isEqualTo(result.start) })
                .satisfies({ assertThat(it?.destination).isEqualTo(result.destination) })
    }

    private fun timeIsCloseToExpected(expectedTime: String): (input: RoutingParametersService.RoutingParameters?) -> Unit =
            {
                assertThat(it?.time).isNotEmpty
                val timeActual = OffsetDateTime.parse(it?.time!!)
                val timeExpected = OffsetDateTime.parse(expectedTime)
                assertThat(timeActual).isCloseTo(timeExpected, within(30, ChronoUnit.SECONDS))
            }

    companion object {
        private val ZONE = ZoneId.of("Europe/Berlin")
        private val NOW_TIME = LocalDateTime.now().atZone(ZONE)
        private val NOW = NOW_TIME.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        private val TOMORROW_AT_8 = NOW_TIME.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        private val TOMORROW_AT_THE_SAME_TIME = NOW_TIME.plusDays(1)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        private val IN_TWO_DAYS_AT_12 = NOW_TIME.plusDays(2).withHour(12).withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        @JvmStatic
        fun data(): Stream<Arguments> = Stream.of(
                of("Wie komme ich morgen um 8 vom Grüningweg zur Holmer Straße in Wedel?",
                        result("Grüningweg", "Holmer Straße, Wedel", TOMORROW_AT_8)),
                of("Vom Hauptbahnhof zur Stadthausbrücke",
                        result("Hauptbahnhof", "Stadthausbrücke", NOW)),
                of("I am in Ahrensburg. When can I get the next connection to Lüneburg?",
                        result("Ahrensburg", "Lüneburg", NOW)),
                of("Wie komme ich jetzt von der Mühlenstraße zum Rathausplatz in Hamburg?",
                        result("Mühlenstraße", "Rathausplatz, Hamburg", NOW)),
                of("Ab Schanzenviertel nach St. Pauli",
                        result("Schanzenviertel", "St. Pauli", NOW)),
                of("Ich möchte gerne zum Altonaer Fischmarkt vom Jungfernstieg.",
                        result("Jungfernstieg", "Altonaer Fischmarkt", NOW)),
                of("Vom Hauptbahnhof zur Stadthausbrücke morgen",
                        result("Hauptbahnhof", "Stadthausbrücke", TOMORROW_AT_THE_SAME_TIME)),
                of("Vom Schulterblatt zur Grindelallee übermorgen um 12 Uhr",
                        result("Schulterblatt", "Grindelallee", IN_TWO_DAYS_AT_12)),
                of("Eh, Digga, bist du Aldi? Wo geht? Ich Edeka.",
                        result("Edeka", "Aldi", NOW)),
        )

        private fun result(
                start: String,
                destination: String,
                time: String?
        ) = RoutingParametersService.RoutingParameters(start = start, destination = destination, time = time)

    }
}