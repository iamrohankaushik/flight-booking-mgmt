package com.example.demo.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication(scanBasePackages = ["com.example.demo"])
@EnableFeignClients
class BookingApplication

fun main(args: Array<String>) {
    runApplication<BookingApplication>(*args)
}
