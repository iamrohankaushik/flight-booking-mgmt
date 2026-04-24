package com.example.demo.booking.service

import com.example.demo.common.dto.BookingRequest
import com.example.demo.common.dto.BookingResponse
import com.example.demo.common.dto.SeatDto
import java.util.UUID

interface BookingService {
    suspend fun createBooking(request: BookingRequest): BookingResponse
    suspend fun getSeatMap(scheduleId: UUID): List<SeatDto>
}
