package com.example.demo.booking.client

import com.example.demo.common.dto.PaymentRequest
import com.example.demo.common.dto.PaymentResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*
import java.util.UUID

@FeignClient(name = "payment-client", url = "\${payment.api.url}")
interface PaymentClient {

    @PostMapping("/v1/payments")
    fun createPaymentIntent(@RequestBody request: PaymentRequest): PaymentResponse

    @GetMapping("/v1/payments/status/{bookingId}")
    fun getPaymentStatus(@PathVariable("bookingId") bookingId: UUID): PaymentResponse
}
