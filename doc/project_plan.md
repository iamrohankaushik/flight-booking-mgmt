# Flight Booking System - Final Project Plan

This document outlines the architecture, documentation for the Flight Booking System.

## 1. Project Architecture
The project is structured as a multi-module Gradle project:
- **`common`**: Shared module containing:
  - **`domain`**: Enum definitions for statuses (Managed in code, stored as VARCHAR).
  - **`dto`**: Data Transfer Objects for cross-service communication.
  - **`repository`**: Interface-based DAO layer.
  - **`repository/impl`**: JOOQ-based implementation of data access.
  - **`db/migration`**: Flyway SQL migrations.
- **`flight-booking`**: Service for seat locking, booking lifecycle, and payments.
  - **`client`**: OpenFeign client for external Payment API.
  - **`service/impl`**: Orchestration logic for bookings.
  - **`listener`**: Kafka (payment callbacks) and RabbitMQ (polling retries) consumers.
- **`flight-searching`**: Service for flight schedule discovery.
  - **`service/impl`**: Search logic with Redis LRU caching.

## 2. Infrastructure Setup (Docker)
The environment consists of:
- **PostgreSQL**: Shared database for persistence.
- **Redis (Booking)**: Port 6379, used for seat locking (distributed locks).
- **Redis (Searching)**: Port 6380, used for Top 10 Search LRU cache.
- **RabbitMQ**: Messaging for payment TTL-based polling status retries.
- **Kafka**: Event stream for payment success/failure callbacks.
- **WireMock**: Mocked external Payment API with stateful scenarios on port 8081.

## 3. Visual Documentation (Diagrams)
Detailed technical diagrams are available in the `doc/diagrams` folder:
- **High-Level Design (HLD)**: [high_level_architecture.mmd](diagrams/high_level_architecture.mmd)
- **Booking Flow (Sequence)**: [booking_flow.mmd](diagrams/booking_flow.mmd)
- **State Machine (Transitions)**: [state_transitions.mmd](diagrams/state_transitions.mmd)
- **Database Architecture (ERD)**: [db_schema.mmd](diagrams/db_schema.mmd)

## 4. Core Logic & Flows
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
2. **Cache Pruning**: Automatically removes the 11th entry from Redis when a new search hit/miss occurs.

## 5. Key Technologies
- **Kotlin**: Core language.
- **Spring Boot**: Application framework.
- **JOOQ**: Type-safe SQL builder with DDL-based codegen.
- **Flyway**: Database versioning.
- **OpenFeign**: Declarative REST client.
- **RabbitMQ / Kafka**: Messaging and event streaming.
- **Docker**: Infrastructure orchestration.
