package de.hbt.routing.configuration

import de.hbt.support.interceptor.HeaderInterceptorRest
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebConfiguration(private val headerInterceptorRest: HeaderInterceptorRest) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(headerInterceptorRest)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        val registration = registry.addMapping("/**")
        registration.allowedMethods(
                HttpMethod.GET.name(), HttpMethod.POST.name(),
                HttpMethod.PUT.name(), HttpMethod.HEAD.name(),
                HttpMethod.DELETE.name()
        )
        registration.allowCredentials(true)
        registration.allowedOrigins(
                "http://localhost:4200",
                "https://timetable.2martens.de"
        )
    }
}
