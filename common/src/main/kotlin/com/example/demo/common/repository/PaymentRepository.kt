package com.example.demo.common.repository

import com.example.demo.common.domain.PaymentStatus
import com.example.demo.jooq.tables.records.PaymentsRecord
import java.util.UUID

interface PaymentRepository {
    fun create(payId: String, amount: Double, bookingId: UUID): UUID
    fun updateStatus(payId: String, status: PaymentStatus, refNumber: String? = null): Int
    fun findByPayId(payId: String): PaymentsRecord?
}
