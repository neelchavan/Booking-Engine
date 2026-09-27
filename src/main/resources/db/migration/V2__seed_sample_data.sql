-- Sample data illustrating the schema; also used as fixtures for local dev/manual
-- testing of the P2 "shows by theatre + date" query.

INSERT INTO theatre (theatre_id, name, city, address) VALUES
    (1, 'PVR Forum Mall', 'Bengaluru', 'Forum Mall, Hosur Road, Koramangala');

INSERT INTO screen (screen_id, theatre_id, name, total_seats) VALUES
    (1, 1, 'Screen 1', 20),
    (2, 1, 'Screen 2', 20);

INSERT INTO movie (movie_id, title, language, duration_minutes, genre, release_date) VALUES
    (1, 'Galaxy Raiders', 'English', 148, 'Sci-Fi', '2026-08-15'),
    (2, 'Monsoon Melody', 'Hindi', 132, 'Drama', '2026-09-01'),
    (3, 'The Last Wicket', 'English', 121, 'Sports', '2026-09-20');

-- Two shows/day on each screen across the "next 7 dates" window shown to users.
INSERT INTO `show` (show_id, movie_id, screen_id, show_date, start_time, end_time) VALUES
    (1,  1, 1, '2026-09-27', '10:00:00', '12:28:00'),
    (2,  2, 1, '2026-09-27', '15:00:00', '17:12:00'),
    (3,  1, 1, '2026-09-28', '10:00:00', '12:28:00'),
    (4,  3, 1, '2026-09-28', '18:30:00', '20:31:00'),
    (5,  2, 1, '2026-09-29', '15:00:00', '17:12:00'),
    (6,  3, 1, '2026-09-29', '18:30:00', '20:31:00'),
    (7,  1, 1, '2026-09-30', '10:00:00', '12:28:00'),
    (8,  2, 1, '2026-10-01', '15:00:00', '17:12:00'),
    (9,  3, 1, '2026-10-02', '18:30:00', '20:31:00'),
    (10, 1, 1, '2026-10-03', '10:00:00', '12:28:00'),
    (11, 2, 2, '2026-09-27', '11:00:00', '13:12:00'),
    (12, 3, 2, '2026-09-27', '19:00:00', '21:01:00'),
    (13, 1, 2, '2026-09-28', '11:00:00', '13:28:00'),
    (14, 2, 2, '2026-09-29', '16:00:00', '18:12:00'),
    (15, 3, 2, '2026-09-30', '19:00:00', '21:01:00'),
    (16, 1, 2, '2026-10-01', '11:00:00', '13:28:00'),
    (17, 2, 2, '2026-10-02', '16:00:00', '18:12:00'),
    (18, 3, 2, '2026-10-03', '19:00:00', '21:01:00');

-- 10 seats per screen (2 rows x 5), a mix of GOLD/PLATINUM pricing.
INSERT INTO seat (seat_id, screen_id, row_label, seat_number, seat_type, price) VALUES
    (1, 1, 'A', 1, 'GOLD', 220.00),
    (2, 1, 'A', 2, 'GOLD', 220.00),
    (3, 1, 'A', 3, 'GOLD', 220.00),
    (4, 1, 'A', 4, 'GOLD', 220.00),
    (5, 1, 'A', 5, 'GOLD', 220.00),
    (6, 1, 'B', 1, 'PLATINUM', 320.00),
    (7, 1, 'B', 2, 'PLATINUM', 320.00),
    (8, 1, 'B', 3, 'PLATINUM', 320.00),
    (9, 1, 'B', 4, 'PLATINUM', 320.00),
    (10, 1, 'B', 5, 'PLATINUM', 320.00),
    (11, 2, 'A', 1, 'GOLD', 220.00),
    (12, 2, 'A', 2, 'GOLD', 220.00),
    (13, 2, 'A', 3, 'GOLD', 220.00),
    (14, 2, 'A', 4, 'GOLD', 220.00),
    (15, 2, 'A', 5, 'GOLD', 220.00),
    (16, 2, 'B', 1, 'PLATINUM', 320.00),
    (17, 2, 'B', 2, 'PLATINUM', 320.00),
    (18, 2, 'B', 3, 'PLATINUM', 320.00),
    (19, 2, 'B', 4, 'PLATINUM', 320.00),
    (20, 2, 'B', 5, 'PLATINUM', 320.00);

INSERT INTO customer (customer_id, name, email, phone) VALUES
    (1, 'Asha Rao', 'asha.rao@example.com', '+919845012345'),
    (2, 'Vikram Shah', 'vikram.shah@example.com', '+919845098765');

-- Confirmed booking: Asha booked seats A1 & A2 for show 1, paid successfully.
INSERT INTO booking (booking_id, show_id, customer_id, status, total_amount, created_at, expires_at) VALUES
    (1, 1, 1, 'CONFIRMED', 440.00, '2026-09-26 09:00:00', '2026-09-26 09:10:00');

INSERT INTO booking_seat (booking_id, seat_id, price_at_booking) VALUES
    (1, 1, 220.00),
    (1, 2, 220.00);

INSERT INTO show_seat_lock (show_id, seat_id, booking_id, status, expires_at) VALUES
    (1, 1, 1, 'CONFIRMED', NULL),
    (1, 2, 1, 'CONFIRMED', NULL);

INSERT INTO payment (payment_id, booking_id, amount, gateway_reference, status, created_at, updated_at) VALUES
    (1, 1, 440.00, 'pay_mock_0001', 'SUCCESS', '2026-09-26 09:01:00', '2026-09-26 09:01:30');

INSERT INTO webhook_event (event_id, payment_id, payload, received_at, processed_at) VALUES
    ('evt_mock_0001', 1, JSON_OBJECT('event', 'payment.succeeded', 'gateway_reference', 'pay_mock_0001', 'amount', 440.00), '2026-09-26 09:01:15', '2026-09-26 09:01:30');

-- Pending hold: Vikram is mid-checkout on seat B1 for show 1 (demo of the HOLD state).
INSERT INTO booking (booking_id, show_id, customer_id, status, total_amount, created_at, expires_at) VALUES
    (2, 1, 2, 'HOLD', 320.00, NOW(), DATE_ADD(NOW(), INTERVAL 10 MINUTE));

INSERT INTO booking_seat (booking_id, seat_id, price_at_booking) VALUES
    (2, 6, 320.00);

INSERT INTO show_seat_lock (show_id, seat_id, booking_id, status, expires_at) VALUES
    (1, 6, 2, 'HOLD', DATE_ADD(NOW(), INTERVAL 10 MINUTE));
