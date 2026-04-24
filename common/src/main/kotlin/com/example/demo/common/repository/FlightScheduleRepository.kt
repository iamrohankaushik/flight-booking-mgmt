package com.example.demo.common.repository

import com.example.demo.common.dto.FlightScheduleDto
import java.time.OffsetDateTime

interface FlightScheduleRepository {
    fun search(src: String, dest: String, start: OffsetDateTime, end: OffsetDateTime): List<FlightScheduleDto>
}
