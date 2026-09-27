// k6 run load-test/k6/webhook-idempotency.js
//
// Simulates a gateway retrying webhook delivery for the SAME event: many VUs
// POST an identical (eventId, gatewayReference) payload concurrently. The
// endpoint always queues and returns 202 (burst absorption via Redis Streams);
// idempotency is proven by asserting - after the run - that `webhook_event`
// has exactly one row for the eventId and the payment/booking only settled once
// (see the SQL check printed at the end of this script and documented in README.md).
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SHOW_ID = Number(__ENV.SHOW_ID || 5);
const SEAT_ID = Number(__ENV.SEAT_ID || 11);
const CUSTOMER_ID = Number(__ENV.CUSTOMER_ID || 2);
const EVENT_ID = __ENV.EVENT_ID || `evt_loadtest_${Date.now()}`;

export const options = {
    scenarios: {
        duplicate_webhook_burst: {
            executor: 'shared-iterations',
            vus: Number(__ENV.VUS || 20),
            iterations: Number(__ENV.VUS || 20),
            maxDuration: '30s',
        },
    },
};

export function setup() {
    const holdRes = http.post(
        `${BASE_URL}/api/bookings`,
        JSON.stringify({ showId: SHOW_ID, customerId: CUSTOMER_ID, seatIds: [SEAT_ID] }),
        { headers: { 'Content-Type': 'application/json' } },
    );
    if (holdRes.status !== 201) {
        throw new Error(`setup: failed to create hold (status ${holdRes.status}): ${holdRes.body}`);
    }
    const bookingId = holdRes.json('bookingId');

    const payRes = http.post(`${BASE_URL}/api/bookings/${bookingId}/payments`, null);
    if (payRes.status !== 201) {
        throw new Error(`setup: failed to create payment (status ${payRes.status}): ${payRes.body}`);
    }
    const gatewayReference = payRes.json('gatewayReference');

    console.log(`Verify afterwards with: SELECT COUNT(*) FROM webhook_event WHERE event_id = '${EVENT_ID}'; -- expect 1`);
    console.log(`Also: SELECT status FROM booking WHERE booking_id = ${bookingId}; -- expect CONFIRMED`);

    return { gatewayReference };
}

export default function (data) {
    const res = http.post(
        `${BASE_URL}/api/webhooks/payment`,
        JSON.stringify({ eventId: EVENT_ID, gatewayReference: data.gatewayReference, status: 'SUCCESS' }),
        { headers: { 'Content-Type': 'application/json' } },
    );
    check(res, { 'webhook accepted (202)': (r) => r.status === 202 });
}
