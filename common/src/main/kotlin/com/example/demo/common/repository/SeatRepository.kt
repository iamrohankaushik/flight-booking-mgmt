package com.example.demo.common.repository

import com.example.demo.common.domain.SeatStatus
import com.example.demo.jooq.tables.records.SeatsRecord
import java.util.UUID

interface SeatRepository {
    fun findByIds(seatIds: List<UUID>): List<SeatsRecord>
    fun findByScheduleId(scheduleId: UUID): List<SeatsRecord>
    fun updateStatus(seatIds: List<UUID>, status: SeatStatus, expectedStatus: SeatStatus? = null): Int
    fun holdSeatsIfAvailable(seatIds: List<UUID>): List<SeatsRecord>
}
