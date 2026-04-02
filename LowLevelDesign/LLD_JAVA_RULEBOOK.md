# LLD + JAVA INTERVIEW RULEBOOK


> **HOW TO USE THIS RULEBOOK**
> When given an LLD question, follow every numbered step in order.
> Do not skip any step. Call out each step by name before starting it.
> Use the exact senior-level phrases listed. Show UML inline using text notation.

---

## STEP 1 — CLARIFY REQUIREMENTS (2–3 min)

Say: *"Before jumping into implementation, let me clarify both functional and non-functional requirements."*

---

### 🟢 Part A — Functional Requirements (What the system DOES)

Say: *"First, let me understand the core use cases."*

Ask:
- Who are the **actors**? (User, Admin, Driver, System, Guest)
- What **actions** can each actor perform?
- What is the **primary happy path** end to end?
- Are there **secondary use cases**? (cancel, refund, search, review)
- Any features that are explicitly **out of scope**?

> Write these as: *"The system shall allow [actor] to [action]."*

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

### 🔴 Part B — Non-Functional Requirements (How the system BEHAVES)

Say: *"Now let me clarify the quality constraints."*

| Area | Questions to Ask |
|---|---|
| Scale | How many users? Expected RPS? Read-heavy or write-heavy? |
| Concurrency | Can multiple users/threads access the same resource simultaneously? |
| Consistency | Strong or eventual consistency? (e.g., seat booking must be strongly consistent) |
| Latency | Any SLA? Response time target? (e.g., < 200ms) |
| Availability | 99.9% uptime? Can the system go down briefly? |
| Durability | In-memory OK or must data survive restart? |
| Idempotency | Should the same request twice produce the same result? |
| Extensibility | Will new types / strategies be added later? (drives pattern choice) |
| Failure Handling | What happens on partial failure? Retry? Rollback? Compensate? |
| Scope | Single JVM or distributed? External DB or in-memory? |

**Example output:**
```
Non-Functional Requirements:
- In-memory storage (single JVM, no external DB)
- Concurrent access expected — thread safety required
- Order placement must be idempotent (same requestId = same result)
- Payment strategy must be extensible (Credit, UPI, Wallet)
- Cancellation only allowed in PENDING state (state validation required)
```

---

### ✅ Output of Step 1 — State These Aloud Before Moving On

```
"Based on my understanding:

Functional:
  1. User can place, cancel, and view orders
  2. Admin can manage inventory
  3. System notifies user on status change

Non-Functional:
  1. In-memory, single JVM
  2. Concurrent access — I'll use ConcurrentHashMap + locks
  3. Idempotent order placement — requestId dedup
  4. Payment is extensible — I'll use Strategy pattern
  5. Order has lifecycle — I'll use State pattern

Let me proceed with these assumptions unless you'd like to change anything."
```

> **Why this matters:**
> Functional → decides your **entities, methods, and relationships**
> Non-Functional → decides your **data structures, design patterns, and concurrency approach**

---
## 2.0
## 1. The Entity Identification Framework

This is the **core skill** you're struggling with. Here's a repeatable algorithm.

### Step 1 — Noun Extraction Filter

Read the problem statement and underline every noun. Then apply this 3-question filter to each:

| Question | If YES → | If NO → |
|---|---|---|
| Does it have its own **identity** (unique ID)? | Strong Entity candidate | Might be a value object / field |
| Does it have **state that changes** over time? | Definite Entity | Might be just an enum / constant |
| Does it have **behavior** (does things or things happen to it)? | Needs its own class | Might be a field on another entity |

### Step 2 — The 4 Entity Archetypes

Every entity in any LLD problem falls into one of these four buckets:

```
┌─────────────────────────────────────────────────────────────────────┐
│  ARCHETYPE 1: The ACTOR                                             │
│  Who uses/interacts with the system?                                │
│  Examples: User, Customer, Driver, Admin, Agent                     │
│  Always has: id, name, contact info, role/status                    │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 2: The RESOURCE                                          │
│  What is being managed / allocated / consumed?                      │
│  Examples: ParkingSpot, Seat, Room, Book, Ticket                    │
│  Always has: id, availability status, type/category                 │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 3: The TRANSACTION                                       │
│  What happens when Actor interacts with Resource?                   │
│  Examples: Booking, Payment, Ticket, Ride, Order                    │
│  Always has: id, timestamp, actorRef, resourceRef, status           │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 4: The COORDINATOR / MANAGER                             │
│  What orchestrates the system? (Often a Singleton)                  │
│  Examples: ParkingLot, Library, VendingMachine, ElevatorController  │
│  Always has: collection of Resources, business rules, state mgmt    │
└─────────────────────────────────────────────────────────────────────┘
```

### Step 3 — Relationship Identification

After finding entities, ask these 3 questions for every pair:

1. **Has-A or Is-A?**
   - `ParkingLot` has `ParkingFloor`s → Composition
   - `Car` is-a `Vehicle` → Inheritance
   - `Booking` has-a reference to `User` → Association

2. **Multiplicity?**
   - One ParkingLot → Many ParkingFloors (1:N)
   - One User → Many Bookings (1:N)
   - One Show → Many Seats (1:N)
   - One Booking → One Seat (1:1)

3. **Lifecycle dependency?**
   - If parent dies, does child die? → **Composition** (strong)
   - Child can exist independently? → **Aggregation** (weak)

### Step 4 — The "What Varies?" Question

This is how you identify where design patterns are needed:

```
Ask: "What might change in future or vary across cases?"

 ↓ The FEE CALCULATION varies? → Strategy Pattern
 ↓ The OBJECT CREATION varies? → Factory / Abstract Factory
 ↓ The NOTIFICATION mechanism varies? → Observer Pattern
 ↓ The OBJECT STATE changes significantly? → State Pattern
 ↓ Only ONE instance should exist? → Singleton
 ↓ You want to ADD behavior dynamically? → Decorator
 ↓ You need to DECOUPLE complex subsystems? → Facade
```

---
## 2.1

## STEP 2 — DRAW CLASS DIAGRAM (3–4 min)

Say: *"Let me model the core entities and their relationships."*

### Class Diagram Notation (Text Format — Use in Interview)

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
| + staticMethod(): Type     |
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
| + abstractMethod(): void   |  ← italicize or mark (abstract)
+----------------------------+
```

### Relationship Notation (Draw on Whiteboard / Write in Text)

```
Association      A ————————> B        A has a reference to B
Dependency       A - - - - > B        A uses B temporarily
Aggregation      A <>———————> B       A has B, B can exist alone   (hollow diamond)
Composition      A <◆>——————> B       A owns B, B cannot exist alone (filled diamond)
Inheritance      A ————————|> B       A extends B                  (hollow arrow)
Implementation   A - - - - |> B       A implements B               (dashed hollow arrow)
```

### What to Include in Every Class Diagram

For EVERY class/interface/abstract class, write:
- Fields with type and visibility (`+`, `-`, `#`)
- Methods with parameters and return types
- Stereotypes: `<<interface>>`, `<<abstract>>`, `<<enum>>`, `<<singleton>>`
- Static members: underline or mark `static`

---

## STEP 3 — DRAW SEQUENCE DIAGRAM (2–3 min, if asked or if flow is complex)

Say: *"Let me trace the flow for the primary use case."*

### Sequence Diagram Notation (Text Format)

```
Client          Controller                Service                Repository            External
  |                 |                        |                        |                    |
  |-- POST /api/v1/kyc ------------------->|                        |                    |
  |   (Create KYC Request)                |                        |                    |
  |                 |-- validate() ------>|                        |                    |
  |                 |                     |-- GET /kyc/{id} ------>|                    |
  |                 |                     |                        |-- query() -------->|
  |                 |                     |                        |<-- result ---------|
  |                 |                     |<-- entity -------------|                    |
  |                 |                     |-- GET /external/user/{id} ----------------->|
  |                 |                     |<---------------------- external data -------|
  |                 |                     |-- process()            |                    |
  |                 |                     |-- PUT /kyc/{id} ------>|                    |
  |                 |                     |                        |-- save() ----------|
  |                 |<-- response --------|                        |                    |
  |<-- 201 Created --|                    |                        |                    |
```

### Rules for Sequence Diagram

- Left to right: Client → Controller → Service → Repository → External
- Show return arrows (dashed or labeled)
- Show alternate flows with `[alt]` / `[opt]` blocks:

```
[alt: item found]
  Service ——> Repository: findById()
  Repository ——> Service: Item
[else: item not found]
  Service ——> Client: throw ItemNotFoundException
[end]
```

- Show loops:
```
[loop: for each item in cart]
  Service ——> PricingService: calculatePrice(item)
[end]
```

---

## STEP 4 — DEFINE RELATIONSHIPS (2 min)

Say: *"Let me define the relationships before writing code."*

### Decision Tree — What to Use When

```
Need shared CONTRACT across unrelated classes?
  └──> INTERFACE
       Example: Payable, Notifiable, Searchable

Need shared STATE + BEHAVIOR with variation?
  └──> ABSTRACT CLASS
       Example: Vehicle, Employee, Shape

Need to REUSE behavior without IS-A relationship?
  └──> COMPOSITION (has-a)
       Example: OrderService has-a PaymentGateway

Need IS-A with full override?
  └──> INHERITANCE (extends)
       Example: CreditCard extends Card

Need pluggable ALGORITHMS?
  └──> STRATEGY PATTERN (interface + implementations)

Need to create objects without specifying class?
  └──> FACTORY PATTERN
```

### Senior-Level Phrases — Relationships

> "I use an **interface** here because multiple unrelated classes need this capability, allowing plug-and-play implementations."

> "I use an **abstract class** because these entities share common state and behavior — the base handles the what, subclasses handle the how."

> "I prefer **composition over inheritance** here to avoid tight coupling. If I inherit, I'm locked into a hierarchy. Composition gives me flexibility."

> "This is a **has-a** relationship, not an **is-a**, so I compose rather than extend."

---

## STEP 5 — FOLDER STRUCTURE (1 min, say this first before writing code)

Say: *"Let me lay out the package structure before writing any code."*

```
project-name/
└── src/main/java/com/example/
    ├── model/                  ← Plain entities (fields, getters, setters, equals, hashCode)
    ├── enums/                  ← Status, Type, Category enums
    ├── interfaces/             ← Capability contracts (Payable, Notifiable, etc.)
    ├── service/                ← Core business logic
    ├── strategy/               ← Strategy pattern implementations
    ├── factory/                ← Factory / Abstract Factory classes
    ├── observer/               ← Observer pattern (EventListener, EventPublisher)
    ├── state/                  ← State pattern (if lifecycle management needed)
    ├── repository/             ← In-memory storage (HashMap / ConcurrentHashMap)
    ├── controller/             ← Entry point, delegates to service
    ├── exception/              ← Custom exception hierarchy
    └── Application.java        ← main(), demo, test scenarios
```

---

## STEP 6 — WRITE CORE CODE (5–8 min)

### Code Comment Style — MANDATORY

Every non-trivial class/method needs a comment block:

```java
/*
 * [Pattern/Principle]: Strategy Pattern
 * [Why]: So we can swap pricing algorithms at runtime
 *        without modifying OrderService.
 * [Principle]: OCP — Open for extension, closed for modification.
 */
```

### File Template — Every File Must Have

```java
package com.example.service;

/**
 * What it does: Processes orders end to end.
 * Why it exists: Central coordination point — keeps Controller thin.
 * Pattern: Follows SRP — only order processing logic here.
 */
public class OrderService {

    // Fields — explain why each exists
    private final PaymentStrategy paymentStrategy;   // Strategy pattern — pluggable payment
    private final Map<String, Order> orderStore;     // ConcurrentHashMap for thread safety

    // Constructor injection — DIP: depend on abstraction, not concrete class
    public OrderService(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
        this.orderStore = new ConcurrentHashMap<>();
    }

    public Order placeOrder(String userId, List<Item> items) {
        // Step 1: Validate inputs
        validateInput(userId, items);

        // Step 2: Build order (Builder pattern if complex)
        Order order = new Order(UUID.randomUUID().toString(), userId, items, OrderStatus.PENDING);

        // Step 3: Process payment
        paymentStrategy.pay(order.getTotalAmount());

        // Step 4: Persist
        orderStore.put(order.getId(), order);

        return order;
    }
}
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

### Enum Template

```java
public enum OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}
```

### Interface Template

```java
/*
 * Capability contract — any class that can process payments implements this.
 * Allows swapping Credit, UPI, Wallet without touching OrderService.
 */
public interface PaymentStrategy {
    void pay(double amount);
    String getPaymentMode();
}
```

### Abstract Class Template

```java
/*
 * Abstract class — all notification channels share senderId + timestamp logic.
 * Subclasses only implement the channel-specific send().
 */
public abstract class Notification {
    protected final String senderId;
    protected final LocalDateTime timestamp;

    protected Notification(String senderId) {
        this.senderId = senderId;
        this.timestamp = LocalDateTime.now();
    }

    // Common behavior
    public void log() {
        System.out.println("[" + timestamp + "] Notification from " + senderId);
    }

    // Subclasses must implement
    public abstract void send(String message);
}
```

### Custom Exception Hierarchy Template

```java
// Base exception — callers can catch at right granularity
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

public class InsufficientBalanceException extends AppException {
    public InsufficientBalanceException(double required, double available) {
        super("INSUFFICIENT_BALANCE",
              "Required: " + required + ", Available: " + available);
    }
}

public class InvalidStateException extends AppException {
    public InvalidStateException(String currentState, String operation) {
        super("INVALID_STATE",
              "Cannot perform " + operation + " in state: " + currentState);
    }
}
```

---

## STEP 7 — CONCURRENCY (CRITICAL FOR ADOBE / KOTAK)

Say: *"Since multiple requests can hit this simultaneously, let me ensure thread safety."*

### Concurrency Toolkit — When to Use What

| Tool | When to Use |
|---|---|
| `ConcurrentHashMap` | Thread-safe map, no full lock needed |
| `synchronized` block | Simple mutual exclusion on a small critical section |
| `ReentrantLock` | Need tryLock, fairness, or lock across methods |
| `AtomicInteger` / `AtomicLong` | Counter increments without synchronized |
| `AtomicReference<T>` | CAS on a single object reference |
| `volatile` | Single variable visibility across threads |
| `ReadWriteLock` | Read-heavy workload — multiple readers, exclusive writer |
| `Semaphore` | Limit concurrent access (e.g., max 10 concurrent bookings) |
| `BlockingQueue` | Producer-consumer pattern |

### Code Patterns

```java
// CAS with AtomicInteger
AtomicInteger seatCount = new AtomicInteger(100);
boolean booked = seatCount.compareAndSet(currentVal, currentVal - 1);

// Synchronized critical section
public synchronized boolean bookSeat(String seatId) {
    if (!availableSeats.contains(seatId)) throw new SeatUnavailableException(seatId);
    availableSeats.remove(seatId);
    bookedSeats.put(seatId, userId);
    return true;
}

// ReentrantLock for fine-grained control
private final ReentrantLock lock = new ReentrantLock();

public void transfer(Account from, Account to, double amount) {
    lock.lock();
    try {
        from.debit(amount);
        to.credit(amount);
    } finally {
        lock.unlock();   // always in finally
    }
}

// Idempotency check
private final Set<String> processedRequestIds = ConcurrentHashMap.newKeySet();

public void process(String requestId, Order order) {
    if (!processedRequestIds.add(requestId)) {
        return;  // duplicate — already processed
    }
    // actual processing
}
```

### Senior-Level Phrase — Concurrency

> "I use ConcurrentHashMap instead of HashMap because multiple threads will read and write simultaneously — ConcurrentHashMap gives us segment-level locking without blocking the entire map."

> "I use AtomicInteger with CAS here to avoid a synchronized block on the counter — it's lock-free and more performant under high contention."

> "I add an idempotency check using a ConcurrentHashMap key set — if the same requestId comes twice, we return early without double-processing."

---

## STEP 8 — DESIGN PATTERNS (USE WHEN NEEDED)

Call out the pattern BY NAME and explain WHY.

| Pattern | Use When | Senior Phrase |
|---|---|---|
| **Strategy** | Multiple algorithms, interchangeable at runtime | "I use Strategy so I can swap logic without touching the service class." |
| **Factory** | Object creation logic is complex or varies | "I use Factory to centralize creation and hide the `new` keyword from callers." |
| **Builder** | Object has many optional fields | "Builder pattern prevents telescoping constructors and makes construction readable." |
| **Singleton** | Global config, registry, thread pool | "I use Singleton for the config manager — one instance, globally accessible." |
| **Observer** | Event notifications to multiple subscribers | "Observer decouples the event source from handlers — add/remove listeners without touching core logic." |
| **State** | Entity has lifecycle (PENDING → CONFIRMED → etc.) | "State pattern externalizes lifecycle transitions — each state knows what's allowed." |
| **Decorator** | Add behavior without modifying class | "Decorator wraps the base service and adds cross-cutting concerns like logging or rate-limiting." |
| **Template Method** | Algorithm skeleton, steps vary | "Template Method defines the flow in the abstract class — subclasses plug in the specifics." |

### State Pattern Template (Common in LLD)

```java
public interface OrderState {
    void confirm(OrderContext ctx);
    void ship(OrderContext ctx);
    void cancel(OrderContext ctx);
}

public class PendingState implements OrderState {
    public void confirm(OrderContext ctx) {
        ctx.setState(new ConfirmedState());
    }
    public void ship(OrderContext ctx) {
        throw new InvalidStateException("PENDING", "ship");
    }
    public void cancel(OrderContext ctx) {
        ctx.setState(new CancelledState());
    }
}

public class OrderContext {
    private OrderState currentState = new PendingState();

    public void setState(OrderState state) { this.currentState = state; }
    public void confirm() { currentState.confirm(this); }
    public void ship()    { currentState.ship(this); }
    public void cancel()  { currentState.cancel(this); }
}
```

---

## STEP 9 — DESIGN PRINCIPLES (ALWAYS MENTION AT LEAST 2)

Say: *"Let me call out the SOLID principles I'm applying."*

| Principle | What to Say |
|---|---|
| **SRP** | "Each class has one reason to change — OrderService only handles order logic, PaymentService only handles payment." |
| **OCP** | "I can add a new payment type by implementing PaymentStrategy — no existing code changes." |
| **LSP** | "Any subclass of Notification can replace the base without breaking behavior." |
| **ISP** | "I split the interface into Payable and Refundable — callers depend only on what they use." |
| **DIP** | "OrderService depends on PaymentStrategy interface, not CreditCardPayment — high-level modules don't depend on low-level." |
| **Composition over Inheritance** | "I compose OrderService with a PaymentStrategy rather than extending a base payment class." |
| **Encapsulation** | "All fields are private — state changes only through validated methods." |
| **Fail Fast** | "I validate at entry points so invalid state never propagates deep into the system." |

---

## STEP 10 — EDGE CASE CHECKLIST (Before Finishing)

Say: *"Let me quickly validate the failure scenarios."*

Go through this list out loud:

```
□ Null inputs — null userId, null item list?
□ Empty collections — empty cart, zero items?
□ Negative / zero values — negative price, zero quantity?
□ Duplicate requests — same order placed twice?
□ Concurrent access — two threads booking same seat?
□ Invalid state transition — shipping a cancelled order?
□ Max limits exceeded — cart limit, rate limit, booking cap?
□ External service failure — payment gateway down?
□ Partial failure — one item fails, what happens to rest?
□ Resource exhaustion — out of stock, no available rooms?
```

---

## STEP 11 — LOGGING (Add After Core Code)

Say: *"I'll add structured logging — no sensitive data."*

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order placeOrder(String userId, List<Item> items) {
        String requestId = UUID.randomUUID().toString();
        log.info("PlaceOrder started | requestId={} userId={} itemCount={}",
                 requestId, userId, items.size());

        try {
            // ... logic ...
            log.info("PlaceOrder success | requestId={} orderId={}", requestId, order.getId());
            return order;
        } catch (AppException e) {
            log.error("PlaceOrder failed | requestId={} errorCode={} reason={}",
                      requestId, e.getErrorCode(), e.getMessage());
            throw e;
        }
        // NO: log.info("Card number: " + card.getNumber());  ← never log sensitive data
    }
}
```

---

## STEP 12 — SCALING DISCUSSION (If Asked)

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
    ├──> [Cache Layer]     ← Redis — hot data, session, idempotency keys
    │
    ├──> [Database]        ← Sharded / replicated — writes to primary, reads from replica
    │
    └──> [Message Queue]   ← Kafka — async processing (notifications, analytics, audit)
              │
              ▼
         [Workers]         ← Consumer groups, independent scaling
```

---

## QUICK-REFERENCE CHEAT SHEET

### Opening Lines

| Moment | What to Say |
|---|---|
| Start | "Before implementation, let me clarify a few requirements." |
| Entities | "Let me identify core entities and model them." |
| Relationships | "Let me define relationships before writing any code." |
| Concurrency | "Since multiple requests can hit this simultaneously, I'll ensure thread safety using..." |
| Review | "Let me quickly validate the edge cases." |
| Refactor | "I'll start with a simple design and refactor using patterns if needed." |
| Scale | "If this needs to go beyond single JVM, I'd add caching and a queue layer." |

### Relationship Decision (30 Second Rule)

```
Shared contract, unrelated classes  ──> Interface
Shared state + behavior, related    ──> Abstract Class
Has-a, independent lifecycle        ──> Aggregation (field reference)
Has-a, dependent lifecycle          ──> Composition (create inside)
Pluggable algorithm                 ──> Strategy pattern
```

### Concurrency Decision (30 Second Rule)

```
Thread-safe map               ──> ConcurrentHashMap
Simple counter                ──> AtomicInteger
Lock with tryLock / fairness  ──> ReentrantLock
Read-heavy                    ──> ReadWriteLock
Limit parallel access         ──> Semaphore
Producer-consumer             ──> BlockingQueue
Prevent duplicate processing  ──> Idempotency key in ConcurrentHashMap
```

---

*End of Rulebook. Run every step. Call out every step name.*
