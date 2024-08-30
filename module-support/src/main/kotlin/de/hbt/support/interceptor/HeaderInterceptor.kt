package de.hbt.support.interceptor

import org.slf4j.MDC
import java.io.Closeable
import java.util.*

abstract class HeaderInterceptor {
    companion object {
        const val LOGGER_TRACE_ID = "trace.id"
        const val LOGGER_REQTYPE_ID = "REQTYPE"

        const val HEADER_FIELD_TRACE_ID = "X-TraceId"
        const val HEADER_FIELD_B3_TRACE_ID = "x-b3-traceid"
        const val HEADER_FIELD_TYPE_ID = "x-type"

        const val REQ_TYPE_HEALTHCHECK = "HEALTH_CHECK"
        const val REQ_TYPE_INTEGRATION_TEST = "INTEGRATION_TEST"
        const val REQ_TYPE_SERVER_TEST = "SERVER_TEST"
        const val REQ_TYPE_WARMUP = "WARMUP"

        fun createNewTraceId(): String {
            return UUID.randomUUID().toString()
        }

        fun getTraceId(): String {
            val traceId = MDC.get(LOGGER_TRACE_ID)
            return if (traceId.isNullOrBlank()) createNewTraceId() else traceId
        }

        fun getRequestType(): String? {
            val type = MDC.get(LOGGER_REQTYPE_ID)
            return if (type.isNullOrBlank()) null else type
        }

        private fun setTraceId(traceId: String): InterceptorCloseables {
            return InterceptorCloseables(MDC.putCloseable(LOGGER_TRACE_ID, traceId))
        }

        private fun mark(requestType: String?): InterceptorCloseables {
            return InterceptorCloseables(MDC.putCloseable(LOGGER_REQTYPE_ID, requestType))
        }

        fun set(traceId: String, requestType: String?): InterceptorCloseables {
            return if (requestType != null) {
                InterceptorCloseables(setTraceId(traceId), mark(requestType))
            } else setTraceId(traceId)
        }

    }

    fun markAsHealthCheck(): InterceptorCloseables {
        return InterceptorCloseables(mark(REQ_TYPE_HEALTHCHECK), setTraceId(createNewTraceId()))
    }

    fun markAsIntegrationTest(): InterceptorCloseables {
        return InterceptorCloseables(mark(REQ_TYPE_INTEGRATION_TEST), setTraceId(createNewTraceId()))
    }

    fun markAsServerTest(): InterceptorCloseables {
        return InterceptorCloseables(mark(REQ_TYPE_SERVER_TEST), setTraceId(createNewTraceId()))
    }

    fun markAsWarmup(): InterceptorCloseables {
        return InterceptorCloseables(mark(REQ_TYPE_WARMUP), setTraceId(createNewTraceId()))
    }

    class InterceptorCloseables(vararg closeables: Closeable) : Closeable {
        private val closeables: Array<out Closeable>

        init {
            this.closeables = closeables
        }

        override fun close() {
            closeables.forEach {
                try {
                    it.close()
                } catch (ignored: Exception) {
                    // do nothing
                }
            }
        }
    }

}