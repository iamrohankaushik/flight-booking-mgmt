package com.example.demo.common.repository.impl

import com.example.demo.common.dto.FlightScheduleDto
import com.example.demo.common.repository.FlightScheduleRepository
import com.example.demo.jooq.tables.references.FLIGHT_SCHEDULES
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class JooqFlightScheduleRepository(private val dsl: DSLContext) : FlightScheduleRepository {
    override fun search(src: String, dest: String, start: OffsetDateTime, end: OffsetDateTime): List<FlightScheduleDto> {
        return dsl.selectFrom(FLIGHT_SCHEDULES)
            .where(FLIGHT_SCHEDULES.SOURCE.eq(src))
            .and(FLIGHT_SCHEDULES.DEST.eq(dest))
            .and(FLIGHT_SCHEDULES.START_TIME.between(start, end))
            .fetch { record ->
                FlightScheduleDto(
                    id = record.id!!,
                    flightId = record.flightId!!,
                    source = record.source!!,
                    dest = record.dest!!,
                    startTime = record.startTime!!,
                    endTime = record.endTime!!
                )
            }
    }
}
