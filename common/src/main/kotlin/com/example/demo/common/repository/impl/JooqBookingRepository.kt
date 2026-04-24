package com.example.demo.common.repository.impl

import com.example.demo.common.domain.BookingStatus
import com.example.demo.common.repository.BookingRepository
import com.example.demo.jooq.tables.records.BookingsRecord
import com.example.demo.jooq.tables.references.BOOKINGS
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSON
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JooqBookingRepository(
    private val dsl: DSLContext,
    private val objectMapper: ObjectMapper
) : BookingRepository {
    override fun create(userId: UUID, scheduleId: UUID, seatIds: List<UUID>): UUID {
        val bookingId = UUID.randomUUID()
        dsl.insertInto(BOOKINGS)
            .set(BOOKINGS.ID, bookingId)
            .set(BOOKINGS.USER_ID, userId)
            .set(BOOKINGS.SCHEDULE_ID, scheduleId)
            .set(
                BOOKINGS.SEAT_IDS,
                JSON.valueOf(objectMapper.writeValueAsString(seatIds))
            ).set(BOOKINGS.STATUS, BookingStatus.INITIATED.name)
            .execute()

        return bookingId
    }

    override fun updateStatus(bookingId: UUID, status: BookingStatus, expectedStatus: BookingStatus?): Boolean {
        val query = dsl.update(BOOKINGS)
            .set(BOOKINGS.STATUS, status.name)
            .where(BOOKINGS.ID.eq(bookingId))

        if (expectedStatus != null) {
            query.and(BOOKINGS.STATUS.eq(expectedStatus.name))
        }

        val rowsAffected = query.execute()
        return rowsAffected > 0
    }

    override fun findById(bookingId: UUID): BookingsRecord? {
        return dsl.selectFrom(BOOKINGS)
            .where(BOOKINGS.ID.eq(bookingId))
            .fetchOne()
    }

    override fun findSeatIdsByBookingId(bookingId: UUID): List<UUID> {
        val record = dsl.select(BOOKINGS.SEAT_IDS)
            .from(BOOKINGS)
            .where(BOOKINGS.ID.eq(bookingId))
            .fetchOne() ?: return emptyList()
        
        return objectMapper.readValue(record.value1()!!.data())
    }
}
