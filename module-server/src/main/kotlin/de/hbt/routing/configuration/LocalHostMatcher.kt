package de.hbt.routing.configuration

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component

@Component
class LocalHostMatcher: RequestMatcher {
    override fun matches(request: HttpServletRequest): Boolean {
        val host = request.localAddr
        return host == "127.0.0.1" || host == "::1"
    }
}