package de.hbt.support.configuration

import de.hbt.support.interceptor.GTIInterceptor
import de.hbt.support.interceptor.HeaderInterceptorRest
import de.hbt.support.interceptor.LoggingInterceptorRest
import de.hbt.support.property.RestTemplateTimeoutProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
import org.springframework.web.client.RestClient

@Configuration
open class RestClientConfiguration {
    @Bean("restClient")
    open fun restClient(
        headerInterceptorRest: HeaderInterceptorRest,
        loggingInterceptor: LoggingInterceptorRest,
        gtiInterceptor: GTIInterceptor,
        restTemplateTimeoutProperties: RestTemplateTimeoutProperties
    ): RestClient {
        return RestClient.builder()
                .messageConverters { it.add(Jaxb2RootElementHttpMessageConverter()) }
                .requestInterceptors { it.addAll(listOf(headerInterceptorRest, gtiInterceptor, loggingInterceptor)) }
                .build()
    }
}