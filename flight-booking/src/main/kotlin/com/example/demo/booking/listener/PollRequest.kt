package com.example.demo.booking.listener

import java.util.UUID

data class PollRequest(
    val bookingId: UUID,
    val retryCount: Int = 0
)
