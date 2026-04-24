package com.example.demo.booking.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {

    companion object {
        const val MAIN_QUEUE = "booking-pay-poll"
        const val RETRY_QUEUE = "booking-pay-poll.retry"
    }

    @Bean
    fun jsonMessageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitAdmin(connectionFactory: org.springframework.amqp.rabbit.connection.ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory)
    }

    /**
     * Main Queue: This is where the listener processes messages.
     */
    @Bean
    fun mainQueue(): Queue {
        return QueueBuilder.durable(MAIN_QUEUE).build()
    }

    /**
     * Retry Queue: Acts as a temporary buffer for delayed retries.
     * x-dead-letter-exchange: "" (routes to the default exchange)
     * x-dead-letter-routing-key: MAIN_QUEUE (sends it back to the main queue after TTL expires)
     */
    @Bean
    fun retryQueue(): Queue {
        return QueueBuilder.durable(RETRY_QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", MAIN_QUEUE)
            .build()
    }
}
