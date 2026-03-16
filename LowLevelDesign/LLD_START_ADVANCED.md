# Complete Low Level Design (LLD) Guide
### OOPS | State & Behavior | Interface vs Abstract Class | Exception Handling | Logging

> Personal revision guide for LLD interviews. Every code example is complete and runnable with a `main()` method.
> Covers Java 17+ | Spring Boot ready | FAANG-tier depth

---

## Table of Contents

1. [State & Behavior — The Foundation](#1-state--behavior--the-foundation)
2. [Interface vs Abstract Class vs Concrete Class](#2-interface-vs-abstract-class-vs-concrete-class)
3. [All OOPS Concepts with Runnable Examples](#3-all-oops-concepts-with-runnable-examples)
4. [Exception Handling — Build a Hierarchy](#4-exception-handling--build-a-hierarchy)
5. [Logging — What, When, How](#5-logging--what-when-how)
6. [Complete LLD — Food Delivery App](#6-complete-lld--food-delivery-app)
7. [Interview Cheat Sheet](#7-interview-cheat-sheet)

---

## 1. State & Behavior — The Foundation

> **State = What an object KNOWS (fields/instance variables)**
> **Behavior = What an object DOES (methods)**

| Concept | What it means | Stored as | Example (Fan) |
|---|---|---|---|
| State | What the object knows / remembers | Fields / instance variables | `isOn`, `speed`, `color` |
| Behavior | What the object can do / actions | Methods | `turnOn()`, `turnOff()`, `increaseSpeed()` |

### The Golden Rule

```
Behavior READS state    → to validate if the operation is allowed
Behavior CHANGES state  → after the operation completes successfully
Never skip validation. Always throw a meaningful exception if state is invalid.
```

### 1.1 Complete Fan Example

```java
import java.util.ArrayList;
import java.util.List;

class Fan {

    // STATE — what the fan remembers
    private boolean isOn;
    private int speed;        // 1, 2, or 3
    private String color;
    private List<String> log;

    public Fan(String color) {
        this.color = color;
        this.isOn  = false;
        this.speed = 0;
        this.log   = new ArrayList<>();
    }

    // BEHAVIOR — Pattern: validate state → do behavior → update state

    public void turnOn() {
        if (isOn) throw new IllegalStateException("Fan is already ON");
        isOn  = true;
        speed = 1;
        log.add("Turned ON at speed 1");
        System.out.println("Fan turned ON. Speed: " + speed);
    }

    public void turnOff() {
        if (!isOn) throw new IllegalStateException("Fan is already OFF");
        isOn  = false;
        speed = 0;
        log.add("Turned OFF");
        System.out.println("Fan turned OFF");
    }

    public void increaseSpeed() {
        if (!isOn)    throw new IllegalStateException("Cannot increase speed — fan is OFF");
        if (speed >= 3) throw new IllegalStateException("Already at max speed (3)");
        speed++;
        log.add("Speed increased to " + speed);
        System.out.println("Speed increased to: " + speed);
    }

    public void decreaseSpeed() {
        if (!isOn)    throw new IllegalStateException("Cannot decrease speed — fan is OFF");
        if (speed <= 1) throw new IllegalStateException("Already at minimum speed. Use turnOff() instead.");
        speed--;
        log.add("Speed decreased to " + speed);
        System.out.println("Speed decreased to: " + speed);
    }

    public int getSpeed()    { return speed; }
    public boolean isOn()    { return isOn; }
    public String getColor() { return color; }

    public void printLog() {
        System.out.println("\n--- Fan Operation Log ---");
        log.forEach(entry -> System.out.println("  > " + entry));
    }
}

public class FanDemo {
    public static void main(String[] args) {

        Fan fan = new Fan("White");
        fan.turnOn();
        fan.increaseSpeed();
        fan.increaseSpeed();
        fan.decreaseSpeed();
        fan.turnOff();

        System.out.println("\n--- Testing illegal state transitions ---");

        try {
            fan.increaseSpeed();   // Fan is OFF
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            fan.turnOff();         // Already OFF
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        fan.turnOn();
        fan.increaseSpeed();
        fan.increaseSpeed();
        try {
            fan.increaseSpeed();   // Already at max
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        fan.printLog();

        /*
         * OUTPUT:
         * Fan turned ON. Speed: 1
         * Speed increased to: 2
         * Speed increased to: 3
         * Speed decreased to: 2
         * Fan turned OFF
         * --- Testing illegal state transitions ---
         * Caught: Cannot increase speed — fan is OFF
         * Caught: Fan is already OFF
         * Caught: Already at max speed (3)
         * --- Fan Operation Log ---
         *   > Turned ON at speed 1
         *   > Speed increased to 2
         *   > Speed increased to 3
         *   > Speed decreased to 2
         *   > Turned OFF
         *   > Turned ON at speed 1
         *   > Speed increased to 2
         *   > Speed increased to 3
         */
    }
}
```

---

### 1.2 State Machines — Thinking in Transitions

A State Machine models an object whose valid operations depend on its current state. An Order, a Ticket, a Connection — these are all state machines in disguise.

Think of it as: **"From state X, which transitions are allowed?"**

| From State | Allowed Next States | Blocked Transitions |
|---|---|---|
| `PLACED` | `CONFIRMED`, `CANCELLED` | `PICKED_UP`, `DELIVERED` |
| `CONFIRMED` | `PREPARING`, `CANCELLED` | `PLACED`, `DELIVERED` |
| `PREPARING` | `PICKED_UP`, `CANCELLED` | `PLACED`, `CONFIRMED` |
| `PICKED_UP` | `DELIVERED` | `PLACED`, `CONFIRMED`, `CANCELLED` |
| `DELIVERED` | — (terminal) | All — cannot undo delivery |
| `CANCELLED` | — (terminal) | All — cannot revive order |

```java
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

enum OrderStatus {
    PLACED, CONFIRMED, PREPARING, PICKED_UP, DELIVERED, CANCELLED
}

class Order {

    private final String orderId;
    private final String customerId;
    private OrderStatus status;
    private final LocalDateTime placedAt;
    private LocalDateTime deliveredAt;
    private final List<String> history;

    public Order(String orderId, String customerId) {
        if (orderId == null || orderId.isEmpty())
            throw new IllegalArgumentException("orderId cannot be null or empty");
        if (customerId == null)
            throw new IllegalArgumentException("customerId cannot be null");

        this.orderId    = orderId;
        this.customerId = customerId;
        this.status     = OrderStatus.PLACED;
        this.placedAt   = LocalDateTime.now();
        this.history    = new ArrayList<>();
        history.add("Order PLACED at " + placedAt);
    }

    public void confirm() {
        requireStatus(OrderStatus.PLACED, "confirm");
        transition(OrderStatus.CONFIRMED, "Restaurant confirmed the order");
    }

    public void startPreparing() {
        requireStatus(OrderStatus.CONFIRMED, "startPreparing");
        transition(OrderStatus.PREPARING, "Kitchen started preparing");
    }

    public void pickUp() {
        requireStatus(OrderStatus.PREPARING, "pickUp");
        transition(OrderStatus.PICKED_UP, "Delivery agent picked up order");
    }

    public void deliver() {
        requireStatus(OrderStatus.PICKED_UP, "deliver");
        this.deliveredAt = LocalDateTime.now();
        transition(OrderStatus.DELIVERED, "Order delivered at " + deliveredAt);
    }

    public void cancel(String reason) {
        if (status == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel — order already DELIVERED");
        if (status == OrderStatus.PICKED_UP)
            throw new IllegalStateException("Cannot cancel — order already PICKED UP");
        if (status == OrderStatus.CANCELLED)
            throw new IllegalStateException("Order is already CANCELLED");
        transition(OrderStatus.CANCELLED, "Cancelled: " + reason);
    }

    // --- Private helpers ---

    private void requireStatus(OrderStatus required, String operation) {
        if (this.status != required) {
            throw new IllegalStateException(String.format(
                "Cannot %s — order is %s, required: %s",
                operation, this.status, required
            ));
        }
    }

    private void transition(OrderStatus next, String note) {
        OrderStatus prev = this.status;
        this.status = next;
        history.add(prev + " --> " + next + " | " + note);
        System.out.printf("  [%s] %s --> %s%n", orderId, prev, next);
    }

    public void printHistory() {
        System.out.println("\nOrder History: " + orderId);
        history.forEach(h -> System.out.println("  " + h));
    }

    public OrderStatus getStatus() { return status; }
    public String getOrderId()     { return orderId; }
}

public class OrderStateMachineDemo {
    public static void main(String[] args) {

        System.out.println("=== Happy Path ===");
        Order order = new Order("ORD-001", "CUST-42");
        order.confirm();
        order.startPreparing();
        order.pickUp();
        order.deliver();
        order.printHistory();

        System.out.println("\n=== Cancellation Path ===");
        Order order2 = new Order("ORD-002", "CUST-43");
        order2.confirm();
        order2.cancel("Customer changed mind");
        order2.printHistory();

        System.out.println("\n=== Illegal Transitions ===");
        Order order3 = new Order("ORD-003", "CUST-44");
        try {
            order3.pickUp();  // Can't skip CONFIRMED + PREPARING
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        order3.confirm();
        order3.startPreparing();
        order3.pickUp();
        order3.deliver();
        try {
            order3.cancel("Too late!");
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        /*
         * OUTPUT:
         * === Happy Path ===
         *   [ORD-001] PLACED --> CONFIRMED
         *   [ORD-001] CONFIRMED --> PREPARING
         *   [ORD-001] PREPARING --> PICKED_UP
         *   [ORD-001] PICKED_UP --> DELIVERED
         *
         * === Cancellation Path ===
         *   [ORD-002] PLACED --> CONFIRMED
         *   [ORD-002] CONFIRMED --> CANCELLED
         *
         * === Illegal Transitions ===
         * Caught: Cannot pickUp — order is PLACED, required: PREPARING
         * Caught: Cannot cancel — order already DELIVERED
         */
    }
}
```

> **In Interview Say:** *"I always validate current state before performing any operation to prevent illegal state transitions. For state machines I use an enum and throw `IllegalStateException` for invalid transitions."*

---

## 2. Interface vs Abstract Class vs Concrete Class

| Type | Mental Model | Keyword | Can have fields? | Can have impl? |
|---|---|---|---|---|
| Interface | Contract / Promise | `implements` | Only `static final` constants | Default methods (Java 8+) |
| Abstract Class | Blueprint / Template | `extends` | Yes — instance variables | Yes — partial impl |
| Concrete Class | Full Implementation | `extends` / `new` | Yes | Yes — complete |

### Decision Framework

```
Is it a CAPABILITY multiple unrelated classes share?
  YES → Interface   (Movable, Payable, Printable, Trackable)

Is it a FAMILY of related classes with shared code/state?
  YES → Abstract Class   (Shape→Circle/Square, Report→Sales/Inventory)

Is it ONE specific thing with complete behavior?
  YES → Concrete Class   (EmailService, UserRepository, StripeProcessor)
```

| Question | Answer | Use |
|---|---|---|
| "Can do" capability across unrelated classes? | Car can move, Dog can bark | Interface |
| "Is a" relationship with shared code? | GoldenRetriever IS-A Dog | Abstract Class |
| Need to swap implementations? | Stripe ↔ Razorpay | Interface |
| Shared state (fields) across a family? | All vehicles have brand, speed | Abstract Class |

---

### 2.1 Interface — Multiple unrelated classes, same capability

```java
// Car, Drone, Boat are unrelated — but ALL share the capability of moving

interface Movable {
    void move();
    void stop();
    String getTransportType();
}

class Car implements Movable {
    private String brand;
    private int speed;

    public Car(String brand) { this.brand = brand; }

    @Override public void move() {
        speed = 60;
        System.out.println(brand + " drives on road at " + speed + " km/h");
    }
    @Override public void stop() {
        speed = 0;
        System.out.println(brand + " applies brakes");
    }
    @Override public String getTransportType() { return "LAND"; }
}

class Drone implements Movable {
    private String model;
    private int altitude;

    public Drone(String model) { this.model = model; }

    @Override public void move() {
        altitude = 100;
        System.out.println(model + " flies at " + altitude + "m altitude");
    }
    @Override public void stop() {
        altitude = 0;
        System.out.println(model + " hovers in place");
    }
    @Override public String getTransportType() { return "AIR"; }
}

class Boat implements Movable {
    private String name;
    public Boat(String name) { this.name = name; }

    @Override public void move() { System.out.println(name + " sails on water"); }
    @Override public void stop() { System.out.println(name + " drops anchor"); }
    @Override public String getTransportType() { return "WATER"; }
}

class TrafficController {
    public static void moveAll(java.util.List<Movable> vehicles) {
        System.out.println("\n--- All vehicles moving ---");
        for (Movable v : vehicles) v.move();
    }
    public static void stopAll(java.util.List<Movable> vehicles) {
        System.out.println("\n--- All vehicles stopping ---");
        for (Movable v : vehicles) v.stop();
    }
}

public class MovableDemo {
    public static void main(String[] args) {
        java.util.List<Movable> fleet = java.util.List.of(
            new Car("Tesla"),
            new Drone("DJI Phantom"),
            new Boat("Sea Eagle")
        );

        TrafficController.moveAll(fleet);
        TrafficController.stopAll(fleet);

        System.out.println("\n--- Transport types ---");
        for (Movable v : fleet) {
            System.out.println(v.getTransportType() + ": " + v.getClass().getSimpleName());
        }

        /*
         * OUTPUT:
         * --- All vehicles moving ---
         * Tesla drives on road at 60 km/h
         * DJI Phantom flies at 100m altitude
         * Sea Eagle sails on water
         * --- All vehicles stopping ---
         * Tesla applies brakes
         * DJI Phantom hovers in place
         * Sea Eagle drops anchor
         * --- Transport types ---
         * LAND: Car
         * AIR: Drone
         * WATER: Boat
         */
    }
}
```

### Subtopic: Default Methods in Interfaces (Java 8+)

Default methods let you add new behavior to an interface without breaking all existing implementations. Use them for shared utility/validation logic.

```java
interface PaymentProcessor {

    // Abstract — MUST be implemented
    boolean processPayment(String userId, double amount);
    boolean refund(String transactionId, double amount);

    // Default — CAN be overridden, but doesn't have to be
    default String getProcessorName() {
        return "Unknown Processor";
    }

    // Static — utility, not inherited
    static void validateAmount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException(
            "Payment amount must be positive, got: " + amount);
    }
}

class StripeProcessor implements PaymentProcessor {
    private final String apiKey;

    public StripeProcessor(String apiKey) {
        if (apiKey == null || apiKey.isEmpty())
            throw new IllegalArgumentException("API key required");
        this.apiKey = apiKey;
    }

    @Override
    public boolean processPayment(String userId, double amount) {
        PaymentProcessor.validateAmount(amount);
        System.out.printf("[Stripe] Charging user=%s amount=%.2f%n", userId, amount);
        return true;
    }

    @Override
    public boolean refund(String transactionId, double amount) {
        System.out.printf("[Stripe] Refunding txn=%s amount=%.2f%n", transactionId, amount);
        return true;
    }

    @Override
    public String getProcessorName() { return "Stripe"; }
}

class RazorpayProcessor implements PaymentProcessor {
    @Override
    public boolean processPayment(String userId, double amount) {
        PaymentProcessor.validateAmount(amount);
        System.out.printf("[Razorpay] Processing user=%s amount=%.2f%n", userId, amount);
        return true;
    }

    @Override
    public boolean refund(String transactionId, double amount) {
        System.out.printf("[Razorpay] Refund txn=%s amount=%.2f%n", transactionId, amount);
        return true;
    }

    @Override
    public String getProcessorName() { return "Razorpay"; }
}

class MockPaymentProcessor implements PaymentProcessor {
    private final boolean shouldSucceed;
    public MockPaymentProcessor(boolean shouldSucceed) { this.shouldSucceed = shouldSucceed; }

    @Override
    public boolean processPayment(String userId, double amount) {
        System.out.printf("[MOCK] Payment %s user=%s amount=%.2f%n",
            shouldSucceed ? "SUCCESS" : "FAIL", userId, amount);
        return shouldSucceed;
    }

    @Override
    public boolean refund(String txnId, double amount) { return shouldSucceed; }
    // Does NOT override getProcessorName() → uses default = "Unknown Processor"
}

class OrderService {
    private final PaymentProcessor processor;  // Depends on INTERFACE, not impl

    public OrderService(PaymentProcessor processor) {
        if (processor == null) throw new IllegalArgumentException("Processor cannot be null");
        this.processor = processor;
    }

    public boolean placeOrder(String userId, double amount) {
        System.out.println("\nPlacing order via " + processor.getProcessorName());
        boolean paid = processor.processPayment(userId, amount);
        System.out.println(paid ? "Order placed!" : "Order failed — payment unsuccessful");
        return paid;
    }
}

public class PaymentDemo {
    public static void main(String[] args) {

        // Swap processors — OrderService never changes!
        OrderService stripeService   = new OrderService(new StripeProcessor("sk_test_key"));
        OrderService razorpayService = new OrderService(new RazorpayProcessor());
        OrderService testService     = new OrderService(new MockPaymentProcessor(true));

        stripeService.placeOrder("user_001", 999.00);
        razorpayService.placeOrder("user_002", 1499.50);
        testService.placeOrder("user_test", 100.00);

        // Static method on interface
        try {
            PaymentProcessor.validateAmount(-50);
        } catch (IllegalArgumentException e) {
            System.out.println("\nValidation: " + e.getMessage());
        }

        // Default method in action
        MockPaymentProcessor mock = new MockPaymentProcessor(false);
        System.out.println("Processor name: " + mock.getProcessorName());  // "Unknown Processor"

        /*
         * OUTPUT:
         * Placing order via Stripe
         * [Stripe] Charging user=user_001 amount=999.00
         * Order placed!
         *
         * Placing order via Razorpay
         * [Razorpay] Processing user=user_002 amount=1499.50
         * Order placed!
         *
         * Placing order via Unknown Processor
         * [MOCK] Payment SUCCESS user=user_test amount=100.00
         * Order placed!
         *
         * Validation: Payment amount must be positive, got: -50.0
         * Processor name: Unknown Processor
         */
    }
}
```

---

### 2.2 Abstract Class — Related family with shared code/state

#### Subtopic: Template Method Pattern

When a method in an abstract class calls abstract methods, that is the **Template Method Pattern** — the parent defines the algorithm structure, child classes fill in the variable steps.

```java
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

abstract class Report {

    // SHARED STATE — all subclasses inherit this
    protected final String companyName;
    protected final LocalDate generatedDate;
    protected final String reportId;

    protected Report(String companyName) {
        if (companyName == null || companyName.isEmpty())
            throw new IllegalArgumentException("Company name cannot be empty");
        this.companyName   = companyName;
        this.generatedDate = LocalDate.now();
        this.reportId      = "RPT-" + System.currentTimeMillis();
    }

    // TEMPLATE METHOD: defines the algorithm structure — final so no one can change the order
    public final void generate() {
        printHeader();     // Common — same for ALL
        generateBody();    // Different — each subclass defines this
        printSummary();    // Hook — subclasses CAN override, but don't HAVE to
        printFooter();     // Common — same for ALL
    }

    private void printHeader() {
        String border = "=".repeat(50);
        System.out.println(border);
        System.out.println("Company : " + companyName);
        System.out.println("Report  : " + reportId);
        System.out.println("Date    : " + generatedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        System.out.println(border);
    }

    private void printFooter() {
        System.out.println("=".repeat(50));
        System.out.println("CONFIDENTIAL — End of Report");
        System.out.println("=".repeat(50) + "\n");
    }

    // Hook method: has a default, but subclasses can override
    protected void printSummary() {
        System.out.println("[No summary section]");
    }

    // Abstract method: subclasses MUST implement
    protected abstract void generateBody();

    // Shared utility available to all subclasses
    protected void printLine(String label, Object value) {
        System.out.printf("  %-20s : %s%n", label, value);
    }
}

class SalesReport extends Report {
    private final double totalSales;
    private final double targetSales;
    private final List<String> topProducts;

    public SalesReport(String company, double totalSales, double target, List<String> topProducts) {
        super(company);
        if (totalSales < 0) throw new IllegalArgumentException("Sales cannot be negative");
        this.totalSales  = totalSales;
        this.targetSales = target;
        this.topProducts = new ArrayList<>(topProducts);
    }

    @Override
    protected void generateBody() {
        System.out.println("  [ SALES REPORT ]");
        printLine("Total Sales",  String.format("Rs %.2f", totalSales));
        printLine("Target",       String.format("Rs %.2f", targetSales));
        printLine("Achievement",  String.format("%.1f%%", (totalSales / targetSales) * 100));
        System.out.println("  Top Products:");
        topProducts.forEach(p -> System.out.println("    - " + p));
    }

    @Override
    protected void printSummary() {  // Override hook method
        boolean met = totalSales >= targetSales;
        System.out.println("  SUMMARY: Target " + (met ? "MET" : "NOT MET") +
            " | Delta: Rs " + String.format("%.2f", Math.abs(totalSales - targetSales)));
    }
}

class InventoryReport extends Report {
    private final int totalItems;
    private final int lowStockCount;
    private final int outOfStockCount;

    public InventoryReport(String company, int totalItems, int lowStock, int outOfStock) {
        super(company);
        if (totalItems < 0) throw new IllegalArgumentException("Items cannot be negative");
        this.totalItems      = totalItems;
        this.lowStockCount   = lowStock;
        this.outOfStockCount = outOfStock;
    }

    @Override
    protected void generateBody() {
        System.out.println("  [ INVENTORY REPORT ]");
        printLine("Total SKUs",       totalItems);
        printLine("Low Stock Alerts", lowStockCount);
        printLine("Out of Stock",     outOfStockCount);
        printLine("Healthy Stock",    totalItems - lowStockCount - outOfStockCount);
    }
    // Does NOT override printSummary() → uses default "[No summary section]"
}

public class ReportDemo {
    public static void main(String[] args) {

        Report salesReport = new SalesReport(
            "Flipkart India",
            4_750_000.00,
            5_000_000.00,
            List.of("iPhone 15", "Samsung S24", "OnePlus 12")
        );

        Report inventoryReport = new InventoryReport(
            "Flipkart India", 12_450, 340, 87
        );

        salesReport.generate();
        inventoryReport.generate();

        // You CANNOT instantiate Report directly
        // Report r = new Report("X");  → COMPILE ERROR: abstract class

        /*
         * OUTPUT:
         * ==================================================
         * Company : Flipkart India
         * Report  : RPT-...
         * Date    : 16 Jun 2025
         * ==================================================
         *   [ SALES REPORT ]
         *   Total Sales          : Rs 4750000.00
         *   Target               : Rs 5000000.00
         *   Achievement          : 95.0%
         *   Top Products:
         *     - iPhone 15
         *     - Samsung S24
         *     - OnePlus 12
         *   SUMMARY: Target NOT MET | Delta: Rs 250000.00
         * ==================================================
         * CONFIDENTIAL — End of Report
         * ==================================================
         */
    }
}
```

> **Why NOT interface here?** Header and footer code is identical across all reports. Interface = copy-paste this in every class = violates DRY.
>
> **Why NOT concrete class?** You don't want anyone creating a plain `Report()` — it makes no sense without a body.

#### Abstract class with shared state

```java
abstract class Vehicle {

    // SHARED STATE — interfaces cannot have this
    protected final String brand;
    protected int currentSpeed;
    protected String fuelType;
    protected boolean isEngineOn;
    protected int odometer;

    protected Vehicle(String brand, String fuelType) {
        if (brand == null || brand.isEmpty())
            throw new IllegalArgumentException("Brand cannot be empty");
        this.brand      = brand;
        this.fuelType   = fuelType;
        this.isEngineOn = false;
    }

    // SHARED BEHAVIOR
    public void refuel() {
        if (isEngineOn) throw new IllegalStateException("Turn off engine before refueling!");
        System.out.println(brand + ": Refueling with " + fuelType);
    }

    public void accelerate(int kmh) {
        if (!isEngineOn) throw new IllegalStateException("Start engine first!");
        if (kmh <= 0) throw new IllegalArgumentException("Acceleration must be positive");
        currentSpeed += kmh;
        odometer += kmh / 10;
        System.out.printf("%s: Accelerated to %d km/h%n", brand, currentSpeed);
    }

    // ABSTRACT — each vehicle starts differently
    public abstract void start();
    public abstract void stop();
    public abstract String getVehicleType();
}

class PetrolCar extends Vehicle {
    public PetrolCar(String brand) { super(brand, "Petrol"); }

    @Override public void start() {
        if (isEngineOn) throw new IllegalStateException("Already started");
        isEngineOn = true;
        System.out.println(brand + ": Turn key → Vroom!");
    }
    @Override public void stop() {
        isEngineOn   = false;
        currentSpeed = 0;
        System.out.println(brand + ": Engine off");
    }
    @Override public String getVehicleType() { return "PETROL_CAR"; }
}

class ElectricCar extends Vehicle {
    private int batteryPercent;

    public ElectricCar(String brand, int initialBattery) {
        super(brand, "Electric");
        if (initialBattery < 0 || initialBattery > 100)
            throw new IllegalArgumentException("Battery must be 0-100%");
        this.batteryPercent = initialBattery;
    }

    @Override public void start() {
        if (batteryPercent == 0) throw new IllegalStateException("Battery dead!");
        isEngineOn = true;
        System.out.println(brand + ": Press button → Silently starts (battery: " + batteryPercent + "%)");
    }
    @Override public void stop() {
        isEngineOn      = false;
        currentSpeed    = 0;
        batteryPercent  = Math.min(100, batteryPercent + 5); // regen braking
        System.out.println(brand + ": Stopped. Regen braking → battery now " + batteryPercent + "%");
    }
    @Override public String getVehicleType() { return "ELECTRIC_CAR"; }
}

public class VehicleDemo {
    public static void main(String[] args) {

        Vehicle petrol   = new PetrolCar("Maruti Swift");
        Vehicle electric = new ElectricCar("Tata Nexon EV", 80);

        petrol.start();
        petrol.accelerate(60);
        petrol.stop();
        petrol.refuel();

        electric.start();
        electric.accelerate(80);
        electric.stop();

        // Polymorphism with abstract class
        java.util.List<Vehicle> garage = java.util.List.of(petrol, electric);
        garage.forEach(v -> System.out.println(v.getVehicleType() + ": " + v.brand));

        /*
         * OUTPUT:
         * Maruti Swift: Turn key → Vroom!
         * Maruti Swift: Accelerated to 60 km/h
         * Maruti Swift: Engine off
         * Maruti Swift: Refueling with Petrol
         * Tata Nexon EV: Press button → Silently starts (battery: 80%)
         * Tata Nexon EV: Accelerated to 80 km/h
         * Tata Nexon EV: Stopped. Regen braking → battery now 85%
         */
    }
}
```

---

## 3. All OOPS Concepts with Runnable Examples

### 3.1 Encapsulation — Hide the data, control the access

#### Subtopic: Getter vs Setter — when to use each

A **getter** provides read access. A **setter** provides write access with validation. Key insight: sometimes you need a getter but **no setter** — `balance` should be readable but only changeable via `deposit()` and `withdraw()`.

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// BAD: No encapsulation
class BankAccountBad {
    public double balance;    // Anyone can set balance = -99999!
    public boolean isActive;
}

// GOOD: Fully encapsulated
class BankAccount {

    private final String accountId;  // final = can never change
    private double balance;
    private boolean isActive;
    private final String ownerId;
    private final List<String> transactions;

    public BankAccount(String accountId, String ownerId, double initialDeposit) {
        if (accountId == null || accountId.isEmpty())
            throw new IllegalArgumentException("Account ID required");
        if (ownerId == null)
            throw new IllegalArgumentException("Owner ID required");
        if (initialDeposit < 0)
            throw new IllegalArgumentException("Initial deposit cannot be negative");

        this.accountId    = accountId;
        this.ownerId      = ownerId;
        this.balance      = initialDeposit;
        this.isActive     = true;
        this.transactions = new ArrayList<>();
        if (initialDeposit > 0)
            transactions.add("OPEN: deposited Rs " + initialDeposit);
    }

    public void deposit(double amount) {
        if (!isActive) throw new IllegalStateException("Account is closed");
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");

        balance += amount;
        transactions.add("DEPOSIT: +" + amount + " | Balance: " + balance);
        System.out.printf("[%s] Deposited Rs %.2f | Balance: Rs %.2f%n", accountId, amount, balance);
    }

    public void withdraw(double amount) {
        if (!isActive) throw new IllegalStateException("Account is closed");
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        if (amount > balance) throw new IllegalArgumentException(
            String.format("Insufficient funds: requested=%.2f available=%.2f", amount, balance));

        balance -= amount;
        transactions.add("WITHDRAW: -" + amount + " | Balance: " + balance);
        System.out.printf("[%s] Withdrew Rs %.2f | Balance: Rs %.2f%n", accountId, amount, balance);
    }

    public void close() {
        if (!isActive) throw new IllegalStateException("Account already closed");
        if (balance > 0) throw new IllegalStateException(
            "Cannot close account with positive balance. Withdraw Rs " + balance + " first.");
        isActive = false;
        transactions.add("ACCOUNT CLOSED");
    }

    // Getters — read-only controlled access
    public double getBalance()   { return balance; }
    public boolean isActive()    { return isActive; }
    public String getAccountId() { return accountId; }

    // Return unmodifiable view — caller cannot modify the internal list
    public List<String> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // NO setBalance() — balance can ONLY change via deposit() / withdraw()
}

public class EncapsulationDemo {
    public static void main(String[] args) {

        BankAccount acc = new BankAccount("ACC-001", "USER-42", 1000.00);
        acc.deposit(500.00);
        acc.withdraw(200.00);

        System.out.println("\nBalance: Rs " + acc.getBalance());

        // Invalid operations
        try { acc.deposit(-100); } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }
        try { acc.withdraw(10000); } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Try to modify transaction list — BLOCKED
        try {
            acc.getTransactions().add("HACK: +99999");
        } catch (UnsupportedOperationException e) {
            System.out.println("Caught: Cannot modify transaction list externally");
        }

        acc.withdraw(1300.00);
        acc.close();

        System.out.println("\nTransaction history:");
        acc.getTransactions().forEach(t -> System.out.println("  " + t));
    }
}
```

---

### 3.2 Inheritance — IS-A relationship

#### Subtopic: Method Overriding vs Method Overloading

| Feature | Overriding | Overloading |
|---|---|---|
| What it is | Child redefines a parent method | Same class, same name, different params |
| Happens at | Runtime (dynamic dispatch) | Compile time (static binding) |
| Annotation | `@Override` (use it always!) | No annotation |
| OOP concept | Runtime Polymorphism | Compile-time Polymorphism |

```java
class Animal {
    protected final String name;
    protected final int age;

    public Animal(String name, int age) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name required");
        if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
        this.name = name;
        this.age  = age;
    }

    // Will be OVERRIDDEN by subclasses
    public void makeSound() {
        System.out.println(name + " makes a generic animal sound");
    }

    // OVERLOADING: same name, different params
    public void eat(String food)                    { System.out.println(name + " eats " + food); }
    public void eat(String food, int quantity)      { System.out.println(name + " eats " + quantity + " units of " + food); }
    public void eat(String food, boolean isCooked)  { System.out.println(name + " eats " + (isCooked ? "cooked" : "raw") + " " + food); }

    public void breathe() { System.out.println(name + " breathes"); }
}

class Dog extends Animal {
    private final String breed;

    public Dog(String name, int age, String breed) {
        super(name, age);
        this.breed = breed;
    }

    @Override
    public void makeSound() {
        System.out.println(name + " barks: Woof! Woof!");
    }

    public void fetch(String item) { System.out.println(name + " fetches the " + item); }
    public String getBreed() { return breed; }
}

class GoldenRetriever extends Dog {
    private final boolean isGuide;

    public GoldenRetriever(String name, int age, boolean isGuide) {
        super(name, age, "Golden Retriever");
        this.isGuide = isGuide;
    }

    @Override
    public void makeSound() {
        if (isGuide) System.out.println(name + " (guide dog) softly whimpers to alert");
        else         System.out.println(name + " happily barks: Woof woof! Tail wagging!");
    }

    public void makeNormalSound() {
        System.out.print("Normal bark: ");
        super.makeSound();  // Calls Dog's makeSound()
    }
}

class Cat extends Animal {
    private final boolean isIndoor;
    public Cat(String name, int age, boolean isIndoor) {
        super(name, age);
        this.isIndoor = isIndoor;
    }
    @Override public void makeSound() {
        System.out.println(name + (isIndoor ? " purrs softly" : " hisses!"));
    }
}

public class InheritanceDemo {
    public static void main(String[] args) {

        System.out.println("=== makeSound() — Method Overriding ===");
        Animal[] animals = {
            new Animal("Generic", 5),
            new Dog("Bruno", 3, "Labrador"),
            new GoldenRetriever("Max", 4, false),
            new GoldenRetriever("Buddy", 6, true),
            new Cat("Whiskers", 2, true)
        };

        for (Animal a : animals) {
            System.out.print(a.getClass().getSimpleName() + ": ");
            a.makeSound();  // Runtime dispatch — calls most specific version
        }

        System.out.println("\n=== eat() — Method Overloading ===");
        Dog dog = new Dog("Bruno", 3, "Labrador");
        dog.eat("bone");
        dog.eat("kibble", 3);
        dog.eat("chicken", false);

        System.out.println("\n=== super.makeSound() ===");
        new GoldenRetriever("Max", 4, false).makeNormalSound();

        System.out.println("\n=== instanceof with pattern matching (Java 16+) ===");
        for (Animal a : animals) {
            if (a instanceof Dog d) {
                System.out.println(d.name + " is a Dog. Breed: " + d.getBreed());
            }
        }
    }
}
```

---

### 3.3 Composition — HAS-A (prefer over inheritance)

| Use Inheritance when... | Use Composition when... |
|---|---|
| IS-A relationship is TRUE and stable | HAS-A relationship |
| Subclass needs ALL parent behavior | You need to reuse just SOME behavior |
| Modeling a family of types | You want to swap components at runtime |

```java
class Engine {
    private final String type;
    private final int horsepower;
    private boolean isRunning;

    public Engine(String type, int horsepower) {
        if (horsepower <= 0) throw new IllegalArgumentException("Horsepower must be positive");
        this.type       = type;
        this.horsepower = horsepower;
    }

    public void start() {
        if (isRunning) throw new IllegalStateException("Engine already running");
        isRunning = true;
        System.out.println("  Engine started: " + horsepower + "HP " + type);
    }

    public void stop() {
        if (!isRunning) throw new IllegalStateException("Engine already stopped");
        isRunning = false;
        System.out.println("  Engine stopped");
    }

    public boolean isRunning() { return isRunning; }
    public String getType()    { return type; }
    public int getHorsepower() { return horsepower; }
}

class FuelTank {
    private final double capacity;
    private double current;

    public FuelTank(double capacity, double initial) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        if (initial < 0 || initial > capacity) throw new IllegalArgumentException("Initial must be 0 to capacity");
        this.capacity = capacity;
        this.current  = initial;
    }

    public void consume(double litres) {
        if (litres > current) throw new IllegalStateException("Not enough fuel!");
        current -= litres;
    }

    public boolean isEmpty()   { return current == 0; }
    public double getPercent() { return (current / capacity) * 100; }
}

// Car COMPOSES multiple components — not inherits
class Car {
    private final String model;
    private final Engine engine;      // Car HAS-A Engine
    private final FuelTank fuelTank;  // Car HAS-A FuelTank
    private int speed;

    // Dependency Injection — components passed in (testable!)
    public Car(String model, Engine engine, FuelTank fuelTank) {
        if (model == null)    throw new IllegalArgumentException("Model required");
        if (engine == null)   throw new IllegalArgumentException("Engine required");
        if (fuelTank == null) throw new IllegalArgumentException("FuelTank required");
        this.model    = model;
        this.engine   = engine;
        this.fuelTank = fuelTank;
    }

    public void start() {
        if (fuelTank.isEmpty()) throw new IllegalStateException("Empty tank! Refuel first.");
        engine.start();
        System.out.println(model + ": Ready to drive!");
    }

    public void accelerate(int kmh) {
        if (!engine.isRunning()) throw new IllegalStateException("Start car first");
        fuelTank.consume(kmh * 0.05);
        speed += kmh;
        System.out.printf("%s: Speed = %d km/h | Fuel: %.1f%%%n", model, speed, fuelTank.getPercent());
    }

    public void stop() {
        engine.stop();
        speed = 0;
        System.out.println(model + ": Parked");
    }
}

public class CompositionDemo {
    public static void main(String[] args) {

        // Petrol car
        Car swift = new Car(
            "Maruti Swift",
            new Engine("petrol", 90),
            new FuelTank(37, 20)
        );

        // EV — DIFFERENT engine, same Car class, no new inheritance hierarchy
        Car nexon = new Car(
            "Tata Nexon EV",
            new Engine("electric", 143),
            new FuelTank(100, 80)
        );

        swift.start();
        swift.accelerate(60);
        swift.stop();

        nexon.start();
        nexon.accelerate(80);
        nexon.stop();

        /*
         * KEY INSIGHT: We built two different cars by swapping Engine.
         * To make a diesel car? Just pass new Engine("diesel", 100).
         * No inheritance chain needed. Much more flexible.
         */
    }
}
```

> **In Interview Say:** *"I prefer composition over inheritance here because it gives more flexibility to swap components and avoids tight coupling. This also makes testing easier — I can inject a MockEngine without changing the Car class."*

---

### 3.4 Polymorphism — Same call, different behavior

```java
import java.util.List;
import java.util.ArrayList;

interface NotificationSender {
    void send(String userId, String message);
    String getChannelType();

    default boolean validate(String userId, String message) {
        if (userId == null || userId.isEmpty()) {
            System.out.println("Invalid userId — skipping");
            return false;
        }
        if (message == null || message.isEmpty()) {
            System.out.println("Empty message — skipping");
            return false;
        }
        return true;
    }
}

class EmailNotification implements NotificationSender {
    private final String from;
    public EmailNotification(String from) { this.from = from; }

    @Override public void send(String userId, String message) {
        if (!validate(userId, message)) return;
        System.out.printf("[EMAIL] From: %s | To: %s | Msg: %s%n", from, userId, message);
    }
    @Override public String getChannelType() { return "EMAIL"; }
}

class SMSNotification implements NotificationSender {
    @Override public void send(String userId, String message) {
        if (!validate(userId, message)) return;
        String trimmed = message.length() > 160 ? message.substring(0, 157) + "..." : message;
        System.out.printf("[SMS] To: %s | Msg: %s%n", userId, trimmed);
    }
    @Override public String getChannelType() { return "SMS"; }
}

class PushNotification implements NotificationSender {
    @Override public void send(String userId, String message) {
        if (!validate(userId, message)) return;
        System.out.printf("[PUSH] To: %s | Alert: %s%n", userId, message);
    }
    @Override public String getChannelType() { return "PUSH"; }
}

class NotificationService {
    private final List<NotificationSender> senders;

    public NotificationService(List<NotificationSender> senders) {
        if (senders == null || senders.isEmpty()) throw new IllegalArgumentException("Need at least one sender");
        this.senders = new ArrayList<>(senders);
    }

    public void notifyUser(String userId, String message) {
        System.out.println("\n--- Notifying userId=" + userId + " ---");
        for (NotificationSender sender : senders) {
            try {
                sender.send(userId, message);
            } catch (Exception e) {
                // One failing channel must not block others
                System.out.println("WARNING: " + sender.getChannelType() + " failed: " + e.getMessage());
            }
        }
    }

    public void addSender(NotificationSender sender) { senders.add(sender); }
}

public class PolymorphismDemo {
    public static void main(String[] args) {

        NotificationService service = new NotificationService(List.of(
            new EmailNotification("noreply@zomato.com"),
            new SMSNotification(),
            new PushNotification()
        ));

        service.notifyUser("USER_123", "Your order has been placed!");
        service.notifyUser("USER_456", "Delivery arriving in 5 minutes");
        service.notifyUser("", "Hello");     // Invalid userId
        service.notifyUser("USER_789", ""); // Empty message

        /*
         * OUTPUT:
         * --- Notifying userId=USER_123 ---
         * [EMAIL] From: noreply@zomato.com | To: USER_123 | Msg: Your order has been placed!
         * [SMS] To: USER_123 | Msg: Your order has been placed!
         * [PUSH] To: USER_123 | Alert: Your order has been placed!
         *
         * --- Notifying userId= ---
         * Invalid userId — skipping
         * Invalid userId — skipping
         * Invalid userId — skipping
         */
    }
}
```

---

## 4. Exception Handling — Build a Hierarchy

Never throw raw `Exception` or `RuntimeException` in production code. Build a hierarchy that lets callers catch at exactly the right level of granularity.

### Checked vs Unchecked

| Type | Extends | Must declare in `throws`? | When to use |
|---|---|---|---|
| Checked | `Exception` | Yes | External failures caller MUST handle: file not found, DB down |
| Unchecked | `RuntimeException` | No | Programming bugs / bad input: null pointer, bad args, illegal state |

> **LLD Interview Standard:** Almost always use `RuntimeException` (unchecked). The one exception: methods that interact with external systems (file I/O, database, network) where callers need to plan for failure.

### Subtopic: Exception Chaining

Always pass `cause` when re-wrapping — `new SomeException("msg", cause)`. Without it you lose the root cause and the original stack trace.

```java
import java.time.LocalDateTime;

// --- LEVEL 1: Application base ---
class AppException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;
    private final LocalDateTime timestamp;

    public AppException(String technicalMsg, String errorCode, String userMessage) {
        super(technicalMsg);
        this.errorCode   = errorCode;
        this.userMessage = userMessage;
        this.timestamp   = LocalDateTime.now();
    }

    // Overload: include cause for exception chaining
    public AppException(String technicalMsg, String errorCode, String userMessage, Throwable cause) {
        super(technicalMsg, cause);  // Chain the original exception!
        this.errorCode   = errorCode;
        this.userMessage = userMessage;
        this.timestamp   = LocalDateTime.now();
    }

    public String getErrorCode()   { return errorCode; }
    public String getUserMessage() { return userMessage; }
}

// --- LEVEL 2: Domain exceptions ---
class PaymentException extends AppException {
    public PaymentException(String msg) {
        super(msg, "PAYMENT_ERROR", "Payment could not be processed");
    }
    public PaymentException(String msg, Throwable cause) {
        super(msg, "PAYMENT_ERROR", "Payment could not be processed", cause);
    }
}

class OrderException extends AppException {
    public OrderException(String msg) { super(msg, "ORDER_ERROR", "Order processing failed"); }
}

class UserException extends AppException {
    public UserException(String msg)  { super(msg, "USER_ERROR", "User operation failed"); }
}

// --- LEVEL 3: Specific exceptions ---
class InsufficientFundsException extends PaymentException {
    private final double required;
    private final double available;

    public InsufficientFundsException(double required, double available) {
        super(String.format("Insufficient funds: required=%.2f available=%.2f", required, available));
        this.required  = required;
        this.available = available;
    }

    public double getRequired()  { return required; }
    public double getAvailable() { return available; }
    public double getShortfall() { return required - available; }
}

class PaymentGatewayException extends PaymentException {
    private final String gatewayName;
    public PaymentGatewayException(String gatewayName, String reason, Throwable cause) {
        super("Gateway " + gatewayName + " failed: " + reason, cause);
        this.gatewayName = gatewayName;
    }
    public String getGatewayName() { return gatewayName; }
}

class OrderNotFoundException extends OrderException {
    private final String orderId;
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }
    public String getOrderId() { return orderId; }
}

class UserNotFoundException extends UserException {
    public UserNotFoundException(String userId) { super("User not found: " + userId); }
}

// --- Service showing catch-at-the-right-level ---
class PaymentService {

    public boolean processPayment(String userId, double amount, String method) {

        // Validate inputs FIRST — before any logic
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId required");
        if (amount <= 0)    throw new IllegalArgumentException("Amount must be positive");
        if (method == null) throw new IllegalArgumentException("Payment method required");

        System.out.printf("Processing: userId=%s amount=%.2f method=%s%n", userId, amount, method);

        try {
            return callGateway(userId, amount, method);

        } catch (InsufficientFundsException e) {
            System.err.printf("Payment failed (insufficient funds): shortfall=%.2f%n", e.getShortfall());
            throw e;  // Re-throw — let caller handle specifically

        } catch (PaymentGatewayException e) {
            System.err.println("Gateway failure: " + e.getGatewayName() + " | cause: " + e.getCause().getMessage());
            throw e;

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            // WRAP + CHAIN original — preserves root cause stack trace
            throw new PaymentException("Unexpected payment failure for userId=" + userId, e);
        }
    }

    private boolean callGateway(String userId, double amount, String method) {
        if (amount > 50000) throw new InsufficientFundsException(amount, 50000);
        if (method.equals("INVALID")) {
            RuntimeException sdkEx = new RuntimeException("SDK: gateway timeout");
            throw new PaymentGatewayException("Stripe", "Connection timeout", sdkEx);
        }
        System.out.println("Gateway: approved!");
        return true;
    }
}

public class ExceptionHierarchyDemo {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();

        System.out.println("=== Happy Path ===");
        service.processPayment("USER_1", 999.00, "UPI");

        System.out.println("\n=== Insufficient Funds ===");
        try {
            service.processPayment("USER_2", 75000.00, "CARD");
        } catch (InsufficientFundsException e) {
            System.out.println("Caught (specific): shortfall=Rs " + e.getShortfall());
            System.out.println("User message: " + e.getUserMessage());
        }

        System.out.println("\n=== Gateway Failure ===");
        try {
            service.processPayment("USER_3", 1000.00, "INVALID");
        } catch (PaymentGatewayException e) {
            System.out.println("Gateway: " + e.getGatewayName());
            System.out.println("Root cause: " + e.getCause().getMessage());
        }

        System.out.println("\n=== Invalid Input ===");
        try {
            service.processPayment(null, 500, "UPI");
        } catch (IllegalArgumentException e) {
            System.out.println("Bad input: " + e.getMessage());
        }
    }
}
```

### Edge Cases Checklist

Memorize this. Go through it out loud in every interview after writing a method.

| Check | Pattern | Exception |
|---|---|---|
| Null input | `if (x == null) throw ...` | `IllegalArgumentException` |
| Empty string / list | `if (s.isEmpty()) throw ...` | `IllegalArgumentException` |
| Negative / zero number | `if (amount <= 0) throw ...` | `IllegalArgumentException` |
| Invalid state | `if (!account.isActive()) throw ...` | `IllegalStateException` |
| Duplicate entry | `if (map.containsKey(id)) throw ...` | Custom `DuplicateXxxException` |
| Concurrent access | Use `ConcurrentHashMap`, `synchronized` | Prevent — don't let it happen |
| External service failure | `try { call() } catch (Exception e) { wrap }` | Custom `ServiceException` |
| Max limit exceeded | `if (items.size() >= MAX) throw ...` | `IllegalStateException` |

---

## 5. Logging — What, When, How

### Log Levels

| Level | Use For | Example |
|---|---|---|
| `DEBUG` | Dev-only detail — variable values, method traces | `Entering processOrder | orderId=X items=[A,B]` |
| `INFO` | Normal business operations | `Order created | orderId=X userId=Y total=999` |
| `WARN` | Unusual but handled — retry, fallback | `Payment retry attempt 2/3 | userId=X` |
| `ERROR` | Failures needing attention | `Order creation failed | userId=X | NullPointerException` |

### What NOT to Log

```
NEVER LOG:
  - Passwords, PINs, OTPs
  - Credit card numbers, CVV
  - Auth tokens, API keys, session tokens
  - Aadhaar, PAN, SSN, government IDs
  - Full name + phone + email together (PII combination)
```

### Complete Logging Example

```java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Simulates SLF4J Logger (replace with real Logger in production)
class Logger {
    private final String name;
    public static Logger getLogger(Class<?> c) { return new Logger(c.getSimpleName()); }
    private Logger(String name) { this.name = name; }

    public void debug(String msg, Object... args) { log("DEBUG", msg, args); }
    public void info (String msg, Object... args) { log("INFO ", msg, args); }
    public void warn (String msg, Object... args) { log("WARN ", msg, args); }
    public void error(String msg, Object... args) { log("ERROR", msg, args); }

    private void log(String level, String msg, Object[] args) {
        String out = msg;
        for (Object a : args) {
            int i = out.indexOf("{}");
            if (i >= 0) out = out.substring(0, i) + a + out.substring(i + 2);
        }
        System.out.printf("[%s] [%s] %s%n", level, name, out);
    }
}

class OrderItem {
    final String itemId;
    final double price;
    final int quantity;
    public OrderItem(String id, double p, int q) { itemId=id; price=p; quantity=q; }
    public double getTotal() { return price * quantity; }
}

class CreatedOrder {
    public final String orderId;
    public final String userId;
    public final double total;
    public CreatedOrder(String userId, double total) {
        this.orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.userId  = userId;
        this.total   = total;
    }
}

class LoggingOrderService {

    // Logger declared as static final — one per class
    private static final Logger log = Logger.getLogger(LoggingOrderService.class);

    private final ConcurrentHashMap<String, CreatedOrder> orders = new ConcurrentHashMap<>();

    public CreatedOrder createOrder(String userId, List<OrderItem> items, String paymentMethod) {

        // LOG ENTRY: who (userId), what (itemCount), how (paymentMethod)
        log.info("Creating order | userId={} itemCount={} paymentMethod={}", userId, items.size(), paymentMethod);

        long startMs = System.currentTimeMillis();

        try {
            validateInputs(userId, items, paymentMethod);

            double total = items.stream().mapToDouble(OrderItem::getTotal).sum();
            log.debug("Total calculated | userId={} total={}", userId, total);

            boolean paid = simulatePayment(userId, total, paymentMethod);
            if (!paid) {
                log.warn("Order not created — payment failed | userId={} total={}", userId, total);
                throw new RuntimeException("Payment unsuccessful");
            }

            CreatedOrder order = new CreatedOrder(userId, total);
            orders.put(order.orderId, order);

            long ms = System.currentTimeMillis() - startMs;
            log.info("Order created | userId={} orderId={} total={} duration={}ms", userId, order.orderId, total, ms);
            return order;

        } catch (IllegalArgumentException e) {
            log.warn("Order rejected — invalid input | userId={} reason={}", userId, e.getMessage());
            throw e;

        } catch (Exception e) {
            // ERROR with stack trace — last arg is the exception
            log.error("Order creation failed | userId={} error={}", userId, e.getMessage());
            throw new RuntimeException("Failed to create order for userId=" + userId);
        }
    }

    private void validateInputs(String userId, List<OrderItem> items, String method) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Items cannot be empty");
        if (method == null) throw new IllegalArgumentException("Payment method required");
        if (items.size() > 50) throw new IllegalArgumentException("Max 50 items per order");
        log.debug("Inputs validated | userId={} items={}", userId, items.size());
    }

    private boolean simulatePayment(String userId, double amount, String method) {
        if (method.equals("FAIL")) {
            log.warn("Payment simulation: forced failure | userId={}", userId);
            return false;
        }
        return true;
    }
}

public class LoggingDemo {
    public static void main(String[] args) {

        LoggingOrderService service = new LoggingOrderService();

        List<OrderItem> items = List.of(
            new OrderItem("PIZZA_001", 299.0, 2),
            new OrderItem("COKE_001",   49.0, 3),
            new OrderItem("GARLIC",     79.0, 1)
        );

        System.out.println("=== Happy Path ===");
        CreatedOrder order = service.createOrder("USER_42", items, "UPI");
        System.out.println("Created: " + order.orderId + " | Total: " + order.total);

        System.out.println("\n=== Payment Failure ===");
        try {
            service.createOrder("USER_43", items, "FAIL");
        } catch (RuntimeException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        System.out.println("\n=== Invalid Input ===");
        try {
            service.createOrder("", items, "UPI");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}
```

---

## 6. Complete LLD — Food Delivery App

Combines everything: State machines, Interfaces, Abstract classes, Composition, Exceptions, Logging — in one runnable example.

### Step 1: Clarifying Questions (say these out loud in the interview)

| Category | Question |
|---|---|
| Scale | Single city or multi-city? Real-time tracking needed? |
| Payments | UPI, Card, COD, Wallet — which to support? |
| Cancellation | Can orders be cancelled after pickup? |
| Reviews | Restaurants and agents separately? |
| Focus | Which component to deep-dive — Order, Payment, or User? |

### Step 2: Entity Map

| Entity | Type | Key Relationships |
|---|---|---|
| `User` | Abstract Class | Parent of Customer, RestaurantOwner, DeliveryAgent |
| `Customer` | Concrete Class | `implements Trackable`, has `List<Order>` |
| `DeliveryAgent` | Concrete Class | `implements Trackable + Reviewable` |
| `Restaurant` | Concrete Class | `implements Reviewable`, has `List<MenuItem>` |
| `Order` | Concrete Class (State Machine) | belongs to Customer, Restaurant, DeliveryAgent |
| `Payment` | Interface (Strategy) | `UPIPayment`, `CardPayment`, `CashOnDelivery` |
| `Notification` | Interface (Observer) | `EmailNotification`, `SMSNotification` |

### Step 3: Complete Runnable Code

```java
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// ================================================================
// FOOD DELIVERY APP — Full LLD (all in one runnable file)
// ================================================================

// ─── INTERFACES ──────────────────────────────────────────────────

interface Trackable {
    String getCurrentLocation();
    void updateLocation(double lat, double lng);
}

interface Payable {
    boolean pay(String userId, double amount);
    boolean refund(String transactionId, double amount);
    String getMethodName();
}

interface NotificationSenderFD {
    void send(String userId, String message);
    String getChannelName();
}

interface Reviewable {
    void addReview(String reviewerId, int rating, String comment);
    double getAverageRating();
    int getReviewCount();
}

// ─── EXCEPTIONS ──────────────────────────────────────────────────

class AppException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;
    public AppException(String msg, String code, String userMsg) {
        super(msg); this.errorCode = code; this.userMessage = userMsg;
    }
    public String getErrorCode()   { return errorCode; }
    public String getUserMessage() { return userMessage; }
}
class PaymentException    extends AppException { public PaymentException(String m) { super(m,"PAYMENT_ERROR","Payment failed"); } }
class OrderException      extends AppException { public OrderException(String m)   { super(m,"ORDER_ERROR","Order failed"); } }
class UserException       extends AppException { public UserException(String m)    { super(m,"USER_ERROR","User error"); } }
class InsufficientFundsException extends PaymentException {
    final double required, available;
    public InsufficientFundsException(double r, double a) {
        super(String.format("Insufficient funds: required=%.2f available=%.2f", r, a));
        required=r; available=a;
    }
    public double getShortfall() { return required - available; }
}
class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(String id) { super("Order not found: " + id); }
}
class UserNotFoundException extends UserException {
    public UserNotFoundException(String id) { super("User not found: " + id); }
}

// ─── LOGGER (simulated SLF4J) ────────────────────────────────────

class Logger {
    private final String name;
    public static Logger getLogger(Class<?> c) { return new Logger(c.getSimpleName()); }
    private Logger(String name) { this.name = name; }
    public void info (String m, Object... a) { log("INFO ", m, a); }
    public void warn (String m, Object... a) { log("WARN ", m, a); }
    public void error(String m, Object... a) { log("ERROR", m, a); }
    private void log(String lvl, String msg, Object[] args) {
        String out = msg;
        for (Object a : args) { int i = out.indexOf("{}"); if (i>=0) out = out.substring(0,i)+a+out.substring(i+2); }
        System.out.printf("[%s] [%s] %s%n", lvl, name, out);
    }
}

// ─── ABSTRACT: User ──────────────────────────────────────────────

abstract class FDUser {
    protected final String userId;
    protected String name;
    protected boolean isActive;

    protected FDUser(String userId, String name) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId required");
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name required");
        this.userId   = userId;
        this.name     = name;
        this.isActive = true;
    }

    public abstract String getUserType();
    public abstract boolean canPerformAction(String action);

    public String getUserId() { return userId; }
    public String getName()   { return name; }
    public boolean isActive() { return isActive; }
}

// ─── Customer ────────────────────────────────────────────────────

class FDCustomer extends FDUser implements Trackable {
    private double lat, lng;
    private final List<String> orderIds = new ArrayList<>();

    public FDCustomer(String userId, String name) { super(userId, name); }

    @Override public String getCurrentLocation()         { return String.format("%.4f,%.4f", lat, lng); }
    @Override public void updateLocation(double la, double ln) { lat=la; lng=ln; }
    @Override public String getUserType()                { return "CUSTOMER"; }
    @Override public boolean canPerformAction(String a)  { return isActive && Set.of("ORDER","TRACK","REVIEW","CANCEL").contains(a); }

    public void recordOrder(String orderId) {
        if (orderId == null) throw new IllegalArgumentException("orderId required");
        orderIds.add(orderId);
    }
    public List<String> getOrderIds() { return Collections.unmodifiableList(orderIds); }
}

// ─── DeliveryAgent ───────────────────────────────────────────────

class FDDeliveryAgent extends FDUser implements Trackable, Reviewable {
    private double lat, lng;
    private boolean isAvailable = true;
    private String activeOrderId;
    private final List<Integer> ratings = new ArrayList<>();

    public FDDeliveryAgent(String userId, String name) { super(userId, name); }

    @Override public String getCurrentLocation()              { return String.format("%.4f,%.4f", lat, lng); }
    @Override public void updateLocation(double la, double ln) { lat=la; lng=ln; }
    @Override public void addReview(String r, int rating, String c) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be 1-5");
        ratings.add(rating);
        System.out.printf("[Review] Agent %s rated %d/5%n", name, rating);
    }
    @Override public double getAverageRating() { return ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(i->i).average().orElse(0); }
    @Override public int getReviewCount()      { return ratings.size(); }
    @Override public String getUserType()      { return "DELIVERY_AGENT"; }
    @Override public boolean canPerformAction(String a) { return isActive && Set.of("PICKUP","DELIVER","UPDATE_LOCATION").contains(a); }

    public void assignOrder(String orderId) {
        if (!isAvailable) throw new IllegalStateException(name + " is not available");
        activeOrderId = orderId;
        isAvailable   = false;
        System.out.println("Agent " + name + " assigned to " + orderId);
    }
    public void completeDelivery() {
        if (activeOrderId == null) throw new IllegalStateException("No active delivery");
        System.out.println("Agent " + name + " completed delivery of " + activeOrderId);
        activeOrderId = null;
        isAvailable   = true;
    }
    public boolean isAvailable() { return isAvailable; }
}

// ─── MenuItem (Abstract) ─────────────────────────────────────────

abstract class MenuItem {
    protected final String itemId;
    protected final String name;
    protected final double basePrice;
    protected boolean isAvailable = true;

    protected MenuItem(String itemId, String name, double basePrice) {
        if (basePrice < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.itemId = itemId; this.name = name; this.basePrice = basePrice;
    }

    public abstract double getFinalPrice();

    public String getSummary() {
        return String.format("%-25s Rs %6.2f  %s", name, getFinalPrice(), isAvailable ? "[AVAILABLE]" : "[UNAVAILABLE]");
    }
    public String getItemId()    { return itemId; }
    public String getName()      { return name; }
    public boolean isAvailable() { return isAvailable; }
}

class RegularItem extends MenuItem {
    public RegularItem(String id, String name, double price) { super(id, name, price); }
    @Override public double getFinalPrice() { return basePrice; }
}

class DiscountedItem extends MenuItem {
    private final double discountPct;
    private final LocalDateTime expiresAt;

    public DiscountedItem(String id, String name, double price, double pct, LocalDateTime exp) {
        super(id, name, price);
        if (pct < 0 || pct > 100) throw new IllegalArgumentException("Discount must be 0-100%");
        discountPct = pct; expiresAt = exp;
    }
    @Override public double getFinalPrice() {
        return LocalDateTime.now().isBefore(expiresAt) ? basePrice * (1 - discountPct/100) : basePrice;
    }
}

// ─── Restaurant ──────────────────────────────────────────────────

class Restaurant implements Reviewable {
    private final String restaurantId;
    private final String name;
    private final Map<String, MenuItem> menu = new LinkedHashMap<>();
    private boolean isOpen = true;
    private final List<Integer> ratings = new ArrayList<>();

    public Restaurant(String id, String name) {
        if (id == null) throw new IllegalArgumentException("restaurantId required");
        this.restaurantId = id; this.name = name;
    }

    public void addMenuItem(MenuItem item) {
        if (item == null) throw new IllegalArgumentException("item cannot be null");
        if (menu.containsKey(item.getItemId())) throw new IllegalArgumentException("Item already exists: " + item.getItemId());
        menu.put(item.getItemId(), item);
    }

    public MenuItem getMenuItem(String itemId) {
        MenuItem item = menu.get(itemId);
        if (item == null) throw new IllegalArgumentException("Item not found: " + itemId);
        return item;
    }

    public void printMenu() {
        System.out.println("\n--- Menu: " + name + " ---");
        menu.values().forEach(i -> System.out.println("  " + i.getSummary()));
    }

    @Override public void addReview(String r, int rating, String c) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating 1-5");
        ratings.add(rating);
    }
    @Override public double getAverageRating() { return ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(i->i).average().orElse(0); }
    @Override public int getReviewCount()      { return ratings.size(); }

    public boolean isOpen()         { return isOpen; }
    public String getRestaurantId() { return restaurantId; }
    public String getName()         { return name; }
}

// ─── Order (State Machine) ────────────────────────────────────────

enum FDOrderStatus { PLACED, CONFIRMED, PREPARING, PICKED_UP, DELIVERED, CANCELLED }

class FoodOrder {
    private static final Logger log = Logger.getLogger(FoodOrder.class);

    private final String orderId;
    private final String customerId;
    private FDOrderStatus status;
    private String agentId;
    private final double totalAmount;
    private LocalDateTime deliveredAt;

    public FoodOrder(String customerId, double total) {
        if (customerId == null) throw new IllegalArgumentException("customerId required");
        if (total <= 0) throw new IllegalArgumentException("Total must be positive");
        this.orderId      = "ORD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        this.customerId   = customerId;
        this.status       = FDOrderStatus.PLACED;
        this.totalAmount  = total;
        log.info("Order placed | orderId={} customerId={} total={}", orderId, customerId, total);
    }

    public void confirm()        { requireStatus(FDOrderStatus.PLACED,     "confirm");        transition(FDOrderStatus.CONFIRMED); }
    public void startPreparing() { requireStatus(FDOrderStatus.CONFIRMED,  "startPreparing"); transition(FDOrderStatus.PREPARING); }
    public void pickUp()         { requireStatus(FDOrderStatus.PREPARING,  "pickUp");         if (agentId==null) throw new IllegalStateException("No agent assigned"); transition(FDOrderStatus.PICKED_UP); }

    public void assignAgent(String agentId) {
        requireStatus(FDOrderStatus.PREPARING, "assignAgent");
        if (agentId == null) throw new IllegalArgumentException("agentId required");
        this.agentId = agentId;
        log.info("Agent assigned | orderId={} agentId={}", orderId, agentId);
    }

    public void deliver() {
        requireStatus(FDOrderStatus.PICKED_UP, "deliver");
        deliveredAt = LocalDateTime.now();
        transition(FDOrderStatus.DELIVERED);
        log.info("Order delivered | orderId={} customerId={}", orderId, customerId);
    }

    public void cancel(String reason) {
        if (status == FDOrderStatus.DELIVERED)  throw new IllegalStateException("Cannot cancel DELIVERED order");
        if (status == FDOrderStatus.PICKED_UP)  throw new IllegalStateException("Cannot cancel PICKED_UP order");
        if (status == FDOrderStatus.CANCELLED)  throw new IllegalStateException("Already cancelled");
        log.warn("Order cancelled | orderId={} reason={}", orderId, reason);
        transition(FDOrderStatus.CANCELLED);
    }

    private void requireStatus(FDOrderStatus required, String op) {
        if (status != required) throw new IllegalStateException(
            String.format("Cannot %s — current: %s, required: %s", op, status, required));
    }

    private void transition(FDOrderStatus next) {
        System.out.printf("  [%s] %s --> %s%n", orderId, status, next);
        this.status = next;
    }

    public String getOrderId()       { return orderId; }
    public String getCustomerId()    { return customerId; }
    public FDOrderStatus getStatus() { return status; }
    public double getTotalAmount()   { return totalAmount; }
}

// ─── Payment implementations ──────────────────────────────────────

class UPIPayment implements Payable {
    private final String upiId;
    public UPIPayment(String upiId) { this.upiId = upiId; }
    @Override public boolean pay(String userId, double amount) {
        System.out.printf("[UPI:%s] Charging %s Rs %.2f%n", upiId, userId, amount);
        return true;
    }
    @Override public boolean refund(String txnId, double amount) {
        System.out.printf("[UPI:%s] Refunding txn=%s Rs %.2f%n", upiId, txnId, amount);
        return true;
    }
    @Override public String getMethodName() { return "UPI"; }
}

class CardPayment implements Payable {
    private final String lastFour;
    public CardPayment(String lastFour) {
        if (lastFour.length() != 4) throw new IllegalArgumentException("Need last 4 digits only");
        this.lastFour = lastFour;
    }
    @Override public boolean pay(String userId, double amount) {
        System.out.printf("[CARD:****%s] Charging %s Rs %.2f%n", lastFour, userId, amount);
        return true;
    }
    @Override public boolean refund(String txnId, double amount) {
        System.out.printf("[CARD:****%s] Refunding txn=%s Rs %.2f%n", lastFour, txnId, amount);
        return true;
    }
    @Override public String getMethodName() { return "CARD"; }
}

// ─── Notification implementations ────────────────────────────────

class SMSNotificationFD implements NotificationSenderFD {
    @Override public void send(String userId, String msg)  { System.out.printf("[SMS]  userId=%s | %s%n", userId, msg); }
    @Override public String getChannelName()               { return "SMS"; }
}

class PushNotificationFD implements NotificationSenderFD {
    @Override public void send(String userId, String msg)  { System.out.printf("[PUSH] userId=%s | %s%n", userId, msg); }
    @Override public String getChannelName()               { return "PUSH"; }
}

// ─── FoodDeliveryApp: Main Orchestrator ──────────────────────────

class FoodDeliveryApp {
    private static final Logger log = Logger.getLogger(FoodDeliveryApp.class);

    private final Map<String, FDCustomer>     customers   = new ConcurrentHashMap<>();
    private final Map<String, Restaurant>      restaurants = new ConcurrentHashMap<>();
    private final Map<String, FDDeliveryAgent> agents      = new ConcurrentHashMap<>();
    private final Map<String, FoodOrder>       orders      = new ConcurrentHashMap<>();
    private final List<NotificationSenderFD>   notifiers;

    public FoodDeliveryApp(List<NotificationSenderFD> notifiers) {
        if (notifiers == null || notifiers.isEmpty()) throw new IllegalArgumentException("Need at least one notifier");
        this.notifiers = new ArrayList<>(notifiers);
    }

    public void registerCustomer(FDCustomer c)   { customers.put(c.getUserId(), c);    log.info("Customer registered | userId={}", c.getUserId()); }
    public void registerRestaurant(Restaurant r)  { restaurants.put(r.getRestaurantId(), r); log.info("Restaurant registered | id={}", r.getRestaurantId()); }
    public void registerAgent(FDDeliveryAgent a)  { agents.put(a.getUserId(), a);       log.info("Agent registered | agentId={}", a.getUserId()); }

    public FoodOrder placeOrder(String customerId, String restaurantId,
                                 Map<String, Integer> cart, Payable payment) {
        log.info("Placing order | customerId={} restaurantId={} method={}", customerId, restaurantId, payment.getMethodName());

        FDCustomer customer = customers.get(customerId);
        if (customer == null)   throw new UserNotFoundException(customerId);
        if (!customer.isActive()) throw new IllegalStateException("Customer account inactive");

        Restaurant restaurant = restaurants.get(restaurantId);
        if (restaurant == null) throw new IllegalArgumentException("Restaurant not found: " + restaurantId);
        if (!restaurant.isOpen()) throw new IllegalStateException("Restaurant is closed");

        double total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            MenuItem item = restaurant.getMenuItem(entry.getKey());
            if (!item.isAvailable()) throw new IllegalStateException(item.getName() + " is not available");
            total += item.getFinalPrice() * entry.getValue();
        }

        boolean paid = payment.pay(customerId, total);
        if (!paid) throw new PaymentException("Payment failed for customerId=" + customerId);

        FoodOrder order = new FoodOrder(customerId, total);
        order.confirm();
        orders.put(order.getOrderId(), order);
        customer.recordOrder(order.getOrderId());

        notifyUser(customerId, "Order confirmed! ID: " + order.getOrderId()
            + " | Total: Rs " + String.format("%.2f", total));
        return order;
    }

    public void assignAgent(String orderId, String agentId) {
        FoodOrder order = getOrder(orderId);
        FDDeliveryAgent agent = agents.get(agentId);
        if (agent == null)          throw new IllegalArgumentException("Agent not found: " + agentId);
        if (!agent.isAvailable())   throw new IllegalStateException("Agent not available: " + agentId);

        order.startPreparing();
        order.assignAgent(agentId);
        agent.assignOrder(orderId);
        notifyUser(order.getCustomerId(), "Agent " + agent.getName() + " is picking up your order!");
    }

    public void completeDelivery(String orderId, String agentId) {
        FoodOrder order        = getOrder(orderId);
        FDDeliveryAgent agent  = agents.get(agentId);
        order.pickUp();
        order.deliver();
        if (agent != null) agent.completeDelivery();
        notifyUser(order.getCustomerId(), "Your order has been delivered! Enjoy your meal!");
    }

    private FoodOrder getOrder(String orderId) {
        FoodOrder order = orders.get(orderId);
        if (order == null) throw new OrderNotFoundException(orderId);
        return order;
    }

    private void notifyUser(String userId, String message) {
        for (NotificationSenderFD sender : notifiers) {
            try {
                sender.send(userId, message);
            } catch (Exception e) {
                log.warn("Notification failed | channel={} userId={}", sender.getChannelName(), userId);
            }
        }
    }
}

// ─── MAIN ─────────────────────────────────────────────────────────

public class FoodDeliveryDemo {
    public static void main(String[] args) {

        System.out.println("============================================");
        System.out.println("   FOOD DELIVERY APP — Full LLD Demo");
        System.out.println("============================================\n");

        FoodDeliveryApp app = new FoodDeliveryApp(
            List.of(new SMSNotificationFD(), new PushNotificationFD())
        );

        // Register entities
        FDCustomer rahul = new FDCustomer("CUST_001", "Rahul Sharma");
        app.registerCustomer(rahul);

        FDDeliveryAgent rohan = new FDDeliveryAgent("AGT_001", "Rohan Verma");
        app.registerAgent(rohan);

        Restaurant pizzaHut = new Restaurant("REST_001", "Pizza Hut - Bandra");
        pizzaHut.addMenuItem(new RegularItem("ITEM_001", "Farmhouse Pizza (M)", 349.00));
        pizzaHut.addMenuItem(new RegularItem("ITEM_002", "Garlic Bread", 79.00));
        pizzaHut.addMenuItem(new DiscountedItem("ITEM_003", "Pepsi (2L)", 99.00, 20.0, LocalDateTime.now().plusDays(7)));
        app.registerRestaurant(pizzaHut);
        pizzaHut.printMenu();

        // Happy path
        System.out.println("\n=== Placing Order ===");
        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("ITEM_001", 1);
        cart.put("ITEM_002", 2);
        cart.put("ITEM_003", 1);

        FoodOrder order = app.placeOrder("CUST_001", "REST_001", cart, new UPIPayment("rahul@paytm"));

        System.out.println("\n=== Assigning Agent ===");
        app.assignAgent(order.getOrderId(), "AGT_001");

        System.out.println("\n=== Completing Delivery ===");
        app.completeDelivery(order.getOrderId(), "AGT_001");

        System.out.println("\n=== Final State ===");
        System.out.println("Order status : " + order.getStatus());
        System.out.println("Customer orders: " + rahul.getOrderIds());
        System.out.println("Agent available: " + rohan.isAvailable());

        // Reviews
        System.out.println("\n=== Reviews ===");
        pizzaHut.addReview("CUST_001", 5, "Amazing pizza!");
        rohan.addReview("CUST_001", 4, "Fast delivery");
        System.out.printf("Pizza Hut rating: %.1f (%d reviews)%n", pizzaHut.getAverageRating(), pizzaHut.getReviewCount());
        System.out.printf("Agent rating: %.1f (%d reviews)%n", rohan.getAverageRating(), rohan.getReviewCount());

        // Error scenarios
        System.out.println("\n=== Error Scenarios ===");

        try {
            order.deliver();  // Already DELIVERED
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            FDDeliveryAgent fakeAgent = new FDDeliveryAgent("AGT_X", "Fake");
            fakeAgent.assignOrder("ORD-001");
            fakeAgent.assignOrder("ORD-002");  // Already assigned
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        /*
         * OUTPUT:
         * --- Menu: Pizza Hut - Bandra ---
         *   Farmhouse Pizza (M)       Rs 349.00  [AVAILABLE]
         *   Garlic Bread              Rs  79.00  [AVAILABLE]
         *   Pepsi (2L)                Rs  79.20  [AVAILABLE]   (20% off)
         *
         * === Placing Order ===
         * [UPI:rahul@paytm] Charging CUST_001 Rs 586.20
         * [SMS]  userId=CUST_001 | Order confirmed! ID: ORD-XXXXXXXX | Total: Rs 586.20
         * [PUSH] userId=CUST_001 | Order confirmed! ...
         *
         * === Assigning Agent ===
         *   [ORD-XXXXXXXX] CONFIRMED --> PREPARING
         * Agent Rohan Verma assigned to ORD-XXXXXXXX
         * [SMS]  userId=CUST_001 | Agent Rohan Verma is picking up your order!
         *
         * === Completing Delivery ===
         *   [ORD-XXXXXXXX] PREPARING --> PICKED_UP
         *   [ORD-XXXXXXXX] PICKED_UP --> DELIVERED
         * Agent Rohan Verma completed delivery of ORD-XXXXXXXX
         *
         * === Final State ===
         * Order status : DELIVERED
         * Agent available: true
         *
         * === Reviews ===
         * Pizza Hut rating: 5.0 (1 reviews)
         * Agent rating: 4.0 (1 reviews)
         *
         * === Error Scenarios ===
         * Caught: Cannot deliver — current: DELIVERED, required: PICKED_UP
         * Caught: Fake is not available
         */
    }
}
```

---

## 7. Interview Cheat Sheet

### 7.1 Interview Flow — Run This Every Time

| Step | Time | What to do |
|---|---|---|
| 1. CLARIFY | 2 min | Ask: scale? constraints? which component to focus on? |
| 2. IDENTIFY ENTITIES | 2 min | List main classes, interfaces, abstract classes |
| 3. DEFINE RELATIONSHIPS | 3 min | Capabilities → Interfaces, Families → Abstract, Specific → Concrete |
| 4. WRITE CORE CODE | 5-8 min | Write the most important state machine or service first |
| 5. SELF-REVIEW OUT LOUD | 2 min | Null inputs? Invalid state? Concurrency? External failures? |
| 6. ADD EXCEPTIONS + LOGGING | 2 min | Custom hierarchy, structured logging, never log sensitive data |

---

### 7.2 Phrases That Signal a 5-Rated Candidate

| Situation | What to Say |
|---|---|
| Choosing interface | *"I use an interface here because multiple unrelated classes need this capability, allowing plug-and-play swapping of implementations."* |
| Choosing abstract class | *"I use abstract class because these are related entities sharing common state and behavior, but core logic differs per type."* |
| Adding exceptions | *"I build a custom exception hierarchy so callers can catch at exactly the right level of granularity."* |
| Adding logging | *"I add structured logging at entry and exit points — logging userId and requestId, but never passwords or card numbers."* |
| Composition choice | *"I prefer composition over inheritance — more flexible, easier to test, avoids tight coupling."* |
| State validation | *"I always validate current state before performing any operation to prevent illegal state transitions."* |
| Edge cases | *"Let me check failure scenarios — null inputs, concurrent access, external service failures, and max limit exceeded."* |

---

### 7.3 Edge Cases Checklist

- [ ] Null inputs
- [ ] Empty collections / empty strings
- [ ] Negative or zero numbers
- [ ] Invalid state (is account active? is engine on?)
- [ ] Duplicate entries
- [ ] Concurrent access (thread safety)
- [ ] External service timeout / failure
- [ ] Max limits exceeded

---

### 7.4 Do's and Don'ts

| DO | DON'T |
|---|---|
| Ask clarifying questions first | Jump into code immediately |
| Think and speak out loud | Stay silent during long pauses |
| Mention trade-offs for each choice | Present only one solution |
| Self-review code for edge cases | Wait for interviewer to find bugs |
| Add logging before asked | Only answer what's explicitly asked |
| Use interfaces for plug-and-play | Hardcode implementations |
| Prefer composition over inheritance | Over-use inheritance for code reuse |
| Validate state before behavior | Skip input validation |
| Use enum for state machines | Use raw String constants for status |
| Chain exceptions (include cause) | Swallow exceptions silently |

---

> **The difference between a 4 and a 5:**
> A 4-rated candidate writes correct code when asked.
> A 5-rated candidate handles edge cases, adds logging, proposes alternatives, and mentions observability **before** being asked.
> Treat every LLD problem like you're writing production code for a real system.
