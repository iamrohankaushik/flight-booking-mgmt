CREATE TABLE "users" (
    "id" UUID PRIMARY KEY,
    "metadata" JSONB
);

CREATE TABLE "flights" (
    "id" UUID PRIMARY KEY,
    "flight_number" VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE "flight_schedules" (
    "id" UUID PRIMARY KEY,
    "flight_id" UUID NOT NULL REFERENCES "flights"("id"),
    "source" VARCHAR(100) NOT NULL,
    "dest" VARCHAR(100) NOT NULL,
    "start_time" TIMESTAMP WITH TIME ZONE NOT NULL,
    "end_time" TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX "idx_flight_schedules_search" ON "flight_schedules" ("source", "dest", "start_time");

CREATE TABLE "seats" (
    "id" UUID PRIMARY KEY,
    "flight_id" UUID NOT NULL REFERENCES "flights"("id"),
    "schedule_id" UUID NOT NULL REFERENCES "flight_schedules"("id"),
    "status" VARCHAR(20) NOT NULL,
    "base_price" DECIMAL(10, 2) NOT NULL,
    "metadata" JSONB
);

CREATE INDEX "idx_seats_schedule" ON "seats" ("schedule_id");

CREATE TABLE "bookings" (
    "id" UUID PRIMARY KEY,
    "user_id" UUID NOT NULL REFERENCES "users"("id"),
    "schedule_id" UUID NOT NULL REFERENCES "flight_schedules"("id"),
    "seat_ids" JSONB NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE "payments" (
    "id" UUID PRIMARY KEY,
    "pay_id" VARCHAR(100) UNIQUE,
    "amount" DECIMAL(10, 2) NOT NULL,
    "booking_id" UUID NOT NULL REFERENCES "bookings"("id"),
    "status" VARCHAR(20) NOT NULL,
    "ref_number" VARCHAR(100),
    "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX "idx_payments_booking" ON "payments" ("booking_id");
CREATE INDEX "idx_payments_pay_id" ON "payments" ("pay_id");
