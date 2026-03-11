## `volatile` Keyword in Java

---

### The Problem First

Your computer has **multiple CPU cores**. Each core has its own **cache** (fast local memory).

```
CPU Core 1          CPU Core 2
┌──────────┐        ┌──────────┐
│  Cache   │        │  Cache   │
│ flag=true│        │ flag=false│  ← Stale copy!
└──────────┘        └──────────┘
      │                   │
      └────────┬──────────┘
               │
         Main Memory
         ┌──────────┐
         │ flag=true│
         └──────────┘
```

Thread 1 updates `flag = true` → stored in Core 1's cache.
Thread 2 reads `flag` → reads from Core 2's cache → still sees `false`.

**This is the visibility problem.** `volatile` solves it.

---

### What `volatile` Does

```java
// Without volatile — Thread 2 may NEVER see the update
boolean flag = false;

// With volatile — Thread 2 is GUARANTEED to see the update immediately
volatile boolean flag = false;
```

`volatile` tells the JVM:
- **Every write** goes directly to **main memory**
- **Every read** comes directly from **main memory**
- Never use the cached copy

---

### Simple Example

```java
class Worker {

    // Without volatile — this loop might run FOREVER
    // Thread 2's cache never gets updated value
    private volatile boolean isRunning = true;

    public void run() {
        while (isRunning) {         // Thread 2 reads this
            System.out.println("Working...");
        }
        System.out.println("Stopped.");
    }

    public void stop() {
        isRunning = false;          // Thread 1 writes this
    }
}

// Usage
Worker worker = new Worker();

Thread t1 = new Thread(() -> worker.run());   // Reads isRunning
Thread t2 = new Thread(() -> worker.stop());  // Writes isRunning

t1.start();
Thread.sleep(100);
t2.start(); // Without volatile, t1 might never stop!
```

Without `volatile` → t1 may loop forever because it reads stale cache.
With `volatile` → t1 sees the update immediately and stops.

---

### What `volatile` Does NOT Solve

This is the most important thing to understand.

```java
volatile int counter = 0;

// Thread 1 and Thread 2 both do this:
counter++; // This looks like 1 operation but is actually 3:
           // 1. READ counter (= 0)
           // 2. ADD 1       (= 1)
           // 3. WRITE back  (= 1)

// Thread 1: READ=0, ADD=1
// Thread 2: READ=0, ADD=1   ← reads before Thread 1 writes back!
// Thread 1: WRITE=1
// Thread 2: WRITE=1
// Final: 1 ❌  (Expected: 2)
```

`volatile` guarantees **visibility** — not **atomicity**.

For counter-type operations, use `AtomicInteger`:

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet(); // Thread-safe, atomic
```

---

### volatile vs synchronized

| | `volatile` | `synchronized` |
|---|---|---|
| Solves visibility | ✅ | ✅ |
| Solves atomicity | ❌ | ✅ |
| Performance | Fast (no locking) | Slower (acquires lock) |
| Use for | Simple flag/state | Compound operations |

```java
// Use volatile for — simple read/write flags
private volatile boolean isShutdown = false;

// Use synchronized for — compound operations (check-then-act)
public synchronized void withdraw(double amount) {
    if (balance >= amount) {   // check
        balance -= amount;     // act
    }                          // these two must be atomic together
}
```

---

### Real LLD Use Case — Singleton Pattern

```java
public class DatabaseConnection {

    // volatile — ensures the reference is visible to all threads
    // without it, a thread might see a partially constructed object!
    private static volatile DatabaseConnection instance;

    private DatabaseConnection() { }

    public static DatabaseConnection getInstance() {

        if (instance == null) {                    // First check (no lock)
            synchronized (DatabaseConnection.class) {
                if (instance == null) {            // Second check (with lock)
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
}
```

This is called **Double-Checked Locking**. Without `volatile`, another thread could see a partially initialized object — the reference is set before the constructor finishes.

---

### One Line Summary

> `volatile` = *"Every thread must read/write this variable from main memory — never use a cached copy."*

Use it when:
- One thread **writes**, one or more threads **read**
- The operation is a **simple assignment** (not `++` or compound)
- You need a **shutdown flag**, **state flag**, or **lazy singleton**
