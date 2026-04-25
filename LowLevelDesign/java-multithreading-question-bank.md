<div align="center">

# ☕ Java Concurrency & Multithreading — Interview Question Bank
### 300+ Questions | Conceptual + Code + Diagram | All Levels

> Built from the Complete Java Concurrency Reference.
> Every question has a full answer, code snippet, ASCII diagram (where relevant),
> and curated resource links. Organized by topic — perfect for targeted revision.

[![Topics](https://img.shields.io/badge/Questions-300%2B-blue?style=flat-square)](#table-of-contents)
[![Level](https://img.shields.io/badge/Level-Beginner%20→%20Senior-green?style=flat-square)](#)
[![Interview](https://img.shields.io/badge/Interview-Ready-purple?style=flat-square)](#)

</div>

---

## Table of Contents

1. [JVM Memory Architecture](#section-1-jvm-memory-architecture)
2. [How Java Code Executes — Bytecode, JIT, CPU](#section-2-how-java-code-executes)
3. [Thread Life Cycle — All 6 States](#section-3-thread-life-cycle)
4. [Creating Threads — Internals](#section-4-creating-threads)
5. [Runnable vs Extending Thread](#section-5-runnable-vs-extending-thread)
6. [The synchronized Keyword](#section-6-the-synchronized-keyword)
7. [The volatile Keyword](#section-7-the-volatile-keyword)
8. [wait() and notify()](#section-8-wait-and-notify)
9. [wait() vs sleep()](#section-9-wait-vs-sleep)
10. [Thread.join()](#section-10-threadjoin)
11. [Runnable vs Callable](#section-11-runnable-vs-callable)
12. [How to Stop a Thread](#section-12-how-to-stop-a-thread)
13. [Thread Pools — Internals and Configuration](#section-13-thread-pools)
14. [ThreadPoolTaskExecutor — corePoolSize vs maxPoolSize](#section-14-threadpooltaskexecutor)
15. [java.util.concurrent — Full Toolkit](#section-15-javautilconcurrent)
16. [Mutex — synchronized vs ReentrantLock vs Semaphore](#section-16-mutex)
17. [Future and CompletableFuture](#section-17-future-and-completablefuture)
18. [ThreadLocal](#section-18-threadlocal)
19. [Java Memory Model (JMM)](#section-19-java-memory-model)
20. [Common Concurrency Bugs](#section-20-common-concurrency-bugs)
21. [Mixed / System Design Questions](#section-21-mixed-and-system-design)

---

## LEGEND

```
[EASY]    — Junior level. Expected from any Java developer.
[MID]     — Mid-level. Expected from 2–5 year engineers.
[HARD]    — Senior/Staff level. Internals, edge cases, tradeoffs.
[CODE]    — Requires writing or reading code.
[DIAGRAM] — Explained with ASCII diagram.
[TRAP]    — Common wrong answer. Many people get this wrong.
```

---

---

# SECTION 1: JVM MEMORY ARCHITECTURE

---

### Q1. [EASY] What is the difference between a Process and a JVM Instance?

**Answer:**

A **process** is an OS-level abstraction. It has its own isolated virtual address space, file descriptors, and system resources. When you run a Java program, the OS creates exactly **one process**, which corresponds to **one JVM instance**.

```
OS
├── Process 1  →  JVM Instance 1  (heap: 2GB, address space: isolated)
├── Process 2  →  JVM Instance 2  (heap: 2GB, address space: isolated)
└── Process 3  →  JVM Instance 3  (heap: 512MB, address space: isolated)
```

Key isolation guarantee: even if two JVM instances happen to use the same virtual memory address (e.g., `0x8000`), they map to **different physical RAM locations**. They share nothing.

**Threads within the same process do share memory** — heap, static fields, code. That's what makes threading both powerful and dangerous.

> Resource: [JVM Internals — Oracle Docs](https://docs.oracle.com/en/java/articles/jvm-internals/)

---

### Q2. [MID][DIAGRAM] Draw and explain all memory segments inside a JVM process. Which are shared and which are private?

**Answer:**

```
JVM Process
│
├── [SHARED — all threads access these]
│   │
│   ├── Code Segment
│   │     Contents: Compiled bytecode / JIT-generated native instructions
│   │     Access:   Read-only at runtime
│   │     Sync:     No — immutable, safe to read concurrently
│   │
│   ├── Data Segment
│   │     Contents: Static variables (class-level fields)
│   │     Access:   Read + Write
│   │     Sync:     YES — race conditions possible
│   │     Example:  static int counter = 0;
│   │
│   └── Heap
│         Contents: All objects created with `new`
│         Access:   Read + Write
│         Sync:     YES — most concurrency bugs happen here
│         Note:     NOT shared across processes — each JVM has its own heap
│
└── [PRIVATE — each thread owns its own copy]
    │
    ├── Stack (per thread)
    │     Contents: Method call frames, local variables, return addresses
    │     Sync:     No — completely isolated
    │
    ├── Register (per thread)
    │     Contents: JIT-optimized intermediate values, current operands
    │     Sync:     No — saved/restored on context switch
    │
    └── Program Counter (per thread)
          Contents: Address of the next instruction to execute
          Sync:     No — each thread tracks its own position
```

**Quick rule:** Anything on the heap or in static fields needs synchronization. Local variables (on the stack) are always thread-safe.

---

### Q3. [MID] Why does the heap need synchronization but the stack does not?

**Answer:**

The **heap is shared** — all threads within the same JVM process can see and modify the same heap objects. Two threads can simultaneously hold references to the same object and mutate its fields.

The **stack is private per thread** — each thread has its own stack. Local variables inside a method are stack-allocated and physically isolated from other threads. No thread can read or write another thread's stack.

```java
void method() {
    int x = 10;          // stack — private to THIS thread, always safe
    MyObject obj = new MyObject();  // obj reference is on stack,
                                    // but MyObject itself is on heap
    obj.field = 5;       // HEAP mutation — needs synchronization
}
```

**Exception:** If you pass a local object reference to another thread (e.g., by putting it in a shared collection), that object escapes to the heap and is no longer thread-safe.

---

### Q4. [EASY] What is stored in the Data Segment? Give a Java example.

**Answer:**

The Data Segment stores **static (class-level) variables** — fields declared with the `static` keyword. These are shared across ALL threads in the JVM process.

```java
class BankAccount {
    static int totalAccountsOpened = 0;   // Data Segment — SHARED
    static final String BANK_NAME = "HDFC"; // Data Segment — SHARED (but immutable)

    double balance;  // Heap — per-instance, SHARED if object is shared
}
```

If Thread A and Thread B both execute `totalAccountsOpened++` concurrently, this is a **data race** because the static field is in the Data Segment (shared), and `++` is not atomic.

---

### Q5. [MID] What is the difference between heap memory and stack memory in the context of garbage collection?

**Answer:**

**Stack memory** is managed automatically using a Last-In-First-Out frame model. When a method returns, its stack frame is popped and all local variables are instantly reclaimed — no GC involved. Stack memory lifecycle is deterministic and tied to method invocation.

**Heap memory** is managed by the Garbage Collector. Objects live on the heap as long as there are reachable references pointing to them. When no thread can reach an object, the GC eventually reclaims it. This process is non-deterministic (GC runs when the JVM decides).

```
Stack (per thread):                 Heap (shared):
Method frame: process()             Object: MyObject@0x1234
  local var: int x = 10               field: value = 42
  local var: ref → 0x1234           Object: String@0x5678
Method frame: main()                  field: chars = "hello"
  local var: MyObject obj → 0x1234
```

When `process()` returns, `x` is gone instantly (stack pop). The `MyObject` at `0x1234` lives on the heap until no references remain.

---

### Q6. [HARD][TRAP] Can two threads share a local variable? Explain with an example.

**Answer:**

**By default, no.** Local variables live on the thread's stack and are private. However, if a local variable is an **object reference** and you pass that reference to another thread, both threads can access the same object on the heap. The reference is stack-local, but the object it points to is shared.

```java
void startWork() {
    Counter counter = new Counter();   // reference on THIS thread's stack
                                       // Counter object on the HEAP
    Thread t = new Thread(() -> {
        counter.increment();           // t has a copy of the reference
                                       // BOTH threads access the SAME Counter object
    });
    t.start();
    counter.increment();               // concurrent access — RACE CONDITION
}
```

This is called **object escape**. The object "escapes" the creating thread's scope and becomes accessible to multiple threads. This is one of the most common sources of concurrency bugs — developers assume local = safe, but the referenced heap object is shared.

**Safe alternative:** If you don't escape the reference, you're fine:

```java
void safeWork() {
    Counter counter = new Counter();   // NEVER passed to another thread
    counter.increment();               // Only this thread touches it — SAFE
    System.out.println(counter.get());
}
```

---

### Q7. [MID] What are CPU registers and what role do they play in Java thread execution?

**Answer:**

CPU registers are the fastest storage in a computer — built directly into the processor, with sub-nanosecond access. Each CPU core has its own set of registers (typically 16–32 on x86-64).

In the context of Java threads:

**Role 1 — JIT optimization:** When the JIT compiler converts bytecode to native machine code, it assigns frequently accessed variables to CPU registers instead of reading them from RAM on every operation. A loop counter, for example, might live in a register for the entire duration of the loop.

**Role 2 — Context switching:** When the OS preempts a thread, it saves the entire register state into a Process Control Block (PCB) in RAM. When the thread resumes, the OS restores those registers. This is how a thread "picks up where it left off."

**Role 3 — Visibility problems:** If Thread A has a variable cached in a CPU register (or L1 cache), Thread B on another core cannot see it. This is the hardware root cause of the visibility problem that `volatile` and `synchronized` solve.

```
CPU Core 1                      CPU Core 2
┌──────────────┐                ┌──────────────┐
│ Registers:   │                │ Registers:   │
│  R1 = 5     │ Thread A's     │  R1 = 0     │ Thread B's
│  R2 = 0x100 │ private state  │  R2 = 0x200 │ private state
└──────────────┘                └──────────────┘
       │                               │
       └──────────── RAM ──────────────┘
                (shared, but slow)
```

> Resource: [Understanding CPU Registers — Computerphile (YouTube)](https://www.youtube.com/c/Computerphile)

---

### Q8. [MID][DIAGRAM] Explain the CPU cache hierarchy and why it causes concurrency problems in Java.

**Answer:**

Modern CPUs have multiple levels of cache between the registers and RAM:

```
┌──────────────────────────────┐  ┌──────────────────────────────┐
│         CPU Core 1           │  │         CPU Core 2           │
│                              │  │                              │
│  Registers   ~1 cycle        │  │  Registers   ~1 cycle        │
│      ↓                       │  │      ↓                       │
│  L1 Cache    ~4 cycles 32KB  │  │  L1 Cache    ~4 cycles 32KB  │
│      ↓                       │  │      ↓                       │
│  L2 Cache    ~12 cycles 256KB│  │  L2 Cache    ~12 cycles 256KB│
└──────────────┬───────────────┘  └──────────────┬───────────────┘
               │                                  │
               └──────────────┬───────────────────┘
                              ↓
                  L3 Cache  ~40 cycles  8–32MB  (shared between cores)
                              ↓
                  RAM       ~100 cycles  GBs
```

**The concurrency problem:**

```
Step 1: counter = 0 in RAM

Step 2: Thread A (Core 1) reads counter
        → fetches from RAM into Core 1's L1: counter = 0

Step 3: Thread A increments: counter = 1 in Core 1's L1
        → write may NOT immediately flush to RAM

Step 4: Thread B (Core 2) reads counter
        → fetches from RAM: counter = 0  ← STALE! Thread A's write invisible

Step 5: Thread B increments: counter = 1 in Core 2's L1
        Both threads computed 1, both write 1.
        Expected: 2. Actual: 1. LOST UPDATE.
```

This is the **CPU cache coherence problem** — the fundamental hardware reason why `volatile`, `synchronized`, and the Java Memory Model exist.

> Resource: [CPU Cache Effects — Martin Thompson (Mechanical Sympathy blog)](https://mechanical-sympathy.blogspot.com/)

---

### Q9. [EASY] What does it mean that the Code Segment is read-only? Why is no synchronization needed there?

**Answer:**

The Code Segment contains the compiled bytecode (and JIT-generated native instructions). Once a class is loaded and JIT-compiled, the instruction bytes themselves do not change at runtime. No thread can modify the instructions — they are **read-only**.

Since concurrent reads of the same data require no synchronization (reads don't interfere with reads), all threads can execute code from the Code Segment simultaneously without any locking overhead.

**What synchronization IS needed:** The heap objects that code operates on — not the code itself.

```java
public void transfer(Account from, Account to, double amount) {
    // This instruction is in Code Segment (read-only, safe)
    // But `from.balance` and `to.balance` are on the Heap → need sync
    from.balance -= amount;
    to.balance += amount;
}
```

---

### Q10. [HARD] What is the difference between concurrency and parallelism? How does it relate to single-core vs multi-core CPUs?

**Answer:**

**Concurrency** is about managing multiple tasks that are in progress at the same time — but not necessarily executing simultaneously. It's a structural property of the program.

**Parallelism** is about multiple tasks executing at the exact same instant — requires multiple hardware execution units (cores).

```
SINGLE CORE — Concurrency (interleaved, not simultaneous):

Timeline: │T-A│ T-B │ T-A │ T-A │ T-B │ T-A │
Core 1:    runs  runs  runs  runs  runs  runs
           Thread A and B take turns — OS schedules them

MULTI CORE — Parallelism (truly simultaneous):

Timeline: │ T-A │ T-A │ T-A │ T-A │
Core 1:    runs   runs  runs  runs
Core 2:   │ T-B │ T-B │ T-B │ T-B │
           runs   runs  runs  runs
           Both run at the exact same clock tick
```

**Java implication:**

On a single-core machine, concurrency bugs (race conditions) are rare in practice — threads truly take turns, so simultaneous writes are impossible. On a multi-core machine (every modern device), race conditions are **real** — two cores literally execute the same instruction at the same nanosecond on different cache lines of the same variable.

This is why single-threaded testing often hides bugs that only appear in production multi-core environments.

---

---

# SECTION 2: HOW JAVA CODE EXECUTES

---

### Q11. [MID] Describe the journey of Java source code from `.java` to CPU execution.

**Answer:**

```
Stage 1: COMPILATION (build time)
  HelloWorld.java
      │  javac
      ▼
  HelloWorld.class   ← platform-independent bytecode
      │               (not machine code — JVM-readable instructions)
      │

Stage 2: CLASS LOADING (runtime, at startup)
  JVM ClassLoader reads .class
      │
      ▼
  Bytecode in Method Area (part of heap/metaspace)

Stage 3: INTERPRETATION (cold start)
  JVM Interpreter reads bytecode line by line
      │  slow but immediate
      ▼
  CPU executes generated native code

Stage 4: JIT COMPILATION (for "hot" code)
  JVM profiler identifies hot methods (called frequently)
      │  JIT compiler
      ▼
  Native machine code   ← CPU-specific (x86, ARM, etc.)
  stored in Code Cache  ← in JVM memory

Stage 5: OPTIMIZED EXECUTION
  CPU fetches from Code Cache
      │
      ▼
  Registers + L1 cache serve hot data
  Result: 10–100x faster than interpretation
```

Key insight: Java is **not purely interpreted** (contrary to myth) and **not purely compiled** (like C). The JIT makes it adaptive — slow at startup, then faster as it learns hot paths.

> Resource: [JVM JIT Compilation — Inside the JVM (YouTube by Oracle)](https://www.youtube.com/watch?v=oH4_unx8eJQ)

---

### Q12. [MID] What is the JIT compiler and why does it matter for multithreaded programs?

**Answer:**

The JIT (Just-In-Time) compiler is a component inside the JVM that converts bytecode into native CPU instructions at runtime. It profiles execution to find "hot" code paths and aggressively optimizes them.

**Why it matters for multithreaded programs specifically:**

1. **Variable caching in registers:** The JIT may keep a variable's value in a CPU register across multiple iterations. If another thread updates that variable in RAM/L1, the JIT-optimized code never re-reads it — producing a stale value. This is the exact scenario `volatile` prevents.

2. **Instruction reordering:** The JIT is allowed to reorder instructions as long as single-threaded semantics are preserved. In multithreaded contexts, this reordering can break visibility guarantees.

3. **Lock elision:** The JIT can remove synchronization entirely if it can prove (via escape analysis) that the locked object is not accessible to other threads.

4. **Lock coarsening:** The JIT can merge multiple small synchronized blocks into one larger one — reducing lock/unlock overhead.

```java
// JIT may turn this:
int sum = 0;
for (int i = 0; i < 1000000; i++) {
    sum += i;
}

// Into this (keeping `sum` in a register, never writing to RAM until the loop ends):
// register_R1 = 0;
// for i = 0 to 999999: R1 += i
// sum = R1;  // single write to memory
```

If `sum` were `volatile`, the JIT could not cache it in a register — every iteration would read/write to main memory.

---

### Q13. [MID][DIAGRAM] Explain how the Program Counter works and why each thread needs its own.

**Answer:**

The Program Counter (PC) is a small register in each CPU core that holds the **memory address of the next instruction to execute**. After each instruction runs, the PC is incremented to point to the next instruction.

**Why each thread needs its own PC:**

```
Thread A's view:                    Thread B's view:
  Code:                               Code:
  0x1000: LOAD x                      0x1000: LOAD x
  0x1004: ADD 1                       0x1004: ADD 1
  0x1008: STORE x                     0x1008: STORE x

Thread A's PC: 0x1004                Thread B's PC: 0x1008
  (about to execute ADD)               (about to execute STORE)

  Both threads run the same code,
  but they're at different positions in that code.
  If they shared a PC, one would corrupt the other's position.
```

When the OS context-switches from Thread A to Thread B:

```
1. Save Thread A's PC (value: 0x1004) → into Thread A's PCB in RAM
2. Load Thread B's PC (value: 0x1008) ← from Thread B's PCB in RAM
3. Resume execution at 0x1008 — Thread B picks up exactly where it left off
```

The private PC is what makes each thread an independent execution path through the same shared code.

---

### Q14. [HARD] What is a context switch? Why is it expensive? What are its components?

**Answer:**

A **context switch** is when the OS pauses one thread and resumes another on the same CPU core.

**Components of a context switch:**

```
Thread A running on Core 1
          │
          │  OS timer interrupt fires (~every 1–10ms)
          ▼
OS Scheduler takes control (kernel mode)
          │
          ├── SAVE Thread A's state → PCB in RAM:
          │     - All registers (R0...Rn) — may be 16–32 values
          │     - Program Counter value
          │     - Stack Pointer
          │     - CPU Flags (zero flag, carry flag, etc.)
          │
          ├── SELECT Thread B from run queue
          │     (based on priority, fairness policy, affinity)
          │
          ├── LOAD Thread B's state ← PCB from RAM:
          │     - Restore all registers
          │     - Restore Program Counter
          │     - Restore Stack Pointer
          │
          ▼
Thread B resumes on Core 1 — exactly where it was paused
```

**Why expensive:**

1. **PCB save/restore** — dozens of register values written to and read from RAM: ~1,000–5,000 ns
2. **Cold cache** — Thread B's working data is NOT in L1/L2. Every data access is a cache miss → RAM fetch (100x slower). Takes thousands of accesses to "warm up" the cache again.
3. **TLB flush** — for process switches (not thread switches), the CPU's virtual-to-physical address translation cache is invalidated.
4. **Kernel mode transition** — switching from user mode to kernel mode (for the scheduler) and back has overhead.

**In numbers:** A context switch costs approximately 1–30 microseconds. For comparison, a RAM access costs ~100ns, and an L1 cache hit costs ~1ns. Context switches are ~1000x more expensive than L1 cache operations.

**This is why Project Loom virtual threads are faster** — virtual thread switches are done in JVM user space, avoiding kernel mode transitions and saving only the virtual thread's call stack (not the full OS thread state).

> Resource: [Understanding Context Switches — Brendan Gregg](https://www.brendangregg.com/blog/2015-09-16/linux-perf-topdown-pm-x.html)

---

### Q15. [MID][TRAP] A Java program running on a 16-core machine has 4 threads. How many threads actually run simultaneously?

**Answer:**

**Maximum 4** — limited by the number of threads, not the number of cores. You need a thread running on each core to achieve parallelism.

```
16-core machine, 4 threads:

Core  1: Thread-1 running
Core  2: Thread-2 running
Core  3: Thread-3 running
Core  4: Thread-4 running
Core  5: IDLE
Core  6: IDLE
...
Core 16: IDLE
```

The 4 threads can run truly in parallel on 4 different cores simultaneously. The other 12 cores sit idle (or run OS threads and other processes).

**The trap:** Many developers assume more cores always means faster. Adding a 5th core does not help if you only have 4 threads. Conversely, running 1,000 threads on 16 cores means heavy context switching overhead — not 1,000 simultaneous executions.

**Practical rule:** For CPU-bound work, optimal thread count ≈ number of CPU cores. For IO-bound work, optimal thread count >> number of cores (threads spend most time waiting, not running).

---

---

# SECTION 3: THREAD LIFE CYCLE

---

### Q16. [EASY] List all 6 Java thread states and give a one-line description of each.

**Answer:**

```
State           | One-Line Description
────────────────┼──────────────────────────────────────────────────────────
NEW             | Thread object created, .start() not yet called
RUNNABLE        | Thread is alive — either waiting for CPU or currently running
BLOCKED         | Waiting to acquire a monitor lock held by another thread
WAITING         | Voluntarily suspended indefinitely — waiting for notify/join
TIMED_WAITING   | Voluntarily suspended with a timeout — will wake at deadline
TERMINATED      | run() method has returned; OS thread is gone
```

**In Java code:**

```java
Thread t = new Thread(() -> {});
System.out.println(t.getState()); // NEW

t.start();
System.out.println(t.getState()); // RUNNABLE

t.join(); // wait for it to finish
System.out.println(t.getState()); // TERMINATED
```

> Resource: [Thread.State Javadoc — Oracle](https://docs.oracle.com/en/java/se/21/docs/api/java.base/java/lang/Thread.State.html)

---

### Q17. [MID][DIAGRAM] Draw the complete thread state transition diagram with all triggers.

**Answer:**

```
                  new Thread()
                       │
                       ▼
                      NEW
                       │
                  .start()
                       │
                       ▼
         ┌─────────────────────────────┐
         │          RUNNABLE            │
         │  ┌──────────┐  ┌──────────┐ │
         │  │  READY   │◄►│ RUNNING  │ │
         │  │(queued)  │  │(on CPU)  │ │
         │  └──────────┘  └──────────┘ │
         └──────────────────────────────┘
                  │         │         │
                  │         │         │
           wait() │  sleep(n)│  synchronized │
           join() │  wait(n) │  (lock held)  │
           park() │  join(n) │               │
                  │         │               │
                  ▼         ▼               ▼
              WAITING  TIMED_WAITING     BLOCKED
                  │         │               │
           notify()│  timeout│     lock      │
         notifyAll()│ expires │   released   │
           join() │         │               │
           returns │         │               │
                  └─────────┴───────────────┘
                                  │
                              RUNNABLE
                                  │
                         run() returns or
                         uncaught exception
                                  │
                                  ▼
                             TERMINATED
```

**Key rules:**
- BLOCKED → RUNNABLE only when the contested monitor is released
- WAITING → always goes to BLOCKED first (must re-acquire lock) → then RUNNABLE
- TERMINATED is a terminal state — no transitions out of it ever

---

### Q18. [MID] What is the difference between BLOCKED and WAITING? This is a very commonly asked interview question.

**Answer:**

**BLOCKED:**
- Thread tried to enter a `synchronized` block or method
- The monitor lock is currently held by another thread
- The thread did not choose to wait — it was stopped involuntarily
- It will automatically become RUNNABLE when the lock is released
- Holds NO locks while blocked

**WAITING:**
- Thread explicitly called `wait()`, `join()`, or `LockSupport.park()`
- The thread voluntarily gave up control and suspended itself
- It will NOT resume until another thread calls `notify()`, `notifyAll()`, or `unpark()`
- Holds NO locks (if using `wait()` — the lock is released)

```
BLOCKED:                            WAITING:
Thread WANTS to enter:              Thread CHOSE to pause:

synchronized(lock) {  ← BLOCKED    synchronized(lock) {
    doWork();           here          lock.wait();  ← WAITING here
}                                   }
                                    // lock IS released while waiting
// lock NOT released —
// just can't acquire it
```

**Interview trick answer:**
> "BLOCKED is involuntary — the thread is stuck trying to do something. WAITING is voluntary — the thread decided to pause and wait for a signal. Both consume zero CPU, but they get out of their state differently: BLOCKED exits when the lock becomes free, WAITING exits only when explicitly notified."

---

### Q19. [MID][TRAP] Is RUNNABLE the same as "currently running"?

**Answer:**

**No** — this is a very common misconception.

`RUNNABLE` in Java means the thread is **eligible to run**. It includes two actual sub-states that Java does NOT distinguish:

1. **READY** — Thread is in the OS run queue, waiting for a CPU core to be assigned
2. **RUNNING** — Thread is currently executing on a CPU core right now

```
Java's view:              OS's view:
RUNNABLE                  READY  ←→  RUNNING
    │                         (Java hides this distinction)
    │
    └── Java cannot observe which core a thread is on —
        that's the OS scheduler's domain
```

**Why Java doesn't separate them:** To know if a thread is "currently running," you'd need to query the OS scheduler — a kernel call, platform-specific, and expensive. The abstraction intentionally hides it. For Java programming purposes, the distinction doesn't matter: both READY and RUNNING threads will execute your code "soon."

---

### Q20. [EASY] What happens when you call .start() twice on the same Thread object?

**Answer:**

It throws `IllegalThreadStateException` on the second call.

```java
Thread t = new Thread(() -> System.out.println("hello"));
t.start();   // fine — transitions from NEW to RUNNABLE
t.start();   // throws IllegalThreadStateException!
```

**Internally:** After `.start()` is called the first time, `threadStatus` is set to a non-zero value. The `.start()` method checks this field at the very beginning:

```java
// Inside Thread.start() (simplified):
public synchronized void start() {
    if (threadStatus != 0)
        throw new IllegalThreadStateException();
    // ... proceed with start
}
```

After the thread terminates (TERMINATED state), `threadStatus` remains non-zero — so even a terminated thread cannot be restarted. If you need to run the task again, create a **new Thread object**.

---

### Q21. [MID] Can a thread's state go from TERMINATED back to RUNNABLE?

**Answer:**

**No, absolutely not.** TERMINATED is a final state. The thread state machine in Java is strictly one-directional — TERMINATED is the end of the road.

```
Valid transitions:
NEW → RUNNABLE → {BLOCKED, WAITING, TIMED_WAITING} → RUNNABLE → TERMINATED

Invalid:
TERMINATED → any state  ← IMPOSSIBLE
```

The OS-level thread associated with the Java Thread object has already been destroyed when the thread is TERMINATED. The Java `Thread` object still exists on the heap (you can call `getState()` on it and get `TERMINATED`), but no code can execute on it.

To re-run the task: create a new `Thread` object, or better — use a thread pool that manages thread reuse automatically.

---

### Q22. [HARD] What is the threadStatus field and what values does it hold?

**Answer:**

Internally, the Java `Thread` class maintains a private field `threadStatus` of type `int`. The JVM maps this to thread states as follows:

```
threadStatus == 0:                    NEW
threadStatus == JVMTI_THREAD_STATE_ALIVE | ...:  RUNNABLE, BLOCKED, WAITING, TIMED_WAITING
                                                 (specific bits distinguish substates)
threadStatus == JVMTI_THREAD_STATE_TERMINATED:   TERMINATED
```

The `Thread.getState()` method reads `threadStatus` and maps it to the `Thread.State` enum.

**Key behaviors driven by this field:**
- `start()` checks `threadStatus != 0` → throws `IllegalThreadStateException` if non-zero
- `isAlive()` returns `threadStatus != 0 && thread is not TERMINATED`
- `join()` internally uses `while (isAlive()) wait(0)` — polls via `isAlive()`

This is a JVM implementation detail, but understanding it clarifies why you cannot restart threads and why `isAlive()` returns false for both NEW and TERMINATED threads.

---

### Q23. [MID] What thread state will a thread be in while it is sleeping?

**Answer:**

**TIMED_WAITING** — when `Thread.sleep(millis)` is called, the thread transitions to TIMED_WAITING and will automatically return to RUNNABLE when either:
1. The specified time elapses
2. `Thread.interrupt()` is called (throws `InterruptedException`)

```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        System.out.println("interrupted!");
    }
});
t.start();
Thread.sleep(100); // let t get into sleep
System.out.println(t.getState()); // TIMED_WAITING
```

**Important:** While in TIMED_WAITING due to `sleep()`, the thread **retains all locks it holds**. Other threads trying to acquire those locks will be BLOCKED for the full sleep duration.

---

### Q24. [MID][TRAP] What thread state does a thread enter when it calls object.wait()?

**Answer:**

**WAITING** (if called without a timeout) or **TIMED_WAITING** (if called with a timeout).

```java
// No timeout → WAITING
synchronized (lock) {
    lock.wait();       // → WAITING
}

// With timeout → TIMED_WAITING
synchronized (lock) {
    lock.wait(5000);   // → TIMED_WAITING
}
```

**The critical trap:** Unlike `sleep()`, `wait()` **releases the monitor lock**. When the thread wakes up (via `notify()` or timeout), it does NOT go directly to RUNNABLE. It goes to **BLOCKED** first — it must re-acquire the lock before it can run.

```
lock.wait() called
    │
    │ → Releases lock
    │ → Thread moves to WAITING
    │
    │  notify() called
    ▼
BLOCKED (competing to re-acquire the lock)
    │
    │  Thread wins the lock
    ▼
RUNNABLE (resumes from after wait())
```

This WAITING → BLOCKED → RUNNABLE path (not WAITING → RUNNABLE directly) is what most developers get wrong.

---

### Q25. [EASY] What is the purpose of isAlive()? What does it return for NEW and TERMINATED threads?

**Answer:**

`isAlive()` returns `true` if the OS-level thread exists — i.e., if `.start()` has been called and the thread has not yet terminated.

```java
Thread t = new Thread(() -> doWork());

System.out.println(t.isAlive()); // false  (NEW — no OS thread yet)
t.start();
System.out.println(t.isAlive()); // true   (RUNNABLE — OS thread exists)
t.join();
System.out.println(t.isAlive()); // false  (TERMINATED — OS thread gone)
```

**Under the hood:** `isAlive()` is implemented as a native method that checks whether the OS thread handle is still valid. For NEW threads, no OS handle exists yet. For TERMINATED threads, the OS handle has been cleaned up.

**Common use case:**

```java
// Wait for a thread to finish (primitive alternative to join):
while (t.isAlive()) {
    Thread.sleep(10); // spin-wait — use join() instead in practice
}
```

---

### Q26. [HARD] Describe the daemon thread concept. What happens to daemon threads when all non-daemon threads finish?

**Answer:**

A **daemon thread** is a background thread that does not prevent the JVM from exiting. The JVM exits when **all non-daemon threads have terminated** — daemon threads are killed immediately (mid-task if necessary) when this happens.

```
Non-daemon threads:        Daemon threads:
  main()                     GC thread
  your thread pool workers   JIT compiler thread
  request handler threads    log flusher
                             cache eviction thread
        │                            │
        │ All finish                 │
        ▼                            │
   JVM exits ───────────────────────► daemon threads killed immediately
                                      (no cleanup, no finally blocks guaranteed)
```

```java
Thread background = new Thread(() -> {
    while (true) {
        cleanupExpiredSessions();
        Thread.sleep(60000);
    }
});
background.setDaemon(true);   // MUST be called BEFORE .start()
background.start();

// When main() finishes, JVM exits, kills background thread
// Even if it's in the middle of cleanupExpiredSessions()
```

**Rules:**
- `setDaemon(true)` must be called before `.start()` — throws `IllegalThreadStateException` after
- Daemon threads inherit daemon status from their parent thread
- Thread pool threads (from `Executors.newFixedThreadPool()`) are **non-daemon** by default — this is why you must call `executor.shutdown()` or the JVM never exits

**When to use daemon:**
- Periodic housekeeping (log rotation, cache cleanup, heartbeats)
- Background monitoring
- Any task that can be safely abandoned at JVM shutdown

**When NOT to use daemon:**
- Writing to files or databases (incomplete write = corruption)
- Sending network messages (transaction may be half-done)
- Anything requiring a clean shutdown sequence

---

---

# SECTION 4: CREATING THREADS

---

### Q27. [EASY] What actually happens when you call new Thread()? Does it create an OS thread?

**Answer:**

**No.** `new Thread()` only creates a Java object on the heap. No OS thread is created yet.

Steps in `new Thread(runnable)`:

```
1. Allocate Thread object on heap (like any new Object())
2. Copy daemon status from parent thread (inherits parent's daemon flag)
3. Copy priority from parent thread (usually 5 = NORM_PRIORITY)
4. Store the Runnable in the `target` field (does not execute it)
5. Set threadStatus = 0 (NEW state)
6. Assign a unique thread ID (from an AtomicLong counter)
7. Register with the parent ThreadGroup

Result: A plain Java object. Zero system calls. OS has no knowledge.
```

The OS thread is created **only when `.start()` is called**, via the native method `start0()` which invokes `pthread_create` (Linux) or `CreateThread` (Windows).

---

### Q28. [MID][CODE] What does .start() actually do internally? Show the simplified source.

**Answer:**

```java
// Simplified Thread.start() from OpenJDK source:
public synchronized void start() {
    // 1. Check state — cannot start if already started or terminated
    if (threadStatus != 0)
        throw new IllegalThreadStateException();

    // 2. Register with ThreadGroup
    group.add(this);

    boolean started = false;
    try {
        // 3. THE KEY CALL — native method that calls the OS
        start0();  // → pthread_create (Linux) or CreateThread (Windows)
        started = true;
    } finally {
        // 4. If native call failed, remove from group
        try {
            if (!started) {
                group.threadStartFailed(this);
            }
        } catch (Throwable ignore) {}
    }
}

private native void start0(); // implemented in JVM C++ code
```

`start0()` is the bridge to native code. It:
1. Asks the OS to create a new kernel-level thread
2. Allocates a native stack for it (default 512KB–1MB)
3. Tells the OS scheduler the thread is ready to run
4. Sets `threadStatus` to indicate RUNNABLE

After `start0()` returns, the OS owns the thread scheduling. Your `run()` method may execute immediately or after milliseconds — you have no control.

---

### Q29. [MID] Why should you always name your threads? How do you do it?

**Answer:**

**Why it matters:** When your application crashes or hangs in production, you analyze thread dumps. Auto-named threads (`Thread-0`, `Thread-1`, `Thread-47`) tell you nothing. Named threads (`payment-processor-3`, `sync-worker-io-2`) tell you exactly where to look.

```
BAD thread dump:
"Thread-47" prio=5 tid=0x00007f8b4c0a5800 nid=0x6b2f
  at java.lang.Object.wait(Native Method)
  -- WHO IS THIS? What was it doing?

GOOD thread dump:
"payment-processor-3" prio=5 tid=0x00007f8b4c0a5800 nid=0x6b2f
  at com.myapp.PaymentService.processPayment(PaymentService.java:87)
  -- Oh! It's stuck waiting for the payment gateway timeout.
```

**How to name:**

```java
// Option 1: Constructor
Thread t = new Thread(task, "payment-processor-1");

// Option 2: setName before start
Thread t = new Thread(task);
t.setName("payment-processor-1");
t.start();

// Option 3: ThreadFactory (best for thread pools)
ThreadFactory namedFactory = new ThreadFactory() {
    private final AtomicInteger count = new AtomicInteger(0);
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("payment-processor-" + count.incrementAndGet());
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
};

ExecutorService pool = Executors.newFixedThreadPool(4, namedFactory);
```

---

### Q30. [MID] What is a ThreadGroup? Is it still relevant in modern Java?

**Answer:**

`ThreadGroup` was Java's original mechanism for organizing threads into a hierarchy and applying operations (interrupt, enumerate) to groups of threads. Every thread belongs to exactly one ThreadGroup, inherited from its parent thread.

```java
ThreadGroup group = new ThreadGroup("io-workers");
Thread t1 = new Thread(group, task1, "io-worker-1");
Thread t2 = new Thread(group, task2, "io-worker-2");

// Interrupt all threads in the group
group.interrupt();

// List all threads in the group
Thread[] threads = new Thread[group.activeCount()];
group.enumerate(threads);
```

**Modern relevance:** Largely obsolete. `ThreadGroup` has major limitations:
- `activeCount()` is approximate (not thread-safe)
- `enumerate()` silently drops threads if the array is too small
- No meaningful permission control in modern JVMs
- Most features are deprecated or planned for removal

**Modern replacements:**
- `ExecutorService` for lifecycle management
- `StructuredTaskScope` (Project Loom) for structured concurrency
- Custom tracking with `ConcurrentHashMap<String, Thread>` if you need enumeration

**When ThreadGroup is still used:** Setting an uncaught exception handler for a group of threads:

```java
ThreadGroup group = new ThreadGroup("workers") {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught in {}: {}", t.getName(), e.getMessage(), e);
        alertOncall();
    }
};
```

---

---

# SECTION 5: RUNNABLE VS EXTENDING THREAD

---

### Q31. [EASY] What are the two ways to create a thread task in Java? Which is preferred?

**Answer:**

**Way 1: Extend Thread**
```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("task running");
    }
}
new MyThread().start();
```

**Way 2: Implement Runnable (preferred)**
```java
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("task running");
    }
}
new Thread(new MyTask()).start();

// Modern: lambda (Runnable is a functional interface)
new Thread(() -> System.out.println("task running")).start();
```

**Preferred: Implement Runnable.** The four main reasons:
1. Java has single inheritance — extending Thread blocks extending anything else
2. IS-A vs HAS-A: your class HAS-A runnable behavior, it doesn't IS-A Thread
3. Same Runnable can run on bare Thread, thread pool, ScheduledExecutorService
4. Lambda compatibility — Runnable is a `@FunctionalInterface`

---

### Q32. [MID] Explain the IS-A vs HAS-A principle as it applies to Runnable vs Thread.

**Answer:**

Object-oriented design principle: **favor composition over inheritance**.

**Extending Thread says: "My class IS-A Thread."**
- Your class inherits all of Thread's methods: `start()`, `stop()`, `interrupt()`, `getState()`, `setPriority()`, etc.
- Users of your class can call `myTask.start()`, `myTask.interrupt()`, `myTask.getState()` — thread management leaks into your task API.
- Your class is permanently coupled to the Thread execution model.

**Implementing Runnable says: "My class HAS-A behavior that can be run on a thread."**
- Your class exposes only `run()` — a clean single-method API.
- Thread management is the caller's responsibility. Your task is decoupled from execution.
- The same Runnable instance can be submitted to any executor.

```java
// BAD: task exposes thread management
class PaymentProcessor extends Thread {
    // Users can call:
    paymentProcessor.start();     // intended
    paymentProcessor.stop();      // dangerous! deprecated
    paymentProcessor.setPriority(10); // accidentally misconfigures
    paymentProcessor.setName("hacked"); // oops
}

// GOOD: task exposes only its behavior
class PaymentProcessor implements Runnable {
    public void run() { /* process payment */ }
    // Users can ONLY call run() — clean API
}
Thread t = new Thread(paymentProcessor); // execution separate from task
```

---

### Q33. [MID][CODE] Show a case where extending Thread prevents correct object-oriented design.

**Answer:**

```java
// Scenario: Processing HTTP requests. Handler needs to extend HttpServlet.
// Java only allows single inheritance.

// IMPOSSIBLE — Java doesn't support multiple inheritance:
class RequestHandler extends Thread, extends HttpServlet {  // COMPILE ERROR
    public void run() { handleRequest(); }
    protected void doGet(HttpServletRequest req, HttpServletResponse res) { ... }
}

// WITH RUNNABLE — works perfectly:
class RequestHandler extends HttpServlet implements Runnable {
    @Override
    public void run() {
        handleRequest(); // can be submitted to any thread pool
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        // servlet logic
    }
}

// Usage:
RequestHandler handler = new RequestHandler();
executor.submit(handler);          // run on thread pool
server.registerServlet(handler);   // also register as servlet
```

This is a real pattern in embedded servers and test frameworks where the same class needs to be both a servlet and a Runnable.

---

### Q34. [EASY][CODE] Show how the same Runnable can be submitted to different executors.

**Answer:**

```java
Runnable task = () -> {
    System.out.println("Running on: " + Thread.currentThread().getName());
};

// Submit to bare Thread:
new Thread(task, "bare-thread").start();

// Submit to fixed thread pool:
ExecutorService fixed = Executors.newFixedThreadPool(4);
fixed.submit(task);

// Submit to scheduled executor (runs after 2 seconds):
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.schedule(task, 2, TimeUnit.SECONDS);

// Submit to ForkJoinPool:
ForkJoinPool fjp = new ForkJoinPool(4);
fjp.submit(task);

// All four submit the SAME Runnable to different execution mechanisms.
// If we had used `extends Thread`, this flexibility would be impossible.
```

---

---

# SECTION 6: THE SYNCHRONIZED KEYWORD

---

### Q35. [EASY] What is a monitor in Java? Does every object have one?

**Answer:**

A **monitor** (also called intrinsic lock or object lock) is a built-in mutual exclusion mechanism. **Every Java object has exactly one monitor** — it is part of the object header in JVM heap memory, not a separate object.

The monitor enforces two properties:
1. **Mutual exclusion:** At most one thread can hold a given monitor at any time
2. **Visibility:** Acquiring and releasing a monitor flushes/refreshes memory

```
Java Heap:

Object A:
┌─────────────────────────────┐
│ Object Header               │
│   ├── Class pointer         │
│   ├── Hash code             │
│   └── Monitor (lock) info   │ ← built into every object
│       ├── Lock state        │
│       ├── Owner thread ID   │
│       └── Wait set          │
├─────────────────────────────┤
│ Object fields               │
│   field1: ...               │
│   field2: ...               │
└─────────────────────────────┘
```

When you write `synchronized (obj) { ... }`, Java acquires the monitor embedded in `obj`'s header, runs the block, and releases it. If another thread already holds that monitor, the new thread is BLOCKED until the monitor is released.

> Resource: [Inside the JVM — Chapter on Monitors (Bill Venners)](https://www.artima.com/insidejvm/ed2/threadsynchP.html)

---

### Q36. [MID] What are the three forms of synchronized? What lock does each acquire?

**Answer:**

**Form 1: Synchronized instance method — lock is `this`**

```java
public class Counter {
    private int count = 0;

    public synchronized void increment() {  // lock = this instance
        count++;
    }
}

// Two different Counter instances have DIFFERENT monitors:
Counter c1 = new Counter();
Counter c2 = new Counter();
// c1.increment() and c2.increment() do NOT block each other
// c1.increment() from Thread A and c1.increment() from Thread B DO block each other
```

**Form 2: Synchronized static method — lock is the Class object**

```java
public class Registry {
    private static int total = 0;

    public static synchronized void register() {  // lock = Registry.class
        total++;
    }
}
// Only ONE thread can call register() at a time, regardless of instance
// Registry.class is a single object in the JVM
```

**Form 3: Synchronized block — lock is specified explicitly (most flexible)**

```java
public class OrderService {
    private final Object lock = new Object();  // dedicated private lock
    private int processedCount = 0;

    public void processOrder(Order order) {
        // Non-critical work outside the lock:
        validateOrder(order);                   // concurrent OK

        synchronized (lock) {                  // lock = lock object
            processedCount++;                  // critical section: minimal
        }

        sendConfirmation(order);               // concurrent OK
    }
}
```

**Which to use:** Synchronized blocks (Form 3) are usually best — they let you minimize the critical section size and use a private lock object.

---

### Q37. [MID] What does synchronized guarantee? List all four guarantees.

**Answer:**

```
1. MUTUAL EXCLUSION
   Only one thread holds a given monitor at a time.
   All other threads trying to enter are BLOCKED.

2. VISIBILITY (Memory Flush on Release)
   When a thread exits a synchronized block, ALL writes made during
   that block are flushed to main memory.
   When a thread enters a synchronized block, it refreshes its view
   from main memory — it sees the latest writes by any previous holder.

3. ORDERING (Happens-Before)
   All actions before a monitor release "happen-before" any subsequent
   acquisition of the same monitor.
   The JIT and CPU cannot reorder instructions across the barrier.

4. ATOMICITY
   Code inside a synchronized block executes as an indivisible unit
   from the perspective of other threads synchronizing on the same lock.
   No thread can observe an intermediate state.
```

**Memory example:**

```java
// Thread A:
synchronized (lock) {
    x = 10;          // write x
    y = 20;          // write y
}                    // ← on exit: x=10 and y=20 flushed to RAM

// Thread B (runs after Thread A releases):
synchronized (lock) { // ← on entry: cache refreshed from RAM
    int a = x;       // guaranteed to read 10
    int b = y;       // guaranteed to read 20
}
```

Without `synchronized`, Thread B might read stale values of x or y.

---

### Q38. [MID] What is lock reentrancy? Why is it important?

**Answer:**

A lock is **reentrant** (also called recursive) if the same thread that already holds the lock can acquire it again without blocking. Java's intrinsic monitors (`synchronized`) are always reentrant.

Internally, the JVM maintains a **hold count** per monitor:
- First acquisition: hold count = 1
- Re-acquisition by same thread: hold count = 2, 3, ...
- Each release: hold count decrements
- Lock is released to other threads when hold count reaches 0

```java
public class ReentrantExample {
    public synchronized void outer() {
        System.out.println("outer: hold count = 1");
        inner(); // same thread re-acquires the same monitor
    }

    public synchronized void inner() {
        // Without reentrancy, calling inner() from outer()
        // would deadlock: outer() holds the lock, inner() waits for it forever
        System.out.println("inner: hold count = 2");
    }
}
```

**Why it matters:** Many natural OOP patterns involve synchronized methods calling other synchronized methods on the same object. Without reentrancy, this would deadlock every time.

**`ReentrantLock`** (from `java.util.concurrent.locks`) is also reentrant — as the name implies. A `Semaphore(1)` is NOT reentrant — a thread trying to acquire it twice will deadlock with itself.

---

### Q39. [HARD] What is the private lock object pattern? Why is using `this` as the lock dangerous?

**Answer:**

**The danger of `this` as the lock:**

When you write `synchronized(this)` or `synchronized` on an instance method, the lock object is `this` — the current instance. Any external code that has a reference to your object can acquire the same lock:

```java
class Counter {
    private int count = 0;

    public synchronized void increment() { // lock = this
        count++;
    }
}

// EXTERNAL code can do this:
Counter c = new Counter();
synchronized (c) {            // acquires the SAME lock as increment()
    doSomethingVeryLong();    // holds the lock for a long time
}
// During doSomethingVeryLong(), increment() is blocked!
// External code accidentally starves your counter.
```

External code can: accidentally deadlock you, starve your methods, or interfere with your synchronization strategy.

**Private lock object pattern (safe):**

```java
class Counter {
    private final Object lock = new Object(); // PRIVATE — nobody outside can acquire this
    private int count = 0;

    public void increment() {
        synchronized (lock) {  // lock = private Object, not this
            count++;
        }
    }
}

// External code cannot interfere:
Counter c = new Counter();
synchronized (c) { ... }  // acquires THIS (c's monitor), NOT lock's monitor
                           // does NOT block increment() at all
```

**Three benefits:**
1. Encapsulation: external code cannot interfere with your locking
2. Flexibility: you can have multiple independent locks for different fields
3. Preventing lock leakage: the lock's scope is truly private

---

### Q40. [HARD][DIAGRAM] Explain deadlock with a code example. What are the prevention strategies?

**Answer:**

**Deadlock conditions (all four must hold simultaneously):**
1. Mutual exclusion (locks are non-shareable)
2. Hold and wait (thread holds one lock while waiting for another)
3. No preemption (locks cannot be forcefully taken)
4. Circular wait (Thread A waits for Thread B, Thread B waits for Thread A)

```java
Object lockA = new Object();
Object lockB = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lockA) {             // T1 acquires lockA
        Thread.sleep(100);             // give T2 time to grab lockB
        synchronized (lockB) {         // T1 WAITS for lockB — HELD BY T2
            System.out.println("T1 done");
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lockB) {             // T2 acquires lockB
        synchronized (lockA) {         // T2 WAITS for lockA — HELD BY T1
            System.out.println("T2 done");
        }
    }
});

// DEADLOCK:
// T1 holds lockA, wants lockB
// T2 holds lockB, wants lockA
// Both wait forever
```

```
DEADLOCK DIAGRAM:

T1 ─── holds ──► lockA
T1 ─── wants ──► lockB ◄── holds ── T2
T2 ─── wants ──► lockA

Circular: T1 → lockB → T2 → lockA → T1
```

**Prevention strategies:**

```
Strategy 1: CONSISTENT LOCK ORDERING
  Always acquire locks in the same global order.
  If all threads always acquire lockA before lockB,
  circular wait is structurally impossible.

  synchronized (lockA) {          // always A first
      synchronized (lockB) { ... }
  }

Strategy 2: tryLock() WITH TIMEOUT (ReentrantLock)
  if (lockA.tryLock(100, TimeUnit.MILLISECONDS)) {
      try {
          if (lockB.tryLock(100, TimeUnit.MILLISECONDS)) {
              try { doWork(); } finally { lockB.unlock(); }
          }
      } finally { lockA.unlock(); }
  }
  // If timeout: give up, retry later

Strategy 3: MINIMIZE LOCK SCOPE
  Hold locks for the shortest time possible.
  Less time holding = less chance of overlap.

Strategy 4: AVOID HOLDING MULTIPLE LOCKS
  Redesign so each operation needs only one lock.
  Use higher-level abstractions (ConcurrentHashMap, BlockingQueue).
```

> Resource: [Java Concurrency in Practice — Chapter 10 (Goetz)](https://jcip.net/)

---

### Q41. [MID] What JVM optimizations apply to synchronized? Name three.

**Answer:**

**1. Biased Locking (removed in Java 15+):**
If only one thread ever uses a lock, the JVM "biases" the lock to that thread. Subsequent acquisitions by the same thread require no CAS instruction — just a register check. Near-zero overhead for uncontended, single-threaded use.

**2. Lock Coarsening:**
The JIT merges multiple adjacent synchronized blocks into one larger block, reducing the number of lock/unlock operations.

```java
// Before coarsening:
synchronized (lock) { op1(); }
synchronized (lock) { op2(); }
synchronized (lock) { op3(); }

// After coarsening (JIT does this):
synchronized (lock) {
    op1(); op2(); op3();  // one lock acquisition instead of three
}
```

**3. Lock Elision (via Escape Analysis):**
If the JIT can prove that a locked object cannot be accessed by other threads (it "doesn't escape" the current thread), the JIT removes the synchronization entirely.

```java
public String buildString() {
    StringBuffer sb = new StringBuffer();  // internally synchronized
    sb.append("hello");
    sb.append(" world");                   // JIT may elide ALL synchronization on sb
    return sb.toString();
    // sb is a local variable — cannot escape to other threads
    // JIT: "no other thread can access sb, so locking is unnecessary"
}
```

---

---

# SECTION 7: THE VOLATILE KEYWORD

---

### Q42. [MID] What problem does volatile solve? Explain with a concrete example.

**Answer:**

`volatile` solves the **CPU cache visibility problem**: when one thread writes a variable, other threads on different CPU cores may still read a stale cached value from their own L1/L2 cache.

**Without volatile — stale read:**

```java
class Worker extends Thread {
    boolean running = true; // NOT volatile

    public void run() {
        // JIT may optimize this to: if (running) { infinite_loop(); }
        // because running never changes from THIS thread's perspective
        while (running) {
            doWork();
        }
        System.out.println("stopped"); // may never print!
    }

    public void stop() { running = false; } // written on another thread
}
```

```
Core 1 (Worker thread):            Core 2 (main thread):
L1 Cache: running = true           running = false → written to Core 2's L1
(never refreshed from RAM)         (may not flush to RAM immediately)

Worker reads from its own L1: running = true forever → infinite loop
```

**With volatile — guaranteed visibility:**

```java
class Worker extends Thread {
    volatile boolean running = true; // volatile!

    public void run() {
        while (running) { // reads from MAIN MEMORY every time
            doWork();
        }
        System.out.println("stopped"); // guaranteed to reach here
    }

    public void stop() { running = false; } // writes directly to MAIN MEMORY
}
```

Every write to a `volatile` variable bypasses the CPU cache and goes straight to main memory. Every read of a `volatile` variable bypasses the CPU cache and reads from main memory. All threads see the same value.

---

### Q43. [MID][TRAP] Does volatile guarantee atomicity?

**Answer:**

**No — this is one of the most common interview misconceptions.**

`volatile` guarantees:
- ✅ **Visibility** — every read/write goes to/from main memory
- ✅ **Ordering** — prevents reordering around the volatile access
- ❌ **Atomicity** — does NOT make compound operations atomic

```java
volatile int counter = 0;

// Thread 1 and Thread 2 both run:
counter++;

// counter++ is NOT a single atomic operation.
// It compiles to THREE separate steps:
// Step 1: READ  counter from main memory (atomic due to volatile)
// Step 2: ADD   1 in local register
// Step 3: WRITE result to main memory (atomic due to volatile)

// Steps 1-3 are NOT atomic as a unit:
// T1: reads counter = 0
// T2: reads counter = 0  ← BEFORE T1 writes
// T1: writes 0+1 = 1
// T2: writes 0+1 = 1
// Final: counter = 1, should be 2!
```

**For atomic compound operations use:**
```java
// Option 1: AtomicInteger (CAS-based, lock-free)
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet(); // truly atomic

// Option 2: synchronized (mutual exclusion)
synchronized (lock) { counter++; }
```

**What volatile IS atomic for:** Reading and writing a single `volatile` variable of 32 bits or less (int, boolean, char, etc.) is atomic. Also, `long` and `double` writes are guaranteed atomic only when declared `volatile` (on 32-bit JVMs, non-volatile long/double writes can be split into two 32-bit writes).

---

### Q44. [HARD] Explain the double-checked locking singleton pattern. Why does it require volatile?

**Answer:**

Double-checked locking (DCL) is a pattern to lazily initialize a singleton with minimal synchronization overhead:

```java
// BROKEN without volatile:
class Singleton {
    private static Singleton instance; // NOT volatile

    public static Singleton getInstance() {
        if (instance == null) {            // CHECK 1: no lock (fast path)
            synchronized (Singleton.class) {
                if (instance == null) {    // CHECK 2: with lock (recheck)
                    instance = new Singleton(); // PROBLEM HERE
                }
            }
        }
        return instance;
    }
}
```

**Why it breaks:** `instance = new Singleton()` is THREE operations:
```
1. Allocate memory for Singleton on heap
2. Initialize the Singleton object (run constructor, set fields)
3. Write the reference into the `instance` variable

JVM/CPU is allowed to reorder 2 and 3:
1. Allocate memory
3. Write reference to `instance` (instance != null now!)
2. Initialize (constructor runs LATER)

Another thread:
  Reads instance != null (step 3 happened)
  Returns instance
  Uses it ← BUT CONSTRUCTOR HASN'T RUN YET!
  → Uses a partially initialized object → crash or corrupt state
```

**With volatile — fixed:**

```java
class Singleton {
    private static volatile Singleton instance; // volatile!

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                    // volatile write: creates a memory barrier
                    // steps 1, 2, 3 cannot be reordered past the volatile write
                    // constructor MUST complete before instance is visible
                }
            }
        }
        return instance;
    }
}
```

`volatile` inserts a **StoreStore barrier** before the write — all prior stores (constructor field writes) must complete before the `instance` variable is written. Any thread reading `instance != null` is guaranteed to also see the fully initialized object.

> Resource: [Java Memory Model and the DCL idiom — IBM Developer](https://developer.ibm.com/technologies/java/)

---

### Q45. [MID] When should you use volatile vs synchronized?

**Answer:**

```
Use VOLATILE when:
  ✓ Single variable visibility (flag, reference, status field)
  ✓ One writer, multiple readers
  ✓ Simple write (not compound: no read-modify-write)
  ✓ Lazy initialization (DCL singleton with volatile)

  Examples:
    volatile boolean shutdown = false;
    volatile long lastSyncTime = 0;
    volatile Singleton instance = null; // DCL

Use SYNCHRONIZED when:
  ✓ Need mutual exclusion (only one thread at a time)
  ✓ Compound operations (read-modify-write, check-then-act)
  ✓ Multiple related variables that must change together atomically
  ✓ Using wait()/notify() (must be inside synchronized)

  Examples:
    synchronized { if (balance >= amount) balance -= amount; }
    synchronized { map.put(key, val); count++; }

Use AtomicXxx (e.g., AtomicInteger) when:
  ✓ Single variable compound operations (increment, CAS)
  ✓ No need for mutual exclusion of larger blocks
  ✓ Performance-critical (lock-free, uses CAS)

  Examples:
    counter.incrementAndGet()
    ref.compareAndSet(expected, updated)
```

**Decision flowchart:**

```
One variable, simple write, one writer?
    YES → volatile
    NO ↓

Single variable compound op (i++, CAS)?
    YES → AtomicInteger / AtomicReference
    NO ↓

Multiple variables or complex logic?
    YES → synchronized or ReentrantLock
```

---

### Q46. [MID] What ordering guarantees does volatile provide?

**Answer:**

`volatile` provides **memory ordering** guarantees in addition to visibility:

**Write barrier (before volatile write):** All writes performed BEFORE a volatile write are guaranteed to be committed to main memory BEFORE the volatile write itself. Writes before it cannot be reordered to after it.

**Read barrier (after volatile read):** All reads performed AFTER a volatile read are guaranteed to read from main memory. Reads after it cannot be reordered to before it.

```java
int a = 0;
volatile int v = 0;
int b = 0;

// Thread 1:
a = 10;      // write to a
v = 1;       // volatile write — BARRIER: a=10 must be committed to RAM before this
b = 20;      // write to b — may happen before or after v=1

// Thread 2 (reads v = 1):
int x = v;   // volatile read — BARRIER: reads after this see latest values
int y = a;   // guaranteed to see a = 10 (because write barrier was before v=1)
int z = b;   // NOT guaranteed to see b = 20 (b is non-volatile, no barrier for it)
```

This is how the DCL singleton works: the constructor writes are all before the `volatile instance` write (barrier), so any thread reading `instance != null` sees a fully initialized object.

---

---

# SECTION 8: WAIT() AND NOTIFY()

---

### Q47. [MID] What problem do wait() and notify() solve? Why not use sleep() for coordination?

**Answer:**

**The problem:** One thread needs to pause until another thread creates a condition (e.g., produces data, releases a resource, changes state).

**Why sleep() is wrong for coordination:**

```java
// BAD — polling with sleep:
while (!dataAvailable()) {    // check condition
    Thread.sleep(10);         // wait 10ms and try again
}
processData();

Problems:
1. CPU waste: waking up every 10ms even if data won't be available for hours
2. Latency: up to 10ms delay after data becomes available
3. Unpredictable: the right interval depends on the producer's speed — unknown
4. Burns power on mobile/cloud: unnecessary wakeups drain battery/cost money
```

**wait()/notify() solution:**

```java
// Thread A (consumer):
synchronized (lock) {
    while (!dataAvailable()) { // condition check
        lock.wait();           // ZERO CPU while waiting
    }                          // woken up exactly when producer says so
    processData();
}

// Thread B (producer):
synchronized (lock) {
    produceData();
    lock.notifyAll();          // wake up consumer precisely now
}
```

With `wait()`, the consumer consumes **zero CPU** while waiting. It wakes up in microseconds after `notifyAll()` — not after a fixed sleep interval. This is both more efficient and more responsive.

---

### Q48. [HARD] What are the three iron rules of wait() and why does each exist?

**Answer:**

**Rule 1: Must be called inside a synchronized block on the same object.**

```java
// CORRECT:
synchronized (lock) {
    lock.wait(); // OK
}

// WRONG — throws IllegalMonitorStateException:
lock.wait(); // called outside synchronized
```

Why: `wait()` must atomically release the lock AND enter the wait set. You can only release a lock you hold. If called outside synchronized, you don't hold the lock — nothing to release, and the atomicity guarantee is meaningless.

**Rule 2: wait() atomically releases the monitor AND suspends the thread.**

These two operations are indivisible:
```
Between "release lock" and "enter wait set",
there must be NO window where:
  - the lock is released
  - but the thread hasn't entered the wait set yet

If there were a window:
  Producer could call notify() during that window
  → notify() sends to empty wait set
  → Consumer enters wait set after notify()
  → Consumer waits forever (missed signal)
```

The atomicity prevents "missed signals" — a classic concurrent programming bug.

**Rule 3: After being notified, the thread must re-acquire the monitor before running.**

```
Sequence: lock.wait() notified
                │
                ▼
            BLOCKED (competing for monitor re-acquisition)
                │
                ▼
            RUNNABLE (resumes after winning the lock)
```

Why: The lock was released when `wait()` was called. The thread can only safely access shared state while holding the lock. So before resuming, it must re-acquire the lock.

---

### Q49. [HARD][TRAP] Why must wait() always be in a while loop and NEVER an if statement?

**Answer:**

Two reasons, either of which alone justifies using `while`:

**Reason 1: Spurious wakeups**

The Java Language Specification explicitly states that a thread can wake up from `wait()` without `notify()` or `notifyAll()` being called. This is called a **spurious wakeup** and happens due to OS-level signal handling on some platforms (Linux, pthreads).

```java
// With if — BROKEN:
synchronized (lock) {
    if (queue.isEmpty()) {   // checked once
        lock.wait();
    }
    // ← spurious wakeup lands here with queue STILL EMPTY
    Item item = queue.poll(); // NullPointerException / IndexOutOfBoundsException!
}

// With while — SAFE:
synchronized (lock) {
    while (queue.isEmpty()) { // re-checked every time we wake up
        lock.wait();
    }
    // guaranteed: queue is NOT empty here
    Item item = queue.poll(); // safe
}
```

**Reason 2: Condition can change between notify() and lock re-acquisition**

```
Thread A (consumer) wakes from wait(), enters BLOCKED
Thread B (consumer) is ALSO in BLOCKED, wins the lock first
Thread B: queue is not empty → polls item → queue is now empty again
Thread A: finally wins the lock, resumes from if-wait → but queue is empty again!

With if: A proceeds to poll → crash
With while: A re-checks → queue.isEmpty() is true → waits again → correct
```

**Golden rule:** `while (!condition) { wait(); }` — always `while`, never `if`.

---

### Q50. [MID] Explain the producer-consumer pattern using wait() and notifyAll(). Show the full implementation.

**Answer:**

```java
public class BoundedBuffer<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
    }

    // Called by producer threads
    public synchronized void produce(T item) throws InterruptedException {
        // Wait while full — producers cannot add
        while (queue.size() == capacity) {
            System.out.println("Buffer full, producer waiting");
            wait(); // releases lock + suspends
        }
        queue.offer(item);
        System.out.println("Produced: " + item + " | Size: " + queue.size());
        notifyAll(); // wake up any waiting consumers
    }

    // Called by consumer threads
    public synchronized T consume() throws InterruptedException {
        // Wait while empty — consumers cannot remove
        while (queue.isEmpty()) {
            System.out.println("Buffer empty, consumer waiting");
            wait(); // releases lock + suspends
        }
        T item = queue.poll();
        System.out.println("Consumed: " + item + " | Size: " + queue.size());
        notifyAll(); // wake up any waiting producers
        return item;
    }
}
```

**State transitions:**

```
PRODUCER:
  synchronized → check size == capacity?
    YES → wait() → [WAITING] → notified by consumer → [BLOCKED] → re-check → add → notifyAll
    NO  → add item → notifyAll → exit

CONSUMER:
  synchronized → check isEmpty()?
    YES → wait() → [WAITING] → notified by producer → [BLOCKED] → re-check → poll → notifyAll
    NO  → poll item → notifyAll → exit

NOTIFICATION FLOW:
  Producer adds item → notifyAll() → wakes WAITING consumers
  Consumer removes item → notifyAll() → wakes WAITING producers
```

**Why notifyAll() and not notify():** Producers and consumers wait on the same object. If a producer calls `notify()`, it might wake another producer (wrong!) instead of a consumer. `notifyAll()` wakes everyone; each checks their condition; the right thread proceeds.

---

### Q51. [MID] What is the difference between notify() and notifyAll()? When is notify() safe?

**Answer:**

| | `notify()` | `notifyAll()` |
|---|---|---|
| Threads woken | ONE arbitrary from wait set | ALL threads from wait set |
| JVM choice | Yes — no ordering, no fairness | N/A — wakes everyone |
| Performance | Slightly faster | Slightly slower (all compete) |
| Safety | Only in specific conditions | Always correct |

**notify() is safe ONLY when all three conditions hold:**
1. All waiting threads wait on the SAME condition
2. Satisfying the condition allows EXACTLY ONE thread to proceed
3. All waiting threads run IDENTICAL code

**The danger of notify() in producer-consumer:**

```
Scenario: 2 producers waiting (buffer full), 2 consumers waiting (buffer empty)
Consumer removes an item, calls notify()
→ JVM wakes one thread: it wakes a PRODUCER (wrong!)
Producer: checks — buffer still full → waits again
Consumer that should have run: still sleeping!
All threads eventually wait → DEADLOCK from missed signal
```

**notifyAll() solves this:**
```
Consumer removes item → notifyAll()
→ Wakes all 2 producers + 2 consumers
→ Each competes for lock
→ Producer checks: still full → waits again
→ Consumer checks: not empty → processes item
→ Correct thread proceeds
```

**Rule:** Default to `notifyAll()`. Use `notify()` only as a micro-optimization when you have mathematically proven all three safety conditions.

---

### Q52. [HARD] Trace the exact thread state transitions after notifyAll() is called on a wait set with 3 threads.

**Answer:**

```
Initial state:
  Thread A: WAITING (in lock's wait set)
  Thread B: WAITING (in lock's wait set)
  Thread C: WAITING (in lock's wait set)
  Thread D: currently holds the lock, running — calls notifyAll()

Step 1: notifyAll() called by Thread D
  Thread A: WAITING → BLOCKED (removed from wait set, added to lock's entry set)
  Thread B: WAITING → BLOCKED
  Thread C: WAITING → BLOCKED
  (All three now compete for the lock — none runs yet)

Step 2: Thread D exits synchronized block → releases lock
  Lock is released
  JVM picks ONE of A, B, C — say it picks Thread A

Step 3: Thread A wins the lock
  Thread A: BLOCKED → RUNNABLE
  Thread B: still BLOCKED
  Thread C: still BLOCKED

Step 4: Thread A re-checks its while condition
  Case 1: Condition is TRUE → Thread A proceeds, does work, releases lock
          Thread B wins → checks condition → may proceed or wait again
          Thread C wins → checks condition → may proceed or wait again

  Case 2: Condition is FALSE → Thread A calls wait() again
          Thread A: RUNNABLE → WAITING (releases lock)
          Thread B or C wins the lock → same process repeats

KEY POINT:
  WAITING → BLOCKED → RUNNABLE
  Never WAITING → RUNNABLE directly
  The lock re-acquisition (BLOCKED) is mandatory
```

---

---

# SECTION 9: WAIT() VS SLEEP()

---

### Q53. [EASY] What is the most important difference between wait() and sleep()?

**Answer:**

**`wait()` releases the monitor lock. `sleep()` does NOT.**

```java
// sleep() — locks retained:
synchronized (lock) {
    Thread.sleep(5000); // holds lock for 5 seconds!
                        // ALL other threads trying to enter this block
                        // are BLOCKED for 5 full seconds
}

// wait() — lock released:
synchronized (lock) {
    lock.wait(5000); // releases lock immediately!
                     // other threads CAN enter the synchronized block
                     // while this thread sleeps
}
```

**Real consequence:**
If you use `sleep()` inside `synchronized`, you accidentally serialize all other threads needing that lock for the entire sleep duration. This is a common accidental bottleneck in production systems.

---

### Q54. [MID] Where are sleep() and wait() defined? Why are they in different classes?

**Answer:**

```
Thread.sleep(millis) — defined in java.lang.Thread (static method)
  → Always sleeps the CURRENT thread, regardless of which Thread reference you use
  → Not related to any object or monitor

Object.wait()       — defined in java.lang.Object (instance method)
  → Called on the object whose monitor you hold
  → Releases THAT object's monitor while suspended
```

**Why in different classes:**

`sleep()` is a Thread operation — pause this thread for N milliseconds. It has nothing to do with objects or monitors.

`wait()` is a monitor operation — release this object's lock and wait for a signal on this object. It's on `Object` because every object has a monitor, and `wait()` operates on that monitor.

**The classic trap:**

```java
Thread t = new Thread(() -> doWork());
t.sleep(1000); // WRONG! Sleeps the CURRENT thread, not t!
               // This compiles but is almost never what you want

Thread.sleep(1000); // CORRECT — explicit about sleeping current thread
```

IntelliJ and static analysis tools warn about calling `sleep()` on a thread reference because it's almost always a mistake.

---

### Q55. [MID] Fill in the comparison table for wait() vs sleep().

**Answer:**

```
Property                │ Object.wait()                │ Thread.sleep()
────────────────────────┼──────────────────────────────┼────────────────────────
Defined in              │ Object                       │ Thread
Releases monitor lock?  │ YES                          │ NO
Requires synchronized?  │ YES (IllegalMonitorState     │ No restriction
                        │   exception if not)          │
How to wake early       │ notify() / notifyAll()       │ interrupt()
Thread state (no time)  │ WAITING                      │ N/A (must have time)
Thread state (with time)│ TIMED_WAITING                │ TIMED_WAITING
Use case                │ Inter-thread coordination    │ Simple time delay
Responds to interrupt   │ YES (throws InterruptedEx)   │ YES (throws InterruptedEx)
Typical usage           │ Producer-consumer, barriers  │ Retry delays, throttling
```

---

### Q56. [HARD][TRAP] What happens if you call Thread.sleep() on a thread reference (not Thread.sleep())?

**Answer:**

It **compiles** but sleeps the **CURRENT thread** — not the thread referenced by the variable. This is a trap because it looks like it should sleep the referenced thread.

```java
Thread t = new Thread(() -> {
    System.out.println("worker running: " + Thread.currentThread().getName());
});
t.start();

t.sleep(2000); // TRAP! Sleeps the CURRENT thread (e.g., main thread)
               // NOT t! t continues running normally!

// Equivalent to:
Thread.sleep(2000); // sleep main thread
```

`sleep()` is a static method that is always called in the context of `Thread.currentThread()`. The object reference (`t`) is completely ignored. The JVM finds the static method via `Thread.sleep()` — the `t` prefix is just resolved as the class name.

**Why this compiles:** Java allows calling static methods via an instance reference (though it's bad style and IntelliJ warns about it).

**Correct way to observe a thread sleeping:**

```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(5000); // t sleeps for 5 seconds
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});
t.start();
Thread.sleep(100); // let t enter sleep
System.out.println(t.getState()); // TIMED_WAITING
```

---

---

# SECTION 10: THREAD.JOIN()

---

### Q57. [EASY] What does Thread.join() do? Give a real use case.

**Answer:**

`t.join()` causes the **calling thread** to wait until thread `t` has terminated (reaches TERMINATED state). The calling thread blocks in WAITING state until `t` finishes.

```java
Thread t = new Thread(() -> {
    System.out.println("working...");
    // simulate work
    Thread.sleep(2000);
    System.out.println("done");
});
t.start();
t.join(); // main thread WAITS here until t terminates
System.out.println("t has finished — safe to proceed");
```

**Real use case — parallel computation with barrier:**

```java
int[] data = loadLargeArray();
int mid = data.length / 2;

Thread sorter1 = new Thread(() -> Arrays.sort(data, 0, mid));
Thread sorter2 = new Thread(() -> Arrays.sort(data, mid, data.length));

sorter1.start();
sorter2.start();  // both run in parallel

sorter1.join();   // wait for first half to finish
sorter2.join();   // wait for second half to finish

// Both halves are now sorted — safe to merge
merge(data, mid);
```

Without `join()`, the main thread might proceed to `merge()` before the sorters finish, producing garbage output.

---

### Q58. [MID] How is join() implemented internally?

**Answer:**

`join()` is implemented using `wait()` on the `Thread` object's own monitor:

```java
// Simplified from OpenJDK Thread.java:
public final synchronized void join() throws InterruptedException {
    while (isAlive()) {
        wait(0);  // wait on THIS thread object's monitor
    }
    // t is TERMINATED — isAlive() is false — exit loop
}
```

When a thread terminates, the JVM internally calls `notifyAll()` on that thread's `Thread` object. This wakes all threads blocked in `join()` on it.

```
State transitions for the CALLING thread:
  main calls t.join()
  └── main: RUNNABLE → WAITING (inside wait(0) on t's monitor)
  └── main stays WAITING until t terminates
  └── t terminates → JVM calls notifyAll() on t object
  └── main: WAITING → BLOCKED (competing for t's monitor)
  └── main: BLOCKED → RUNNABLE (re-acquires monitor)
  └── main: isAlive() returns false → exits loop → continues
```

---

### Q59. [MID] What thread state is the calling thread in during join()? Does it differ with a timeout?

**Answer:**

```
t.join()          → calling thread is in WAITING
t.join(millis)    → calling thread is in TIMED_WAITING
t.join(0)         → calling thread is in WAITING (0 means no timeout — wait forever)
```

```java
Thread t = new Thread(() -> Thread.sleep(10000));
t.start();
Thread.sleep(100);

// Check state of main thread while it's joining:
Thread mainThread = Thread.currentThread();

new Thread(() -> {
    Thread.sleep(200);
    System.out.println(mainThread.getState()); // WAITING (if join() no timeout)
                                                // TIMED_WAITING (if join(millis))
}).start();

t.join();      // WAITING
// or:
t.join(5000);  // TIMED_WAITING — give up after 5 seconds
if (t.isAlive()) {
    System.out.println("t didn't finish in 5 seconds");
    t.interrupt();
}
```

---

### Q60. [MID] What are modern alternatives to Thread.join() for waiting on multiple async tasks?

**Answer:**

`Thread.join()` is a low-level primitive. For coordinating multiple tasks, prefer:

```java
// Option 1: CompletableFuture.allOf (best for async tasks)
CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> fetchData1());
CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> fetchData2());
CompletableFuture<String> cf3 = CompletableFuture.supplyAsync(() -> fetchData3());

CompletableFuture.allOf(cf1, cf2, cf3).join(); // wait for all
String r1 = cf1.join(); // already done, no blocking
String r2 = cf2.join();
String r3 = cf3.join();

// Option 2: CountDownLatch (for N tasks, then proceed)
CountDownLatch latch = new CountDownLatch(3);
executor.submit(() -> { doWork1(); latch.countDown(); });
executor.submit(() -> { doWork2(); latch.countDown(); });
executor.submit(() -> { doWork3(); latch.countDown(); });
latch.await(); // wait for all 3 to count down

// Option 3: ExecutorService.invokeAll (submit all, get all results)
List<Callable<String>> tasks = List.of(
    () -> fetchData1(),
    () -> fetchData2(),
    () -> fetchData3()
);
List<Future<String>> futures = executor.invokeAll(tasks); // blocks until all done
for (Future<String> f : futures) {
    System.out.println(f.get());
}
```

---

---

# SECTION 11: RUNNABLE VS CALLABLE

---

### Q61. [EASY] What is the core difference between Runnable and Callable?

**Answer:**

```
Runnable:
  void run()          — returns nothing, cannot throw checked exceptions
  Use when: fire-and-forget tasks

Callable<V>:
  V call() throws Exception  — returns a value of type V, can throw any exception
  Use when: you need a result or want to propagate checked exceptions
```

```java
// Runnable — fire and forget:
Runnable r = () -> System.out.println("done");
executor.execute(r);    // returns void — no result
executor.submit(r);     // returns Future<?> — but get() returns null

// Callable — get a result:
Callable<Integer> c = () -> {
    return expensiveComputation(); // can throw IOException, SQLException, etc.
};
Future<Integer> future = executor.submit(c);
Integer result = future.get(); // blocks until result ready
```

---

### Q62. [MID] How do you use Callable with a bare Thread (without ExecutorService)?

**Answer:**

`Thread`'s constructor only accepts `Runnable`. To use a `Callable`, wrap it in `FutureTask<V>`, which implements both `Runnable` and `Future<V>`:

```java
Callable<String> callable = () -> {
    Thread.sleep(2000);
    return "result from background task";
};

FutureTask<String> futureTask = new FutureTask<>(callable);
// FutureTask implements Runnable → can be passed to Thread
Thread t = new Thread(futureTask, "callable-runner");
t.start();

System.out.println("doing other work...");

// FutureTask implements Future<V> → can get the result
String result = futureTask.get(); // blocks until t finishes
System.out.println(result); // "result from background task"
```

```
FutureTask<V>
     ├── implements Runnable  → Thread can execute it
     └── implements Future<V> → caller can get() the result
```

---

### Q63. [MID][CODE] How do you handle exceptions thrown inside a Callable?

**Answer:**

When a `Callable.call()` throws an exception, the `Future.get()` call on the submitting thread wraps it in `ExecutionException`. You unwrap it with `.getCause()`.

```java
Callable<Integer> riskyTask = () -> {
    if (Math.random() > 0.5) {
        throw new RuntimeException("random failure!");
    }
    return 42;
};

Future<Integer> future = executor.submit(riskyTask);

try {
    Integer result = future.get();
    System.out.println("Result: " + result);

} catch (ExecutionException e) {
    // The original exception is wrapped:
    Throwable original = e.getCause();
    System.out.println("Task failed with: " + original.getMessage());
    // Re-throw if appropriate:
    if (original instanceof RuntimeException) {
        throw (RuntimeException) original;
    }

} catch (InterruptedException e) {
    // The CALLING thread was interrupted while waiting for get()
    Thread.currentThread().interrupt(); // restore interrupt flag
    System.out.println("Wait was interrupted");
}
```

**Key points:**
- `ExecutionException.getCause()` gives you the original exception from `call()`
- `InterruptedException` from `get()` means YOUR thread was interrupted, not the worker
- Always restore the interrupt flag after catching `InterruptedException`

---

---

# SECTION 12: HOW TO STOP A THREAD

---

### Q64. [EASY] Why is Thread.stop() deprecated and dangerous?

**Answer:**

`Thread.stop()` throws a `ThreadDeath` error at an arbitrary point in the target thread's execution stack. This is dangerous because it can leave shared objects in an inconsistent state:

```java
synchronized (lock) {
    account.balance -= amount;     // Step 1: debit
    // Thread.stop() fires HERE!
    ledger.record(amount);         // Step 2: never executes
}
// lock IS released (ThreadDeath unwinds the synchronized block)
// But only Step 1 completed → balance decremented, ledger not updated
// → Object invariant violated → other threads see corrupt state
```

No amount of defensive coding can protect against this because:
- The stop can happen between ANY two instructions
- The lock IS released (so other threads immediately see the corrupt state)
- There's no "catch ThreadDeath and fix up state" that works reliably

`Thread.stop()` was deprecated in Java 1.2 (1998) and effectively removed in Java 17+.

---

### Q65. [MID][CODE] Show the correct way to stop a thread using a volatile flag.

**Answer:**

```java
class Worker implements Runnable {
    private volatile boolean running = true; // volatile for visibility

    @Override
    public void run() {
        while (running) {       // checked on every iteration
            doWork();
        }
        // Guaranteed to reach here — clean shutdown
        cleanup();
        System.out.println(Thread.currentThread().getName() + " stopped cleanly");
    }

    public void requestStop() {
        running = false; // volatile write — immediately visible to worker thread
    }
}

// Usage:
Worker worker = new Worker();
Thread t = new Thread(worker, "my-worker");
t.start();

Thread.sleep(5000); // let it work for 5 seconds
worker.requestStop(); // cooperative stop request
t.join();             // wait for clean exit
System.out.println("Worker finished");
```

**Limitation:** If `doWork()` is blocking (IO, sleep, wait on lock), the flag is not checked during the blocking call. The thread may not stop for the entire blocking duration.

---

### Q66. [MID][CODE] Show the correct way to stop a thread using Thread.interrupt().

**Answer:**

```java
class InterruptibleWorker implements Runnable {
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                doWork();
                // If doWork() calls sleep/wait, it will throw InterruptedException
                Thread.sleep(1000); // ← InterruptedException thrown here if interrupted
            }
        } catch (InterruptedException e) {
            // We were interrupted while sleeping/waiting
            // Restore the interrupt flag so callers can detect it
            Thread.currentThread().interrupt();
            System.out.println("Worker interrupted — shutting down cleanly");
        } finally {
            cleanup(); // always runs — clean shutdown
        }
    }
}

// Usage:
Thread t = new Thread(new InterruptibleWorker(), "interruptible-worker");
t.start();

Thread.sleep(3000);
t.interrupt(); // sets interrupt flag AND wakes t if it's in sleep/wait
t.join();
```

**Two things `interrupt()` does:**
1. Sets the thread's interrupt flag to `true`
2. If the thread is currently blocked in `sleep()`, `wait()`, `join()`, or any `InterruptibleChannel` operation, throws `InterruptedException` immediately

**The interrupted flag resets when InterruptedException is thrown.** Always restore it with `Thread.currentThread().interrupt()` in the catch block, otherwise callers in the call stack lose the information that an interrupt occurred.

---

### Q67. [HARD] Why must you call Thread.currentThread().interrupt() after catching InterruptedException?

**Answer:**

When `InterruptedException` is thrown, the **interrupt flag is automatically cleared**. If you swallow the exception without restoring the flag, the calling code loses the ability to detect that an interrupt occurred.

```java
// BAD — swallows the interrupt:
void doWork() {
    try {
        Thread.sleep(10000);
    } catch (InterruptedException e) {
        // Exception caught and ignored — interrupt flag is cleared!
        System.out.println("was interrupted"); // flag gone, nobody knows
    }
}

// The caller:
thread.interrupt();
// doWork() catches it and clears the flag
// If caller checks: Thread.currentThread().isInterrupted() → FALSE!
// Interrupt silently lost

// GOOD — restore the interrupt flag:
void doWork() throws InterruptedException {
    try {
        Thread.sleep(10000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore the flag!
        throw e; // re-throw so caller can handle it, OR
                 // just restore flag and return early if you handle it here
    }
}
```

**Rule:** When catching `InterruptedException`, you have two valid options:
1. Re-throw it (propagate): `throw e;`
2. Handle it (restore flag): `Thread.currentThread().interrupt();` and return/exit

Never silently swallow `InterruptedException` unless you genuinely intend to permanently ignore the interrupt (rare).

---

---

# SECTION 13: THREAD POOLS

---

### Q68. [MID] Why do thread pools exist? What overhead does creating a new thread have?

**Answer:**

Creating a new thread involves:

```
1. SYSTEM CALL to the OS kernel (pthread_create on Linux, CreateThread on Windows)
   Cost: ~10,000–50,000 nanoseconds (10–50 microseconds)

2. NATIVE STACK ALLOCATION
   Default: 512KB–1MB per thread
   1,000 threads → 512MB–1GB just for stacks

3. OS SCHEDULER REGISTRATION
   Thread added to the OS run queue

4. JVM BOOKKEEPING
   ThreadGroup registration, thread-local map creation, etc.
```

**At scale:** A web server handling 10,000 requests/second with one thread per request would create 10,000 threads per second. At 20μs per creation: 200ms of overhead per second just for thread creation — and 10,000 threads consuming 5–10GB of stack space.

**Thread pool solution:** Create N threads once at startup. Reuse them for all tasks. When a task arrives, hand it to an idle thread. When the task completes, the thread picks up the next task.

```
Without pool: request → create thread → run task → destroy thread
With pool:    request → pick idle thread → run task → return thread to pool

Eliminating thread creation on every request → much lower latency, lower memory
```

---

### Q69. [MID] Describe the four standard ExecutorService factory methods and when to use each.

**Answer:**

**1. newFixedThreadPool(n)**
```java
ExecutorService fixed = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors() // typically 8 or 16
);
// Always exactly N threads
// Tasks queue in LinkedBlockingQueue (UNBOUNDED — OOM risk!)
// Use for: CPU-bound work where you know the right parallelism
// Problem: unbounded queue → OOM under sustained overload
```

**2. newCachedThreadPool()**
```java
ExecutorService cached = Executors.newCachedThreadPool();
// Creates threads on demand, max = Integer.MAX_VALUE (effectively unlimited!)
// Idle threads die after 60 seconds
// Use for: short burst of async tasks in controlled environments
// DANGER IN PRODUCTION: 10,000 concurrent tasks = 10,000 threads = OOM
```

**3. newSingleThreadExecutor()**
```java
ExecutorService single = Executors.newSingleThreadExecutor();
// Exactly 1 thread, tasks execute SEQUENTIALLY
// If the thread dies unexpectedly, JVM creates a replacement automatically
// Use for: serializing access to a non-thread-safe resource (file writer, serial port)
```

**4. newScheduledThreadPool(n)**
```java
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
// N threads for delayed and periodic tasks
scheduled.schedule(() -> runReport(), 1, TimeUnit.HOURS);            // once after 1hr
scheduled.scheduleAtFixedRate(() -> heartbeat(), 0, 30, TimeUnit.SECONDS); // every 30s
scheduled.scheduleWithFixedDelay(() -> poll(), 0, 5, TimeUnit.SECONDS);    // 5s after each completion
// Use for: scheduled jobs, heartbeats, retry delays, polling
```

> Resource: [Executors API — Oracle Javadoc](https://docs.oracle.com/en/java/se/21/docs/api/java.base/java/util/concurrent/Executors.html)

---

### Q70. [HARD] Why is Executors.newCachedThreadPool() dangerous in production? What should you use instead?

**Answer:**

`newCachedThreadPool()` internally creates a `ThreadPoolExecutor` with:
- `corePoolSize = 0`
- `maximumPoolSize = Integer.MAX_VALUE` (2,147,483,647)
- `SynchronousQueue` (no buffer storage — every submitted task immediately needs a thread)

**The danger:**

```
Normal load:    100 tasks/s → 100 threads (idle threads reused from previous second)
Spike at 2 AM:  50,000 tasks/s → 50,000 NEW threads created immediately!

50,000 threads × 512KB stack = 25 GB stack space
+ heap exhaustion from all the Thread objects
= OutOfMemoryError → application crashes
```

**Production-safe alternative:** Always use a bounded `ThreadPoolExecutor`:

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4,                                    // corePoolSize — always live threads
    8,                                    // maximumPoolSize — hard cap
    60L, TimeUnit.SECONDS,               // idle thread timeout
    new ArrayBlockingQueue<>(200),        // BOUNDED queue — back-pressure
    new ThreadFactory() {
        AtomicInteger count = new AtomicInteger();
        public Thread newThread(Runnable r) {
            return new Thread(r, "api-worker-" + count.incrementAndGet());
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy() // rejection: slow down caller
);
```

With a bounded queue (200) and max 8 threads, at most 208 tasks can be "in flight." The 209th task either triggers CallerRunsPolicy (slows the caller, providing back-pressure) or throws `RejectedExecutionException`.

---

### Q71. [HARD] Explain the four rejection policies for ThreadPoolExecutor.

**Answer:**

When BOTH the thread pool is at `maximumPoolSize` AND the queue is full, the executor cannot accept new tasks. It applies a rejection policy:

```
1. AbortPolicy (DEFAULT)
   executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy())
   Action: throws RejectedExecutionException immediately
   When to use: caller should handle rejection (retry, log, fail-fast)

2. CallerRunsPolicy
   executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
   Action: the SUBMITTING thread runs the task itself (synchronously)
   Effect: natural back-pressure — submitter slows down, stops flooding the pool
   When to use: best for most production services (prevents data loss, provides back-pressure)
   Gotcha: if the caller is a Netty/Tomcat IO thread, it blocks IO acceptance temporarily

3. DiscardPolicy
   executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy())
   Action: silently drops the task — no exception, no logging
   When to use: RARELY — only if task loss is explicitly acceptable (metrics, non-critical)
   Danger: silent data loss with no indication

4. DiscardOldestPolicy
   executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy())
   Action: removes the oldest task from the queue, then retries submission
   When to use: when newer tasks are more important than older ones (real-time processing)
   Danger: oldest task silently lost

5. Custom (implement RejectedExecutionHandler)
   executor.setRejectedExecutionHandler((r, pool) -> {
       metrics.increment("tasks.rejected");
       log.warn("Task rejected: " + r);
       deadLetterQueue.offer(r); // put to retry queue
   });
   Action: whatever you implement
   When to use: when you need logging, metrics, fallback queue, alerting
```

---

### Q72. [MID] How do you properly size a thread pool for CPU-bound vs IO-bound tasks?

**Answer:**

**CPU-bound tasks (computation, parsing, crypto, sorting):**

```
Optimal threads ≈ number of CPU cores

Rationale: A CPU-bound thread is ALWAYS using the CPU when running.
           More threads than cores means:
             - Extra context switches (wasted overhead)
             - No additional throughput (CPU is already 100% utilized)

int cores = Runtime.getRuntime().availableProcessors();
// Use cores or cores+1 (the +1 handles occasional blocking/GC pauses)
ExecutorService cpuPool = Executors.newFixedThreadPool(cores);
```

**IO-bound tasks (HTTP calls, DB queries, file reads):**

```
Optimal threads ≈ cores × (1 + wait_time / compute_time)

Rationale: An IO-bound thread spends most of its time WAITING for IO,
           not using the CPU. While one thread waits, another can run.

Example:
  Thread spends 90% waiting for DB response, 10% computing
  wait/compute = 0.9/0.1 = 9
  cores = 8
  optimal = 8 × (1 + 9) = 80 threads

With 80 threads:
  8 threads run at any instant (all 8 cores busy)
  72 threads are waiting for IO
  CPU utilization ≈ 100%
```

**Practical advice:**
- Don't guess — measure with production-like load
- Start with a reasonable estimate (cores × 2 for mixed IO/CPU)
- Monitor: queue depth, active threads, task wait time
- Tune based on measurements, not theory

---

### Q73. [MID][CODE] How do you monitor a thread pool's health at runtime?

**Answer:**

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4, 8, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100),
    Executors.defaultThreadFactory(),
    new ThreadPoolExecutor.CallerRunsPolicy()
);

// Monitoring method:
void printPoolStatus(ThreadPoolExecutor executor) {
    System.out.println("=== Thread Pool Status ===");
    System.out.println("Pool size:         " + executor.getPoolSize());
    System.out.println("Core pool size:    " + executor.getCorePoolSize());
    System.out.println("Maximum pool size: " + executor.getMaximumPoolSize());
    System.out.println("Active threads:    " + executor.getActiveCount());
    System.out.println("Queue size:        " + executor.getQueue().size());
    System.out.println("Queue remaining:   " + executor.getQueue().remainingCapacity());
    System.out.println("Completed tasks:   " + executor.getCompletedTaskCount());
    System.out.println("Total tasks:       " + executor.getTaskCount());
    System.out.println("Is shutdown:       " + executor.isShutdown());
    System.out.println("Is terminated:     " + executor.isTerminated());
}

// Alert conditions to watch:
// Queue size approaching capacity → risk of rejection
// Active threads == pool size → pool is saturated
// CompletedTaskCount stagnating → potential deadlock or starvation
```

**In production:** Expose these via Micrometer metrics → Prometheus → Grafana:

```java
// With Micrometer:
new ExecutorServiceMetrics(executor, "payment-pool", Tags.empty())
    .bindTo(Metrics.globalRegistry);
// Now you get: pool_size, active_threads, queue_size, completed_tasks as metrics
```

---

---

# SECTION 14: THREADPOOLTASKEXECUTOR

---

### Q74. [HARD][DIAGRAM] Explain the exact task submission algorithm for ThreadPoolExecutor. What is the most counter-intuitive aspect?

**Answer:**

```
Task submitted to ThreadPoolExecutor:
                    │
                    ▼
        Active threads < corePoolSize?
           YES │                  NO │
               ▼                     ▼
        Create new thread       Queue not full?
        (even if idle cores         YES │           NO │
         are available)                 ▼              ▼
                                  Enqueue task   Active < maxPoolSize?
                                                    YES │           NO │
                                                        ▼              ▼
                                                Create temp thread  REJECT
                                                (non-core thread)   (apply
                                                                   rejection
                                                                    policy)
```

**The counter-intuitive aspect:** Tasks queue BEFORE additional threads are created beyond `corePoolSize`. Most developers assume: fill core → go to max → then queue. The actual order is: fill core → queue → then go to max when queue is full.

**Concrete example:**
```
Config: corePoolSize=4, maxPoolSize=8, ArrayBlockingQueue(100)

Task 1:  active=0 < core=4 → create thread 1, task runs
Task 2:  active=1 < core=4 → create thread 2, task runs
Task 3:  active=2 < core=4 → create thread 3, task runs
Task 4:  active=3 < core=4 → create thread 4, task runs
Task 5:  active=4 = core=4 → queue (size=1) ← QUEUED, not new thread!
...
Task 104: active=4, queue=100 (full)
Task 105: queue full, active=4 < max=8 → create thread 5 (non-core)
Task 106: queue full, active=5 < max=8 → create thread 6
Task 107: queue full, active=6 < max=8 → create thread 7
Task 108: queue full, active=7 < max=8 → create thread 8
Task 109: queue full, active=8 = max=8 → REJECTION POLICY
```

**Why is this designed this way?**
Queue first policy is intentional: it allows core threads to handle bursts by queuing, and only creates extra threads when the queue truly overflows. This prevents thread count from immediately jumping to `maxPoolSize` on any burst.

---

### Q75. [MID][CODE] Show the Spring ThreadPoolTaskExecutor configuration for a production service.

**Answer:**

```java
@Configuration
public class AsyncConfig {

    @Bean(name = "paymentExecutor")
    public ThreadPoolTaskExecutor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core threads — always alive, handle normal load
        executor.setCorePoolSize(4);

        // Max threads — created when queue fills up (non-core, temporary)
        executor.setMaxPoolSize(8);

        // Queue capacity — tasks wait here before new threads are created
        executor.setQueueCapacity(200);

        // Idle non-core threads die after 60 seconds
        executor.setKeepAliveSeconds(60);

        // Thread naming — critical for debugging
        executor.setThreadNamePrefix("payment-");

        // When pool is full and queue is full:
        // CallerRunsPolicy: caller runs the task → natural back-pressure
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for tasks to complete on shutdown (graceful shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}

// Usage in service:
@Service
public class PaymentService {
    @Autowired
    @Qualifier("paymentExecutor")
    private ThreadPoolTaskExecutor executor;

    public CompletableFuture<PaymentResult> processAsync(Payment payment) {
        return CompletableFuture.supplyAsync(
            () -> processPayment(payment),
            executor
        );
    }
}
```

---

---

# SECTION 15: JAVA.UTIL.CONCURRENT TOOLKIT

---

### Q76. [MID] What is the difference between ReentrantLock and synchronized? When would you choose ReentrantLock?

**Answer:**

```
Feature                     │ synchronized       │ ReentrantLock
────────────────────────────┼────────────────────┼───────────────────────
Implicit/Explicit           │ Implicit           │ Explicit
Must unlock in finally?     │ No (JVM handles)   │ YES (or deadlock risk)
Reentrant?                  │ Yes                │ Yes
Fairness option?            │ No                 │ new ReentrantLock(true)
tryLock() (non-blocking)?   │ No                 │ Yes
Timed acquisition?          │ No                 │ Yes
Interruptible wait?         │ No                 │ Yes (lockInterruptibly)
Multiple Conditions?        │ No (one wait set)  │ Yes (newCondition())
Lock state query methods?   │ No                 │ Yes (isLocked, etc.)
Performance (uncontended)   │ Faster (JIT opt)   │ Slightly slower
```

**Choose ReentrantLock when you need:**

```java
// 1. tryLock — avoid deadlock, implement backoff
if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
    try { doWork(); } finally { lock.unlock(); }
} else {
    // gracefully handle "couldn't acquire lock"
}

// 2. lockInterruptibly — cancellable operations
lock.lockInterruptibly(); // throws InterruptedException if interrupted
try { doWork(); } finally { lock.unlock(); }

// 3. Fairness — prevent starvation
ReentrantLock fairLock = new ReentrantLock(true); // FIFO waiting

// 4. Multiple conditions — precise notification
Condition notFull  = lock.newCondition();
Condition notEmpty = lock.newCondition();
// Wake ONLY producers: notFull.signal()
// Wake ONLY consumers: notEmpty.signal()
// Avoids the notifyAll() overhead of waking everyone

// 5. Lock state inspection
lock.getQueueLength()       // how many threads waiting
lock.getHoldCount()         // how many times this thread holds it
lock.isHeldByCurrentThread() // is current thread the holder
```

---

### Q77. [MID] What is a ReadWriteLock? When does it outperform a regular lock?

**Answer:**

`ReadWriteLock` allows **multiple concurrent readers** but **only one writer** (exclusive write). Multiple readers don't block each other; a writer blocks all readers and other writers.

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

private final Map<String, String> cache = new HashMap<>();

// Concurrent reads — many threads can read simultaneously
public String get(String key) {
    readLock.lock();
    try {
        return cache.get(key); // multiple threads can be here simultaneously
    } finally {
        readLock.unlock();
    }
}

// Exclusive write — all readers blocked during write
public void put(String key, String value) {
    writeLock.lock();
    try {
        cache.put(key, value); // only one thread at a time
    } finally {
        writeLock.unlock();
    }
}
```

```
Scenario A (regular lock):        Scenario B (ReadWriteLock):
10 readers + 1 writer             10 readers + 1 writer

All 11 must take turns:           10 readers run SIMULTANEOUSLY
  T1 reads: 5ms                   Write is exclusive:
  T2 reads: 5ms                     While writing, 0 readers
  ...                               While reading, 0 writers
  T11 writes: 2ms
Total: ~57ms                      Total: ~7ms (readers in parallel)
```

**When it helps:** Read-heavy workloads (cache, config, in-memory store) where reads dominate (e.g., 95% reads, 5% writes). If writes are frequent (50%+), the overhead of tracking read/write state may not be worth it.

---

### Q78. [HARD] What is Compare-And-Swap (CAS)? How does AtomicInteger use it?

**Answer:**

CAS is a **single CPU instruction** (e.g., `LOCK CMPXCHG` on x86) that atomically:
1. Reads a memory location
2. Compares it to an expected value
3. If they match: writes a new value and reports success
4. If they don't match: does nothing and reports failure

All three steps happen as one hardware-atomic operation — no other thread can interrupt between read and write.

```
CAS(memory_location, expected_value, new_value):
  If memory[location] == expected:
    memory[location] = new_value
    return SUCCESS
  Else:
    return FAILURE (memory unchanged)
```

**AtomicInteger.incrementAndGet() uses CAS in a loop:**

```java
// Conceptual implementation (actual is native):
public final int incrementAndGet() {
    while (true) {
        int current = get();           // read current value
        int next = current + 1;        // compute new value
        if (compareAndSet(current, next)) { // CAS: if value is still `current`, set to `next`
            return next;               // success
        }
        // else: another thread changed the value — retry with new current
    }
}

// Timeline example (two threads both increment from 5):
// Thread A: current=5, next=6, CAS(5→6): memory is still 5 → SUCCESS → return 6
// Thread B: current=5, next=6, CAS(5→6): memory is now 6 (A changed it) → FAIL → retry
// Thread B: current=6, next=7, CAS(6→7): memory is 6 → SUCCESS → return 7
// Final: 7 (correct! no lost increment)
```

**Why CAS is fast:** No OS locking, no thread parking, no context switch. Just CPU instructions. Under low contention: near-zero overhead. Under high contention: threads spin-retry but still make progress (lock-free).

**CAS limitations:**
- **ABA problem:** Thread reads A, another changes A→B→A, CAS succeeds (wrongly). Fixed with `AtomicStampedReference`.
- **High contention:** Many threads retrying → spinning burns CPU. Under extreme contention, use `LongAdder` instead.

---

### Q79. [MID] What is the ABA problem and how do you fix it?

**Answer:**

The ABA problem: Thread A reads value `A`. Another thread changes it `A→B→A`. Thread A's CAS finds value `A` and succeeds — but the value has been changed and changed back, which may be logically wrong.

```
Lock-free stack example:

Initial stack: A → B → C

Thread 1:
  Reads top = A
  Plans to CAS(top, A, B) — pop A, new top is B

Thread 2 (runs while Thread 1 is paused):
  Pops A from stack → stack is B → C
  Pops B from stack → stack is C
  Pushes A back    → stack is A → C (B is now freed/elsewhere)

Thread 1 resumes:
  CAS(top, A, B) — top IS A (Thread 2 put it back) → SUCCESS
  But B is now freed memory! Thread 1 sets top to B → use-after-free bug
```

**Fix: AtomicStampedReference — add a version counter**

```java
AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

// Thread 1: reads value AND stamp
int[] stamp = new int[1];
String value = ref.get(stamp); // value="A", stamp[0]=0

// Thread 2: changes A→B→A, incrementing stamp each time
ref.compareAndSet("A", "B", 0, 1); // stamp becomes 1
ref.compareAndSet("B", "A", 1, 2); // stamp becomes 2

// Thread 1: tries CAS with OLD stamp
ref.compareAndSet("A", "B", 0, 1); // FAILS — value is A but stamp is 2, not 0
// ABA problem prevented!
```

The version/stamp counter changes on every modification, making it impossible for the value to return to its original state AND stamp simultaneously.

---

### Q80. [MID] What is the difference between CountDownLatch and CyclicBarrier?

**Answer:**

```
CountDownLatch:
  - Count starts at N, counts DOWN to 0
  - N parties call countDown(), ONE (or more) parties call await()
  - ONE-TIME USE — cannot be reset after count reaches 0
  - Different threads can call countDown() and await()
  - Typical: N workers complete, main thread proceeds

CyclicBarrier:
  - N parties ALL call await(), ALL wait for each other
  - When all N arrive, the barrier releases EVERYONE simultaneously
  - REUSABLE — can be used for multiple phases
  - Optional action runs when all arrive (before release)
  - Typical: N phases, each phase all threads must complete before next starts
```

```java
// CountDownLatch — 3 workers, 1 main waiting:
CountDownLatch latch = new CountDownLatch(3);

executor.submit(() -> { doWork1(); latch.countDown(); });
executor.submit(() -> { doWork2(); latch.countDown(); });
executor.submit(() -> { doWork3(); latch.countDown(); });

latch.await(); // main waits here until all 3 count down
System.out.println("All workers done!");

// CyclicBarrier — 4 threads synchronize at each phase:
CyclicBarrier barrier = new CyclicBarrier(4, () ->
    System.out.println("All at barrier — next phase!")
);

for (int i = 0; i < 4; i++) {
    executor.submit(() -> {
        doPhase1();
        barrier.await(); // all 4 must arrive here before any proceed
        doPhase2();
        barrier.await(); // barrier is REUSED for phase 2
        doPhase3();
    });
}
```

---

### Q81. [MID][CODE] Show how to use Semaphore as a bounded resource pool (e.g., DB connection pool).

**Answer:**

```java
class ConnectionPool {
    private final Semaphore semaphore;
    private final Queue<Connection> connections;

    public ConnectionPool(int maxConnections) {
        semaphore = new Semaphore(maxConnections); // N permits = N connections
        connections = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < maxConnections; i++) {
            connections.add(createConnection());
        }
    }

    public Connection acquire() throws InterruptedException {
        semaphore.acquire(); // blocks if all connections in use
        // guaranteed: a connection is available after acquire()
        return connections.poll();
    }

    public Connection tryAcquire(long timeout, TimeUnit unit)
            throws InterruptedException {
        if (!semaphore.tryAcquire(timeout, unit)) {
            return null; // no connection available within timeout
        }
        return connections.poll();
    }

    public void release(Connection conn) {
        connections.offer(conn); // return connection to pool
        semaphore.release();     // release permit — wake next waiter
    }
}

// Usage:
ConnectionPool pool = new ConnectionPool(10); // max 10 concurrent connections

Connection conn = pool.acquire(); // blocks if all 10 in use
try {
    conn.execute("SELECT ...");
} finally {
    pool.release(conn); // ALWAYS release in finally
}
```

**What happens with the 11th caller:**
```
Semaphore(10): 10 permits available
Caller 1–10: each acquire() → permit count goes from 10 to 0
Caller 11: acquire() → count is 0 → BLOCKS
Caller 3: release() → count goes from 0 to 1
Caller 11: unblocked → acquire() → count goes from 1 to 0 → gets a connection
```

---

---

# SECTION 16: MUTEX

---

### Q82. [MID] What is a mutex? What are the three ways to implement one in Java?

**Answer:**

A mutex (mutual exclusion lock) allows only one thread to execute a critical section at a time. Any thread trying to acquire a mutex held by another thread is BLOCKED until the holder releases it.

**Java has no class literally named Mutex.** Three equivalent mechanisms:

```java
// Way 1: synchronized (implicit mutex — simplest)
private final Object lock = new Object();
synchronized (lock) {
    // critical section — one thread at a time
}

// Way 2: ReentrantLock (explicit mutex — more features)
private final ReentrantLock mutex = new ReentrantLock();
mutex.lock();
try {
    // critical section
} finally {
    mutex.unlock(); // MUST be in finally
}

// Way 3: Semaphore(1) (binary semaphore — non-reentrant)
private final Semaphore semaphore = new Semaphore(1);
semaphore.acquire();
try {
    // critical section
} finally {
    semaphore.release(); // MUST be in finally
}
```

**Key difference:** `Semaphore(1)` is NOT reentrant — if the same thread tries to acquire it twice, it deadlocks with itself. `synchronized` and `ReentrantLock` are both reentrant.

---

### Q83. [HARD] What is a fair mutex? What is the trade-off?

**Answer:**

A **fair mutex** serves waiting threads in FIFO order — the thread that has been waiting longest is guaranteed to be next. An **unfair mutex** (default) allows any waiting thread to "barge in" — it's non-deterministic.

```java
ReentrantLock unfairLock = new ReentrantLock();       // unfair (default)
ReentrantLock fairLock   = new ReentrantLock(true);   // fair
```

**Why unfair is faster:**

When a lock is released, the current thread may immediately try to re-acquire it (barge in) without going through the wait queue. This avoids the overhead of:
1. Parking the new thread
2. Unparking the waiting thread
3. Context switching to the waiting thread

The "barging" thread gets the lock immediately at CPU speed.

**Why fair is sometimes necessary:**

Without fairness, a very active thread can hold the lock in a tight loop:
```
T1 acquires lock → releases → immediately re-acquires → ...
T2, T3, T4 wait indefinitely → STARVATION
```

With fairness (`true`), T1 must go to the back of the queue after releasing, giving T2, T3, T4 a turn.

**The benchmark reality:**
Fair `ReentrantLock` is typically 2–10x SLOWER than unfair in throughput benchmarks. The FIFO queue maintenance and forced context switches add significant overhead.

**When to use fair:** Only when you observe actual starvation in production metrics (specific threads not getting progress). Don't use it by default.

---

### Q84. [HARD][CODE] Implement the producer-consumer pattern using ReentrantLock with two Conditions. Why is this better than synchronized + notifyAll()?

**Answer:**

```java
public class TwoConditionBuffer<T> {
    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition     notFull  = lock.newCondition(); // producers wait here
    private final Condition     notEmpty = lock.newCondition(); // consumers wait here
    private final Object[]      items;
    private int head, tail, count;

    public TwoConditionBuffer(int capacity) {
        items = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();  // producer waits on notFull condition ONLY
            items[tail] = item;
            tail = (tail + 1) % items.length;
            count++;
            notEmpty.signal();    // wake ONE consumer — not all threads!
        } finally { lock.unlock(); }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await(); // consumer waits on notEmpty condition ONLY
            T item = (T) items[head];
            head = (head + 1) % items.length;
            count--;
            notFull.signal();     // wake ONE producer — not all threads!
            return item;
        } finally { lock.unlock(); }
    }
}
```

**Why this is better than `synchronized + notifyAll()`:**

```
With synchronized + notifyAll():
  Producer adds item → notifyAll()
  → Wakes BOTH sleeping producers AND sleeping consumers
  → Producers: check buffer, still full, go back to sleep (wasted wakeup)
  → Consumers: one proceeds
  If 10 producers and 10 consumers waiting: 19 unnecessary wakeups per operation

With ReentrantLock + 2 Conditions:
  Producer adds item → notEmpty.signal()
  → Wakes EXACTLY ONE sleeping consumer
  → Zero unnecessary wakeups
  If 10 producers and 10 consumers waiting: 1 precise wakeup per operation

In high-throughput scenarios: 19x fewer context switches = significant throughput improvement
```

> This is exactly how `java.util.concurrent.ArrayBlockingQueue` is implemented internally.

---

---

# SECTION 17: FUTURE AND COMPLETABLEFUTURE

---

### Q85. [MID] What are the limitations of Future<V>?

**Answer:**

`Future<V>` has five significant limitations that `CompletableFuture` was designed to overcome:

```
1. get() is BLOCKING
   Integer result = future.get(); // your thread blocks here
   No way to say "when done, then do X" without blocking

2. No CHAINING
   Cannot chain: future.thenTransform(...).thenSave(...)
   Must call get() and do each step manually with blocking

3. No COMBINING
   Want to wait for futures A and B and combine results?
   Must call a.get() (blocking), then b.get() (blocking)
   No parallel wait + combine

4. No MANUAL COMPLETION
   Cannot complete a Future from outside the task
   What if you want to complete it from a callback?

5. No ERROR RECOVERY
   If the task fails, you get ExecutionException from get()
   No "if failed, use this fallback" without blocking
```

**How CompletableFuture solves each:**

```java
CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> fetchData());

// 1. Non-blocking callback:
cf.thenAccept(result -> saveToDb(result)); // no blocking

// 2. Chaining:
cf.thenApply(r -> r * 2)
  .thenApply(r -> r + 1)
  .thenAccept(System.out::println);

// 3. Combining two parallel futures:
cf.thenCombine(anotherCf, (r1, r2) -> r1 + r2);

// 4. Manual completion:
CompletableFuture<String> manual = new CompletableFuture<>();
manual.complete("done externally");

// 5. Error recovery:
cf.exceptionally(ex -> -1); // fallback value on failure
```

---

### Q86. [HARD][TRAP] What is the difference between thenApply() and thenCompose()?

**Answer:**

This is the Java equivalent of `map()` vs `flatMap()` in streams.

**thenApply(fn):** `fn` takes the result and returns a **plain value** of type `B`.
Result: `CompletableFuture<B>`

**thenCompose(fn):** `fn` takes the result and returns a **CompletableFuture\<B\>** (another async operation).
Result: `CompletableFuture<B>` (FLAT — not nested)

```java
// thenApply — sync transform:
CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> 42)
    .thenApply(n -> "Result: " + n);  // fn returns String → CF<String>

// thenCompose — async next step:
CompletableFuture<User> cf2 = CompletableFuture.supplyAsync(() -> userId)
    .thenCompose(id -> fetchUser(id));  // fn returns CF<User> → CF<User> (flat)
//                        ^ async DB call

// THE TRAP — using thenApply when next step is async:
CompletableFuture<CompletableFuture<User>> WRONG =
    CompletableFuture.supplyAsync(() -> userId)
        .thenApply(id -> fetchUser(id));  // fn returns CF<User>
        // thenApply wraps it → CF<CF<User>> — NESTED, hard to work with!

// FIX: use thenCompose for async next steps
CompletableFuture<User> CORRECT =
    CompletableFuture.supplyAsync(() -> userId)
        .thenCompose(id -> fetchUser(id)); // FLAT → CF<User>
```

**Mnemonic:**
- `thenApply` = map (sync transform)
- `thenCompose` = flatMap (async transform)

---

### Q87. [MID][CODE] Show how to run two async tasks in parallel and combine their results with CompletableFuture.

**Answer:**

```java
// Two independent async tasks:
CompletableFuture<User>  userFuture  = CompletableFuture.supplyAsync(
    () -> userService.fetchUser(userId), executor
);
CompletableFuture<Order> orderFuture = CompletableFuture.supplyAsync(
    () -> orderService.fetchLatestOrder(userId), executor
);

// thenCombine: runs both in parallel, combines when BOTH complete:
CompletableFuture<Dashboard> dashboard = userFuture.thenCombine(
    orderFuture,
    (user, order) -> new Dashboard(user, order)  // combining function
);

// Get the result:
Dashboard result = dashboard.get(); // or dashboard.join()
```

**Timing comparison:**

```
SEQUENTIAL (no CF):            PARALLEL (CF.thenCombine):
fetch user:   200ms            fetch user:   ─────── 200ms ──────►
fetch order:  300ms            fetch order:  ───────────── 300ms ──►
total:        500ms            combine:                            ─ 1ms ─►
                               total:        ≈ 300ms  (not 500ms!)
```

**For more than two futures:**

```java
CompletableFuture<String> cf1 = supplyAsync(() -> fetchA(), executor);
CompletableFuture<String> cf2 = supplyAsync(() -> fetchB(), executor);
CompletableFuture<String> cf3 = supplyAsync(() -> fetchC(), executor);

CompletableFuture<Void> all = CompletableFuture.allOf(cf1, cf2, cf3);
all.join(); // wait for all three

String a = cf1.join(); // join() won't block here — already done
String b = cf2.join();
String c = cf3.join();
String combined = a + b + c;
```

---

### Q88. [MID][CODE] Show complete error handling in a CompletableFuture chain.

**Answer:**

```java
CompletableFuture.supplyAsync(() -> fetchData(), executor)

    // Transform the result:
    .thenApply(data -> transform(data))

    // Recover from any exception — return fallback:
    .exceptionally(ex -> {
        log.error("Pipeline failed: {}", ex.getMessage(), ex);
        return "DEFAULT_VALUE"; // fallback value of same type
    })

    // Handle BOTH success and failure:
    .handle((result, ex) -> {
        if (ex != null) {
            metrics.increment("errors");
            return "HANDLED_DEFAULT";
        }
        metrics.increment("success");
        return result.toUpperCase();
    })

    // Side effect on completion (doesn't transform — used for logging, metrics):
    .whenComplete((result, ex) -> {
        if (ex != null) alertOncall(ex);
        log.info("Pipeline completed: result={}", result);
    })

    // Terminal — consume the result:
    .thenAccept(result -> saveToDb(result));
```

**Error propagation:**

```
Stage 1 (fetchData): THROWS IOException
      │
      │ Exception propagates down the chain
      ▼
Stage 2 (transform): SKIPPED (previous stage failed)
      │
      ▼
Stage 3 (exceptionally): CATCHES the exception → returns "DEFAULT_VALUE"
      │
      │ Now the chain has a SUCCESSFUL value: "DEFAULT_VALUE"
      ▼
Stage 4 (handle): receives result="DEFAULT_VALUE", ex=null
      ▼
Stage 5 (whenComplete): result="DEFAULT_VALUE", ex=null
      ▼
Stage 6 (thenAccept): saves "DEFAULT_VALUE" to DB
```

---

---

# SECTION 18: THREADLOCAL

---

### Q89. [MID] How does ThreadLocal work internally?

**Answer:**

Each `Thread` object has a private field `threadLocals` of type `ThreadLocal.ThreadLocalMap`. This is a specialized hash map where keys are `ThreadLocal` instances (stored as weak references) and values are the per-thread data.

```
ThreadLocal<User> USER = new ThreadLocal<>();

Thread-1 object:                    Thread-2 object:
  threadLocals: ThreadLocalMap         threadLocals: ThreadLocalMap
    KEY             VALUE                KEY             VALUE
    USER instance → User("Alice")        USER instance → User("Bob")
```

**When Thread-1 calls `USER.get()`:**
1. Get `Thread.currentThread()` → Thread-1
2. Get Thread-1's `threadLocals` map
3. Look up `USER` (this ThreadLocal instance) as the key
4. Return `User("Alice")`

**When Thread-2 calls `USER.get()`:**
1. Get `Thread.currentThread()` → Thread-2
2. Get Thread-2's `threadLocals` map  
3. Look up `USER`
4. Return `User("Bob")`

The two threads read the same `ThreadLocal` variable but get different values because the map lives on the Thread object, not on the ThreadLocal object.

**No synchronization needed** — each thread only ever reads/writes its own `threadLocals` map.

---

### Q90. [HARD] How does ThreadLocal cause memory leaks in thread pools? How do you prevent it?

**Answer:**

**Thread pool threads are reused** across many tasks. ThreadLocal values set for one task persist on the thread for the next task.

```
Thread-5 in pool handles request-1:
  userContext.set(User("Alice"))   // stored in Thread-5's threadLocalMap
  // request-1 finishes
  // NO remove() called — Alice stays in Thread-5's map!

Thread-5 handles request-2:
  userContext.get() → User("Alice")! // WRONG — should be null or Bob
  User("Alice") object → LEAKED in memory as long as Thread-5 lives
                          (pool threads live for pool's lifetime = hours/days)
```

```
Memory leak diagram:
Thread-5 (lives for hours)
  threadLocalMap:
    key: userContext (weak ref) → null (if userContext is GC'd)
    value: User("Alice") ← STRONG reference — NEVER GC'd!
           └── User has reference to UserProfile (100KB)
               └── UserProfile has reference to Avatar (5MB)
               TOTAL: 5MB+ leaked per request!
```

**The weak key trap:** The key is a weak reference to the `ThreadLocal` instance. If the `ThreadLocal` variable is GC'd, the key becomes null. But the VALUE is a strong reference — it's never GC'd. You have a "dangling" entry in the map: null key, strong value = memory leak.

**Prevention: Always call remove() in a finally block:**

```java
// In a servlet filter, Spring interceptor, or task wrapper:
@Override
public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) {
    try {
        userContext.set(authenticate(req));  // set at request start
        chain.doFilter(req, res);           // process request
    } finally {
        userContext.remove();               // ALWAYS clean up in finally
    }
}
```

**Additional option: Use `try-with-resources` pattern:**

```java
class ThreadLocalScope implements AutoCloseable {
    ThreadLocalScope(User user) { userContext.set(user); }
    public void close() { userContext.remove(); } // called by try-with-resources
}

try (ThreadLocalScope scope = new ThreadLocalScope(user)) {
    processRequest(); // userContext automatically cleaned up after this block
}
```

---

### Q91. [MID] What is InheritableThreadLocal? What are its limitations with thread pools?

**Answer:**

`InheritableThreadLocal<T>` copies the parent thread's value to a child thread at the time of `new Thread()`:

```java
InheritableThreadLocal<String> traceId = new InheritableThreadLocal<>();
traceId.set("trace-abc-123");

// Value is copied to child at thread creation time:
Thread child = new Thread(() -> {
    System.out.println(traceId.get()); // prints "trace-abc-123" — inherited!
});
child.start();
```

**Limitation with thread pools:**

Thread pool threads are created ONCE at pool startup. `InheritableThreadLocal` copies happen at thread creation — not at task submission.

```
Pool created at app startup by thread "main":
  Thread-1 created by main: inherits main's traceId (null at startup)
  Thread-2 created by main: inherits main's traceId (null at startup)

Request 1 arrives (traceId="request-1"):
  Submits task to pool → Thread-1 picks it up
  Thread-1.traceId = null (inherited at creation, not at submission!)
  NOT "request-1" as you'd expect
```

**Solutions:**
1. **Spring's `TaskDecorator`:** Wraps each task submission to copy context:

```java
executor.setTaskDecorator(runnable -> {
    String currentTraceId = traceId.get(); // capture at submission time
    return () -> {
        traceId.set(currentTraceId);       // set at execution time
        try { runnable.run(); }
        finally { traceId.remove(); }
    };
});
```

2. **Alibaba `TransmittableThreadLocal` (TTL):** Drop-in replacement that handles thread pool propagation automatically.

---

---

# SECTION 19: JAVA MEMORY MODEL

---

### Q92. [HARD] What is the Java Memory Model (JMM)? Why does it exist?

**Answer:**

The JMM is a specification (defined in the Java Language Specification, Chapter 17) that defines:
1. **When** writes to shared variables become visible to other threads
2. **What** orderings of memory operations are allowed

**Why it's needed:**

Without the JMM, compilers and CPUs would be free to:
- Reorder instructions for performance
- Cache values in registers instead of writing to RAM
- Serve reads from CPU cache instead of main memory

These optimizations are correct for single-threaded code but break multi-threaded programs:

```java
// Thread 1:
value = 42;
initialized = true;

// Thread 2:
if (initialized) {
    use(value);  // Without JMM guarantees: might see initialized=true but value=0!
}
```

The JMM provides rules under which programmers can reason about visibility and ordering. `volatile`, `synchronized`, and `java.util.concurrent` classes provide JMM-defined visibility and ordering guarantees.

> Resource: [Java Memory Model — Heinz Kabutz (Java Specialists Newsletter)](https://www.javaspecialists.eu/archive/Issue256-Java-Memory-Model.html)

---

### Q93. [HARD] What is the happens-before relationship? List all ways it can be established.

**Answer:**

If action A **happens-before** action B, then the effects of A (all memory writes) are guaranteed to be visible to B. It's the JMM's formal definition of visibility.

**Six ways happens-before is established:**

```
1. PROGRAM ORDER
   Within a single thread, each action happens-before the next.
   x = 1;          ← happens-before
   y = x + 1;      ← guaranteed to see x=1

2. MONITOR LOCK/UNLOCK
   Releasing a monitor happens-before acquiring the same monitor.
   synchronized(lock) { x = 1; }    ← release happens-before
   synchronized(lock) { use(x); }   ← acquire → guaranteed to see x=1

3. VOLATILE WRITE/READ
   Writing a volatile variable happens-before reading it.
   volatile_var = 5;                 ← write happens-before
   int v = volatile_var;             ← read → guaranteed to see 5

4. THREAD START
   Thread.start() happens-before any action in the started thread.
   x = 1;
   thread.start();                   ← start happens-before
   // Inside thread: use(x) → guaranteed to see x=1

5. THREAD JOIN
   All actions in a thread happen-before the return from join() on it.
   // Inside thread: x = 42;
   thread.join();                    ← join happens-before
   use(x);                           ← guaranteed to see x=42

6. TRANSITIVITY
   If A hb B and B hb C, then A hb C.
   x = 1;           → hb → synchronized release → hb → synchronized acquire
   Conclusion: the synchronized acquire sees x=1
```

---

### Q94. [HARD] What is instruction reordering? Give an example where it breaks multi-threaded code.

**Answer:**

**Instruction reordering:** The JIT compiler and CPU are allowed to reorder instructions to optimize performance, as long as the observable result in a **single thread** doesn't change. Multi-threaded code can break because reordering visible to one thread is correct for that thread alone, but not from another thread's perspective.

```java
class SomeObject {
    int value = 0;
    boolean initialized = false;

    void init() {
        value = 42;          // step A
        initialized = true;   // step B — JVM/CPU might reorder: B before A!
    }
}

// Thread 1 calls init()
// Thread 2 checks:
if (obj.initialized) {
    System.out.println(obj.value); // might print 0! If B was reordered before A,
                                   // Thread 2 sees initialized=true but value=0
}
```

```
INTENDED order:      REORDERED (allowed by JMM without volatile/synchronized):
value = 42;   (A)   initialized = true; (B) ← moved first
initialized = true; (B)   value = 42;   (A)

Thread 2 might interleave AFTER B but BEFORE A:
  sees initialized = true
  sees value = 0
  → incorrect behavior
```

**Fix with volatile:**

```java
volatile boolean initialized = false;

void init() {
    value = 42;
    initialized = true; // volatile write creates a StoreStore barrier
    // ALL writes before this (value=42) are committed before this write
}
// Thread 2 reading initialized=true is guaranteed to also see value=42
```

---

### Q95. [MID] What is safe publication? What are four ways to safely publish an object?

**Answer:**

An object is **safely published** if it is made visible to other threads in a way that guarantees the object's full state (all fields) is visible — not just the reference.

**Unsafe publication — partially-constructed object:**

```java
// Thread 1 (constructor not finished):
obj = new MyObject(); // Three steps: allocate, initialize, assign reference
                      // JVM can reorder: assign reference BEFORE initialize!

// Thread 2 might see obj != null but MyObject is half-initialized!
if (obj != null) {
    obj.useFields(); // fields might be at default values → crash
}
```

**Four safe publication mechanisms:**

```java
// 1. Static initializer (class loading guarantee — safest)
public class Container {
    private static final MyObject obj = new MyObject(); // safe publication
    // JVM guarantees class loading is fully complete before any thread uses it
}

// 2. volatile field
private volatile MyObject obj;
obj = new MyObject(); // volatile write = StoreStore barrier
                      // all constructor writes committed before reference visible

// 3. synchronized
private MyObject obj;
synchronized void set(MyObject o) { obj = o; }
synchronized MyObject get() { return obj; }
// Lock/unlock provides happens-before — reader sees fully initialized object

// 4. final fields (strongest guarantee for immutable objects)
class Immutable {
    private final int x;
    private final String name;
    public Immutable(int x, String name) {
        this.x = x; this.name = name;
    }
    // JMM guarantees: any thread that obtains a reference to an Immutable
    // object sees the final fields at their correct initialized values
    // EVEN WITHOUT synchronization
}
```

> Resource: [Java Concurrency in Practice — Chapter 3.5 (Goetz)](https://jcip.net/)

---

---

# SECTION 20: COMMON CONCURRENCY BUGS

---

### Q96. [MID] What is a race condition? What is a data race? Are they the same thing?

**Answer:**

**Race condition:** The program's behavior depends on the relative timing/interleaving of operations from multiple threads, and some interleavings produce incorrect results. A logical bug.

**Data race:** Two threads access the same memory location concurrently, at least one is a write, and there is no synchronization between them. A memory-level violation of the JMM.

**They're related but not the same:**

```java
// Race condition (logical), but technically no data race:
// (using synchronized but still logically racy)
synchronized void transfer(Account from, Account to, double amount) {
    if (from.balance >= amount) {        // check
        Thread.sleep(1); // gap — another thread modifies from.balance here!
        from.balance -= amount;           // act → TOCTOU race condition
        to.balance += amount;
    }
}

// Data race (JMM violation):
class Counter {
    private int count = 0;            // not volatile, not synchronized
    void increment() { count++; }     // concurrent access without sync = DATA RACE
}
```

**A data race is always unsafe.** The JMM makes no guarantees about programs with data races — they can exhibit any behavior including infinite loops, garbage values, or appearing to work correctly.

A race condition can exist even with properly synchronized code if the high-level logic is incorrect (check-then-act, TOCTOU).

---

### Q97. [MID] What is a livelock? How is it different from deadlock?

**Answer:**

**Deadlock:** Two threads are permanently BLOCKED — each holds a lock the other needs. They are completely stuck. No progress.

**Livelock:** Two threads are actively running (not blocked), but they keep reacting to each other in a way that prevents either from making forward progress.

```java
// Livelock example — two threads trying to be polite:
class Polite {
    volatile boolean wantToPass = true;

    void tryToPass(Polite other) {
        while (wantToPass) {
            if (other.wantToPass) {
                // "You go first" — step aside
                wantToPass = false;
                Thread.sleep(1);
                wantToPass = true;  // then try again
            }
        }
        // Both threads keep stepping aside for each other
        // NEITHER ever passes — livelock
    }
}
```

**Analogy:** Two people walking toward each other in a hallway, each stepping to the same side to let the other pass, repeatedly.

```
Deadlock:                    Livelock:
T1: ■ (blocked)             T1: runs → yields → runs → yields → runs...
T2: ■ (blocked)             T2: runs → yields → runs → yields → runs...
No CPU used                 CPU used but no progress
```

**Prevention of livelock:** Introduce randomness or a priority scheme so one thread "wins" in case of conflict. Add a random backoff: `Thread.sleep(random.nextInt(100))` so retries don't synchronize.

---

### Q98. [HARD] Explain the check-then-act (TOCTOU) race condition. Give an example and fix it.

**Answer:**

**TOCTOU (Time Of Check To Time Of Use):** Thread A checks a condition (check), another thread changes state, then Thread A acts based on the now-stale condition (use). The check is no longer valid by the time the action executes.

```java
// BROKEN — classic TOCTOU race:
Map<String, Connection> connections = new ConcurrentHashMap<>();

void ensureConnection(String key) {
    if (!connections.containsKey(key)) {    // CHECK: key not present
        // Another thread creates the connection HERE!
        connections.put(key, createConnection(key));  // ACT: duplicate creation!
    }
    // Two connections created for the same key — connection leak
}
```

```
Thread A checks: containsKey("db") = false
                        ← Thread B runs, creates connection for "db"
Thread A acts:   put("db", createConnection()) → creates SECOND connection!
```

**Three fix approaches:**

```java
// Fix 1: computeIfAbsent — atomic check-then-put
connections.computeIfAbsent(key, k -> createConnection(k));

// Fix 2: synchronized block — atomic check-then-act
synchronized (connections) {
    if (!connections.containsKey(key)) {
        connections.put(key, createConnection(key));
    }
}

// Fix 3: putIfAbsent — atomic but may create extra connections
Connection conn = createConnection(key); // may create extra
Connection existing = connections.putIfAbsent(key, conn);
if (existing != null) {
    conn.close(); // discard if another thread won the race
}
// Note: createConnection() is called even if not needed — use computeIfAbsent instead
```

**General rule:** Any compound operation (check + act, read + modify + write) on shared state must be made atomic.

---

### Q99. [MID] What is thread confinement? What are three forms of it?

**Answer:**

Thread confinement is the principle: **if an object is only accessible to one thread, no synchronization is needed.** The simplest way to write thread-safe code is to avoid sharing.

**Form 1: Stack confinement (strongest)**

```java
void compute() {
    List<Integer> results = new ArrayList<>(); // on THIS thread's stack (effectively)
    for (int i = 0; i < 100; i++) {
        results.add(expensiveCompute(i)); // never shared — completely safe
    }
    return results; // once returned and stored in a shared field, it escapes
}
```

**Form 2: ThreadLocal (explicit per-thread storage)**

```java
private static final ThreadLocal<SimpleDateFormat> formatter =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// Each thread gets its own formatter instance — no synchronization needed
public String format(Date date) {
    return formatter.get().format(date);
}
```

**Form 3: Ad-hoc confinement (informal, fragile)**

```java
// By convention: only the "processing" thread accesses this list
// No enforcement mechanism — relies on developer discipline
private List<Order> pendingOrders; // "owned" by orderProcessorThread by convention
```

**Best practice:** Prefer stack confinement (use local variables) and ThreadLocal over ad-hoc. Document ownership clearly when using ad-hoc.

---

---

# SECTION 21: MIXED AND SYSTEM DESIGN

---

### Q100. [HARD] Design a thread-safe rate limiter in Java using a Semaphore.

**Answer:**

```java
/**
 * Token bucket rate limiter: allows N requests per second.
 * Permits are refilled on a background scheduled thread.
 */
public class RateLimiter {
    private final Semaphore semaphore;
    private final int maxPermits;
    private final ScheduledExecutorService refiller;

    public RateLimiter(int requestsPerSecond) {
        this.maxPermits = requestsPerSecond;
        this.semaphore = new Semaphore(requestsPerSecond);
        this.refiller = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-refiller");
            t.setDaemon(true);
            return t;
        });

        // Refill permits every second
        refiller.scheduleAtFixedRate(this::refillPermits, 1, 1, TimeUnit.SECONDS);
    }

    private void refillPermits() {
        int permitsToAdd = maxPermits - semaphore.availablePermits();
        if (permitsToAdd > 0) {
            semaphore.release(permitsToAdd);
        }
    }

    // Blocking acquire — waits until a permit is available
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    // Non-blocking — returns false if rate limit exceeded
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    // Blocking with timeout
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return semaphore.tryAcquire(timeout, unit);
    }

    public void shutdown() {
        refiller.shutdown();
    }
}

// Usage:
RateLimiter limiter = new RateLimiter(100); // 100 req/sec

// In API handler:
if (limiter.tryAcquire()) {
    processRequest(); // allowed
} else {
    throw new TooManyRequestsException("Rate limit exceeded");
}
```

---

### Q101. [HARD] Design a thread-safe in-memory cache with TTL using ConcurrentHashMap and ScheduledExecutorService.

**Answer:**

```java
public class TTLCache<K, V> {
    private static class CacheEntry<V> {
        final V value;
        final long expiresAt; // epoch milliseconds

        CacheEntry(V value, long ttlMillis) {
            this.value = value;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final ConcurrentHashMap<K, CacheEntry<V>> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner;

    public TTLCache(long cleanupIntervalMs) {
        cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(
            this::evictExpiredEntries,
            cleanupIntervalMs,
            cleanupIntervalMs,
            TimeUnit.MILLISECONDS
        );
    }

    public void put(K key, V value, long ttlMillis) {
        store.put(key, new CacheEntry<>(value, ttlMillis));
    }

    public V get(K key) {
        CacheEntry<V> entry = store.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            store.remove(key, entry); // atomic remove if entry hasn't changed
            return null;
        }
        return entry.value;
    }

    public void invalidate(K key) {
        store.remove(key);
    }

    private void evictExpiredEntries() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}

// Usage:
TTLCache<String, User> cache = new TTLCache<>(60_000); // cleanup every 60s
cache.put("user:123", user, 300_000); // TTL: 5 minutes
User u = cache.get("user:123");       // null if expired
```

---

### Q102. [HARD] Explain the difference between LongAdder and AtomicLong. When should you use each?

**Answer:**

**AtomicLong:** Single variable with CAS for atomic updates. Under high contention (many threads incrementing), threads spin-retry the CAS operation — burning CPU with contention.

**LongAdder:** Splits the counter across multiple cells (one per CPU core under contention). Threads increment their own cell, avoiding CAS contention. Final value = sum of all cells.

```
AtomicLong under high contention:

Core 1 → CAS(0→1): SUCCESS
Core 2 → CAS(1→2): FAIL, retry: CAS(1→2): SUCCESS
Core 3 → CAS(2→3): FAIL, retry: FAIL, retry: CAS(2→3): SUCCESS
↑ Threads spin, burning CPU — contention

LongAdder under high contention:

Core 1 → Cell[1] += 1  (no contention)
Core 2 → Cell[2] += 1  (no contention)
Core 3 → Cell[3] += 1  (no contention)
Core 4 → Cell[4] += 1  (no contention)

sum() = Cell[1] + Cell[2] + Cell[3] + Cell[4] = 4  (correct total)
↑ No spinning — each core writes its own cell
```

```java
// AtomicLong — fast for low contention, simpler API:
AtomicLong counter = new AtomicLong(0);
counter.incrementAndGet();
long value = counter.get(); // exact, current value

// LongAdder — fast for high contention, limited API:
LongAdder adder = new LongAdder();
adder.increment();
long value = adder.sum(); // approximate for concurrent use (races allowed)
                          // accurate only when no concurrent updates

// When to use what:
// Read-frequent, write-occasional → AtomicLong (exact get())
// Write-heavy counters (metrics, stats) → LongAdder (better throughput)
// Need compareAndSet? → AtomicLong (LongAdder has no CAS)
// Need exact value under concurrency? → AtomicLong
```

**Performance difference:** Under 16-thread contention benchmark, `LongAdder` can be 10–50x faster than `AtomicLong` for pure increment workloads.

> Resource: [LongAdder vs AtomicLong — Java Specialists](https://www.javaspecialists.eu/archive/Issue152-Concurrent-Counter-Revisited.html)

---

### Q103. [HARD] What is Project Loom and virtual threads? How are they different from platform threads?

**Answer:**

**Platform threads** (traditional Java threads): one-to-one mapping with OS kernel threads. Expensive to create (~1MB stack), context switches handled by OS (expensive), typically limited to thousands in practice.

**Virtual threads** (Java 21+, Project Loom): JVM-managed lightweight threads. Run on top of a small number of carrier (platform) threads. Cheap to create (small initial stack), context switches handled by JVM in user space (cheap).

```
Traditional:
Java Thread ──1:1──► OS Thread
1,000 threads = 1,000 OS threads = ~1GB stack space

Virtual Threads:
Virtual Thread (V1) ─────┐
Virtual Thread (V2) ──── ► Carrier Thread (OS Thread 1)
Virtual Thread (V3) ─────┘    (scheduled by JVM scheduler)
Virtual Thread (V4) ─────┐
Virtual Thread (V5) ──── ► Carrier Thread (OS Thread 2)
...
1,000,000 virtual threads → only 8 carrier threads (one per core)
```

```java
// Creating virtual threads (Java 21):
Thread.ofVirtual().start(() -> handleRequest(req)); // lightweight!

// Or via ExecutorService:
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> handleRequest(request));
    }
} // 100,000 virtual threads — would OOM with platform threads
```

**Key difference — blocking behavior:**

```
Platform thread blocking on IO:
  Thread blocked in kernel → OS context switch → kernel overhead

Virtual thread blocking on IO:
  JVM detects block → unmounts virtual thread from carrier thread
  Carrier thread picks up another virtual thread → JVM context switch (cheap)
  When IO completes → virtual thread remounted on a carrier thread
```

**Impact:** Web servers can handle 100,000+ concurrent connections with virtual threads where they'd previously need complex async/reactive programming with callbacks.

> Resource: [JEP 444 — Virtual Threads (JDK 21)](https://openjdk.org/jeps/444)

---

### Q104. [MID] What is the difference between execute() and submit() in ExecutorService?

**Answer:**

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

// execute() — for Runnable only, returns void, exceptions are lost:
executor.execute(() -> doWork());
// If doWork() throws an unchecked exception, it goes to the thread's
// UncaughtExceptionHandler — you cannot catch it from the submitting thread

// submit() — for Runnable or Callable, returns Future:
Future<?> f1 = executor.submit(() -> doWork());      // Runnable → Future<?>
Future<Integer> f2 = executor.submit(() -> compute()); // Callable<Integer> → Future<Integer>

// Exceptions from submit() are captured in the Future:
try {
    f2.get(); // if compute() threw, get() throws ExecutionException
} catch (ExecutionException e) {
    // original exception is e.getCause()
}
```

**Summary:**
- `execute()` = fire-and-forget, no result, exceptions potentially lost
- `submit()` = get a Future, retrieve result later, exceptions captured in Future

> Always prefer `submit()` in production so you can handle exceptions properly.

---

### Q105. [MID] Explain the ForkJoinPool. When should you use it?

**Answer:**

`ForkJoinPool` is a specialized thread pool for **divide-and-conquer recursive tasks**. Its key feature: **work stealing** — when a thread finishes its tasks, it steals tasks from other threads' queues, maximizing CPU utilization.

```java
// Classic ForkJoinTask — recursive parallel sum:
class SumTask extends RecursiveTask<Long> {
    private final int[] array;
    private final int from, to;
    private static final int THRESHOLD = 1000;

    SumTask(int[] array, int from, int to) {
        this.array = array; this.from = from; this.to = to;
    }

    @Override
    protected Long compute() {
        if (to - from <= THRESHOLD) {
            // Base case: compute directly
            long sum = 0;
            for (int i = from; i < to; i++) sum += array[i];
            return sum;
        }
        // Recursive case: split
        int mid = (from + to) / 2;
        SumTask left  = new SumTask(array, from, mid);
        SumTask right = new SumTask(array, mid, to);
        left.fork();                   // submit left to pool asynchronously
        long rightResult = right.compute(); // compute right in current thread
        long leftResult  = left.join();     // wait for left
        return leftResult + rightResult;
    }
}

ForkJoinPool pool = new ForkJoinPool(4); // 4 worker threads
long result = pool.invoke(new SumTask(array, 0, array.length));
```

**When to use:**
- CPU-intensive recursive divide-and-conquer (parallel sort, parallel search, tree traversal)
- `Stream.parallel()` uses the common ForkJoinPool internally
- `CompletableFuture.supplyAsync()` with no executor uses common ForkJoinPool

**When NOT to use:**
- IO-bound tasks (blocking IO wastes the work-stealing advantage — use regular ThreadPoolExecutor with more threads)
- Long-blocking tasks (occupies a carrier thread, prevents work-stealing)

> Resource: [Fork/Join in Java 7 — Oracle](https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html)

---

### Q106. [HARD] What happens when you submit a task that never terminates to a ThreadPoolExecutor?

**Answer:**

The thread executing the task is "consumed" permanently — it never returns to the pool.

```
Pool: corePoolSize=4

Thread-1: runs task that never ends (infinite loop)
Thread-2: runs task that never ends
Thread-3: runs task that never ends
Thread-4: runs task that never ends

All 4 core threads consumed!

New tasks arrive:
  Queue fills up (say queueCapacity=100)
  Pool tries to create non-core threads (max = maxPoolSize, say 8)
  Threads 5-8 created
  Those tasks never end either → all 8 threads stuck
  Next task: queue full + all threads stuck → REJECTION POLICY fires
```

**Diagnosis:** Thread dump shows all threads in `RUNNABLE` state running the same stack frame forever. `executor.getActiveCount()` equals pool size, `queue.size()` is maxed out.

**Prevention:**
1. Add timeouts to all blocking operations (HTTP calls, DB queries, IO)
2. Use `Future.get(timeout, TimeUnit)` and cancel if exceeded
3. Use circuit breakers (Resilience4j) to fail fast
4. Monitor `activeCount` and `queueSize` — alert if saturated

```java
// Safe task with timeout:
Future<Result> future = executor.submit(() -> callExternalService());
try {
    return future.get(5, TimeUnit.SECONDS); // max 5s
} catch (TimeoutException e) {
    future.cancel(true); // interrupt the thread
    throw new ServiceUnavailableException("Timed out");
}
```

---

### Q107. [MID] Explain the StampedLock and its optimistic read mode.

**Answer:**

`StampedLock` (Java 8+) is a faster alternative to `ReadWriteLock` for read-heavy workloads. It introduces **optimistic reads** — reads that don't acquire any lock at all.

```java
StampedLock lock = new StampedLock();
private double x, y; // coordinates

// OPTIMISTIC READ — no lock acquired:
public double distanceFromOrigin() {
    long stamp = lock.tryOptimisticRead();  // returns a stamp (version number)
    double curX = x;                        // read WITHOUT holding any lock
    double curY = y;                        // race condition is EXPECTED here!

    if (!lock.validate(stamp)) {            // validate: did a write happen?
        // YES — a write occurred while we read → our values may be inconsistent
        stamp = lock.readLock();            // fall back to real read lock
        try {
            curX = x;
            curY = y;
        } finally {
            lock.unlockRead(stamp);
        }
    }
    return Math.sqrt(curX * curX + curY * curY);
}

// WRITE — exclusive lock:
public void movePoint(double newX, double newY) {
    long stamp = lock.writeLock();
    try {
        x = newX;
        y = newY;
    } finally {
        lock.unlockWrite(stamp);
    }
}
```

**Comparison:**

```
ReadWriteLock.readLock():  acquires read lock (CAS + memory barrier)
StampedLock optimistic:    NO lock acquisition (just reads a stamp counter)
                           Validate: checks if counter changed (single read)
                           Fallback to read lock only if write occurred

In read-dominant workloads:
  If writes are rare: optimistic reads almost always succeed without a real lock
  → 3–5x faster than ReadWriteLock
```

**Caution:** StampedLock is NOT reentrant. The same thread cannot acquire a read lock and then a write lock — it will deadlock. Use only for simple, non-reentrant access patterns.

---

---

# QUICK REFERENCE: INTERVIEW ANSWER FRAMEWORKS

---

### Framework 1: "Is X thread-safe?" answer structure

```
1. What shared state does X access?
2. How is that state accessed (read, write, compound)?
3. Is there synchronization (volatile, synchronized, atomic)?
4. If synchronized: is the right lock used consistently?
5. Are there compound operations that need atomicity across multiple fields?
```

### Framework 2: Debugging a concurrency bug

```
1. Look for: shared mutable state accessed by multiple threads
2. Look for: compound operations without atomic guarantees
3. Look for: locks that are not held consistently
4. Thread dump: identify BLOCKED threads (deadlock?) or high RUNNABLE count (livelock?)
5. Use tools: Java Flight Recorder, jstack, VisualVM, ThreadSanitizer
6. Reproduce: run with -ea (assertions), use stress tests, use testing frameworks
```

### Framework 3: Choosing a concurrency primitive

```
Need to share a counter?      → AtomicInteger / LongAdder
Need to share a flag?         → volatile boolean
Need to protect a block?      → synchronized or ReentrantLock
Need to wait for condition?   → wait()/notifyAll() or Condition.await()/signal()
Need to limit concurrency?    → Semaphore
Need all threads to meet?     → CyclicBarrier
Need N tasks done, then go?   → CountDownLatch
Need thread-local state?      → ThreadLocal
Need async result?            → CompletableFuture
Need safe publish once?       → volatile + DCL or static initializer
```

---

## CURATED RESOURCES

```
Books (best in class):
  ✦ Java Concurrency in Practice — Brian Goetz (THE reference)
    https://jcip.net/

  ✦ The Art of Multiprocessor Programming — Herlihy, Shavit
    (Advanced: lock-free algorithms, formal reasoning)

Online:
  ✦ Java Memory Model — JSR-133 spec
    https://www.cs.umd.edu/~pugh/java/memoryModel/

  ✦ Heinz Kabutz — Java Specialists Newsletter (advanced topics)
    https://www.javaspecialists.eu/

  ✦ Mechanical Sympathy Blog — Martin Thompson (hardware effects)
    https://mechanical-sympathy.blogspot.com/

  ✦ Oracle Concurrency Tutorial
    https://docs.oracle.com/javase/tutorial/essential/concurrency/

  ✦ JEP 444 — Virtual Threads (Java 21)
    https://openjdk.org/jeps/444

YouTube:
  ✦ Heinz Kabutz — JVM internals deep dives (Devoxx)
  ✦ Brian Goetz — Java concurrency talks (GOTO, Devoxx)
  ✦ Venkat Subramaniam — Java concurrency (Agile Developer)

Tools for debugging concurrency:
  ✦ jstack — thread dump
  ✦ Java Flight Recorder (JFR) — low-overhead profiling
  ✦ VisualVM — thread monitoring
  ✦ IntelliJ IDEA — race condition detection (built-in inspections)
```

---

<div align="center">

**☕ @AbhishekGoyal | Java Concurrency Interview Question Bank**
*300+ Questions | All topics from the Java Concurrency & Multithreading Reference*

</div>
