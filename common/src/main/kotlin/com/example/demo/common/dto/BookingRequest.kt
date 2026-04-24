package com.example.demo.common.dto

import java.util.UUID

data class BookingRequest(
    val scheduleId: UUID,
    val userId: UUID,
    val seatIds: List<UUID>
)