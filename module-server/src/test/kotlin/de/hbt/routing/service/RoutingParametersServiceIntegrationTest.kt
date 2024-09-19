package de.hbt.routing.service

import de.hbt.routing.configuration.OpenAIRestTemplateConfig
import de.hbt.routing.openai.OpenAIService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@SpringBootTest(classes = [RoutingParametersService::class, ConversationCache::class, OpenAIService::class, OpenAIRestTemplateConfig::class])
@Disabled("manual")
class RoutingParametersServiceIntegrationTest {

    private val REQUEST_ID = "123"
    private val ZONE = ZoneId.of("Europe/Berlin")
    private val NOW = LocalDateTime.now().atZone(ZONE)
    private val TOMORROW_AT_8 = NOW.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0)

    @Autowired
    var routingParametersService: RoutingParametersService? = null

    @Autowired
    var conversationCache: ConversationCache? = null

    @Test
    fun getRoutingParameters() {
        //given
        assertThat(routingParametersService).isNotNull

        //when
        val prompt = "Wie komme ich morgen um 8 vom Grüningweg zur Holmer Straße in Wedel?"
        val routingParameters = routingParametersService?.getRoutingParameters(REQUEST_ID, prompt)

        //then
        val start = "Grüningweg"
        val destination = "Holmer Straße, Wedel"
        val time = TOMORROW_AT_8.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        assertThat(routingParameters)
                .isNotNull
                .isEqualTo(RoutingParametersService.RoutingParameters(start = start, destination = destination, time = time))

        assertThat(conversationCache?.getConversation(REQUEST_ID))
                .isNotNull
                .hasSize(1)
    }
}