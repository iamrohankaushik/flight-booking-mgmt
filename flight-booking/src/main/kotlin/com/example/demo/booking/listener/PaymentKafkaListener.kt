package com.example.demo.booking.listener

import com.example.demo.common.domain.BookingStatus
import com.example.demo.common.domain.PaymentStatus
import com.example.demo.common.domain.SeatStatus
import com.example.demo.common.repository.BookingRepository
import com.example.demo.common.repository.PaymentRepository
import com.example.demo.common.repository.SeatRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentKafkaListener(
    private val bookingRepository: BookingRepository,
    private val seatRepository: SeatRepository,
    private val paymentRepository: PaymentRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(PaymentKafkaListener::class.java)

    @KafkaListener(topics = ["pay-event"])
    fun handlePaymentEvent(message: String) {
        val event = try {
            objectMapper.readValue(message, PaymentEvent::class.java)
        } catch (e: Exception) {
            logger.error("Failed to parse PaymentEvent: $message", e)
            return
        }
        
        val payment = paymentRepository.findByPayId(event.payId) ?: return
        val bookingId = payment.bookingId!!

        when (event.status) {
            PaymentStatus.SUCCESS -> {
                val updated = bookingRepository.updateStatus(bookingId, BookingStatus.CONFIRMED, expectedStatus = BookingStatus.PROCESSING)
                
                if (updated) {
                    logger.info("handlePaymentEvent: Successfully transitioned $bookingId to CONFIRMED (Kafka)")
                    paymentRepository.updateStatus(event.payId, PaymentStatus.SUCCESS, event.refNumber)

                    val seatIds = bookingRepository.findSeatIdsByBookingId(bookingId)
                    seatRepository.updateStatus(seatIds, SeatStatus.BOOKED, SeatStatus.HOLD)
                } else {
                    logger.info("handlePaymentEvent: Booking $bookingId already CONFIRMED or not in PROCESSING. Skipping.")
                }
            }
            PaymentStatus.FAILED -> {
                val updated = bookingRepository.updateStatus(bookingId, BookingStatus.FAILED, expectedStatus = BookingStatus.PROCESSING)
                
                if (updated) {
                    logger.error("handlePaymentEvent: Transitioned $bookingId to FAILED (Kafka)")
                    paymentRepository.updateStatus(event.payId, PaymentStatus.FAILED)

                    val seatIds = bookingRepository.findSeatIdsByBookingId(bookingId)
                    seatRepository.updateStatus(seatIds, SeatStatus.AVAILABLE)
                }
            }
            else -> {
                // Do nothing
                logger.info("handlePaymentEvent: Payment status is ${event.status} for booking $bookingId. Ignoring")
            }
        }
    }
}
