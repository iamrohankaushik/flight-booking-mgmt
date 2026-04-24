package com.example.demo.common.domain

enum class SeatStatus {
    AVAILABLE, HOLD, BOOKED
}

enum class BookingStatus {
    INITIATED, PROCESSING, CONFIRMED, FAILED
}

enum class PaymentStatus {
    INITIATED, SUCCESS, FAILED
}
