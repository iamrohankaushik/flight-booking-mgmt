package com.example.demo.common.repository.impl

import com.example.demo.common.domain.SeatStatus
import com.example.demo.common.repository.SeatRepository
import com.example.demo.jooq.tables.records.SeatsRecord
import com.example.demo.jooq.tables.references.SEATS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JooqSeatRepository(private val dsl: DSLContext) : SeatRepository {
    override fun findByIds(seatIds: List<UUID>): List<SeatsRecord> {
        return dsl.selectFrom(SEATS)
            .where(SEATS.ID.`in`(seatIds))
            .fetch()
    }

    override fun findByScheduleId(scheduleId: UUID): List<SeatsRecord> {
        return dsl.selectFrom(SEATS)
            .where(SEATS.SCHEDULE_ID.eq(scheduleId))
            .fetch()
    }

    override fun updateStatus(seatIds: List<UUID>, status: SeatStatus, expectedStatus: SeatStatus?): Int {
        var query = dsl.update(SEATS)
            .set(SEATS.STATUS, status.name)
            .where(SEATS.ID.`in`(seatIds))
        
        if (expectedStatus != null) {
            query = query.and(SEATS.STATUS.eq(expectedStatus.name))
        }
        
        return query.execute()
    }

    override fun holdSeatsIfAvailable(seatIds: List<UUID>): List<SeatsRecord> {
        return dsl.update(SEATS)
            .set(SEATS.STATUS, SeatStatus.HOLD.name)
            .where(SEATS.ID.`in`(*seatIds.toTypedArray()))
            .and(SEATS.STATUS.eq(SeatStatus.AVAILABLE.name))
            .returning()
            .fetch()
    }
}
