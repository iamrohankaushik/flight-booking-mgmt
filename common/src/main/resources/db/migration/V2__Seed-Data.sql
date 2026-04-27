-- Seed Data for Local Testing
-- Sample User
INSERT INTO users (id, metadata)
VALUES ('d290f1ee-6c54-4b01-90e6-d701748f0851', '{"name": "John Doe", "email": "john@example.com"}');

-- Sample Flight
INSERT INTO flights (id, flight_number)
VALUES ('a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'AI-101');

-- Sample Schedule (London to New York)
INSERT INTO flight_schedules (id, flight_id, source, dest, start_time, end_time)
VALUES ('f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f',
        'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'LHR',
        'JFK',
        NOW() + INTERVAL '1 day',
        NOW() + INTERVAL '1 day 8 hours');

-- Sample Seats (Valid hex UUIDs)
INSERT INTO seats (id, flight_id, schedule_id, status, base_price, metadata)
VALUES ('f1000000-0000-0000-0000-000000000001', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 150.00, '{"row": "1", "col": "A"}'),
       ('f1000000-0000-0000-0000-000000000002', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 150.00, '{"row": "1", "col": "B"}'),
       ('f1000000-0000-0000-0000-000000000003', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 150.00, '{"row": "1", "col": "C"}'),
       ('f1000000-0000-0000-0000-000000000004', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 150.00, '{"row": "2", "col": "A"}'),
       ('f1000000-0000-0000-0000-000000000005', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 150.00, '{"row": "2", "col": "B"}'),
       ('f1000000-0000-0000-0000-000000000006', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 120.00, '{"row": "3", "col": "A"}'),
       ('f1000000-0000-0000-0000-000000000007', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 120.00, '{"row": "3", "col": "B"}'),
       ('f1000000-0000-0000-0000-000000000008', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 120.00, '{"row": "4", "col": "A"}'),
       ('f1000000-0000-0000-0000-000000000009', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 100.00, '{"row": "5", "col": "A"}'),
       ('f1000000-0000-0000-0000-000000000010', 'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
        'f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'AVAILABLE', 100.00, '{"row": "5", "col": "B"}');

-- Seed Flights
INSERT INTO "flights" ("id", "flight_number")
VALUES ('f2222222-2222-2222-2222-222222222222', 'AI-202'),
       ('f3333333-3333-3333-3333-333333333333', 'AI-303'),
       ('f4444444-4444-4444-4444-444444444444', 'AI-404'),
       ('f5555555-5555-5555-5555-555555555555', 'AI-505'),
       ('f6666666-6666-6666-6666-666666666666', 'AI-606');

-- Seed Flight Schedules
INSERT INTO "flight_schedules" ("id", "flight_id", "source", "dest", "start_time", "end_time")
VALUES ('a2222222-2222-2222-2222-222222222222', 'f2222222-2222-2222-2222-222222222222', 'BOM', 'BLR',
        NOW() + INTERVAL '1 day 4 hours', NOW() + INTERVAL '1 day 6 hours'),
       ('a3333333-3333-3333-3333-333333333333', 'f3333333-3333-3333-3333-333333333333', 'DEL', 'BLR',
        NOW() + INTERVAL '1 day 8 hours', NOW() + INTERVAL '1 day 10 hours'),
       ('a4444444-4444-4444-4444-444444444444', 'f4444444-4444-4444-4444-444444444444', 'BLR', 'MAA',
        NOW() + INTERVAL '1 day 12 hours', NOW() + INTERVAL '1 day 13 hours'),
       ('a5555555-5555-5555-5555-555555555555', 'f5555555-5555-5555-5555-555555555555', 'MAA', 'HYD',
        NOW() + INTERVAL '1 day 14 hours', NOW() + INTERVAL '1 day 15 hours'),
       ('a6666666-6666-6666-6666-666666666666', 'f6666666-6666-6666-6666-666666666666', 'HYD', 'DEL',
        NOW() + INTERVAL '1 day 16 hours', NOW() + INTERVAL '1 day 18 hours');

-- Seed Seats for Schedule 2 (AI-202 BOM-BLR)
INSERT INTO "seats" ("id", "flight_id", "schedule_id", "status", "base_price", "metadata")
VALUES (gen_random_uuid(), 'f2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', 'AVAILABLE',
        4000.00, '{}'),
       (gen_random_uuid(), 'f2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', 'AVAILABLE',
        4200.00, '{}');

-- Seed Seats for Schedule 3 (AI-303 DEL-BLR)
INSERT INTO "seats" ("id", "flight_id", "schedule_id", "status", "base_price", "metadata")
VALUES (gen_random_uuid(), 'f3333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', 'AVAILABLE',
        7000.00, '{}'),
       (gen_random_uuid(), 'f3333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', 'AVAILABLE',
        7500.00, '{}');

-- Seed Seats for Schedule 4 (AI-404 BLR-MAA)
INSERT INTO "seats" ("id", "flight_id", "schedule_id", "status", "base_price", "metadata")
VALUES (gen_random_uuid(), 'f4444444-4444-4444-4444-444444444444', 'a4444444-4444-4444-4444-444444444444', 'AVAILABLE',
        3000.00, '{}'),
       (gen_random_uuid(), 'f4444444-4444-4444-4444-444444444444', 'a4444444-4444-4444-4444-444444444444', 'AVAILABLE',
        3500.00, '{}');

-- Seed Seats for Schedule 5 (AI-505 MAA-HYD)
INSERT INTO "seats" ("id", "flight_id", "schedule_id", "status", "base_price", "metadata")
VALUES (gen_random_uuid(), 'f5555555-5555-5555-5555-555555555555', 'a5555555-5555-5555-5555-555555555555', 'AVAILABLE',
        3200.00, '{}'),
       (gen_random_uuid(), 'f5555555-5555-5555-5555-555555555555', 'a5555555-5555-5555-5555-555555555555', 'AVAILABLE',
        3400.00, '{}');

-- Seed Seats for Schedule 6 (AI-606 HYD-DEL)
INSERT INTO "seats" ("id", "flight_id", "schedule_id", "status", "base_price", "metadata")
VALUES (gen_random_uuid(), 'f6666666-6666-6666-6666-666666666666', 'a6666666-6666-6666-6666-666666666666', 'AVAILABLE',
        4500.00, '{}'),
       (gen_random_uuid(), 'f6666666-6666-6666-6666-666666666666', 'a6666666-6666-6666-6666-666666666666', 'AVAILABLE',
        4800.00, '{}');
