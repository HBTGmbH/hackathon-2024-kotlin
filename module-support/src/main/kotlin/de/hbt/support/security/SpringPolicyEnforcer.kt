package de.hbt.support.security

import mu.KotlinLogging
import org.keycloak.AuthorizationContext
import org.keycloak.adapters.authorization.PolicyEnforcer
import org.keycloak.adapters.authorization.integration.elytron.ServletHttpRequest
import org.keycloak.adapters.authorization.spi.HttpRequest
import org.keycloak.adapters.authorization.spi.HttpResponse
import org.keycloak.authorization.client.ClientAuthorizationContext
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig.EnforcementMode
import org.keycloak.representations.idm.authorization.Permission

class SpringPolicyEnforcer(private val policyEnforcer: PolicyEnforcer,
                           private val policyEnforcerConfig: PolicyEnforcerConfig) {

    fun enforce(request: ServletHttpRequest, response: HttpResponse): AuthorizationContext {
        if (log.isDebugEnabled) {
            log.debug("Policy enforcement is enabled. Enforcing policy decisions for path [{}].", request.uri)
        }
        val context = authorize(request, response)
        if (log.isDebugEnabled) {
            log.debug("Policy enforcement result for path [{}] is : {}", request.uri, if (context.isGranted) "GRANTED" else "DENIED")
            log.debug("Returning authorization context with permissions:")
            for (permission in context.permissions) {
                log.debug(permission.toString())
            }
        }
        return context
    }

    private fun authorize(request: HttpRequest, response: HttpResponse): AuthorizationContext {
        val enforcementMode = policyEnforcerConfig.enforcementMode
        return if (EnforcementMode.DISABLED == enforcementMode) {
            createAuthorizedContext()
        } else policyEnforcer.enforce(request, response)
    }

    private fun createAuthorizedContext(): AuthorizationContext {
        return object : ClientAuthorizationContext(policyEnforcer.authzClient) {
            override fun hasPermission(resourceName: String, scopeName: String): Boolean {
                return true
            }

            override fun hasResourcePermission(resourceName: String): Boolean {
                return true
            }

            override fun hasScopePermission(scopeName: String): Boolean {
                return true
            }

            override fun getPermissions(): List<Permission> {
                return emptyList()
            }

            override fun isGranted(): Boolean {
                return true
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}