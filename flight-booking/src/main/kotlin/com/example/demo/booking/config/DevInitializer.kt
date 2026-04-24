package com.example.demo.booking.config

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@Configuration
@Profile("local")
class DevInitializer(
    private val rabbitAdmin: RabbitAdmin
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DevInitializer::class.java)
    private val restTemplate = RestTemplate()

    override fun run(vararg args: String?) {
        logger.info("Initializing Development Environment...")

        // 1. Reset WireMock Scenarios
        try {
            restTemplate.postForEntity("http://localhost:8081/__admin/scenarios/reset", null, String::class.java)
            logger.info("✅ WireMock scenarios reset successfully.")
        } catch (e: Exception) {
            logger.warn("⚠️ Could not reset WireMock (is it running?): ${e.message}")
        }

        // 2. Purge RabbitMQ Queues
        try {
            rabbitAdmin.purgeQueue(RabbitConfig.MAIN_QUEUE)
            rabbitAdmin.purgeQueue(RabbitConfig.RETRY_QUEUE)
            logger.info("✅ RabbitMQ queues purged successfully.")
        } catch (e: Exception) {
            logger.warn("⚠️ Could not purge RabbitMQ queues: ${e.message}")
        }

        logger.info("Development Environment Ready!")
    }
}
