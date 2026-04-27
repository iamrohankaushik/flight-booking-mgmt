package com.example.demo.booking.exception

import com.example.demo.common.exception.SeatUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(SeatUnavailableException::class)
    fun handleSeatUnavailable(ex: SeatUnavailableException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(mapOf("message" to ex.message))
    }
}