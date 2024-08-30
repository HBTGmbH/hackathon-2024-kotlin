package de.hbt.support.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.servlet.HandlerInterceptor
import java.io.IOException
import java.util.*

class HeaderInterceptorRest : HeaderInterceptor(), HandlerInterceptor, ClientHttpRequestInterceptor {
    companion object {
        val CLASS_NAME: String = HeaderInterceptorRest::class.java.getName()

        fun extractTraceId(request: HttpServletRequest): String {
            var traceId = request.getHeader(HEADER_FIELD_TRACE_ID)
            if (traceId.isNullOrBlank()) traceId = request.getHeader(HEADER_FIELD_B3_TRACE_ID)
            if (traceId.isNullOrBlank()) return createNewTraceId()
            return traceId
        }

        fun extractTraceId(request: HttpRequest): String {
            var traceId = request.headers[HEADER_FIELD_TRACE_ID]?.first()
            if (traceId.isNullOrBlank()) traceId = request.headers[HEADER_FIELD_B3_TRACE_ID]?.first()
            if (traceId.isNullOrBlank()) return UUID.randomUUID().toString()
            return traceId
        }

        fun extractRequestType(request: HttpServletRequest): String? {
            val type = request.getHeader(HEADER_FIELD_TYPE_ID)
            if (type.isNullOrBlank()) return null
            return type
        }

        fun extractRequestType(request: HttpRequest): String? {
            val type = request.headers[HEADER_FIELD_TYPE_ID]?.first()
            if (type.isNullOrBlank()) return null
            return type
        }
    }

    // ClientHttpRequestInterceptor
    @Throws(IOException::class)
    override fun intercept(
            request: HttpRequest, body: ByteArray,
            execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.add(HEADER_FIELD_TRACE_ID, getTraceId())
        val requestType = getRequestType()
        if (requestType != null) {
            request.headers.add(HEADER_FIELD_TYPE_ID, requestType)
        }

        return try {
            execution.execute(request, body)
        } finally {
            request.headers.remove(HEADER_FIELD_TRACE_ID)
            request.headers.remove(HEADER_FIELD_TYPE_ID)
        }
    }

    // HandlerInterceptor
    override fun preHandle(
            request: HttpServletRequest, response: HttpServletResponse,
            handler: Any
    ): Boolean {
        val traceId = extractTraceId(request)
        val requestType = extractRequestType(request)
        val closeable = set(traceId, requestType)
        request.setAttribute(CLASS_NAME, closeable)
        return true
    }

    // HandlerInterceptor
//    override fun postHandle(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        handler: Any,
//        modelAndView: ModelAndView?
//    ) {
//        val obj = request.getAttribute(CLASS_NAME)
//        if (obj != null && obj is InterceptorCloseables) {
//            obj.close()
//        }
//    }
}