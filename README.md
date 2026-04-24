# Flight Booking System

A multi-module Spring Boot application in Kotlin for managing flight bookings and searches.

Architecture diagram:
<img src="doc/diagrams/high_level_architecture.png" width="800" height="600"/>

## 🚀 Getting Started
Please refer to the **[Setup & Run Guide](doc/setup_guide.md)** for instructions on how to boot up the Docker infrastructure, generate the database bindings, and start the local services.

## 🏗️ Project Architecture
The project is structured as a multi-module Gradle project:
- **`common`**: Shared module containing:
  - **`domain`**: Enum definitions for statuses.
  - **`dto`**: Data Transfer Objects for cross-service communication.
  - **`repository`** & **`repository/impl`**: JOOQ-based implementation of data access.
  - **`db/migration`**: Flyway SQL migrations.
- **`flight-booking`**: Service for seat locking, booking lifecycle, and payments.
  - **`client`**: OpenFeign client for external Payment API.
  - **`service/impl`**: Orchestration logic for bookings.
  - **`listener`**: Kafka (payment callbacks) and RabbitMQ (polling retries) consumers.
- **`flight-searching`**: Service for flight schedule discovery.
  - **`service/impl`**: Search logic with Redis LRU caching.

## 🧠 Core Logic & Flows
### A. Booking Flow
1. **Distributed Locking**: Redis `SETNX` on `booking:{scheduleId}:{seatId}`.
2. **State Transition**: Seat moves to `HOLD`, Booking created as `INITIATED`.
3. **Intent Creation Retries**: 3 attempts (Try 1 + 2 fallback retries) to create payment intent.
4. **Dual-Path Reconciliation**: 
   - **Kafka**: Event-driven callback for payment success/failure.
   - **RabbitMQ Polling**: Fallback path that polls the Payment API if Kafka is delayed.
5. **Finalization**: Listener updates status to `CONFIRMED` (Seat -> `BOOKED`) or `FAILED` (Seat -> `AVAILABLE`).

### B. Search & Caching
1. **Top 10 LRU**: Redis List used to track recent unique searches.
2. **Cache Pruning**: Automatically removes the oldest entry from Redis when a new search occurs and the cache is full.

## 📊 Visual Documentation (Diagrams)
Detailed technical diagrams are available in the `doc/diagrams` folder:
- **Booking Flow (Sequence)**: [booking_flow.mmd](doc/diagrams/booking_flow.mmd)
- **Payment Events (Sequence)**: [payment-events.mmd](doc/diagrams/payment-events.mmd)
- **Search LRU Caching (Sequence)**: [search_flow.mmd](doc/diagrams/search_flow.mmd)
- **State Machine (Transitions)**: [state_transitions.mmd](doc/diagrams/state_transitions.mmd)
- **Database Architecture (ERD)**: [db_schema.mmd](doc/diagrams/db_schema.mmd)

## 🧪 Testing
Please refer to the comprehensive [Testing Guide](doc/testing_guide.md) for detailed `curl` commands and scenarios to test both the Booking Flow and Search LRU Cache logic.
