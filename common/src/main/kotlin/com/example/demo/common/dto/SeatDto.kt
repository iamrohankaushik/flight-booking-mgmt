package com.example.demo.common.dto

import java.util.UUID

data class SeatDto(
    val id: UUID,
    val status: String,
    val price: Double
)
