package com.example.demo.searching.dto

import java.time.LocalDateTime
import java.util.UUID

data class FlightSearchRequest(
    val source: String,
    val dest: String,
    val date: String // Expected format: YYYY-MM-DD
)

data class FlightScheduleDto(
    val scheduleId: UUID,
    val flightId: UUID,
    val flightNumber: String,
    val source: String,
    val dest: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
