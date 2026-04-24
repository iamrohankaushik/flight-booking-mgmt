package com.example.demo.common.dto

import java.time.OffsetDateTime
import java.util.UUID

data class FlightScheduleDto(
    val id: UUID,
    val flightId: UUID,
    val source: String,
    val dest: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime
)
