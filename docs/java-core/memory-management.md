# Java Memory Management and Garbage Collection - Complete Deep Dive Guide

> A one-stop reference covering JVM memory architecture, reference types, heap structure, GC algorithms, modern collectors, JVM tuning flags, monitoring tools, and interview prep — from fundamentals to production-level knowledge.

---

## Table of Contents

1. [Why JVM Manages Memory](#1-why-jvm-manages-memory)
2. [Two Core Memory Areas - Stack and Heap](#2-two-core-memory-areas)
3. [Stack Memory in Depth](#3-stack-memory-in-depth)
4. [Heap Memory in Depth](#4-heap-memory-in-depth)
5. [String Pool](#5-string-pool)
6. [Metaspace (Non-Heap)](#6-metaspace-non-heap)
7. [Types of References in Java](#7-types-of-references-in-java)
8. [Heap Memory Structure - Generations](#8-heap-memory-structure---generations)
9. [Object Lifecycle and Promotion](#9-object-lifecycle-and-promotion)
10. [Garbage Collection Algorithms](#10-garbage-collection-algorithms)
11. [Minor GC vs Major GC vs Full GC](#11-minor-gc-vs-major-gc-vs-full-gc)
12. [Stop-The-World Pauses](#12-stop-the-world-pauses)
13. [Garbage Collector Types](#13-garbage-collector-types)
14. [Modern GCs - ZGC and Shenandoah](#14-modern-gcs---zgc-and-shenandoah)
15. [Choosing the Right GC](#15-choosing-the-right-gc)
16. [JVM Flags and Tuning](#16-jvm-flags-and-tuning)
17. [Monitoring and Profiling Tools](#17-monitoring-and-profiling-tools)
18. [Common Memory Errors](#18-common-memory-errors)
19. [Memory Leak Patterns](#19-memory-leak-patterns)
20. [Best Practices for Developers](#20-best-practices-for-developers)
21. [Interview Questions and Answers](#21-interview-questions-and-answers)
22. [Quick Reference Cheat Sheet](#22-quick-reference-cheat-sheet)

---

## 1. Why JVM Manages Memory

In languages like C and C++, developers manually allocate and free memory using `malloc()` and `free()`. This leads to:

- Dangling pointers (accessing freed memory)
- Memory leaks (never freeing memory)
- Double-free errors
- Buffer overflows

Java solves this with automatic memory management via the **JVM (Java Virtual Machine)**. The JVM:

- Allocates memory when objects are created
- Tracks which objects are still in use
- Automatically reclaims memory from unreachable objects (Garbage Collection)
- Prevents developers from directly manipulating memory addresses

The JVM divides the system RAM it gets into multiple regions, each serving a distinct purpose.

---

## 2. Two Core Memory Areas

When the JVM starts, it carves up its allocated RAM into two primary regions:

```
RAM (System Memory)
  |
  +--- Stack Memory (per thread, smaller, LIFO)
  |
  +--- Heap Memory (shared across all threads, larger)
```

| Feature | Stack | Heap |
|---|---|---|
| Scope | Per thread (private) | Shared across all threads |
| Size | Smaller (typically 256KB - 1MB per thread) | Larger (configured via -Xmx) |
| Storage | Primitives, references, method frames | Object instances |
| Lifecycle | Auto-destroyed when method returns | Managed by Garbage Collector |
| Access speed | Faster | Slower (but still fast) |
| Order | LIFO (Last-In, First-Out) | No specific order |
| Thread-safety | Thread-safe by default | Not thread-safe by default |
| Error | StackOverflowError | OutOfMemoryError |

---

## 3. Stack Memory in Depth

### What the Stack Holds

Each thread in a Java application gets its own private stack. The stack stores:

- **Stack Frames** - one frame is pushed for every method call
- Inside each frame:
  - Local primitive variables (int, boolean, double, char, etc.) with their actual values
  - References (pointers) to objects that live on the heap
  - The return address (where to continue after the method finishes)
  - Operand stack (used by the JVM bytecode interpreter internally)

### Stack Lifecycle Example

```java
public class StackDemo {

    public static void main(String[] args) {
        int a = 10;                    // 'a' (value 10) stored in main's stack frame
        Person p = new Person("Rahul"); // reference 'p' stored in stack, Person object on heap
        greet(p);                      // new stack frame created for greet()
    }

    public static void greet(Person person) {
        String msg = "Hello";          // 'msg' reference in greet's frame, String in String Pool
        System.out.println(msg + " " + person.getName());
        // when greet() returns, its entire stack frame is destroyed
        // 'msg' reference is gone, 'person' reference is gone
    }
}
```

**Execution flow:**

1. JVM starts, `main()` frame is pushed onto stack
2. Primitive `a = 10` is stored directly in the frame
3. `new Person("Rahul")` creates object on heap; reference address stored in frame
4. `greet(p)` is called - new frame pushed on top of `main()`'s frame
5. `greet()` finishes - its frame is popped (destroyed)
6. `main()` finishes - its frame is popped (destroyed)
7. The `Person` object on heap is now unreachable - eligible for GC

### StackOverflowError

This happens when the stack runs out of space, almost always due to infinite or very deep recursion:

```java
public static void infinite() {
    infinite(); // each call pushes a new frame; stack fills up
                // throws StackOverflowError
}
```

---

## 4. Heap Memory in Depth

The Heap is where all **object instances** live. Any time you use the `new` keyword, the object is created on the heap.

```java
Person p = new Person();
// 'p' (reference) --> Stack
// Person object --> Heap
```

### What Lives in the Heap

- All object instances created with `new`
- Arrays (even arrays of primitives - the array object is on the heap)
- String literals (in a dedicated String Pool area)
- Wrapper class instances (Integer, Boolean, Double, etc.)
- Collections (ArrayList, HashMap, etc.) and their contained objects

### What Does NOT Live in the Heap

- Local primitive variables (they live in the stack frame)
- References themselves (they live in the stack or inside other objects on the heap)
- Class metadata (that's in Metaspace)

---

## 5. String Pool

Java maintains a special area within the heap called the **String Pool** (also called String Intern Pool or String Literal Pool).

### Why Does It Exist?

Strings are the most commonly used object in Java. Without pooling, every string literal would create a new object, wasting memory massively.

```java
String s1 = "hello";   // created in String Pool
String s2 = "hello";   // reuses the same object from pool
String s3 = new String("hello"); // forced to create a new heap object OUTSIDE pool

System.out.println(s1 == s2);      // true  (same reference from pool)
System.out.println(s1 == s3);      // false (s3 is a new heap object)
System.out.println(s1.equals(s3)); // true  (same content)
```

### intern() Method

You can manually add a string to the pool:

```java
String s = new String("world").intern(); // now 's' points to the pooled version
```

### String Pool in Java 7+

Before Java 7, the String Pool was in PermGen (outside the regular heap). From Java 7 onwards, it was moved into the main heap, meaning it can be garbage collected when strings are no longer referenced.

---

## 6. Metaspace (Non-Heap)

Before Java 8, there was a memory region called **PermGen (Permanent Generation)** that stored class metadata. It had a fixed size and commonly caused `OutOfMemoryError: PermGen space`.

Java 8 replaced PermGen with **Metaspace**, which is outside the regular heap and lives in **native memory**.

### What Metaspace Stores

- Class definitions (bytecode structure, field names, method signatures)
- Static variables
- Constants (from constant pool)
- JIT-compiled code metadata

### Key Difference from PermGen

| Feature | PermGen (before Java 8) | Metaspace (Java 8+) |
|---|---|---|
| Location | Part of JVM heap | Native OS memory |
| Default size | Fixed (64MB by default) | Grows automatically |
| Common error | OutOfMemoryError: PermGen space | OutOfMemoryError: Metaspace |
| GC managed | Yes | Yes (when class is unloaded) |

### Metaspace Flags

```bash
-XX:MetaspaceSize=128m       # initial metaspace size
-XX:MaxMetaspaceSize=512m    # cap metaspace growth
```

Without `-XX:MaxMetaspaceSize`, Metaspace can grow unboundedly and consume all native memory.

---

## 7. Types of References in Java

Java provides four types of references, giving you control over how aggressively the GC can reclaim objects.

### 7.1 Strong Reference (default)

The standard way of referencing objects. An object with a strong reference is **never** garbage collected as long as any strong reference to it exists.

```java
Person p = new Person("Abhishek"); // strong reference
// p is NEVER GC'd while this reference is alive
p = null; // now GC can collect the Person object
```

### 7.2 Weak Reference

Created using `java.lang.ref.WeakReference`. The GC will collect the object **at the very next GC cycle**, regardless of whether memory is full or not.

```java
import java.lang.ref.WeakReference;

Person p = new Person("Rahul");
WeakReference<Person> weakRef = new WeakReference<>(p);

p = null; // remove strong reference
// Next GC run WILL collect the Person object
// weakRef.get() will return null after GC runs

System.gc(); // suggest GC (not guaranteed to run immediately)
System.out.println(weakRef.get()); // likely null
```

Use case: WeakHashMap - caches where entries should auto-expire when not used elsewhere.

### 7.3 Soft Reference

Created using `java.lang.ref.SoftReference`. The GC will collect the object **only if JVM is critically low on memory** (approaching OutOfMemoryError threshold).

```java
import java.lang.ref.SoftReference;

byte[] cache = new byte[1024 * 1024]; // 1MB cached data
SoftReference<byte[]> softRef = new SoftReference<>(cache);

cache = null; // remove strong reference
// GC will NOT collect this unless memory is critically low
// Good for memory-sensitive image caches, for example
```

Use case: In-memory caches where you want to use available memory but not cause OOM.

### 7.4 Phantom Reference

Created using `java.lang.ref.PhantomReference`. The weakest form. Used for **cleanup actions** before an object is fully reclaimed. You cannot retrieve the actual object via `get()` - it always returns null.

```java
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> phantomRef = new PhantomReference<>(new Object(), queue);
// Used to perform post-mortem cleanup (e.g., closing native resources)
```

Use case: Replacing `finalize()` for cleanup of native resources.

### Reference Summary Table

| Type | GC Behavior | Common Use Case |
|---|---|---|
| Strong | Never collected while reachable | All normal variables |
| Soft | Collected only on memory pressure | Memory-sensitive caches |
| Weak | Collected at next GC cycle | WeakHashMap, canonicalized mappings |
| Phantom | Object already dead; used for cleanup | Native resource cleanup |

---

## 8. Heap Memory Structure - Generations

The JVM heap is divided into **generations** based on the observation known as the **Weak Generational Hypothesis**:

> Most objects die young. A small fraction of objects survive for a long time.

Based on this, the heap is split so that short-lived and long-lived objects are managed separately with different GC strategies.

```
HEAP MEMORY
  |
  +--- Young Generation (new objects created here)
  |       |
  |       +--- Eden Space        (all new objects start here)
  |       +--- Survivor Space S0 (also called FromSpace)
  |       +--- Survivor Space S1 (also called ToSpace)
  |
  +--- Old Generation / Tenured Generation (long-lived objects)
  |
(Metaspace - outside heap, but part of JVM memory)
```

### Young Generation

- Where **all new objects** are first created (via `new`)
- Eden Space is where allocations happen
- S0 and S1 are two equal-size buffers used during Minor GC
- Only one of S0 or S1 is active at any time; the other is empty
- GC here is called **Minor GC** - happens frequently and quickly

### Old Generation (Tenured)

- Holds **objects that survived multiple Minor GC cycles**
- Much larger than Young Generation (by default, ~2/3 of total heap)
- GC here is called **Major GC** or **Full GC** - happens less often but takes longer
- Filling up Old Gen usually triggers a much more expensive collection

---

## 9. Object Lifecycle and Promotion

This is the complete journey of an object from birth to death:

```
Step 1: new Person() created
        --> Allocated in Eden Space

Step 2: Minor GC runs
        --> Eden scanned
        --> Unreachable objects deleted
        --> Surviving objects moved to S0 (age = 1)

Step 3: More objects created in Eden
        Minor GC runs again
        --> Eden survivors + S0 survivors moved to S1 (age = 2)
        --> S0 is cleared

Step 4: Next Minor GC
        --> Eden survivors + S1 survivors moved to S0 (age = 3)
        --> S1 is cleared

... (this ping-pong continues between S0 and S1)

Step N: Object age reaches threshold (default: 15 for most GCs)
        --> Object PROMOTED to Old Generation

Step N+1: Object becomes unreachable in Old Gen
          --> Collected during Major GC
```

### Object Age and Tenuring Threshold

Every object has an "age" counter stored in its object header. Each Minor GC cycle that the object survives increments its age. When age reaches the tenuring threshold, it gets promoted.

```bash
-XX:MaxTenuringThreshold=15   # default for most GCs; set to 1 to promote early
```

### Humongous Objects (G1 GC)

In G1 GC, objects larger than 50% of a heap region are called **Humongous objects**. They skip the Young Generation entirely and are allocated directly in dedicated Humongous Regions in the Old Generation area.

---

## 10. Garbage Collection Algorithms

### 10.1 Mark and Sweep

The foundation of almost all Java GC algorithms.

**Phase 1 - Mark:**
- GC starts from **GC Roots** (stack variables, static fields, JNI references)
- Traverses the entire object graph
- Marks all reachable objects as "live"
- Any object NOT marked is considered garbage

```
GC Roots --> Object A --> Object B --> Object C (all marked live)
                       --> Object D (marked live)
Orphan:     Object E (no reference from roots, not marked - GARBAGE)
```

**Phase 2 - Sweep:**
- Scans the heap
- Removes all unmarked (garbage) objects
- Reclaims their memory

**Problem with basic Mark-Sweep:** Creates memory fragmentation. Free space is scattered in small holes between surviving objects. Large object allocation may fail even if total free space is sufficient.

### 10.2 Mark, Sweep, and Compact

Adds a third phase after sweep:

**Phase 3 - Compact:**
- Shifts all surviving (live) objects to one end of the heap
- Creates a large contiguous free block at the other end
- Updates all references to point to new locations
- Eliminates fragmentation but takes longer

```
Before:  [Live][FREE][Live][FREE][FREE][Live][FREE]
After:   [Live][Live][Live][FREE][FREE][FREE][FREE]
```

### 10.3 Mark and Copy (Copying Collector)

Used in Young Generation (for Eden + Survivor spaces).

- Divides memory into two equal halves (e.g., S0 and S1)
- Only one half is active at a time
- During GC: copy all live objects from active half to inactive half
- Flip active/inactive
- The old half is now completely free (no sweep or compact needed)

This is very fast for Young Generation because most objects are dead (few live objects to copy).

---

## 11. Minor GC vs Major GC vs Full GC

### Minor GC

- Collects only the **Young Generation** (Eden + Survivor spaces)
- Very frequent (can happen every few seconds under load)
- Very fast (milliseconds)
- Always a Stop-the-World event, but brief
- Surviving objects are promoted to Old Generation over time

### Major GC (Old GC)

- Collects the **Old Generation**
- Much less frequent
- Much slower (can take hundreds of milliseconds to seconds)
- Always Stop-the-World
- Triggered when Old Gen runs out of space

### Full GC

- Collects **both Young + Old Generation + Metaspace**
- The most expensive GC event
- Can cause multi-second pauses in production
- Common triggers:
  - Explicit `System.gc()` call
  - Old Gen is nearly full (Allocation Failure)
  - Metaspace is full
  - Promotion failure (no room in Old Gen for promoted objects)
  - Heap dump request via tools

### Comparison Table

| GC Type | What it Cleans | Frequency | Duration | Pause |
|---|---|---|---|---|
| Minor GC | Young Generation | Frequent | Milliseconds | Yes (short) |
| Major GC | Old Generation | Occasional | Hundreds of ms | Yes (long) |
| Full GC | Entire heap + Metaspace | Rare | Seconds | Yes (longest) |

---

## 12. Stop-The-World Pauses

**"Stop-The-World" (STW)** means all application threads are paused while the GC does its work.

### Why is it necessary?

If the application keeps modifying the object graph while GC is traversing it, GC cannot reliably determine which objects are live. STW ensures a consistent snapshot.

### Why it matters

For most applications, occasional short STW pauses are fine. But for:

- High-frequency trading systems (sub-millisecond latency requirements)
- Real-time game servers
- REST APIs with SLA guarantees
- Large heap applications (GBs of heap means longer STW pauses)

...long STW pauses cause missed deadlines, timeout errors, and degraded user experience.

### How Modern GCs Reduce STW

Modern GCs (G1, ZGC, Shenandoah) move as much GC work as possible to **concurrent phases** (running alongside application threads), reducing STW to only the absolutely necessary operations.

---

## 13. Garbage Collector Types

### 13.1 Serial GC

- **Single-threaded** - uses one CPU for GC work
- **Stop-the-World** for all phases
- Simplest GC, lowest overhead when single-threaded
- Suitable for: small applications, single-core environments, embedded systems

```bash
-XX:+UseSerialGC
```

Behavior: Pause everything, one thread does all work, resume.

### 13.2 Parallel GC (Throughput GC)

- **Multi-threaded** - uses multiple CPU cores for GC work
- **Stop-the-World** but shorter pauses than Serial because multiple threads work simultaneously
- Default in Java 8
- Suitable for: batch jobs, ETL pipelines, CPU-intensive backend processing where throughput > latency

```bash
-XX:+UseParallelGC
-XX:ParallelGCThreads=8   # number of GC threads
```

Behavior: Pause everything, multiple threads do GC work in parallel, resume. Better throughput, still has pauses.

### 13.3 CMS (Concurrent Mark and Sweep) - Deprecated

- Aimed to reduce pause times by doing mark and sweep **concurrently** with application threads
- Only Stop-the-World for initial mark and final re-mark phases
- **Does NOT compact memory** - leads to fragmentation over time
- **Deprecated in Java 9, removed in Java 14**
- Replaced by G1 GC

```bash
-XX:+UseConcMarkSweepGC  # Java 8 only (deprecated)
```

Key weakness: Fragmentation eventually forces a Full GC with compaction, causing long pauses.

### 13.4 G1 GC (Garbage-First)

The **default GC since Java 9** (on systems with 2+ CPUs and 2+ GB heap).

**How G1 works differently:**

Instead of one large contiguous Young and Old generation, G1 divides the entire heap into many **equal-sized regions** (1MB to 32MB, up to 2048 regions). Each region is dynamically assigned a role (Eden, Survivor, Old, or Humongous) as needed.

```
Heap divided into N regions:
[E][E][S][O][O][H][E][O][S][O][E][FREE]...
 E=Eden  S=Survivor  O=Old  H=Humongous
```

**Garbage-First approach:** G1 tracks how much garbage is in each region. It prioritizes collecting regions with the most garbage first, maximizing memory reclaimed per unit of pause time.

**Concurrent marking:** G1 marks live objects concurrently with the application, then only pauses for evacuation (copying live objects out of collected regions).

```bash
-XX:+UseG1GC                       # enable G1 (default since Java 9)
-XX:MaxGCPauseMillis=200            # pause time goal (soft target, not guarantee)
-XX:G1HeapRegionSize=16m            # region size
-XX:G1NewSizePercent=5              # min Young Gen as % of heap
-XX:G1MaxNewSizePercent=60          # max Young Gen as % of heap
-XX:InitiatingHeapOccupancyPercent=45  # when to start concurrent marking
```

Suitable for: most enterprise applications, large heaps (4GB to 128GB), when you need balance of throughput and low pauses.

Limitation: Despite concurrent marking, evacuation is still STW. True sub-10ms pauses are not realistic with G1.

---

## 14. Modern GCs - ZGC and Shenandoah

### 14.1 ZGC (Z Garbage Collector)

Introduced in Java 11 (experimental), production-ready in Java 15, Generational ZGC available in Java 21+.

**Goal:** Sub-millisecond pause times regardless of heap size (even for terabyte-scale heaps).

**Key techniques:**

- **Colored pointers:** ZGC uses 64-bit pointers and repurposes bits to store GC metadata directly in the pointer itself (load state, relocation state, mark state). This allows ZGC to do most of its work concurrently.
- **Load barriers:** Small code injected at every object load that checks and fixes references on-the-fly as objects are being relocated.
- **Concurrent relocation:** Unlike G1 which pauses to move objects, ZGC relocates objects while the application is running.

```bash
-XX:+UseZGC
-XX:+ZGenerational   # Generational ZGC (Java 21+, better throughput)
```

Trade-offs: Higher CPU overhead (5-10% extra), requires more heap headroom (25-35% free space recommended). Not suitable for very small containers.

### 14.2 Shenandoah GC

Developed by Red Hat, available in OpenJDK 12+.

**Goal:** Consistent low pause times (under 10ms), even for large heaps.

**Key technique: Brooks Pointers (Forwarding Pointers)**
- Every object has an extra pointer field pointing to its current location
- When an object is relocated, GC atomically updates the Brooks pointer
- Application threads always follow the pointer to find the object
- This enables **concurrent compaction** without STW

```bash
-XX:+UseShenandoahGC
```

Trade-offs: 5-15% higher CPU than G1. Brooks pointer adds memory overhead per object.

### Modern GC Comparison

| GC | Default Since | Pause Goal | Best For | Overhead |
|---|---|---|---|---|
| Serial GC | Java 1 | Long (seconds) | Small/embedded apps | Minimal |
| Parallel GC | Java 8 | Moderate (100ms-1s) | Batch/throughput jobs | Low |
| G1 GC | Java 9 | Bounded (50-200ms) | Most enterprise apps | Moderate |
| ZGC | Java 15 | Sub-millisecond | Latency-critical, huge heaps | Higher CPU |
| Shenandoah | Java 12 | Under 10ms | Low-latency, large heaps | Higher CPU |

---

## 15. Choosing the Right GC

| Application Type | Recommended GC | Reason |
|---|---|---|
| Microservices / REST APIs | G1 GC or ZGC | Low pause, good throughput |
| Batch processing / ETL | Parallel GC | Maximum throughput, pauses OK |
| Real-time trading / gaming | ZGC or Shenandoah | Sub-ms pauses critical |
| Large heaps (>32GB) | G1 GC or ZGC | Region-based management handles large heaps |
| Small containers (<4GB) | G1 GC | ZGC needs headroom; G1 is memory-efficient |
| Single-core embedded | Serial GC | No overhead, simple |
| Stateful web sessions | G1 GC with tuned SurvivorRatio | Sessions are medium-lived, avoid early promotion |

---

## 16. JVM Flags and Tuning

### Heap Size

```bash
-Xms512m           # initial heap size (set same as Xmx to avoid resizing)
-Xmx4g             # maximum heap size
-Xmn512m           # size of Young Generation
```

### GC Selection

```bash
-XX:+UseSerialGC
-XX:+UseParallelGC
-XX:+UseG1GC
-XX:+UseZGC
-XX:+UseShenandoahGC
```

### G1 Tuning

```bash
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
-XX:G1NewSizePercent=5
-XX:G1MaxNewSizePercent=60
-XX:MaxTenuringThreshold=15
```

### GC Logging (Java 9+)

```bash
-Xlog:gc*:file=gc.log:time,uptime,level,tags
```

### Metaspace

```bash
-XX:MetaspaceSize=128m
-XX:MaxMetaspaceSize=512m
```

### Other Useful Flags

```bash
-XX:+DisableExplicitGC            # ignore System.gc() calls
-XX:+UseStringDeduplication       # G1 only; deduplicate String objects to save heap
-XX:+ParallelRefProcEnabled       # parallelize reference processing
-XX:SurvivorRatio=8               # Eden:Survivor ratio (8 means Eden = 8x each survivor)
```

---

## 17. Monitoring and Profiling Tools

### jstat (command-line, comes with JDK)

```bash
jps                          # list running Java processes with their PIDs
jstat -gc <pid> 2000         # GC stats every 2 seconds
jstat -gcutil <pid> 1000     # GC utilization as percentages
```

Output columns: S0C, S1C (Survivor capacities), EC (Eden capacity), OC (Old Gen capacity), EU, OU (utilization), YGC (Young GC count), YGCT (Young GC time), FGC (Full GC count), FGCT (Full GC time).

### jmap

```bash
jmap -heap <pid>             # print heap summary
jmap -histo <pid>            # object histogram (count and size by class)
jmap -dump:format=b,file=heap.hprof <pid>  # heap dump for analysis
```

### jconsole (GUI, JDK built-in)

```bash
jconsole
```

Visual dashboard showing: heap memory usage, GC activity, thread count, class loading, CPU usage. Good for quick monitoring during development.

### VisualVM (GUI)

More powerful than jconsole. Features: heap dump analysis, CPU and memory profiling, thread analysis, GC monitoring with graphs.

### Java Flight Recorder (JFR)

Low-overhead production profiler built into the JVM. Can record GC events, allocations, method profiling.

```bash
-XX:+FlightRecorder
-XX:StartFlightRecording=duration=60s,filename=recording.jfr
```

Analyze with JDK Mission Control (JMC).

---

## 18. Common Memory Errors

### OutOfMemoryError: Java heap space

The heap is full and GC cannot reclaim enough memory.

**Causes:**
- Memory leak (objects accumulating and never collected)
- Heap too small for the workload
- Unbounded caches (List/Map growing without bounds)

**Fix:** Analyze heap dump with VisualVM or Eclipse MAT to find what's eating memory.

### OutOfMemoryError: GC overhead limit exceeded

The JVM spent more than 98% of its time doing GC but recovered less than 2% of memory. Usually indicates a severe memory leak.

### OutOfMemoryError: Metaspace

Too many classes loaded; Metaspace is full.

**Causes:** Dynamic class generation (reflection, CGLIB, ASM), too many classloaders (common in application servers), classloader leak.

**Fix:** Set `-XX:MaxMetaspaceSize`, analyze with heap dump tools.

### StackOverflowError

Stack memory exhausted, almost always due to infinite recursion.

---

## 19. Memory Leak Patterns

Unlike C/C++, Java memory leaks are **logical** - the object is no longer needed by the application but is still referenced, so GC cannot collect it.

### Pattern 1: Static Collections

```java
public class Cache {
    private static final List<Object> items = new ArrayList<>();

    public static void add(Object item) {
        items.add(item); // NEVER removed; grows forever
    }
}
```

### Pattern 2: Listener / Observer Not Removed

```java
button.addActionListener(myListener);
// When button is removed but listener not: myListener stays referenced
// Fix: button.removeActionListener(myListener) before removing button
```

### Pattern 3: Unclosed Resources (pre-try-with-resources)

```java
// BAD
Connection conn = dataSource.getConnection();
// exception thrown, conn never closed - connection pool exhausted
```

```java
// GOOD - use try-with-resources
try (Connection conn = dataSource.getConnection()) {
    // auto-closed
}
```

### Pattern 4: Inner Class Holding Outer Class Reference

Non-static inner classes hold an implicit reference to their outer class. If the inner class is long-lived, the outer class cannot be GC'd.

```java
// BAD - anonymous inner class holds reference to Activity in Android, for example
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        // holds implicit reference to outer class
    }
}, 60000);
```

### Pattern 5: ThreadLocal not Removed

```java
ThreadLocal<Object> tl = new ThreadLocal<>();
tl.set(bigObject);
// Thread pool threads - thread never dies, ThreadLocal never cleaned
// Fix:
tl.remove(); // always call remove when done
```

---

## 20. Best Practices for Developers

### Object Creation

- Avoid creating unnecessary objects in tight loops
- Reuse StringBuilder instead of concatenating Strings with `+` in loops
- Use primitive types instead of wrapper classes where possible (int vs Integer)
- Use object pools for expensive-to-create objects (e.g., database connections, threads)

### References

- Nullify references you no longer need in long-lived objects
- Use WeakReference or SoftReference for caches
- Always remove event listeners before discarding UI components
- Always call `ThreadLocal.remove()` in thread pool environments

### Collections

- Initialize collections with expected capacity to avoid resizing
- Choose the right collection (LinkedList vs ArrayList, HashMap vs TreeMap)
- Use `Collections.unmodifiableList()` to prevent accidental retention

### Resources

- Always use try-with-resources for Closeable objects
- Close streams, connections, and files in finally blocks if not using try-with-resources

### GC-Friendly Code

- Prefer immutable objects (they tend to be short-lived and GC-friendly)
- Avoid keeping large objects in cache without eviction policy
- Profile before tuning - measure first, optimize second

---

## 21. Interview Questions and Answers

**Q: What is the difference between Stack and Heap memory in Java?**

Stack is thread-private and stores local primitives and object references. It operates in LIFO order and is auto-managed when methods return. Heap is shared across all threads and stores actual object instances, managed by the GC.

**Q: Where are String literals stored?**

In the String Pool, which is a special area within the Heap (moved to main heap in Java 7+). Identical string literals share the same object instance.

**Q: What is Metaspace and how does it differ from PermGen?**

Metaspace (Java 8+) stores class metadata, static variables, and constants in native memory outside the JVM heap. Unlike PermGen (which had a fixed default size of 64MB and was inside the JVM), Metaspace grows automatically and is limited only by available native memory (unless `-XX:MaxMetaspaceSize` is set).

**Q: Explain Weak, Soft, and Strong references.**

Strong references are default; objects with strong refs are never GC'd. Soft references are GC'd only under memory pressure (good for caches). Weak references are GC'd at the next GC cycle regardless of memory state (good for canonicalized mappings).

**Q: What is the Weak Generational Hypothesis?**

The empirical observation that most objects die young. This justifies the generational heap design - frequently collecting the smaller Young Generation (where most objects die quickly) is far more efficient than frequently collecting the entire heap.

**Q: What is a Minor GC vs Full GC?**

Minor GC collects only the Young Generation - fast (milliseconds), frequent. Full GC collects the entire heap including Old Generation and Metaspace - slow (potentially seconds), rare, and triggered by heap exhaustion or explicit `System.gc()`.

**Q: What is "Stop-The-World"?**

A phase where all application threads are paused so the GC can safely traverse and modify the object graph. Modern GCs minimize STW by doing as much work concurrently as possible.

**Q: Why does G1 GC divide the heap into regions?**

Regions allow G1 to: (1) collect the highest-garbage regions first (Garbage-First), (2) collect incrementally instead of the whole old gen at once, (3) flexibly assign regions to Young or Old generation as needed, and (4) support compaction without collecting the entire heap.

**Q: Can you force garbage collection in Java?**

You can suggest it via `System.gc()`, but the JVM is not obligated to run GC immediately. It is generally a bad practice in production code. You can disable it entirely with `-XX:+DisableExplicitGC`.

**Q: What causes an OutOfMemoryError: GC overhead limit exceeded?**

The JVM is spending over 98% of its time in GC and recovering less than 2% of heap. This almost always indicates a severe memory leak.

**Q: What is object promotion in Java?**

When an object survives enough Minor GC cycles (reaching the tenuring threshold, default 15), it is moved from the Young Generation (Survivor space) to the Old Generation. This is called promotion.

**Q: What is the difference between CMS and G1 GC?**

CMS does concurrent marking and sweeping but does NOT compact memory, leading to fragmentation that eventually causes expensive Full GCs. G1 does compact memory incrementally, avoids fragmentation, and has a configurable pause time goal. CMS is deprecated in Java 9 and removed in Java 14.

---

## 22. Quick Reference Cheat Sheet

```
JVM MEMORY AREAS
  Stack    - Thread-private | Primitives + References | LIFO | Auto-freed on method return
  Heap     - Shared | Object instances | GC-managed
  Metaspace - Native memory | Class metadata + Statics | Grows automatically

HEAP REGIONS
  Young Gen  -> Eden -> S0 -> S1 -> (promote after N cycles) -> Old Gen
  Old Gen    - Long-lived objects | Major GC
  Humongous  - Objects > 50% region size | Directly in Old Gen (G1)

REFERENCE TYPES (GC aggressiveness)
  Strong    - Never GC'd while reachable
  Soft      - GC only on memory pressure   --> use for caches
  Weak      - GC at next GC cycle          --> use for WeakHashMap
  Phantom   - Post-mortem cleanup only

GC ALGORITHMS
  Mark + Sweep          - Find live, delete dead; causes fragmentation
  Mark + Sweep + Compact - Same + pack survivors; no fragmentation; slower
  Copying               - Copy live to new space; fast for short-lived (Young Gen)

GC TYPES (modern Java)
  Serial GC     - Single-thread | Small apps
  Parallel GC   - Multi-thread STW | Throughput jobs | Default Java 8
  G1 GC         - Region-based | Concurrent mark | Default Java 9+ | Most apps
  ZGC           - Colored pointers | Concurrent relocation | Sub-ms pauses
  Shenandoah    - Brooks pointers | Concurrent compact | Sub-10ms pauses

KEY JVM FLAGS
  -Xms / -Xmx                       Heap min / max size
  -Xmn                              Young Gen size
  -XX:MaxTenuringThreshold=15       Promotion age threshold
  -XX:MaxGCPauseMillis=200          G1 pause goal
  -XX:MetaspaceSize / MaxMetaspaceSize
  -Xlog:gc*:file=gc.log:time,...    GC logging
  -XX:+DisableExplicitGC            Ignore System.gc()

MONITORING TOOLS
  jstat -gc <pid> 2000              CLI: live GC stats
  jmap -dump:format=b,file=h.hprof  Heap dump
  jconsole / VisualVM               GUI monitoring
  Java Flight Recorder (JFR)        Low-overhead production profiler

COMMON ERRORS
  OutOfMemoryError: Java heap space          Heap full (leak or too small)
  OutOfMemoryError: GC overhead limit exceeded  Severe memory leak
  OutOfMemoryError: Metaspace               Too many classes loaded
  StackOverflowError                        Infinite recursion
```

---

*Sources: Video transcript (Concept && Coding - Shreyansh), Datadog GC deep dive, DigitalOcean JVM memory guide, Oracle GC documentation, Andrew Baker's pauseless GC blog (Java 25), foojay.io GC workload guide (2026).*
