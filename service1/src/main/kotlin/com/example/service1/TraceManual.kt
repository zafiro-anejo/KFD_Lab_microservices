package com.example.service1

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

@Suppress("unused")
@Component
class TraceManual {

    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var tracer: Tracer
    private lateinit var spanProcessor: BatchSpanProcessor

    @PostConstruct
    fun init() {
        logger.info("=== TraceManual initializing ===")
        try {
            val spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint("http://tempo:4318/v1/traces")
                .setTimeout(Duration.ofSeconds(10))
                .build()

            spanProcessor = BatchSpanProcessor.builder(spanExporter).build()

            val resource = Resource.create(
                Attributes.builder()
                    .put("service.name", "service1-manual")
                    .build()
            )

            val tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .setResource(resource)
                .build()

            val openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build()

            tracer = openTelemetry.getTracer("manual-tracer")
            logger.info("=== TraceManual initialized successfully ===")
        } catch (e: Exception) {
            logger.error("Failed to initialize TraceManual", e)
        }
    }

    fun createSpan(operation: String, body: String): Span {
        logger.info("=== Creating span: $operation ===")
        val span = tracer.spanBuilder(operation).startSpan()
        span.setAttribute("http.body", body)
        return span
    }

    fun createChildSpan(parent: Span, operation: String, body: String): Span {
        logger.info("=== Creating child span: $operation under parent ${parent.spanContext.traceId} ===")
        val span = tracer.spanBuilder(operation)
            .setParent(Context.current().with(parent))
            .startSpan()
        span.setAttribute("http.body", body)
        return span
    }
}