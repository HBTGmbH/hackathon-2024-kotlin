package de.hbt.support.configuration

import de.hbt.support.interceptor.GTIInterceptor
import de.hbt.support.interceptor.HeaderInterceptorRest
import de.hbt.support.interceptor.LoggingInterceptorRest
import de.hbt.support.property.RestTemplateTimeoutProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
open class RestTemplateConfiguration {
    @Bean("restTemplate")
    open fun restTemplate(
        headerInterceptorRest: HeaderInterceptorRest,
        loggingInterceptor: LoggingInterceptorRest,
        gtiInterceptor: GTIInterceptor,
        restTemplateTimeoutProperties: RestTemplateTimeoutProperties
    ): RestTemplate {
        return RestTemplateBuilder()
                .additionalInterceptors(headerInterceptorRest, loggingInterceptor, gtiInterceptor)
                .setConnectTimeout(restTemplateTimeoutProperties.connectionRestTemplateTimeoutInMillis)
                .setReadTimeout(restTemplateTimeoutProperties.readTimeoutRestTemplateInMillis)
                .build()
    }

    @Bean("restTemplateRestHealthIndicator")
    open fun restTemplateRestHealthIndicator(
        headerInterceptorRest: HeaderInterceptorRest,
        restTemplateTimeoutProperties: RestTemplateTimeoutProperties
    ): RestTemplate {
        return RestTemplateBuilder()
                .additionalInterceptors(headerInterceptorRest)
                .setConnectTimeout(restTemplateTimeoutProperties.connectionRestHealthIndicatorTimeoutInMillis)
                .setReadTimeout(restTemplateTimeoutProperties.readTimeoutRestHealthIndicatorInMillis)
                .build()
    }
}