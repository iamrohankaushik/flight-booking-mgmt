package com.example.demo.common.exception

import org.springframework.http.HttpStatus

class SeatUnavailableException(message: String) : BaseException(message, HttpStatus.CONFLICT)
class BookingNotFoundException(message: String) : BaseException(message, HttpStatus.NOT_FOUND)
class PaymentGatewayException(message: String) : BaseException(message, HttpStatus.SERVICE_UNAVAILABLE)
class ValidationException(message: String) : BaseException(message, HttpStatus.BAD_REQUEST)
