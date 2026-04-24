package com.example.demo.booking.service.impl

import com.example.demo.booking.config.RabbitConfig
import com.example.demo.booking.listener.PollRequest
import com.example.demo.booking.service.BookingService
import com.example.demo.booking.service.PaymentService
import com.example.demo.common.domain.BookingStatus
import com.example.demo.common.domain.PaymentStatus
import com.example.demo.common.domain.SeatStatus
import com.example.demo.common.dto.BookingRequest
import com.example.demo.common.dto.BookingResponse
import com.example.demo.common.dto.SeatDto
import com.example.demo.common.exception.SeatUnavailableException
import com.example.demo.common.repository.BookingRepository
import com.example.demo.common.repository.PaymentRepository
import com.example.demo.common.repository.SeatRepository
import com.example.demo.common.service.RedisLockService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.util.*

@Service
class BookingServiceImpl(
    private val bookingRepository: BookingRepository,
    private val seatRepository: SeatRepository,
    private val paymentRepository: PaymentRepository,
    private val redisLockService: RedisLockService,
    private val paymentService: PaymentService,
    private val rabbitTemplate: RabbitTemplate,
    transactionManager: PlatformTransactionManager
) : BookingService {
    private val logger = LoggerFactory.getLogger(BookingServiceImpl::class.java)
    private val transactionTemplate = TransactionTemplate(transactionManager)

    override suspend fun createBooking(request: BookingRequest): BookingResponse {
        logger.info("Booking request: $request")
        val scheduleId = request.scheduleId
        val seatIds = request.seatIds
        val lockKeys = seatIds.map { "booking:$scheduleId:$it" }
        var acquiredLocks = emptyList<String>()
        
        try {
            acquiredLocks = redisLockService.acquireLocks(lockKeys, Duration.ofMinutes(1))
            logger.info("Acquired locks: $acquiredLocks")

            val (bookingId, totalAmount) = transactionTemplate.execute {
                val heldSeats = seatRepository.holdSeatsIfAvailable(seatIds)
                if (heldSeats.size != seatIds.size) {
                    throw SeatUnavailableException("Selected seats are not available")
                }

                val bId = bookingRepository.create(request.userId, scheduleId, seatIds)
                val amount = heldSeats.sumOf { it.basePrice!!.toDouble() }
                
                logger.info("Created booking with ID: $bId and amount: $amount")
                Pair(bId, amount)
            }!!

            val paymentResult = paymentService.createPaymentIntent(request.userId, bookingId, totalAmount)
            logger.info("Payment result: $paymentResult")

            return if (paymentResult.status == PaymentStatus.INITIATED) {
                handlePaymentInitiated(bookingId, paymentResult.payId, totalAmount)
            } else {
                handlePaymentFailure(bookingId, seatIds, paymentResult.errorMessage)
            }

        } catch (e: SeatUnavailableException) {
            logger.error("Booking failed due to seat unavailability: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during booking creation: ${e.message}", e)
            throw RuntimeException("Failed to create booking: ${e.message}")
        } finally {
            if (acquiredLocks.isNotEmpty()) {
                logger.info("Releasing locks: $acquiredLocks")
                redisLockService.releaseLocks(acquiredLocks)
            }
        }
    }

    private fun handlePaymentInitiated(bookingId: UUID, payId: String?, amount: Double): BookingResponse {
        try {
            transactionTemplate.execute {
                bookingRepository.updateStatus(bookingId, BookingStatus.PROCESSING)
                payId?.let { paymentRepository.create(it, amount, bookingId) }
            }
            logger.info("Payment initiated for booking with ID: $bookingId and payId: $payId")
            rabbitTemplate.convertAndSend(RabbitConfig.MAIN_QUEUE, PollRequest(bookingId))
            logger.info("Sent poll request for booking with ID: $bookingId")
            return BookingResponse(bookingId = bookingId, payId = payId, status = BookingStatus.PROCESSING.name)
        } catch (e: Exception) {
            logger.error("Unexpected error during payment success: ${e.message}", e)
            throw RuntimeException("Failed to handle payment success: ${e.message}")
        }
    }

    private fun handlePaymentFailure(bookingId: UUID, seatIds: List<UUID>, error: String?): BookingResponse {
        try {
            transactionTemplate.execute {
                bookingRepository.updateStatus(bookingId, BookingStatus.FAILED)
                seatRepository.updateStatus(seatIds, SeatStatus.AVAILABLE)
            }
            logger.info("Payment failed for booking with ID: $bookingId and error: $error")
            return BookingResponse(
                bookingId = bookingId,
                status = BookingStatus.FAILED.name,
                message = "Payment failed: $error. Seats have been released."
            )
        } catch (e: Exception) {
            logger.error("Unexpected error during payment failure: ${e.message}", e)
            throw RuntimeException("Failed to handle payment failure: ${e.message}")
        }
    }

    override suspend fun getSeatMap(scheduleId: UUID): List<SeatDto> {
        try {
            logger.info("Getting seat map for schedule ID: $scheduleId")
            val seats = seatRepository.findByScheduleId(scheduleId).map {
                SeatDto(
                    id = it.id!!,
                    status = it.status!!,
                    price = it.basePrice!!.toDouble()
                )
            }
            logger.info("Seat map for schedule ID: $scheduleId: $seats")
            return seats
        } catch (e: Exception) {
            logger.error("Unexpected error during seat map retrieval: ${e.message}", e)
            throw RuntimeException("Failed to retrieve seat map: ${e.message}")
        }
    }
}
