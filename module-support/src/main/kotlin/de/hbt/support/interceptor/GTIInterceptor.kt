package de.hbt.support.interceptor

import de.hbt.support.property.GtiProperties
import mu.KotlinLogging
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class GTIInterceptor(private val properties: GtiProperties) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers.set("geofox-auth-user", properties.user)
        request.headers.set("geofox-auth-signature", computeHmacSHA1(body, properties.secret))
        return execution.execute(request, body)
    }

    private fun computeHmacSHA1(body: ByteArray, key: String): String {
        try {
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA1")
            mac.init(secretKeySpec)
            val digest = mac.doFinal(body)
            return Base64.getEncoder().encodeToString(digest)
        } catch (e: IllegalArgumentException) {
            logger.error("Error computing hmac", e)
            return ""
        } catch (e: IllegalStateException) {
            logger.error("Error computing hmac", e)
            return ""
        } catch (e: InvalidKeyException) {
            logger.error("Error computing hmac", e)
            return ""
        } catch (e: NoSuchAlgorithmException) {
            logger.error("Error computing hmac", e)
            return ""
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
