package com.example.demo.booking.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MessagingConfig {

    @Bean
    fun payEventTopic(): NewTopic {
        return NewTopic("pay-event", 1, 1)
    }
}
