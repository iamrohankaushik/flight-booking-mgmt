# Flight Booking System: Setup & Run Guide

## 1. Start Infrastructure
Run the following command from the project root:
```bash
docker-compose -f docker/docker-compose.yml up -d
```
This starts:
- PostgreSQL (5432)
- Redis Booking (6379)
- Redis Searching (6380)
- RabbitMQ (5672, 15672)
- Kafka (9092)
- WireMock (8081)

## 2. Generate JOOQ Code
Before starting the application, you must generate the JOOQ database classes based on the Flyway migrations:
```bash
./gradlew :common:jooqCodegen
```

## 3. Run Services
Start the services in separate terminals:

**Flight Booking Service:**
```bash
./gradlew :flight-booking:bootRun
```
- API: http://localhost:8080/v1/booking

**Flight Searching Service:**
```bash
./gradlew :flight-searching:bootRun
```
- API: http://localhost:8082/v1/search-flights
