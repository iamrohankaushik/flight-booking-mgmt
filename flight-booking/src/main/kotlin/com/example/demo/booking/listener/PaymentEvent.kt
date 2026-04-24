package com.example.demo.booking.listener

import com.example.demo.common.domain.PaymentStatus

data class PaymentEvent(
    val payId: String,
    val status: PaymentStatus,
    val refNumber: String? = null
)
