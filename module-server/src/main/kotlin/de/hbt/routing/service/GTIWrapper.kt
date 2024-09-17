package de.hbt.routing.service

import mu.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

@Service
class GTIWrapper {

    fun sign(body: String, key: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.add("geofox-auth-user", "hbt47")
        headers.add("geofox-auth-signature", computeHmacSHA1(body, key))
        headers.contentType = MediaType.APPLICATION_JSON
        val entity: HttpEntity<String> = HttpEntity(body, headers)
        return entity
    }

    private fun computeHmacSHA1(body: String, key: String): String {
        try {
            val mac = Mac.getInstance("HmacSHA1")
            val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA1")
            mac.init(secretKeySpec)
            val digest = mac.doFinal(body.toByteArray(StandardCharsets.UTF_8))
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
}