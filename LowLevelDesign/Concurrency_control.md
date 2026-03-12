# 🔐 Concurrency Control in Distributed Systems
> A comprehensive guide to managing concurrent data access in distributed environments

---

## 📌 Table of Contents
1. [Problem Statement](#1-problem-statement)
2. [Why Local Locking Fails](#2-why-local-locking-fails)
3. [Database Fundamentals](#3-database-fundamentals)
   - [Transactions (ACID)](#a-transactions)
   - [Database Lock Types](#b-database-lock-types)
4. [Isolation Levels](#4-isolation-levels)
   - [Read Problems](#read-problems-defined)
   - [The Four Isolation Levels](#the-four-isolation-levels)
5. [Distributed Concurrency Techniques](#5-distributed-concurrency-techniques)
   - [Optimistic Concurrency Control](#a-optimistic-concurrency-control-occ)
   - [Pessimistic Concurrency Control](#b-pessimistic-concurrency-control-pcc)
6. [How to Choose](#6-how-to-choose-the-right-strategy)
7. [Quick Reference Cheat Sheet](#7-quick-reference-cheat-sheet)

---

## 1. Problem Statement

When multiple users or processes try to access and modify a **shared resource simultaneously**, conflicts arise.

### 🎬 Classic Example: Movie Seat Booking
```
User A ──┐
          ├──► Same Seat ──► Race Condition ──► Double Booking ❌
User B ──┘
```

**The Goal:** Ensure data integrity and prevent race conditions.

**The Challenge:** In a distributed microservices architecture, traditional single-process solutions simply don't work.

---

## 2. Why Local Locking Fails

Developers often reach for `synchronized` blocks first — but this only works within a **single JVM process**.

```java
// ❌ This does NOT work in distributed systems
public synchronized void bookSeat(int seatId) {
    // Only safe within ONE instance
}
```

### The Distributed Problem

```
User A ──► Instance 1 (synchronized) ──┐
                                        ├──► Same DB Row ──► CONFLICT ❌
User B ──► Instance 2 (synchronized) ──┘

Each instance has its OWN memory → locks are invisible to each other
```

**Bottom line:** Once you scale to multiple service instances, you need database-level concurrency control.

---

## 3. Database Fundamentals

### A. Transactions

A transaction groups multiple DB operations into a **single unit of work**.

**ACID Properties:**

| Property | Meaning |
|---|---|
| **A**tomicity | All operations succeed, or none do |
| **C**onsistency | DB moves from one valid state to another |
| **I**solation | Transactions don't interfere with each other |
| **D**urability | Committed data is permanently saved |

```sql
-- Example: Money Transfer Transaction
BEGIN TRANSACTION;
    UPDATE accounts SET balance = balance - 500 WHERE id = 1;  -- Debit
    UPDATE accounts SET balance = balance + 500 WHERE id = 2;  -- Credit
    -- If anything fails → ROLLBACK → DB stays consistent
COMMIT;
```

> If the credit step fails, the debit is **rolled back** — money is never lost.

---

### B. Database Lock Types

Locks control row/table access between concurrent transactions.

#### 🔵 Shared Lock (S-Lock / Read Lock)
- Multiple transactions can **read simultaneously**
- **No writes** allowed while shared lock is active

```sql
-- Acquiring a shared lock
SELECT * FROM seats WHERE id = 101;  -- Implicit S-Lock in some isolation levels
```

#### 🔴 Exclusive Lock (X-Lock / Write Lock)
- Only **one transaction** can hold it
- **No other reads or writes** allowed until released

```sql
-- Acquiring an exclusive lock
SELECT * FROM seats WHERE id = 101 FOR UPDATE;  -- Explicit X-Lock
```

#### Lock Compatibility Matrix

|  | S-Lock | X-Lock |
|---|---|---|
| **S-Lock** | ✅ Compatible | ❌ Conflict |
| **X-Lock** | ❌ Conflict | ❌ Conflict |

---

## 4. Isolation Levels

Isolation levels balance **data correctness** vs **system concurrency**.

### Read Problems Defined

#### 🟡 Dirty Read
Transaction A reads **uncommitted** data from Transaction B. If B rolls back, A acted on data that never existed.

```
T1: BEGIN
T1: UPDATE seat SET status='booked'   ← not committed yet
T2: SELECT status FROM seat           ← reads 'booked' (DIRTY READ)
T1: ROLLBACK                          ← T2 acted on invalid data ❌
```

#### 🟠 Non-Repeatable Read
Transaction A reads a row **twice** and gets **different values** because B updated it in between.

```
T1: SELECT price FROM product WHERE id=1  → $100
T2: UPDATE product SET price=150 WHERE id=1; COMMIT;
T1: SELECT price FROM product WHERE id=1  → $150 ← DIFFERENT! ❌
```

#### 🔴 Phantom Read
Transaction A re-runs a query and sees **new rows** inserted by Transaction B.

```
T1: SELECT * FROM seats WHERE status='available'  → 5 rows
T2: INSERT INTO seats (status) VALUES ('available'); COMMIT;
T1: SELECT * FROM seats WHERE status='available'  → 6 rows ← PHANTOM! ❌
```

---

### The Four Isolation Levels

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Concurrency | Use Case |
|---|---|---|---|---|---|
| **Read Uncommitted** | ✅ Possible | ✅ Possible | ✅ Possible | 🚀 Highest | Analytics, approximate counts |
| **Read Committed** | ❌ Prevented | ✅ Possible | ✅ Possible | ⚡ High | Most OLTP apps (PostgreSQL default) |
| **Repeatable Read** | ❌ Prevented | ❌ Prevented | ✅ Possible | 🐢 Medium | Financial reads (MySQL InnoDB default) |
| **Serializable** | ❌ Prevented | ❌ Prevented | ❌ Prevented | 🐌 Lowest | Critical financial ops, seat booking |

#### Locking Strategies Per Level

```
Read Uncommitted  → No locks for reads
Read Committed    → S-Lock acquired + released immediately after read
Repeatable Read   → S-Lock held until transaction END
Serializable      → Range locks (prevents phantom rows entirely)
```

---

## 5. Distributed Concurrency Techniques

### A. Optimistic Concurrency Control (OCC)

**Philosophy:** "Conflicts are RARE — don't lock, just verify before saving."

#### How It Works — Version Column

```sql
-- Table structure
CREATE TABLE seats (
    id INT PRIMARY KEY,
    status VARCHAR(20),
    version INT DEFAULT 0    -- ← The key column
);
```

#### Flow Diagram
```
Read row + version (v=1)
        │
   Do your work
        │
   Attempt Update:
   UPDATE seats SET status='booked', version=2
   WHERE id=101 AND version=1   ← Check version!
        │
   ┌────┴────┐
   │         │
Rows=1    Rows=0
(Success) (Version changed → someone else updated → RETRY)
```

#### Java Implementation (Spring/JPA)

```java
@Entity
public class Seat {
    @Id
    private Long id;

    private String status;

    @Version                    // ← JPA handles version automatically
    private Integer version;
}

// Repository
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {}

// Service
@Service
public class BookingService {

    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public boolean bookSeat(Long seatId) {
        try {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found"));

            if (!"available".equals(seat.getStatus())) {
                return false; // Already booked
            }

            seat.setStatus("booked");
            seatRepository.save(seat);  // JPA checks version automatically
            return true;

        } catch (OptimisticLockingFailureException e) {
            // Version mismatch → retry logic here
            System.out.println("Conflict detected, retrying...");
            return false;
        }
    }
}
```

#### Raw JDBC Version

```java
@Transactional
public boolean bookSeatOptimistic(Long seatId, int expectedVersion) {
    String sql = """
        UPDATE seats
        SET status = 'booked', version = version + 1
        WHERE id = ? AND version = ? AND status = 'available'
    """;

    int rowsUpdated = jdbcTemplate.update(sql, seatId, expectedVersion);
    return rowsUpdated == 1;  // 0 means conflict
}
```

**✅ Best For:** High read, low write contention (e.g., product catalog, user profiles)

**❌ Drawback:** Retry storms under high contention — many threads fail and retry simultaneously

---

### B. Pessimistic Concurrency Control (PCC)

**Philosophy:** "Conflicts are LIKELY — lock first, then work."

#### Flow Diagram
```
SELECT ... FOR UPDATE  ← Acquire X-Lock immediately
        │
   Row is LOCKED — other transactions WAIT
        │
   Do your work safely
        │
   UPDATE row
        │
   COMMIT → Lock Released → Other transactions proceed
```

#### Java Implementation (Spring/JPA)

```java
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)   // ← SELECT FOR UPDATE
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);
}

@Service
public class BookingService {

    @Autowired
    private SeatRepository seatRepository;

    @Transactional
    public boolean bookSeat(Long seatId) {
        // Acquires DB-level exclusive lock
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (!"available".equals(seat.getStatus())) {
            return false; // Already booked
        }

        seat.setStatus("booked");
        seatRepository.save(seat);
        return true;
        // Lock released on COMMIT
    }
}
```

#### Raw JDBC Version

```java
@Transactional
public boolean bookSeatPessimistic(Long seatId) {
    // Step 1: Lock the row
    String selectSql = "SELECT * FROM seats WHERE id = ? FOR UPDATE";
    Seat seat = jdbcTemplate.queryForObject(selectSql, seatRowMapper, seatId);

    if (!"available".equals(seat.getStatus())) {
        return false;
    }

    // Step 2: Safe to update (row is locked)
    String updateSql = "UPDATE seats SET status = 'booked' WHERE id = ?";
    jdbcTemplate.update(updateSql, seatId);

    return true;
    // COMMIT → lock auto-released
}
```

**✅ Best For:** High write contention (e.g., seat booking, flash sales, inventory)

**❌ Risk: Deadlocks**

```
T1 locks Seat A → wants Seat B
T2 locks Seat B → wants Seat A
         ↓
    DEADLOCK 🔒💀

Solution: DB detects → aborts one transaction → other proceeds
```

```java
// Always handle deadlock exceptions
try {
    bookSeat(seatId);
} catch (DeadlockLoserDataAccessException e) {
    // Retry the transaction
    retryBookSeat(seatId);
}
```

---

## 6. How to Choose the Right Strategy

```
Is data contention HIGH?
        │
       YES                    NO
        │                      │
Use Pessimistic            Use Optimistic
(SELECT FOR UPDATE)        (@Version column)
        │                      │
Great for:                 Great for:
- Seat booking             - User profiles
- Flash sales              - Product views
- Bank transfers           - Read-heavy APIs
- Inventory mgmt           - Low-traffic writes
```

### Decision Table

| Factor | Optimistic | Pessimistic |
|---|---|---|
| Conflict frequency | Low | High |
| Read/Write ratio | Read-heavy | Write-heavy |
| Throughput | Higher | Lower |
| Deadlock risk | None | Yes (handle it!) |
| Retry complexity | Yes | No |
| DB load | Lower | Higher (locks held) |

---

## 7. Quick Reference Cheat Sheet

```
┌─────────────────────────────────────────────────────────┐
│              CONCURRENCY CONTROL SUMMARY                 │
├─────────────────┬───────────────────────────────────────┤
│ Local Sync      │ ❌ Useless in distributed systems      │
├─────────────────┼───────────────────────────────────────┤
│ Shared Lock     │ Multiple readers, no writers           │
│ Exclusive Lock  │ One writer, no readers or writers      │
├─────────────────┼───────────────────────────────────────┤
│ Read Uncommitted│ Dirty + NR + Phantom reads possible    │
│ Read Committed  │ No dirty reads (PostgreSQL default)    │
│ Repeatable Read │ No dirty/NR reads (MySQL default)      │
│ Serializable    │ No read anomalies (slowest)            │
├─────────────────┼───────────────────────────────────────┤
│ Optimistic Lock │ @Version, check before update, retry  │
│ Pessimistic Lock│ SELECT FOR UPDATE, holds lock, commit │
└─────────────────┴───────────────────────────────────────┘
```

---

## 📚 References

- [Concept & Coding — Concurrency Control in Distributed Systems](https://www.youtube.com/@ConceptandCoding)
- Spring Data JPA — `@Lock`, `@Version` annotations
- PostgreSQL Docs — Explicit Locking
- MySQL InnoDB — Transaction Isolation Levels

---

*Guide based on insights by Shrayansh @ Concept & Coding*
