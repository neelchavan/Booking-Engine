-- High-Concurrency Booking Engine schema (MySQL 8, InnoDB, utf8mb4)
-- Normalization notes:
--  * booking_seat.price_at_booking is an intentional historical snapshot, not a
--    redundant copy of seat.price (seat prices can change after a booking is made).
--  * show.end_time is an intentional denormalization (derivable from movie.duration
--    + start_time) kept for cheap read-time queries; it does not break BCNF since it
--    still depends on the whole key (show_id).
--  * show_seat_lock is the concurrency-safety linchpin: its PRIMARY KEY (show_id,
--    seat_id) means a second concurrent attempt to hold/book the same seat for the
--    same show fails at INSERT time with a duplicate-key error, independent of any
--    application-level locking.

CREATE TABLE theatre (
    theatre_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE screen (
    screen_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    theatre_id   BIGINT NOT NULL,
    name         VARCHAR(50) NOT NULL,
    total_seats  INT NOT NULL,
    CONSTRAINT fk_screen_theatre FOREIGN KEY (theatre_id) REFERENCES theatre (theatre_id),
    CONSTRAINT uq_screen_theatre_name UNIQUE (theatre_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE movie (
    movie_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    language          VARCHAR(50) NOT NULL,
    duration_minutes  INT NOT NULL,
    genre             VARCHAR(100),
    release_date      DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `show` (
    show_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id     BIGINT NOT NULL,
    screen_id    BIGINT NOT NULL,
    show_date    DATE NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movie (movie_id),
    CONSTRAINT fk_show_screen FOREIGN KEY (screen_id) REFERENCES screen (screen_id),
    CONSTRAINT uq_show_screen_slot UNIQUE (screen_id, show_date, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_show_screen_date_time ON `show` (screen_id, show_date, start_time);

CREATE TABLE seat (
    seat_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    screen_id    BIGINT NOT NULL,
    row_label    VARCHAR(5) NOT NULL,
    seat_number  INT NOT NULL,
    seat_type    ENUM('SILVER', 'GOLD', 'PLATINUM') NOT NULL DEFAULT 'SILVER',
    price        DECIMAL(8, 2) NOT NULL,
    CONSTRAINT fk_seat_screen FOREIGN KEY (screen_id) REFERENCES screen (screen_id),
    CONSTRAINT uq_seat_screen_position UNIQUE (screen_id, row_label, seat_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE customer (
    customer_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    phone        VARCHAR(20),
    CONSTRAINT uq_customer_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE booking (
    booking_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id       BIGINT NOT NULL,
    customer_id   BIGINT NOT NULL,
    status        ENUM('HOLD', 'CONFIRMED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'HOLD',
    total_amount  DECIMAL(10, 2) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP NOT NULL,
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES `show` (show_id),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_booking_show ON booking (show_id);
CREATE INDEX idx_booking_status_expiry ON booking (status, expires_at);

-- Historical/audit record of which seats belong to which booking.
CREATE TABLE booking_seat (
    booking_id        BIGINT NOT NULL,
    seat_id           BIGINT NOT NULL,
    price_at_booking  DECIMAL(8, 2) NOT NULL,
    PRIMARY KEY (booking_id, seat_id),
    CONSTRAINT fk_booking_seat_booking FOREIGN KEY (booking_id) REFERENCES booking (booking_id),
    CONSTRAINT fk_booking_seat_seat FOREIGN KEY (seat_id) REFERENCES seat (seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Live, concurrency-safe reservation state: at most one row per (show_id, seat_id)
-- ever exists, so a duplicate INSERT for an already-held/booked seat fails atomically.
CREATE TABLE show_seat_lock (
    show_id     BIGINT NOT NULL,
    seat_id     BIGINT NOT NULL,
    booking_id  BIGINT NOT NULL,
    status      ENUM('HOLD', 'CONFIRMED') NOT NULL DEFAULT 'HOLD',
    expires_at  TIMESTAMP NULL,
    PRIMARY KEY (show_id, seat_id),
    CONSTRAINT fk_lock_show FOREIGN KEY (show_id) REFERENCES `show` (show_id),
    CONSTRAINT fk_lock_seat FOREIGN KEY (seat_id) REFERENCES seat (seat_id),
    CONSTRAINT fk_lock_booking FOREIGN KEY (booking_id) REFERENCES booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_lock_status_expiry ON show_seat_lock (status, expires_at);

CREATE TABLE payment (
    payment_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id         BIGINT NOT NULL,
    amount             DECIMAL(10, 2) NOT NULL,
    gateway_reference  VARCHAR(100) NOT NULL,
    status             ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking (booking_id),
    CONSTRAINT uq_payment_booking UNIQUE (booking_id),
    CONSTRAINT uq_payment_gateway_reference UNIQUE (gateway_reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Idempotency guard: event_id comes from the payment gateway, so a duplicate
-- webhook delivery fails this INSERT and is skipped instead of reprocessed.
CREATE TABLE webhook_event (
    event_id      VARCHAR(100) PRIMARY KEY,
    payment_id    BIGINT NOT NULL,
    payload       JSON NOT NULL,
    received_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at  TIMESTAMP NULL,
    CONSTRAINT fk_webhook_payment FOREIGN KEY (payment_id) REFERENCES payment (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
