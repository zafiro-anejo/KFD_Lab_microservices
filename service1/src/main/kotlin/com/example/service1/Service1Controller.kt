package com.example.service1

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Suppress("unused")
@RestController
@RequestMapping("/api")
class Service1Controller(
    private val webClientBuilder: WebClient.Builder,
    private val traceManual: TraceManual
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/service1")
    fun getCombined(): Mono<Map<String, Any>> {
        val parentSpan = traceManual.createSpan("GET /api/service1", "Start request")
        logger.info("=== Parent span created ===")

        val childSpan = traceManual.createChildSpan(parentSpan, "Call service2", "http://service2/api/service2")
        childSpan.setAttribute("http.url", "http://service2/api/service2")

        return webClientBuilder.build()
            .get()
            .uri("http://service2/api/service2")
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .doFinally {
                childSpan.end()
                logger.info("=== Child span ended ===")
            }
            .map { service2Data ->
                mapOf<String, Any>(
                    "service1" to mapOf(
                        "message" to "Hello from service1",
                        "timestamp" to System.currentTimeMillis()
                    ),
                    "service2_data" to service2Data
                )
            }
            .doFinally {
                parentSpan.end()
                logger.info("=== Parent span ended ===")
                Thread.sleep(2000)
            }
    }
}