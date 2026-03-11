# 🚨 Complete Java Exception Handling Guide
### From Basics to Production-Level Design

> **Purpose:** Master exception handling for LLD interviews and real-world Java development.

---

## 📚 Table of Contents

1. [What is an Exception?](#1-what-is-an-exception)
2. [Exception Hierarchy in Java](#2-exception-hierarchy-in-java)
3. [Checked vs Unchecked Exceptions](#3-checked-vs-unchecked-exceptions)
4. [try-catch-finally](#4-try-catch-finally)
5. [throw vs throws](#5-throw-vs-throws)
6. [Custom Exception Hierarchy](#6-custom-exception-hierarchy)
7. [Multi-catch and Exception Chaining](#7-multi-catch-and-exception-chaining)
8. [try-with-resources](#8-try-with-resources)
9. [Best Practices](#9-best-practices)
10. [Anti-Patterns — What NOT to Do](#10-anti-patterns--what-not-to-do)
11. [Complete LLD Example — Banking System](#11-complete-lld-example--banking-system)
12. [Interview Cheat Sheet](#12-interview-cheat-sheet)

---

## 1. What is an Exception?

An exception is an **event that disrupts normal program flow**.

```
Normal Flow:
method1() → method2() → method3() → ✅ returns result

Exception Flow:
method1() → method2() → method3() → 💥 Exception thrown
                                         ↓
                         JVM unwinds the call stack
                                         ↓
                         Looks for a matching catch block
                                         ↓
                         If none found → program crashes
```

### Two Types of Problems

```java
// TYPE 1: Programming mistake → should NEVER happen in production
int[] arr = new int[5];
arr[10] = 1; // ArrayIndexOutOfBoundsException — YOUR bug, fix the code

// TYPE 2: Expected real-world failure → must be handled gracefully
File file = new File("config.json");
file.read(); // FileNotFoundException — file may not exist, HANDLE it
```

---

## 2. Exception Hierarchy in Java

```
Throwable
├── Error                          ← JVM problems, DO NOT catch these
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── VirtualMachineError
│
└── Exception                      ← Application problems, handle these
    ├── IOException                ← CHECKED
    ├── SQLException               ← CHECKED
    ├── ClassNotFoundException     ← CHECKED
    │
    └── RuntimeException           ← UNCHECKED
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IllegalStateException
        ├── ArrayIndexOutOfBoundsException
        ├── ClassCastException
        └── ArithmeticException
```

### Key Rule

```
Error            → JVM is dying. Don't catch. Let it crash.
Checked          → Must handle or declare. Compiler forces you.
RuntimeException → Optional to handle. Usually YOUR programming mistake.
```

---

## 3. Checked vs Unchecked Exceptions

### Checked Exception — Compiler FORCES you to handle

```java
// Compiler says: "This might fail — you MUST handle it"

// ❌ This won't even compile
public void readFile(String path) {
    FileReader reader = new FileReader(path); // Compile error!
}

// ✅ Option 1: Handle it with try-catch
public void readFile(String path) {
    try {
        FileReader reader = new FileReader(path);
    } catch (FileNotFoundException e) {
        System.out.println("File not found: " + path);
    }
}

// ✅ Option 2: Declare it with throws (pass responsibility to caller)
public void readFile(String path) throws FileNotFoundException {
    FileReader reader = new FileReader(path);
}
```

**Use for:** Things outside your control — file missing, network down, DB unreachable.

---

### Unchecked Exception — Compiler does NOT force you

```java
// Compiler says nothing — but it can still blow up at runtime

public double divide(int a, int b) {
    return a / b; // ArithmeticException if b=0 — no compile error!
}

// ✅ You should still validate
public double divide(int a, int b) {
    if (b == 0)
        throw new IllegalArgumentException("Divisor cannot be zero");
    return a / b;
}
```

**Use for:** Programming mistakes — null pointer, bad argument, illegal state.

---

### Decision Table

| Situation | Use |
|---|---|
| File not found | Checked (`IOException`) |
| Network timeout | Checked (`IOException`) |
| DB connection failed | Checked (`SQLException`) |
| Null argument passed | Unchecked (`NullPointerException` / `IllegalArgumentException`) |
| Negative amount passed | Unchecked (`IllegalArgumentException`) |
| Invalid state (withdraw from closed account) | Unchecked (`IllegalStateException`) |
| Index out of range | Unchecked (`IndexOutOfBoundsException`) |

---

## 4. try-catch-finally

### Basic Structure

```java
try {
    // Code that might throw an exception
    riskyOperation();

} catch (SpecificException e) {
    // Handle specific exception
    System.out.println("Specific error: " + e.getMessage());

} catch (AnotherException e) {
    // Handle another specific exception
    System.out.println("Another error: " + e.getMessage());

} catch (Exception e) {
    // Handle any remaining exception — ALWAYS put this last
    System.out.println("Unexpected error: " + e.getMessage());

} finally {
    // ALWAYS runs — whether exception happened or not
    // Use for cleanup: close connections, release locks
    cleanup();
}
```

### finally Always Runs

```java
public String readFile(String path) {
    FileReader reader = null;

    try {
        reader = new FileReader(path);
        return readContent(reader); // Even if this returns...

    } catch (IOException e) {
        return "Error reading file"; // ...or this returns...

    } finally {
        // ...this ALWAYS runs
        if (reader != null) {
            try { reader.close(); } catch (IOException e) { }
        }
        System.out.println("Cleanup done"); // Always prints
    }
}
```

### Catch Order — Most Specific FIRST

```java
// ✅ CORRECT — specific first, generic last
try {
    processPayment();
} catch (InsufficientFundsException e) {    // Most specific
    handleInsufficientFunds(e);
} catch (PaymentException e) {              // Parent of above
    handlePaymentError(e);
} catch (AppException e) {                  // Grandparent
    handleAppError(e);
} catch (Exception e) {                     // Most generic — last
    handleUnexpected(e);
}

// ❌ WRONG — generic first swallows specific
try {
    processPayment();
} catch (Exception e) {                     // Catches EVERYTHING
    handleError(e);                         // InsufficientFundsException never reaches its handler!
} catch (InsufficientFundsException e) {   // COMPILE ERROR — unreachable
    handleInsufficientFunds(e);
}
```

---

## 5. throw vs throws

### `throw` — Actually throws an exception right now

```java
// throw = "Something went wrong RIGHT NOW, I'm throwing this"

public void setAge(int age) {
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("Invalid age: " + age);
        // Execution stops here immediately
    }
    this.age = age;
}
```

### `throws` — Declares that a method MIGHT throw an exception

```java
// throws = "I'm warning you — this method might throw this"
// Caller must handle or re-declare

public void readConfig(String path) throws IOException, ParseException {
    // This method doesn't handle these — it passes responsibility to caller
    FileReader reader = new FileReader(path);       // might throw IOException
    parseContent(reader.read());                    // might throw ParseException
}

// Caller must handle
public void loadApp() {
    try {
        readConfig("config.json");
    } catch (IOException e) {
        System.out.println("Config file missing");
    } catch (ParseException e) {
        System.out.println("Config file is corrupted");
    }
}
```

### throw vs throws — Side by Side

```java
//      throw                              throws
//        ↓                                  ↓
void validate(int x) throws IllegalArgumentException {
    if (x < 0)
        throw new IllegalArgumentException("x must be positive");
    //  ↑
    // Creates and throws the exception object right now
}
```

---

## 6. Custom Exception Hierarchy

This is what separates a 3-rated candidate from a 5-rated one.

### Level 1: Base Application Exception

```java
// Root of your entire application's exception tree
public class AppException extends RuntimeException {

    private final String errorCode;
    private final String userFriendlyMessage;

    public AppException(String technicalMessage, String errorCode, String userFriendlyMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    // For wrapping another exception (chaining)
    public AppException(String technicalMessage, String errorCode,
                        String userFriendlyMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public String getErrorCode() { return errorCode; }
    public String getUserFriendlyMessage() { return userFriendlyMessage; }
}
```

### Level 2: Domain Exceptions

```java
// Payment domain
public class PaymentException extends AppException {
    public PaymentException(String message) {
        super(message, "PAYMENT_ERROR", "Payment could not be processed");
    }
    public PaymentException(String message, Throwable cause) {
        super(message, "PAYMENT_ERROR", "Payment could not be processed", cause);
    }
}

// Order domain
public class OrderException extends AppException {
    public OrderException(String message) {
        super(message, "ORDER_ERROR", "Order processing failed");
    }
}

// User domain
public class UserException extends AppException {
    public UserException(String message) {
        super(message, "USER_ERROR", "User operation failed");
    }
}

// Inventory domain
public class InventoryException extends AppException {
    public InventoryException(String message) {
        super(message, "INVENTORY_ERROR", "Inventory operation failed");
    }
}
```

### Level 3: Specific Exceptions

```java
// ─── Payment specific ─────────────────────────────────────────

public class InsufficientFundsException extends PaymentException {
    private final double required;
    private final double available;

    public InsufficientFundsException(double required, double available) {
        super(String.format(
            "Insufficient funds: required=%.2f, available=%.2f", required, available
        ));
        this.required = required;
        this.available = available;
    }

    public double getRequired() { return required; }
    public double getAvailable() { return available; }
    public double getShortfall() { return required - available; }
}

public class PaymentGatewayException extends PaymentException {
    private final String gatewayName;
    private final int httpStatusCode;

    public PaymentGatewayException(String gatewayName, int httpStatusCode, String reason) {
        super(String.format("Gateway '%s' failed with status %d: %s",
              gatewayName, httpStatusCode, reason));
        this.gatewayName = gatewayName;
        this.httpStatusCode = httpStatusCode;
    }

    public String getGatewayName() { return gatewayName; }
    public int getHttpStatusCode() { return httpStatusCode; }
}

public class DuplicateTransactionException extends PaymentException {
    private final String existingTransactionId;

    public DuplicateTransactionException(String existingTransactionId) {
        super("Transaction already exists: " + existingTransactionId);
        this.existingTransactionId = existingTransactionId;
    }

    public String getExistingTransactionId() { return existingTransactionId; }
}


// ─── Order specific ───────────────────────────────────────────

public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}

public class OrderAlreadyCancelledException extends OrderException {
    public OrderAlreadyCancelledException(String orderId) {
        super("Order already cancelled: " + orderId);
    }
}

public class InvalidOrderStateException extends OrderException {
    private final String currentState;
    private final String attemptedAction;

    public InvalidOrderStateException(String currentState, String attemptedAction) {
        super(String.format("Cannot '%s' when order is in '%s' state",
              attemptedAction, currentState));
        this.currentState = currentState;
        this.attemptedAction = attemptedAction;
    }
}


// ─── User specific ────────────────────────────────────────────

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}

public class AccountLockedException extends UserException {
    private final int remainingLockMinutes;

    public AccountLockedException(int remainingLockMinutes) {
        super("Account is locked for " + remainingLockMinutes + " more minutes");
        this.remainingLockMinutes = remainingLockMinutes;
    }

    public int getRemainingLockMinutes() { return remainingLockMinutes; }
}


// ─── Inventory specific ───────────────────────────────────────

public class OutOfStockException extends InventoryException {
    private final String itemId;
    private final int requested;
    private final int available;

    public OutOfStockException(String itemId, int requested, int available) {
        super(String.format("Item '%s' out of stock: requested=%d, available=%d",
              itemId, requested, available));
        this.itemId = itemId;
        this.requested = requested;
        this.available = available;
    }
}
```

### The Full Hierarchy Visual

```
AppException
├── PaymentException
│   ├── InsufficientFundsException
│   ├── PaymentGatewayException
│   └── DuplicateTransactionException
│
├── OrderException
│   ├── OrderNotFoundException
│   ├── OrderAlreadyCancelledException
│   └── InvalidOrderStateException
│
├── UserException
│   ├── UserNotFoundException
│   ├── UserAlreadyExistsException
│   └── AccountLockedException
│
└── InventoryException
    └── OutOfStockException
```

---

## 7. Multi-catch and Exception Chaining

### Multi-catch (Java 7+) — Handle multiple exceptions the same way

```java
// ❌ OLD WAY — repetitive
try {
    process();
} catch (IOException e) {
    log.error("Failed: " + e.getMessage());
    throw new AppException("Processing failed");
} catch (SQLException e) {
    log.error("Failed: " + e.getMessage());   // same code repeated!
    throw new AppException("Processing failed");
}

// ✅ NEW WAY — multi-catch
try {
    process();
} catch (IOException | SQLException e) {
    log.error("Failed: " + e.getMessage());
    throw new AppException("Processing failed");
}
```

### Exception Chaining — Never lose the original cause

```java
// ❌ BAD — original cause is LOST forever
try {
    dbConnection.query(sql);
} catch (SQLException e) {
    throw new OrderException("Failed to fetch order"); // WHERE did it fail? No idea!
}

// ✅ GOOD — original cause is PRESERVED
try {
    dbConnection.query(sql);
} catch (SQLException e) {
    throw new OrderException("Failed to fetch order: " + e.getMessage(), e);
    //                                                                    ↑
    //                                            Pass original as 'cause'
    // Now the stack trace shows BOTH exceptions
}

// Reading a chained exception
try {
    orderService.getOrder("123");
} catch (OrderException e) {
    System.out.println("Order error: " + e.getMessage());
    System.out.println("Root cause: " + e.getCause().getMessage()); // Original SQLException
    e.printStackTrace(); // Shows full chain
}
```

---

## 8. try-with-resources

### The Problem — Resources must always be closed

```java
// ❌ OLD WAY — messy, easy to forget
FileReader reader = null;
try {
    reader = new FileReader("file.txt");
    // use reader
} catch (IOException e) {
    handleError(e);
} finally {
    if (reader != null) {
        try {
            reader.close(); // close can ALSO throw!
        } catch (IOException e) {
            // Now what?!
        }
    }
}
```

### The Solution — try-with-resources (Java 7+)

```java
// ✅ NEW WAY — auto-closes, clean and safe
try (FileReader reader = new FileReader("file.txt")) {
    // use reader
    String content = readContent(reader);
} catch (IOException e) {
    handleError(e);
}
// reader.close() is called AUTOMATICALLY — even if exception occurs

// Multiple resources — closed in REVERSE order
try (
    Connection conn = dataSource.getConnection();          // opened first
    PreparedStatement stmt = conn.prepareStatement(sql);   // opened second
    ResultSet rs = stmt.executeQuery()                     // opened third
) {
    while (rs.next()) {
        processRow(rs);
    }
} catch (SQLException e) {
    throw new DatabaseException("Query failed", e);
}
// rs closed first, then stmt, then conn — reverse order
```

### Making Your Own Class Work with try-with-resources

```java
// Implement AutoCloseable
public class DatabaseConnection implements AutoCloseable {
    private Connection connection;

    public DatabaseConnection(String url) throws SQLException {
        this.connection = DriverManager.getConnection(url);
    }

    public ResultSet query(String sql) throws SQLException {
        return connection.createStatement().executeQuery(sql);
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Connection closed");
        }
    }
}

// Now it works with try-with-resources!
try (DatabaseConnection db = new DatabaseConnection(DB_URL)) {
    ResultSet rs = db.query("SELECT * FROM orders");
    // process results
} // db.close() called automatically
```

---

## 9. Best Practices

### 1. Be Specific — Catch the Most Specific Exception

```java
// ❌ BAD — too generic, hides real problems
try {
    processOrder(orderId);
} catch (Exception e) {
    System.out.println("Something went wrong");
}

// ✅ GOOD — specific, meaningful handling
try {
    processOrder(orderId);
} catch (OrderNotFoundException e) {
    return Response.notFound("Order " + orderId + " does not exist");
} catch (InsufficientFundsException e) {
    return Response.badRequest("Insufficient funds. Need ₹" + e.getShortfall() + " more");
} catch (PaymentException e) {
    return Response.serverError("Payment failed: " + e.getUserFriendlyMessage());
}
```

---

### 2. Fail Fast — Validate at Entry Point

```java
// ✅ Validate ALL inputs at the top — before doing any work
public Order createOrder(String userId, List<String> itemIds, String paymentMethod) {

    // Validate everything first
    if (userId == null || userId.isBlank())
        throw new IllegalArgumentException("UserId cannot be null or empty");
    if (itemIds == null || itemIds.isEmpty())
        throw new IllegalArgumentException("Order must have at least one item");
    if (paymentMethod == null)
        throw new IllegalArgumentException("Payment method cannot be null");
    if (!SUPPORTED_PAYMENT_METHODS.contains(paymentMethod))
        throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);

    // Now do the actual work — inputs are guaranteed valid
    User user = userRepo.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // ...rest of logic
}
```

---

### 3. Never Swallow Exceptions Silently

```java
// ❌ WORST THING YOU CAN DO — exception disappears silently
try {
    sendNotification(userId, message);
} catch (Exception e) {
    // Empty catch — exception is gone forever
    // You will never know this failed
}

// ✅ At minimum — log it
try {
    sendNotification(userId, message);
} catch (Exception e) {
    log.error("Failed to send notification | userId={} error={}", userId, e.getMessage(), e);
    // Decide: rethrow? return false? depends on whether notification is critical
}
```

---

### 4. Don't Use Exceptions for Flow Control

```java
// ❌ BAD — using exception as if/else — very slow, bad design
public boolean isUserExists(String userId) {
    try {
        userRepo.findById(userId);
        return true;
    } catch (UserNotFoundException e) {
        return false; // Exception as flow control — wrong!
    }
}

// ✅ GOOD — use Optional or boolean check
public boolean isUserExists(String userId) {
    return userRepo.existsById(userId); // Simple boolean check
}

public Optional<User> findUser(String userId) {
    return userRepo.findById(userId); // Return Optional — caller decides
}
```

---

### 5. Always Include Context in Exception Messages

```java
// ❌ BAD — useless message, no context
throw new OrderException("Order failed");

// ✅ GOOD — who, what, why
throw new OrderNotFoundException(
    String.format("Order not found | orderId=%s userId=%s", orderId, userId)
);

// ✅ GOOD — include the values that caused the problem
throw new InsufficientFundsException(requiredAmount, availableBalance);

// ✅ GOOD — include what state it was in
throw new InvalidOrderStateException(order.getStatus().name(), "cancel");
// Message: "Cannot 'cancel' when order is in 'DELIVERED' state"
```

---

### 6. Re-throw vs Wrap vs Handle — Know When to Do What

```java
public void processOrder(String orderId) {

    try {
        Order order = orderRepo.findById(orderId);
        paymentService.charge(order);
        notificationService.notify(order.getUserId());

    } catch (OrderNotFoundException e) {
        // HANDLE — you know exactly what to do
        log.warn("Order not found | orderId={}", orderId);
        return; // or return a default, or notify user

    } catch (InsufficientFundsException e) {
        // RE-THROW — let the caller handle this business decision
        throw e;

    } catch (SQLException e) {
        // WRAP — translate low-level to domain exception + preserve cause
        throw new OrderException("Database error while processing order: " + orderId, e);

    } catch (NotificationException e) {
        // SUPPRESS (with logging) — notification failure shouldn't fail the order
        log.warn("Notification failed but order was processed | orderId={}", orderId);
        // Don't re-throw — order was successful
    }
}
```

---

## 10. Anti-Patterns — What NOT to Do

### ❌ Anti-Pattern 1: Pokemon Exception Handling ("Gotta catch 'em all")

```java
// ❌ NEVER DO THIS
try {
    everythingInOneBlock();
} catch (Exception e) {
    e.printStackTrace(); // Just printing — not handling!
}
```

---

### ❌ Anti-Pattern 2: Empty Catch Block

```java
// ❌ THE CARDINAL SIN
try {
    criticalOperation();
} catch (Exception e) {
    // Nothing here — exception silently eaten
}
```

---

### ❌ Anti-Pattern 3: Logging AND Re-throwing (Double Logging)

```java
// ❌ BAD — logs the same error twice in the stack
try {
    processPayment();
} catch (PaymentException e) {
    log.error("Payment failed", e);  // Logged here
    throw e;                          // AND caught+logged by caller too
}

// ✅ GOOD — either log OR re-throw, not both
// Option A: Handle it here (log + don't re-throw)
try {
    processPayment();
} catch (PaymentException e) {
    log.error("Payment failed | orderId={}", orderId, e);
    return PaymentResult.failed(e.getMessage());
}

// Option B: Re-throw for caller to handle (don't log here)
try {
    processPayment();
} catch (PaymentException e) {
    throw new OrderException("Order failed due to payment issue", e); // Caller logs
}
```

---

### ❌ Anti-Pattern 4: Catching and Ignoring InterruptedException

```java
// ❌ BAD — clears the interrupt flag, breaks thread communication
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // Ignore — WRONG
}

// ✅ GOOD — restore the interrupt flag
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Restore flag
    throw new RuntimeException("Thread was interrupted", e);
}
```

---

### ❌ Anti-Pattern 5: Using Exception Message for Logic

```java
// ❌ BAD — fragile, breaks if message changes
try {
    processPayment();
} catch (Exception e) {
    if (e.getMessage().contains("insufficient")) { // String matching — terrible!
        handleInsufficientFunds();
    }
}

// ✅ GOOD — catch specific exception type
try {
    processPayment();
} catch (InsufficientFundsException e) {
    handleInsufficientFunds(e.getShortfall()); // Type-safe, clean
}
```

---

## 11. Complete LLD Example — Banking System

### Full Exception Hierarchy + Service + Logging

```java
// ─── CUSTOM EXCEPTIONS ────────────────────────────────────────

class BankException extends RuntimeException {
    private final String errorCode;

    public BankException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}

class AccountException extends BankException {
    public AccountException(String message) {
        super(message, "ACCOUNT_ERROR");
    }
}

class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
    }
}

class AccountFrozenException extends AccountException {
    private final String reason;

    public AccountFrozenException(String accountId, String reason) {
        super("Account " + accountId + " is frozen: " + reason);
        this.reason = reason;
    }

    public String getReason() { return reason; }
}

class InsufficientFundsException extends BankException {
    private final double required;
    private final double available;

    public InsufficientFundsException(double required, double available) {
        super(String.format("Insufficient funds: required=%.2f, available=%.2f",
              required, available), "INSUFFICIENT_FUNDS");
        this.required = required;
        this.available = available;
    }

    public double getShortfall() { return required - available; }
}

class TransferLimitExceededException extends BankException {
    public TransferLimitExceededException(double attempted, double limit) {
        super(String.format("Transfer limit exceeded: attempted=%.2f, limit=%.2f",
              attempted, limit), "TRANSFER_LIMIT_EXCEEDED");
    }
}


// ─── ACCOUNT — with State Machine ─────────────────────────────

enum AccountStatus { ACTIVE, FROZEN, CLOSED }

class BankAccount {
    private static final Logger log = LoggerFactory.getLogger(BankAccount.class);

    private final String accountId;
    private final String ownerId;
    private double balance;
    private AccountStatus status;
    private final double dailyTransferLimit;
    private double transferredToday;
    private final List<String> transactionHistory;

    public BankAccount(String accountId, String ownerId,
                       double initialBalance, double dailyTransferLimit) {
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("AccountId cannot be blank");
        if (initialBalance < 0)
            throw new IllegalArgumentException("Initial balance cannot be negative");
        if (dailyTransferLimit <= 0)
            throw new IllegalArgumentException("Daily transfer limit must be positive");

        this.accountId = accountId;
        this.ownerId = ownerId;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
        this.dailyTransferLimit = dailyTransferLimit;
        this.transferredToday = 0;
        this.transactionHistory = new ArrayList<>();

        log.info("Account created | accountId={} ownerId={} balance={}",
                 accountId, ownerId, initialBalance);
    }

    public void deposit(double amount) {
        // Validate state
        validateAccountActive("deposit");

        // Validate input
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive, got: " + amount);
        if (amount > 10_000_000)
            throw new IllegalArgumentException("Single deposit cannot exceed ₹1,00,00,000");

        // Perform operation
        double previousBalance = balance;
        balance += amount;
        transactionHistory.add(String.format("DEPOSIT +%.2f | balance=%.2f", amount, balance));

        log.info("Deposit successful | accountId={} amount={} prevBalance={} newBalance={}",
                 accountId, amount, previousBalance, balance);
    }

    public void withdraw(double amount) {
        // Validate state
        validateAccountActive("withdraw");

        // Validate input
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive, got: " + amount);

        // Validate balance
        if (amount > balance)
            throw new InsufficientFundsException(amount, balance);

        // Perform operation
        double previousBalance = balance;
        balance -= amount;
        transactionHistory.add(String.format("WITHDRAW -%.2f | balance=%.2f", amount, balance));

        log.info("Withdrawal successful | accountId={} amount={} prevBalance={} newBalance={}",
                 accountId, amount, previousBalance, balance);
    }

    public void transfer(BankAccount target, double amount) {
        // Validate state
        validateAccountActive("transfer");

        // Validate input
        if (target == null)
            throw new IllegalArgumentException("Target account cannot be null");
        if (target.accountId.equals(this.accountId))
            throw new IllegalArgumentException("Cannot transfer to the same account");
        if (amount <= 0)
            throw new IllegalArgumentException("Transfer amount must be positive");

        // Check daily limit
        if (transferredToday + amount > dailyTransferLimit)
            throw new TransferLimitExceededException(amount, dailyTransferLimit - transferredToday);

        // Check balance
        if (amount > balance)
            throw new InsufficientFundsException(amount, balance);

        // Check target is active
        if (target.status != AccountStatus.ACTIVE)
            throw new AccountFrozenException(target.accountId, "Target account is not active");

        // Perform — both sides must succeed or both fail (atomicity)
        try {
            this.balance -= amount;
            this.transferredToday += amount;
            target.balance += amount;

            String txnRecord = String.format(
                "TRANSFER -%.2f to %s | balance=%.2f", amount, target.accountId, balance
            );
            this.transactionHistory.add(txnRecord);
            target.transactionHistory.add(String.format(
                "TRANSFER +%.2f from %s | balance=%.2f", amount, this.accountId, target.balance
            ));

            log.info("Transfer successful | from={} to={} amount={}",
                     accountId, target.accountId, amount);

        } catch (Exception e) {
            // Rollback on failure
            log.error("Transfer failed, rolling back | from={} to={} amount={} error={}",
                      accountId, target.accountId, amount, e.getMessage(), e);
            // Restore state
            this.balance += amount;
            this.transferredToday -= amount;
            target.balance -= amount;
            throw new BankException("Transfer failed and was rolled back", "TRANSFER_FAILED", e);
        }
    }

    public void freeze(String reason) {
        if (status == AccountStatus.CLOSED)
            throw new AccountException("Cannot freeze a closed account: " + accountId);
        if (status == AccountStatus.FROZEN)
            throw new AccountException("Account is already frozen: " + accountId);

        this.status = AccountStatus.FROZEN;
        log.warn("Account frozen | accountId={} reason={}", accountId, reason);
    }

    public void close() {
        if (status == AccountStatus.CLOSED)
            throw new AccountException("Account is already closed: " + accountId);
        if (balance > 0)
            throw new AccountException(
                String.format("Cannot close account with remaining balance=%.2f. Withdraw first.", balance)
            );

        this.status = AccountStatus.CLOSED;
        log.info("Account closed | accountId={}", accountId);
    }

    // Private helper — DRY principle
    private void validateAccountActive(String operation) {
        if (status == AccountStatus.FROZEN)
            throw new AccountFrozenException(accountId, "Account is frozen");
        if (status == AccountStatus.CLOSED)
            throw new AccountException("Cannot " + operation + " on closed account: " + accountId);
    }

    // Getters
    public String getAccountId() { return accountId; }
    public double getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public List<String> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }
}


// ─── BANK SERVICE — Orchestration with Exception Handling ──────

class BankService {
    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final Map<String, BankAccount> accounts = new ConcurrentHashMap<>();

    public BankAccount createAccount(String ownerId, double initialDeposit) {
        if (ownerId == null || ownerId.isBlank())
            throw new IllegalArgumentException("OwnerId cannot be blank");
        if (initialDeposit < 0)
            throw new IllegalArgumentException("Initial deposit cannot be negative");

        String accountId = "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BankAccount account = new BankAccount(accountId, ownerId, initialDeposit, 100_000.0);
        accounts.put(accountId, account);

        log.info("New account opened | accountId={} ownerId={}", accountId, ownerId);
        return account;
    }

    public void transfer(String fromAccountId, String toAccountId, double amount) {
        log.info("Transfer initiated | from={} to={} amount={}", fromAccountId, toAccountId, amount);

        // Fetch accounts — throws if not found
        BankAccount from = getAccount(fromAccountId);
        BankAccount to = getAccount(toAccountId);

        try {
            from.transfer(to, amount);
            log.info("Transfer completed | from={} to={} amount={}", fromAccountId, toAccountId, amount);

        } catch (InsufficientFundsException e) {
            log.warn("Transfer failed: insufficient funds | from={} shortfall={}",
                     fromAccountId, e.getShortfall());
            throw e; // Re-throw — caller decides what to show user

        } catch (TransferLimitExceededException e) {
            log.warn("Transfer failed: limit exceeded | from={} amount={}", fromAccountId, amount);
            throw e;

        } catch (AccountFrozenException e) {
            log.warn("Transfer failed: account frozen | accountId={}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Transfer failed unexpectedly | from={} to={} amount={} error={}",
                      fromAccountId, toAccountId, amount, e.getMessage(), e);
            throw new BankException("Transfer failed due to unexpected error", "TRANSFER_FAILED", e);
        }
    }

    private BankAccount getAccount(String accountId) {
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("AccountId cannot be blank");

        BankAccount account = accounts.get(accountId);
        if (account == null)
            throw new AccountNotFoundException(accountId);

        return account;
    }
}
```

---

## 12. Interview Cheat Sheet

### Exception Handling — Decision Tree

```
Something went wrong. What do I throw?
        │
        ▼
Is it a programming mistake? (null arg, bad value, wrong state)
        │
   YES ─┼─→ Unchecked: IllegalArgumentException / IllegalStateException
        │
   NO  ─┼
        ▼
Is it a domain/business rule violation? (insufficient funds, not found)
        │
   YES ─┼─→ Custom Exception: InsufficientFundsException / OrderNotFoundException
        │
   NO  ─┼
        ▼
Is it an external/IO failure? (file, network, DB)
        │
   YES ─┼─→ Checked or wrap in custom: DatabaseException / NetworkException
```

### 6 Things to Say in an LLD Interview

1. *"I'll build a custom exception hierarchy so callers can catch at the right level of granularity."*
2. *"I always validate inputs at the entry point and fail fast before doing any work."*
3. *"I preserve the original cause when wrapping exceptions so we don't lose stack trace context."*
4. *"I never swallow exceptions silently — at minimum I log them."*
5. *"For resources like DB connections, I use try-with-resources to guarantee cleanup."*
6. *"I either log an exception OR re-throw it — never both, to avoid duplicate log entries."*

### Quick Reference

| Scenario | Exception to Throw |
|---|---|
| Null argument | `IllegalArgumentException` |
| Empty string/list | `IllegalArgumentException` |
| Negative amount | `IllegalArgumentException` |
| Account is closed/frozen | `IllegalStateException` or custom |
| Resource not found | Custom: `OrderNotFoundException` |
| Business rule violated | Custom: `InsufficientFundsException` |
| External service down | Wrap in custom: `PaymentGatewayException` |
| DB query failed | Wrap in custom: `DatabaseException` |

### Golden Rules (Memorize These)

```
1. Catch specific first, generic last
2. Never swallow — always log or rethrow
3. Fail fast — validate at entry point
4. Preserve cause — wrap with original exception
5. Use try-with-resources for any resource
6. Log OR rethrow — never both
7. Include context in every exception message
8. Build a hierarchy — domain → specific
```

---

> 💡 **Remember:** Exception handling is not just about `try-catch`.
> It's about designing a system where failures are **expected, handled gracefully,
> and always traceable.** A 5-rated candidate builds exception hierarchies,
> validates inputs upfront, and never loses the root cause.

---

*Java Exception Handling Complete Guide — Last Updated March 2026*
