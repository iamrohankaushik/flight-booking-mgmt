package com.example.demo.booking

import com.example.demo.booking.client.PaymentClient
import com.example.demo.booking.listener.PaymentKafkaListener
import com.example.demo.booking.listener.PaymentPollListener
import com.example.demo.booking.listener.PollRequest
import com.example.demo.common.domain.BookingStatus
import com.example.demo.common.domain.PaymentStatus
import com.example.demo.common.domain.SeatStatus
import com.example.demo.common.dto.BookingResponse
import com.example.demo.common.dto.PaymentResponse
import com.example.demo.common.dto.SeatDto
import com.example.demo.common.repository.BookingRepository
import com.example.demo.common.repository.SeatRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class BookingIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var bookingRepository: BookingRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var paymentPollListener: PaymentPollListener

    @MockBean
    private lateinit var paymentClient: PaymentClient

    @Autowired
    lateinit var paymentKafkaListener: PaymentKafkaListener

    // Seed data from V2__Seed-Data.sql
    val userId = "d290f1ee-6c54-4b01-90e6-d701748f0851"
    val scheduleId = "f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f"

    @AfterEach
    fun tearDown() {
        // Clean up only the Redis keys created by booking logic (pattern: booking:<scheduleId>:<seatId>)
        val keys = redisTemplate.keys("booking:*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    /**
     * Helper: Fetches available seats from the /v1/seat-map API dynamically.
     * This ensures we always use real seat IDs from the Flyway seed data.
     */
    private fun getAvailableSeats(): List<SeatDto> {
        val mvcResult = mockMvc.perform(
            get("/v1/seat-map")
                .param("scheduleId", scheduleId.toString())
        ).andExpect(request().asyncStarted())
            .andReturn()

        val asyncResult = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()

        val body = asyncResult.response.contentAsString
        return objectMapper.readValue(body, object : TypeReference<List<SeatDto>>() {})
    }

    /**
     * Helper: Creates a booking request JSON from dynamic seat IDs.
     */
    private fun bookingJson(seatIds: List<UUID>): String {
        val seatArray = seatIds.joinToString(",") { "\"$it\"" }
        return """
            {
                "userId": "$userId",
                "scheduleId": "$scheduleId",
                "seatIds": [$seatArray]
            }
        """.trimIndent()
    }

    private fun performBooking(requestJson: String): BookingResponse {
        val mvcResult = mockMvc.perform(
            post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
            .andExpect(request().asyncStarted())
            .andReturn()

        val asyncResult = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()

        return objectMapper.readValue(
            asyncResult.response.contentAsString,
            BookingResponse::class.java
        )
    }

    // ========================================================================
    // Test 1: Feign Retry — Payment intent creation fails once, succeeds on retry
    // ========================================================================
    @Test
    fun `test intent creation fails initially but succeeds on retry`() {
        val seats = getAvailableSeats()
        assertTrue(seats.isNotEmpty())
        val seatId = UUID.fromString(seats.first().id.toString())
        val payId = "pay_retry_${UUID.randomUUID()}"

        whenever(paymentClient.createPaymentIntent(any()))
            .thenThrow(RuntimeException("Simulated Connection Timeout"))
            .thenReturn(PaymentResponse(payId, PaymentStatus.INITIATED))

        val response = performBooking(bookingJson(listOf(seatId)))

        // Booking should succeed after retry
        assertEquals(payId, response.payId)
        assertEquals(BookingStatus.PROCESSING.name, response.status)

        // Retry verified
        verify(paymentClient, times(2)).createPaymentIntent(any())
    }

    // ========================================================================
    // Test 2: Kafka Success — External Kafka event confirms booking instantly
    // ========================================================================
    @Test
    fun `test kafka event confirms booking and overrides poller`() {
        val seats = getAvailableSeats()
        assertTrue(seats.size >= 2, "Need at least 2 seats for this test")
        val seatId = UUID.fromString(seats[1].id.toString())

        val payId = "pay_kafka_${UUID.randomUUID()}"

        whenever(paymentClient.createPaymentIntent(any()))
            .thenReturn(PaymentResponse(payId, PaymentStatus.INITIATED))

        val response = performBooking(bookingJson(listOf(seatId)))
        val bookingId = response.bookingId

        // Simulate Kafka payment success event via internal test endpoint
        val kafkaPayload = """
            {
                "payId": "$payId",
                "status": "SUCCESS",
                "refNumber": "REF-TEST-001"
            }
        """.trimIndent()

        mockMvc.perform(post("/internal/kafka/payment-event")
            .contentType(MediaType.APPLICATION_JSON)
            .content(kafkaPayload))
            .andExpect(status().isOk)

        // Kafka consumer to process
        paymentKafkaListener.handlePaymentEvent(kafkaPayload)

        // Assert: Booking is CONFIRMED and seat is BOOKED
        val booking = bookingRepository.findById(UUID.fromString(bookingId.toString()))!!
        assertEquals(BookingStatus.CONFIRMED.name, booking.status)

        val seat = seatRepository.findByIds(listOf(seatId)).firstOrNull()!!
        assertEquals(SeatStatus.BOOKED.name, seat.status)
    }

    // ========================================================================
    // Test 3: RabbitMQ Exhaustion — Poller retries exhaust, booking fails, seat reverts
    // ========================================================================
    @Test
    fun `test rabbitmq delay queue exhausts retries and fails booking`() {
        val seats = getAvailableSeats()
        assertTrue(seats.size >= 3, "Need at least 3 seats for this test")
        val seatId = UUID.fromString(seats[2].id.toString())

        val payId = "pay_exhaust_${UUID.randomUUID()}"

        whenever(paymentClient.createPaymentIntent(any()))
            .thenReturn(PaymentResponse(payId, PaymentStatus.INITIATED))

        // Payment status always returns INITIATED (user never completes payment)
        whenever(paymentClient.getPaymentStatus(any()))
            .thenReturn(PaymentResponse(payId, PaymentStatus.INITIATED))

        val response = performBooking(bookingJson(listOf(seatId)))
        val bookingId = response.bookingId

        // Assert initial state is PROCESSING
        val initialBooking = bookingRepository.findById(UUID.fromString(bookingId.toString()))!!
        assertEquals(BookingStatus.PROCESSING.name, initialBooking.status)

        // Directly invoke the poll listener with retryCount=3 (max) to simulate exhaustion
        // This avoids waiting for real RabbitMQ delays (10s, 30s, 60s)
        paymentPollListener.pollPaymentStatus(PollRequest(UUID.fromString(bookingId.toString()), 3))

        // Assert: Booking is FAILED and seat is back to AVAILABLE
        val finalBooking = bookingRepository.findById(UUID.fromString(bookingId.toString()))!!
        assertEquals(BookingStatus.FAILED.name, finalBooking.status)

        val finalSeat = seatRepository.findByIds(listOf(seatId)).firstOrNull()!!
        assertEquals(SeatStatus.AVAILABLE.name, finalSeat.status)
    }

    // ========================================================================
    // Test 4: Concurrent Lock — Second booking for same seat gets 409 CONFLICT
    // ========================================================================
    @Test
    fun `test concurrent booking for same seat returns conflict`() {
        val seats = getAvailableSeats()
        assertTrue(seats.size >= 4, "Need at least 4 seats for this test")
        val seatId = seats[3].id

        // Pre-acquire a Redis lock on this seat (simulating another user's in-flight booking)
        val lockKey = "booking:$scheduleId:$seatId"
        redisTemplate.opsForValue().set(lockKey, "locked", Duration.ofMinutes(1))

        whenever(paymentClient.createPaymentIntent(any()))
            .thenReturn(PaymentResponse("pay_${UUID.randomUUID()}", PaymentStatus.INITIATED))

        // This booking attempt should fail because the seat lock is already held
        val mvcResult = mockMvc.perform(
            post("/v1/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(listOf(seatId)))
        )
            .andExpect(request().asyncStarted())
            .andReturn()

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
            .andExpect(status().isConflict)

        // Verify: payment was never even called (lock failed before reaching payment)
        verify(paymentClient, never()).createPaymentIntent(any())
    }
}
