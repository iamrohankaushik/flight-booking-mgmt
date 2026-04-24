package com.example.demo.common.dto

import com.example.demo.common.domain.PaymentStatus

data class PaymentResponse(
    val payId: String? = null,
    val status: PaymentStatus,
    val errorMessage: String? = null
)
