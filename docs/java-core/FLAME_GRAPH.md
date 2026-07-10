# 🔥 Fivetran Performance Engineering Interview — Flame Graph Prep Guide

> **One-stop guide** to crack the flame graph performance optimization round.
> Focus: identify bottlenecks, reason about them out loud, propose fixes.

---

## Table of Contents

1. [What is a Flame Graph?](#1-what-is-a-flame-graph)
2. [How to Read a Flame Graph — 5-Step Process](#2-how-to-read-a-flame-graph--5-step-process)
3. [Key Visual Patterns to Recognize](#3-key-visual-patterns-to-recognize)
4. [Common Bottleneck Types](#4-common-bottleneck-types)
   - [N+1 Query Problem](#41-n1-query-problem)
   - [Synchronous I/O Blocking](#42-synchronous-io-blocking)
   - [Lock / Monitor Contention](#43-lock--monitor-contention)
   - [Excessive GC / Object Allocation](#44-excessive-gc--object-allocation)
   - [Inefficient Serialization](#45-inefficient-serialization)
   - [Connection Pool Exhaustion](#46-connection-pool-exhaustion)
   - [Algorithmic Inefficiency (O(n²))](#47-algorithmic-inefficiency-on)
5. [Fivetran-Specific Context](#5-fivetran-specific-context)
6. [Java Profiling Tools](#6-java-profiling-tools)
7. [Interview Strategy & Phrases](#7-interview-strategy--phrases)
8. [Practice Q&A](#8-practice-qa)
9. [Practice Resources & Sites](#9-practice-resources--sites)
10. [Quick Cheat Sheet](#10-quick-cheat-sheet)

---

## 1. What is a Flame Graph?

A flame graph is a visualization of **sampled stack traces** from a profiler. It answers the question: *"Where is my program spending its time?"*

```
         ┌──────────────────────┐
  TOP    │  leaf function (CPU) │   ← Flat top = CPU burning here
         ├──────────────────────┤
         │   calleeB()          │
         ├──────────────────────┴──────────────┐
         │   calleeA()  ← WIDE = slow!         │
         ├─────────────────────────────────────┤
  BOTTOM │   main() / entry point              │
         └─────────────────────────────────────┘
         ←────────────── TIME (X-axis) ────────────────→
```

| Axis | Meaning |
|------|---------|
| **X-axis** | Proportion of total profiling samples (width = time spent). NOT chronological. |
| **Y-axis** | Call stack depth. Bottom = entry point, top = leaf (what CPU is doing right now). |
| **Width** | How much total time a function and its callees consumed. |
| **Color** | Usually random/warm — no semantic meaning by default (tool-dependent). |

**Golden rule: Wide + near the top = bottleneck. Wide + tall column = hot path.**

---

## 2. How to Read a Flame Graph — 5-Step Process

Use this framework out loud during the interview:

### Step 1 — Identify the Widest Frame
> "The widest frame here is `fetchRows()` consuming ~61% of samples."

Start by naming it. The widest box is where your optimization effort is.

### Step 2 — Trace Downward (Root Cause)
> "Tracing down the call stack from `fetchRows()`, I see it calls `jdbc.executeQuery()` which calls `SocketInputStream.read()`."

Follow the hot path to its root to understand the trigger context.

### Step 3 — Classify the Bottleneck
Ask yourself:

- Is there `SocketInputStream.read()` or `FileInputStream`? → **I/O bound**
- Is there `Object.wait()`, `LockSupport.park()`, `monitorenter`? → **Lock / thread contention**
- Is there GC thread, `Unsafe.park()`, heavy `Object.<init>`? → **Allocation / GC pressure**
- Is there tight CPU loop with no I/O or locks? → **CPU bound (algorithm)**
- Is there `HikariPool.getConnection()`? → **Connection pool exhaustion**

### Step 4 — Look for Repeated Patterns
Same 3–4 frame stack appearing many times = **hot loop**. The function is being called N times where it should run fewer times or be batched.

### Step 5 — Propose a Targeted Fix
Map classification → fix:

| Classification | Fix Direction |
|----------------|---------------|
| I/O bound | Async, batching, parallelism |
| Lock contention | ConcurrentHashMap, atomic ops, reduce critical section |
| GC pressure | Object reuse, pools, StringBuilder |
| CPU / algorithmic | Better data structure, memoization, vectorization |
| Connection pool | Tune pool size, reduce hold time |

---

## 3. Key Visual Patterns to Recognize

### Pattern A: Flat-Top Plateau
```
┌─────────────────────────────────┐  ← WIDE, nothing above
│  processRow() — 74%             │     CPU burning directly here
├─────────────────────────────────┤
│  pipeline.run()                 │
└─────────────────────────────────┘
```
**Meaning:** CPU is actively executing this function (not blocked). Look for tight loops, regex, sorting, or string operations inside.

### Pattern B: Hot Vertical Column (I/O Wait)
```
┌────────────┐
│ read()     │   ← thin but extends all the way up
├────────────┤
│ execute()  │
├────────────┤
│ fetch()    │   ← WIDE — most time here
├────────────┤
│ main()     │
└────────────┘
```
**Meaning:** Thread is blocked waiting for I/O. CPU graph looks thin, wall-clock is high.

### Pattern C: Repeated Stacks
```
[fetchUser][jdbc.query][socket.read]  ← appears 200 times side by side
```
**Meaning:** A DB call (or expensive op) is inside a loop — classic N+1.

### Pattern D: GC Frames Visible
```
┌─────────────────────────────────────────────┐
│  G1GC / ZGC / ParallelGC — stop-the-world  │  ← Wide GC frame
└─────────────────────────────────────────────┘
```
**Meaning:** Frequent full GC pauses. Look for allocation-heavy code in the frames just before.

---

## 4. Common Bottleneck Types

### 4.1 N+1 Query Problem

**Flame graph signal:** `jdbc.executeQuery()` or HTTP client call appearing wide, called from inside a loop function.

**The problem:**
```java
// BAD: N queries for N records
for (String id : recordIds) {
    Record r = db.query("SELECT * FROM records WHERE id = ?", id);
    process(r);
}
```

**The fix:**
```java
// GOOD: 1 query for N records
String placeholders = String.join(",", Collections.nCopies(recordIds.size(), "?"));
List<Record> records = db.query(
    "SELECT * FROM records WHERE id IN (" + placeholders + ")",
    recordIds.toArray()
);
records.forEach(this::process);
```

**At Fivetran scale:** When syncing records that reference foreign keys or metadata from another endpoint, batch those lookups into a single API call or SQL IN clause.

---

### 4.2 Synchronous I/O Blocking

**Flame graph signal:** Wide `SocketInputStream.read()`, `HttpURLConnection`, or REST client blocking on main thread. Low CPU%, high wall time.

**The problem:**
```java
// BAD: Sequential HTTP calls — each blocks the thread
for (String pageUrl : pages) {
    Response r = httpClient.get(pageUrl); // thread sleeps here
    process(r.body());
}
```

**The fix:**
```java
// GOOD: Parallel async fetches
List<CompletableFuture<Response>> futures = pages.stream()
    .map(url -> CompletableFuture.supplyAsync(() -> httpClient.get(url), executor))
    .collect(Collectors.toList());

List<Response> responses = futures.stream()
    .map(CompletableFuture::join)
    .collect(Collectors.toList());
```

**Key insight:** If each API call has 200ms latency and you have 50 pages, sequential = 10s. Parallel (10 threads) = 1s.

---

### 4.3 Lock / Monitor Contention

**Flame graph signal:** Wide frames showing `Object.wait()`, `LockSupport.park()`, `AbstractQueuedSynchronizer`, `monitorenter`. CPU is low but threads are all stalled.

**The problem:**
```java
// BAD: Global synchronized block — all threads queue here
private final Map<String, Long> counters = new HashMap<>();

public synchronized void increment(String key) {
    counters.merge(key, 1L, Long::sum);
}
```

**The fix:**
```java
// GOOD: Lock-free concurrent structure
private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();

public void increment(String key) {
    counters.computeIfAbsent(key, k -> new LongAdder()).increment();
}
```

**Striped locking alternative (Guava):**
```java
Striped<Lock> striped = Striped.lock(64);
Lock lock = striped.get(key);
lock.lock();
try { /* critical section */ } finally { lock.unlock(); }
```

---

### 4.4 Excessive GC / Object Allocation

**Flame graph signal:** GC thread visible in flame graph. Heavy `Object.<init>`, `Array.newInstance`, or `String` creation in hot path. Periodic throughput drops.

**The problem:**
```java
// BAD: Allocates new String per row — millions of short-lived objects
for (Row row : rows) {
    String csv = row.getCol1() + "," + row.getCol2() + "," + row.getCol3();
    writer.write(csv);
}
```

**The fix:**
```java
// GOOD: Reuse StringBuilder across iterations
StringBuilder sb = new StringBuilder(256);
for (Row row : rows) {
    sb.setLength(0); // reset without allocation
    sb.append(row.getCol1()).append(',')
      .append(row.getCol2()).append(',')
      .append(row.getCol3());
    writer.write(sb.toString());
}
```

**Pre-size collections:**
```java
// BAD: Default ArrayList grows and copies
List<Record> batch = new ArrayList<>();

// GOOD: Pre-size when count is known
List<Record> batch = new ArrayList<>(expectedBatchSize);
```

**GC tuning (JVM flags):**
```bash
# Use ZGC for low-pause GC (JDK 15+)
-XX:+UseZGC

# Increase Eden space to reduce minor GC frequency
-XX:NewRatio=2

# Enable GC logging to correlate with flame graph
-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=20m
```

---

### 4.5 Inefficient Serialization

**Flame graph signal:** Wide frames in `ObjectMapper.readValue()`, `JsonParser`, `ReflectionUtils.getFields()` — serialization consuming disproportionate CPU.

**The problem:**
```java
// BAD: Creates new ObjectMapper per record (triggers reflection scan each time)
public Row deserialize(String json) {
    return new ObjectMapper().readValue(json, Row.class); // NEVER do this
}
```

**The fix:**
```java
// GOOD: Singleton ObjectMapper (thread-safe for reads)
private static final ObjectMapper MAPPER = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

public Row deserialize(String json) {
    return MAPPER.readValue(json, Row.class);
}
```

**For very high volume — use streaming parser:**
```java
// Avoids building full object tree for large payloads
try (JsonParser parser = MAPPER.createParser(json)) {
    while (parser.nextToken() != null) {
        if ("fieldName".equals(parser.getCurrentName())) {
            parser.nextToken();
            String value = parser.getText();
        }
    }
}
```

**Longer-term:** Switch to Avro or Protobuf — binary format eliminates parsing overhead, reduces payload size ~3-5x.

---

### 4.6 Connection Pool Exhaustion

**Flame graph signal:** Wide frames in `HikariPool.getConnection()`, `PoolBase.newConnection()`, or connection timeout exceptions alongside flame data.

**The problem:** Too few connections in pool, or connections held too long (long transactions, missing finally block).

**The fix (HikariCP):**
```java
HikariConfig config = new HikariConfig();
// Formula: (CPU cores * 2) + number of disk spindles
config.setMaximumPoolSize(20);
config.setMinimumIdle(5);
config.setConnectionTimeout(3000);      // fail fast, don't queue forever
config.setIdleTimeout(600000);
config.setMaxLifetime(1800000);
config.setLeakDetectionThreshold(2000); // warn if connection held > 2s
```

**Always release in finally:**
```java
Connection conn = pool.getConnection();
try {
    // use connection
} finally {
    conn.close(); // returns to pool
}
// Better: use try-with-resources
try (Connection conn = pool.getConnection()) {
    // use connection — auto-released
}
```

---

### 4.7 Algorithmic Inefficiency O(n²)

**Flame graph signal:** Flat-top plateau (CPU burning) in a contains/search/sort method. Time grows super-linearly as data grows.

**The problem:**
```java
// BAD: List.contains() = O(n) per call = O(n²) total
List<String> seen = new ArrayList<>();
for (Record record : incomingRecords) {
    if (!seen.contains(record.getId())) { // O(n) scan each time
        seen.add(record.getId());
        process(record);
    }
}
```

**The fix:**
```java
// GOOD: HashSet.contains() = O(1) per call = O(n) total
Set<String> seen = new HashSet<>(incomingRecords.size());
for (Record record : incomingRecords) {
    if (seen.add(record.getId())) { // O(1), returns false if duplicate
        process(record);
    }
}
```

**At scale:** For 1M records: O(n²) ≈ 10^12 operations. O(n) ≈ 10^6 operations. The difference is hours vs milliseconds.

**Memory-constrained alternative — Bloom Filter:**
```java
// Probabilistic dedup — configurable false positive rate
BloomFilter<String> filter = BloomFilter.create(
    Funnels.stringFunnel(Charset.defaultCharset()),
    expectedInsertions,
    0.01 // 1% false positive rate
);
if (!filter.mightContain(id)) {
    filter.put(id);
    process(record);
}
```

---

## 5. Fivetran-Specific Context

Understanding what Fivetran does helps you predict which bottlenecks will appear:

| Fivetran Component | Likely Bottleneck |
|-------------------|-------------------|
| Source connector (API pagination) | Sync I/O, N+1 HTTP calls |
| Schema change detection | N+1 metadata queries |
| Data type mapping / coercion | Serialize overhead, reflection |
| Row transform pipeline | GC pressure, O(n²) dedup |
| Destination writer (warehouse) | Connection pool, batch size |
| Change Data Capture (CDC) | Lock contention on offset tracking |
| Incremental sync | N+1 cursor queries |

**Key principle at Fivetran:** Data moves between systems in large volumes. The bottleneck is almost always at the boundaries — reading from source, transforming, writing to destination. Throughput (records/second) matters more than individual call latency.

---

## 6. Java Profiling Tools

### async-profiler (Recommended for Production)
```bash
# Attach to running JVM (no restart needed)
./profiler.sh -d 30 -f flamegraph.html <PID>

# CPU profiling
./profiler.sh -e cpu -d 30 -f cpu.html <PID>

# Allocation profiling
./profiler.sh -e alloc -d 30 -f alloc.html <PID>

# Lock contention profiling
./profiler.sh -e lock -d 30 -f lock.html <PID>
```

### JFR — Java Flight Recorder (Built into JDK 11+)
```bash
# Start recording
jcmd <PID> JFR.start duration=60s filename=recording.jfr

# Convert to flamegraph with JMC or async-profiler
./profiler.sh -d 0 --jfrsync default -f jfr_flame.html <PID>
```

### JMH — Microbenchmarks (Before/After Validation)
```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class SerializationBenchmark {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String sampleJson;

    @Setup
    public void setup() { sampleJson = "{\"id\":\"123\",\"value\":\"test\"}"; }

    @Benchmark
    public Row parseWithCachedMapper() throws Exception {
        return MAPPER.readValue(sampleJson, Row.class);
    }

    @Benchmark
    public Row parseWithNewMapper() throws Exception {
        return new ObjectMapper().readValue(sampleJson, Row.class);
    }
}
```

### Tool Comparison

| Tool | Best For | Overhead | Prod Safe? |
|------|----------|----------|------------|
| async-profiler | CPU, alloc, locks, wall-clock | Very low | Yes |
| JFR | Always-on production monitoring | Very low | Yes |
| VisualVM | Local dev, GUI exploration | Medium | No |
| IntelliJ Profiler | IDE-integrated dev workflow | Low | No |
| JMH | Microbenchmark before/after | N/A | N/A |
| Arthas | Runtime diagnosis, hot-swap | Low | Yes |

---

## 7. Interview Strategy & Phrases

### Navigation (Most People Fail Here)
- **Zoom in:** Click a frame to focus on that subtree
- **Zoom out:** Click the breadcrumb at the top or press Escape
- **Search:** Use Ctrl+F or the search box to highlight all frames matching a function name (great for finding "all calls to ObjectMapper")
- **Reverse flame graph:** Some tools offer "icicle" view (top = entry, bottom = leaf) — recognize both

### Phrases That Signal Expertise

| Situation | Say This |
|-----------|----------|
| Starting analysis | "The widest frame is `X` at `Y%` of samples. Let me trace the call path downward." |
| I/O identified | "I see `SocketInputStream.read()` consuming most of this stack — this is synchronous I/O blocking. The thread is doing nothing useful while waiting." |
| Lock identified | "The presence of `LockSupport.park()` tells me threads are blocked on a monitor. This is contention, not CPU saturation." |
| Proposing fix | "My first fix would be X, which should reduce this from O(n²) to O(n). I'd validate with async-profiler before and after." |
| Being uncertain | "I'd want to correlate this flame graph with metrics — CPU%, thread count, GC pause time — to confirm my hypothesis." |
| Always | "What's the target throughput or latency SLO? That determines how far we need to optimize." |

### Mental Checklist During the Interview

```
[ ] Name the widest frame immediately
[ ] Trace it to the root cause
[ ] Classify: I/O / CPU / Lock / GC / Algorithm
[ ] Check for N+1 pattern (repeated stacks)
[ ] Propose fix with expected impact
[ ] Mention how you'd measure improvement
[ ] Ask about constraints (memory budget, API rate limits, SLO)
```

---

## 8. Practice Q&A

### Q1: Walk me through this flame graph.
> "I start by finding the widest frame — that's where most samples are concentrated. Then I trace downward to understand what triggered it. I classify the bottleneck as I/O, CPU, lock, or allocation. Then I propose a targeted fix and explain how I'd measure the improvement."

### Q2: You see 70% of time in `SocketInputStream.read()`. What do you do?
> "This is synchronous I/O — threads are sleeping waiting for network data. First I check: is this one large read or many small reads? If many small reads (N+1 API calls), I batch them. If the latency is fixed (network RTT), I add parallelism — CompletableFuture or reactive HTTP client. I'd expect throughput to scale linearly with parallelism up to the API rate limit."

### Q3: CPU is 90% but throughput is low. What does the flame graph show?
> "High CPU with low throughput usually means an algorithmic inefficiency — O(n²) or O(n log n) where O(n) is possible. I'd look for `contains()` on a List, nested loops, or `sort()` inside a hot path. The fix is typically a better data structure — HashSet for O(1) lookup, or pre-sorting."

### Q4: CPU is 15% but the service is slow.
> "Low CPU with slow service = threads are blocked, not working. I'd look for `park()`, `wait()`, or `monitorenter` in the flame graph. Could be lock contention, I/O wait, or rate limiting. Fix depends on root cause: lock = reduce critical section or use concurrent structures; I/O = add threads or go async."

### Q5: Same 3-frame stack appears 200 times side by side.
> "That's a hot loop — the same expensive operation is being called once per record instead of being batched. Classic N+1. I'd extract the call outside the loop and batch it. If it's a DB call, use an IN clause. If it's an API call, use the bulk endpoint or parallel fetches."

### Q6: What profiler would you use in production?
> "async-profiler — it's the best for Java in production. Very low overhead, bypasses safepoint bias so it gives accurate samples, and supports CPU, allocation, and lock profiling modes. It generates a flame graph HTML directly. For always-on monitoring, JFR is great since it's built into the JDK and has near-zero overhead."

---

## 9. Practice Resources & Sites

### Interactive Flame Graph Tools (Hands-On Practice)

| Resource | URL | What to Do |
|----------|-----|------------|
| **Speedscope** | https://speedscope.app | Upload any `.json`, `.collapsed`, `.jfr` profile. Practice zooming, searching, left-heavy/sandwich views |
| **Brendan Gregg's Flamegraph** | https://github.com/brendangregg/FlameGraph | The original tool. Practice generating from perf data, reading the SVG output |
| **async-profiler** | https://github.com/async-profiler/async-profiler | Run against your own Java app locally. Profile a Spring Boot app |
| **IntelliJ Profiler** | Built into IntelliJ IDEA Ultimate | Profile any local Java app with one click, generates flame graph |
| **Firefox Profiler** | https://profiler.firefox.com | Can load Java profiles, great UI for exploring |

### Sample Profiles to Practice On

| Resource | URL | Notes |
|----------|-----|-------|
| **async-profiler demos** | https://github.com/async-profiler/async-profiler/tree/master/test | Real Java profiles you can open in speedscope |
| **Brendan Gregg examples** | https://www.brendangregg.com/flamegraphs.html | CPU, memory, off-CPU flame graph examples with explanations |
| **JFR sample recordings** | https://github.com/flight-recorder/samples | Open in JDK Mission Control |

### Learning Resources

| Resource | URL | Type |
|----------|-----|------|
| **Brendan Gregg — Flame Graphs** | https://www.brendangregg.com/flamegraphs.html | Deep theory, the authoritative source |
| **async-profiler documentation** | https://github.com/async-profiler/async-profiler/wiki | Practical Java profiling guide |
| **JVM Anatomy Quarks** | https://shipilev.net/jvm/anatomy-quarks/ | JVM internals that explain what you see in profiles |
| **Aleksey Shipilev's talks** | https://shipilev.net | JVM performance deep dives |
| **Java Performance (book)** | Scott Oaks — O'Reilly | Comprehensive Java perf reference |
| **Systems Performance (book)** | Brendan Gregg — Addison-Wesley | Systems-level perf, flame graph theory |

### Video Courses & Talks

| Resource | URL | Focus |
|----------|-----|-------|
| **Brendan Gregg — FlameGraph talk (USENIX)** | https://www.youtube.com/watch?v=D53T1Ejig1Q | Original flame graph talk |
| **Nitsan Wakart — JVM profiling** | https://youtu.be/Yg6_ulhwLw0 | Java-specific profiling techniques |
| **Martin Thompson — Mechanical Sympathy** | https://mechanical-sympathy.blogspot.com | Low-latency Java performance |
| **Inside Java Podcast — Performance** | https://inside.java/podcast | JVM performance episodes |

### Java Performance Benchmarking Practice

| Resource | URL | Notes |
|----------|-----|-------|
| **JMH Samples** | https://github.com/openjdk/jmh/tree/master/jmh-samples | Official JMH examples — run and profile them |
| **Renaissance Benchmark Suite** | https://renaissance.dev | Modern JVM workloads, great for profiling |
| **DaCapo Benchmarks** | https://www.dacapobench.org | Real-world Java workloads |

### Online Coding Platforms (Algorithmic Optimization)

| Platform | URL | Focus |
|----------|-----|-------|
| **LeetCode** | https://leetcode.com | Practice replacing O(n²) with O(n log n) / O(n) — directly applicable |
| **HackerRank — Java** | https://hackerrank.com/domains/java | Java-specific performance problems |
| **Project Euler** | https://projecteuler.net | CPU-bound optimization problems |

---

## 10. Quick Cheat Sheet

### Flame Graph Reading Shortcuts

```
Wide frame near top      → Main bottleneck — start here
Flat top (plateau)       → CPU burning in this function
Tall thin column         → I/O wait or deep recursion  
park() / wait()          → Thread blocked (lock or I/O)
GC frames visible        → Allocation pressure
Same stack ×N            → Hot loop / N+1 pattern
```

### Classify → Fix Map

```
SocketInputStream.read()     → Async I/O / CompletableFuture / batch API calls
jdbc.executeQuery() in loop  → Batch query with IN clause
LockSupport.park()           → ConcurrentHashMap / AtomicLong / stripe locks
Object.<init> in hot path    → StringBuilder / object pooling / pre-allocation
new ObjectMapper() in loop   → Singleton mapper / binary format (Avro)
HikariPool.getConnection()   → Tune pool size / fix connection leaks
List.contains() in loop      → Replace with HashSet / BloomFilter
```

### Java Concurrent Utilities Quick Reference

```java
ConcurrentHashMap<K,V>       // Lock-free map
CopyOnWriteArrayList<E>      // Safe reads, rare writes
AtomicLong / LongAdder       // Atomic counter (LongAdder better under contention)
ReentrantReadWriteLock       // Many readers, one writer
Semaphore                    // Rate limiting / connection slots
CompletableFuture<T>         // Async composition
ForkJoinPool                 // Parallel divide-and-conquer
BlockingQueue<E>             // Producer-consumer handoff
```

### Key Metrics to Always Quote

| Metric | Why It Matters |
|--------|----------------|
| Throughput (records/sec) | Primary goal for a data pipeline |
| P99 latency | Tail latency matters at Fivetran scale |
| GC pause time & frequency | Correlates with throughput drops |
| CPU utilization % | Low CPU + slow = blocked threads |
| Thread pool queue depth | Backpressure indicator |
| DB connection wait time | Pool exhaustion signal |

---

## Appendix: Self-Practice Checklist

Before the interview, make sure you can:

- [ ] Open a flame graph in speedscope.app and zoom in/out fluently
- [ ] Name the top 3 widest frames in under 10 seconds
- [ ] Search for a specific function name across the flame graph
- [ ] Identify N+1, sync I/O, and lock contention just from function names
- [ ] Write the fix for each bottleneck type from memory
- [ ] Explain the difference between CPU flame graph and wall-clock (off-CPU) flame graph
- [ ] Use async-profiler to profile a local Java app and open the output
- [ ] Run a JMH benchmark on two approaches and compare throughput numbers

---

*Good luck Abhishek! You've got this. Remember: think out loud, classify before you fix, and always mention measurement.*
