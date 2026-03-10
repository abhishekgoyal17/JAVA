# 🏗️ Complete Low Level Design (LLD) Guide
### OOPS | State & Behavior | Interface vs Class | Exception Handling | Logging

> **Purpose:** A personal revision guide for LLD interviews. Study this before every interview.

---

## 📚 Table of Contents

1. [State & Behavior — The Foundation](#1-state--behavior--the-foundation)
2. [Interface vs Abstract Class vs Concrete Class](#2-interface-vs-abstract-class-vs-concrete-class)
3. [The Decision Framework](#3-the-decision-framework)
4. [All OOPS Concepts with Examples](#4-all-oops-concepts-with-examples)
5. [Exception Handling](#5-exception-handling)
6. [Logging — What, When, How](#6-logging--what-when-how)
7. [Complete LLD Example — Food Delivery App](#7-complete-lld-example--food-delivery-app)
8. [Interview Cheat Sheet](#8-interview-cheat-sheet)

---

## 1. State & Behavior — The Foundation

> **State = What an object KNOWS (data/fields)**
> **Behavior = What an object DOES (methods)**

### Real World Analogy

Think of a **Fan** in your room:

| Concept | Fan Example | Code |
|---|---|---|
| State | Is it ON? What speed? What color? | `boolean isOn; int speed; String color;` |
| Behavior | Turn on, Turn off, Increase speed | `turnOn(); turnOff(); increaseSpeed();` |

### Code Example

```java
class Fan {

    // ─── STATE (what the fan remembers) ───────────────────────
    private boolean isOn;
    private int speed;        // 1, 2, or 3
    private String color;

    // ─── BEHAVIOR (what the fan can do) ───────────────────────
    public void turnOn() {
        isOn = true;
        speed = 1;            // State changes!
    }

    public void turnOff() {
        isOn = false;
        speed = 0;            // State changes!
    }

    public void increaseSpeed() {
        // ✅ ALWAYS check state before doing behavior
        if (!isOn) {
            throw new IllegalStateException("Cannot increase speed — fan is OFF");
        }
        if (speed >= 3) {
            throw new IllegalStateException("Already at max speed");
        }
        speed++;              // State changes!
    }

    public int getSpeed() {
        return speed;         // Reading state
    }
}
```

### 🔑 The Golden Rule

```
Behavior READS state    → to validate if operation is allowed
Behavior CHANGES state  → after the operation is done
```

### Pattern Every Method Should Follow

```java
public void withdraw(double amount) {

    // STEP 1: Check STATE — is this operation valid right now?
    if (!isActive)
        throw new IllegalStateException("Account is closed");
    if (amount <= 0)
        throw new IllegalArgumentException("Amount must be positive");
    if (amount > balance)
        throw new InsufficientFundsException(amount, balance);

    // STEP 2: DO the behavior
    balance -= amount;

    // STEP 3: UPDATE state
    transactionHistory.add("Withdrawn: ₹" + amount);
    lastTransactionDate = LocalDateTime.now();
}
```

---

## 2. Interface vs Abstract Class vs Concrete Class

### The Simple Truth

```
Interface      = Contract     → defines WHAT to do (no implementation)
Abstract Class = Blueprint    → defines WHAT + SOME HOW (partial implementation)
Concrete Class = Full impl    → defines complete HOW (full implementation)
```

### Real World Analogy

| Type | Job Analogy |
|---|---|
| Interface | Job Description — "Must be able to drive, speak English" |
| Abstract Class | Training Manual — "Here's standard process, customize steps 3 & 5" |
| Concrete Class | Actual Employee — does everything completely |

---

### 2.1 Interface — Use When...

#### Rule: Multiple UNRELATED classes share the same CAPABILITY

```java
// 🚗 Car, 🚁 Drone, 🚤 Boat are completely unrelated...
// BUT all share the CAPABILITY of moving

interface Movable {
    void move();
    void stop();
}

class Car implements Movable {
    public void move() { System.out.println("Car drives on road"); }
    public void stop() { System.out.println("Car applies brakes"); }
}

class Drone implements Movable {
    public void move() { System.out.println("Drone flies in air"); }
    public void stop() { System.out.println("Drone hovers in place"); }
}

class Boat implements Movable {
    public void move() { System.out.println("Boat sails on water"); }
    public void stop() { System.out.println("Boat drops anchor"); }
}
```

**Why NOT a class here?**
- Car, Drone, Boat share ZERO common code
- They only share a *promise* → "I can move"
- No shared state, no shared logic

---

#### Rule: You want PLUG & PLAY (swap implementations without changing callers)

```java
// Tomorrow you switch from Stripe to Razorpay
// Your OrderService should NEVER change

interface PaymentProcessor {
    boolean processPayment(String userId, double amount);
    boolean refund(String transactionId, double amount);
}

class StripeProcessor implements PaymentProcessor {
    public boolean processPayment(String userId, double amount) {
        // Stripe-specific API call
        System.out.println("Processing via Stripe: ₹" + amount);
        return true;
    }
    public boolean refund(String transactionId, double amount) {
        // Stripe refund logic
        return true;
    }
}

class RazorpayProcessor implements PaymentProcessor {
    public boolean processPayment(String userId, double amount) {
        // Razorpay-specific API call
        System.out.println("Processing via Razorpay: ₹" + amount);
        return true;
    }
    public boolean refund(String transactionId, double amount) {
        // Razorpay refund logic
        return true;
    }
}

// ✅ OrderService never changes — just swap the implementation
class OrderService {
    private PaymentProcessor processor; // Depends on INTERFACE, not implementation

    public OrderService(PaymentProcessor processor) {
        this.processor = processor;
    }

    public void placeOrder(String userId, double amount) {
        boolean success = processor.processPayment(userId, amount);
        if (success) System.out.println("Order placed!");
    }
}

// Usage
OrderService service = new OrderService(new StripeProcessor());
// Want to switch? Just do:
OrderService service2 = new OrderService(new RazorpayProcessor());
```

---

### 2.2 Abstract Class — Use When...

#### Rule: RELATED classes share COMMON CODE and/or STATE

```java
// All reports have the SAME header and footer
// But each report has a DIFFERENT body
// → Share the common code, force each to define their own body

abstract class Report {

    // ─── SHARED STATE ─────────────────────────────────────────
    protected String companyName;
    protected LocalDate generatedDate;

    // ─── SHARED BEHAVIOR (same for ALL reports) ───────────────
    public void generate() {
        printHeader();          // Common
        generateBody();         // Different per report
        printFooter();          // Common
    }

    private void printHeader() {
        System.out.println("═══════════════════════════════");
        System.out.println("Company: " + companyName);
        System.out.println("Date: " + generatedDate);
        System.out.println("═══════════════════════════════");
    }

    private void printFooter() {
        System.out.println("═══════════════════════════════");
        System.out.println("End of Report — Confidential");
        System.out.println("═══════════════════════════════");
    }

    // ─── FORCED BEHAVIOR (each subclass MUST implement this) ──
    protected abstract void generateBody();
}

class SalesReport extends Report {
    private double totalSales;

    protected void generateBody() {
        System.out.println("Total Sales: ₹" + totalSales);
        System.out.println("Region: South India");
    }
}

class InventoryReport extends Report {
    private int totalItems;

    protected void generateBody() {
        System.out.println("Total Items: " + totalItems);
        System.out.println("Low Stock Alerts: 3 items");
    }
}
```

**Why NOT interface here?**
- Header and footer code is IDENTICAL across all reports
- Using interface = copy-paste this code in every class → violates DRY

**Why NOT concrete class?**
- You don't want anyone creating a plain `Report()` object — it makes no sense

---

#### Rule: You have SHARED STATE (fields/variables) across related classes

```java
// All vehicles share brand, speed, fuelType
// But each starts differently

abstract class Vehicle {

    // ─── SHARED STATE (all vehicles have this) ────────────────
    protected String brand;
    protected int currentSpeed;
    protected String fuelType;
    protected boolean isEngineOn;

    // ─── SHARED BEHAVIOR ──────────────────────────────────────
    public void refuel() {
        if (isEngineOn) {
            throw new IllegalStateException("Turn off engine before refueling!");
        }
        System.out.println("Refueling " + brand + " with " + fuelType);
    }

    public void accelerate(int by) {
        if (!isEngineOn) {
            throw new IllegalStateException("Start engine first!");
        }
        currentSpeed += by;
        System.out.println(brand + " accelerating to " + currentSpeed + " km/h");
    }

    // ─── FORCED BEHAVIOR (each vehicle starts differently) ────
    public abstract void start();
    public abstract void stop();
}

class PetrolCar extends Vehicle {
    public void start() {
        isEngineOn = true;
        System.out.println(brand + ": Turn key → Vroom!");
    }
    public void stop() {
        isEngineOn = false;
        currentSpeed = 0;
    }
}

class ElectricCar extends Vehicle {
    public void start() {
        isEngineOn = true;
        System.out.println(brand + ": Press button → Silently starts");
    }
    public void stop() {
        isEngineOn = false;
        currentSpeed = 0;
        System.out.println("Regenerative braking engaged");
    }
}
```

**Key point:** Interfaces CANNOT have instance variables with state. If you need shared fields → use Abstract Class.

---

### 2.3 Concrete Class — Use When...

The behavior is **complete, specific, and final**. No variation expected.

```java
class EmailSender {
    private String smtpHost;
    private int port;
    private String username;

    public EmailSender(String smtpHost, int port, String username) {
        this.smtpHost = smtpHost;
        this.port = port;
        this.username = username;
    }

    public void sendEmail(String to, String subject, String body) {
        // Complete, specific SMTP implementation
        System.out.println("Connecting to " + smtpHost + ":" + port);
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
    }
}
```

---

## 3. The Decision Framework

Use this flowchart in every interview:

```
Ask yourself: "What am I designing?"
        │
        ▼
Is it a CAPABILITY multiple unrelated things share?
        │
   YES ─┼─→ Use INTERFACE
        │    Example: Printable, Serializable, Trackable, Payable
        │
   NO  ─┼
        ▼
Is it a FAMILY of related things that share some code/state?
        │
   YES ─┼─→ Use ABSTRACT CLASS
        │    Example: Shape→Circle/Square, Report→Sales/Inventory
        │
   NO  ─┼
        ▼
Is it ONE specific thing with complete behavior?
        │
   YES ─┼─→ Use CONCRETE CLASS
             Example: EmailService, UserRepository, StripeProcessor
```

### Quick Reference Table

| Question | Answer | Use |
|---|---|---|
| Is it a "can do" relationship? | "Car **can** move", "Dog **can** bark" | Interface |
| Is it a "is a" relationship with shared code? | "GoldenRetriever **is a** Dog" | Abstract Class |
| Multiple classes need same capability but unrelated? | Car, Drone, Boat all **can** move | Interface |
| Multiple classes share code and belong to same family? | All Reports have header/footer | Abstract Class |
| Need to swap implementations? | Stripe ↔ Razorpay | Interface |
| Have shared fields/variables? | All vehicles have brand, speed | Abstract Class |

---

## 4. All OOPS Concepts with Examples

### 4.1 Encapsulation — "Hide the data, control the access"

```java
// ❌ BAD — No encapsulation
class BankAccountBad {
    public double balance; // Anyone can set balance = -999999 !
}

// ✅ GOOD — Encapsulated
class BankAccount {
    private double balance;          // Hidden!
    private String accountId;
    private boolean isActive;

    // Controlled access — validation happens here
    public void deposit(double amount) {
        if (!isActive)
            throw new IllegalStateException("Account is closed");
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive");

        balance += amount;
    }

    public double getBalance() {
        return balance; // Read-only access
    }
    // No setBalance() method — can't set balance directly!
}
```

**In interview say:** *"I'm encapsulating balance so no one can directly manipulate it — all access goes through validated methods."*

---

### 4.2 Inheritance — "IS-A relationship"

```java
// ✅ Use inheritance ONLY for IS-A relationships

class Animal {
    protected String name;
    protected int age;

    public void breathe() {
        System.out.println(name + " is breathing");
    }

    public void eat(String food) {
        System.out.println(name + " is eating " + food);
    }
}

class Dog extends Animal {
    private String breed;

    public void bark() {
        System.out.println(name + " says: Woof!");
    }
}

class GoldenRetriever extends Dog {
    public void fetch() {
        System.out.println(name + " fetches the ball!");
    }
}

// ❌ WRONG — Dog IS NOT A Car
class Dog extends Car { } // Never do this!
```

---

### 4.3 Composition — "HAS-A relationship" (PREFER over Inheritance)

```java
// ✅ Prefer composition — more flexible

// BAD: Using inheritance for HAS-A
class Driver extends Car { }  // Driver IS-NOT-A Car!

// GOOD: Composition
class Car {
    private Engine engine;      // Car HAS-A Engine
    private Wheels wheels;      // Car HAS-A Wheels
    private FuelTank fuelTank;  // Car HAS-A FuelTank

    public Car(Engine engine, Wheels wheels, FuelTank fuelTank) {
        this.engine = engine;
        this.wheels = wheels;
        this.fuelTank = fuelTank;
    }

    public void start() {
        fuelTank.checkFuel();
        engine.ignite();
        wheels.engage();
    }
}

class Engine {
    private String type; // "petrol", "diesel", "electric"
    private int horsepower;

    public void ignite() {
        System.out.println(type + " engine started — " + horsepower + "HP");
    }
}
```

**Why composition is better:**
- You can swap Engine without changing Car class
- `ElectricCar` just uses `ElectricEngine` — no new inheritance chain needed
- More testable — can mock individual components

**In interview say:** *"I prefer composition over inheritance here because it gives us more flexibility to swap components and avoids tight coupling."*

---

### 4.4 Polymorphism — "Same call, different behavior"

```java
// Define once, behave differently
interface NotificationSender {
    void send(String userId, String message);
}

class EmailNotification implements NotificationSender {
    public void send(String userId, String message) {
        System.out.println("📧 Email to " + userId + ": " + message);
    }
}

class SMSNotification implements NotificationSender {
    public void send(String userId, String message) {
        System.out.println("📱 SMS to " + userId + ": " + message);
    }
}

class PushNotification implements NotificationSender {
    public void send(String userId, String message) {
        System.out.println("🔔 Push to " + userId + ": " + message);
    }
}

// ✅ Polymorphism in action — same loop, different behavior
class NotificationService {
    private List<NotificationSender> senders;

    public NotificationService(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public void notifyAll(String userId, String message) {
        for (NotificationSender sender : senders) {
            sender.send(userId, message); // Each behaves differently!
        }
    }
}

// Usage
NotificationService service = new NotificationService(
    List.of(new EmailNotification(), new SMSNotification(), new PushNotification())
);
service.notifyAll("user123", "Your order has shipped!");
```

---

## 5. Exception Handling

### Exception Hierarchy Design

```java
// ─── LEVEL 1: Base exception for your whole application ───────

class AppException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;

    public AppException(String message, String errorCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public String getErrorCode() { return errorCode; }
    public String getUserMessage() { return userMessage; }
}


// ─── LEVEL 2: Domain-specific exceptions ──────────────────────

class PaymentException extends AppException {
    public PaymentException(String message) {
        super(message, "PAYMENT_ERROR", "Payment could not be processed");
    }
}

class OrderException extends AppException {
    public OrderException(String message) {
        super(message, "ORDER_ERROR", "Order processing failed");
    }
}

class UserException extends AppException {
    public UserException(String message) {
        super(message, "USER_ERROR", "User operation failed");
    }
}


// ─── LEVEL 3: Specific exceptions ─────────────────────────────

class InsufficientFundsException extends PaymentException {
    private final double required;
    private final double available;

    public InsufficientFundsException(double required, double available) {
        super(String.format(
            "Insufficient funds: required=%.2f, available=%.2f",
            required, available
        ));
        this.required = required;
        this.available = available;
    }

    public double getRequired() { return required; }
    public double getAvailable() { return available; }
}

class PaymentGatewayException extends PaymentException {
    private final String gatewayName;

    public PaymentGatewayException(String gatewayName, String reason) {
        super("Gateway " + gatewayName + " failed: " + reason);
        this.gatewayName = gatewayName;
    }
}

class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
```

### Catch at the Right Level

```java
class PaymentService {

    public boolean processPayment(String userId, double amount) {

        // Validate inputs first
        if (userId == null || userId.isEmpty())
            throw new IllegalArgumentException("UserId cannot be null or empty");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        try {
            boolean result = paymentGateway.charge(userId, amount);
            return result;

        } catch (InsufficientFundsException e) {
            // ✅ Catch specific — you know exactly what happened
            log.warn("Payment failed: insufficient funds | userId={} required={}", userId, amount);
            throw e; // Re-throw — let caller handle it

        } catch (PaymentGatewayException e) {
            // ✅ Catch specific — gateway issue
            log.error("Gateway failure | gateway={} userId={}", e.getGatewayName(), userId);
            throw e;

        } catch (Exception e) {
            // ✅ Catch generic LAST — unexpected errors only
            log.error("Unexpected payment failure | userId={} error={}", userId, e.getMessage(), e);
            throw new PaymentException("Payment processing failed unexpectedly");
        }
    }
}
```

### Edge Cases to Always Cover

```java
// For EVERY method — check these BEFORE writing logic:

// 1. Null inputs
if (input == null) throw new IllegalArgumentException("Input cannot be null");

// 2. Empty collections/strings
if (items.isEmpty()) throw new IllegalArgumentException("Items list cannot be empty");

// 3. Negative/zero numbers
if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

// 4. Invalid state
if (!account.isActive()) throw new IllegalStateException("Account is not active");

// 5. Duplicate entries
if (existingUsers.containsKey(userId)) throw new DuplicateUserException(userId);

// 6. Concurrent access — use synchronized or concurrent collections
private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

// 7. External service failures — always wrap in try-catch
try {
    externalService.call();
} catch (TimeoutException e) {
    throw new ServiceUnavailableException("External service timed out");
}
```

---

## 6. Logging — What, When, How

### Log Levels Guide

| Level | When to Use | Example |
|---|---|---|
| `DEBUG` | Detailed dev info | Variable values, method entry |
| `INFO` | Normal operations | Request received, order placed |
| `WARN` | Something unusual but handled | Retry attempt, low stock |
| `ERROR` | Something failed unexpectedly | Exception thrown, data corruption |

### What TO Log vs NOT Log

```java
// ✅ ALWAYS LOG:
// - Request/Transaction ID (for tracing)
// - User ID (not user name/email)
// - Method name / operation
// - Input parameters (non-sensitive)
// - Outcome (success/failure)
// - Error type and message
// - Duration for slow operations

// ❌ NEVER LOG:
// - Passwords
// - Credit card numbers
// - OTPs / PINs
// - Full SSN / Aadhaar
// - Auth tokens / API keys
// - Personal data (email, phone — unless necessary)
```

### Complete Logging Example

```java
class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order createOrder(String userId, List<String> itemIds, String paymentMethod) {

        // ✅ LOG: Entry with context
        log.info("Creating order | userId={} itemCount={} paymentMethod={}",
                 userId, itemIds.size(), paymentMethod);

        long startTime = System.currentTimeMillis();

        try {
            // Validate
            validateItems(itemIds);

            // Process
            Order order = new Order(userId, itemIds);
            boolean paid = paymentService.processPayment(userId, order.getTotalAmount());

            if (!paid) {
                // ✅ LOG: Business failure — WARN level
                log.warn("Payment failed for order | userId={} amount={}",
                         userId, order.getTotalAmount());
                throw new PaymentException("Payment unsuccessful");
            }

            orderRepository.save(order);

            // ✅ LOG: Success with key info
            long duration = System.currentTimeMillis() - startTime;
            log.info("Order created successfully | userId={} orderId={} amount={} duration={}ms",
                     userId, order.getId(), order.getTotalAmount(), duration);

            return order;

        } catch (InsufficientFundsException e) {
            log.warn("Order failed: insufficient funds | userId={}", userId);
            throw e;

        } catch (Exception e) {
            // ✅ LOG: Unexpected failure — ERROR with stack trace
            log.error("Order creation failed unexpectedly | userId={} error={}",
                      userId, e.getMessage(), e); // Last param = exception → prints stack trace
            throw new OrderException("Failed to create order");
        }
    }
}
```

---

## 7. Complete LLD Example — Food Delivery App

This example combines EVERYTHING: State, Behavior, Interfaces, Abstract Classes, Exceptions, Logging.

### Step 1: Identify Entities and Ask Clarifying Questions

```
Entities: User, Customer, RestaurantOwner, DeliveryAgent
          Restaurant, MenuItem, Order, OrderItem
          Payment, Notification, Location

Clarifying questions to ask in interview:
- Single city or multiple cities?
- Real-time tracking needed?
- Which payment methods?
- Should we handle cancellations?
```

### Step 2: Define Interfaces (Capabilities)

```java
// ─── INTERFACES — shared capabilities ─────────────────────────

interface Trackable {
    // Both Order AND DeliveryAgent need tracking
    String getCurrentLocation();
    void updateLocation(double lat, double lng);
}

interface Payable {
    // Multiple payment methods — plug & play
    boolean pay(String userId, double amount);
    boolean refund(String transactionId, double amount);
}

interface Notifiable {
    // Email, SMS, Push all notify differently
    void sendNotification(String userId, String message);
    String getChannelType();
}

interface Reviewable {
    // Restaurants and DeliveryAgents can be reviewed
    void addReview(String userId, int rating, String comment);
    double getAverageRating();
}
```

### Step 3: Define Abstract Classes (Shared Family Behavior)

```java
// ─── ABSTRACT CLASSES — shared family code ────────────────────

abstract class User {

    // SHARED STATE — all users have this
    protected final String userId;
    protected String name;
    protected String phoneNumber;
    protected String email;
    protected boolean isActive;

    protected User(String userId, String name, String phoneNumber, String email) {
        if (userId == null) throw new IllegalArgumentException("UserId cannot be null");
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name cannot be empty");

        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isActive = true;
    }

    // SHARED BEHAVIOR — same for all users
    public String getProfile() {
        return String.format("User[id=%s, name=%s, active=%s]", userId, name, isActive);
    }

    public void deactivate() {
        this.isActive = false;
    }

    // FORCED BEHAVIOR — each user type defines this
    public abstract String getUserType();
    public abstract boolean canPerformAction(String action);
}


abstract class MenuItem {

    // SHARED STATE
    protected String itemId;
    protected String name;
    protected double basePrice;
    protected boolean isAvailable;

    // SHARED BEHAVIOR
    public String getDetails() {
        return String.format("%s — ₹%.2f %s",
            name,
            getFinalPrice(),
            isAvailable ? "✓" : "✗ (Unavailable)"
        );
    }

    // FORCED — price calculation differs per type
    public abstract double getFinalPrice();
}
```

### Step 4: Concrete Classes

```java
// ─── CONCRETE CLASSES — full implementations ──────────────────

class Customer extends User implements Trackable {

    private double currentLat;
    private double currentLng;
    private List<Order> orderHistory;
    private List<String> savedAddresses;

    public Customer(String userId, String name, String phoneNumber, String email) {
        super(userId, name, phoneNumber, email);
        this.orderHistory = new ArrayList<>();
        this.savedAddresses = new ArrayList<>();
    }

    // Trackable implementation
    public String getCurrentLocation() {
        return String.format("%.4f, %.4f", currentLat, currentLng);
    }

    public void updateLocation(double lat, double lng) {
        this.currentLat = lat;
        this.currentLng = lng;
    }

    public String getUserType() { return "CUSTOMER"; }

    public boolean canPerformAction(String action) {
        return isActive && List.of("ORDER", "TRACK", "REVIEW", "CANCEL").contains(action);
    }

    public void addOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");
        orderHistory.add(order);
    }
}


class DeliveryAgent extends User implements Trackable, Reviewable {

    private double currentLat;
    private double currentLng;
    private boolean isAvailable;
    private List<int[]> ratings; // [rating, timestamp]
    private Order currentOrder;

    public DeliveryAgent(String userId, String name, String phoneNumber, String email) {
        super(userId, name, phoneNumber, email);
        this.isAvailable = true;
        this.ratings = new ArrayList<>();
    }

    // Trackable implementation
    public String getCurrentLocation() {
        return String.format("%.4f, %.4f", currentLat, currentLng);
    }

    public void updateLocation(double lat, double lng) {
        this.currentLat = lat;
        this.currentLng = lng;
    }

    // Reviewable implementation
    public void addReview(String userId, int rating, String comment) {
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        ratings.add(new int[]{rating});
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream()
            .mapToInt(r -> r[0])
            .average()
            .orElse(0.0);
    }

    public void assignOrder(Order order) {
        if (!isAvailable)
            throw new IllegalStateException("Agent is not available");
        if (order == null)
            throw new IllegalArgumentException("Order cannot be null");

        this.currentOrder = order;
        this.isAvailable = false;
    }

    public void completeDelivery() {
        if (currentOrder == null)
            throw new IllegalStateException("No active order to complete");

        this.currentOrder = null;
        this.isAvailable = true;
    }

    public String getUserType() { return "DELIVERY_AGENT"; }
    public boolean canPerformAction(String action) {
        return isActive && List.of("PICKUP", "DELIVER", "UPDATE_LOCATION").contains(action);
    }
}


class RegularMenuItem extends MenuItem {
    public RegularMenuItem(String itemId, String name, double basePrice) {
        this.itemId = itemId;
        this.name = name;
        this.basePrice = basePrice;
        this.isAvailable = true;
    }

    public double getFinalPrice() {
        return basePrice; // No discount
    }
}

class DiscountedMenuItem extends MenuItem {
    private double discountPercent;
    private LocalDateTime discountExpiry;

    public DiscountedMenuItem(String itemId, String name, double basePrice,
                               double discountPercent, LocalDateTime expiry) {
        this.itemId = itemId;
        this.name = name;
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
        this.discountExpiry = expiry;
        this.isAvailable = true;
    }

    public double getFinalPrice() {
        // Check if discount is still valid
        if (LocalDateTime.now().isAfter(discountExpiry)) {
            return basePrice; // Discount expired
        }
        return basePrice * (1 - discountPercent / 100);
    }
}
```

### Step 5: Order with Full State Machine

```java
// ─── ORDER — State Machine Example ────────────────────────────

enum OrderStatus {
    PLACED, CONFIRMED, PREPARING, PICKED_UP, DELIVERED, CANCELLED
}

class Order {
    private static final Logger log = LoggerFactory.getLogger(Order.class);

    // STATE
    private final String orderId;
    private final String customerId;
    private final String restaurantId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private double totalAmount;
    private LocalDateTime placedAt;
    private LocalDateTime deliveredAt;
    private String assignedAgentId;

    public Order(String customerId, String restaurantId, List<OrderItem> items) {
        if (customerId == null) throw new IllegalArgumentException("CustomerId cannot be null");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Order must have items");

        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PLACED;
        this.placedAt = LocalDateTime.now();
        this.totalAmount = calculateTotal();

        log.info("Order created | orderId={} customerId={} itemCount={} total={}",
                 orderId, customerId, items.size(), totalAmount);
    }

    // BEHAVIOR — each transition validates current STATE
    public void confirm() {
        if (status != OrderStatus.PLACED)
            throw new IllegalStateException("Can only confirm a PLACED order. Current: " + status);

        this.status = OrderStatus.CONFIRMED;
        log.info("Order confirmed | orderId={}", orderId);
    }

    public void startPreparing() {
        if (status != OrderStatus.CONFIRMED)
            throw new IllegalStateException("Can only start preparing a CONFIRMED order. Current: " + status);

        this.status = OrderStatus.PREPARING;
        log.info("Order preparation started | orderId={}", orderId);
    }

    public void assignDeliveryAgent(String agentId) {
        if (agentId == null) throw new IllegalArgumentException("AgentId cannot be null");
        if (status != OrderStatus.PREPARING)
            throw new IllegalStateException("Can only assign agent to PREPARING order");

        this.assignedAgentId = agentId;
        log.info("Delivery agent assigned | orderId={} agentId={}", orderId, agentId);
    }

    public void markPickedUp() {
        if (assignedAgentId == null)
            throw new IllegalStateException("No delivery agent assigned");
        if (status != OrderStatus.PREPARING)
            throw new IllegalStateException("Can only pick up a PREPARING order. Current: " + status);

        this.status = OrderStatus.PICKED_UP;
        log.info("Order picked up | orderId={} agentId={}", orderId, assignedAgentId);
    }

    public void markDelivered() {
        if (status != OrderStatus.PICKED_UP)
            throw new IllegalStateException("Can only deliver a PICKED_UP order. Current: " + status);

        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        log.info("Order delivered | orderId={} customerId={} deliveredAt={}",
                 orderId, customerId, deliveredAt);
    }

    public void cancel(String reason) {
        if (status == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel a delivered order");
        if (status == OrderStatus.PICKED_UP)
            throw new IllegalStateException("Cannot cancel — order is already picked up");

        this.status = OrderStatus.CANCELLED;
        log.warn("Order cancelled | orderId={} reason={} previousStatus={}", orderId, reason, status);
    }

    private double calculateTotal() {
        return items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
}
```

### Step 6: Payment — Strategy Pattern

```java
// ─── PAYMENT — Plug & Play using Interface ────────────────────

class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final Map<String, Payable> processors;

    public PaymentService(Map<String, Payable> processors) {
        if (processors == null || processors.isEmpty())
            throw new IllegalArgumentException("At least one payment processor required");
        this.processors = processors;
    }

    public boolean processPayment(String userId, double amount, String method) {
        log.info("Processing payment | userId={} amount={} method={}", userId, amount, method);

        Payable processor = processors.get(method.toUpperCase());
        if (processor == null)
            throw new IllegalArgumentException("Unsupported payment method: " + method);

        try {
            boolean result = processor.pay(userId, amount);
            if (result) {
                log.info("Payment successful | userId={} amount={} method={}", userId, amount, method);
            } else {
                log.warn("Payment failed | userId={} amount={} method={}", userId, amount, method);
            }
            return result;

        } catch (InsufficientFundsException e) {
            log.warn("Payment failed: insufficient funds | userId={} required={}", userId, amount);
            throw e;
        } catch (Exception e) {
            log.error("Payment failed unexpectedly | userId={} method={} error={}",
                      userId, method, e.getMessage(), e);
            throw new PaymentException("Payment processing failed");
        }
    }
}

// Payment implementations
class UPIPayment implements Payable {
    public boolean pay(String userId, double amount) {
        System.out.println("Paid ₹" + amount + " via UPI for user: " + userId);
        return true;
    }
    public boolean refund(String transactionId, double amount) {
        System.out.println("Refunded ₹" + amount + " via UPI | txn: " + transactionId);
        return true;
    }
}

class CardPayment implements Payable {
    public boolean pay(String userId, double amount) {
        System.out.println("Paid ₹" + amount + " via Card for user: " + userId);
        return true;
    }
    public boolean refund(String transactionId, double amount) {
        System.out.println("Refunded ₹" + amount + " via Card | txn: " + transactionId);
        return true;
    }
}

class CashOnDelivery implements Payable {
    public boolean pay(String userId, double amount) {
        // COD — payment happens at delivery
        System.out.println("COD order confirmed for user: " + userId + " | amount: ₹" + amount);
        return true;
    }
    public boolean refund(String transactionId, double amount) {
        System.out.println("COD refund initiated | txn: " + transactionId);
        return true;
    }
}
```

### Step 7: Putting It All Together

```java
// ─── MAIN SERVICE — Orchestration ─────────────────────────────

class FoodDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(FoodDeliveryService.class);

    private final Map<String, Customer> customers;
    private final Map<String, Order> orders;
    private final PaymentService paymentService;
    private final List<NotificationSender> notificationSenders;

    public FoodDeliveryService(PaymentService paymentService,
                                List<NotificationSender> notificationSenders) {
        this.customers = new ConcurrentHashMap<>();
        this.orders = new ConcurrentHashMap<>();
        this.paymentService = paymentService;
        this.notificationSenders = notificationSenders;
    }

    public Order placeOrder(String customerId, String restaurantId,
                             List<OrderItem> items, String paymentMethod) {

        log.info("Placing order | customerId={} restaurantId={} itemCount={}",
                 customerId, restaurantId, items.size());

        // Validate inputs
        Customer customer = customers.get(customerId);
        if (customer == null)
            throw new UserException("Customer not found: " + customerId);
        if (!customer.isActive)
            throw new IllegalStateException("Customer account is not active");

        // Create order
        Order order = new Order(customerId, restaurantId, items);

        // Process payment
        boolean paid = paymentService.processPayment(customerId, order.getTotalAmount(), paymentMethod);
        if (!paid) throw new PaymentException("Payment failed for order: " + order.getOrderId());

        // Confirm order
        order.confirm();
        orders.put(order.getOrderId(), order);
        customer.addOrder(order);

        // Notify customer
        notifyUser(customerId, "Order placed! Order ID: " + order.getOrderId());

        return order;
    }

    private void notifyUser(String userId, String message) {
        for (NotificationSender sender : notificationSenders) {
            try {
                sender.send(userId, message);
            } catch (Exception e) {
                // Don't fail the whole operation if notification fails
                log.warn("Notification failed | userId={} channel={} error={}",
                         userId, sender.getChannelType(), e.getMessage());
            }
        }
    }
}
```

---

## 8. Interview Cheat Sheet

### Phrases That Signal a 5-Rated Candidate

| Situation | What to Say |
|---|---|
| Choosing interface | *"I'm using an interface here because multiple unrelated classes need this capability, and it allows plug-and-play swapping of implementations."* |
| Choosing abstract class | *"I'm using an abstract class because these are related entities sharing common state and behavior, but the core logic differs per type."* |
| Handling edge cases | *"Before finalizing the design, let me think about failure scenarios — null inputs, concurrent access, and external service failures."* |
| Adding exceptions | *"I'll build a custom exception hierarchy so callers can catch at the right level of granularity."* |
| Adding logging | *"I'll add structured logging at entry and exit points — logging user ID and request ID, but never any sensitive data like passwords or card numbers."* |
| Composition choice | *"I prefer composition over inheritance here because it gives more flexibility and avoids tight coupling."* |
| State validation | *"I always validate the current state before performing any operation to prevent illegal state transitions."* |

### Interview Flow — Follow This Every Time

```
1. CLARIFY (2 mins)
   "Can I ask a few questions first?"
   - Scale? Single user / multi-user?
   - Any specific constraints?
   - Which component to focus on?

2. IDENTIFY ENTITIES (2 mins)
   "Let me identify the main classes and interfaces..."

3. DEFINE RELATIONSHIPS (3 mins)
   - What are the capabilities? → Interfaces
   - Which are related families? → Abstract Classes
   - What are the concrete implementations?

4. WRITE CORE CODE (5-8 mins)

5. SELF-REVIEW OUT LOUD (2 mins)
   "Let me check edge cases..."
   → Null inputs?
   → Invalid state transitions?
   → Concurrency?
   → External failures?

6. ADD EXCEPTIONS + LOGGING (2 mins)
   "I'll add custom exceptions and logging here..."
```

### Edge Cases Checklist (Memorize This)

- [ ] Null inputs
- [ ] Empty collections / empty strings
- [ ] Negative or zero numbers
- [ ] Invalid state (is account active? is engine on?)
- [ ] Duplicate entries
- [ ] Concurrent access (thread safety)
- [ ] External service timeout / failure
- [ ] Max limits exceeded (order limit, retry limit)

### Do's and Don'ts

| ✅ DO | ❌ DON'T |
|---|---|
| Ask clarifying questions first | Jump into code immediately |
| Think out loud | Stay silent during long pauses |
| Mention trade-offs | Present only one solution |
| Self-review your code | Wait to be told about bugs |
| Bring up observability/logging | Only answer what's asked |
| Use interfaces for plug-and-play | Hardcode implementations |
| Prefer composition over inheritance | Over-use inheritance |
| Validate state before behavior | Skip input validation |

---

> 💡 **Remember:** The rubric rewards candidates who don't need hints.
> Handle edge cases, suggest best practices, and add logging/exceptions **before** the interviewer asks.
> That's the difference between a 4 and a 5.

---











    
