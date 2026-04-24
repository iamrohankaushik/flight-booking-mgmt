package com.example.demo.booking.listener

import com.example.demo.booking.client.PaymentClient
import com.example.demo.booking.config.RabbitConfig
import com.example.demo.common.domain.BookingStatus
import com.example.demo.common.domain.PaymentStatus
import com.example.demo.common.domain.SeatStatus
import com.example.demo.common.repository.BookingRepository
import com.example.demo.common.repository.PaymentRepository
import com.example.demo.common.repository.SeatRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Component
class PaymentPollListener(
    private val bookingRepository: BookingRepository,
    private val seatRepository: SeatRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentClient: PaymentClient,
    private val rabbitTemplate: RabbitTemplate,
    transactionManager: PlatformTransactionManager
) {
    private val logger = LoggerFactory.getLogger(PaymentPollListener::class.java)
    private val maxRetries = 3
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @RabbitListener(queues = [RabbitConfig.MAIN_QUEUE])
    fun pollPaymentStatus(request: PollRequest) {
        val bookingId = request.bookingId
        val retryCount = request.retryCount

        val booking = bookingRepository.findById(bookingId)
        if (booking == null || booking.status != BookingStatus.PROCESSING.name) {
            logger.info("Booking $bookingId is already ${booking?.status ?: "UNKNOWN"}. Stopping poll.")
            return
        }

        logger.info("Polling payment status for booking $bookingId (Attempt $retryCount)")

        try {
            val response = paymentClient.getPaymentStatus(bookingId)

            when (response.status) {
                PaymentStatus.SUCCESS -> handleSuccess(bookingId, response.payId!!)
                PaymentStatus.FAILED -> handleFailure(bookingId)
                else -> handleRetryOrFailure(bookingId, retryCount, "Non-terminal status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.error("Poll error for booking $bookingId: ${e.message}")
            handleRetryOrFailure(bookingId, retryCount, "Exception: ${e.message}")
        }
    }

    private fun handleRetryOrFailure(bookingId: UUID, currentRetryCount: Int, reason: String) {
        if (currentRetryCount < maxRetries) {
            retry(bookingId, currentRetryCount + 1)
        } else {
            logger.error("Max retries reached for $bookingId. Finalizing as FAILED. Reason: $reason")
            handleFailure(bookingId)
        }
    }

    protected fun handleSuccess(bookingId: UUID, payId: String) {
        transactionTemplate.execute {
            val updated = bookingRepository.updateStatus(bookingId, BookingStatus.CONFIRMED, expectedStatus = BookingStatus.PROCESSING)
            
            if (updated) {
                logger.info("handleSuccess: Successfully transitioned $bookingId to CONFIRMED")
                paymentRepository.updateStatus(payId, PaymentStatus.SUCCESS)

                val seatIds = bookingRepository.findSeatIdsByBookingId(bookingId)
                seatRepository.updateStatus(seatIds, SeatStatus.BOOKED, SeatStatus.HOLD)
            } else {
                logger.info("handleSuccess: Booking $bookingId already processed. Skipping.")
            }
        }
    }

    protected fun handleFailure(bookingId: UUID) {
        transactionTemplate.execute {
            val updated = bookingRepository.updateStatus(bookingId, BookingStatus.FAILED, expectedStatus = BookingStatus.PROCESSING)
            
            if (updated) {
                logger.error("handleFailure: Successfully transitioned $bookingId to FAILED")
                val seatIds = bookingRepository.findSeatIdsByBookingId(bookingId)
                seatRepository.updateStatus(seatIds, SeatStatus.AVAILABLE)
            } else {
                logger.warn("handleFailure: Could not fail booking $bookingId (possibly already CONFIRMED).")
            }
        }
    }

    private fun retry(bookingId: UUID, nextRetry: Int) {
        val delayMs = when (nextRetry) {
            1 -> 10_000L
            2 -> 30_000L
            else -> 60_000L
        }

        logger.warn("Retrying poll for $bookingId, attempt $nextRetry. Delaying ${delayMs / 1000}s via RabbitMQ")
        
        rabbitTemplate.convertAndSend(RabbitConfig.RETRY_QUEUE, PollRequest(bookingId, nextRetry)) { message ->
            message.messageProperties.expiration = delayMs.toString()
            message
        }
    }
}
