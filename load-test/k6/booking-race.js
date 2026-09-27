// k6 run load-test/k6/booking-race.js
//
// Many VUs race to hold the SAME seat on the SAME show at the same time.
// Expectation: exactly one request succeeds (201), all others are rejected
// (409) by either the Redis fast-path lock or the show_seat_lock PK — proving
// no double-booking under concurrent load.
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SHOW_ID = Number(__ENV.SHOW_ID || 2);
const SEAT_ID = Number(__ENV.SEAT_ID || 3);
const CUSTOMER_ID = Number(__ENV.CUSTOMER_ID || 1);

export const successes = new Counter('booking_successes');
export const conflicts = new Counter('booking_conflicts');
export const unexpected = new Counter('booking_unexpected_errors');

export const options = {
    scenarios: {
        seat_race: {
            executor: 'per-vu-iterations',
            vus: Number(__ENV.VUS || 50),
            iterations: 1,
            maxDuration: '30s',
        },
    },
};

export default function () {
    const res = http.post(
        `${BASE_URL}/api/bookings`,
        JSON.stringify({ showId: SHOW_ID, customerId: CUSTOMER_ID, seatIds: [SEAT_ID] }),
        { headers: { 'Content-Type': 'application/json' } },
    );

    if (res.status === 201) {
        successes.add(1);
    } else if (res.status === 409) {
        conflicts.add(1);
    } else {
        unexpected.add(1);
    }

    check(res, {
        'status is 201 or 409': (r) => r.status === 201 || r.status === 409,
    });
}

// After the run, assert:
//   sum(booking_successes) === 1
//   sum(booking_conflicts) === VUS - 1
//   sum(booking_unexpected_errors) === 0
