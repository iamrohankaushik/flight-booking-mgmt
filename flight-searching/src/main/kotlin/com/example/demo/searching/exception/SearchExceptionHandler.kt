package com.example.demo.searching.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice(basePackages = ["com.example.demo.searching.controller"])
class SearchExceptionHandler {
    private val logger = LoggerFactory.getLogger(SearchExceptionHandler::class.java)

    @ExceptionHandler(InvalidSearchRequestException::class)
    fun handleInvalidRequest(e: InvalidSearchRequestException): ResponseEntity<Map<String, String>> {
        logger.warn("Invalid search request: ${e.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to (e.message ?: "Invalid request")))
    }

    @ExceptionHandler(SearchServiceException::class)
    fun handleServiceException(e: SearchServiceException): ResponseEntity<Map<String, String>> {
        logger.error("Search service error: ${e.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "Internal server error")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, String>> {
        logger.error("Unexpected error in search service: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "An unexpected error occurred."))
    }
}
