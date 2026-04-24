package com.example.demo.common.advice

import com.example.demo.common.dto.ErrorResponse
import com.example.demo.common.exception.BaseException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = ex.status.value(),
            error = ex.status.reasonPhrase,
            message = ex.messageStr
        )
        return ResponseEntity(error, ex.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "An unexpected error occurred"
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
