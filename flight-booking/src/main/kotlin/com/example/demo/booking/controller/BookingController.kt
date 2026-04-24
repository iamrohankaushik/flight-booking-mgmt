package com.example.demo.booking.controller

import com.example.demo.booking.service.BookingService
import com.example.demo.common.dto.BookingRequest
import com.example.demo.common.dto.BookingResponse
import com.example.demo.common.dto.SeatDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1")
class BookingController(private val bookingService: BookingService) {

    @PostMapping("/booking")
    suspend fun createBooking(@RequestBody request: BookingRequest): ResponseEntity<BookingResponse> {
        val response = bookingService.createBooking(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/seat-map")
    suspend fun getSeatMap(@RequestParam scheduleId: UUID): ResponseEntity<List<SeatDto>> {
        val response = bookingService.getSeatMap(scheduleId)
        return ResponseEntity.ok(response)
    }
}
