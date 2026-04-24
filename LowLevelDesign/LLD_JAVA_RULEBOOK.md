# LLD + JAVA INTERVIEW RULEBOOK
## Updated Edition — Includes DB Schema Design + REST API Design

> **HOW TO USE THIS RULEBOOK**
> When given an LLD question, follow every numbered step in order.
> Do not skip any step. Call out each step by name before starting it.
> Use the exact senior-level phrases listed. Show UML inline using text notation.
>
> **NEW STEP ORDER (based on actual Fivetran / product-company interview reports):**
> Step 1 → Clarify → Step 2 → Entities → Step 3 → DB Schema → Step 4 → API Design
> → Step 5 → Class Diagram → Step 6 → Sequence Diagram → Step 7 → Code
> → Step 8 → Concurrency → Step 9 → Patterns → Step 10 → Principles
> → Step 11 → Edge Cases → Step 12 → Logging → Step 13 → Scale

---

## STEP 1 — CLARIFY REQUIREMENTS (2–3 min)

Say: *"Before jumping into implementation, let me clarify both functional and non-functional requirements."*

---

### Part A — Functional Requirements (What the system DOES)

Say: *"First, let me understand the core use cases."*

Ask:
- Who are the **actors**? (User, Admin, Driver, System, Guest)
- What **actions** can each actor perform?
- What is the **primary happy path** end to end?
- Are there **secondary use cases**? (cancel, refund, search, review)
- Any features explicitly **out of scope**?

Write these as: *"The system shall allow [actor] to [action]."*

**Example output:**
```
Functional Requirements:
- User can browse and search items
- User can add items to cart
- User can place an order
- User can cancel an order (only if status = PENDING)
- User can view order history
- Admin can update inventory
- System sends notification on order confirmation
```

---

### Part B — Non-Functional Requirements (How the system BEHAVES)

Say: *"Now let me clarify the quality constraints."*

| Area | Questions to Ask |
|---|---|
| Scale | How many users? Expected RPS? Read-heavy or write-heavy? |
| Concurrency | Can multiple users/threads access the same resource simultaneously? |
| Consistency | Strong or eventual consistency? |
| Latency | Any SLA? Response time target? |
| Availability | 99.9% uptime? Can the system go down briefly? |
| Durability | In-memory OK or must data survive restart? |
| Idempotency | Should the same request twice produce the same result? |
| Extensibility | Will new types / strategies be added later? |
| Failure Handling | What happens on partial failure? Retry? Rollback? |
| Scope | Single JVM or distributed? External DB or in-memory? |

---

### Output of Step 1 — State Aloud Before Moving On

```
"Based on my understanding:

Functional:
  1. User can place, cancel, and view orders
  2. Admin can manage inventory
  3. System notifies user on status change

Non-Functional:
  1. In-memory storage OR persistent DB (I'll clarify which)
  2. Concurrent access — I'll use ConcurrentHashMap + locks
  3. Idempotent order placement — requestId dedup
  4. Payment is extensible — I'll use Strategy pattern
  5. Order has lifecycle — I'll use State pattern

Let me proceed with these assumptions unless you'd like to change anything."
```

> Functional → decides your **entities, methods, and relationships**
> Non-Functional → decides your **data structures, design patterns, and concurrency approach**

---

## STEP 2 — ENTITY IDENTIFICATION (2 min)

Say: *"Let me identify the core entities before modeling the schema or classes."*

### The 4 Entity Archetypes

Every entity falls into one of these four buckets:

```
┌─────────────────────────────────────────────────────────────────────┐
│  ARCHETYPE 1: The ACTOR                                             │
│  Who uses/interacts with the system?                                │
│  Examples: User, Customer, Driver, Admin, Agent                     │
│  Always has: id, name, contact info, role/status                    │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 2: The RESOURCE                                          │
│  What is being managed / allocated / consumed?                      │
│  Examples: ParkingSpot, Seat, Room, Book, Ticket, Connector         │
│  Always has: id, availability status, type/category                 │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 3: The TRANSACTION                                       │
│  What happens when Actor interacts with Resource?                   │
│  Examples: Booking, Payment, SyncJob, Order, AuditEvent             │
│  Always has: id, timestamp, actorRef, resourceRef, status           │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 4: The COORDINATOR / MANAGER                             │
│  What orchestrates the system? (Often a Singleton)                  │
│  Examples: ParkingLot, Library, SyncScheduler, ConnectorRegistry    │
│  Always has: collection of Resources, business rules, state mgmt    │
└─────────────────────────────────────────────────────────────────────┘
```

### The "What Varies?" Question (Pattern Trigger)

```
What might change or vary across cases?

 ↓ The FEE / ALGORITHM varies?         → Strategy Pattern
 ↓ The OBJECT CREATION varies?         → Factory / Abstract Factory
 ↓ The NOTIFICATION mechanism varies?  → Observer Pattern
 ↓ The OBJECT STATE changes?           → State Pattern
 ↓ Only ONE instance should exist?     → Singleton
 ↓ You want to ADD behavior?           → Decorator
 ↓ You need to DECOUPLE subsystems?    → Facade
```

---

## STEP 3 — DATABASE SCHEMA DESIGN (5–7 min)

Say: *"Before writing any code, let me design the DB schema. This grounds the entire system in concrete data. I'll define tables, columns, data types, constraints, and indexes."*

---

### DB Schema Design Principles — Say These Explicitly

**Append-only for events/audit:**
> "Sync events, audit logs, and transaction records are append-only. I never UPDATE these rows — I always INSERT a new event. This preserves history and enables replay."

**Soft deletes for core entities:**
> "I never hard-delete connectors, users, or products. I use `is_deleted = TRUE` + `deleted_at`. Foreign keys from event tables reference the entity — hard delete would break referential integrity."

**Idempotency key on mutations:**
> "Each write operation gets an `idempotency_key`. Duplicate submissions are rejected at the DB layer via UNIQUE constraint — no double-processing."

**Separate checkpoint / cursor table:**
> "I store the cursor/watermark in a separate table, not embedded in the parent entity. This lets me atomically update the checkpoint without touching the main entity row."

**No FK on append-only log tables:**
> "Audit and event tables reference entity IDs as plain VARCHAR — no FK. If the entity is deleted, the log must survive. A FK would either block deletion or orphan the log."

**Index strategy — always call this out:**
> "I index on the most frequent query patterns. For status-based queries: `(connector_id, status)`. For history: `(connector_id, created_at DESC)`. Partial indexes with `WHERE is_deleted = FALSE` keep the index small."

---

### SQL DDL Template

```sql
-- Core entity table template
CREATE TABLE entities (
    id              VARCHAR(36)   PRIMARY KEY,          -- UUID, not auto-increment
    name            VARCHAR(255)  NOT NULL,
    type            VARCHAR(50)   NOT NULL,             -- enum-like: use CHECK constraint
    config          JSONB         NOT NULL DEFAULT '{}',-- flexible attributes, avoid over-normalization
    status          VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(36)   NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,                          -- NULL until soft-deleted
    CONSTRAINT chk_type CHECK (type IN ('TYPE_A', 'TYPE_B', 'TYPE_C'))
);

-- Transaction / event table template (append-only)
CREATE TABLE transactions (
    id                  VARCHAR(36)   PRIMARY KEY,
    entity_id           VARCHAR(36)   NOT NULL REFERENCES entities(id),
    idempotency_key     VARCHAR(255)  NOT NULL,
    status              VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    -- PENDING | RUNNING | SUCCESS | FAILED
    actor_id            VARCHAR(36),                   -- who triggered it (NULL = SYSTEM)
    metadata            JSONB         NOT NULL DEFAULT '{}',
    error_message       TEXT,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_idempotency UNIQUE (idempotency_key)
);

-- Audit log template (append-only, no FK on entity_id)
CREATE TABLE audit_events (
    id              VARCHAR(36)   PRIMARY KEY,
    entity_id       VARCHAR(36)   NOT NULL,             -- plain VARCHAR, not FK
    actor_id        VARCHAR(36),
    actor_type      VARCHAR(20)   NOT NULL DEFAULT 'USER',  -- USER | SYSTEM
    action          VARCHAR(50)   NOT NULL,
    old_value       JSONB,
    new_value       JSONB,
    metadata        JSONB         NOT NULL DEFAULT '{}',
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
    -- NOTE: No updated_at. Append-only — rows never mutated.
);

-- Config / rule table template
CREATE TABLE rules (
    id              VARCHAR(36)   PRIMARY KEY,
    entity_id       VARCHAR(36)   NOT NULL REFERENCES entities(id),
    rule_type       VARCHAR(50)   NOT NULL,
    config          JSONB         NOT NULL DEFAULT '{}',  -- {threshold: 10.0, window: 60}
    severity        VARCHAR(20)   NOT NULL DEFAULT 'ERROR',  -- ERROR | WARNING
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Result / output table template
CREATE TABLE results (
    id              VARCHAR(36)   PRIMARY KEY,
    transaction_id  VARCHAR(36)   NOT NULL REFERENCES transactions(id),
    rule_id         VARCHAR(36)   NOT NULL REFERENCES rules(id),
    entity_id       VARCHAR(36)   NOT NULL,
    passed          BOOLEAN       NOT NULL,
    failure_reason  TEXT,
    sample_ids      JSONB,                              -- array of up to 10 failed IDs
    evaluated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Junction table template (M:N)
CREATE TABLE entity_handlers (
    entity_id       VARCHAR(36)   NOT NULL REFERENCES entities(id),
    handler_id      VARCHAR(36)   NOT NULL REFERENCES handlers(id),
    PRIMARY KEY (entity_id, handler_id)
);

-- Index templates (always add these)
CREATE INDEX idx_transactions_entity_status  ON transactions (entity_id, status);
CREATE INDEX idx_transactions_entity_created ON transactions (entity_id, created_at DESC);
CREATE INDEX idx_entities_status             ON entities (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_audit_entity_time           ON audit_events (entity_id, created_at DESC);
CREATE INDEX idx_audit_actor                 ON audit_events (actor_id, created_at DESC) WHERE actor_id IS NOT NULL;
-- Partial index: only rows matching a condition are indexed — keeps index small
CREATE INDEX idx_rules_active                ON rules (entity_id, rule_type) WHERE is_active = TRUE;
```

---

### DB Schema Checklist (Run Through Before Moving On)

```
□ Every table has: id (UUID), created_at, is it append-only or mutable?
□ Mutable tables: updated_at column present?
□ Entity tables: is_deleted + deleted_at for soft delete?
□ Transaction tables: idempotency_key with UNIQUE constraint?
□ Audit/event tables: no FK on entity_id (must survive entity deletion)?
□ JSONB for flexible/polymorphic configs instead of over-normalizing?
□ Indexes on: (entity_id, status), (entity_id, created_at DESC), partial WHERE clauses?
□ CHECK constraints on enum-like string columns?
□ Separate checkpoint table if cursor/watermark needed?
```

---

### ER Diagram (Text Format — Draw This Inline)

```
entities            1 ──── N   transactions
entities            1 ──── 1   entity_checkpoints    (cursor/watermark)
entities            1 ──── N   rules
transactions        1 ──── N   results
results             N ──── 1   rules
entities            N ──── M   handlers              (via entity_handlers junction)
audit_events        N ──── 1   entities              (VARCHAR, no FK)
```

---

### Senior-Level Phrases — DB Schema

> "I use UUID primary keys instead of auto-increment integers — UUIDs are safe across distributed systems and never reveal record count."

> "I store `source_config` as JSONB because connectors have 400+ types — fully normalizing every field would require 400 tables. JSONB gives flexibility with indexability."

> "The `idempotency_key` UNIQUE constraint is my first line of defense against duplicate writes — before any application-level check even runs."

> "I don't put a FK on `audit_events.entity_id` because audit logs must outlive the entity they describe. A FK would block deletion or cascade-delete the audit trail."

> "I use a partial index `WHERE is_deleted = FALSE` — only active records are in the index, so it stays compact and fast even as the table grows."

---

## STEP 4 — REST API DESIGN (4–5 min)

Say: *"Now let me define the API contract. This is what clients use to interact with the system."*

---

### REST API Design Principles — Say These Explicitly

**RESTful resource naming:**
> "Resources are nouns, not verbs. `/orders/{id}/cancel` not `/cancelOrder`. Sub-resources model ownership: `/connectors/{id}/jobs` not `/getJobsByConnector`."

**Async vs sync response codes:**
> "Sync operations (reads, updates) → 200. Resource creation → 201. Async operations (triggering a job, resync) → 202 Accepted. The client polls for status."

**Cursor-based pagination on all list endpoints:**
> "I use cursor-based pagination, not offset. Offset pagination degrades at scale — if 1000 rows are inserted between page 1 and page 2, offset skips rows. Cursor is stable."

**Idempotency header on mutating POSTs:**
> "POST endpoints that trigger state changes accept an optional `Idempotency-Key` header. Same key within 24h returns the cached response — no duplicate job creation."

**Consistent error envelope:**
> "All errors return a consistent JSON envelope: `{error_code, message, details}`. Clients can programmatically handle error codes without parsing message strings."

---

### HTTP Status Code Reference

| Code | When to Use |
|---|---|
| `200 OK` | Successful read, synchronous update |
| `201 Created` | Resource successfully created |
| `202 Accepted` | Async operation triggered (job, sync, resync) |
| `204 No Content` | Successful delete (idempotent — also return 204 for already-deleted) |
| `400 Bad Request` | Malformed JSON, missing required field |
| `401 Unauthorized` | Missing or invalid auth token |
| `403 Forbidden` | Authenticated but not permitted |
| `404 Not Found` | Resource doesn't exist |
| `409 Conflict` | Duplicate unique resource, operation already running |
| `422 Unprocessable Entity` | Semantically invalid (invalid state transition, bad enum value) |
| `429 Too Many Requests` | Rate limit hit |
| `500 Internal Server Error` | Unexpected failure |

---

### API Endpoint Template

```
HTTP_METHOD /resource-path
[Headers]

Request Body (if applicable):
{
  "field": "value"
}

Response STATUS:
{
  "field": "value"
}

Errors:
  4XX Code    → When this error fires and why
```

---

### Full API Design Example — Order Management

**Create an order**
```
POST /orders
Content-Type: application/json
Idempotency-Key: <client-generated UUID>

Request:
{
  "user_id": "usr-abc123",
  "items": [
    { "product_id": "prod-001", "quantity": 2 },
    { "product_id": "prod-002", "quantity": 1 }
  ],
  "payment_method": "CREDIT_CARD",
  "address_id": "addr-xyz"
}

Response 201 Created:
{
  "id": "ord-001",
  "user_id": "usr-abc123",
  "status": "PENDING",
  "total_amount": 149.99,
  "created_at": "2025-01-15T10:00:00Z"
}

Errors:
  400 Bad Request       → items is empty or malformed
  404 Not Found         → product_id or address_id not found
  409 Conflict          → Idempotency-Key already used (same order, return cached response)
  422 Unprocessable     → product out of stock
```

---

**Get order details**
```
GET /orders/{order_id}

Response 200:
{
  "id": "ord-001",
  "user_id": "usr-abc123",
  "status": "CONFIRMED",
  "items": [
    { "product_id": "prod-001", "name": "Widget A", "quantity": 2, "unit_price": 49.99 }
  ],
  "total_amount": 149.99,
  "payment": { "method": "CREDIT_CARD", "status": "CAPTURED" },
  "created_at": "2025-01-15T10:00:00Z",
  "updated_at": "2025-01-15T10:01:00Z"
}

Errors:
  404 Not Found         → order_id not found or does not belong to caller
```

---

**List orders (cursor-paginated)**
```
GET /users/{user_id}/orders?status=CONFIRMED&limit=20&cursor=<next_cursor>

Response 200:
{
  "items": [ { ...order objects... } ],
  "next_cursor": "eyJpZCI6Im9yZC0wMDEifQ==",  // base64 of last seen id + sort field
  "has_more": true,
  "total_count": 142                            // optional: expensive, skip for large datasets
}

Query params:
  status     = filter by OrderStatus enum value
  limit      = page size, default 20, max 100
  cursor     = opaque string from previous response's next_cursor
```

---

**Cancel an order**
```
POST /orders/{order_id}/cancel
Content-Type: application/json

Request:
{
  "reason": "Changed my mind"   // optional
}

Response 200:
{
  "id": "ord-001",
  "status": "CANCELLED",
  "cancelled_at": "2025-01-15T10:05:00Z"
}

Errors:
  404 Not Found         → order not found
  422 Unprocessable     → order status is not PENDING (can only cancel PENDING orders)
```

---

**Trigger async operation (e.g., reprocess order)**
```
POST /orders/{order_id}/reprocess
Content-Type: application/json
Idempotency-Key: <UUID>

Response 202 Accepted:
{
  "job_id": "job-xyz789",
  "order_id": "ord-001",
  "status": "PENDING",
  "created_at": "2025-01-15T10:05:00Z"
}

Note: Returns 202, not 200. Client polls GET /orders/{id} or GET /jobs/{job_id}.

Errors:
  409 Conflict          → a reprocess job is already RUNNING for this order
```

---

**Delete a resource (soft delete)**
```
DELETE /orders/{order_id}

Response 204 No Content

Note: Idempotent — deleting an already-deleted resource also returns 204.
```

---

### Pagination Design — Cursor vs Offset

```
CURSOR-BASED (preferred for production):

  First request:  GET /orders?limit=20
  Response:       { items: [...], next_cursor: "abc123", has_more: true }
  Next request:   GET /orders?limit=20&cursor=abc123

  How cursor is built:
    cursor = base64(JSON.stringify({ id: lastItem.id, created_at: lastItem.created_at }))

  SQL query:
    WHERE (created_at, id) < (:lastCreatedAt, :lastId)   -- keyset pagination
    ORDER BY created_at DESC, id DESC
    LIMIT :limit + 1  -- fetch one extra to determine has_more

OFFSET-BASED (only for small, stable datasets):

  GET /items?page=2&page_size=20
  SQL: LIMIT 20 OFFSET 40
  Problem: if rows inserted between pages, data shifts — rows skipped or duplicated
```

---

### Error Response Envelope (Consistent Across All Endpoints)

```json
{
  "error": {
    "code": "ORDER_ALREADY_CANCELLED",
    "message": "Cannot cancel an order that is already in CANCELLED state.",
    "details": {
      "order_id": "ord-001",
      "current_status": "CANCELLED"
    },
    "request_id": "req-abc123",
    "timestamp": "2025-01-15T10:05:32Z"
  }
}
```

---

### API Design Checklist (Run Through Before Moving On)

```
□ Resource names are nouns, not verbs?
□ Hierarchy is correct? Sub-resources under parent? (/connectors/{id}/jobs)
□ POST for create, GET for read, PUT/PATCH for update, DELETE for delete?
□ Async operations return 202, not 200?
□ All list endpoints paginated with cursor?
□ Idempotency-Key header on state-mutating POSTs?
□ Soft deletes return 204 (idempotent)?
□ Error responses use consistent envelope with error_code?
□ Status codes correct for each scenario?
□ Request/response payloads defined with field names and types?
```

---

### Senior-Level Phrases — API Design

> "I return 202 for sync triggers because the job runs asynchronously — the client should not block waiting. They poll the job status endpoint."

> "My list endpoints use cursor-based pagination. Offset pagination is broken at scale — rows inserted between requests cause skips. Cursor gives a stable snapshot."

> "I accept an `Idempotency-Key` header on this POST. If the client retries due to a network timeout, the second request gets the same response as the first — no duplicate order created."

> "DELETE is idempotent by design — deleting an already-deleted resource returns 204, not 404. This prevents client retry logic from blowing up."

> "I use 422 Unprocessable Entity for business rule violations — not 400. The request is syntactically valid JSON, but semantically it violates a constraint."

---

## STEP 5 — CLASS DIAGRAM (3–4 min)

Say: *"Now let me model the core entities and their relationships in Java class structure."*

### Class Diagram Notation (Text Format)

```
+----------------------------+
|       ClassName            |  ← Concrete Class
+----------------------------+
| - privateField: Type       |
| # protectedField: Type     |
| + publicField: Type        |
+----------------------------+
| + publicMethod(): ReturnType|
| - privateMethod(): void    |
+----------------------------+

<<interface>>
+----------------------------+
|       InterfaceName        |
+----------------------------+
| + method1(): void          |
| + method2(): String        |
+----------------------------+

<<abstract>>
+----------------------------+
|    AbstractClassName       |
+----------------------------+
| # sharedField: Type        |
+----------------------------+
| + concreteMethod(): void   |
| + abstractMethod(): void   |
+----------------------------+
```

### Relationship Notation

```
Association      A ————————> B        A has a reference to B
Aggregation      A <>———————> B       A has B, B can exist alone
Composition      A <◆>——————> B       A owns B, B cannot exist alone
Inheritance      A ————————|> B       A extends B
Implementation   A - - - - |> B       A implements B
```

### Relationship Decision (30 Second Rule)

```
Shared contract, unrelated classes  ──> Interface
Shared state + behavior, related    ──> Abstract Class
Has-a, independent lifecycle        ──> Aggregation (field reference)
Has-a, dependent lifecycle          ──> Composition (create inside)
Pluggable algorithm                 ──> Strategy Pattern
```

---

## STEP 6 — SEQUENCE DIAGRAM (2–3 min, if flow is complex)

Say: *"Let me trace the primary use case end to end."*

```
Client          Controller                Service                Repository
  |                 |                        |                        |
  |-- POST /orders -->                       |                        |
  |                 |-- validate() ------->  |                        |
  |                 |                        |-- findById() -------> |
  |                 |                        |<-- entity ------------|
  |                 |                        |-- save() -----------> |
  |                 |<-- response ---------- |                        |
  |<-- 201 Created -|                        |                        |

[alt: product out of stock]
  |                 |                        |-- throw OutOfStockException
  |<-- 422 ---------|
[end]
```

---

## STEP 7 — FOLDER STRUCTURE (1 min)

Say: *"Let me lay out the package structure before writing any code."*

```
project-name/
└── src/main/java/com/example/
    ├── model/                  ← Plain entities (fields, getters, setters)
    ├── enums/                  ← Status, Type, Category enums
    ├── interfaces/             ← Capability contracts (Payable, Notifiable)
    ├── service/                ← Core business logic
    ├── strategy/               ← Strategy pattern implementations
    ├── factory/                ← Factory / Abstract Factory classes
    ├── observer/               ← Observer pattern (EventListener, EventPublisher)
    ├── state/                  ← State pattern (lifecycle management)
    ├── repository/             ← In-memory storage (ConcurrentHashMap)
    ├── controller/             ← Entry point, delegates to service
    ├── exception/              ← Custom exception hierarchy
    └── Application.java        ← main(), demo, test scenarios
```

---

## STEP 8 — WRITE CORE CODE (5–8 min)

### Mandatory Comment Style

```java
/*
 * [Pattern]: Strategy Pattern
 * [Why]: Swap pricing algorithms at runtime without modifying OrderService.
 * [Principle]: OCP — Open for extension, closed for modification.
 */
```

### Model Template

```java
public class Order {
    private final String id;          // immutable
    private final String userId;
    private List<Item> items;
    private OrderStatus status;       // mutable — lifecycle changes
    private final LocalDateTime createdAt;
    // Constructor, getters, equals(), hashCode(), toString()
}
```

### Service Template

```java
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final PaymentStrategy paymentStrategy;    // Strategy pattern
    private final Map<String, Order> orderStore;      // ConcurrentHashMap for thread safety

    // Constructor injection — DIP: depend on abstraction
    public OrderService(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
        this.orderStore = new ConcurrentHashMap<>();
    }

    public Order placeOrder(String userId, List<Item> items) {
        validateInput(userId, items);                                    // Step 1: Validate
        Order order = new Order(UUID.randomUUID().toString(), userId, items, OrderStatus.PENDING);
        paymentStrategy.pay(order.getTotalAmount());                     // Step 2: Process
        orderStore.put(order.getId(), order);                           // Step 3: Persist
        log.info("Order placed | orderId={} userId={}", order.getId(), userId);
        return order;
    }
}
```

### Custom Exception Hierarchy

```java
public class AppException extends RuntimeException {
    private final String errorCode;
    public AppException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
public class ItemNotFoundException extends AppException {
    public ItemNotFoundException(String itemId) {
        super("ITEM_NOT_FOUND", "Item not found: " + itemId);
    }
}
public class InvalidStateException extends AppException {
    public InvalidStateException(String currentState, String operation) {
        super("INVALID_STATE", "Cannot perform " + operation + " in state: " + currentState);
    }
}
```

---

## STEP 9 — CONCURRENCY (CRITICAL)

Say: *"Since multiple requests can hit this simultaneously, let me ensure thread safety."*

### Concurrency Toolkit — When to Use What

| Tool | When to Use |
|---|---|
| `ConcurrentHashMap` | Thread-safe map, segment-level locking |
| `synchronized` block | Simple mutual exclusion on critical section |
| `ReentrantLock` | Need tryLock, fairness, or lock across methods |
| `AtomicInteger / AtomicLong` | Counter increments without synchronized |
| `AtomicReference<T>` | CAS on a single object reference |
| `volatile` | Single variable visibility across threads |
| `ReadWriteLock` | Read-heavy: multiple concurrent readers, exclusive writer |
| `Semaphore` | Limit concurrent access (max N concurrent bookings) |
| `BlockingQueue` | Producer-consumer pattern |
| `CopyOnWriteArrayList` | List that is iterated frequently, written rarely |

### Code Patterns

```java
// Idempotency check
private final Set<String> processedIds = ConcurrentHashMap.newKeySet();
public void process(String requestId) {
    if (!processedIds.add(requestId)) return;  // duplicate — skip
    // actual processing
}

// ReentrantLock with fairness (prevents starvation)
private final ReentrantLock lock = new ReentrantLock(true);  // fair=true
public void criticalSection() {
    lock.lock();
    try { /* ... */ } finally { lock.unlock(); }
}

// AtomicInteger CAS
AtomicInteger count = new AtomicInteger(100);
boolean reserved = count.compareAndSet(currentVal, currentVal - 1);

// ReadWriteLock (autocomplete, search history)
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
public List<Item> read() {
    rwLock.readLock().lock();
    try { return new ArrayList<>(items); } finally { rwLock.readLock().unlock(); }
}
public void write(Item item) {
    rwLock.writeLock().lock();
    try { items.add(item); } finally { rwLock.writeLock().unlock(); }
}
```

### Senior-Level Phrases — Concurrency

> "I use ConcurrentHashMap instead of HashMap because multiple threads will read and write simultaneously — segment-level locking without blocking the entire map."

> "I use AtomicInteger with CAS here to avoid a synchronized block on the counter — lock-free and more performant under high contention."

> "I use ReadWriteLock because this is read-heavy — autocomplete is called on every keystroke, writes happen only on explicit search. Concurrent reads don't block each other."

---

## STEP 10 — DESIGN PATTERNS (CALL OUT BY NAME)

| Pattern | Use When | Senior Phrase |
|---|---|---|
| **Strategy** | Multiple algorithms, interchangeable at runtime | "I use Strategy so I can swap logic without touching the service class." |
| **Factory** | Object creation logic is complex or varies | "Factory centralizes creation and hides `new` from callers." |
| **Builder** | Object has many optional fields | "Builder prevents telescoping constructors." |
| **Singleton** | Global config, registry, thread pool | "One instance, globally accessible — initialized once." |
| **Observer** | Event notifications to multiple subscribers | "Observer decouples event source from handlers." |
| **State** | Entity has lifecycle (PENDING → CONFIRMED → etc.) | "State pattern externalizes lifecycle — each state knows what's allowed." |
| **Decorator** | Add behavior without modifying class | "Decorator wraps the base and adds cross-cutting concerns." |
| **Template Method** | Algorithm skeleton, steps vary | "Template Method defines the flow; subclasses plug in specifics." |

### State Pattern Template

```java
public interface OrderState {
    void confirm(OrderContext ctx);
    void ship(OrderContext ctx);
    void cancel(OrderContext ctx);
}
public class PendingState implements OrderState {
    public void confirm(OrderContext ctx) { ctx.setState(new ConfirmedState()); }
    public void ship(OrderContext ctx)    { throw new InvalidStateException("PENDING", "ship"); }
    public void cancel(OrderContext ctx)  { ctx.setState(new CancelledState()); }
}
public class OrderContext {
    private OrderState state = new PendingState();
    public void setState(OrderState s) { this.state = s; }
    public void confirm() { state.confirm(this); }
    public void cancel()  { state.cancel(this); }
}
```

---

## STEP 11 — DESIGN PRINCIPLES (MENTION AT LEAST 2)

Say: *"Let me call out the SOLID principles I'm applying."*

| Principle | What to Say |
|---|---|
| **SRP** | "Each class has one reason to change — OrderService only handles order logic." |
| **OCP** | "I can add a new payment type by implementing PaymentStrategy — no existing code changes." |
| **LSP** | "Any subclass of Notification can replace the base without breaking behavior." |
| **ISP** | "I split the interface into Payable and Refundable — callers depend only on what they use." |
| **DIP** | "OrderService depends on PaymentStrategy interface, not CreditCardPayment." |
| **Composition over Inheritance** | "I compose OrderService with PaymentStrategy rather than extending a base payment class." |
| **Fail Fast** | "I validate at entry points so invalid state never propagates deep." |

---

## STEP 12 — EDGE CASE CHECKLIST

Say: *"Let me quickly validate the failure scenarios."*

```
□ Null inputs — null userId, null item list?
□ Empty collections — empty cart, zero items?
□ Negative / zero values — negative price, zero quantity?
□ Duplicate requests — same order placed twice? (idempotency key)
□ Concurrent access — two threads booking same seat?
□ Invalid state transition — shipping a cancelled order?
□ Max limits exceeded — cart limit, rate limit, booking cap?
□ External service failure — payment gateway down?
□ Partial failure — one item fails, what happens to rest?
□ Resource exhaustion — out of stock, no available rooms?
□ Idempotent deletes — deleting already-deleted resource?
□ Pagination edge — cursor points to deleted row?
□ JSONB query — querying inside JSONB config field (need GIN index)?
```

---

## STEP 13 — LOGGING (Add After Core Code)

Say: *"I'll add structured logging — no sensitive data, all log lines include contextual IDs."*

```java
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

public Order placeOrder(String userId, List<Item> items) {
    String requestId = UUID.randomUUID().toString();
    log.info("PlaceOrder started | requestId={} userId={} itemCount={}", requestId, userId, items.size());
    try {
        // ... logic ...
        log.info("PlaceOrder success | requestId={} orderId={}", requestId, order.getId());
        return order;
    } catch (AppException e) {
        log.error("PlaceOrder failed | requestId={} errorCode={} reason={}", requestId, e.getErrorCode(), e.getMessage());
        throw e;
    }
    // NEVER: log.info("Card: " + card.getNumber());
}
```

---

## STEP 14 — SCALING DISCUSSION (If Asked)

Say: *"If this needs to scale beyond a single JVM..."*

```
Client Request
    │
    ▼
[API Gateway]          ← Auth, Rate Limiting, SSL termination
    │
    ▼
[Load Balancer]        ← Round-robin / Least connections
    │
    ▼
[Service Layer]        ← Stateless, horizontally scalable
    │
    ├──> [Cache Layer]     ← Redis — hot data, session, idempotency keys, cursor checkpoints
    │
    ├──> [Database]        ← Sharded / replicated — writes to primary, reads from replica
    │
    └──> [Message Queue]   ← Kafka — async processing (notifications, analytics, audit)
              │
              ▼
         [Workers]         ← Consumer groups, independent scaling
```

**Production upgrades to mention for each component:**
- `CheckpointStore` → Redis (survives restarts, shared across JVMs)
- `ConnectorRegistry` → etcd or Consul (distributed coordination)
- `ConcurrentHashMap` (in-memory) → PostgreSQL with row-level locking
- `ExecutorService` (local) → Kafka topic + consumer group (distributed job queue)
- `in-memory idempotency set` → Redis SET with TTL (distributed dedup)

---

## QUICK-REFERENCE CHEAT SHEET

### Step Order at a Glance

```
1.  Clarify Requirements          → functional + non-functional
2.  Entity Identification         → 4 archetypes + "what varies?"
3.  DB Schema Design              → SQL DDL, indexes, constraints    ← NEW
4.  REST API Design               → endpoints, status codes, pagination ← NEW
5.  Class Diagram                 → UML text notation
6.  Sequence Diagram              → primary use case flow
7.  Folder Structure              → package layout
8.  Core Code                     → entities, services, patterns
9.  Concurrency                   → thread safety decisions
10. Design Patterns               → named patterns with rationale
11. Design Principles             → SOLID + composition
12. Edge Cases                    → failure scenario checklist
13. Logging                       → structured, no sensitive data
14. Scaling                       → beyond single JVM
```

### Opening Lines

| Moment | What to Say |
|---|---|
| Start | "Before implementation, let me clarify requirements — then I'll design the DB schema, then the API, then the Java classes." |
| DB Schema | "I'll start with append-only for event tables, soft deletes for entities, and idempotency keys on writes." |
| API Design | "I'll use RESTful nouns, 202 for async operations, cursor pagination on all list endpoints." |
| Entities | "Let me model the core entities and their relationships." |
| Concurrency | "Since multiple requests can hit this simultaneously, I'll use ConcurrentHashMap and AtomicLong for thread safety." |
| Review | "Let me quickly validate the edge cases before finishing." |
| Scale | "If this needs to go beyond single JVM, I'd move checkpoints to Redis, job queue to Kafka, and registry to etcd." |

### Concurrency Decision (30 Second Rule)

```
Thread-safe map               ──> ConcurrentHashMap
Simple counter                ──> AtomicInteger
Lock with tryLock / fairness  ──> ReentrantLock
Read-heavy workload           ──> ReadWriteLock
Limit parallel access         ──> Semaphore
Producer-consumer             ──> BlockingQueue
Prevent duplicate processing  ──> Idempotency key in ConcurrentHashMap.newKeySet()
Iteration-heavy list          ──> CopyOnWriteArrayList
```

### API Status Code Decision (30 Second Rule)

```
Successful read / sync update   ──> 200 OK
Resource created                ──> 201 Created
Async job triggered             ──> 202 Accepted
Successful delete               ──> 204 No Content (idempotent)
Malformed request               ──> 400 Bad Request
Auth missing / invalid          ──> 401 Unauthorized
Not permitted                   ──> 403 Forbidden
Resource not found              ──> 404 Not Found
Already exists / job running    ──> 409 Conflict
Business rule violation         ──> 422 Unprocessable Entity
Rate limit hit                  ──> 429 Too Many Requests
```

### DB Design Decision (30 Second Rule)

```
Mutable entity                  ──> updated_at + is_deleted + deleted_at
Append-only event / log         ──> no updated_at, no FK on entity_id
Transaction / job               ──> idempotency_key UNIQUE constraint
Flexible polymorphic config     ──> JSONB column
Cursor / watermark              ──> separate _checkpoints table
Most frequent query pattern     ──> (entity_id, status) index
History queries                 ──> (entity_id, created_at DESC) index
Large table, filter by status   ──> partial index WHERE is_deleted = FALSE
```

---

*End of Rulebook. Run every step. Call out every step name.*
*Total steps: 14 | New additions: DB Schema (Step 3), REST API Design (Step 4)*
