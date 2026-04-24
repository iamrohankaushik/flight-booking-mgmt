package com.example.demo.common.dto

import java.util.UUID

data class BookingResponse(
    val bookingId: UUID,
    val payId: String? = null,
    val status: String,
    val message: String? = null
)
