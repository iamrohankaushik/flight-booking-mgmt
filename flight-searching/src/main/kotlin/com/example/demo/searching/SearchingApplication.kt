package com.example.demo.searching

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example.demo"])
class SearchingApplication

fun main(args: Array<String>) {
    runApplication<SearchingApplication>(*args)
}
