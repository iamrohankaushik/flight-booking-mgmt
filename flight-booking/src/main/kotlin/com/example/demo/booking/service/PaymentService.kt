package com.example.demo.booking.service

import com.example.demo.common.dto.PaymentResponse
import java.util.*

interface PaymentService {
    suspend fun createPaymentIntent(userId: UUID, bookingId: UUID, amount: Double): PaymentResponse
}
