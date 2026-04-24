package com.example.demo.common.dto

import java.util.UUID

data class PaymentRequest(
    val userId: UUID,
    val bookingId: UUID,
    val amount: Double
)
