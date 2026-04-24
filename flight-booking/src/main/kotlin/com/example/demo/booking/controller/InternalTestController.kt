package com.example.demo.booking.controller

import com.example.demo.booking.listener.PaymentEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/kafka")
class InternalTestController(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(InternalTestController::class.java)

    @PostMapping("/payment-event")
    fun simulatePaymentEvent(@RequestBody event: PaymentEvent): Map<String, String> {
        logger.info("Simulating Kafka Payment Event: $event")
        
        val message = objectMapper.writeValueAsString(event)
        kafkaTemplate.send("pay-event", message)
        
        return mapOf(
            "status" to "SENT",
            "topic" to "pay-event",
            "payload" to message
        )
    }
}
