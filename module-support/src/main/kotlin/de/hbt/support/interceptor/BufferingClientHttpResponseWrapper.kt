package de.hbt.support.interceptor

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class BufferingClientHttpResponseWrapper(private val response: ClientHttpResponse) : ClientHttpResponse {
    private var body: ByteArray? = null

    @Throws(IOException::class)
    override fun getStatusCode(): HttpStatusCode {
        return response.statusCode
    }

    @Throws(IOException::class)
    override fun getStatusText(): String {
        return response.statusText
    }

    override fun getHeaders(): HttpHeaders {
        return response.headers
    }

    @Throws(IOException::class)
    override fun getBody(): InputStream {
        if (body == null) {
            body = StreamUtils.copyToByteArray(response.body)
        }
        return ByteArrayInputStream(body)
    }

    override fun close() {
        response.close()
    }
}