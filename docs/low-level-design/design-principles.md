# LLD Design Principles - Complete One-Stop Guide

> A comprehensive reference covering every principle used in Low Level Design: OOP, SOLID, KISS, DRY, YAGNI, GRASP, Law of Demeter, Composition over Inheritance, Fail Fast, Separation of Concerns, and more — with Java code examples, anti-patterns, interview Q&A, and a cheat sheet.

---

## Table of Contents

1. [Object-Oriented Programming (OOP)](#1-object-oriented-programming-oop)
   - Encapsulation
   - Abstraction
   - Inheritance
   - Polymorphism
2. [SOLID Principles](#2-solid-principles)
   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion
3. [DRY - Don't Repeat Yourself](#3-dry---dont-repeat-yourself)
4. [KISS - Keep It Simple, Stupid](#4-kiss---keep-it-simple-stupid)
5. [YAGNI - You Aren't Gonna Need It](#5-yagni---you-arent-gonna-need-it)
6. [GRASP Principles](#6-grasp-principles)
7. [Law of Demeter (LoD)](#7-law-of-demeter-lod)
8. [Composition Over Inheritance](#8-composition-over-inheritance)
9. [Separation of Concerns (SoC)](#9-separation-of-concerns-soc)
10. [Fail Fast Principle](#10-fail-fast-principle)
11. [Principle of Least Astonishment (POLA)](#11-principle-of-least-astonishment-pola)
12. [Tell, Don't Ask](#12-tell-dont-ask)
13. [High Cohesion and Low Coupling](#13-high-cohesion-and-low-coupling)
14. [Package Design Principles](#14-package-design-principles)
15. [How Principles Work Together](#15-how-principles-work-together)
16. [Interview Questions and Answers](#16-interview-questions-and-answers)
17. [Quick Reference Cheat Sheet](#17-quick-reference-cheat-sheet)

---

## 1. Object-Oriented Programming (OOP)

OOP is the foundation of LLD. Every other principle in this guide builds on top of OOP concepts. Java is a primarily object-oriented language, so understanding OOP deeply is non-negotiable.

OOP organizes code around **objects** (data + behavior) rather than functions and procedures. The four pillars are:

---

### 1.1 Encapsulation

**Definition:** Bundle related data (fields) and behavior (methods) together inside a class, and restrict direct access to the internal state from the outside.

**Core idea:** Hide the **how** (implementation), expose only the **what** (interface/API).

```java
// BAD - No encapsulation
public class BankAccount {
    public double balance;  // anyone can set balance = -99999
    public String pin;      // pin is publicly visible
}

// GOOD - Encapsulated
public class BankAccount {
    private double balance;   // hidden
    private String pin;       // hidden

    public BankAccount(double initialBalance, String pin) {
        if (initialBalance < 0) throw new IllegalArgumentException("Balance cannot be negative");
        this.balance = initialBalance;
        this.pin = pin;
    }

    public double getBalance(String enteredPin) {
        validatePin(enteredPin);
        return balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        this.balance += amount;
    }

    public void withdraw(double amount, String enteredPin) {
        validatePin(enteredPin);
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        this.balance -= amount;
    }

    private void validatePin(String enteredPin) {
        if (!this.pin.equals(enteredPin)) throw new SecurityException("Invalid PIN");
    }
}
```

**Benefits:**
- Protects invariants (balance can never go negative due to internal checks)
- You can change internal implementation without breaking callers
- Prevents external code from putting the object in an invalid state

**Access modifiers in Java (tightest to loosest):**

| Modifier | Class | Package | Subclass | World |
|---|---|---|---|---|
| private | YES | NO | NO | NO |
| (default/package) | YES | YES | NO | NO |
| protected | YES | YES | YES | NO |
| public | YES | YES | YES | YES |

Rule of thumb: Start with `private`. Widen only when necessary.

---

### 1.2 Abstraction

**Definition:** Show only the relevant details of an object to the outside world and hide the unnecessary complexity.

**Core idea:** Users of a class should work with a simplified model, not worry about internals.

Achieved in Java via:
- Abstract classes
- Interfaces

```java
// Abstraction via Interface
public interface PaymentGateway {
    boolean processPayment(double amount, String currency);
    boolean refund(String transactionId, double amount);
}

// Concrete implementation - all complexity hidden behind the interface
public class StripeGateway implements PaymentGateway {
    private final StripeClient client;

    @Override
    public boolean processPayment(double amount, String currency) {
        // Stripe-specific HTTP calls, error handling, retries, signing - all hidden
        return client.charge(amount, currency).isSuccess();
    }

    @Override
    public boolean refund(String transactionId, double amount) {
        return client.refund(transactionId, amount).isSuccess();
    }
}

// Caller only knows about the interface - zero Stripe-specific coupling
public class OrderService {
    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public void placeOrder(Order order) {
        boolean paid = paymentGateway.processPayment(order.getTotal(), "INR");
        if (!paid) throw new PaymentFailedException("Payment failed for order: " + order.getId());
    }
}
```

**Abstract class vs Interface:**

| Feature | Abstract Class | Interface |
|---|---|---|
| Can have state (fields) | YES | Only constants (static final) |
| Can have constructors | YES | NO |
| Multiple inheritance | NO (single) | YES (multiple) |
| Method implementations | YES (partial) | YES (default methods, Java 8+) |
| When to use | Shared base behavior + state | Defining a contract / capability |

---

### 1.3 Inheritance

**Definition:** A class (child/subclass) can inherit fields and methods from another class (parent/superclass), enabling code reuse and establishing IS-A relationships.

```java
public abstract class Vehicle {
    protected String brand;
    protected int speed;

    public Vehicle(String brand) {
        this.brand = brand;
    }

    public void accelerate(int delta) {
        this.speed += delta;
        System.out.println(brand + " accelerating to " + speed + " km/h");
    }

    public abstract String getFuelType();  // subclasses must define this
}

public class ElectricCar extends Vehicle {
    private int batteryPercent;

    public ElectricCar(String brand, int battery) {
        super(brand);
        this.batteryPercent = battery;
    }

    @Override
    public String getFuelType() {
        return "Electric";
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }
}
```

**Types of Inheritance:**
- Single: A extends B
- Multilevel: A extends B, B extends C
- Hierarchical: B extends A, C extends A (multiple children, one parent)
- Multiple inheritance: Not supported via classes in Java; supported via interfaces

**Dangers of Inheritance (when to avoid it):**
- Tight coupling between parent and child
- Fragile Base Class Problem: changes to the parent break all children
- Deep hierarchies (more than 2-3 levels) become unmaintainable

Rule: Use inheritance only for true IS-A relationships. Prefer composition for HAS-A. See Section 8.

---

### 1.4 Polymorphism

**Definition:** The ability of a single interface or method call to behave differently depending on the actual type of the object at runtime or compile time.

Two types:

**Compile-time polymorphism (Method Overloading):**
```java
public class Calculator {
    public int add(int a, int b) { return a + b; }
    public double add(double a, double b) { return a + b; }          // overloaded
    public int add(int a, int b, int c) { return a + b + c; }        // overloaded
}
```
Resolved at compile time based on method signature.

**Runtime polymorphism (Method Overriding):**
```java
public abstract class Notification {
    public abstract void send(String message);
}

public class EmailNotification extends Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

public class SMSNotification extends Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

public class PushNotification extends Notification {
    @Override
    public void send(String message) {
        System.out.println("Sending push: " + message);
    }
}

// Polymorphic call - actual behavior decided at runtime
public class AlertService {
    public void alert(List<Notification> channels, String msg) {
        for (Notification n : channels) {
            n.send(msg);   // calls Email/SMS/Push version based on actual type
        }
    }
}
```

**Why polymorphism matters in LLD:**
- You can write code that works with abstractions (interfaces)
- New types can be added without modifying existing code (Open/Closed Principle)
- Powers all behavioral design patterns (Strategy, Command, Template Method, etc.)

---

## 2. SOLID Principles

SOLID is an acronym for five principles introduced by Robert C. Martin (Uncle Bob) for writing maintainable object-oriented code. These are the most commonly asked LLD principles in interviews.

---

### 2.1 S - Single Responsibility Principle (SRP)

**Definition:** A class should have one, and only one, reason to change. It should do one thing and do it well.

**One reason to change = one responsibility = one actor that could request a change.**

```java
// BAD - This class has multiple responsibilities:
// 1. Managing employee data
// 2. Calculating salary (Finance team changes this)
// 3. Saving to DB (DB team changes this)
// 4. Generating report (HR team changes this)
public class Employee {
    private String name;
    private double baseSalary;

    public double calculateSalary() {
        // complex tax, bonus logic
        return baseSalary * 1.2 - 5000;
    }

    public void saveToDatabase() {
        // JDBC code to persist employee
    }

    public String generatePayslip() {
        // HTML/PDF generation logic
    }
}
```

```java
// GOOD - Each class has one responsibility
public class Employee {
    private String name;
    private double baseSalary;
    // only employee data, getters/setters
}

public class SalaryCalculator {
    public double calculate(Employee emp) {
        return emp.getBaseSalary() * 1.2 - 5000;
    }
}

public class EmployeeRepository {
    public void save(Employee emp) {
        // DB persistence logic
    }
}

public class PayslipGenerator {
    public String generate(Employee emp, double salary) {
        // report generation logic
    }
}
```

**How to identify SRP violations:**
- A class has methods used by different "teams" or "actors"
- A class is too large (500+ lines is a warning sign)
- A class imports from many unrelated domains (e.g., HTTP, DB, file IO, email all in one class)
- You frequently find yourself changing a class for multiple unrelated reasons

---

### 2.2 O - Open/Closed Principle (OCP)

**Definition:** Software entities (classes, modules, functions) should be **open for extension** but **closed for modification**.

In other words: add new behavior by adding new code, not by editing existing code.

```java
// BAD - Every new discount type requires modifying this class
public class DiscountCalculator {
    public double calculate(Order order, String discountType) {
        if (discountType.equals("STUDENT")) {
            return order.getTotal() * 0.10;
        } else if (discountType.equals("SENIOR")) {
            return order.getTotal() * 0.15;
        } else if (discountType.equals("EMPLOYEE")) {   // keep adding else-if forever...
            return order.getTotal() * 0.20;
        }
        return 0;
    }
}
```

```java
// GOOD - Adding new discount = adding new class, zero modification to existing code
public interface DiscountStrategy {
    double calculate(Order order);
}

public class StudentDiscount implements DiscountStrategy {
    @Override
    public double calculate(Order order) {
        return order.getTotal() * 0.10;
    }
}

public class SeniorDiscount implements DiscountStrategy {
    @Override
    public double calculate(Order order) {
        return order.getTotal() * 0.15;
    }
}

public class EmployeeDiscount implements DiscountStrategy {
    @Override
    public double calculate(Order order) {
        return order.getTotal() * 0.20;
    }
}

// New discount in future? Just add a new class. Nothing existing is touched.
public class DiscountCalculator {
    public double calculate(Order order, DiscountStrategy strategy) {
        return strategy.calculate(order);
    }
}
```

**How to achieve OCP:**
- Program to interfaces/abstractions, not concrete implementations
- Use Strategy, Template Method, Decorator, and Observer design patterns
- Identify the axis of variation (what is likely to change?) and abstract it

---

### 2.3 L - Liskov Substitution Principle (LSP)

**Definition:** Objects of a subtype must be substitutable for objects of their supertype without breaking the correctness of the program.

Formally: if S is a subtype of T, then wherever T is expected, S can be used and the program should still work correctly.

**Classic LSP Violation - The Rectangle/Square Problem:**

```java
// BAD - Square "is-a" Rectangle mathematically, but violates LSP
public class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public int getArea() { return width * height; }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;   // Square forces both sides equal
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        this.width = height;   // Square forces both sides equal
    }
}

// This function is designed for Rectangle but BREAKS with Square
public void resize(Rectangle r) {
    r.setWidth(10);
    r.setHeight(5);
    // Expected area: 50
    // With Square: area = 25 (because setHeight changed width too!)
    assert r.getArea() == 50; // FAILS for Square
}
```

```java
// GOOD - Don't force inheritance just because of mathematical IS-A
// Use a common interface instead
public interface Shape {
    int getArea();
}

public class Rectangle implements Shape {
    private int width, height;
    // setters independent of each other
    public int getArea() { return width * height; }
}

public class Square implements Shape {
    private int side;
    public int getArea() { return side * side; }
}
```

**LSP Rules (Behavioral Subtyping):**
- Preconditions cannot be strengthened in the subtype (subtype cannot be stricter)
- Postconditions cannot be weakened in the subtype (subtype cannot be laxer)
- Subtype must maintain all invariants of the parent
- Subtype must not throw new/unexpected exceptions not in parent's contract

**Real-world LSP violation to watch for:**
```java
// BAD - ReadOnlyList violates LSP if substituted for List
class ReadOnlyList<T> extends ArrayList<T> {
    @Override
    public boolean add(T element) {
        throw new UnsupportedOperationException(); // NOT expected from a List
    }
}
```

---

### 2.4 I - Interface Segregation Principle (ISP)

**Definition:** Clients should not be forced to depend on interfaces they do not use. Prefer many small, specific interfaces over one large, fat interface.

```java
// BAD - Fat interface forces all implementors to implement everything
public interface Worker {
    void work();
    void eat();
    void sleep();
    void attendMeeting();
    void writeCode();
    void manageTeam();   // not all workers manage teams
    void approveLeave(); // not all workers approve leave
}

// RobotWorker is forced to implement eat/sleep/manageTeam - nonsensical
public class RobotWorker implements Worker {
    @Override
    public void work() { System.out.println("Robot working"); }

    @Override
    public void eat() { throw new UnsupportedOperationException("Robots don't eat"); }

    @Override
    public void sleep() { throw new UnsupportedOperationException("Robots don't sleep"); }

    // ... forced to implement all others too
}
```

```java
// GOOD - Segregated interfaces
public interface Workable {
    void work();
}

public interface Feedable {
    void eat();
    void sleep();
}

public interface Manageable {
    void manageTeam();
    void approveLeave();
}

// Human employee - implements all relevant interfaces
public class HumanEmployee implements Workable, Feedable, Manageable {
    public void work() { System.out.println("Human working"); }
    public void eat() { System.out.println("Human eating"); }
    public void sleep() { System.out.println("Human sleeping"); }
    public void manageTeam() { System.out.println("Managing team"); }
    public void approveLeave() { System.out.println("Approving leave"); }
}

// Robot - only implements what it needs
public class RobotWorker implements Workable {
    public void work() { System.out.println("Robot working"); }
}

// Junior dev - no management responsibilities
public class JuniorDev implements Workable, Feedable {
    public void work() { System.out.println("Junior dev coding"); }
    public void eat() { System.out.println("Eating lunch"); }
    public void sleep() { System.out.println("Sleeping"); }
}
```

**Signs of ISP violation:**
- Classes implement an interface but leave methods throwing `UnsupportedOperationException`
- Interfaces have methods that don't logically belong together
- Adding a method to an interface forces unrelated classes to change

---

### 2.5 D - Dependency Inversion Principle (DIP)

**Definition:**
- High-level modules should not depend on low-level modules. Both should depend on abstractions.
- Abstractions should not depend on details. Details should depend on abstractions.

```java
// BAD - High-level OrderService directly depends on low-level MySQLRepository
public class MySQLOrderRepository {
    public void save(Order order) {
        // MySQL-specific JDBC code
    }
}

public class OrderService {
    private MySQLOrderRepository repository = new MySQLOrderRepository(); // tight coupling!

    public void placeOrder(Order order) {
        // ... business logic
        repository.save(order);  // if DB changes, OrderService must change too
    }
}
```

```java
// GOOD - Both depend on the abstraction (interface)
public interface OrderRepository {
    void save(Order order);
    Order findById(String id);
}

// Low-level detail
public class MySQLOrderRepository implements OrderRepository {
    @Override
    public void save(Order order) { /* MySQL logic */ }

    @Override
    public Order findById(String id) { /* MySQL query */ }
}

// Easy to swap later
public class MongoOrderRepository implements OrderRepository {
    @Override
    public void save(Order order) { /* MongoDB logic */ }

    @Override
    public Order findById(String id) { /* MongoDB query */ }
}

// High-level - depends only on abstraction
public class OrderService {
    private final OrderRepository repository;  // depends on interface, not concrete

    // Dependency is INJECTED from outside (Dependency Injection)
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void placeOrder(Order order) {
        // ... business logic
        repository.save(order);
    }
}

// Wiring (typically done by Spring IoC container)
OrderRepository repo = new MySQLOrderRepository();
OrderService service = new OrderService(repo);
// swap to Mongo anytime: new OrderService(new MongoOrderRepository())
```

**DIP vs Dependency Injection:**
- DIP is the principle: "depend on abstractions"
- Dependency Injection (DI) is a technique to implement DIP by providing dependencies from outside the class
- IoC containers like Spring automate DI

---

## 3. DRY - Don't Repeat Yourself

**Definition:** Every piece of knowledge must have a single, unambiguous, authoritative representation within a system.

**Origin:** From "The Pragmatic Programmer" by Andy Hunt and Dave Thomas.

**Core idea:** If you have the same logic, data, or behavior in multiple places, a change requires updating all of them — and you will forget one.

```java
// BAD - Tax calculation logic duplicated in 3 places
public class InvoiceService {
    public double calculateTotal(double price) {
        return price + (price * 0.18);   // GST logic here
    }
}

public class QuoteService {
    public double calculateQuoteTotal(double price) {
        return price + (price * 0.18);   // same GST logic, duplicated
    }
}

public class ReportService {
    public double getReportAmount(double price) {
        return price + (price * 0.18);   // again, duplicated
    }
}
// If GST rate changes to 0.20, you must update 3 places.
// One missed = bug.
```

```java
// GOOD - Single source of truth
public class TaxCalculator {
    private static final double GST_RATE = 0.18;  // defined once

    public double addGST(double price) {
        return price + (price * GST_RATE);
    }
}

// All services reuse the single implementation
public class InvoiceService {
    private final TaxCalculator taxCalc;
    public double calculateTotal(double price) {
        return taxCalc.addGST(price);
    }
}
// Change GST_RATE in one place = all services updated automatically
```

**DRY is NOT just about code:**

DRY applies to:
- Constants and magic numbers (define once, use everywhere)
- Configuration (one config file, not hardcoded in 10 places)
- Database schema (single source of truth for data structure)
- Documentation (don't duplicate docs and code; they diverge)
- Test data setup (shared fixtures, not copy-paste in each test)

**DRY Anti-patterns:**

- Copy-paste programming (duplicating entire methods with minor tweaks)
- Magic numbers scattered across codebase (use constants)
- Parallel inheritance hierarchies (two class hierarchies that always change together)

**DRY vs WET:**
WET = "Write Everything Twice" or "We Enjoy Typing" - the anti-pattern of duplication.

**IMPORTANT - When NOT to over-apply DRY:**

Premature deduplication is dangerous. Two pieces of code that look the same today may diverge tomorrow for different reasons (violating SRP). The "Rule of Three" is a practical heuristic: only deduplicate on the third occurrence.

---

## 4. KISS - Keep It Simple, Stupid

**Definition:** Most systems work best if they are kept simple rather than made complicated. Simplicity should be a key design goal, and unnecessary complexity should be avoided.

**Origin:** U.S. Navy design principle (1960s). Popularized in software by Extreme Programming community.

**Core idea:** Simple code is easier to read, debug, test, and maintain. Complex code is a liability.

```java
// BAD - Overly complex for the task
public boolean isPalindrome(String s) {
    Stack<Character> stack = new Stack<>();
    Queue<Character> queue = new LinkedList<>();
    for (char c : s.toCharArray()) {
        stack.push(c);
        queue.add(c);
    }
    while (!stack.isEmpty()) {
        if (!stack.pop().equals(queue.poll())) return false;
    }
    return true;
}

// GOOD - Simple and readable
public boolean isPalindrome(String s) {
    String reversed = new StringBuilder(s).reverse().toString();
    return s.equals(reversed);
}
```

```java
// BAD - over-engineered configuration for a simple feature flag
public class FeatureFlagManager {
    private static final Map<String, Map<String, Object>> flags = new HashMap<>();

    public static void registerFlag(String name, String description, boolean defaultValue,
                                    List<String> allowedRoles, Date expiryDate) {
        Map<String, Object> config = new HashMap<>();
        config.put("description", description);
        config.put("value", defaultValue);
        config.put("roles", allowedRoles);
        config.put("expiry", expiryDate);
        flags.put(name, config);
    }
    // ... 200 more lines for a yes/no flag
}

// GOOD - Simple enough for the actual need
public class FeatureFlags {
    private static final boolean DARK_MODE_ENABLED = true;
    private static final boolean NEW_CHECKOUT_ENABLED = false;
}
```

**KISS in practice:**
- Write the simplest code that passes all the tests
- Avoid speculative abstraction ("maybe we'll need this later")
- Use standard library methods before writing custom implementations
- Short methods (ideally 5-20 lines) are easier to understand
- Avoid deeply nested conditionals (extract methods, use guard clauses)

**Guard clauses instead of deep nesting:**
```java
// BAD - Arrow code (deeply nested)
public void processOrder(Order order) {
    if (order != null) {
        if (order.isValid()) {
            if (order.getCustomer() != null) {
                if (order.getCustomer().hasValidAddress()) {
                    // actual work buried 4 levels deep
                    ship(order);
                }
            }
        }
    }
}

// GOOD - Guard clauses (fail fast, flat structure)
public void processOrder(Order order) {
    if (order == null) throw new IllegalArgumentException("Order cannot be null");
    if (!order.isValid()) throw new InvalidOrderException("Order is invalid");
    if (order.getCustomer() == null) throw new IllegalStateException("No customer");
    if (!order.getCustomer().hasValidAddress()) throw new IllegalStateException("Invalid address");

    ship(order);  // actual work at top level, easy to find
}
```

---

## 5. YAGNI - You Aren't Gonna Need It

**Definition:** Don't implement functionality until it is actually needed. Don't build features "just in case."

**Origin:** Extreme Programming (XP) practice by Ron Jeffries.

**Core idea:** Every line of code you write has to be read, tested, maintained, and debugged. Code that isn't needed yet is pure liability.

```java
// BAD - Building for imaginary future requirements
public class UserService {
    public User createUser(String name, String email) { ... }

    // Nobody asked for this. No feature needs it. Just "in case."
    public User createUserWithOAuth(String token, OAuthProvider provider) { ... }
    public User createUserWithBiometric(BiometricData data) { ... }
    public User createUserForEnterprise(String name, String email, Organization org) { ... }
    public User createUserBatch(List<UserRequest> requests) { ... }
}
```

```java
// GOOD - Build only what's needed now
public class UserService {
    public User createUser(String name, String email) {
        // implement this properly, with tests, since it's actually needed
    }
    // OAuth? Biometric? Add them when a feature requires them.
}
```

**YAGNI cost-benefit:**

Every premature feature:
- Takes time to build now
- Adds complexity that makes other features slower to build
- May be wrong (requirements change)
- Must be maintained even if never used
- Adds surface area for bugs

**YAGNI vs SOLID:**

These are not in conflict. SOLID says "design so that change is easy." YAGNI says "don't implement change that hasn't been asked for yet." You can have a clean, extensible design that is currently minimal in features.

**Common YAGNI violations:**
- Adding abstract factories for a single concrete implementation
- Building a plugin system before a second plugin exists
- Adding internationalization before any non-English user exists
- Generic "config-driven" systems for things that never actually change

---

## 6. GRASP Principles

GRASP = General Responsibility Assignment Software Patterns. Defined by Craig Larman, these are 9 guidelines for deciding which class should be responsible for what in OOP.

### 6.1 Information Expert

**Assign responsibility to the class that has the information required to fulfill it.**

```java
// Order has the line items, so Order should calculate its own total
public class Order {
    private List<LineItem> items;

    public double getTotal() {    // Order is the Information Expert here
        return items.stream()
                    .mapToDouble(LineItem::getSubtotal)
                    .sum();
    }
}

// LineItem has price and qty, so it calculates its own subtotal
public class LineItem {
    private double price;
    private int quantity;

    public double getSubtotal() {   // LineItem is the expert for this calculation
        return price * quantity;
    }
}
```

### 6.2 Creator

**Assign class B the responsibility to create instances of class A if:**
- B contains or aggregates A
- B closely uses A
- B has the initialization data for A

```java
// Order aggregates LineItems, so Order creates LineItems
public class Order {
    private List<LineItem> items = new ArrayList<>();

    public void addItem(Product product, int qty) {
        items.add(new LineItem(product, qty));  // Order is the Creator of LineItem
    }
}
```

### 6.3 Controller

**Assign responsibility to handle system events to a class that represents either:**
- The overall system (Facade Controller), or
- A use-case scenario (Use Case Controller)

```java
// Use-case controller for order-related operations
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Order order = orderService.create(request);
        paymentService.charge(order);
        return OrderResponse.from(order);
    }
}
```

### 6.4 Low Coupling

**Assign responsibilities to minimize dependencies between classes.** Classes should depend on as few other classes as possible.

Low coupling = easier to change, test, and reuse each class independently.

```java
// BAD - OrderService knows about specific notification implementations
public class OrderService {
    private EmailService emailService = new EmailService();
    private SMSService smsService = new SMSService();
    private PushService pushService = new PushService();

    public void placeOrder(Order order) {
        // ... logic
        emailService.sendConfirmation(order);
        smsService.sendConfirmation(order);
        pushService.sendConfirmation(order);
    }
}

// GOOD - OrderService coupled to abstraction only
public class OrderService {
    private final List<NotificationService> notifiers;

    public OrderService(List<NotificationService> notifiers) {
        this.notifiers = notifiers;
    }

    public void placeOrder(Order order) {
        // ... logic
        notifiers.forEach(n -> n.sendConfirmation(order));
    }
}
```

### 6.5 High Cohesion

**Keep related responsibilities together. A class should have a focused, meaningful purpose.** Cohesion is about how strongly related the responsibilities of a single class are.

Low cohesion = "God class" that does everything = hard to maintain.

```java
// BAD - Low cohesion, does unrelated things
public class ApplicationManager {
    public void startServer() { ... }
    public void sendEmail() { ... }
    public void generateReport() { ... }
    public void connectToDatabase() { ... }
    public void validateUserInput() { ... }
    public void calculateTax() { ... }
}

// GOOD - Each class highly cohesive
public class ServerManager { public void start() { ... } }
public class EmailService { public void send(String to, String msg) { ... } }
public class ReportGenerator { public Report generate(...) { ... } }
```

### 6.6 Polymorphism

Assign responsibility for type-based behavior variations using polymorphism instead of type-checking conditionals. (Covered fully in OOP section, Section 1.4.)

### 6.7 Pure Fabrication

**Create an artificial (not domain-inspired) class to achieve low coupling and high cohesion when no natural domain class fits.**

```java
// There's no real-world "OrderRepository" object - it's a fabrication
// But it exists to achieve separation of concerns and low coupling
public class OrderRepository {
    public void save(Order order) { /* persistence logic */ }
    public Optional<Order> findById(String id) { /* query logic */ }
}
```

Services, Repositories, Factories, and Validators are all examples of Pure Fabrications.

### 6.8 Indirection

**Assign responsibility to an intermediate object to avoid direct coupling between two components.**

```java
// Instead of all classes depending on a single LoggingImpl class,
// they depend on a Logger interface (indirection layer)
public interface Logger {
    void info(String message);
    void error(String message, Exception e);
}

// Classes depend on Logger interface, not Log4j/Slf4j/etc. directly
public class OrderService {
    private final Logger logger;
    public OrderService(Logger logger) { this.logger = logger; }
}
```

### 6.9 Protected Variations

**Design so that variations in one part of the system do not destabilize other parts.** Wrap points of instability (things likely to change) behind a stable interface.

```java
// Payment provider will change (new providers, API changes)
// Wrap it behind a stable interface
public interface PaymentGateway {
    PaymentResult charge(Amount amount);
}
// Now variations in Stripe/Razorpay/PayU are protected behind this contract
```

---

## 7. Law of Demeter (LoD)

**Also known as: Principle of Least Knowledge**

**Definition:** A unit should only talk to its immediate friends. Do not talk to strangers.

Formally, a method M of class C should only call methods on:
1. `this` (the current object)
2. Objects passed as parameters to M
3. Objects created/instantiated within M
4. Direct component fields of C

**Do NOT call methods on objects returned by other methods (chaining through other objects).**

```java
// BAD - Violates LoD ("train wreck" code)
public double calculateShipping(Order order) {
    // order.getCustomer() --> get stranger (Customer)
    // .getAddress() --> go deeper into stranger (Address)
    // .getCity() --> go even deeper
    // .getShippingZone() --> now 4 levels deep from Order
    String zone = order.getCustomer().getAddress().getCity().getShippingZone();
    return shippingRates.get(zone);
}
// Problem: This method is tightly coupled to Order, Customer, Address, AND City
// Any change to any of those breaks this method
```

```java
// GOOD - Ask Order for what you need; let Order talk to its own friends
public class Order {
    private Customer customer;

    public String getShippingZone() {
        return customer.getShippingZone();   // Order talks to Customer (its friend)
    }
}

public class Customer {
    private Address address;

    public String getShippingZone() {
        return address.getShippingZone();    // Customer talks to Address (its friend)
    }
}

public class Address {
    private City city;

    public String getShippingZone() {
        return city.getShippingZone();       // Address talks to City (its friend)
    }
}

// Now calculateShipping only knows about Order - clean
public double calculateShipping(Order order) {
    String zone = order.getShippingZone();   // one hop only
    return shippingRates.get(zone);
}
```

**Benefits of LoD:**
- Reduces coupling between classes
- Makes refactoring easier (change internals without affecting external callers)
- Code is more readable (no long dot chains)

**Warning:** LoD should not be applied blindly. Fluent APIs (builder pattern, stream chains) are intentionally designed for chaining and are fine. The key is whether you are traversing unrelated object graphs vs. working within a designed fluent interface.

---

## 8. Composition Over Inheritance

**Definition:** Favor composing objects from smaller, reusable parts (composition/delegation) over creating deep class hierarchies (inheritance) to achieve code reuse.

**Why Inheritance Can Hurt:**

- Creates tight coupling between parent and child
- Fragile Base Class Problem: parent changes break all children
- Deep hierarchies are hard to understand
- Java only allows single inheritance (no multiple inheritance for classes)
- You inherit everything, even what you don't want

**Composition gives you flexibility:**

```java
// BAD - Trying to model coffee with inheritance
public class Coffee { }
public class CoffeeWithMilk extends Coffee { }
public class CoffeeWithSugar extends Coffee { }
public class CoffeeWithMilkAndSugar extends CoffeeWithMilk { } // explosion!
public class IcedCoffeeWithMilk extends CoffeeWithMilk { }    // combinatorial explosion
// 10 variations = potentially 2^10 subclasses needed
```

```java
// GOOD - Composition via Decorator Pattern
public interface Coffee {
    double getCost();
    String getDescription();
}

public class SimpleCoffee implements Coffee {
    public double getCost() { return 50.0; }
    public String getDescription() { return "Simple Coffee"; }
}

// Each add-on wraps (composes) another Coffee
public class MilkDecorator implements Coffee {
    private final Coffee coffee;
    public MilkDecorator(Coffee coffee) { this.coffee = coffee; }
    public double getCost() { return coffee.getCost() + 10.0; }
    public String getDescription() { return coffee.getDescription() + " + Milk"; }
}

public class SugarDecorator implements Coffee {
    private final Coffee coffee;
    public SugarDecorator(Coffee coffee) { this.coffee = coffee; }
    public double getCost() { return coffee.getCost() + 5.0; }
    public String getDescription() { return coffee.getDescription() + " + Sugar"; }
}

// Build any combination at runtime
Coffee myOrder = new MilkDecorator(new SugarDecorator(new SimpleCoffee()));
System.out.println(myOrder.getDescription()); // Simple Coffee + Sugar + Milk
System.out.println(myOrder.getCost());        // 65.0
```

**Another example - replacing inheritance with delegation:**

```java
// BAD - Inheritance just for code reuse (not a true IS-A)
public class Logger {
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

public class UserService extends Logger {  // UserService IS-A Logger? No!
    public void createUser(String name) {
        log("Creating user: " + name);     // just wanted to reuse log()
    }
}
```

```java
// GOOD - Composition (HAS-A makes more sense here)
public class UserService {
    private final Logger logger;    // UserService HAS-A Logger

    public UserService(Logger logger) {
        this.logger = logger;
    }

    public void createUser(String name) {
        logger.log("Creating user: " + name);
    }
}
```

**Rule of thumb:**
- Use inheritance for: IS-A + sharing interface contract + polymorphism needed
- Use composition for: code reuse + behavior variation + HAS-A relationships

---

## 9. Separation of Concerns (SoC)

**Definition:** A software system should be divided into distinct sections, each addressing a separate concern. A concern is any piece of information that affects the code.

**Origin:** Edsger W. Dijkstra, 1974.

**Core idea:** Don't mix business logic with UI logic, persistence logic, validation logic, logging, etc. in the same place.

### SoC in Layered Architecture

```
Presentation Layer (Controllers, DTOs)
        |
Service Layer (Business Logic)
        |
Repository Layer (Data Access / Persistence)
        |
Database
```

```java
// BAD - All concerns mixed in one class
@RestController
public class UserController {
    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody Map<String, String> req) {
        // Validation concern
        if (req.get("email") == null || !req.get("email").contains("@")) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        // Business logic concern
        String hashedPassword = BCrypt.hashpw(req.get("password"), BCrypt.gensalt());

        // Persistence concern
        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";
        jdbcTemplate.update(sql, req.get("email"), hashedPassword);

        // Notification concern
        emailService.sendWelcomeEmail(req.get("email"));

        return ResponseEntity.ok("User created");
    }
}
```

```java
// GOOD - Each concern in its own layer
// Validation concern
public class UserValidator {
    public void validate(CreateUserRequest req) {
        if (req.getEmail() == null || !req.getEmail().contains("@"))
            throw new ValidationException("Invalid email");
    }
}

// Business logic concern
@Service
public class UserService {
    private final UserRepository repo;
    private final EmailService emailService;
    private final UserValidator validator;

    public User createUser(CreateUserRequest req) {
        validator.validate(req);
        User user = User.builder()
            .email(req.getEmail())
            .password(hashPassword(req.getPassword()))
            .build();
        User saved = repo.save(user);
        emailService.sendWelcomeEmail(saved.getEmail());
        return saved;
    }
}

// Persistence concern
@Repository
public class UserRepository {
    public User save(User user) { /* JPA/JDBC */ }
}

// Presentation concern
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest req) {
        User user = userService.createUser(req);
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
```

**SoC in other forms:**
- **Aspect-Oriented Programming (AOP):** Cross-cutting concerns (logging, security, transactions) are separated into Aspects
- **CSS/HTML/JS separation:** Structure, style, and behavior separated
- **MVC pattern:** Model (data), View (UI), Controller (coordination) separated

---

## 10. Fail Fast Principle

**Definition:** A system should report errors as early as possible, immediately when a fault is detected, rather than allowing the system to continue in an invalid state that causes harder-to-diagnose problems later.

```java
// BAD - Fail Late: problems discovered far from the source
public class OrderProcessor {
    private List<Order> orders;

    public OrderProcessor() {
        // orders never initialized - null
    }

    public void addOrder(Order order) {
        orders.add(order);   // NullPointerException here at runtime
                             // but the bug is in the constructor - hard to trace
    }
}
```

```java
// GOOD - Fail Fast: validate at construction, at entry points
public class OrderProcessor {
    private final List<Order> orders;

    public OrderProcessor(List<Order> initialOrders) {
        // Validate all inputs immediately
        Objects.requireNonNull(initialOrders, "Initial orders list cannot be null");
        this.orders = new ArrayList<>(initialOrders);
    }

    public void addOrder(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");
        if (!order.isValid()) throw new InvalidOrderException("Order " + order.getId() + " is invalid");
        orders.add(order);
    }
}
```

**Fail Fast at different levels:**

1. **Constructor/factory validation:** Validate all required parameters before creating the object
2. **Method preconditions:** Validate inputs at the start of every method
3. **Type system:** Use types that prevent invalid states (Optional instead of null, enums instead of strings)
4. **Assertions:** Use `assert` in development/test to catch invariant violations early

```java
// Using Objects.requireNonNull (Java 7+)
public UserService(UserRepository repo, EmailService emailService) {
    this.repo = Objects.requireNonNull(repo, "UserRepository required");
    this.emailService = Objects.requireNonNull(emailService, "EmailService required");
}

// Defensive validation at method entry
public void transferMoney(Account from, Account to, double amount) {
    Objects.requireNonNull(from, "Source account required");
    Objects.requireNonNull(to, "Destination account required");
    if (amount <= 0) throw new IllegalArgumentException("Amount must be positive, got: " + amount);
    if (from.equals(to)) throw new IllegalArgumentException("Cannot transfer to same account");
    // ... actual transfer logic
}
```

**Benefits:**
- Bugs are caught close to their source - easier to diagnose
- Invalid state never propagates through the system
- Stack traces point to the actual source of the problem
- Self-documenting: preconditions serve as documentation of expected inputs

---

## 11. Principle of Least Astonishment (POLA)

**Also known as: Principle of Least Surprise**

**Definition:** A component (method, class, API) should behave in a way that developers would expect based on its name, signature, and context. It should not surprise or astonish its users.

```java
// BAD - Extremely astonishing behavior
public class UserService {
    // Method named "get" that also deletes! Astonishing.
    public User getUser(String id) {
        User user = repo.findById(id);
        repo.delete(id);   // Nobody expects getUser to delete anything
        return user;
    }

    // Method that says "add" but actually replaces
    public void addPermission(User user, String permission) {
        user.clearAllPermissions();   // astonishing!
        user.addPermission(permission);
    }
}
```

```java
// BAD - astonishing null return vs exception behavior inconsistency
public class UserService {
    public User findById(String id) {
        // returns null if not found (no indication in signature)
    }

    public User findByEmail(String email) {
        if (email == null) throw new RuntimeException("email required"); // inconsistent
        // throws exception if not found (different from findById)
    }
}
```

```java
// GOOD - Consistent, unsurprising API
public class UserService {
    // Name makes clear it might not find one
    public Optional<User> findById(String id) {
        return repo.findById(id);  // Optional makes nullability explicit
    }

    // Consistent: Optional used for all lookup methods
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "email required");
        return repo.findByEmail(email);
    }

    // Throws when not found (expectation set by name "get")
    public User getById(String id) {
        return repo.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
}
```

**POLA in practice:**
- Name methods clearly: `findBy` (may return empty), `getBy` (throws if not found), `delete` (mutates), `calculate` (returns value)
- Be consistent: if one method returns Optional, all similar methods should
- Don't cause side effects in getter/query methods
- Follow conventions of the language and framework (e.g., `equals` and `hashCode` should be consistent)

---

## 12. Tell, Don't Ask

**Definition:** Instead of asking an object for its data and then making decisions based on that data, tell the object what you want it to do and let it decide internally.

This principle is closely related to Encapsulation and the Law of Demeter.

```java
// BAD - Asking (procedural style despite using objects)
public class OrderProcessor {
    public void processOrder(Order order) {
        // Asking order for data, then making decisions outside
        if (order.getStatus().equals("PENDING")
            && order.getTotal() > 0
            && order.getCustomer().isActive()
            && !order.getItems().isEmpty()) {
            order.setStatus("PROCESSING");
            paymentGateway.charge(order.getTotal());
        }
    }
}
```

```java
// GOOD - Telling (OOP style, behavior where data lives)
public class Order {
    private String status;
    private double total;
    private Customer customer;
    private List<LineItem> items;

    public boolean isReadyToProcess() {
        return status.equals("PENDING")
            && total > 0
            && customer.isActive()
            && !items.isEmpty();
    }

    public void markAsProcessing() {
        if (!isReadyToProcess()) throw new IllegalStateException("Order not ready");
        this.status = "PROCESSING";
    }
}

public class OrderProcessor {
    public void processOrder(Order order) {
        if (order.isReadyToProcess()) {      // Tell order to assess itself
            order.markAsProcessing();         // Tell order to change its own state
            paymentGateway.charge(order.getTotal());
        }
    }
}
```

**Why it matters:**
- Data and the logic that operates on that data are co-located (high cohesion)
- Objects are responsible for their own invariants
- Callers don't need to know the internals of objects they use

---

## 13. High Cohesion and Low Coupling

These two ideas are often mentioned together and are arguably the meta-goal of most design principles.

### Cohesion

**How strongly related are the responsibilities within a single class/module?**

| Type | Description | Quality |
|---|---|---|
| Functional | Class does exactly one well-defined task | Best |
| Sequential | Methods use output of the previous method | Good |
| Communicational | Methods operate on the same data | Acceptable |
| Procedural | Methods grouped arbitrarily by execution order | Weak |
| Logical | Methods grouped because they're "similar" (e.g., all I/O) | Weak |
| Coincidental | Methods grouped for no real reason | Worst |

### Coupling

**How much does one class depend on the internals of another?**

| Type | Description | Quality |
|---|---|---|
| No coupling | Classes are totally independent | Best |
| Data coupling | Classes share only simple data (parameters) | Good |
| Stamp coupling | Classes share complex data structures | Acceptable |
| Control coupling | One class controls the behavior of another via flags | Weak |
| External coupling | Multiple classes depend on same external format/protocol | Weak |
| Content coupling | One class directly accesses internals of another | Worst |

**The goal:** Maximize cohesion within each class. Minimize coupling between classes.

```java
// Low cohesion + high coupling (worst)
public class GodClass {
    private User user;
    private Database db;
    private HttpClient http;

    public void doEverything(String userId) {
        user = db.query("SELECT * FROM users WHERE id = " + userId); // internal SQL
        http.post("https://api.analytics.com", user.toJson());       // external API knowledge
        System.out.println("Hello " + user.getName());               // UI concern
    }
}

// High cohesion + low coupling (best)
public class UserRepository {  // cohesive: only persistence concerns
    public Optional<User> findById(String id) { ... }
}
public class AnalyticsService { // cohesive: only analytics concerns
    public void track(User user) { ... }
}
public class UserPresenter {    // cohesive: only presentation concerns
    public String greet(User user) { return "Hello " + user.getName(); }
}
```

---

## 14. Package Design Principles

Beyond class-level design, Uncle Bob also defined principles for organizing classes into packages.

### REP - Reuse/Release Equivalence Principle

Classes in a package should be reused together and released together. Package is the unit of release.

### CCP - Common Closure Principle

Classes that change together should be in the same package. If a change affects a package, it should affect only classes in that package. This is SRP applied to packages.

### CRP - Common Reuse Principle

Classes that are reused together belong in the same package. Don't force users of a package to depend on classes they don't use.

### ADP - Acyclic Dependencies Principle

There should be no cycles in the package dependency graph. Cycles make independent builds and releases impossible.

```
GOOD:                    BAD:
A --> B --> C            A --> B --> C --> A  (cycle!)
|                        Circular dependency
+--> D
```

### SDP - Stable Dependencies Principle

Packages should depend only in the direction of stability. Depend on packages that are more stable than you are.

### SAP - Stable Abstractions Principle

A package should be as abstract as it is stable. The most stable packages should be the most abstract. Instability and concreteness go together.

---

## 15. How Principles Work Together

All these principles are interconnected. Here's how they complement each other:

```
OOP provides the foundation
  |
  +-- Encapsulation --> Supports: SRP, Tell-Don't-Ask, LoD
  +-- Abstraction  --> Enables: OCP, DIP, ISP
  +-- Inheritance  --> Governed by: LSP, Composition-over-Inheritance
  +-- Polymorphism --> Enables: OCP, Strategy/Command patterns

SOLID builds on OOP
  |
  +-- SRP  <--> High Cohesion (same idea, different scope)
  +-- OCP  --> Uses: Polymorphism, Abstraction
  +-- LSP  --> Governs: Inheritance use
  +-- ISP  <--> Low Coupling (fat interfaces increase coupling)
  +-- DIP  --> Uses: Abstraction, Dependency Injection

Simplicity principles govern over-engineering
  |
  +-- KISS  --> Opposes: premature abstraction from OCP/DIP
  +-- YAGNI --> Opposes: speculative generality from OCP
  +-- DRY   --> Relates to: SRP (duplication often means missing abstraction)

GRASP provides assignment guidelines
  |
  +-- Information Expert --> Guides: method placement
  +-- Low Coupling       <--> DIP, ISP, LoD
  +-- High Cohesion      <--> SRP, SoC

Meta-principles
  +-- SoC    --> Achieved by: SRP + Layered Architecture
  +-- Fail Fast --> Implemented via: Preconditions + Encapsulation
  +-- POLA   --> Governs: API design and naming
```

**Practical Priority for LLD Interviews:**
1. Design classes around OOP (identify entities, attributes, behaviors)
2. Apply SRP to each class (one responsibility, one reason to change)
3. Apply OCP when variation points are identified (use interfaces/abstractions)
4. Verify LSP for all inheritance hierarchies
5. Apply ISP when interfaces start getting fat
6. Apply DIP to wire everything together (inject dependencies)
7. Apply KISS and YAGNI to avoid over-engineering
8. Apply DRY to remove duplication
9. Apply LoD to clean up coupling in method implementations

---

## 16. Interview Questions and Answers

**Q: What are the four pillars of OOP?**

Encapsulation (hiding data behind controlled access), Abstraction (exposing only relevant details), Inheritance (IS-A reuse of parent behavior), and Polymorphism (one interface, multiple behaviors at runtime).

**Q: What is the difference between Abstraction and Encapsulation?**

Encapsulation is about hiding the internal implementation and protecting data from unauthorized access (how data is stored and modified). Abstraction is about hiding complexity from the user and showing only the relevant interface (what an object does, not how). Encapsulation is achieved via access modifiers; Abstraction is achieved via abstract classes and interfaces.

**Q: Explain SRP with a real example.**

An `Invoice` class should be responsible for representing invoice data. It should not also handle saving to database (that's `InvoiceRepository`'s job) or sending emails (that's `EmailService`'s job). If any of those three concerns change, only the relevant class changes.

**Q: What is the Open/Closed Principle? How do you achieve it?**

A class should be open for extension but closed for modification. You achieve it by programming to interfaces, so new behavior can be added by creating a new implementing class rather than changing existing code. The Strategy pattern is the classic implementation of OCP.

**Q: What is the Liskov Substitution Principle? Give an example of a violation.**

Subtypes must be substitutable for their parent types. Classic violation: Square extending Rectangle. A `resize(Rectangle r)` function that independently sets width and height will produce wrong results when passed a Square (because Square forces both equal). The fix is to not force this inheritance — use a common Shape interface instead.

**Q: What is the difference between DIP and Dependency Injection?**

DIP is the principle: "high-level modules should depend on abstractions, not concrete implementations." Dependency Injection is a design pattern/technique that implements DIP by providing (injecting) the concrete dependency from outside the class, typically through the constructor.

**Q: What is the difference between DRY and KISS?**

DRY is about eliminating duplication — every piece of knowledge should have one authoritative representation. KISS is about avoiding unnecessary complexity — the simplest solution that works is preferred. They are complementary: DRY removes redundancy, KISS prevents over-engineering.

**Q: What is YAGNI and when should it override other principles?**

YAGNI says don't build features until they are needed. It overrides OCP/DIP when you're tempted to add abstractions speculatively ("we might need multiple implementations later"). The correct time to add an abstraction is when the second implementation actually arrives, not before.

**Q: What is the Law of Demeter?**

Only talk to immediate friends. A method should only call methods on: the current object, its direct fields, parameters passed to it, and objects it creates. Avoid chaining through other objects (`a.getB().getC().doSomething()`). This reduces coupling.

**Q: When would you prefer Composition over Inheritance?**

When you want code reuse without a true IS-A relationship, when you need to mix and match behaviors at runtime, when deep inheritance hierarchies are becoming hard to maintain, or when the Fragile Base Class Problem is causing issues. Example: Decorator pattern uses composition to add behaviors to objects dynamically.

**Q: What is Separation of Concerns? How does it relate to SRP?**

SoC says that a system should be divided so that each part addresses a separate concern. SRP is SoC applied to a single class. SoC is more architectural (e.g., separate layers for persistence, business logic, presentation), while SRP is more class-level.

**Q: What is the difference between high cohesion and low coupling?**

Cohesion is about how related the things inside a single class are to each other (should be HIGH). Coupling is about how dependent classes are on each other (should be LOW). The goal is to maximize cohesion and minimize coupling simultaneously. Most SOLID principles are mechanisms to achieve this goal.

**Q: What is Fail Fast? How do you implement it in Java?**

Fail Fast means detecting and reporting errors as early as possible rather than propagating invalid state. In Java: validate constructor parameters with `Objects.requireNonNull()`, add method preconditions at entry points, throw meaningful exceptions immediately when invalid state is detected, use assertions for invariant checks.

---

## 17. Quick Reference Cheat Sheet

```
OOP PILLARS
  Encapsulation   - Hide data; expose controlled interface via access modifiers
  Abstraction     - Hide complexity; expose only what's needed (interfaces, abstract classes)
  Inheritance     - IS-A reuse; governed by LSP; prefer composition for HAS-A
  Polymorphism    - One interface, many behaviors; enables OCP; runtime dispatch

SOLID
  S - SRP  - One class, one reason to change
  O - OCP  - Open for extension, closed for modification (use interfaces)
  L - LSP  - Subtypes must be substitutable for their base types
  I - ISP  - Many small interfaces > one fat interface
  D - DIP  - Depend on abstractions; inject concrete dependencies from outside

SIMPLICITY
  DRY    - Single source of truth; no duplication of knowledge
  KISS   - Simplest solution that works; avoid speculative complexity
  YAGNI  - Build only what's needed now; no "just in case" features

GRASP
  Information Expert  - Assign responsibility to who has the info
  Creator             - Assign creation to who contains/aggregates
  Low Coupling        - Minimize class dependencies
  High Cohesion       - Related responsibilities together
  Polymorphism        - Use polymorphism over type-checking conditionals
  Protected Variation - Wrap instability behind stable interfaces

OTHER PRINCIPLES
  Law of Demeter         - Only talk to immediate friends; avoid chaining
  Composition > Inherit  - HAS-A flexibility > IS-A rigidity
  Separation of Concerns - Each layer/module addresses one concern
  Fail Fast              - Detect and report errors at the earliest possible point
  Tell, Don't Ask        - Tell objects what to do; don't extract data and decide externally
  POLA                   - Don't surprise callers; behavior matches name and convention
  High Cohesion          - Maximize relatedness within a class
  Low Coupling           - Minimize dependencies between classes

META-GOAL OF ALL PRINCIPLES:
  --> Maintainable, readable, extensible, testable code
  --> Easy to change: one change = one place = one reason
```

---

*Cover: OOP fundamentals, all 5 SOLID principles, DRY, KISS, YAGNI, all 9 GRASP principles, Law of Demeter, Composition over Inheritance, Separation of Concerns, Fail Fast, Tell-Don't-Ask, POLA, Cohesion & Coupling, Package Design Principles, interview Q&A, and a cheat sheet.*
