package com.example.demo.searching.repository

import com.example.demo.searching.dto.FlightScheduleDto
import java.time.LocalDate

interface SearchRepository {
    fun searchSchedules(source: String, dest: String, date: LocalDate): List<FlightScheduleDto>
}
