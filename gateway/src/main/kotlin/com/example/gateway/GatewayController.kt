package com.example.gateway

import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Suppress("unused")
@RestController
class GatewayController(
    private val webClientBuilder: WebClient.Builder
) {
    @GetMapping("/api/service1")
    fun forwardToService1(): Mono<String> {
        return webClientBuilder.build()
            .get()
            .uri("http://service1/api/service1")
            .retrieve()
            .bodyToMono(String::class.java)
    }
}

@Suppress("unused")
@Configuration
class AppConfig {
    @Bean
    @LoadBalanced
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}
