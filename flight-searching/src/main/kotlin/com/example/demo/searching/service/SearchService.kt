package com.example.demo.searching.service

import com.example.demo.searching.dto.FlightScheduleDto
import com.example.demo.searching.dto.FlightSearchRequest

interface SearchService {
    fun searchFlights(request: FlightSearchRequest): List<FlightScheduleDto>
}
