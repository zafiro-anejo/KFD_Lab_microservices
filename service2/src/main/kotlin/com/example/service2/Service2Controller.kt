package com.example.service2

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Suppress("unused")
@RestController
@RequestMapping("/api")
class Service2Controller {

    @GetMapping("/service2")
    fun getData(): Map<String, Any> {
        return mapOf(
            "service" to "service2",
            "timestamp" to Instant.now().toString(),
            "data" to listOf("Hello", "from", "service2")
        )
    }
}