package com.example.demo.common.repository

import com.example.demo.common.domain.BookingStatus
import com.example.demo.jooq.tables.records.BookingsRecord
import java.util.UUID

interface BookingRepository {
    fun create(userId: UUID, scheduleId: UUID, seatIds: List<UUID>): UUID
    fun updateStatus(bookingId: UUID, status: BookingStatus, expectedStatus: BookingStatus? = null): Boolean
    fun findById(bookingId: UUID): BookingsRecord?
    fun findSeatIdsByBookingId(bookingId: UUID): List<UUID>
}
