package com.example.demo.common.repository.impl

import com.example.demo.common.domain.PaymentStatus
import com.example.demo.common.repository.PaymentRepository
import com.example.demo.jooq.tables.records.PaymentsRecord
import com.example.demo.jooq.tables.references.PAYMENTS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JooqPaymentRepository(private val dsl: DSLContext) : PaymentRepository {
    override fun create(payId: String, amount: Double, bookingId: UUID): UUID {
        val id = UUID.randomUUID()
        dsl.insertInto(PAYMENTS)
            .set(PAYMENTS.ID, id)
            .set(PAYMENTS.PAY_ID, payId)
            .set(PAYMENTS.AMOUNT, amount.toBigDecimal())
            .set(PAYMENTS.BOOKING_ID, bookingId)
            .set(PAYMENTS.STATUS, PaymentStatus.INITIATED.name)
            .execute()
        return id
    }

    override fun updateStatus(payId: String, status: PaymentStatus, refNumber: String?): Int {
        val query = dsl.update(PAYMENTS)
            .set(PAYMENTS.STATUS, status.name)
        
        if (refNumber != null) {
            query.set(PAYMENTS.REF_NUMBER, refNumber)
        }
        
        return query.where(PAYMENTS.PAY_ID.eq(payId)).execute()
    }

    override fun findByPayId(payId: String): PaymentsRecord? {
        return dsl.selectFrom(PAYMENTS)
            .where(PAYMENTS.PAY_ID.eq(payId))
            .fetchOne()
    }
}
