<div align="center">

# ☕ Java Concurrency & Multithreading
### The Complete, Internals-First Reference

> From `new Thread()` to `CompletableFuture`, from CPU cache lines to the JVM memory model —  
> everything explained from the ground up, with real code and zero hand-waving.

[![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square&logo=java)](https://www.java.com)
[![Topics](https://img.shields.io/badge/Topics-16-blue?style=flat-square)](#table-of-contents)
[![Level](https://img.shields.io/badge/Level-Beginner%20→%20Advanced-green?style=flat-square)](#)
[![Interview Ready](https://img.shields.io/badge/Interview-Ready-purple?style=flat-square)](#interview-questions)

</div>

---

## Table of Contents

1. [Why Concurrency Exists - The Hardware Story](#1-why-concurrency-exists--the-hardware-story)
2. [Thread Life Cycle - All 6 States](#2-thread-life-cycle--all-6-states)
3. [Creating Threads - new Thread() Internals](#3-creating-threads--new-thread-internals)
4. [Runnable vs Extending Thread](#4-runnable-vs-extending-thread)
5. [The synchronized Keyword - Deep Dive](#5-the-synchronized-keyword--deep-dive)
6. [The volatile Keyword --CPU Cache & Memory Barriers](#6-the-volatile-keyword--cpu-cache--memory-barriers)
7. [wait() and notify() - Inter-Thread Coordination](#7-wait-and-notify--inter-thread-coordination)
8. [wait() vs sleep() - The Critical Difference](#8-wait-vs-sleep--the-critical-difference)
9. [Thread.join() - Ordering Execution](#9-threadjoin--ordering-execution)
10. [Runnable vs Callable](#10-runnable-vs-callable)
11. [How to Stop a Thread - The Right Way](#11-how-to-stop-a-thread--the-right-way)
12. [Thread Pools — Internals & Configuration](#12-thread-pools--internals--configuration)
13. [ThreadPoolTaskExecutor — corePoolSize vs maxPoolSize](#13-threadpooltaskexecutor--corepoolsize-vs-maxpoolsize)
14. [java.util.concurrent — The Full Toolkit](#14-javautilconcurrent--the-full-toolkit)
15. [Using a Mutex Object in Java](#15-using-a-mutex-object-in-java)
16. [java.util.concurrent.Future](#16-javautilconcurrentfuture)
17. [ThreadLocal — Per-Thread State](#17-threadlocal--per-thread-state)
18. [Asynchronous Programming — CompletableFuture](#18-asynchronous-programming--completablefuture)
19. [The Java Memory Model (JMM)](#19-the-java-memory-model-jmm)
20. [Common Concurrency Bugs](#20-common-concurrency-bugs)
21. [Interview Questions & Answers](#21-interview-questions--answers)

---

# JVM Memory Model — Process, Threads & Memory Segments

> **Where to insert:** Add this section at the very top of your existing multithreading README, before Topic 1 (Why Concurrency Exists).

---

## 0. JVM Memory Architecture — The Foundation

Before diving into threads, you need to understand **how the JVM lays out memory** and what each thread actually owns vs shares.

---

### Process vs JVM Instance

A **Process** is an OS-level abstraction. Each Java program you run = one **OS Process** = one **JVM Instance**.

```
OS
├── Process1  →  JVM Instance1  (heap: 2GB, its own address space)
└── Process2  →  JVM Instance2  (heap: 2GB, its own address space)
```

- Two JVM instances are **completely isolated** from each other.
- Same virtual memory address (e.g., `0x8000`) in Process1 and Process2 points to **different physical RAM locations**.
- Processes do NOT share heap memory. Threads within the same process DO.

---

### Memory Segments Inside a JVM Process

Every JVM process has two **shared** segments and per-thread **private** state:

```
JVM Instance (Process)
│
├── [SHARED — all threads see this]
│   ├── Code Segment
│   ├── Data Segment
│   └── Heap
│
└── [PRIVATE — each thread owns its own copy]
    ├── Stack
    ├── Register
    └── Program Counter (PC)
```

---

### Code Segment

- Contains the **compiled Bytecode** (i.e., machine code) of the Java program.
- **Read-only** — no thread can modify it at runtime.
- **Shared** across all threads within the same process.
- Since it's immutable, there are **no synchronization concerns** here.

---

### Data Segment

- Contains **global and static variables** (e.g., `static int counter`).
- **Shared** across all threads within the same process.
- Threads can **read and modify** the same data.
- ⚠️ **Synchronization is required** — this is one of the primary sources of race conditions in Java.

```java
class Counter {
    static int count = 0; // lives in Data Segment — shared by all threads
}
```

---

### Heap

- Objects created at runtime via the `new` keyword are allocated on the heap.
- **Shared** among all threads of the **same process**.
- **NOT shared across processes** — each JVM instance has its own heap.
- Threads can read and modify heap data concurrently → synchronization needed.

```java
// This object lives on the heap — visible to all threads in the process
MyObject obj = new MyObject();
```

> Example: In Process1, virtual address `0x8000` may point to some heap object.  
> In Process2, the same address `0x8000` points to a **completely different** physical memory location.

---

### Stack (Per-Thread)

- **Each thread has its own private Stack.**
- Manages:
  - Method call frames
  - Local variables
  - Return addresses
- Stack data is **never shared** between threads → no synchronization needed for local variables.

```java
void compute() {
    int x = 10; // lives on THIS thread's stack — completely private
}
```

---

### Register (Per-Thread)

- **Each thread has its own set of CPU Registers.**
- When the JIT (Just-In-Time) compiler converts Bytecode into native machine code, it uses registers to **optimize** the generated instructions.
- Registers also hold intermediate values during **context switching** — the CPU saves/restores register state when switching between threads.

---

### Program Counter / Counter (Per-Thread)

- Also called the **Program Counter (PC)**.
- Points to the instruction currently being executed by that thread.
- **Increments** after each successful instruction execution.
- Each thread has its own PC → threads execute independently and track their own position in the code.

---

### Full Picture — CPU + JVM + RAM

```
CPU1                        CPU2
├── Register (T1)           ├── Register (T2)
└── L1/L2 Cache             └── L1/L2 Cache
         │                           │
         └──────────┬────────────────┘
                    │
              Main Memory (RAM)
              ├── JVM Process1
              │   ├── Heap (shared among T1, T2, T3)
              │   ├── Code Segment (shared, read-only)
              │   ├── Data Segment (shared, mutable)
              │   ├── Stack-T1 (private)
              │   ├── Stack-T2 (private)
              │   └── Stack-T3 (private)
              └── JVM Process2
                  └── (completely isolated heap + segments)
```

> **Why this matters for concurrency:** The CPU cache layer is what causes visibility problems. A value written by CPU1 may sit in its cache and not be flushed to Main Memory, making it invisible to CPU2. This is exactly why `volatile` and the Java Memory Model (JMM) exist — covered in Topic 19.

---

### Quick Reference — What's Shared vs Private

| Memory Area    | Shared or Private?              | Sync Needed? |
|----------------|---------------------------------|--------------|
| Code Segment   | Shared (all threads in process) | No (read-only) |
| Data Segment   | Shared (all threads in process) | ✅ Yes        |
| Heap           | Shared (all threads in process) | ✅ Yes        |
| Stack          | Private (per thread)            | No            |
| Register       | Private (per thread)            | No            |
| Program Counter| Private (per thread)            | No            |

---

## 0.1 — How Java Code Actually Executes: Bytecode → CPU → RAM

---

### Step 1: Java source → Bytecode → Machine code (JIT)

```
Java source (.java)
        │
        │  javac
        ▼
Bytecode (.class file)          ← platform-independent
        │
        │  JVM loads it
        ▼
JIT Compiler (inside JVM)
    ├── Phase 1: Interpret bytecode line by line (slow, cold start)
    ├── Phase 2: Profile which methods are "hot" (called frequently)
    └── Phase 3: Compile hot methods → native machine code
        │
        ▼
Native machine code             ← stored in JVM Code Cache (RAM)
        │
        ▼
CPU executes it using registers
```

**Key points:**
- Bytecode is NOT machine code. It is an intermediate representation understood by the JVM.
- JIT compiles bytecode to CPU-specific instructions (x86, ARM, etc.) at runtime.
- The JIT uses CPU registers to **optimize** the generated native instructions — e.g., keeping a loop counter in a register instead of reading it from RAM every iteration.
- Each thread has its own Register set and Program Counter. The Register holds intermediate values; the PC points to the next instruction to execute.

---

### Step 2: CPU Register → L1 → L2 → L3 → RAM (cache hierarchy)

```
┌──────────────────────┐    ┌──────────────────────┐
│      CPU Core 1      │    │      CPU Core 2       │
│                      │    │                       │
│  ┌────────────────┐  │    │  ┌────────────────┐   │
│  │   Registers    │  │    │  │   Registers    │   │
│  │  ~1 cycle      │  │    │  │  ~1 cycle      │   │
│  │  ~1KB per thrd │  │    │  │  ~1KB per thrd │   │
│  └───────┬────────┘  │    │  └───────┬────────┘   │
│          │           │    │          │             │
│  ┌───────▼────────┐  │    │  ┌───────▼────────┐   │
│  │   L1 Cache     │  │    │  │   L1 Cache     │   │
│  │  ~4 cycles     │  │    │  │  ~4 cycles     │   │
│  │  32–64KB       │  │    │  │  32–64KB       │   │
│  └───────┬────────┘  │    │  └───────┬────────┘   │
│          │           │    │          │             │
│  ┌───────▼────────┐  │    │  ┌───────▼────────┐   │
│  │   L2 Cache     │  │    │  │   L2 Cache     │   │
│  │  ~12 cycles    │  │    │  │  ~12 cycles    │   │
│  │  256KB–1MB     │  │    │  │  256KB–1MB     │   │
│  └───────┬────────┘  │    │  └───────┬────────┘   │
└──────────┼───────────┘    └──────────┼─────────────┘
           │                           │
           └──────────┬────────────────┘
                      │
           ┌──────────▼────────────┐
           │   L3 Cache (shared)   │
           │   ~40 cycles          │
           │   4–32MB              │
           └──────────┬────────────┘
                      │
           ┌──────────▼────────────┐
           │   Main Memory (RAM)   │
           │   ~100 cycles         │
           │   GBs                 │
           └───────────────────────┘
```

**Why this matters for Java concurrency:**
- Thread A runs on Core 1. It writes `counter = 5` — this write goes into Core 1's L1 cache first.
- Thread B runs on Core 2. It reads `counter` — but Core 2's L1 might still have the OLD value.
- This is the **CPU cache visibility problem** — the root cause of why `volatile` and `synchronized` exist.
- `volatile` forces writes to go to main memory and reads to come from main memory, bypassing cache.

---

### Step 3: Program Counter (PC) and Register — per-thread execution

```
Thread 1                         Thread 2
┌─────────────────┐              ┌─────────────────┐
│ Program Counter │              │ Program Counter │
│ Points to next  │              │ Points to next  │
│ instruction     │              │ instruction     │
│                 │              │                 │
│ Register set    │              │ Register set    │
│ Holds JIT-      │              │ Holds JIT-      │
│ optimized temp  │              │ optimized temp  │
│ values          │              │ values          │
└─────────────────┘              └─────────────────┘
         │                                │
         └──────── CPU (shared) ──────────┘
```

- **Program Counter**: each thread tracks its OWN position in the instruction stream. When a thread is paused and resumed, execution continues from exactly where the PC left off.
- **Register**: the JIT compiler maps frequently used variables to CPU registers for speed. Each thread's register state is completely private — saved/restored on every context switch.

---

### Step 4: Context switching — OS scheduler

When the OS switches a CPU core from Thread A to Thread B:

```
Thread A running on Core 1
        │
        │  OS timer interrupt fires (every ~1–10ms)
        ▼
OS Scheduler takes control
        │
        ├─► Save Thread A state → PCB (Process Control Block) in RAM
        │       - all registers (R0..Rn)
        │       - program counter value
        │       - stack pointer
        │       - CPU flags
        │
        ├─► Select Thread B from run queue (priority, fairness policy)
        │
        ├─► Load Thread B state ← PCB from RAM
        │       - restore all registers
        │       - restore program counter
        │       - restore stack pointer
        │
        ▼
Thread B resumes on Core 1 — exactly where it left off
```

**Why context switches are expensive:**
1. **PCB save/restore** — reading/writing registers to RAM costs cycles.
2. **Cache is now cold** — Thread B's working data is NOT in L1/L2. The CPU must fetch from RAM (100× slower) until the cache warms up again.
3. **TLB flush** — if switching between processes (not just threads), the CPU's address translation cache is invalidated.

> This is why virtual threads (Project Loom) are faster — they are scheduled by the JVM, not the OS. The JVM switch cost is lower and avoids kernel mode transitions.

---

### Step 5: Multi-core — concurrency vs true parallelism

```
Single core (concurrency — interleaved):

Core 1 timeline:
│ Thread A │ Thread B │ Thread A │ Thread B │ ...
└──────────────────────────────────────────────▶ time

Only one thread executes at any moment. Threads take turns.


Multi-core (parallelism — simultaneous):

Core 1:  │ Thread A │ Thread A │ Thread A │ ...
Core 2:  │ Thread B │ Thread B │ Thread B │ ...
                                               ▶ time

Both threads run at the exact same instant.
```

**The danger of multi-core:**

```java
// Thread A (Core 1)           // Thread B (Core 2)
counter++;                      counter++;
// Both read counter = 0
// Both compute 0 + 1 = 1
// Both write 1
// Final value: 1 — not 2!     ← RACE CONDITION
```

- Multi-core makes race conditions **real** — two cores can literally execute the same instruction simultaneously on different cache-lines of the same variable.
- Single-core concurrency has the illusion of simultaneity but the CPU actually serializes at instruction level.
- Shared mutable state (heap, static fields) is the problem. Per-thread state (stack, registers, PC) is always safe.

---

### Quick Reference — What each component stores

| Component | Scope | Stores | Speed |
|-----------|-------|--------|-------|
| Register | Per-thread (private) | JIT-optimized temp values, current operands | ~1 cycle |
| Program Counter | Per-thread (private) | Address of next instruction | ~1 cycle |
| Stack | Per-thread (private) | Method frames, local variables | L1 speed |
| L1 Cache | Per-core (private) | Recently accessed data/instructions | ~4 cycles |
| L2 Cache | Per-core (private) | Overflow from L1 | ~12 cycles |
| L3 Cache | Shared across cores | Overflow from L2 | ~40 cycles |
| RAM (Heap) | Shared across all threads | Objects, static fields, code cache | ~100 cycles |

---

## 1. Why Concurrency Exists — The Hardware Story

Before writing a single line of concurrent Java, you need to understand *why* concurrency exists at all. It is not a software abstraction. It is a direct consequence of how modern hardware works.

### 1.1 The Single-Core Era

In the early days of computing, CPUs had a single core. The operating system gave the illusion of parallelism through **time-slicing**: rapidly switching between processes so quickly that users perceived them as running simultaneously. But underneath, only one instruction executed at a time.

### 1.2 The Memory Wall

Through the 1990s and 2000s, CPU clock speeds increased dramatically. But memory (RAM) access speeds did not keep up. By the early 2000s, a CPU could execute hundreds of instructions in the time it took to fetch a single value from RAM. This gap is called the **memory wall**.

The solution was **CPU caches** — small, ultra-fast memories placed physically on the CPU die:

```
┌──────────────────────────────────────────────────┐
│                   CPU (Core)                     │
│                                                  │
│   ┌──────────┐   ┌──────────┐   ┌────────────┐  │
│   │  L1 Cache│   │  L2 Cache│   │  L3 Cache  │  │
│   │  ~32 KB  │──▶│  ~256 KB │──▶│  ~8–32 MB  │  │
│   │  ~1 ns   │   │  ~4 ns   │   │  ~10–40 ns │  │
│   └──────────┘   └──────────┘   └────────────┘  │
│         │                                        │
└─────────┼────────────────────────────────────────┘
          │
    ┌─────▼──────┐
    │    RAM     │  ~60–100 ns
    │ (Main Mem) │
    └────────────┘
```

When a CPU reads a variable, it fetches it from RAM into its L1 cache. Subsequent reads are served from cache — thousands of times faster.

### 1.3 Why This Causes Concurrency Problems

On a **multi-core** CPU (every modern machine), each core has its own L1 and L2 cache. If Core-1 reads `x = 5` into its cache, then Core-2 reads `x` into its own cache, and Core-1 writes `x = 10` — Core-2's cache still says `x = 5`. They are out of sync.

```
Core-1                    Core-2
──────────────────────    ──────────────────────
L1 Cache: x = 10          L1 Cache: x = 5  ← stale!
         ↑                          ↑
         │                          │
         └──────────┬───────────────┘
                    │
              RAM: x = 5  (not yet updated)
```

This is the **visibility problem** — the root cause of many Java concurrency bugs. The `volatile` keyword and `synchronized` both exist primarily to address this.

### 1.4 The Multi-Core Revolution & Why Concurrency Matters

Around 2005, CPU manufacturers hit a power wall — they could no longer increase clock speeds without generating too much heat. Instead, they added more cores. A modern laptop has 8–16 cores; a server may have 128.

**Programs that cannot use multiple cores are leaving performance on the table.** A single-threaded Java program running on a 16-core machine uses approximately 6% of the available CPU. Concurrency is the tool that unlocks the other 94%.

But concurrency introduces problems that don't exist in single-threaded code: **race conditions**, **deadlocks**, **memory visibility issues**, and **liveness failures**. The rest of this document is about understanding and solving those problems.

---

## 2. Thread Life Cycle — All 6 States

A Java thread transitions through exactly **6 states**, defined in the `Thread.State` enum inside `java.lang.Thread`. Understanding these states — and what triggers every transition — is foundational.

### 2.1 The State Diagram

```
                    ┌──────────────────────────────────────────────────────┐
                    │                    RUNNABLE                           │
  new Thread()      │  ┌────────────┐              ┌────────────────────┐  │
  ─────────────▶  NEW │  │  READY     │  scheduler  │     RUNNING        │  │
                    │  │  (queued)  │ ◀──────────▶ │  (on CPU)         │  │
       .start()     │  └────────────┘              └────────────────────┘  │
  ─────────────────▶│                                                       │
                    └───────────────────────────────────────────────────────┘
                              │              │               │
                   wait()     │              │ sleep(n)/     │ synchronized
                   join()     │              │ wait(n)/      │ (lock held
                   park()     │              │ join(n)       │ elsewhere)
                              ▼              ▼               ▼
                          WAITING    TIMED_WAITING        BLOCKED
                              │              │               │
                              │  notify()    │  timeout      │  lock
                              │  notifyAll() │  expires      │  released
                              └──────────────┴───────────────┘
                                             │
                                             ▼
                                        RUNNABLE
                                             │
                                   run() ends or
                                   uncaught exception
                                             │
                                             ▼
                                        TERMINATED
```

### 2.2 State 1: NEW

**What it means:** The `Thread` object has been constructed but `.start()` has not been called. No OS-level thread exists. The Java object is sitting on the heap.

**Internal representation:** Java stores the thread state internally as an integer field `threadStatus`. The value `0` corresponds to NEW.

```java
Thread t = new Thread(() -> System.out.println("hello"));
System.out.println(t.getState());   // NEW
System.out.println(t.isAlive());    // false — no OS thread yet
System.out.println(t.getId());      // auto-assigned unique long ID
```

**What you can do in NEW state:**
- `setName(String)` — always name your threads in production
- `setPriority(int)` — set scheduling hint (1–10, default 5)
- `setDaemon(boolean)` — **MUST** be called before `.start()`, impossible after

**What you cannot do:**
- Call `.start()` twice — throws `IllegalThreadStateException` on the second call
- Set daemon status after starting — throws `IllegalThreadStateException`

### 2.3 State 2: RUNNABLE

**What it means:** `.start()` was called. An OS-level thread now exists and the JVM has allocated a stack for it. The thread is either waiting for CPU time (ready) or currently executing on a CPU core (running). Java does NOT distinguish between these two sub-states — both are simply `RUNNABLE`.

**Why Java doesn't separate "ready" and "running":** The JVM delegates scheduling entirely to the OS. Tracking which exact core a thread is on would require OS-level calls that are platform-specific and expensive. The abstraction intentionally hides this.

**What causes a RUNNABLE thread to leave this state:**
- `run()` method completes → TERMINATED
- Tries to enter a `synchronized` block held by another thread → BLOCKED
- Calls `object.wait()` → WAITING
- Calls `Thread.sleep(n)` or `object.wait(n)` → TIMED_WAITING
- Calls `LockSupport.park()` → WAITING

### 2.4 State 3: BLOCKED

**What it means:** The thread attempted to enter a `synchronized` block or method and the monitor lock is held by another thread. The thread is parked by the JVM, consuming zero CPU cycles, waiting for the lock to become available.

**Key insight:** A BLOCKED thread holds NO locks. It is passively waiting for one.

```java
Object lock = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lock) {
        Thread.sleep(3000);  // holds lock for 3 seconds
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lock) {   // cannot enter — t1 holds it
        System.out.println("t2 got the lock");
    }
});

t1.start();
Thread.sleep(100);   // let t1 grab the lock
t2.start();
Thread.sleep(100);

System.out.println(t2.getState());  // BLOCKED
```

**Transition out of BLOCKED:** When the holding thread releases the monitor (exits the `synchronized` block), the JVM picks one of the BLOCKED threads (no fairness guarantee) and transitions it to RUNNABLE.

### 2.5 State 4: WAITING

**What it means:** The thread has voluntarily suspended itself and released its monitor lock. It will not resume until explicitly notified. No timeout — it waits indefinitely.

**Three ways to enter WAITING:**
1. `object.wait()` — releases the lock and waits for `notify()`
2. `Thread.join()` (no argument) — waits for another thread to terminate
3. `LockSupport.park()` — low-level parking primitive

**What happens internally with `wait()`:**
1. Thread checks it holds the monitor (throws `IllegalMonitorStateException` if not)
2. Thread is added to the object's **wait set** (a data structure in the JVM)
3. Monitor is atomically released
4. Thread is suspended

**What happens internally with `notify()`:**
1. One thread is removed from the wait set
2. That thread moves from WAITING to BLOCKED (not directly to RUNNABLE)
3. The notified thread must re-acquire the monitor before it can run
4. This is why you always need a `while` loop, not `if`

```java
// The pattern every Java developer must know
synchronized (lock) {
    while (!condition) {   // WHILE — not if
        lock.wait();
    }
    // proceed — condition is guaranteed true here
}
```

### 2.6 State 5: TIMED_WAITING

**What it means:** Same as WAITING, but with a deadline. The thread wakes up either when signaled or when the timeout expires — whichever comes first.

**Methods that cause TIMED_WAITING:**
- `Thread.sleep(millis)` — most common; does NOT release locks
- `object.wait(millis)` — releases the lock, sets a timeout
- `Thread.join(millis)` — waits for another thread up to a deadline
- `LockSupport.parkNanos(nanos)` / `LockSupport.parkUntil(deadline)`

**Critical difference from sleep vs wait:**
- `Thread.sleep(1000)` → TIMED_WAITING, **lock NOT released**
- `object.wait(1000)` → TIMED_WAITING, **lock IS released**

This distinction causes real bugs. See [Section 8](#8-wait-vs-sleep--the-critical-difference) for the full breakdown.

### 2.7 State 6: TERMINATED

**What it means:** The `run()` method has returned (normally or via uncaught exception). The OS-level thread is gone. The `Thread` Java object still exists on the heap and you can call methods on it (`getState()` returns TERMINATED, `isAlive()` returns false), but no code can execute on it.

**Can a TERMINATED thread be restarted?**

No. Calling `.start()` on a TERMINATED thread throws `IllegalThreadStateException`. The thread state machine is strictly one-directional: it can reach TERMINATED but never leave it. If you need to run the task again, create a new `Thread` object.

```java
Thread t = new Thread(() -> System.out.println("done"));
t.start();
t.join();                         // wait for completion
System.out.println(t.getState()); // TERMINATED
t.start();                        // throws IllegalThreadStateException!
```

### 2.8 State Summary Table

| State | OS Thread Exists? | Holds Lock? | CPU Active? | How to Exit |
|---|---|---|---|---|
| NEW | No | No | No | `.start()` |
| RUNNABLE | Yes | Maybe | Yes/No (scheduled) | Voluntary or blocking op |
| BLOCKED | Yes | No | No | Other thread releases lock |
| WAITING | Yes | No | No | `notify()` / `notifyAll()` / `join()` returns |
| TIMED_WAITING | Yes | Maybe (see sleep) | No | Timeout expires or signal |
| TERMINATED | No | No | No | Final — cannot leave |

---

## 3. Creating Threads — new Thread() Internals

### 3.1 What Actually Happens When You Write `new Thread()`

```java
Thread t = new Thread(() -> System.out.println("Hello"));
```

When this executes, Java does the following — none of which involves the OS:

**Step 1: Allocate on heap.** A new `Thread` object is created on the Java heap, like any other object.

**Step 2: Copy group from parent.** The new thread inherits its `ThreadGroup` from the calling thread (typically `main`). Thread groups are a largely obsolete feature, but the mechanism still runs.

**Step 3: Copy daemon status.** `daemon = parentThread.isDaemon()`. Your new thread is non-daemon by default (since main is non-daemon).

**Step 4: Copy priority.** `priority = parentThread.getPriority()` — usually 5 (NORM_PRIORITY).

**Step 5: Store the Runnable.** The lambda or Runnable is stored in a field called `target`. It does not execute yet.

**Step 6: Set threadStatus = 0.** Internally, the thread state is represented as an int. 0 = NEW.

**Step 7: Assign a thread ID.** A unique long ID is generated from an atomic counter and stored in the `tid` field.

**Result:** You have a plain Java object. Zero system calls. The OS has no idea this thread exists.

### 3.2 What `.start()` Does — The System Call

```java
t.start();
```

This is where everything changes. `.start()` is a thin Java wrapper around a native method called `start0()`:

```java
// Inside Thread.java (simplified)
public synchronized void start() {
    if (threadStatus != 0)
        throw new IllegalThreadStateException();
    // ... group bookkeeping ...
    start0();  // native method — triggers the OS call
}

private native void start0();
```

`start0()` makes a system call to the OS (e.g., `pthread_create` on Linux, `CreateThread` on Windows) which:
1. Creates an actual kernel-level thread
2. Allocates a native stack for it (typically 512KB–1MB by default)
3. Registers it with the OS scheduler
4. Sets `threadStatus` to a non-zero value indicating RUNNABLE

From this point, the thread is "alive" (`isAlive()` returns true) and the OS controls when your `run()` method actually executes.

### 3.3 The Four Constructor Overloads

```java
// 1. Just a Runnable — auto-name: "Thread-0", "Thread-1", etc.
Thread t1 = new Thread(myRunnable);

// 2. Runnable + explicit name — ALWAYS do this in production
Thread t2 = new Thread(myRunnable, "payment-processor-1");

// 3. ThreadGroup + Runnable + name
ThreadGroup group = new ThreadGroup("io-workers");
Thread t3 = new Thread(group, myRunnable, "io-worker-1");

// 4. Extending Thread (explained in next section — avoid this)
class MyThread extends Thread {
    public void run() { /* task */ }
}
```

**Why naming matters:** When your application crashes at 2 AM and you look at a thread dump, `Thread-47` tells you nothing. `payment-processor-3` tells you exactly where to look. Always name threads or use a `ThreadFactory` that names them.

### 3.4 Daemon vs Non-Daemon Threads

The JVM shuts down when **all non-daemon threads have terminated**. Daemon threads are automatically killed when the JVM decides to exit, without waiting for them to finish.

```java
Thread background = new Thread(() -> {
    while (true) {
        performBackgroundTask();
        Thread.sleep(1000);
    }
});
background.setDaemon(true);   // MUST be before .start()
background.start();

// When main() finishes, the JVM exits,
// killing background even if it's in the middle of a task
```

**Rule of thumb:**
- Use non-daemon (default) for threads doing work that must complete (writing files, sending transactions)
- Use daemon for threads doing housekeeping that can be safely interrupted (log flushing, cache cleanup, heartbeats)

Threads in `Executors.newFixedThreadPool()` are **non-daemon** by default. This is why you must call `executor.shutdown()` to allow the JVM to exit — otherwise those threads keep the JVM alive forever.

---

## 4. Runnable vs Extending Thread

### 4.1 The Two Approaches

**Approach 1: Extending Thread**

```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}

new MyThread().start();
```

**Approach 2: Implementing Runnable (preferred)**

```java
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}

new Thread(new MyTask()).start();

// Or with a lambda (Java 8+)
new Thread(() -> System.out.println("Running")).start();
```

### 4.2 Why Runnable is Almost Always Correct

**Reason 1: Java single inheritance.** If your class extends `Thread`, it cannot extend any other class. This is a significant constraint in large systems where your task class might need to extend a framework class (`HttpServlet`, `AbstractService`, etc.).

**Reason 2: IS-A vs HAS-A.** Object-oriented design principle: "Favor composition over inheritance." Your class does not *IS-A* thread. It HAS-A behavior that can be run on a thread. `implements Runnable` models this correctly; `extends Thread` does not.

**Reason 3: Separation of concerns.** A `Runnable` separates the *task definition* from the *execution mechanism*. The same `Runnable` can run on a bare `Thread`, a thread pool, a `ScheduledExecutorService`, or inside a `ForkJoinPool`. A class that extends `Thread` is permanently coupled to the Thread execution model.

**Reason 4: Lambda compatibility.** `Runnable` is a functional interface (exactly one abstract method). This means any lambda expression is automatically a `Runnable`. `extends Thread` has no such property.

**Reason 5: No thread instance state leakage.** When you extend `Thread`, your class inherits dozens of thread-management methods (`start()`, `stop()`, `interrupt()`, `getState()`, etc.). Users of your class can call these methods directly, which is rarely intended.

```java
// With extends Thread — callers can do this:
MyThread t = new MyThread();
t.start();
t.setPriority(10);      // they can manipulate thread config
t.setName("hacked");   // they can rename your thread
t.stop();              // deprecated and dangerous

// With Runnable — the Thread object is internal:
Thread t = new Thread(new MyTask());
t.start();
// Caller doesn't need a reference to Thread at all
```

### 4.3 When Extending Thread Is Acceptable

The only time extending `Thread` makes sense is when you genuinely need to override Thread behavior itself — for example, building a custom thread class that provides specialized interrupt handling or monitoring. In practice, this is extremely rare and almost always better served by a `ThreadFactory`.

---

## 5. The synchronized Keyword — Deep Dive

### 5.1 What Is a Monitor?

Every Java object — every single one — has an associated **monitor** (also called an intrinsic lock or object lock). This is not a separate object; it is part of the object header in the JVM heap.

The monitor is a mutual exclusion mechanism. At any moment, at most one thread can own a given monitor. When a thread owns a monitor, all other threads trying to acquire it are moved to BLOCKED state and wait.

### 5.2 The Three Forms of `synchronized`

**Form 1: Synchronized instance method**

```java
public class Counter {
    private int count = 0;
    
    // Lock is: this instance
    public synchronized void increment() {
        count++;  // only one thread at a time
    }
    
    public synchronized int getCount() {
        return count;
    }
}
```

The lock acquired is the `this` reference — the specific instance being called on. Two different `Counter` instances have two different monitors, so they don't block each other.

**Form 2: Synchronized static method**

```java
public class Registry {
    private static int totalInstances = 0;
    
    // Lock is: Registry.class (the Class object)
    public static synchronized void register() {
        totalInstances++;
    }
}
```

The lock acquired is the `Class` object (`Registry.class`). There is exactly one `Class` object per class per JVM, so this is a class-level lock — much coarser than instance locks.

**Form 3: Synchronized block (most flexible and usually preferred)**

```java
public class OrderProcessor {
    private final Object lock = new Object();  // private dedicated lock
    private int processedCount = 0;
    
    public void process(Order order) {
        // Non-critical: do this outside the lock
        validateOrder(order);
        
        synchronized (lock) {
            // Only critical section is guarded
            processedCount++;
            updateDatabase(order);
        }
        
        // Non-critical: do this outside the lock
        sendConfirmationEmail(order);
    }
}
```

### 5.3 What synchronized Guarantees

**Atomicity:** The code inside the synchronized block executes as a single indivisible unit from the perspective of other threads synchronizing on the same lock. No other thread can see an intermediate state.

**Visibility:** When a thread releases a monitor (exits synchronized block), all writes made while holding the lock are **flushed to main memory**. When a thread acquires a monitor (enters synchronized block), its cache is **invalidated and refreshed from main memory**. This guarantees that the entering thread sees the most up-to-date values written by any thread that previously held the lock.

**Ordering (happens-before):** The Java Memory Model (JMM) establishes a happens-before relationship: all actions performed before a monitor release are visible to any thread that subsequently acquires the same monitor. This prevents the compiler and CPU from reordering instructions in ways that break this guarantee.

**Mutual exclusion:** Only one thread holds a given monitor at a time. All others are BLOCKED.

### 5.4 synchronized is Reentrant

A thread that already holds a monitor can re-acquire it without blocking. The JVM tracks a **lock count** — each re-acquisition increments it, each release decrements it. The lock is released when the count reaches zero.

```java
public class ReentrantDemo {
    public synchronized void outer() {
        System.out.println("outer — holds the lock");
        inner();  // same thread re-acquires the same lock — safe
    }
    
    public synchronized void inner() {
        System.out.println("inner — still holds the lock (count = 2)");
    }
}
// Without reentrancy, calling outer() would deadlock because
// inner() would block waiting for the lock that outer() holds.
```

### 5.5 The Private Lock Object Pattern

Using `this` as the lock is dangerous because external code can acquire `this` too:

```java
// DANGEROUS: external code can do this
Counter c = new Counter();
synchronized (c) {  // acquires the same lock as c.increment()
    // now increment() will block until this synchronized block exits
    doSomethingLong();
}
```

The safe pattern is to use a **private final lock object**:

```java
public class SafeCounter {
    private final Object lock = new Object();  // private, nobody outside can acquire this
    private int count = 0;
    
    public void increment() {
        synchronized (lock) {
            count++;
        }
    }
}
```

### 5.6 Deadlock — How It Happens and How to Prevent It

A deadlock occurs when:
1. Thread A holds lock-1, wants lock-2
2. Thread B holds lock-2, wants lock-1
3. Both wait forever

```java
Object lockA = new Object();
Object lockB = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lockA) {
        Thread.sleep(100);      // give t2 time to grab lockB
        synchronized (lockB) { // WAITS for lockB
            doWork();
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lockB) {
        synchronized (lockA) { // WAITS for lockA → DEADLOCK
            doWork();
        }
    }
});
```

**Prevention Rule 1: Consistent lock ordering.** Always acquire locks in the same global order. If every thread acquires lockA before lockB, deadlock is structurally impossible.

**Prevention Rule 2: Use `tryLock()` with timeout.** `ReentrantLock.tryLock(timeout)` allows you to give up if a lock cannot be acquired, preventing indefinite waiting.

**Prevention Rule 3: Minimize lock scope.** The less code runs while holding a lock, the less chance of deadlock.

**Prevention Rule 4: Avoid holding multiple locks.** When possible, restructure code to require only one lock at a time.

### 5.7 Performance Implications

Every `synchronized` operation involves:
- A memory fence (flush writes, invalidate cache)
- A CAS (compare-and-swap) instruction to acquire/release the lock
- Potential context switching if the lock is contended

Modern JVMs use several optimizations:

**Biased locking:** If only one thread ever uses a lock, the JVM biases the lock toward that thread, making subsequent acquisitions nearly free (no CAS needed). Removed in Java 15+.

**Lock coarsening:** The JIT compiler merges multiple consecutive synchronized blocks into one larger block, reducing lock/unlock overhead.

**Lock elision:** If the JIT determines that a lock object cannot escape (is only accessible to one thread), it eliminates the lock entirely through escape analysis.

```java
// The JIT can eliminate this synchronization
// because sb is local and cannot be accessed by other threads
public String buildLocal() {
    StringBuffer sb = new StringBuffer();  // synchronized internally
    sb.append("a");
    sb.append("b");     // JIT may elide all locks on sb
    sb.append("c");
    return sb.toString();
}
```

---

## 6. The volatile Keyword — CPU Cache & Memory Barriers

### 6.1 The Problem volatile Solves

Every CPU core has its own L1 and L2 cache. When your code reads a variable, the CPU fetches it from RAM into its cache. Subsequent reads are served from cache — not from RAM. This is excellent for performance but creates a problem: if another core modifies the variable in its cache, your core's cache is stale.

```
// Thread-1 (Core-1):
running = false;  // writes to Core-1's cache
                  // may NOT immediately propagate to RAM

// Thread-2 (Core-2):
while (running) { // reads from Core-2's cache
    doWork();     // sees running = true forever!
}
```

This is not a bug in the code logic. It is a consequence of CPU hardware design. Without `volatile`, the JVM and CPU are free to:
1. Cache variable values in CPU registers indefinitely
2. Reorder memory reads and writes for performance
3. Let different threads see different versions of the same variable

### 6.2 What volatile Guarantees

**Visibility guarantee:** Every write to a `volatile` variable is immediately written through to main memory. Every read of a `volatile` variable is fetched directly from main memory, bypassing the CPU cache. This ensures all threads always see the latest written value.

**Ordering guarantee (memory barrier):** `volatile` inserts a **memory barrier** at the point of read/write. A write barrier prevents reordering of writes before it with writes after it. A read barrier prevents reordering of reads after it with reads before it.

```java
class Worker implements Runnable {
    private volatile boolean running = true;  // volatile!
    
    public void run() {
        while (running) {  // reads from main memory every iteration
            doWork();
        }
        // Reaches here after stopWorker() is called
        cleanup();
    }
    
    public void stopWorker() {
        running = false;  // write goes directly to main memory
    }
}
```

### 6.3 What volatile Does NOT Guarantee

**volatile does not guarantee atomicity.** Atomicity means "this operation is indivisible." `volatile` only guarantees that each individual read and write is atomic (for primitive types and references). It does NOT make compound operations atomic.

```java
volatile int counter = 0;

// Two threads running this concurrently:
counter++;

// This is NOT atomic. It compiles to:
// 1. READ  counter from main memory  (atomic due to volatile)
// 2. ADD 1 to the local copy
// 3. WRITE result back to main memory (atomic due to volatile)
//
// Steps 1-3 are NOT atomic as a unit.
// Thread 1 and Thread 2 can both read counter=5,
// both compute 6, both write 6.
// Expected: 7. Actual: 6. Lost an increment.
```

For atomic compound operations, use `AtomicInteger`, `AtomicLong`, `AtomicReference`, or `synchronized`.

### 6.4 The Double-Checked Locking Pattern (Singleton)

This is a famous use of `volatile` that catches many developers off guard:

```java
// BROKEN without volatile (pre-Java 5 style)
public class Singleton {
    private static Singleton instance;
    
    public static Singleton getInstance() {
        if (instance == null) {            // check 1 (no lock)
            synchronized (Singleton.class) {
                if (instance == null) {    // check 2 (with lock)
                    instance = new Singleton();  // PROBLEM HERE
                }
            }
        }
        return instance;
    }
}
```

The problem: `instance = new Singleton()` is not a single operation. It is three steps:
1. Allocate memory
2. Initialize the object (run constructor)
3. Assign the reference to `instance`

The JVM can reorder steps 2 and 3. Another thread might see `instance != null` after step 3 but before step 2 — and use an uninitialized object.

```java
// CORRECT: volatile prevents the reordering
public class Singleton {
    private static volatile Singleton instance;  // volatile!
    
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

`volatile` on `instance` inserts a memory barrier that prevents steps 2 and 3 from being reordered. The object is fully initialized before the reference becomes visible to other threads.

### 6.5 volatile vs synchronized — When to Use Which

| Need | Use |
|---|---|
| Only need visibility (simple flag) | `volatile` |
| Need atomicity on compound ops | `synchronized` or `AtomicXxx` |
| Need mutual exclusion | `synchronized` or `ReentrantLock` |
| Lazy initialization (singleton) | `volatile` + double-checked locking |
| Counter increments | `AtomicInteger` or `synchronized` |
| Multiple related variables | `synchronized` (ensures all-or-nothing update) |

---

## 7. wait() and notify() — Inter-Thread Coordination

### 7.1 The Problem They Solve

Synchronization primitives like `synchronized` control **access** to shared resources. But sometimes you need threads to **coordinate** — one thread needs to pause until some condition is true, while another thread creates that condition.

The polling approach is terrible:

```java
// TERRIBLE — wastes CPU, unpredictable latency, burns power
while (!messageAvailable()) {
    Thread.sleep(10);  // spin-wait: checking every 10ms
}
processMessage();
```

`wait()` and `notify()` provide an efficient alternative: the waiting thread is completely suspended (zero CPU), and is woken up precisely when the condition changes.

### 7.2 The Three Iron Rules

1. **Must be called from within a `synchronized` block** on the same object. Calling `wait()` outside `synchronized` throws `IllegalMonitorStateException` at runtime.

2. **`wait()` atomically releases the monitor and suspends the thread.** These two operations happen as one — there is no window where the lock is released but the thread isn't yet in the wait set.

3. **After being notified, the thread must re-acquire the monitor before running.** This means it goes to BLOCKED first, not directly to RUNNABLE.

### 7.3 Producer-Consumer — The Canonical Example

```java
public class BoundedBuffer<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    
    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
    }
    
    public synchronized void produce(T item) throws InterruptedException {
        while (queue.size() == capacity) {  // buffer full
            wait();  // releases lock, suspends this thread
        }
        queue.offer(item);
        notifyAll();  // wake up any waiting consumers
    }
    
    public synchronized T consume() throws InterruptedException {
        while (queue.isEmpty()) {  // buffer empty
            wait();  // releases lock, suspends this thread
        }
        T item = queue.poll();
        notifyAll();  // wake up any waiting producers
        return item;
    }
}
```

### 7.4 The WAITING → BLOCKED → RUNNABLE Sequence

When `notifyAll()` is called:

```
Thread is WAITING
    │
    │  notifyAll() is called
    ▼
Thread is BLOCKED (competing to re-acquire the monitor)
    │
    │  Thread wins the lock
    ▼
Thread is RUNNABLE
    │
    │  Resumes from exactly where wait() was called
    ▼
while (!condition) check executes again
```

This sequence — going BLOCKED before RUNNABLE — is why the `while` loop is mandatory. Multiple threads may have been waiting. All are notified. They all compete for the lock. The first one to win might consume the available item. The second one wakes up, checks the condition, finds the item is gone, and calls `wait()` again.

### 7.5 notify() vs notifyAll()

**`notify()`:** Wakes up one arbitrary thread from the wait set. The JVM chooses which one — there is no guarantee of fairness or order.

**`notifyAll()`:** Wakes up all threads in the wait set. Each thread then competes for the lock. Each re-checks its condition. Those whose condition is not met go back to waiting.

**When is `notify()` safe to use?**
- All waiting threads are waiting for the same condition
- Satisfying the condition enables exactly one thread to proceed
- All waiting threads run identical code

**When is `notifyAll()` necessary?**
- Different threads wait for different conditions (e.g., some wait for "buffer not full", others wait for "buffer not empty")
- Using `notify()` might wake the wrong thread, causing all threads to wait indefinitely (missed signal)

**General advice:** Default to `notifyAll()`. The performance difference is usually negligible, and it's always correct.

---

## 8. wait() vs sleep() — The Critical Difference

This is one of the most common Java interview questions. The differences are deep and consequential.

### 8.1 Side-by-Side Comparison

| Property | `Object.wait()` | `Thread.sleep()` |
|---|---|---|
| Defined in | `Object` | `Thread` |
| **Releases monitor lock?** | **YES** | **NO** |
| Requires synchronized block? | YES — throws `IllegalMonitorStateException` otherwise | No restriction |
| How to wake early | `notify()` or `notifyAll()` | `interrupt()` (throws `InterruptedException`) |
| Thread state | WAITING or TIMED_WAITING | TIMED_WAITING |
| Use case | Inter-thread coordination | Simple time delay |
| Interrupt behavior | Throws `InterruptedException`, clears flag | Throws `InterruptedException`, clears flag |

### 8.2 The Lock Behavior in Detail

```java
Object lock = new Object();

// Example 1: sleep() inside synchronized — lock NOT released
Thread t1 = new Thread(() -> {
    synchronized (lock) {
        System.out.println("t1 sleeping — holding the lock");
        Thread.sleep(5000);  // t2 CANNOT enter for 5 seconds
        System.out.println("t1 done sleeping");
    }
});

// Example 2: wait() inside synchronized — lock IS released
Thread t2 = new Thread(() -> {
    synchronized (lock) {
        System.out.println("t2 waiting — lock released");
        lock.wait();  // lock released immediately; another thread can enter
        System.out.println("t2 was notified");
    }
});
```

**Practical consequence:** If you use `sleep()` inside a synchronized block, all other threads trying to enter that block are blocked for the entire sleep duration. This reduces concurrency dramatically. Use `wait()` when you need to pause but want other threads to proceed.

### 8.3 Where Each Method Lives

`Thread.sleep()` is a static method on `Thread` — it always affects the *currently executing thread*, regardless of which reference you call it on.

```java
Thread t = new Thread(() -> doWork());
t.sleep(1000);  // WARNING: this sleeps the CURRENT thread, not t!
                // IntelliJ will warn you about this
Thread.sleep(1000);  // correct — explicit about sleeping current thread
```

`Object.wait()` is an instance method on `Object`. You call it on the object whose monitor you hold.

```java
synchronized (lock) {
    lock.wait();   // wait on lock's monitor — correct
    this.wait();   // wait on this object's monitor — requires synchronized (this)
}
```

# wait() and notify() — Interview Questions

---

## Q1. Why is the `while` loop mandatory with `wait()` and not `if`?

This is the question that separates people who read about `wait()` from people who have debugged it at 2am.

Using `if` means you check the condition once, get notified, and assume the condition is still true. That assumption is wrong in practice.

Here is why.

When `notifyAll()` is called, every waiting thread wakes up. All of them compete for the lock. The first thread wins, checks the condition, finds it true, proceeds, and consumes the resource. Now the second thread wins the lock. The condition is no longer true. But if you used `if`, you skip the check entirely and proceed anyway. You just introduced a bug.

This is called a spurious wakeup. The JVM is also allowed to wake up a waiting thread for no reason at all. It is in the Java specification. If you use `if`, a spurious wakeup will send your thread past the condition check with no resource available.

The `while` loop solves both problems. Every time a thread wakes up, it re-checks the condition. If the condition is not met, it calls `wait()` again and goes back to sleep.

The pattern is always this:
```java
while (!condition) {
    wait();
}
```

Never use `if`. Always `while`.

This one mistake causes some of the hardest bugs to reproduce in concurrent Java code because they only show up under specific thread scheduling conditions in production.

---

## Q2. What happens if you call `wait()` outside a synchronized block?

It throws `IllegalMonitorStateException` at runtime. Not a compile error. A runtime crash.

But the deeper interview answer is explaining why this rule exists.

`wait()` does two things atomically. It releases the monitor lock and suspends the thread. These two operations happen as a single indivisible unit. There is no gap between releasing the lock and entering the wait set.

If `wait()` were allowed outside a synchronized block, you would not hold the lock in the first place. There would be nothing to release. The atomicity guarantee would be meaningless.

Here is the race condition that would happen without this rule:

1. Thread A checks the condition. Condition is false.
2. Thread A is about to call `wait()`.
3. Before it does, Thread B runs, changes the condition to true, and calls `notify()`.
4. Now Thread A calls `wait()`. It missed the notification. It waits forever.

This is called a **missed signal** and it is a deadlock.

The `synchronized` block prevents this. Thread A holds the lock while checking the condition and while calling `wait()`. Thread B cannot call `notify()` until Thread A has released the lock, which only happens inside `wait()` after Thread A is already in the wait set. The signal cannot be missed.

This is why all three iron rules exist together. They are not arbitrary restrictions. They prevent a specific class of deadlocks.

---

## Q3. What is the difference between `notify()` and `notifyAll()`?

`notify()` wakes up one arbitrary thread from the wait set. The JVM decides which one. You have no control. No fairness. No ordering guarantee.

`notifyAll()` wakes up every thread in the wait set. Each one competes for the lock. Each one re-checks its condition. The ones whose condition is not met go back to waiting.

**`notify()` is only safe when all three conditions are true:**
- All waiting threads are waiting for the same condition
- Satisfying that condition enables exactly one thread to proceed
- All waiting threads run identical code

The producer-consumer pattern violates this. Some threads wait because the buffer is full. Other threads wait because the buffer is empty. These are two different conditions.

If a producer calls `notify()` and it accidentally wakes another producer instead of a consumer, that producer checks the buffer, finds it still full, and calls `wait()` again. The consumer that could have proceeded is still sleeping. Eventually all threads are waiting and none can proceed. That is a **deadlock caused by a missed signal**.

`notifyAll()` solves this. Every thread wakes up. Every thread checks its own condition. The right thread proceeds.

> **Default rule: Always use `notifyAll()`. It is always correct. `notify()` is an optimization you apply only when you can prove all three conditions above are met.**

---

## Q4. What is the difference between `wait()` and `sleep()`?

| Property | `Object.wait()` | `Thread.sleep()` |
|---|---|---|
| Defined in | `Object` | `Thread` |
| Releases monitor lock? | YES | NO |
| Requires `synchronized` block? | YES | No restriction |
| How to wake early | `notify()` or `notifyAll()` | `interrupt()` |
| Thread state | `WAITING` or `TIMED_WAITING` | `TIMED_WAITING` |
| Use case | Inter-thread coordination | Simple time delay |

**The most important difference:**

`wait()` releases the monitor lock while the thread is suspended. `sleep()` does not release any lock. It holds every lock it acquired and just pauses execution. If another thread needs that lock while this thread is sleeping, it blocks.

This is how `sleep()` inside a `synchronized` block causes accidental bottlenecks.

**The interview trap:**

People confuse them because both throw `InterruptedException` and both suspend a thread. The suspension looks the same from the outside. The behavior with respect to locks is completely different on the inside.

- Use `wait()` when a thread needs to pause until another thread creates a condition.
- Use `sleep()` when you just need a fixed time delay with no coordination involved.

---

## Q5. Walk me through the exact thread state transitions after `notifyAll()` is called.

This is a senior-level question. Most people get it wrong because they skip the `BLOCKED` state in the middle.

Here is the exact sequence:
```
Thread is WAITING
    │
    │  notifyAll() is called
    ▼
Thread is BLOCKED (competing to re-acquire the monitor)
    │
    │  Thread wins the lock
    ▼
Thread is RUNNABLE
    │
    │  Resumes from exactly where wait() was called
    ▼
while (!condition) check executes again
```

**Step by step:**

1. Thread is in `WAITING` state. It called `wait()` earlier, released the lock, and is fully suspended. Zero CPU usage.
2. `notifyAll()` is called. The thread moves out of the wait set. It does not go to `RUNNABLE`. It goes to `BLOCKED`. This is the step most people miss.
3. `BLOCKED` means the thread is alive and wants to run but cannot because it needs to re-acquire the monitor lock.
4. If multiple threads were waiting, all move to `BLOCKED` simultaneously. They all compete for the same lock. Only one wins.
5. The winner moves from `BLOCKED` to `RUNNABLE`. It resumes from exactly the line after `wait()` was called.
6. The first thing it does is re-check the `while` loop condition. Not proceed. Check first.
7. If the condition is not met, it calls `wait()` again and goes back to `WAITING`.
8. The losers stay in `BLOCKED` until the winner releases the lock. Then the cycle repeats.

> The path is always `WAITING` → `BLOCKED` → `RUNNABLE`. Never `WAITING` directly to `RUNNABLE`. The lock re-acquisition step is mandatory and it is not instant.
---

## 9. Thread.join() — Ordering Execution

### 9.1 What join() Does

`t.join()` causes the calling thread to wait until thread `t` has terminated (reached TERMINATED state).

**Internally:** `join()` is implemented using `wait()` on the `Thread` object itself. When a thread terminates, the JVM calls `notifyAll()` on that thread's object, waking up any thread blocked in `join()`.

```java
// Simplified implementation of join() inside Thread.java
public final void join() throws InterruptedException {
    synchronized (this) {
        while (isAlive()) {
            wait(0);  // wait on the Thread object's monitor
        }
    }
    // Thread is now TERMINATED
}
```

### 9.2 Parallel Work + Barrier

```java
// Classic fork-join without ForkJoinPool
int[] data = loadHugeDataset();
int mid = data.length / 2;

Thread t1 = new Thread(() -> sort(data, 0, mid));
Thread t2 = new Thread(() -> sort(data, mid, data.length));

t1.start();
t2.start();  // both run in parallel

t1.join();   // wait for t1
t2.join();   // wait for t2

// Now both halves are sorted — safe to merge
merge(data, mid);
```

### 9.3 join() with Timeout

```java
t1.join(5000);   // wait at most 5000 milliseconds

if (t1.isAlive()) {
    // t1 didn't finish in 5 seconds
    t1.interrupt();
    System.out.println("t1 timed out — interrupted it");
}
```

### 9.4 Thread State During join()

| Variant | Calling thread state |
|---|---|
| `t.join()` (no timeout) | WAITING |
| `t.join(millis)` | TIMED_WAITING |
| `t.join()` on NEW thread | Returns immediately (isAlive() = false) |
| `t.join()` on TERMINATED thread | Returns immediately |

### 9.5 Modern Alternatives

`Thread.join()` is a low-level primitive. In modern Java, prefer:
- `CompletableFuture.allOf(cf1, cf2, cf3).join()` — handles any number of async tasks
- `CountDownLatch.await()` — more flexible barrier
- `ExecutorService.invokeAll()` — submit all tasks, wait for all results

---

## 10. Runnable vs Callable

### 10.1 The Core Difference

Both `Runnable` and `Callable<V>` define a single-method interface for submitting tasks to threads or executors. The fundamental difference:

- **`Runnable.run()`** — returns `void`, cannot throw checked exceptions
- **`Callable<V>.call()`** — returns a value of type `V`, can throw any `Exception`

```java
@FunctionalInterface
public interface Runnable {
    void run();  // no return, no checked exceptions
}

@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;  // returns V, can throw
}
```

### 10.2 Practical Usage

```java
ExecutorService executor = Executors.newFixedThreadPool(4);

// Runnable — fire-and-forget
Runnable task = () -> System.out.println("done");
executor.execute(task);     // execute() accepts Runnable, returns void
executor.submit(task);      // submit() also accepts Runnable, returns Future<?>

// Callable — get a result back
Callable<Integer> computation = () -> {
    // can throw IOException, SQLException, anything
    return expensiveCalculation();
};
Future<Integer> future = executor.submit(computation);  // returns Future<V>
Integer result = future.get();  // blocks until result is ready
```

### 10.3 Using Callable with Thread (via FutureTask)

`Thread`'s constructor only accepts `Runnable`. To use `Callable` with a bare `Thread`, wrap it in `FutureTask<V>`, which implements both `Runnable` and `Future<V>`:

```java
Callable<String> callable = () -> {
    Thread.sleep(2000);
    return "result from callable";
};

FutureTask<String> futureTask = new FutureTask<>(callable);
Thread t = new Thread(futureTask);  // FutureTask is Runnable
t.start();

// Do other work...

String result = futureTask.get();   // FutureTask is Future<V>
System.out.println(result);         // "result from callable"
```

### 10.4 Exception Handling

With `Runnable`, unchecked exceptions propagate to the thread's `UncaughtExceptionHandler`. Checked exceptions cannot be thrown at all.

With `Callable`, any exception thrown by `call()` is caught and wrapped in an `ExecutionException`. You retrieve it from `future.get()`:

```java
Future<Integer> future = executor.submit(() -> {
    throw new IOException("DB connection failed");
});

try {
    future.get();
} catch (ExecutionException e) {
    Throwable cause = e.getCause();   // the original IOException
    System.out.println("Task failed: " + cause.getMessage());
}
```

---

## 11. How to Stop a Thread — The Right Way

### 11.1 Why Thread.stop() Is Deprecated and Dangerous

`Thread.stop()` was deprecated in Java 1.2 (1998) and removed in later versions. Here is exactly why it is dangerous:

When `Thread.stop()` is called, the JVM throws a `ThreadDeath` error at an arbitrary point in the target thread's execution stack. This has catastrophic consequences for shared state:

```java
synchronized (lock) {
    data[0] = newValue0;
    // Thread.stop() fires HERE
    data[1] = newValue1;  // never executes
}
// The lock IS released (stop() unwinds the stack)
// But data[0] is updated and data[1] is not
// The object is now in an inconsistent state
// Other threads will see this broken state
```

The lock is released because `ThreadDeath` is caught by `synchronized`'s cleanup, but the invariant of the data structure is violated. No amount of defensive coding can protect against this.

### 11.2 Approach 1: volatile Flag (Simple, Works When Not Blocking)

```java
class Worker implements Runnable {
    private volatile boolean running = true;
    
    @Override
    public void run() {
        while (running) {
            doWork();
        }
        // Guaranteed to reach here and clean up
        cleanUp();
        System.out.println("Worker stopped cleanly");
    }
    
    public void requestStop() {
        running = false;  // write visible to worker thread immediately
    }
}

Worker worker = new Worker();
Thread t = new Thread(worker, "my-worker");
t.start();

Thread.sleep(5000);
worker.requestStop();  // clean cooperative shutdown
t.join();              // wait for worker to actually finish
```

**Limitation:** If `doWork()` blocks (e.g., waiting for IO, sleeping, waiting on a lock), the flag won't be checked until the blocking call returns. The thread may not stop promptly.

### 11.3 Approach 2: Thread Interrupt Mechanism (Handles Blocking Calls)

`Thread.interrupt()` is the standard cooperative cancellation mechanism for threads that may block. It does two things:
1. Sets an **interrupt flag** on the target thread
2. If the thread is currently blocked in `sleep()`, `wait()`, `join()`, or any `InterruptibleChannel` operation, it throws `InterruptedException` immediately

```java
class InterruptibleWorker implements Runnable {
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                doWork();
                Thread.sleep(1000);  // InterruptedException thrown here if interrupted
            }
        } catch (InterruptedException e) {
            // Thread was interrupted while sleeping or waiting
            Thread.currentThread().interrupt();  // restore the flag
            // Fall through to cleanup
        }
        // Clean shutdown
        cleanUp();
    }
}

Thread t = new Thread(new InterruptibleWorker(), "interruptible-worker");
t.start();

Thread.sleep(5000);
t.interrupt();   // sets interrupt flag and/or throws InterruptedException
t.join();
```

**Why restore the interrupt flag?** When `InterruptedException` is caught, the interrupt flag is **cleared**. If you swallow the exception without restoring the flag, callers higher in the stack lose the information that an interrupt occurred. Always call `Thread.currentThread().interrupt()` in a catch block for `InterruptedException` unless you are genuinely handling the interrupt at that level.

### 11.4 Approach 3: ExecutorService.shutdown()

When using thread pools, you don't directly control individual threads. Instead:

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.execute(() -> longRunningTask());

// Graceful shutdown:
executor.shutdown();                          // stop accepting new tasks
executor.awaitTermination(10, TimeUnit.SECONDS);  // wait for running tasks

// Forceful shutdown (if tasks don't finish):
if (!executor.isTerminated()) {
    List<Runnable> dropped = executor.shutdownNow();  // sends interrupt to all threads
    System.out.println("Dropped tasks: " + dropped.size());
}
```

`shutdownNow()` sends `interrupt()` to all currently running threads — this is why writing interrupt-responsive code matters even in thread pools.

---

## 12. Thread Pools — Internals & Configuration

### 12.1 Why Thread Pools Exist

Creating a new thread is expensive. It involves:
- A system call to the OS kernel
- Allocating a native stack (512KB–1MB by default)
- Registering with the OS scheduler
- JVM-side bookkeeping

For a web server handling 1,000 requests/second, creating a thread per request means 1,000 thread creations per second — an enormous overhead. Thread pools solve this by maintaining a set of pre-created, reusable threads.

### 12.2 The Four Standard Factories

```java
// 1. Fixed Thread Pool
// Always exactly N threads. Tasks queue when all N are busy.
// Use for: CPU-bound work where you know the right parallelism
ExecutorService fixed = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);

// 2. Cached Thread Pool
// Creates threads on demand. Reuses idle threads.
// Idle threads die after 60 seconds.
// WARNING: can create unlimited threads → OOM under load
// Use for: short-lived async tasks in controlled environments
ExecutorService cached = Executors.newCachedThreadPool();

// 3. Single Thread Executor
// Exactly 1 thread. Tasks execute sequentially.
// If the thread dies, a new one is created automatically.
// Use for: serializing access to a resource
ExecutorService single = Executors.newSingleThreadExecutor();

// 4. Scheduled Thread Pool
// Supports delayed and periodic task execution
// Use for: scheduled jobs, heartbeats, timeouts
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.schedule(() -> runReport(), 1, TimeUnit.HOURS);
scheduled.scheduleAtFixedRate(() -> heartbeat(), 0, 30, TimeUnit.SECONDS);
```

### 12.3 ThreadPoolExecutor — The Full Constructor

All the factory methods above are convenience wrappers around `ThreadPoolExecutor`. For production use, always configure it directly:

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4,                                      // corePoolSize
    8,                                      // maximumPoolSize
    60L,                                    // keepAliveTime
    TimeUnit.SECONDS,                       // keepAliveTime unit
    new ArrayBlockingQueue<>(200),          // work queue (bounded!)
    new ThreadFactory() {                   // custom thread factory
        private final AtomicInteger count = new AtomicInteger(0);
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "worker-" + count.incrementAndGet());
            t.setDaemon(false);
            return t;
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy()  // rejection policy
);
```

### 12.4 Work Queue Types

The work queue choice dramatically affects behavior:

| Queue Type | Behavior | Risk |
|---|---|---|
| `ArrayBlockingQueue(n)` | Bounded — blocks at capacity | Safe, provides back-pressure |
| `LinkedBlockingQueue()` | Unbounded — never fills | OOM under load |
| `LinkedBlockingQueue(n)` | Bounded linked queue | Safe |
| `SynchronousQueue` | No storage — hands off directly | Creates threads immediately; needs large maxPoolSize |
| `PriorityBlockingQueue` | Priority-ordered | High-priority tasks always first |

### 12.5 The Four Rejection Policies

When both the queue and the thread pool are at capacity, the executor cannot accept new tasks. It applies a **rejection policy**:

```java
// AbortPolicy (default): throws RejectedExecutionException
new ThreadPoolExecutor.AbortPolicy()

// CallerRunsPolicy: calling thread runs the task itself
// Natural back-pressure — slows down the submitter
new ThreadPoolExecutor.CallerRunsPolicy()

// DiscardPolicy: silently drops the task
new ThreadPoolExecutor.DiscardPolicy()

// DiscardOldestPolicy: drops the oldest queued task, retries submission
new ThreadPoolExecutor.DiscardOldestPolicy()

// Custom: implement RejectedExecutionHandler
executor.setRejectedExecutionHandler((r, e) -> {
    log.warn("Task rejected: " + r);
    metrics.increment("tasks.rejected");
    // maybe put to a fallback queue, or log to disk
});
```

### 12.6 Thread Pool Sizing

**For CPU-bound tasks:**
```
corePoolSize = maxPoolSize = number of CPU cores
```
More threads than cores means more context switching with no benefit. Each thread is always doing useful work; you just need enough threads to keep every core busy.

```java
int cores = Runtime.getRuntime().availableProcessors();
ExecutorService cpuPool = Executors.newFixedThreadPool(cores);
```

**For IO-bound tasks:**
```
corePoolSize = cores × (1 + wait_time / compute_time)
```

If each thread spends 90% of its time waiting for IO and 10% computing, 10 threads can keep one core busy. On an 8-core machine: `8 × (1 + 0.9/0.1) = 8 × 10 = 80 threads`.

In practice: start with a reasonable estimate, then profile under realistic load and tune based on measurements. Never guess in production.

---

## 13. ThreadPoolTaskExecutor — corePoolSize vs maxPoolSize

This section covers Spring's `ThreadPoolTaskExecutor` which wraps `ThreadPoolExecutor`, but the algorithm applies to both.

### 13.1 The Counter-Intuitive Algorithm

Most developers assume the pool grows from core to max before queuing. The actual algorithm is different:

```
Task arrives
     │
     ▼
Active threads < corePoolSize?
     │ YES                      │ NO
     ▼                          ▼
Create new core thread    Queue not full?
                               │ YES              │ NO
                               ▼                  ▼
                          Enqueue task       Active < maxPoolSize?
                                                  │ YES              │ NO
                                                  ▼                  ▼
                                           Create temp thread   Apply rejection
                                                                   policy
```

**The key insight: tasks queue BEFORE additional threads are created.** A pool with `corePoolSize=4`, `maxPoolSize=8`, and an `ArrayBlockingQueue(100)`:
- Tasks 1–4: start core threads
- Tasks 5–104: queued (no new threads created yet!)
- Task 105: queue is full → create thread 5 (first non-core thread)
- Tasks 106–108: threads 6, 7, 8 created
- Task 109: 8 threads busy, queue full → rejection policy

### 13.2 Configuration in Spring

```java
@Bean
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(200);
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("my-service-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

### 13.3 Monitoring Your Thread Pool

```java
ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor.getThreadPoolExecutor();

System.out.println("Pool size:     " + tpe.getPoolSize());
System.out.println("Active:        " + tpe.getActiveCount());
System.out.println("Queue size:    " + tpe.getQueue().size());
System.out.println("Completed:     " + tpe.getCompletedTaskCount());
System.out.println("Total tasks:   " + tpe.getTaskCount());
```

Expose these as metrics (Micrometer/Prometheus) and alert on queue size approaching capacity.

---

## 14. java.util.concurrent — The Full Toolkit

Java 5 introduced the `java.util.concurrent` (JUC) package, which provides high-quality, battle-tested concurrency building blocks. Always prefer these over rolling your own with raw `synchronized`.

### 14.1 Locks

**ReentrantLock** — explicit lock with more features than `synchronized`:

```java
ReentrantLock lock = new ReentrantLock(true);  // true = fair (FIFO)

// Basic usage
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();  // MUST be in finally
}

// Non-blocking attempt
if (lock.tryLock()) {
    try { /* critical section */ } finally { lock.unlock(); }
} else {
    // lock not available — handle gracefully
}

// Timed attempt — avoids deadlock
if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
    try { /* critical section */ } finally { lock.unlock(); }
} else {
    // gave up after 100ms
}

// Interruptible — responds to Thread.interrupt()
lock.lockInterruptibly();
```

**ReadWriteLock** — allows concurrent reads, exclusive writes:

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

// Many threads can read simultaneously
public String read(String key) {
    readLock.lock();
    try {
        return cache.get(key);
    } finally {
        readLock.unlock();
    }
}

// Only one thread can write; all readers blocked during write
public void write(String key, String value) {
    writeLock.lock();
    try {
        cache.put(key, value);
    } finally {
        writeLock.unlock();
    }
}
```

**StampedLock** (Java 8+) — optimistic reads, even faster for read-heavy workloads:

```java
StampedLock lock = new StampedLock();

// Optimistic read — no lock acquired
long stamp = lock.tryOptimisticRead();
double x = this.x;
double y = this.y;
if (!lock.validate(stamp)) {
    // Someone wrote while we were reading — fall back to real read lock
    stamp = lock.readLock();
    try {
        x = this.x;
        y = this.y;
    } finally {
        lock.unlockRead(stamp);
    }
}
```

### 14.2 Atomic Variables

Atomic variables use **Compare-And-Swap (CAS)** — a single CPU instruction that atomically reads a value, compares it to an expected value, and writes a new value only if they match. No lock, no blocking — dramatically faster than `synchronized` for simple operations.

```java
AtomicInteger counter = new AtomicInteger(0);

counter.get();               // read
counter.set(10);             // write
counter.incrementAndGet();   // i++ (atomic)
counter.decrementAndGet();   // i-- (atomic)
counter.addAndGet(5);        // i += 5 (atomic)
counter.getAndAdd(5);        // get then add (atomic)

// CAS — only updates if current value == expected
boolean success = counter.compareAndSet(10, 20);
// If counter == 10, sets to 20, returns true
// If counter != 10, no-op, returns false

// Functional update (Java 8+)
counter.updateAndGet(x -> x * 2);  // double the current value atomically
```

```java
AtomicReference<String> ref = new AtomicReference<>("hello");
ref.compareAndSet("hello", "world");  // CAS on object references
```

### 14.3 Concurrent Collections

**ConcurrentHashMap** — thread-safe HashMap without a global lock. Uses segment-level locking (Java 7) or CAS/synchronized on individual buckets (Java 8+).

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("key", 1);
map.get("key");
map.computeIfAbsent("key", k -> expensiveLoad(k));  // atomic check-then-put
map.merge("key", 1, Integer::sum);                   // atomic read-modify-write
```

**CopyOnWriteArrayList** — thread-safe list where every write creates a new copy of the array. Reads never block. Writes are expensive. Ideal for lists that are rarely written and frequently read (e.g., event listener lists).

**ConcurrentLinkedQueue** — non-blocking, lock-free queue using CAS operations. Excellent for high-throughput producer-consumer scenarios.

**BlockingQueue** implementations — queues that block producers when full and consumers when empty:
- `ArrayBlockingQueue(n)` — bounded, array-backed
- `LinkedBlockingQueue(n)` — bounded, linked-list-backed
- `PriorityBlockingQueue` — ordered by priority
- `DelayQueue` — elements become available after a delay
- `SynchronousQueue` — no storage; each put must be matched by a take

### 14.4 Synchronizers

**CountDownLatch** — one-time barrier. N threads count down; main thread waits for zero:

```java
CountDownLatch latch = new CountDownLatch(3);

executor.submit(() -> { doWork(); latch.countDown(); });
executor.submit(() -> { doWork(); latch.countDown(); });
executor.submit(() -> { doWork(); latch.countDown(); });

latch.await();           // blocks until count = 0
latch.await(10, TimeUnit.SECONDS);  // with timeout
System.out.println("All workers done!");
```

**CyclicBarrier** — reusable barrier. All N threads wait for each other at a meeting point, then all proceed simultaneously:

```java
CyclicBarrier barrier = new CyclicBarrier(4, () -> {
    // optional: runs when all parties arrive
    System.out.println("All threads at barrier — proceeding");
});

for (int i = 0; i < 4; i++) {
    executor.submit(() -> {
        doPhase1();
        barrier.await();  // wait for all threads
        doPhase2();
        barrier.await();  // wait again (barrier is reusable)
        doPhase3();
    });
}
```

**Semaphore** — limits concurrent access to N permits. Useful for rate limiting and bounded resource pools:

```java
Semaphore semaphore = new Semaphore(3);  // max 3 concurrent

public void accessResource() throws InterruptedException {
    semaphore.acquire();  // blocks if all 3 permits taken
    try {
        useResource();
    } finally {
        semaphore.release();  // always release in finally
    }
}
```

---

## 15. Using a Mutex Object in Java

### 15.1 What Is a Mutex?

A **mutex** (mutual exclusion lock) is a synchronization primitive that allows only one thread at a time to execute a critical section. Any thread that tries to acquire a mutex already held by another thread is blocked until the holder releases it.

Java has no class literally named `Mutex`. Instead, Java gives you two mechanisms that implement mutex semantics:

1. **`synchronized` keyword** — implicit mutex backed by the object's monitor
2. **`ReentrantLock`** — explicit mutex with richer control
3. **`Semaphore(1)`** — a semaphore with one permit acts as a non-reentrant mutex

Understanding when to use each, and how they differ internally, is a key senior-level Java skill.

### 15.2 Mutex with `synchronized` (Implicit)

The simplest mutex in Java. Every object's monitor is a mutex. When a thread enters a `synchronized` block, it acquires the mutex. When it exits (normally or via exception), it releases it.

```java
public class AccountBalance {
    private double balance;
    private final Object lock = new Object(); // dedicated private mutex

    public void deposit(double amount) {
        synchronized (lock) {           // acquire mutex
            balance += amount;
            // only one thread here at a time
        }                               // release mutex — even on exception
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        synchronized (lock) {
            if (amount > balance) throw new InsufficientFundsException();
            balance -= amount;
        }
    }

    public double getBalance() {
        synchronized (lock) {
            return balance;
        }
    }
}
```

**Characteristics of `synchronized` as a mutex:**
- Reentrant — the same thread can acquire it multiple times without deadlocking
- Not fair — no FIFO guarantee on who gets the lock next
- Cannot be interrupted while waiting (unlike `ReentrantLock`)
- Cannot attempt to acquire with a timeout
- Always released — the JVM guarantees release on block exit

### 15.3 Mutex with `ReentrantLock` (Explicit)

`ReentrantLock` is the explicit mutex provided by `java.util.concurrent.locks`. It implements the same mutual exclusion as `synchronized` but exposes far more control.

```java
import java.util.concurrent.locks.ReentrantLock;

public class SafeCounter {
    private int count = 0;
    private final ReentrantLock mutex = new ReentrantLock(); // the mutex

    public void increment() {
        mutex.lock();           // acquire — blocks if another thread holds it
        try {
            count++;
        } finally {
            mutex.unlock();     // ALWAYS in finally — never forget this
        }
    }

    public int get() {
        mutex.lock();
        try {
            return count;
        } finally {
            mutex.unlock();
        }
    }
}
```

> **Critical rule:** `lock()` must always be paired with `unlock()` in a `finally` block. If the critical section throws an exception and you don't have `finally`, the mutex is never released and every other thread blocks forever — a deadlock.

### 15.4 tryLock() — Non-Blocking Mutex Acquisition

One of the most powerful features of `ReentrantLock` over `synchronized`: the ability to *attempt* to acquire the mutex and give up gracefully if it is not available.

```java
public class ResourceManager {
    private final ReentrantLock mutex = new ReentrantLock();

    // Attempt 1: Non-blocking — returns immediately
    public boolean tryProcess() {
        if (mutex.tryLock()) {          // returns true if acquired, false if not
            try {
                doProtectedWork();
                return true;
            } finally {
                mutex.unlock();
            }
        } else {
            System.out.println("Resource busy — skipping this cycle");
            return false;
        }
    }

    // Attempt 2: Timed — wait up to N milliseconds
    public boolean tryProcessWithTimeout() throws InterruptedException {
        if (mutex.tryLock(500, TimeUnit.MILLISECONDS)) {
            try {
                doProtectedWork();
                return true;
            } finally {
                mutex.unlock();
            }
        } else {
            System.out.println("Gave up after 500ms");
            return false;
        }
    }
}
```

**Why tryLock() matters for deadlock prevention:** If thread A holds mutex-1 and wants mutex-2, and thread B holds mutex-2 and wants mutex-1, a regular `lock()` causes deadlock. With `tryLock()`:

```java
// Deadlock-safe lock acquisition
public boolean acquireBoth(ReentrantLock m1, ReentrantLock m2)
        throws InterruptedException {
    while (true) {
        if (m1.tryLock()) {
            try {
                if (m2.tryLock()) {
                    return true; // acquired both — caller must release both
                }
            } finally {
                if (!m2.isHeldByCurrentThread()) m1.unlock();
            }
        }
        Thread.sleep(1); // back off before retrying
    }
}
```

### 15.5 lockInterruptibly() — Cancellable Mutex Acquisition

With `synchronized`, a thread waiting for a lock cannot be interrupted — it waits forever. `ReentrantLock.lockInterruptibly()` allows the waiting thread to respond to `Thread.interrupt()`:

```java
public class CancellableTask {
    private final ReentrantLock mutex = new ReentrantLock();

    public void runTask() throws InterruptedException {
        mutex.lockInterruptibly(); // throws InterruptedException if interrupted while waiting
        try {
            performCriticalWork();
        } finally {
            mutex.unlock();
        }
    }
}

// From another thread:
taskThread.interrupt(); // wakes the waiting thread in lockInterruptibly()
                        // → throws InterruptedException → thread can clean up
```

This is essential for building responsive, cancellable services. A thread blocked on `synchronized` is immune to interruption — you cannot cancel it without it eventually acquiring the lock.

### 15.6 Fair Mutex — Preventing Starvation

By default, both `synchronized` and `ReentrantLock` are **unfair** — any thread can "barge in" and acquire a lock even if other threads have been waiting longer. A fast-producing thread can repeatedly re-acquire the lock, starving slower threads indefinitely.

`ReentrantLock(true)` creates a **fair** mutex. Waiting threads are served in FIFO order — the thread that has been waiting longest is next.

```java
// Unfair mutex (default) — high throughput, possible starvation
ReentrantLock unfairMutex = new ReentrantLock();

// Fair mutex — guaranteed progress for all threads, lower throughput
ReentrantLock fairMutex = new ReentrantLock(true);

// Check fairness at runtime
System.out.println(fairMutex.isFair()); // true
```

**Trade-off:** Fair locks are significantly slower (2–10x in benchmarks) because the JVM must maintain a FIFO queue and cannot use the fast-path optimizations available with unfair locks. Use fairness only when starvation is a genuine observed problem, not as a default.

### 15.7 Mutex with `Semaphore(1)` — Non-Reentrant Mutex

A `Semaphore` initialized with 1 permit acts as a mutex. The key difference from `ReentrantLock`: a `Semaphore(1)` is **not reentrant** — if the same thread tries to acquire it twice, it deadlocks with itself.

```java
import java.util.concurrent.Semaphore;

public class SemaphoreMutex {
    private final Semaphore semaphore = new Semaphore(1); // 1 permit = mutex

    public void criticalSection() throws InterruptedException {
        semaphore.acquire();    // acquire the single permit
        try {
            doWork();
        } finally {
            semaphore.release(); // release the permit
        }
    }
}
```

**When `Semaphore(1)` over `ReentrantLock`?**
- The releasing thread can be different from the acquiring thread (Semaphore has no owner concept; ReentrantLock can only be released by the thread that locked it)
- You want a non-reentrant mutex intentionally (prevents accidental re-acquisition)
- Implementing "binary semaphore" semantics across thread handoffs

```java
// Example: one thread acquires, another releases
Semaphore handoff = new Semaphore(0); // starts at 0

Thread producer = new Thread(() -> {
    produce();
    handoff.release(); // signal consumer
});

Thread consumer = new Thread(() -> {
    handoff.acquire(); // wait for producer's signal
    consume();
});
```

### 15.8 ReentrantLock vs synchronized — Complete Comparison

| Feature | `synchronized` | `ReentrantLock` | `Semaphore(1)` |
|---|---|---|---|
| Implicit/Explicit | Implicit | Explicit | Explicit |
| Must unlock in `finally` | No (JVM handles it) | **Yes** | **Yes** |
| Reentrant | Yes | Yes | **No** |
| Fairness option | No | `new RLock(true)` | `new Semaphore(1, true)` |
| `tryLock()` | No | Yes | `tryAcquire()` |
| Timed acquisition | No | Yes | Yes |
| Interruptible wait | No | Yes (`lockInterruptibly`) | Yes (`acquireInterruptibly`) |
| Multiple `Condition`s | No (one wait set) | Yes (`newCondition()`) | No |
| Lock query methods | No | Yes (`getHoldCount()`, `isHeldByCurrentThread()`, etc.) | Yes |
| Different release thread | No | No | **Yes** |
| Performance (uncontended) | Faster (JIT-optimized) | Slightly slower | Slower |

### 15.9 Multiple Conditions — The Power of ReentrantLock

`synchronized` gives you one wait set per object — you can't distinguish "waiting for buffer not full" from "waiting for buffer not empty". `ReentrantLock` allows multiple `Condition` objects, each with its own wait set.

```java
public class BoundedBuffer<T> {
    private final ReentrantLock lock     = new ReentrantLock();
    private final Condition     notFull  = lock.newCondition();
    private final Condition     notEmpty = lock.newCondition();
    private final Object[]      items;
    private int head, tail, count;

    public BoundedBuffer(int capacity) {
        items = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();     // wait specifically for "not full"
            items[tail] = item;
            tail = (tail + 1) % items.length;
            count++;
            notEmpty.signal();       // wake only consumers — not other producers!
        } finally { lock.unlock(); }
    }

    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();    // wait specifically for "not empty"
            T item = (T) items[head];
            head = (head + 1) % items.length;
            count--;
            notFull.signal();        // wake only producers — not other consumers!
            return item;
        } finally { lock.unlock(); }
    }
}
```

With `synchronized`, you'd have to use `notifyAll()` to wake both producers and consumers, causing unnecessary wakeups. With separate `Condition`s, you wake exactly the right threads — a meaningful throughput improvement in high-contention scenarios.

### 15.10 Practical Guidelines — When to Use What

**Use `synchronized` when:**
- The critical section is simple and short
- You don't need tryLock, timeouts, or interruptible waiting
- You don't need multiple conditions
- You want the compiler to prevent mistakes (can't forget unlock)
- You're writing library code that needs to interoperate with `wait()`/`notify()`

**Use `ReentrantLock` when:**
- You need `tryLock()` to avoid deadlock
- You need a timed acquisition
- You need `lockInterruptibly()` for cancellable operations
- You need fairness to prevent starvation
- You need multiple `Condition` objects
- You need to query lock state (`getQueueLength()`, `isLocked()`, etc.)

**Use `Semaphore(1)` when:**
- The releasing thread can be different from the acquiring thread
- You need explicit non-reentrant semantics
- You are implementing a resource pool (use `Semaphore(n)` for n resources)

---

## 16. java.util.concurrent.Future

### 16.1 What Future Represents

`Future<V>` is a handle to an asynchronous computation. When you submit a `Callable` to an `ExecutorService`, you get a `Future<V>` back immediately. The computation runs in the background; you retrieve the result later via `get()`.

### 16.2 The Five Methods

```java
Future<Integer> future = executor.submit(() -> heavyComputation());

// 1. get() — blocks indefinitely until result is ready
Integer result = future.get();

// 2. get(timeout, unit) — blocks up to timeout; throws TimeoutException
Integer result = future.get(5, TimeUnit.SECONDS);

// 3. isDone() — non-blocking check (true if complete, cancelled, or failed)
if (future.isDone()) { ... }

// 4. cancel(mayInterruptIfRunning)
// true: send interrupt to the running thread
// false: only cancel if not yet started
boolean cancelled = future.cancel(true);

// 5. isCancelled()
if (future.isCancelled()) { ... }
```

### 16.3 Exception Handling

If the `Callable` throws an exception, `get()` wraps it in an `ExecutionException`:

```java
Future<String> future = executor.submit(() -> {
    throw new IOException("connection refused");
});

try {
    String result = future.get();
} catch (ExecutionException e) {
    Throwable original = e.getCause();  // the original IOException
    log.error("Task failed", original);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    // our thread was interrupted while waiting
} catch (TimeoutException e) {
    future.cancel(true);
    // took too long
}
```

### 16.4 Limitations of Future

`Future<V>` has significant limitations that `CompletableFuture` was designed to overcome:

1. **`get()` is blocking** — there is no callback mechanism. Your thread blocks while waiting.
2. **No chaining** — cannot say "when result is available, transform it and do X"
3. **No combining** — cannot easily wait for two futures and combine their results
4. **No manual completion** — you cannot complete a Future from outside the task
5. **No error recovery** — no way to provide a fallback value on failure without blocking

---

## 17. ThreadLocal — Per-Thread State

### 16.1 How ThreadLocal Works Internally

Each `Thread` object in Java has a field called `threadLocals` of type `ThreadLocal.ThreadLocalMap`. This is a specialized hash map where:
- Keys are `ThreadLocal` instances (stored as weak references)
- Values are the per-thread values

When you call `threadLocal.get()`, the JVM:
1. Gets `Thread.currentThread()`
2. Gets that thread's `threadLocals` map
3. Looks up `this` (the ThreadLocal instance) in the map
4. Returns the associated value (or calls `initialValue()` if not set)

When you call `threadLocal.set(value)`, the JVM:
1. Gets `Thread.currentThread()`
2. Gets (or creates) that thread's `threadLocals` map
3. Stores the entry with key=`this`, value=`value`

Because the map lives on the `Thread` object, different threads accessing the same `ThreadLocal` instance get completely independent values. There is no sharing, no synchronization needed.

```
ThreadLocal<User> userContext = new ThreadLocal<>();

Thread-1's ThreadLocalMap:          Thread-2's ThreadLocalMap:
  userContext → User("Alice")         userContext → User("Bob")

// Thread-1 calls userContext.get() → "Alice"
// Thread-2 calls userContext.get() → "Bob"
// Completely isolated, no synchronization
```

### 16.2 Classic Use Cases

**Non-thread-safe utilities per thread:**

```java
// SimpleDateFormat is not thread-safe
// Give each thread its own instance
private static final ThreadLocal<SimpleDateFormat> formatter =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

public String formatDate(Date date) {
    return formatter.get().format(date);  // this thread's formatter
}
```

**Per-request context in web servers:**

```java
public class RequestContext {
    private static final ThreadLocal<HttpRequest> request = new ThreadLocal<>();
    private static final ThreadLocal<User>        user    = new ThreadLocal<>();
    
    public static void setRequest(HttpRequest req) { request.set(req); }
    public static HttpRequest getRequest()         { return request.get(); }
    
    public static void setUser(User u) { user.set(u); }
    public static User getUser()       { return user.get(); }
    
    public static void clear() {
        request.remove();  // CRITICAL — prevent memory leaks
        user.remove();
    }
}

// In a servlet filter:
public void doFilter(HttpRequest req, HttpResponse res, FilterChain chain) {
    RequestContext.setRequest(req);
    RequestContext.setUser(authenticate(req));
    try {
        chain.doFilter(req, res);
    } finally {
        RequestContext.clear();  // ALWAYS clear in finally
    }
}
```

**Database transaction management (how Spring @Transactional works):**

Spring stores the current `Connection` in a `ThreadLocal`. All `@Transactional` methods on the same thread get the same connection (and thus participate in the same transaction), without passing the connection around explicitly.

### 16.3 The Memory Leak

In thread pools, threads are reused. A ThreadLocal set for request-1 on Thread-5 remains in Thread-5's `ThreadLocalMap` when Thread-5 picks up request-2. This has two problems:

1. **Stale data:** request-2 sees data from request-1
2. **Memory leak:** The value (e.g., a large object) is never GC'd as long as Thread-5 is alive — and thread pool threads live for the pool's lifetime

```
Thread-5's ThreadLocalMap after request-1 (no remove() called):
  userContext → User("Alice")   ← stale, holds User object in memory

Thread-5 now handles request-2:
  userContext.get() → User("Alice")  ← WRONG! Should be null or Bob
  User("Alice") object → LEAKED
```

**Prevention:** Always call `threadLocal.remove()` in a `finally` block in any code that runs on pooled threads.

### 16.4 InheritableThreadLocal

`InheritableThreadLocal<T>` copies the parent thread's value to the child thread at the time of `new Thread()`. Useful for propagating context to explicitly created threads:

```java
InheritableThreadLocal<String> traceId = new InheritableThreadLocal<>();
traceId.set("trace-123");

Thread child = new Thread(() -> {
    System.out.println(traceId.get());  // prints "trace-123"
});
child.start();
```

**Limitation with thread pools:** Thread pool threads are created once at pool startup, not once per task. The inheritance happens at thread creation, not at task submission — so `InheritableThreadLocal` does not propagate to thread pool threads as you might expect. For thread pools, use Spring's `TaskDecorator` or the open-source `TransmittableThreadLocal` library.

---

## 18. Asynchronous Programming — CompletableFuture

### 17.1 Why CompletableFuture

`Future<V>` forces you to block to get a result. `CompletableFuture<V>` allows you to define *what to do when the result is available*, without blocking anything.

It implements both `Future<V>` (for backward compatibility) and `CompletionStage<V>` (the reactive-style API).

### 17.2 Creating CompletableFutures

```java
// supplyAsync — runs in ForkJoinPool.commonPool() (or custom executor)
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> fetchData());

// With custom executor — always do this in production
CompletableFuture<String> cf = CompletableFuture.supplyAsync(
    () -> fetchData(),
    myExecutorService
);

// runAsync — for Runnable (no return value)
CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> doWork());

// completedFuture — already-done future (useful for testing)
CompletableFuture<String> cf = CompletableFuture.completedFuture("result");

// Manual completion
CompletableFuture<String> cf = new CompletableFuture<>();
cf.complete("done");           // signal completion with value
cf.completeExceptionally(ex);  // signal failure
```

### 17.3 Transformation Methods

```java
CompletableFuture.supplyAsync(() -> fetchUser(userId))
    
    // thenApply — transform result synchronously (like Stream.map)
    // runs in the completing thread
    .thenApply(user -> user.getEmail())
    
    // thenApplyAsync — transform result asynchronously
    // runs in a thread pool
    .thenApplyAsync(email -> validate(email), executor)
    
    // thenAccept — consume result, returns CompletableFuture<Void>
    .thenAccept(email -> sendWelcomeEmail(email))
    
    // thenRun — run action, ignores result
    .thenRun(() -> log.info("pipeline complete"));
```

### 17.4 Composing Async Pipelines

```java
// thenCompose — flatMap for CompletableFuture
// Use when the next step is itself async
CompletableFuture<Order> order =
    CompletableFuture.supplyAsync(() -> fetchUser(userId))
        .thenCompose(user -> fetchLatestOrder(user.getId()));
//                            ^ returns CompletableFuture<Order>
//  thenApply would give CompletableFuture<CompletableFuture<Order>> — wrong!

// thenCombine — combine two independent futures
CompletableFuture<User>  userFuture  = CompletableFuture.supplyAsync(() -> fetchUser(id));
CompletableFuture<Order> orderFuture = CompletableFuture.supplyAsync(() -> fetchOrder(id));

CompletableFuture<Dashboard> dashboard = userFuture.thenCombine(
    orderFuture,
    (user, order) -> new Dashboard(user, order)
);
// Both fetches run in parallel!
```

### 17.5 Combining Multiple Futures

```java
// allOf — wait for ALL futures to complete
CompletableFuture<Void> allDone = CompletableFuture.allOf(cf1, cf2, cf3);
allDone.join();  // block until all done

// Get all results after allOf
CompletableFuture.allOf(cf1, cf2, cf3)
    .thenRun(() -> {
        String r1 = cf1.join();  // join() won't block here — already done
        String r2 = cf2.join();
        String r3 = cf3.join();
        combine(r1, r2, r3);
    });

// anyOf — complete when ANY future completes
CompletableFuture<Object> fastest = CompletableFuture.anyOf(cf1, cf2, cf3);
Object result = fastest.join();
```

### 17.6 Error Handling

```java
CompletableFuture.supplyAsync(() -> riskyOperation())
    
    // exceptionally — recover from any exception, provide fallback
    .exceptionally(ex -> {
        log.error("Failed: {}", ex.getMessage());
        return "default value";
    })
    
    // handle — handles both success and failure
    .handle((result, ex) -> {
        if (ex != null) return "fallback";
        return result.toUpperCase();
    })
    
    // whenComplete — side effect on completion (doesn't transform)
    .whenComplete((result, ex) -> {
        if (ex != null) metrics.increment("errors");
        else metrics.increment("success");
    });
```

### 17.7 thenApply vs thenCompose — The Critical Distinction

```java
// thenApply: sync transform of the result
// returns: CompletableFuture<B> where B is the return type of the function
CompletableFuture<String> cf = fetchUserId()           // CF<Integer>
    .thenApply(id -> "user-" + id);                    // CF<String>

// thenCompose: async transform — the function returns a CF
// returns: CompletableFuture<B> (FLAT, not nested)
CompletableFuture<User> cf = fetchUserId()             // CF<Integer>
    .thenCompose(id -> fetchUser(id));                 // CF<User>
//                     ^ returns CF<User>

// WRONG — using thenApply when the function returns a CF
CompletableFuture<CompletableFuture<User>> cf = fetchUserId()
    .thenApply(id -> fetchUser(id));  // nested — hard to work with!
```

Think of it like `Stream.map()` vs `Stream.flatMap()`.

---

## 19. The Java Memory Model (JMM)

### 18.1 What the JMM Defines

The Java Memory Model is a specification (part of the Java Language Specification) that defines the rules under which values written by one thread become visible to other threads. Without the JMM, the compiler and CPU could optimize code in ways that make multi-threaded programs behave incorrectly.

### 18.2 The Happens-Before Relationship

The JMM defines visibility in terms of **happens-before**: if action A happens-before action B, then the effects of A are guaranteed to be visible to B.

Happens-before is established by:

1. **Program order:** Within a single thread, each action happens-before the next action in program order.

2. **Monitor lock:** Releasing a monitor happens-before acquiring that same monitor.

3. **volatile write:** A write to a volatile variable happens-before any subsequent read of that variable.

4. **Thread start:** `Thread.start()` on a thread happens-before any action in the started thread.

5. **Thread join:** All actions in a thread happen-before the return from `Thread.join()` on that thread.

6. **Transitivity:** If A happens-before B, and B happens-before C, then A happens-before C.

### 18.3 Instruction Reordering

Both the JIT compiler and the CPU are allowed to reorder instructions for performance, as long as they don't change the observable behavior **within a single thread**. Reordering can break multi-threaded code:

```java
// Thread 1:
value = 42;          // line A
initialized = true;  // line B — could be reordered before A!

// Thread 2:
if (initialized) {
    use(value);  // might see value = 0 if reordering occurred
}
```

`volatile` on `initialized` inserts a memory barrier that prevents this reordering. `synchronized` prevents reordering across the barrier as well.

### 18.4 Safe Publication

An object is **safely published** if it is made visible to other threads in a way that the JMM guarantees the object's state is fully initialized. Unsafe publication can expose partially-constructed objects.

```java
// UNSAFE — another thread might see partially constructed Foo
Foo foo;
foo = new Foo();  // three operations that can be reordered

// SAFE — final fields are guaranteed to be fully initialized before reference is visible
public class Foo {
    private final int x;
    public Foo(int x) { this.x = x; }
}

// SAFE — volatile write after construction
volatile Foo foo;
foo = new Foo();

// SAFE — static initialization (class loading guarantee)
public class Singleton {
    private static final Singleton INSTANCE = new Singleton();
    public static Singleton getInstance() { return INSTANCE; }
}
```

---

## 20. Common Concurrency Bugs

### 19.1 Race Condition

A race condition occurs when the program's behavior depends on the interleaving of operations from multiple threads, and some interleavings produce incorrect results.

```java
// BUG: check-then-act race condition
if (!map.containsKey(key)) {
    // Another thread might insert here!
    map.put(key, computeValue(key));  // now two threads insert
}

// FIX: atomic operation
map.computeIfAbsent(key, k -> computeValue(k));
```

### 19.2 Data Race

A data race is when two threads access the same variable concurrently and at least one is a write, with no synchronization.

```java
// BUG: data race on count
class Counter {
    private int count = 0;  // not volatile, not synchronized
    
    public void increment() { count++; }   // Thread 1 and Thread 2 concurrently
    public int get()        { return count; }
}

// FIX:
class Counter {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public void increment() { count.incrementAndGet(); }
    public int get()        { return count.get(); }
}
```

### 19.3 Deadlock

Two or more threads waiting for each other's locks. See [Section 5.6](#56-deadlock--how-it-happens-and-how-to-prevent-it) for details.

### 19.4 Livelock

Two threads are actively running (not blocked) but keep responding to each other in a way that prevents either from making progress.

```java
// Analogy: two people trying to pass in a hallway,
// each stepping aside in the same direction repeatedly

while (true) {
    if (canProceed()) break;
    // "step aside" — causes the other thread to see cannotProceed()
    Thread.yield();
}
```

### 19.5 Starvation

A thread cannot gain regular access to shared resources because other threads consume them continuously. `synchronized` with no fairness can cause starvation — use `ReentrantLock(true)` for fairness.

### 19.6 Thread Confinement

The safest form of concurrency is to avoid sharing — give each thread its own data. This is **thread confinement**:

- **Stack confinement:** Use local variables, which live on the thread's stack and are naturally confined.
- **ThreadLocal:** Explicit per-thread storage (see [Section 16](#16-threadlocal--per-thread-state)).
- **Ad-hoc confinement:** By convention — informal "this object belongs to that thread."

---

## 21. Interview Questions & Answers

### Core Concepts

**Q: What is the difference between a process and a thread?**

A process is an independent program instance with its own memory space, file descriptors, and OS resources. A thread is a lighter-weight unit of execution that exists within a process and shares that process's memory and resources with other threads. Creating a thread is ~100x cheaper than creating a process. Threads within a process can communicate via shared memory; processes must use IPC (pipes, sockets, shared memory segments).

---

**Q: What is the difference between BLOCKED and WAITING?**

BLOCKED means the thread attempted to enter a `synchronized` block and the monitor is held by another thread. It is involuntary — the thread wanted to proceed but couldn't.

WAITING means the thread explicitly called `wait()`, `join()`, or `park()` and is voluntarily suspended, waiting for a signal from another thread. A WAITING thread holds no lock. A BLOCKED thread is competing for one.

---

**Q: Does `Thread.sleep()` release the lock?**

No. `sleep()` suspends the thread and transitions it to TIMED_WAITING but retains all monitor locks it holds. Other threads trying to enter the same synchronized block remain BLOCKED for the entire sleep duration.

`wait()` does release the lock. This is the critical difference.

---

**Q: Is `i++` thread-safe?**

No. `i++` compiles to three separate operations: read `i`, add 1, write back. Two threads can read the same value concurrently, both compute the same result, and both write it — losing an increment. Use `AtomicInteger.incrementAndGet()` or `synchronized`.

---

**Q: What is a happens-before relationship?**

A guarantee from the JMM: if action A happens-before action B, then all memory writes performed by A and any action transitively before A are guaranteed to be visible to B. Established by: monitor lock/unlock, volatile read/write, thread start/join, and program order within a thread.

---

**Q: What is the difference between `notify()` and `notifyAll()`?**

`notify()` wakes one arbitrary thread from the wait set. `notifyAll()` wakes all of them. After being notified, all woken threads compete for the monitor; each re-checks its condition; those whose condition is not met go back to `wait()`.

Default to `notifyAll()` unless you are certain that only one thread can benefit from the notification and all waiting threads wait on the same condition.

---

**Q: Why must `wait()` always be called inside a `while` loop?**

Two reasons: (1) Spurious wakeups — the JVM may wake a thread without `notify()` being called (permitted by the JLS). (2) Condition can change — between the `notify()` and when the woken thread acquires the lock, another thread might have made the condition false again. The `while` loop re-checks the condition every time the thread wakes up.

---

**Q: What is the Java Memory Model?**

The JMM is a specification that defines when writes to shared memory become visible across threads. Without it, compilers and CPUs could optimize code in ways that break multi-threaded programs. The JMM defines happens-before relationships that programmers rely on — synchronized, volatile, thread start, thread join, and program order all establish happens-before.

---

### Synchronization

**Q: What is the difference between `volatile` and `synchronized`?**

`volatile` guarantees visibility (every read/write goes to/from main memory) and prevents reordering around the volatile access. It does NOT guarantee atomicity or mutual exclusion.

`synchronized` guarantees visibility, atomicity, ordering, AND mutual exclusion (only one thread at a time). It is heavier — involves lock acquisition/release overhead and potential blocking.

---

**Q: Can you use `volatile` on object references?**

Yes. A `volatile` reference guarantees that the reference itself is visible (all threads see the latest reference), but it does NOT make the object's fields volatile. If you write `volatile Foo foo` and another thread reads `foo.bar`, they see the latest `foo` reference, but `bar` might be stale if it hasn't been synchronized.

---

**Q: What is a monitor in Java?**

Every Java object has an associated monitor (intrinsic lock). It is a mutual exclusion mechanism — at most one thread can hold a monitor at a time. `synchronized` acquires and releases the monitor. `wait()`, `notify()`, and `notifyAll()` interact with the monitor's wait set.

---

**Q: What are the problems with using `this` as the lock?**

External code can acquire `this` via `synchronized(myObject)`, unexpectedly locking out other `synchronized` methods on that object. Use a `private final Object lock = new Object()` for encapsulation and safety.

---

### Thread Pools

**Q: What is the task submission algorithm for ThreadPoolExecutor?**

1. If active threads < corePoolSize → create new thread immediately
2. Else if queue not full → enqueue task
3. Else if active threads < maxPoolSize → create temporary thread
4. Else → apply rejection policy

The counter-intuitive part: tasks queue BEFORE extra threads are created.

---

**Q: Why is `Executors.newCachedThreadPool()` dangerous in production?**

It uses `Integer.MAX_VALUE` as maxPoolSize and a `SynchronousQueue` (no buffer). Every submitted task creates a new thread if no idle thread exists. Under high load, this creates thousands of threads, exhausting heap and causing OOM. Always use a bounded `ThreadPoolExecutor`.

---

**Q: What is `CallerRunsPolicy` and why is it useful?**

It runs rejected tasks on the calling thread — the thread that called `executor.submit()`. This provides back-pressure: if the pool is overwhelmed, producers slow down naturally because they're busy executing tasks themselves instead of submitting more. Prevents task loss without throwing exceptions.

---

### CompletableFuture

**Q: What is the difference between `thenApply` and `thenCompose`?**

`thenApply(fn)` is like `map` — the function takes a value and returns a new value. Result: `CF<B>`.

`thenCompose(fn)` is like `flatMap` — the function takes a value and returns a `CF<B>`. `thenCompose` flattens the nested future. Result: `CF<B>`, not `CF<CF<B>>`. Use `thenCompose` when the next step is itself asynchronous.

---

**Q: What thread does the callback in `thenApply` run on?**

By default, it runs on the thread that completes the previous stage (could be the pool thread that computed the result, or the calling thread if already complete). Use `thenApplyAsync(fn, executor)` to explicitly control which thread runs the callback.

---

**Q: What happens if you don't handle exceptions in a CompletableFuture chain?**

The exception propagates down the chain — all stages depending on the failed one are also completed exceptionally. If you call `join()` or `get()` at the end, the exception surfaces there as an `ExecutionException`. If you never call `get()` or `join()`, the exception is silently swallowed. Always attach `.exceptionally()` or `.handle()` to production pipelines.

---

### ThreadLocal

**Q: How does ThreadLocal cause a memory leak in thread pools?**

Thread pool threads live for the pool's lifetime. If a task sets a ThreadLocal value but doesn't call `remove()`, the value stays in the thread's `ThreadLocalMap` forever (until the thread dies). The ThreadLocalMap holds strong references to values, preventing GC. Large objects (connections, byte buffers, user sessions) can accumulate across millions of requests. Always call `threadLocal.remove()` in a `finally` block.

---

**Q: Does `InheritableThreadLocal` work with thread pools?**

No, not reliably. Thread pool threads are created once at pool startup, not per task. `InheritableThreadLocal` copies values at thread creation time — so values set by the submitting thread are NOT propagated to pool threads after the pool is created. Use Spring's `TaskDecorator` or `TransmittableThreadLocal` for context propagation to pool threads.

---

### Advanced

**Q: What is a CAS operation and why is it fast?**

Compare-And-Swap is a single CPU instruction (e.g., `LOCK CMPXCHG` on x86) that atomically reads a memory location, compares it to an expected value, and writes a new value only if they match. It is fast because it requires no OS-level lock, no thread parking, and no context switch. It is the foundation of `AtomicInteger`, `AtomicReference`, and `ConcurrentHashMap`. The only downside is "ABA problem" and possible retry loops under high contention.

---

**Q: What is the ABA problem?**

A thread reads value A, then another thread changes it to B, then back to A. The first thread's CAS succeeds (it still sees A) — but the value has actually changed and back. For most use cases this is fine. For some (like lock-free stacks), it causes bugs. Solved with `AtomicStampedReference` — which includes a version counter alongside the value.

---

**Q: What is lock-free programming?**

Writing concurrent algorithms that do not use blocking locks. Instead they use CAS-based retry loops. If the CAS fails (another thread changed the value), the operation restarts. Lock-free algorithms guarantee system-wide progress even if individual threads spin. `AtomicInteger`, `ConcurrentLinkedQueue`, and `ConcurrentHashMap` are all lock-free internally.

---

**Q: What is the ForkJoinPool and when should you use it?**

`ForkJoinPool` is a thread pool optimized for divide-and-conquer recursive tasks. It uses **work-stealing**: idle threads steal tasks from the queues of busy threads, maximizing CPU utilization. `CompletableFuture.supplyAsync()` uses the common `ForkJoinPool` by default. Use it for CPU-intensive recursive tasks (parallel sort, parallel stream operations, recursive algorithms). For IO-bound tasks, use a regular `ThreadPoolExecutor` with a larger pool size.

---

## Quick Reference Cheat Sheet

```
Thread States:       NEW → RUNNABLE → {BLOCKED, WAITING, TIMED_WAITING} → TERMINATED
sleep() vs wait():   sleep = keeps lock | wait = releases lock
volatile:            visibility only, NOT atomicity
synchronized:        visibility + atomicity + mutual exclusion
i++ atomic?:         NO — use AtomicInteger.incrementAndGet()
notify vs notifyAll: notifyAll is safer; notify only if one thread benefits
wait() rule:         ALWAYS in a while loop, never if
Pool algorithm:      core threads → queue → extra threads → rejection
Stop a thread:       volatile flag or interrupt() — NEVER Thread.stop()
ThreadLocal leak:    ALWAYS call remove() in finally in pooled threads
thenApply vs Compose: thenApply = map | thenCompose = flatMap (async next step)
```

---

<div align="center">

**☕ Happy Coding. Write thread-safe, efficient, readable concurrent Java.**

*Found an error or want to add something? Contributions welcome.*

</div>
