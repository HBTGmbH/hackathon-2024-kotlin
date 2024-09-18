package de.hbt.routing.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
open class OpenAIRestTemplateConfig(@Value("\${openai.api.token}") private val apiKey: String) {

    @Bean
    @Qualifier("openaiRestTemplate")
    open fun openaiRestTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.add("Authorization", "Bearer $apiKey")
            execution.execute(request, body!!)
        })
        return restTemplate
    }

    @Bean
    open fun jacksonObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerKotlinModule()
            registerModule(JavaTimeModule())
        }
    }
}