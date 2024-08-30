package de.hbt.support.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
open class FilterConfiguration {
    @Bean
    open fun forwardedFilter(): ForwardedHeaderFilter {
        return ForwardedHeaderFilter()
    }
}