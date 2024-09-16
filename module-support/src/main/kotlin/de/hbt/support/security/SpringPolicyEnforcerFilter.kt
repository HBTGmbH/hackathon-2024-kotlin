package de.hbt.support.security

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.keycloak.AuthorizationContext
import org.keycloak.adapters.authorization.PolicyEnforcer
import org.keycloak.adapters.authorization.integration.elytron.ServletHttpRequest
import org.keycloak.adapters.authorization.integration.elytron.ServletHttpResponse
import org.keycloak.adapters.authorization.spi.ConfigurationResolver
import org.keycloak.adapters.authorization.spi.HttpRequest
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


class SpringPolicyEnforcerFilter(private val configResolver: ConfigurationResolver) : Filter {
    private val policyEnforcer: MutableMap<PolicyEnforcerConfig, SpringPolicyEnforcer> = ConcurrentHashMap()

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse?, filterChain: FilterChain) {
        val request = servletRequest as HttpServletRequest
        val response = servletResponse as HttpServletResponse?
        val httpRequest = ServletHttpRequest(request) { extractBearerToken(request) }
        val policyEnforcer = getOrCreatePolicyEnforcer(httpRequest)
        val authzContext = policyEnforcer.enforce(httpRequest, ServletHttpResponse(response))
        request.setAttribute(AuthorizationContext::class.java.name, authzContext)
        if (authzContext.isGranted) {
            log.debug("Request authorized, continuing the filter chain")
            filterChain.doFilter(servletRequest, servletResponse)
        } else {
            log.debug("Unauthorized request to path [{}], aborting the filter chain", request.requestURI)
        }
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val authorizationHeaderValues = request.getHeaders("Authorization")
        while (authorizationHeaderValues.hasMoreElements()) {
            val value = authorizationHeaderValues.nextElement()
            val parts = value.trim()
                    .split("\\s+".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (parts.size != 2) {
                continue
            }
            val bearer = parts[0]
            if (bearer.equals("Bearer", ignoreCase = true)) {
                return parts[1]
            }
        }
        return null
    }

    private fun getOrCreatePolicyEnforcer(request: ServletHttpRequest): SpringPolicyEnforcer {
        return policyEnforcer.computeIfAbsent(configResolver.resolve(request)) { createPolicyEnforcer(it) }
    }

    private fun createPolicyEnforcer(enforcerConfig: PolicyEnforcerConfig): SpringPolicyEnforcer {
        val authServerUrl = enforcerConfig.authServerUrl
        return SpringPolicyEnforcer(PolicyEnforcer.builder()
                .authServerUrl(authServerUrl)
                .realm(enforcerConfig.realm)
                .clientId(enforcerConfig.resource)
                .credentials(enforcerConfig.credentials)
                .bearerOnly(false)
                .enforcerConfig(enforcerConfig).build(), enforcerConfig)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}