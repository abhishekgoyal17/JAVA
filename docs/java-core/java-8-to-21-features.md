# Java 8 to Java 21 Features - Complete Interview Deep Dive

> A practical interview guide to modern Java features: lambdas, functional interfaces, streams, Optional, default methods, date/time API, CompletableFuture, var, records, sealed classes, pattern matching, switch expressions, text blocks, and virtual threads.

---

## Table of Contents

1. [Why Java 8 to 21 Matters](#1-why-java-8-to-21-matters)
2. [Java 8 - Lambdas](#2-java-8---lambdas)
3. [Functional Interfaces](#3-functional-interfaces)
4. [Method References](#4-method-references)
5. [Streams](#5-streams)
6. [Optional](#6-optional)
7. [Default and Static Interface Methods](#7-default-and-static-interface-methods)
8. [Date and Time API](#8-date-and-time-api)
9. [CompletableFuture](#9-completablefuture)
10. [Java 9 to 11 Features](#10-java-9-to-11-features)
11. [var and Local Type Inference](#11-var-and-local-type-inference)
12. [Text Blocks](#12-text-blocks)
13. [Records](#13-records)
14. [Sealed Classes](#14-sealed-classes)
15. [Pattern Matching](#15-pattern-matching)
16. [Switch Expressions](#16-switch-expressions)
17. [Virtual Threads](#17-virtual-threads)
18. [Interview Questions and Answers](#18-interview-questions-and-answers)
19. [Quick Reference Cheat Sheet](#19-quick-reference-cheat-sheet)

---

## 1. Why Java 8 to 21 Matters

Java interviews often test two things:

```
1. Core Java fundamentals
2. Whether you can write modern Java instead of old verbose Java
```

Timeline:

```
Java 8   -> Lambdas, Streams, Optional, new Date/Time API
Java 9   -> Modules, List.of, Map.of, Stream improvements
Java 10  -> var
Java 11  -> HTTP Client, String methods, LTS
Java 14+ -> Switch expressions mature
Java 15  -> Text blocks
Java 16  -> Records
Java 17  -> Sealed classes, LTS
Java 21  -> Virtual threads, pattern matching for switch, LTS
```

Interview expectation:

You do not need to memorize every release note. You should deeply understand features that changed day-to-day backend Java:

- Lambdas and functional interfaces
- Streams
- Optional
- Records
- Sealed classes
- Pattern matching
- Switch expressions
- Virtual threads

---

## 2. Java 8 - Lambdas

A lambda is a concise way to pass behavior as data.

Before Java 8:

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Running");
    }
};
```

With lambda:

```java
Runnable task = () -> System.out.println("Running");
```

Mental model:

```
Lambda = implementation of a functional interface method
```

Example:

```java
Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());
```

This implements:

```java
int compare(String a, String b);
```

### Lambda syntax

```java
() -> System.out.println("No args")

x -> x * x

(a, b) -> a + b

(a, b) -> {
    int sum = a + b;
    return sum;
}
```

ASCII:

```
Input parameters      Arrow      Body
        |               |         |
     (a, b)            ->       a + b
```

### Effectively final variable

```java
int factor = 10;

Function<Integer, Integer> multiply = x -> x * factor;

// factor = 20; // compile error if uncommented
```

Local variables captured by lambdas must be final or effectively final.

Why?

```
Local variable lives on stack.
Lambda may outlive method call.
Java captures the value safely only if it does not change.
```

---

## 3. Functional Interfaces

A functional interface has exactly one abstract method.

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}
```

Usage:

```java
Calculator add = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;
```

`@FunctionalInterface` is optional but recommended. It lets the compiler protect the contract.

Common built-in functional interfaces:

| Interface | Method | Use |
|---|---|---|
| `Predicate<T>` | `boolean test(T t)` | Condition |
| `Function<T, R>` | `R apply(T t)` | Transform |
| `Consumer<T>` | `void accept(T t)` | Side-effect |
| `Supplier<T>` | `T get()` | Lazy supply |
| `UnaryOperator<T>` | `T apply(T t)` | T to T |
| `BinaryOperator<T>` | `T apply(T a, T b)` | Combine two T values |

Examples:

```java
Predicate<String> isLong = s -> s.length() > 5;
Function<String, Integer> length = String::length;
Consumer<String> printer = System.out::println;
Supplier<UUID> uuid = UUID::randomUUID;
```

---

## 4. Method References

Method references are shorthand for lambdas that call an existing method.

```java
names.forEach(name -> System.out.println(name));
names.forEach(System.out::println);
```

Types:

| Type | Example | Equivalent lambda |
|---|---|---|
| Static method | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instance method of object | `System.out::println` | `x -> System.out.println(x)` |
| Instance method of type | `String::toLowerCase` | `s -> s.toLowerCase()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

Interview point:

Use method references when they improve readability. Lambdas are better when logic is not just a direct method call.

---

## 5. Streams

A Stream is a pipeline for processing data.

```java
List<String> names = List.of("amit", "neha", "arjun");

List<String> result = names.stream()
    .filter(name -> name.length() > 4)
    .map(String::toUpperCase)
    .toList();
```

Pipeline:

```
Source -> intermediate operations -> terminal operation

List
  -> filter
  -> map
  -> toList
```

### Intermediate vs terminal operations

Intermediate operations are lazy:

```java
filter()
map()
sorted()
distinct()
limit()
skip()
```

Terminal operations trigger execution:

```java
collect()
toList()
forEach()
count()
reduce()
anyMatch()
findFirst()
```

Example:

```java
Stream<String> stream = names.stream()
    .filter(name -> {
        System.out.println("filtering " + name);
        return name.length() > 4;
    });

// Nothing printed yet.

long count = stream.count();
// Now pipeline executes.
```

### Stream execution diagram

```
names: [amit, neha, arjun]

filter length > 4:
  amit  -> false
  neha  -> false
  arjun -> true

map uppercase:
  arjun -> ARJUN

result:
  [ARJUN]
```

### map vs flatMap

`map` transforms one element into one result.

```java
List<Integer> lengths = names.stream()
    .map(String::length)
    .toList();
```

`flatMap` flattens nested streams.

```java
List<List<String>> groups = List.of(
    List.of("A", "B"),
    List.of("C", "D")
);

List<String> all = groups.stream()
    .flatMap(List::stream)
    .toList();
```

ASCII:

```
map:
[A, B] -> stream/list object
[C, D] -> stream/list object

flatMap:
[A, B], [C, D] -> A, B, C, D
```

### reduce

```java
int sum = List.of(1, 2, 3, 4).stream()
    .reduce(0, Integer::sum);
```

Flow:

```
identity = 0
0 + 1 = 1
1 + 2 = 3
3 + 3 = 6
6 + 4 = 10
```

### Streams are single-use

```java
Stream<String> stream = names.stream();
stream.count();
// stream.toList(); // IllegalStateException
```

Create a new stream for another operation.

### Parallel streams

```java
names.parallelStream()
    .map(this::expensiveOperation)
    .toList();
```

Use carefully.

Good candidates:

- CPU-heavy independent operations
- Large data sets
- No shared mutable state

Bad candidates:

- Blocking I/O
- Small lists
- Operations with side effects
- Code already running inside server thread pools without planning

---

## 6. Optional

`Optional<T>` represents a value that may or may not exist.

Before:

```java
User user = repository.findById(id);
if (user != null) {
    return user.getEmail();
}
return "unknown";
```

With Optional:

```java
String email = repository.findById(id)
    .map(User::getEmail)
    .orElse("unknown");
```

Good return type:

```java
Optional<User> findById(long id);
```

Bad field usage:

```java
class User {
    private Optional<String> email; // usually avoid
}
```

Optional is primarily for return types, not fields, DTO properties, or method parameters in most codebases.

### orElse vs orElseGet

```java
String value = optional.orElse(expensiveDefault());
```

`expensiveDefault()` runs even if optional has a value.

```java
String value = optional.orElseGet(() -> expensiveDefault());
```

Supplier runs only when optional is empty.

ASCII:

```
orElse:
  default computed immediately

orElseGet:
  default computed lazily only if needed
```

### Avoid get without check

```java
Optional<User> user = repository.findById(id);
User value = user.get(); // risky
```

Better:

```java
User value = user.orElseThrow(() -> new UserNotFoundException(id));
```

---

## 7. Default and Static Interface Methods

Java 8 allowed interfaces to have default methods.

```java
interface Vehicle {
    void start();

    default void stop() {
        System.out.println("Stopping");
    }
}
```

Why?

To evolve interfaces without breaking all existing implementations.

Example:

```
Before Java 8:
  Add method to interface -> all implementors break

After Java 8:
  Add default method -> old implementors still compile
```

Static methods in interfaces:

```java
interface Validators {
    static boolean isEmail(String value) {
        return value != null && value.contains("@");
    }
}
```

Call:

```java
Validators.isEmail("a@test.com");
```

### Diamond conflict

```java
interface A {
    default void log() {
        System.out.println("A");
    }
}

interface B {
    default void log() {
        System.out.println("B");
    }
}

class C implements A, B {
    @Override
    public void log() {
        A.super.log();
    }
}
```

If two interfaces provide same default method, implementing class must resolve the conflict.

---

## 8. Date and Time API

Java 8 introduced `java.time`, replacing many uses of mutable `Date` and `Calendar`.

Common types:

| Type | Meaning |
|---|---|
| `LocalDate` | Date without time zone |
| `LocalTime` | Time without date/time zone |
| `LocalDateTime` | Date-time without time zone |
| `ZonedDateTime` | Date-time with time zone |
| `Instant` | Machine timestamp in UTC |
| `Duration` | Time-based amount |
| `Period` | Date-based amount |

Examples:

```java
LocalDate today = LocalDate.now();
LocalDate nextWeek = today.plusWeeks(1);

Instant now = Instant.now();
ZonedDateTime indiaTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
```

Important:

```
LocalDateTime has no timezone.
Instant is best for machine timestamps.
ZonedDateTime is best for user-facing timezone-aware time.
```

---

## 9. CompletableFuture

`CompletableFuture` supports async programming and composing async tasks.

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> fetchUser());

String result = future.join();
```

### thenApply vs thenCompose

`thenApply` transforms value:

```java
CompletableFuture<String> nameFuture =
    userFuture.thenApply(User::getName);
```

Flow:

```
CompletableFuture<User>
  -> thenApply(User::getName)
CompletableFuture<String>
```

`thenCompose` flattens nested futures:

```java
CompletableFuture<Order> orderFuture =
    userFuture.thenCompose(user -> fetchOrder(user.getId()));
```

Without compose:

```java
CompletableFuture<CompletableFuture<Order>> nested =
    userFuture.thenApply(user -> fetchOrder(user.getId()));
```

ASCII:

```
thenApply:
  T -> R

thenCompose:
  T -> CompletableFuture<R>
  flattened to CompletableFuture<R>
```

### Combine independent tasks

```java
CompletableFuture<User> user = fetchUserAsync(id);
CompletableFuture<List<Order>> orders = fetchOrdersAsync(id);

CompletableFuture<UserProfile> profile =
    user.thenCombine(orders, UserProfile::new);
```

### Error handling

```java
CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> riskyCall())
    .exceptionally(ex -> "fallback");
```

or:

```java
CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> riskyCall())
    .handle((value, ex) -> ex == null ? value : "fallback");
```

---

## 10. Java 9 to 11 Features

### Factory methods for collections

```java
List<String> names = List.of("A", "B");
Set<Integer> numbers = Set.of(1, 2, 3);
Map<String, Integer> scores = Map.of("A", 90, "B", 95);
```

These collections are unmodifiable.

```java
names.add("C"); // UnsupportedOperationException
```

They also reject null:

```java
// List.of("A", null); // NullPointerException
```

### Stream improvements

```java
Stream.of(1, 2, 3, 4, 5)
    .takeWhile(n -> n < 4)
    .toList(); // 1, 2, 3

Stream.of(1, 2, 3, 4, 5)
    .dropWhile(n -> n < 4)
    .toList(); // 4, 5
```

### Java 11 String methods

```java
" ".isBlank();                 // true
" Java ".strip();              // "Java"
"a\nb".lines().count();        // 2
"ha".repeat(3);                // "hahaha"
```

### HTTP Client

```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://example.com"))
    .GET()
    .build();

HttpResponse<String> response =
    client.send(request, HttpResponse.BodyHandlers.ofString());
```

---

## 11. var and Local Type Inference

Java 10 introduced `var` for local variables.

```java
var name = "Java";
var count = 10;
var users = new ArrayList<User>();
```

Compiler infers type:

```
name  -> String
count -> int
users -> ArrayList<User>
```

Rules:

```java
// var x;              // not allowed, no initializer
// var y = null;       // not allowed, cannot infer
// class User { var x; } // not allowed for fields
```

Use `var` when it improves readability:

```java
var response = paymentGateway.charge(request);
```

Avoid when it hides important type:

```java
var data = service.getData(); // unclear if type is not obvious
```

Interview answer:

> `var` is compile-time local type inference. It does not make Java dynamically typed. The variable still has a fixed static type.

---

## 12. Text Blocks

Text blocks make multi-line strings readable.

Before:

```java
String json = "{\n" +
    "  \"name\": \"Amit\",\n" +
    "  \"role\": \"admin\"\n" +
    "}";
```

With text block:

```java
String json = """
    {
      "name": "Amit",
      "role": "admin"
    }
    """;
```

Useful for:

- JSON examples
- SQL
- HTML templates
- Test fixtures

Example SQL:

```java
String sql = """
    SELECT id, name, email
    FROM users
    WHERE status = ?
    ORDER BY created_at DESC
    """;
```

---

## 13. Records

Records are concise immutable data carriers.

Before:

```java
public final class UserDto {
    private final long id;
    private final String email;

    public UserDto(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public long id() {
        return id;
    }

    public String email() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        // generated manually
    }

    @Override
    public int hashCode() {
        // generated manually
    }
}
```

With record:

```java
public record UserDto(long id, String email) { }
```

Compiler generates:

```
private final fields
canonical constructor
accessor methods: id(), email()
equals()
hashCode()
toString()
```

### Compact constructor

```java
public record UserDto(long id, String email) {
    public UserDto {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email required");
        }
    }
}
```

### Record limitations

- Cannot extend another class
- Fields are final
- Good for data, not complex mutable entities
- Can implement interfaces

Good uses:

```java
record Point(int x, int y) { }
record UserResponse(long id, String email) { }
record Money(BigDecimal amount, String currency) { }
```

Bad uses:

```java
// Mutable JPA entity with lazy relationships
// Complex domain object with changing lifecycle state
```

---

## 14. Sealed Classes

Sealed classes restrict which classes can extend or implement them.

```java
public sealed interface Payment
    permits CardPayment, UpiPayment, WalletPayment {
}

public final class CardPayment implements Payment { }
public final class UpiPayment implements Payment { }
public final class WalletPayment implements Payment { }
```

Mental model:

```
Payment can only be:
  CardPayment
  UpiPayment
  WalletPayment
```

Why useful:

- Model closed hierarchies
- Make domain states explicit
- Improve switch exhaustiveness
- Prevent unknown subclasses

Example:

```java
public sealed interface OrderState
    permits Created, Paid, Shipped, Cancelled {
}

public final class Created implements OrderState { }
public final class Paid implements OrderState { }
public final class Shipped implements OrderState { }
public final class Cancelled implements OrderState { }
```

Subclasses must be one of:

| Modifier | Meaning |
|---|---|
| `final` | Cannot be extended further |
| `sealed` | Further restricted hierarchy |
| `non-sealed` | Opens extension again |

---

## 15. Pattern Matching

Pattern matching reduces boilerplate around type checks and casts.

Before:

```java
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.toUpperCase());
}
```

With pattern matching:

```java
if (obj instanceof String s) {
    System.out.println(s.toUpperCase());
}
```

Flow:

```
obj instanceof String s
  |
  +-- if true, s is available as String inside block
  +-- if false, block not entered
```

### Pattern matching for switch

```java
static String describe(Object obj) {
    return switch (obj) {
        case String s -> "String length " + s.length();
        case Integer i -> "Integer " + i;
        case null -> "null";
        default -> "unknown";
    };
}
```

This is powerful with sealed classes:

```java
static String handle(Payment payment) {
    return switch (payment) {
        case CardPayment card -> "card";
        case UpiPayment upi -> "upi";
        case WalletPayment wallet -> "wallet";
    };
}
```

If the sealed hierarchy is fully covered, no default may be needed.

---

## 16. Switch Expressions

Old switch statement:

```java
String label;
switch (status) {
    case 1:
        label = "NEW";
        break;
    case 2:
        label = "PAID";
        break;
    default:
        label = "UNKNOWN";
}
```

Switch expression:

```java
String label = switch (status) {
    case 1 -> "NEW";
    case 2 -> "PAID";
    default -> "UNKNOWN";
};
```

Benefits:

- No accidental fall-through
- Returns a value
- More concise
- Better exhaustiveness with enums/sealed types

Multiple labels:

```java
String type = switch (day) {
    case SATURDAY, SUNDAY -> "WEEKEND";
    default -> "WEEKDAY";
};
```

Block with `yield`:

```java
String message = switch (status) {
    case 200 -> "OK";
    case 400 -> {
        log.warn("Bad request");
        yield "BAD_REQUEST";
    }
    default -> "UNKNOWN";
};
```

---

## 17. Virtual Threads

Virtual threads became a final feature in Java 21.

They are lightweight threads managed by the JVM, designed for high-concurrency blocking I/O workloads.

Traditional platform thread:

```
Java Thread -> OS Thread -> Kernel scheduled
```

Virtual thread:

```
Virtual Thread -> JVM scheduler -> small pool of carrier OS threads
```

ASCII:

```
Virtual threads:

VT-1 ----+
VT-2 ----+
VT-3 ----+----> JVM scheduler ----> Carrier Thread 1
VT-4 ----+                         Carrier Thread 2
VT-5 ----+                         Carrier Thread 3
```

### Why virtual threads exist

Server apps often block on I/O:

```
HTTP request
  -> call DB
  -> wait
  -> call another service
  -> wait
  -> return response
```

With platform threads, each blocked request holds an expensive OS thread.

With virtual threads, when code blocks on supported blocking I/O, the JVM can unmount the virtual thread from the carrier thread.

```
Virtual thread blocks on DB
  |
  v
JVM unmounts it
  |
  v
Carrier thread runs another virtual thread
```

### Creating virtual threads

```java
Thread.startVirtualThread(() -> {
    System.out.println("Running in virtual thread");
});
```

Executor style:

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> callRemoteService());
    }
}
```

### Virtual threads are not faster CPU threads

Virtual threads help with blocking I/O concurrency, not CPU-heavy parallelism.

Bad use:

```java
// CPU-heavy computation
for (int i = 0; i < cores * 1000; i++) {
    Thread.startVirtualThread(() -> calculatePrimeNumbers());
}
```

CPU-bound work is still limited by CPU cores.

Good use:

```java
Thread.startVirtualThread(() -> {
    String user = callUserService();
    String orders = callOrderService();
});
```

### Virtual threads and synchronized pinning

Some blocking operations can pin a virtual thread to its carrier, reducing scalability.

Important guidance:

- Avoid long blocking operations inside `synchronized`
- Prefer `ReentrantLock` for virtual-thread-friendly locking in many cases
- Keep critical sections small

Interview answer:

> Virtual threads make the thread-per-request style scalable for blocking I/O. They are cheap to create and block, but they do not make CPU-bound code magically faster.

---

## 18. Interview Questions and Answers

### Q1. What are the most important Java 8 features?

Lambdas, functional interfaces, streams, Optional, default methods in interfaces, CompletableFuture, and the new Date/Time API.

### Q2. What is a lambda?

A lambda is a concise implementation of a functional interface. It lets behavior be passed as data.

### Q3. What is a functional interface?

An interface with exactly one abstract method. It can have default and static methods. Examples include `Predicate`, `Function`, `Consumer`, and `Supplier`.

### Q4. What is the difference between map and flatMap?

`map` transforms one input into one output. `flatMap` transforms one input into a stream/collection of outputs and flattens the result.

### Q5. Are streams lazy?

Intermediate operations are lazy. They execute only when a terminal operation such as `collect`, `count`, or `forEach` is invoked.

### Q6. Can a stream be reused?

No. A stream is single-use. Once a terminal operation runs, the stream is consumed.

### Q7. When should you avoid parallel streams?

Avoid them for small data sets, blocking I/O, shared mutable state, order-sensitive side effects, and code running inside managed server environments without understanding the common ForkJoinPool impact.

### Q8. What is Optional used for?

Optional expresses possible absence of a return value. It reduces null-check boilerplate when used correctly. It is usually best as a return type, not as a field or method parameter.

### Q9. Difference between orElse and orElseGet?

`orElse` evaluates the default value immediately. `orElseGet` evaluates the supplier lazily only if the Optional is empty.

### Q10. What is var?

`var` is local variable type inference. Java remains statically typed; the compiler infers a fixed type from the initializer.

### Q11. What are records?

Records are concise immutable data carriers that automatically provide constructor, accessors, `equals()`, `hashCode()`, and `toString()`.

### Q12. What are sealed classes?

Sealed classes restrict which classes can extend or implement them. They are useful for closed domain hierarchies and exhaustive switch handling.

### Q13. What is pattern matching for instanceof?

It combines type check and cast:

```java
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

### Q14. What are switch expressions?

Switch expressions return a value and use arrow syntax to avoid accidental fall-through.

### Q15. What are virtual threads?

Virtual threads are lightweight JVM-managed threads introduced as a final feature in Java 21. They are designed for scalable blocking I/O and allow a thread-per-request style without one OS thread per request.

### Q16. Are virtual threads good for CPU-bound work?

No. CPU-bound work is limited by CPU cores. Virtual threads mainly improve scalability for blocking I/O workloads.

### Q17. thenApply vs thenCompose?

`thenApply` maps a value from T to R. `thenCompose` maps T to `CompletableFuture<R>` and flattens the nested future.

### Q18. Why were default methods added to interfaces?

To allow interfaces to evolve without breaking existing implementations, especially for adding methods to collection interfaces in Java 8.

---

## 19. Quick Reference Cheat Sheet

### Feature map

| Feature | Version | Why it matters |
|---|---:|---|
| Lambdas | 8 | Pass behavior concisely |
| Streams | 8 | Declarative collection processing |
| Optional | 8 | Express absent return values |
| Date/Time API | 8 | Immutable modern date handling |
| CompletableFuture | 8 | Async composition |
| List.of/Map.of | 9 | Compact unmodifiable collections |
| var | 10 | Local type inference |
| HTTP Client | 11 | Built-in modern HTTP |
| Text blocks | 15 | Multi-line strings |
| Records | 16 | Immutable data carriers |
| Sealed classes | 17 | Restricted hierarchies |
| Pattern matching switch | 21 | Cleaner type-based branching |
| Virtual threads | 21 | Scalable blocking I/O concurrency |

### Stream rules

```
Intermediate operations are lazy.
Terminal operations trigger execution.
Streams are single-use.
Avoid side effects in streams.
Use parallel streams carefully.
```

### Optional rules

```
Use for return values.
Avoid Optional fields in most DTO/entities.
Avoid Optional.get without checking.
Use orElseGet for expensive defaults.
```

### Virtual thread rules

```
Great for blocking I/O.
Not a CPU-speed booster.
Cheap to create.
Use thread-per-task style.
Avoid long blocking synchronized sections.
```

### Strong interview summary

> Modern Java moved the language toward concise, expressive, safer backend code. Java 8 introduced lambdas, streams, Optional, and CompletableFuture. Later versions added local type inference, text blocks, records, sealed classes, pattern matching, switch expressions, and Java 21 virtual threads. For interviews, focus less on memorizing versions and more on explaining the trade-offs, pitfalls, and production use cases.

