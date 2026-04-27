# Movie Ticket Booking System — Low-Level Design Guide

## A complete learning resource for understanding the design decisions, concurrency patterns, and implementation details behind a production-grade ticket booking system.

---

## Table of Contents

1. [Problem Understanding](#1-problem-understanding)
2. [Requirement Gathering — The Interview Mindset](#2-requirement-gathering)
3. [Entity Identification — Thinking in Objects](#3-entity-identification)
4. [Relationships — Why They Matter](#4-relationships)
5. [The ShowSeat Abstraction — The Most Important Design Decision](#5-the-showseat-abstraction)
6. [Seat Status State Machine](#6-seat-status-state-machine)
7. [Concurrency — The Heart of the Problem](#7-concurrency)
8. [The Seat Locking Mechanism — Deep Dive](#8-seat-locking-mechanism)
9. [The Booking Flow — Step by Step](#9-booking-flow)
10. [Design Patterns Applied](#10-design-patterns)
11. [Exception Handling Strategy](#11-exception-handling)
12. [Payment Retry and Failure Recovery](#12-payment-retry)
13. [Scaling Considerations — From Single JVM to Distributed](#13-scaling)
14. [Common Interview Follow-ups](#14-interview-followups)
15. [Complete Code Reference](#15-code-reference)

---

## 1. Problem Understanding

At its core, the Movie Ticket Booking System is a **resource allocation problem under concurrency**. You have a finite set of seats for each show, and multiple users competing for them simultaneously. The system must guarantee that no two users can book the same seat — ever.

This makes it fundamentally different from, say, a blog platform or an e-commerce catalog. Those systems can tolerate eventual consistency. A ticket booking system cannot. If User A and User B both see seat A1 as available and both click "Book," exactly one must succeed and the other must fail. There is no middle ground.

The secondary challenge is **session management**. A user who selects seats but walks away (closes the browser, gets distracted, loses internet) shouldn't permanently block those seats. The system needs a timeout mechanism that reclaims abandoned seats.

### What interviewers are really testing

When an interviewer asks you to design this system, they're evaluating:

- Can you identify the **concurrency bottleneck** (seat selection) without being told?
- Do you understand the difference between **optimistic and pessimistic locking**?
- Can you design a **state machine** for seat lifecycle?
- Do you separate concerns cleanly — locking logic vs. business logic vs. payment logic?
- Can you handle **failure recovery** gracefully — what happens when payment fails?

---

## 2. Requirement Gathering — The Interview Mindset

Before writing a single line of code, the structured approach is to clarify requirements with the interviewer. This is not a formality — it directly shapes your design.

### Questions you should ask

**"How should we handle concurrent users trying to book the same seats?"**
This tells the interviewer you recognize concurrency as the core challenge. The answer will guide whether you use pessimistic locking (lock rows in DB), optimistic locking (version numbers), or distributed locks (Redis).

**"What happens if a user selects seats but doesn't complete payment?"**
This reveals the need for a timeout/TTL mechanism. Without this, a malicious or careless user could lock up all seats indefinitely.

**"Are there timing constraints for completing a booking?"**
This defines the lock duration. Too short (30 seconds) and users can't enter payment details. Too long (30 minutes) and seats stay blocked.

**"Should we support multiple seat types with different pricing?"**
This shapes the entity model — do you need a `SeatType` enum? Does pricing live on the seat or on a per-show basis?

### The agreed-upon scope for this design

After clarification, the requirements settle into:

- A theatre has multiple screens, each with a fixed seat layout.
- Shows are scheduled on screens for specific movies.
- Users browse available shows, select seats, and complete booking with payment.
- Seats are temporarily locked during the payment window (8 minutes).
- If payment isn't completed within the timeout, seats are released.
- No two users can book the same seat for the same show.

---

## 3. Entity Identification — Thinking in Objects

The entities fall into three natural groups:

### Infrastructure entities (rarely change)

- **Theatre** — a physical location. Has a name, city, and a list of screens.
- **Screen** — a hall within the theatre. Has a name and a fixed set of seats.
- **Seat** — a physical chair. Has a row number, seat number, and type (Regular/Premium/VIP). This entity represents the *physical* seat — it doesn't change between shows.

### Scheduling entities (change per show)

- **Movie** — the film being screened. Has a title, duration, and genre.
- **Show** — a specific screening. Ties together a movie, a screen, and a time slot.
- **ShowSeat** — the critical bridge entity. Represents a specific seat for a specific show. This is where availability, pricing, and status live. (More on this below.)

### Transaction entities (change per booking)

- **Booking** — a user's reservation. Links a user to a show and a set of seats.
- **Payment** — the financial transaction. Tracks amount, method, and status.

### Why this grouping matters

Infrastructure entities are set up once and rarely modified. Scheduling entities are created when a theatre publishes its show schedule. Transaction entities are created in real-time as users book. Separating these concerns means your locking and concurrency logic only needs to touch **ShowSeat** and **Booking** — not the entire entity graph.

---

## 4. Relationships — Why They Matter

Here is how the entities connect:

```
Theatre  1 ──── N  Screen  1 ──── N  Seat
                        │
                        │ 1
                        │
                        N
                      Show  N ──── 1  Movie
                        │
                        │ 1
                        │
                        N
                    ShowSeat
                        │
                        │ N
                        │
                        1
                     Booking  1 ──── 1  Payment
```

Key relationships to articulate in an interview:

- **Theatre → Screen**: One-to-many. A theatre contains multiple screens.
- **Screen → Seat**: One-to-many. A screen has a fixed set of physical seats.
- **Screen → Show**: One-to-many. A screen hosts multiple shows at different times.
- **Movie → Show**: One-to-many. A movie can be shown multiple times across screens.
- **Show + Seat → ShowSeat**: Many-to-many bridge. Each show has its own set of ShowSeats.
- **Booking → ShowSeat**: One-to-many. A booking reserves multiple seats.
- **Booking → Payment**: One-to-one. Each booking has exactly one payment.

---

## 5. The ShowSeat Abstraction — The Most Important Design Decision

This deserves its own section because it's the single most impactful design choice in the entire system, and it's the one most candidates miss.

### The naive approach (and why it fails)

A first instinct might be to track seat availability directly on the `Seat` entity:

```java
class Seat {
    private String id;
    private int row;
    private int number;
    private SeatStatus status;  // AVAILABLE, LOCKED, BOOKED
}
```

This breaks immediately. A `Seat` is a **physical object** — row 1, seat 3 exists regardless of what's showing. If you put `status` on `Seat`, then seat A3 can only have one status at a time. But seat A3 might be:

- **BOOKED** for the 6:00 PM show
- **AVAILABLE** for the 9:00 PM show
- **LOCKED** for tomorrow's 3:00 PM show

One status field cannot represent three simultaneous states.

### The correct approach: ShowSeat

`ShowSeat` is the **join entity** between `Show` and `Seat`. It represents "seat A3 *during the 6 PM show*" — a unique, bookable unit.

```java
class ShowSeat {
    private String id;
    private Show show;       // which show
    private Seat seat;       // which physical seat
    private double price;    // price can vary by show (matinee vs. evening)
    private SeatStatus status;  // AVAILABLE, LOCKED, BOOKED
    private String lockedBy;    // which user holds the lock
}
```

Now each show has its own independent set of ShowSeat records. The 6 PM and 9 PM shows don't interfere with each other. Locking seat A3 for the 6 PM show has zero impact on the 9 PM show's A3.

### Why price lives on ShowSeat, not Seat

Pricing often varies by show timing (matinee tickets are cheaper), by day (weekends cost more), and by seat type. Putting `price` on `ShowSeat` gives maximum flexibility — the system can set per-seat, per-show pricing without changing the seat layout.

### The database implication

In a relational database, `ShowSeat` would be a table with a **composite unique constraint** on `(show_id, seat_id)`. This is your last line of defense against double bookings — even if the application-level locking fails, the database constraint prevents duplicates.

---

## 6. Seat Status State Machine

Every `ShowSeat` moves through a well-defined lifecycle:

```
            lockSeats()           confirmSeats()
AVAILABLE  ──────────►  LOCKED  ──────────────►  BOOKED
    ▲                     │                         │
    │                     │                         │
    │    unlockSeats()    │    cancelBooking()      │
    │  (timeout/cancel/   │    (+ refund)           │
    │   max retries)      │                         │
    └─────────────────────┘                         │
    └───────────────────────────────────────────────┘
```

### State definitions

**AVAILABLE**: The seat is open. Any user can attempt to lock it. This is the only state from which a lock can be acquired.

**LOCKED**: The seat is temporarily held for a specific user. Other users see it as unavailable. The lock has a TTL (time-to-live) — typically 5-10 minutes. If the user doesn't complete payment within this window, the seat reverts to AVAILABLE.

**BOOKED**: The seat is permanently reserved. Payment has been confirmed. The only way back to AVAILABLE is through an explicit cancellation (which triggers a refund).

### Why three states and not two?

You might think AVAILABLE and BOOKED are sufficient. But consider this scenario without LOCKED:

1. User A selects seat A3 (still AVAILABLE).
2. User A enters credit card details (takes 2 minutes).
3. User B selects seat A3 (still AVAILABLE — A hasn't paid yet).
4. User A submits payment. System tries to book A3.
5. User B submits payment. System also tries to book A3.

Both succeed? Double booking. The LOCKED state prevents this by giving User A exclusive access during the payment window.

### Transitions and who triggers them

| Transition | Trigger | Method |
|---|---|---|
| AVAILABLE → LOCKED | User selects seats | `SeatLockProvider.lockSeats()` |
| LOCKED → BOOKED | Payment succeeds | `SeatLockProvider.confirmSeats()` |
| LOCKED → AVAILABLE | Timeout expires | Background job / lazy check |
| LOCKED → AVAILABLE | User cancels | `BookingService.cancelBooking()` |
| LOCKED → AVAILABLE | Max payment retries exceeded | `BookingService.handlePaymentFailure()` |
| BOOKED → AVAILABLE | User cancels confirmed booking | `BookingService.cancelBooking()` + refund |

---

## 7. Concurrency — The Heart of the Problem

This is where most candidates either shine or struggle. The concurrency problem in ticket booking boils down to one thing: **the check-then-act race condition**.

### The race condition, illustrated

Without any locking:

```
Time    User A                          User B
─────   ─────────────────────────       ─────────────────────────────
T1      Check: Is seat A3 available?
T2                                      Check: Is seat A3 available?
T3      Yes → Mark A3 as LOCKED
T4                                      Yes → Mark A3 as LOCKED  ← BUG!
```

Both users read the status as AVAILABLE before either one writes. Both proceed. Double booking.

### The solution: atomic check-and-set

The fix is to make the "check if available" and "mark as locked" operations happen as a single, indivisible unit. There are several ways to achieve this:

### Approach 1: Synchronized block (single JVM)

```java
public synchronized boolean lockSeats(List<String> seatIds, String showId, String userId) {
    // Phase 1: Validate ALL seats are AVAILABLE
    for (String seatId : seatIds) {
        if (showSeatStore.get(seatId).getStatus() != SeatStatus.AVAILABLE) {
            return false;  // Fail fast, no partial lock
        }
    }
    // Phase 2: Lock ALL seats atomically
    for (ShowSeat ss : seatsToLock) {
        ss.setStatus(SeatStatus.LOCKED);
        ss.setLockedBy(userId);
    }
    return true;
}
```

The `synchronized` keyword ensures only one thread can execute this method at a time. Simple, correct, but limits throughput to one booking operation at a time across the entire system.

### Approach 2: Per-show locking (better)

Users booking seats for different shows don't conflict. Only users competing for the same show need serialization:

```java
ConcurrentHashMap<String, ReentrantLock> showLocks = new ConcurrentHashMap<>();

public boolean lockSeats(String showId, List<String> seatIds, String userId) {
    ReentrantLock lock = showLocks.computeIfAbsent(showId, k -> new ReentrantLock());
    lock.lock();
    try {
        // same check-then-act logic
    } finally {
        lock.unlock();
    }
}
```

Now the 6 PM show and 9 PM show can process bookings in parallel. Only users competing for the *same* show are serialized.

### Approach 3: Database-level CAS (production)

In a real system with multiple application servers, in-memory locks don't work (they're JVM-local). Instead, you use the database as the coordination point:

```sql
UPDATE show_seat
SET status = 'LOCKED', locked_by = :userId, locked_at = NOW()
WHERE id IN (:seatIds) AND status = 'AVAILABLE';
```

Then check `affected_rows`. If it equals the number of requested seats, all were locked successfully. If it's less, some seats were already taken — rollback the transaction.

This is essentially an **optimistic compare-and-swap (CAS)** at the database level. No application-level locking needed. The database's own row-level locks handle the concurrency.

### Approach 4: Distributed lock (Redis)

For extremely high throughput, use Redis:

```
SET lock:show:sh1:seat:A3 userId NX EX 480
```

`NX` = set only if not exists (atomic). `EX 480` = auto-expire in 480 seconds (8 minutes). If the SET returns OK, you have the lock. If it returns nil, someone else has it.

### Which to use in an interview?

Start with the synchronized approach (shows you understand the problem). Then mention per-show locking (shows you think about throughput). Then mention DB-level CAS or Redis (shows you've thought about distribution). This progression demonstrates depth.

---

## 8. The Seat Locking Mechanism — Deep Dive

### Why locking is a separate class (SeatLockProvider)

Following the Single Responsibility Principle, locking semantics are separated from booking business logic:

```java
class SeatLockProvider {
    // Owns: what does "lock" mean, how is atomicity guaranteed
    boolean lockSeats(List<String> seatIds, String showId, String userId);
    void unlockSeats(String showId, List<ShowSeat> seats, String userId);
    void confirmSeats(List<ShowSeat> seats);
}

class BookingService {
    // Owns: business workflow (create booking → pay → confirm)
    // Uses SeatLockProvider for the locking step
}
```

In production, you'd swap `SeatLockProvider`'s implementation from `synchronized` to Redis-backed without touching `BookingService`. This is the Open-Closed Principle in action.

### The two-phase lock pattern

The `lockSeats` method uses a deliberate two-phase approach:

**Phase 1 — Validate**: Iterate through all requested seats and verify each is AVAILABLE. If *any* seat is not available, return false immediately. This is the "check" phase.

**Phase 2 — Commit**: Only if all seats passed validation, iterate again and mark each as LOCKED. This is the "act" phase.

Why two passes? Because we want **all-or-nothing semantics**. If a user selects seats A1, A2, A3 and A2 is already taken, we don't want to lock A1 and A3 (a partial lock). The user asked for three specific seats. Either all three are available, or the request fails.

This maps directly to a database transaction: you'd check all rows, then update all rows within a single transaction. If any check fails, the transaction rolls back.

### Lock ownership

Every lock records *who* holds it (`lockedBy` field). This prevents several classes of bugs:

- User A can't accidentally confirm User B's locked seats.
- The unlock method verifies ownership: `if (userId.equals(ss.getLockedBy()))`.
- If a lock expires and gets reclaimed, a stale confirmation from the original user fails safely.

### Lock timeout and cleanup

Locks have a TTL (8 minutes in our design). There are two cleanup strategies:

**Active cleanup (background job)**: A scheduled task runs every 30 seconds, scans for expired locks, and releases them. This guarantees seats are freed promptly.

**Lazy cleanup**: When any user browses available seats, the system first scans for and releases expired locks. This ensures the UI never shows stale data.

In practice, you use both. The background job handles the general case, and lazy cleanup handles edge cases where the background job hasn't run yet.

---

## 9. The Booking Flow — Step by Step

Here's the complete flow from the user's perspective, with what happens internally at each step:

### Step 1: Browse shows

User selects a movie and theatre. System returns available shows.

**Internally**: Simple read query. No locking, no state changes. The system may lazily release expired locks at this point to ensure fresh availability data.

### Step 2: View seat layout

User picks a show. System displays the seat map with availability.

**Internally**: Query all `ShowSeat` records for the show. Filter by status. Return a grid showing AVAILABLE seats (selectable), LOCKED seats (grayed out), and BOOKED seats (grayed out). The user doesn't see the difference between LOCKED and BOOKED — both are "unavailable."

### Step 3: Select seats and initiate booking

User selects 1-N seats and clicks "Book."

**Internally** (`BookingService.createBooking()`):

1. First, check if any selected seat is `BOOKED` (permanent). If yes, throw `SeatPermanentlyUnavailableException`. This is a fast rejection — no need to attempt locking.
2. Call `SeatLockProvider.lockSeats()`. This atomically validates all seats are AVAILABLE and marks them LOCKED. If any seat was grabbed by another user between step 1 and now, this returns false — throw `SeatLockFailedException`.
3. Create a `Booking` record in `PENDING` state.
4. The 8-minute countdown starts now.

### Step 4: Payment

User enters payment details and submits.

**Internally** (`BookingService.processPayment()`):

1. The method receives a `PaymentStrategy` (UPI, credit card, wallet — the caller decides). This is the Strategy Pattern.
2. Call `strategy.pay(amount)`. The strategy talks to the payment gateway and returns a `Payment` object with a status.
3. If `PaymentStatus.SUCCESS`: call `SeatLockProvider.confirmSeats()` to move seats from LOCKED to BOOKED. Set booking status to CONFIRMED. Done.
4. If `PaymentStatus.FAILED`: call `handlePaymentFailure()` — increment retry counter, and if max retries exceeded, release seats and cancel booking.

### Step 5a: Success

Booking is CONFIRMED. Seats are BOOKED. User receives a ticket (booking ID, show details, seat numbers).

### Step 5b: Timeout

The 8-minute window expires before payment completes. The background cleanup job (or lazy cleanup) detects the expired lock, sets seats back to AVAILABLE, and marks the booking as EXPIRED. Another user can now book these seats.

### Step 5c: Payment failure after retries

Payment fails 3 times. The system automatically releases the locked seats and cancels the booking. Seats go back to AVAILABLE. This prevents seats from being indefinitely held by a user with a failing payment method.

---

## 10. Design Patterns Applied

### Strategy Pattern — Payment Processing

**Problem**: The system needs to support multiple payment methods (credit card, UPI, wallet) without modifying booking logic when new methods are added.

**Solution**: Define a `PaymentStrategy` interface with a single method `pay(double amount)`. Each payment method implements this interface:

```java
interface PaymentStrategy {
    Payment pay(double amount);
}

class UpiPaymentStrategy implements PaymentStrategy { ... }
class CreditCardPaymentStrategy implements PaymentStrategy { ... }
class WalletPaymentStrategy implements PaymentStrategy { ... }
```

The `BookingService` accepts any `PaymentStrategy`:

```java
public Booking processPayment(String bookingId, String userId,
                               PaymentStrategy strategy) {
    Payment payment = strategy.pay(booking.getTotalAmount());
    // ... handle result
}
```

**Why this matters**: Adding `NetBankingPaymentStrategy` requires zero changes to `BookingService`. This is the Open-Closed Principle — open for extension, closed for modification.

**Interview tip**: When the interviewer hears "Strategy Pattern for payment methods," they know you understand SOLID principles and can apply patterns appropriately (not just name them).

### Single Responsibility Principle — SeatLockProvider

**Problem**: If locking logic lives inside `BookingService`, the service becomes a god class that knows about locking mechanics, business rules, payment processing, and retry logic.

**Solution**: Extract locking into `SeatLockProvider`. Now each class has one reason to change:

- `SeatLockProvider` changes when the locking mechanism changes (e.g., from synchronized to Redis).
- `BookingService` changes when the business workflow changes (e.g., adding a loyalty points step).

### Enum-based state machines

Using enums (`SeatStatus`, `BookingStatus`, `PaymentStatus`) for state makes invalid transitions explicit. You can't accidentally set a seat to a status that doesn't exist. Combined with the state machine diagram, this makes the system's behavior predictable and debuggable.

---

## 11. Exception Handling Strategy

Exception handling in this system isn't just about catching errors — it's about **communicating failure modes** to the caller clearly.

### SeatPermanentlyUnavailableException

**When**: A selected seat has status `BOOKED`. This is a hard failure — no amount of retrying will make this seat available (unlike LOCKED, which might expire).

**Why a separate exception**: The caller needs to know the difference between "seat is taken forever" and "seat is temporarily held by someone else." The former should show "Seat unavailable" to the user. The latter might warrant a "Try again in a few minutes" message.

### SeatLockFailedException

**When**: The `SeatLockProvider.lockSeats()` call returns false. This means one or more seats were LOCKED by another user between the time the current user viewed the seat map and clicked "Book."

**Recovery**: The user should re-fetch the seat map and try different seats.

### BadRequestException

**When**: Invalid input — booking not found, user trying to modify someone else's booking, ShowSeat ID doesn't exist.

**Why generic**: These are programming errors or invalid API calls, not business failures. They should return HTTP 400 in a REST context.

### Retry tracking on payment failure

Rather than throwing on payment failure, the system tracks failures per booking. This is a deliberate design choice — the first two failures should let the user retry (maybe they entered the wrong CVV). Only after the third failure does the system give up and release the seats.

```java
private void handlePaymentFailure(Booking booking, String userId) {
    int failures = bookingFailures.getOrDefault(bookingId, 0) + 1;
    bookingFailures.put(bookingId, failures);

    if (failures >= ALLOWED_PAYMENT_RETRIES) {
        seatLockProvider.unlockSeats(...);
        booking.setStatus(BookingStatus.CANCELLED);
    }
}
```

---

## 12. Payment Retry and Failure Recovery

This is a topic interviewers love to probe because it tests your understanding of **distributed system failure modes**.

### Why retries matter

Payment gateways are external systems. They can timeout, return errors, or be temporarily unavailable. A single failure doesn't mean the user's payment method is invalid — it might just mean the gateway had a hiccup.

### The retry budget

Our system allows 3 attempts per booking. This is a trade-off:

- **Too few retries (1)**: Frustrating for users. A single network blip loses their seats.
- **Too many retries (10)**: Seats stay locked for too long. Other users can't book them.
- **3 retries**: Enough for transient failures, not so many that seats are hoarded.

### What happens after max retries

The system automatically:
1. Calls `unlockSeats()` to release the locked seats back to AVAILABLE.
2. Sets the booking status to CANCELLED.
3. Clears the failure counter.

This is important: the seats don't stay in limbo. Another user can immediately book them.

### The idempotency consideration

In production, you'd add an **idempotency key** to each payment attempt. This prevents double-charging if a payment request succeeds but the response is lost (network timeout). The payment gateway uses the idempotency key to deduplicate — if it receives the same key twice, it returns the original result instead of charging again.

---

## 13. Scaling Considerations — From Single JVM to Distributed

### Single JVM (what we implemented)

- `synchronized` or `ReentrantLock` for concurrency.
- In-memory `ConcurrentHashMap` for state.
- Works for a single theatre with moderate traffic.

### Multiple application servers (real-world)

When you have 2+ app servers behind a load balancer, in-memory locks don't work. Two servers can lock the same seat simultaneously because they don't share memory.

**Solution 1 — Database as coordinator**:

```sql
-- Optimistic CAS
UPDATE show_seat
SET status = 'LOCKED', locked_by = ?, locked_at = NOW()
WHERE id IN (?, ?, ?) AND status = 'AVAILABLE';
-- Check: affected_rows == 3? If yes, success. If no, rollback.
```

The database's row-level locks serialize concurrent updates. No application-level locking needed.

**Solution 2 — Redis distributed lock**:

```
SET lock:show:{showId}:seat:{seatId} {userId} NX EX 480
```

Faster than DB locking. The `NX` flag makes it atomic (set-if-not-exists). The `EX 480` auto-expires the lock after 8 minutes, handling the timeout for free.

**Solution 3 — Message queue for serialization**:

Route all booking requests for the same show through a single partition in Kafka. The consumer processes them sequentially, eliminating concurrency entirely for a given show. This trades latency for simplicity.

### Database schema for production

```sql
CREATE TABLE show_seat (
    id          VARCHAR(36) PRIMARY KEY,
    show_id     VARCHAR(36) NOT NULL,
    seat_id     VARCHAR(36) NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    status      ENUM('AVAILABLE','LOCKED','BOOKED') DEFAULT 'AVAILABLE',
    locked_by   VARCHAR(36),
    locked_at   TIMESTAMP,
    UNIQUE KEY uk_show_seat (show_id, seat_id)  -- prevents duplicate entries
);

CREATE TABLE booking (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    show_id     VARCHAR(36) NOT NULL,
    status      ENUM('PENDING','CONFIRMED','CANCELLED','EXPIRED'),
    total_amount DECIMAL(10,2),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Junction table for booking ↔ show_seat (many-to-many)
CREATE TABLE booking_seat (
    booking_id  VARCHAR(36) NOT NULL,
    show_seat_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (booking_id, show_seat_id),
    UNIQUE KEY uk_show_seat_booking (show_seat_id)  -- one booking per show_seat
);
```

The `UNIQUE KEY uk_show_seat_booking` on `show_seat_id` is the **ultimate safety net** — even if application logic has a bug, the database won't allow the same seat to appear in two bookings.

---

## 14. Common Interview Follow-ups

### "How would you handle 10,000 concurrent users for a popular movie release?"

The bottleneck is the lock contention on `show_seat` rows. Solutions:

1. **Shard by show**: Different shows go to different database shards. Users booking for show A and show B never contend.
2. **Redis queue**: Put booking requests into a queue partitioned by show_id. A worker processes them sequentially per show.
3. **Optimistic locking with retry**: Use version numbers. If the update fails (version mismatch), retry with fresh data. Fast under low contention, degrades under high contention.

### "How do you handle partial failures — 2 of 3 seats locked successfully?"

We don't allow partial locks. The `lockSeats` method validates ALL seats in phase 1 before locking ANY in phase 2. If any seat is unavailable, the entire request fails with zero side effects. This is the **all-or-nothing** guarantee.

### "What if the payment gateway is down?"

The user gets a payment failure. They have 3 retries within the 8-minute lock window. If the gateway stays down, after 3 attempts the seats are released. The user can try again when the gateway recovers — they'll get a fresh set of locks.

### "How would you add support for seat selection preferences (e.g., 'best available')?"

Add a `SeatSelectionStrategy` (another Strategy pattern):

```java
interface SeatSelectionStrategy {
    List<ShowSeat> selectSeats(List<ShowSeat> available, int count);
}

class BestAvailableStrategy implements SeatSelectionStrategy {
    // Picks seats closest to the center of the screen
}

class CheapestFirstStrategy implements SeatSelectionStrategy {
    // Picks the cheapest available seats
}
```

### "How would you prevent bots from hoarding tickets?"

1. **Rate limiting**: Max N lock attempts per user per minute.
2. **CAPTCHA**: Before the lock step.
3. **Queue-based fairness**: Put all users in a virtual queue when a popular show opens. Process in order.
4. **Short lock TTL for high-demand shows**: 3 minutes instead of 8.

---

## 15. Complete Code Reference

The full implementation is in the companion `MovieTicketBookingSystem.java` file. Here's a quick map of what's where:

| Class | Responsibility |
|---|---|
| `Movie`, `Seat`, `Screen`, `Theater`, `Show` | Domain entities — rarely change |
| `ShowSeat` | Per-show seat instance with status tracking |
| `Booking`, `Payment` | Transaction entities |
| `SeatStatus`, `BookingStatus`, `PaymentStatus` | State machine enums |
| `PaymentStrategy` + implementations | Strategy Pattern for payment methods |
| `SeatLockProvider` | Atomic seat locking/unlocking (SRP) |
| `BookingService` | Business orchestrator — ties everything together |
| Exception classes | Clear failure communication |

### Demo scenarios covered

1. **Happy path**: User books seats, pays via UPI, gets confirmed.
2. **Conflict**: Another user tries the same seats, gets rejected.
3. **Different seats**: Second user successfully books different seats.
4. **Payment failure**: User fails payment 3 times, seats auto-released.
5. **Recovery**: Released seats are immediately bookable by another user.

---

## Summary — The Mental Model

If you remember one thing from this guide, let it be this:

The entire system revolves around the **ShowSeat state machine** and the **atomic lock acquisition**. Everything else — the entity hierarchy, the patterns, the retry logic — exists to support those two core mechanisms. If you can draw the state machine on a whiteboard and explain the two-phase locking approach, you've demonstrated the core understanding the interviewer is looking for.

The progression to articulate in an interview:

1. Identify the concurrency problem (seat contention).
2. Design the state machine (AVAILABLE → LOCKED → BOOKED).
3. Implement atomic locking (two-phase validate-then-commit).
4. Handle failure recovery (timeout, retries, cleanup).
5. Mention scaling path (DB CAS → Redis → message queue).

Each step shows deeper understanding. Get through all five and you've aced the LLD round.
