package com.example.demo.searching.repository.impl

import com.example.demo.jooq.tables.references.FLIGHTS
import com.example.demo.jooq.tables.references.FLIGHT_SCHEDULES
import com.example.demo.searching.dto.FlightScheduleDto
import com.example.demo.searching.repository.SearchRepository
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZoneOffset

@Repository
class JooqSearchRepository(
    private val dsl: DSLContext
) : SearchRepository {

    override fun searchSchedules(source: String, dest: String, date: LocalDate): List<FlightScheduleDto> {
        val startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)

        return dsl.select(
            FLIGHT_SCHEDULES.ID.`as`("scheduleId"),
            FLIGHT_SCHEDULES.FLIGHT_ID.`as`("flightId"),
            FLIGHTS.FLIGHT_NUMBER.`as`("flightNumber"),
            FLIGHT_SCHEDULES.SOURCE,
            FLIGHT_SCHEDULES.DEST,
            FLIGHT_SCHEDULES.START_TIME.`as`("startTime"),
            FLIGHT_SCHEDULES.END_TIME.`as`("endTime")
        )
            .from(FLIGHT_SCHEDULES)
            .join(FLIGHTS).on(FLIGHT_SCHEDULES.FLIGHT_ID.eq(FLIGHTS.ID))
            .where(FLIGHT_SCHEDULES.SOURCE.eq(source))
            .and(FLIGHT_SCHEDULES.DEST.eq(dest))
            .and(FLIGHT_SCHEDULES.START_TIME.ge(startOfDay))
            .and(FLIGHT_SCHEDULES.START_TIME.lt(endOfDay))
            .fetchInto(FlightScheduleDto::class.java)
    }
}
