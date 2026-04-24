package com.example.demo.booking.service.impl

import com.example.demo.booking.client.PaymentClient
import com.example.demo.booking.service.PaymentService
import com.example.demo.common.domain.PaymentStatus
import com.example.demo.common.dto.PaymentRequest
import com.example.demo.common.dto.PaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentServiceImpl(
    private val paymentClient: PaymentClient
) : PaymentService {
    private val logger = LoggerFactory.getLogger(PaymentServiceImpl::class.java)

    override suspend fun createPaymentIntent(userId: UUID, bookingId: UUID, amount: Double): PaymentResponse {
        return try {
            logger.info("Creating payment intent for booking $bookingId")
            paymentClient.createPaymentIntent(PaymentRequest(userId, bookingId, amount))
        } catch (e: Exception) {
            logger.error("Initial payment intent creation failed for booking $bookingId: ${e.message}. Retrying...")
            retryCreatePaymentIntent(userId, bookingId, amount, e)
        }
    }

    private suspend fun retryCreatePaymentIntent(userId: UUID, bookingId: UUID, amount: Double, lastError: Exception): PaymentResponse {
        var error = lastError
        for (attempt in 1..2) {
            try {
                logger.info("Retrying payment intent creation for booking $bookingId (Attempt $attempt)")
                return paymentClient.createPaymentIntent(PaymentRequest(userId, bookingId, amount))
            } catch (e: Exception) {
                logger.error("Retry attempt $attempt failed for $bookingId: ${e.message}")
                error = e
                if (attempt < 2) kotlinx.coroutines.delay(500)
            }
        }
        
        // If all retries fail, return a FAILED response
        return PaymentResponse(
            status = PaymentStatus.FAILED, 
            errorMessage = "Failed after retries: ${error.message}"
        )
    }
}
