# Flight Booking System

A multi-module Spring Boot application in Kotlin for managing flight bookings and searches.

## 🚀 How to Run

### 1. Start Infrastructure
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

### 2. Generate JOOQ Code
```bash
./gradlew :common:jooqCodegen
```

### 3. Run Services
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

## 🧪 Testing
- **WireMock Mapping**: Payment mock is at `http://localhost:8081/v1/payments`.
- **Search Cache**: Searching for the same `src/dest/date` twice will trigger the Redis LRU cache on port 6380.
- **Seat Locks**: Simultaneous booking attempts for the same seat will trigger Redis locks on port 6379.
