package de.hbt.support.configuration

import de.hbt.support.interceptor.GTIInterceptor
import de.hbt.support.interceptor.HeaderInterceptorRest
import de.hbt.support.interceptor.LoggingInterceptorRest
import de.hbt.support.property.GtiProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
open class InterceptorConfiguration {
    @Bean
    open fun loggingInterceptorRest(clock: Clock): LoggingInterceptorRest {
        return LoggingInterceptorRest(clock)
    }

    @Bean
    open fun headerInterceptorRest(): HeaderInterceptorRest {
        return HeaderInterceptorRest()
    }

    @Bean
    open fun gtiInterceptor(properties: GtiProperties): GTIInterceptor {
        return GTIInterceptor(properties)
    }
}