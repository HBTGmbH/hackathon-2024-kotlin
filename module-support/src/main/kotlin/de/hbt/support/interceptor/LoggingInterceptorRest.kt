package de.hbt.support.interceptor

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.apache.logging.log4j.message.StringMapMessage
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StreamUtils
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Function

class LoggingInterceptorRest(
    private val requestLogBehaviour: FieldLogBehaviour,
    private val responseLogBehaviour: FieldLogBehaviour,
    val clock: Clock
) : Filter, ClientHttpRequestInterceptor {

    constructor(clock: Clock) : this(FieldLogBehaviour.NEVER, FieldLogBehaviour.NEVER, clock)

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
            request: ServletRequest, response: ServletResponse,
            chain: FilterChain
    ) {
        val requestTime = ZonedDateTime.now(clock)
        val httpRequest = request as HttpServletRequest
        val requestWrapper = ContentCachingRequestWrapper(httpRequest)
        val responseWrapper = ContentCachingResponseWrapper(
                (response as HttpServletResponse)
        )
        var fullResponseBytes: ByteArray? = null
        var throwable: Throwable? = null
        var responseBody: String? = null
        var httpStatusCode = -1
        try {
            try {
                chain.doFilter(
                        if (requestLogBehaviour != FieldLogBehaviour.NEVER) requestWrapper else httpRequest,
                        responseWrapper
                )
                if (responseLogBehaviour != FieldLogBehaviour.NEVER) {
                    fullResponseBytes = responseWrapper.contentAsByteArray
                }
                httpStatusCode = responseWrapper.status
            } finally {
                responseWrapper.copyBodyToResponse()
            }
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            try {
                val responseSize = responseWrapper.contentSize
                val responseHeaders = extractHeaders(
                        headerNames = responseWrapper.headerNames.iterator()
                ) { responseWrapper.getHeaders(it).iterator() }
                if (
                        (responseLogBehaviour == FieldLogBehaviour.ONLY_ON_ERROR && isError(httpStatusCode)
                                || responseLogBehaviour == FieldLogBehaviour.ALWAYS)
                        && fullResponseBytes != null
                ) {
                    responseBody = String(
                            fullResponseBytes,
                            determineResponseEncoding()
                    )
                }
                val query = if (httpRequest.queryString != null) "?${httpRequest.queryString}" else ""
                val requestUrl = URI.create("${httpRequest.requestURL}$query").toURL()
                val requestHeaders = extractHeaders(
                        headerNames = httpRequest.headerNames.asIterator(),
                        ignoreList = listOf("authorization")) {
                    httpRequest.getHeaders(it).asIterator()
                }
                var requestBody: String? = null
                var businessType: String? = null
                if (requestLogBehaviour == FieldLogBehaviour.ONLY_ON_ERROR && isError(httpStatusCode)
                        || requestLogBehaviour == FieldLogBehaviour.ALWAYS
                ) {
                    val fullRequestBytes = requestWrapper.contentAsByteArray
                    requestBody = String(fullRequestBytes, determineRequestEncoding())
                    businessType = determineBusinessType()
                }
                log(
                        LogMessage(
                                requestHeaders = requestHeaders,
                                responseHeaders = responseHeaders,
                                url = requestUrl,
                                method = httpRequest.method,
                                requestMimeType = typeToString(request.getContentType()),
                                responseMimeType = typeToString(response.getContentType()),
                                requestBody = requestBody,
                                responseBody = responseBody,
                                requestSize = httpRequest.contentLength,
                                responseSize = responseSize,
                                httpStatus = httpStatusCode,
                                direction = DIRECTION_IN,
                                requestTime = requestTime,
                                responseTime = ZonedDateTime.now(clock),
                                traceId = HeaderInterceptor.getTraceId(),
                                requestType = HeaderInterceptor.getRequestType(),
                                businessType = businessType,
                                throwable = throwable
                        )
                )
                val interceptorCloseables =
                        request.getAttribute(HeaderInterceptorRest.CLASS_NAME) as HeaderInterceptor.InterceptorCloseables
                interceptorCloseables.close()
            } catch (e: java.lang.RuntimeException) {
                log.error(e.toString(), e)
            }
        }
    }

    private fun isError(httpStatusCode: Int): Boolean {
        return httpStatusCode in 400..599
    }

    @Throws(IOException::class)
    override fun intercept(
            request: HttpRequest,
            requestBytes: ByteArray, execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val requestTime = ZonedDateTime.now(clock)
        var responseSize = 0
        var responseHeaders: Map<String, Collection<String>> = emptyMap()
        var responseMediaType: MediaType? = null
        var httpStatusCode = -1
        var throwable: Throwable? = null
        var requestBody: String? = null
        var responseBody: String? = null
        var businessType: String? = null
        return try {
            val result = BufferingClientHttpResponseWrapper(
                    execution.execute(request, requestBytes)
            )
            val responseBytes = StreamUtils.copyToByteArray(result.getBody())
            responseSize = responseBytes.size
            responseHeaders = extractHeaders(result.headers)
            responseMediaType = result.headers.getContentType()
            httpStatusCode = result.statusCode.value()
            if (responseLogBehaviour == FieldLogBehaviour.ONLY_ON_ERROR && isError(httpStatusCode)
                    || responseLogBehaviour == FieldLogBehaviour.ALWAYS
            ) {
                responseBody = String(
                        responseBytes,
                        determineRequestEncoding()
                )
            }
            result
        } catch (e: Exception) {
            throwable = e
            throw e
        } finally {
            try {
                val url = request.uri.toURL()
                val requestHeaders: Map<String, Collection<String>> = extractHeaders(request.headers)
                if (requestLogBehaviour == FieldLogBehaviour.ONLY_ON_ERROR && isError(httpStatusCode)
                        || requestLogBehaviour == FieldLogBehaviour.ALWAYS
                ) {
                    requestBody = String(
                            requestBytes,
                            determineRequestEncoding()
                    )
                    businessType = determineBusinessType()
                }
                log(
                        LogMessage(
                                requestHeaders = requestHeaders,
                                responseHeaders = responseHeaders,
                                url = url,
                                method = request.method.name(),
                                requestMimeType = typeToString(request.headers.getContentType()),
                                requestBody = requestBody,
                                responseBody = responseBody,
                                responseMimeType = typeToString(responseMediaType!!),
                                requestSize = requestBytes.size,
                                responseSize = responseSize,
                                httpStatus = httpStatusCode,
                                direction = DIRECTION_OUT,
                                requestTime = requestTime,
                                responseTime = ZonedDateTime.now(clock),
                                businessType = businessType,
                                throwable = throwable,
                                traceId = HeaderInterceptorRest.extractTraceId(request),
                                requestType = HeaderInterceptorRest.extractRequestType(request),
                        )
                )
            } catch (e: java.lang.RuntimeException) {
                log.error(e.toString(), e)
            }
        }
    }

    private fun log(logMessage: LogMessage) {
        val stringMapMessage = StringMapMessage()
        addLogString(
                stringMapMessage, PARAM_REQUEST_HEADERS,
                toHeaderString(logMessage.requestHeaders)
        )
        addLogString(
                stringMapMessage, PARAM_RESPONSE_HEADERS,
                toHeaderString(logMessage.responseHeaders)
        )
        addLogString(stringMapMessage, PARAM_URL_FULL, logMessage.url.toString())
        addLogString(stringMapMessage, PARAM_URL_DOMAIN, logMessage.url.host)
        addLogString(
                stringMapMessage, PARAM_URL_EXTENSION,
                extractExtension(logMessage.url.path)
        )
        addLogString(stringMapMessage, PARAM_URL_PATH, logMessage.url.path)
        addLogString(stringMapMessage, PARAM_URL_PORT, logMessage.url.port.toString())
        addLogString(stringMapMessage, PARAM_URL_SCHEME, logMessage.url.protocol)
        addLogString(stringMapMessage, PARAM_URL_QUERY, logMessage.url.query)
        addLogString(stringMapMessage, PARAM_REQUEST_METHOD, logMessage.method)
        addLogString(
                stringMapMessage, PARAM_REQUEST_REFERER,
                getHeader(logMessage.requestHeaders, HttpHeaders.REFERER)
        )
        addLogString(stringMapMessage, PARAM_REQUEST_MIMETYPE, logMessage.requestMimeType)
        addLogString(stringMapMessage, PARAM_RESPONSE_MIMETYPE, logMessage.responseMimeType)
        addLogString(stringMapMessage, PARAM_REQUEST_BYTES, logMessage.requestSize.toString())
        addLogString(stringMapMessage, PARAM_RESPONSE_BYTES, logMessage.responseSize.toString())
        addLogString(stringMapMessage, PARAM_RESPONSE_STATUS, logMessage.httpStatus.toString())
        addLogString(stringMapMessage, PARAM_DIRECTION, logMessage.direction)
        addLogString(stringMapMessage, PARAM_PROTOCOL, PROTOCOL_NAME)
        addLogString(
                stringMapMessage, PARAM_REQUEST_TIME,
                logMessage.requestTime.format(DATE_TIME_FORMATTER)
        )
        addLogString(
                stringMapMessage, PARAM_RESPONSE_TIME,
                logMessage.responseTime.format(DATE_TIME_FORMATTER)
        )
        addLogString(
                stringMapMessage,
                PARAM_DURATION,
                getDurationBetweenRequestAndResponseTime(logMessage).toNanos().toString()
        )
        addLogString(
                stringMapMessage, PARAM_USER_AGENT,
                getHeader(logMessage.requestHeaders, HttpHeaders.USER_AGENT)
        )
        addLogString(stringMapMessage, HeaderInterceptor.LOGGER_TRACE_ID, logMessage.traceId)
        addLogString(stringMapMessage, HeaderInterceptor.LOGGER_REQTYPE_ID, logMessage.requestType)
        addLogString(stringMapMessage, PARAM_BUSINESS_TYPE, logMessage.businessType)
        addLogString(stringMapMessage, PARAM_REQUEST_BODY, cutToMaxLength(logMessage.requestBody))
        addLogString(stringMapMessage, PARAM_RESPONSE_BODY, cutToMaxLength(logMessage.responseBody))
        log.debug(MARKER, stringMapMessage.asString(), logMessage.throwable)
    }

    private fun getDurationBetweenRequestAndResponseTime(logMessage: LogMessage): Duration {
        return Duration.between(logMessage.requestTime, logMessage.responseTime)
    }

    private fun getHeader(headers: Map<String, Collection<String?>>, headerKey: String): String? {
        return headers.entries.asSequence()
                .filter { it.key.equals(headerKey, ignoreCase = true) }
                .flatMap { it.value.asSequence() }
                .firstOrNull()
    }

    private fun addLogString(stringMapMessage: StringMapMessage, key: String, value: String?) {
        if (!value.isNullOrBlank()) {
            stringMapMessage.with(key, value.trim { it <= ' ' })
        }
    }

    /**
     * usually returns null, but can be overridden to implement more complex logic
     */
    private fun determineBusinessType(): String? {
        return null
    }

    /**
     * usually returns UTF-8, but can be overridden to implement more complex logic
     */
    private fun determineRequestEncoding(): Charset {
        return StandardCharsets.UTF_8
    }

    /**
     * usually returns UTF-8, but can be overridden to implement more complex logic
     */
    private fun determineResponseEncoding(): Charset {
        return StandardCharsets.UTF_8
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val MAX_LOG_SIZE = 20480 // 20 KB - logging could fail with bigger logmessages

        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val MARKER: Marker = MarkerFactory.getMarker("communication")

        const val DIRECTION_IN = "inbound"
        const val DIRECTION_OUT = "outbound"
        const val PROTOCOL_NAME = "http"

        const val PARAM_URL_FULL = "url.full"
        const val PARAM_URL_DOMAIN = "url.domain"
        const val PARAM_URL_EXTENSION = "url.extension"
        const val PARAM_URL_PATH = "url.path"
        const val PARAM_URL_PORT = "url.port"
        const val PARAM_URL_SCHEME = "url.scheme"
        const val PARAM_URL_QUERY = "url.query"
        const val PARAM_BUSINESS_TYPE = "http.request.type"
        const val PARAM_REQUEST_BODY = "http.request.body.content"
        const val PARAM_RESPONSE_BODY = "http.response.body.content"
        const val PARAM_RESPONSE_STATUS = "http.response.status_code"
        const val PARAM_REQUEST_HEADERS = "http.request.headers"
        const val PARAM_RESPONSE_HEADERS = "http.response.headers"
        const val PARAM_REQUEST_BYTES = "http.request.body.bytes"
        const val PARAM_RESPONSE_BYTES = "http.response.body.bytes"
        const val PARAM_REQUEST_MIMETYPE = "http.request.mime_type"
        const val PARAM_RESPONSE_MIMETYPE = "http.response.mime_type"
        const val PARAM_REQUEST_METHOD = "http.request.method"
        const val PARAM_REQUEST_REFERER = "http.request.referrer"
        const val PARAM_REQUEST_TIME = "event.start"
        const val PARAM_RESPONSE_TIME = "event.end"
        const val PARAM_DURATION = "event.duration"
        const val PARAM_USER_AGENT = "user_agent.original"
        const val PARAM_DIRECTION = "network.direction"
        const val PARAM_PROTOCOL = "network.protocol"

        private fun extractHeaders(
                headerNames: Iterator<String>,
                ignoreList: List<String> = emptyList(),
                headerValuesSupplier: Function<String, Iterator<String>>
        ): Map<String, MutableCollection<String>> {
            val requestHeaders: MutableMap<String, MutableCollection<String>> = mutableMapOf()
            while (headerNames.hasNext()) {
                val name = headerNames.next()
                if (name in ignoreList) continue
                val values = requestHeaders.computeIfAbsent(name) { mutableSetOf() }
                val headerValues = headerValuesSupplier.apply(name)
                while (headerValues.hasNext()) {
                    values.add(headerValues.next())
                }
            }
            return requestHeaders
        }

        private fun extractHeaders(headers: HttpHeaders): Map<String, Collection<String>> {
            val result: MutableMap<String, Collection<String>> = mutableMapOf()
            for ((key, value) in headers) {
                result[key] = value.toList()
            }
            return result
        }

        private fun toHeaderString(headerMap: Map<String, Collection<String>>): String {
            return headerMap.entries.asSequence()
                    .flatMap { entry ->
                        val key = entry.key
                        entry.value.asSequence().map { Pair(key, it) }
                    }
                    .map { "${it.first}=${it.second}" }
                    .joinToString(separator = ",")
        }

        private fun typeToString(contentType: String?): String? {
            return try {
                if (contentType == null) return null
                val mediaType = MediaType.parseMediaType(contentType)
                return typeToString(mediaType)
            } catch (e: InvalidMediaTypeException) {
                log.info(e.toString(), e)
                e.toString()
            }
        }

        private fun typeToString(mediaType: MediaType?): String? {
            return try {
                if (mediaType == null) return null
                "${mediaType.type}/${mediaType.subtype}"
            } catch (e: RuntimeException) {
                log.info(e.toString(), e)
                e.toString()
            }
        }

        private fun extractExtension(fileName: String?): String? {
            if (fileName == null || !fileName.contains(".")) return null
            return fileName.substring(fileName.lastIndexOf('.') + 1)
        }

        private fun cutToMaxLength(string: String?): String? {
            return if (string != null && string.length > MAX_LOG_SIZE) {
                string.substring(0, MAX_LOG_SIZE)
            } else string
        }

        private data class LogMessage(
                val requestHeaders: Map<String, Collection<String>>,
                val responseHeaders: Map<String, Collection<String>>,
                val url: URL,
                val method: String,
                val requestMimeType: String?,
                val responseMimeType: String?,
                val requestBody: String?,
                val responseBody: String?,
                val requestSize: Int,
                val responseSize: Int,
                val httpStatus: Int,
                val direction: String,
                val requestTime: ZonedDateTime,
                val responseTime: ZonedDateTime,
                val traceId: String?,
                val requestType: String?,
                val businessType: String?,
                val throwable: Throwable?
        )

        enum class FieldLogBehaviour {
            NEVER,
            ONLY_ON_ERROR,
            ALWAYS
        }

    }
}
