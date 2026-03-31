# Redis — One Shot Interview Preparation Guide

> Complete reference for SDE-2 / Senior Engineer interviews. Covers internals, architecture, system design patterns, use cases, and 30+ interview questions with answers.

---

## Table of Contents

1. [What is Redis?](#1-what-is-redis)
2. [Why is Redis Fast?](#2-why-is-redis-fast)
3. [Redis Data Structures](#3-redis-data-structures)
4. [Persistence — RDB vs AOF](#4-persistence--rdb-vs-aof)
5. [Replication Architecture](#5-replication-architecture)
6. [Redis Sentinel](#6-redis-sentinel)
7. [Redis Cluster](#7-redis-cluster)
8. [Eviction Policies](#8-eviction-policies)
9. [Caching Patterns](#9-caching-patterns)
10. [Caching Failure Modes](#10-caching-failure-modes)
11. [Pros and Cons](#11-pros-and-cons)
12. [Use Cases with Examples](#12-use-cases-with-examples)
13. [System Design — Architecture Diagrams](#13-system-design--architecture-diagrams)
14. [30+ Interview Questions with Answers](#14-30-interview-questions-with-answers)

---

## 1. What is Redis?

Redis stands for **Remote Dictionary Server**.

It is an open-source, in-memory data structure store that can be used as a database, cache, message broker, and streaming engine.

Key facts:
- Single-threaded command execution (I/O multiplexing via epoll/kqueue)
- Data lives in RAM — reads and writes happen at memory speed
- Supports persistence to disk (optional)
- Supports replication, clustering, and pub/sub
- Written in C

```
Redis = In-Memory + Data Structures + Optional Persistence + Network Protocol
```

---

## 2. Why is Redis Fast?

This is the most common Redis interview question. The answer has five layers.

### 2.1 In-Memory Storage

All data is stored in RAM. RAM access is ~100 nanoseconds. Disk access is ~10 milliseconds. Redis is 100,000x faster than disk at the storage layer alone.

### 2.2 Single-Threaded Command Execution

Redis processes all commands on a single thread. No locking. No context switching. No mutex contention between threads. Every command executes atomically.

```
Request 1 --> [Single Thread] --> Response 1
Request 2 --> [Single Thread] --> Response 2
Request 3 --> [Single Thread] --> Response 3
                 (no locks needed)
```

### 2.3 I/O Multiplexing

Redis uses non-blocking I/O with epoll (Linux) or kqueue (macOS). A single thread handles thousands of concurrent connections without blocking on any one of them.

```
[Client 1] \
[Client 2]  --> [epoll/kqueue event loop] --> [Single Command Thread]
[Client 3] /
...
[Client N] /
```

### 2.4 Optimized Data Structures

Redis data structures are implemented in C with hand-tuned memory layouts. Small sets and hashes use ziplist encoding (contiguous memory, cache-friendly). Larger structures switch to hashtable or skiplist automatically.

### 2.5 Simple Network Protocol

Redis uses RESP (Redis Serialization Protocol) — a simple text protocol that is fast to parse and generates minimal overhead per command.

### Summary Table

| Reason | What it eliminates |
|---|---|
| In-memory storage | Disk I/O latency |
| Single-threaded execution | Lock contention, context switching |
| I/O multiplexing (epoll) | Blocking on slow clients |
| Optimized C data structures | Memory fragmentation, cache misses |
| RESP protocol | Protocol parsing overhead |

---

## 3. Redis Data Structures

### 3.1 String

The most basic type. Can store text, integers, or binary data up to 512MB.

```bash
SET user:name "Abhishek"
GET user:name              # "Abhishek"
INCR page:views            # atomic increment — no race condition
SETEX session:abc 3600 "token123"  # set with TTL (seconds)
```

**Use cases:** Session tokens, counters, feature flags, simple caching.

### 3.2 Hash

A map of field-value pairs. Think of it as a row in a database.

```bash
HSET user:1001 name "Abhishek" age "28" city "Hyderabad"
HGET user:1001 name           # "Abhishek"
HGETALL user:1001             # all fields and values
HINCRBY user:1001 age 1       # atomic field increment
```

**Use cases:** User profiles, product data, object storage.

### 3.3 List

Doubly linked list. Supports O(1) push/pop from both ends.

```bash
LPUSH notifications:user1 "New message"
RPUSH notifications:user1 "Order shipped"
LRANGE notifications:user1 0 -1   # get all elements
LPOP notifications:user1           # pop from left
BRPOP queue:jobs 30                # blocking pop — waits 30s for item
```

**Use cases:** Message queues, activity feeds, job queues.

### 3.4 Set

Unordered collection of unique strings. O(1) add, remove, lookup.

```bash
SADD online:users "user1" "user2" "user3"
SISMEMBER online:users "user1"     # 1 (true)
SCARD online:users                 # 3 (count)
SUNION online:users premium:users  # union of two sets
SINTER online:users premium:users  # intersection
```

**Use cases:** Online user tracking, tag systems, unique visitor counting.

### 3.5 Sorted Set (ZSet)

Set where every member has a score. Members ordered by score. O(log N) operations.

```bash
ZADD leaderboard 9500 "player1"
ZADD leaderboard 8200 "player2"
ZADD leaderboard 9800 "player3"
ZRANGE leaderboard 0 -1 WITHSCORES REV   # top players, descending
ZRANK leaderboard "player1"              # rank of a specific player
ZINCRBY leaderboard 100 "player1"        # add 100 to player1's score
```

**Use cases:** Leaderboards, rate limiting, priority queues, time-series data.

### 3.6 Bitmap

Bit array operations on string values. Extremely space-efficient.

```bash
SETBIT user:active:2024-01-01 1001 1   # user 1001 was active on Jan 1
GETBIT user:active:2024-01-01 1001     # 1
BITCOUNT user:active:2024-01-01        # count of active users that day
```

**Use cases:** Daily active user tracking, feature flag rollouts, attendance systems.

### 3.7 HyperLogLog

Probabilistic data structure for cardinality estimation. Uses ~12KB regardless of set size. Accuracy ~0.81% error.

```bash
PFADD unique:visitors "ip1" "ip2" "ip3"
PFCOUNT unique:visitors   # approximate count of unique visitors
PFMERGE all:visitors site1:visitors site2:visitors  # merge two HLLs
```

**Use cases:** Unique visitor counts at scale, distinct event counting.

### 3.8 Stream

Append-only log structure. Each entry has an auto-generated ID and key-value fields.

```bash
XADD events * action "login" user "user1"
XREAD COUNT 10 STREAMS events 0   # read from beginning
XGROUP CREATE events processors $ MKSTREAM  # consumer group
XREADGROUP GROUP processors worker1 COUNT 5 STREAMS events >
```

**Use cases:** Event sourcing, activity logs, real-time analytics pipelines.

---

## 4. Persistence — RDB vs AOF

Redis is in-memory but offers two persistence mechanisms to survive restarts.

### 4.1 RDB (Redis Database Snapshot)

Takes a point-in-time snapshot of all data and saves it to a `.rdb` file.

```
Every N seconds, if M keys changed --> fork() --> child writes snapshot
Main process continues serving requests
```

```bash
# redis.conf
save 900 1      # snapshot if 1 key changed in 900 seconds
save 300 10     # snapshot if 10 keys changed in 300 seconds
save 60 10000   # snapshot if 10000 keys changed in 60 seconds
```

**Pros:**
- Compact single file — easy to backup and transfer
- Fast restart — loading a snapshot is faster than replaying a log
- Low runtime overhead

**Cons:**
- Data loss between snapshots — if Redis crashes at minute 4 of a 5-minute window, you lose 4 minutes of writes
- fork() call can cause brief latency spikes on large datasets

### 4.2 AOF (Append-Only File)

Logs every write command to an `.aof` file. On restart, replays the log to reconstruct state.

```
SET key value --> append "SET key value\r\n" to aof file
INCR counter  --> append "INCR counter\r\n" to aof file
```

```bash
# redis.conf
appendonly yes
appendfsync always     # fsync after every command — safest, slowest
appendfsync everysec   # fsync every second — default, good balance
appendfsync no         # let OS decide — fastest, least safe
```

**Pros:**
- Much better durability — at most 1 second of data loss with `everysec`
- Human-readable log
- AOF rewrite compacts the log periodically

**Cons:**
- AOF files are larger than RDB files
- Slower restart — replaying a large log takes time
- Slightly higher runtime overhead

### 4.3 RDB + AOF Together (Recommended for Production)

Use both. RDB for fast restarts and backups. AOF for durability.

```
Restart sequence: Load RDB snapshot --> replay AOF from that point forward
```

### 4.4 Comparison Table

| Property | RDB | AOF |
|---|---|---|
| Data loss risk | Up to minutes | Up to 1 second |
| File size | Compact | Larger |
| Restart speed | Fast | Slower |
| Write overhead | Low | Higher |
| Use case | Backups, fast restarts | Durability-critical data |

---

## 5. Replication Architecture

### 5.1 Master-Replica Replication

```
                    WRITE
Client ---------> [Master]
                     |
          +----------+----------+
          |                     |
       [Replica 1]          [Replica 2]
          |
       READ                  READ
    (scale reads)        (scale reads)
```

- Replication is asynchronous by default
- Replicas are read-only
- One master can have multiple replicas
- Replicas can have their own replicas (cascaded replication)

### 5.2 Initial Sync

```
Replica connects to Master
    |
Master: fork() --> create RDB snapshot
    |
Master: sends RDB snapshot to replica
    |
Master: buffers all write commands during snapshot transfer
    |
Replica: loads RDB snapshot
    |
Replica: applies buffered commands
    |
Replica: receives live replication stream
```

### 5.3 Partial Resync

If a replica reconnects after a brief disconnect, it does not need a full resync. It uses the replication offset and `repl_backlog` (a ring buffer) to catch up on missed commands only.

---

## 6. Redis Sentinel

Sentinel provides **automatic failover** for a master-replica setup.

### Architecture

```
[Sentinel 1]   [Sentinel 2]   [Sentinel 3]
      \               |               /
       +----------+---+---+-----------+
                  |       |
              [Master]  [Replica]
```

### What Sentinel Does

1. **Monitoring** — continuously pings master and replicas
2. **Notification** — alerts when a Redis instance goes down
3. **Automatic failover** — promotes a replica to master when master fails
4. **Configuration provider** — clients ask Sentinel for the current master address

### Quorum

Minimum number of Sentinels that must agree a master is down before triggering failover.

```
Quorum = 2 (for a 3-Sentinel setup)
Both Sentinel 1 and Sentinel 2 must agree master is unreachable
Only then does failover trigger
```

This prevents false positives — if one Sentinel has a network issue, it cannot alone cause a failover.

### Failover Sequence

```
Master goes down
    |
Sentinel 1 marks master as S_DOWN (subjectively down)
    |
Sentinels gossip — reach quorum agreement
    |
Master marked O_DOWN (objectively down)
    |
Sentinel leader elected (via Raft)
    |
Leader promotes best replica to master
    |
Other replicas reconfigured to follow new master
    |
Clients notified of new master address
```

---

## 7. Redis Cluster

Redis Cluster provides **horizontal scaling** across multiple nodes. Data is automatically sharded.

### Hash Slots

Redis Cluster divides the keyspace into 16384 hash slots.

```
slot = CRC16(key) % 16384
```

Each master node owns a range of slots:

```
Node A (Master): slots 0 - 5460
Node B (Master): slots 5461 - 10922
Node C (Master): slots 10923 - 16383

Each master has 1+ replicas for failover
```

### Architecture

```
         [Client]
            |
   +--------+--------+
   |                 |
[Node A]          [Node B]          [Node C]
[slots 0-5460]    [slots 5461-10922] [slots 10923-16383]
[Replica A]       [Replica B]        [Replica C]
```

### MOVED Redirect

If a client sends a command to the wrong node, that node responds with a MOVED redirect.

```
Client --> SET user:1 "data" --> Node A
Node A: key hashes to slot 7000 -- that's Node B
Node A --> MOVED 7000 node-b:6379
Client --> SET user:1 "data" --> Node B (correct node)
```

Smart clients cache the slot map and route directly, avoiding redirects.

### Cluster vs Sentinel

| Property | Sentinel | Cluster |
|---|---|---|
| Purpose | High availability | Horizontal scaling + HA |
| Sharding | No | Yes (16384 slots) |
| Node count | 1 master + N replicas + 3+ sentinels | 3+ masters, each with replicas |
| Multi-key ops | Supported | Only if keys on same slot |
| Complexity | Lower | Higher |

---

## 8. Eviction Policies

When Redis runs out of memory, it needs to decide what to evict. Set with `maxmemory-policy` in redis.conf.

| Policy | Behavior |
|---|---|
| `noeviction` | Return error when memory is full. No eviction. |
| `allkeys-lru` | Evict least recently used key from all keys |
| `volatile-lru` | Evict least recently used key from keys with TTL set |
| `allkeys-lfu` | Evict least frequently used key from all keys |
| `volatile-lfu` | Evict least frequently used key from keys with TTL set |
| `allkeys-random` | Evict random key from all keys |
| `volatile-random` | Evict random key from keys with TTL set |
| `volatile-ttl` | Evict key with shortest remaining TTL |

### When to Use Which

- **Cache (data exists in DB):** `allkeys-lru` or `allkeys-lfu`
- **Session store (cannot lose active sessions):** `volatile-lru` — only evict keys that have TTL
- **Primary database (data only in Redis):** `noeviction` — never silently lose data

---

## 9. Caching Patterns

### 9.1 Cache Aside (Lazy Loading)

Application manages the cache manually.

```
Read:
  1. Check cache
  2. Cache HIT  --> return data
  3. Cache MISS --> query DB --> write to cache --> return data

Write:
  1. Write to DB
  2. Invalidate (delete) the cache key
```

```java
public User getUser(String userId) {
    String cached = redis.get("user:" + userId);
    if (cached != null) return deserialize(cached);

    User user = db.findById(userId);
    redis.setex("user:" + userId, 3600, serialize(user));
    return user;
}
```

**Pros:** Cache only contains data actually requested. Resilient — if cache fails, app still works.
**Cons:** First request always hits DB (cold start). Risk of stale data.

### 9.2 Write Through

Write to cache and DB together, synchronously.

```
Write:
  1. Write to cache
  2. Write to DB (synchronous)
  3. Return success only after both succeed

Read:
  1. Always check cache first
  2. Cache should always be warm
```

**Pros:** Cache always consistent with DB. No stale reads.
**Cons:** Higher write latency. Cache fills with data that may never be read.

### 9.3 Write Behind (Write Back)

Write to cache immediately. Write to DB asynchronously.

```
Write:
  1. Write to cache --> return success immediately
  2. Background worker --> flush to DB in batches

Read:
  1. Read from cache
```

**Pros:** Extremely low write latency. Batch DB writes reduce load.
**Cons:** Risk of data loss if cache fails before flush. Complex to implement correctly.

### 9.4 Read Through

Cache sits in front of DB. Cache itself fetches from DB on miss.

```
Read:
  1. App queries cache
  2. Cache HIT  --> return data
  3. Cache MISS --> cache fetches from DB --> returns to app --> stores result
```

**Pros:** App code is simple — talks to cache only.
**Cons:** First request is slow. Cache must know how to talk to DB.

---

## 10. Caching Failure Modes

### 10.1 Cache Penetration

**Problem:** Requests for keys that do not exist in cache OR database. Every request goes straight to DB.

```
Attacker sends: GET user:99999999  (non-existent user)
Cache: MISS
DB: no record found
Response: null

Next request: GET user:99999999
Cache: MISS again (nothing was cached)
DB: hit again

Result: DB gets hammered with requests for non-existent data
```

**Solutions:**
1. Cache null responses with a short TTL
2. Use Bloom Filter to check if key possibly exists before hitting DB

```java
// Solution 1: cache null
String result = db.find(key);
if (result == null) {
    redis.setex(key, 60, "NULL_SENTINEL");  // cache null for 60s
}

// Solution 2: Bloom Filter
if (!bloomFilter.mightContain(userId)) {
    return null;  // definitely does not exist — skip DB
}
```

### 10.2 Cache Breakdown (Hotspot Key Expiry)

**Problem:** A single very popular key expires. Thousands of requests simultaneously hit the DB trying to rebuild the cache.

```
Cache key "trending:posts" expires at 12:00:00
At 12:00:01 -- 10,000 requests all get MISS
All 10,000 hit DB simultaneously
DB gets overwhelmed
```

**Solutions:**
1. Mutex lock — only one request rebuilds the cache, others wait
2. Logical TTL — cache never actually expires; store expiry time inside value

```java
// Mutex solution
String value = redis.get(key);
if (value == null) {
    if (redis.setnx("lock:" + key, "1", 10)) {  // acquire lock
        value = db.fetch(key);
        redis.setex(key, 3600, value);
        redis.del("lock:" + key);
    } else {
        Thread.sleep(50);
        return get(key);  // retry after lock holder rebuilds cache
    }
}
```

### 10.3 Cache Avalanche

**Problem:** Large number of cache keys expire at the same time. Massive wave of DB requests.

```
You deploy a new cache. Set all keys with TTL = 3600s.
3600 seconds later -- ALL keys expire simultaneously.
Entire traffic load falls on DB at once.
DB crashes.
```

**Solutions:**
1. Add random jitter to TTL values
2. Use different TTLs for different data categories
3. Set up Redis cluster so cache failure is partial, not total

```java
// Add jitter to TTL
int baseTTL = 3600;
int jitter = random.nextInt(600);  // 0-600 seconds random
redis.setex(key, baseTTL + jitter, value);
```

---

## 11. Pros and Cons

### Pros

| Benefit | Detail |
|---|---|
| Extremely fast | Sub-millisecond latency for reads and writes |
| Rich data structures | String, Hash, List, Set, ZSet, Stream, Bitmap, HyperLogLog |
| Atomic operations | Single-threaded execution means no race conditions on individual commands |
| Versatile | Cache, queue, pub/sub, rate limiter, leaderboard — one tool |
| Persistence options | RDB + AOF for durability when needed |
| High availability | Sentinel for auto-failover, Cluster for horizontal scale |
| Lua scripting | Execute multi-command scripts atomically |
| TTL support | Native key expiration — no manual cleanup |

### Cons

| Limitation | Detail |
|---|---|
| Memory bound | Dataset must fit in RAM — expensive at large scale |
| No complex queries | No joins, no aggregations, no SQL-style filtering |
| Single-threaded | CPU-bound operations block all other commands |
| Eventual consistency in cluster | Asynchronous replication means replicas can lag |
| Not a primary database | Loses data on crash without proper persistence config |
| Cross-slot operations | Redis Cluster does not support multi-key ops across different slots |
| Persistence has tradeoffs | RDB loses data on crash; AOF adds write overhead |

---

## 12. Use Cases with Examples

### 12.1 Session Management

```
User logs in --> server creates session --> store in Redis with TTL

SET session:abc123 "{userId:1001, role:admin}" EX 3600

Next request:
GET session:abc123 --> returns session data
TTL session:abc123 --> returns remaining seconds

User logs out:
DEL session:abc123
```

Why Redis: Fast lookup per request. Automatic expiry on inactivity. Works across multiple app servers (stateless).

### 12.2 Rate Limiting

```
User makes API request
INCR rate:user1001:2024-01-01-14:30   --> returns count
EXPIRE rate:user1001:2024-01-01-14:30 60

If count > 100: reject request (429 Too Many Requests)
If count <= 100: allow request
```

Why Redis: INCR is atomic. No race condition. TTL handles window expiry automatically.

### 12.3 Leaderboard

```
Player completes a level:
ZINCRBY game:leaderboard 150 "player1"

Top 10 players:
ZRANGE game:leaderboard 0 9 WITHSCORES REV

Player's rank:
ZREVRANK game:leaderboard "player1"
```

Why Redis: Sorted Set maintains sorted order automatically. O(log N) updates. O(log N + M) range queries.

### 12.4 Pub/Sub Messaging

```
Publisher:
PUBLISH news:sports "India wins by 6 wickets"

Subscriber:
SUBSCRIBE news:sports
--> receives "India wins by 6 wickets"
```

Why Redis: Real-time message delivery. Decouples publishers from subscribers. Multiple subscribers can listen to same channel.

### 12.5 Distributed Lock

```java
// Acquire lock
String lockId = UUID.randomUUID().toString();
boolean acquired = redis.set("lock:payment:order123", lockId,
    SetParams.setParams().nx().ex(30));  // NX = only if not exists, EX = 30s TTL

if (!acquired) throw new LockException("Payment already in progress");

try {
    processPayment(order123);
} finally {
    // Release only if we still own the lock (Lua script for atomicity)
    redis.eval(
        "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end",
        List.of("lock:payment:order123"), List.of(lockId)
    );
}
```

Why Redis: SET NX EX is atomic. TTL ensures lock is released even if holder crashes. Lua script ensures atomic check-and-delete.

### 12.6 Job Queue

```
Producer:
LPUSH queue:emails '{"to":"user@example.com","subject":"Welcome"}'

Consumer:
BRPOP queue:emails 0   # blocks until item available
--> process email

Dead letter queue for failed jobs:
LPUSH queue:emails:failed <failed-job-payload>
```

Why Redis: BRPOP blocks efficiently without polling. LPUSH is O(1). Simple to implement priority queues with multiple lists.

---

## 13. System Design — Architecture Diagrams

### 13.1 Standard Cache Layer

```
                        [Load Balancer]
                              |
              +---------------+---------------+
              |               |               |
         [App Server 1]  [App Server 2]  [App Server 3]
              |               |               |
              +-------+-------+-------+-------+
                      |               |
                 [Redis Cache]    [Redis Cache]
                 (Primary)        (Replica - reads)
                      |
                 [PostgreSQL / MySQL]
                 (Source of truth)
```

Flow:
1. App checks Redis for data
2. Cache HIT: return immediately
3. Cache MISS: query PostgreSQL, write to Redis, return data

### 13.2 Redis Sentinel Setup (High Availability)

```
[Client] --> [HAProxy / Smart Client]
                      |
         +------------+------------+
         |                         |
    [Master Redis]            [Replica Redis]
         |                         |
         +----------+--------------+
                    |
          [Sentinel 1]  [Sentinel 2]  [Sentinel 3]
              (monitor, failover coordination)

On master failure:
[Sentinel quorum reached] --> [Replica promoted to Master] --> [Clients redirected]
```

### 13.3 Redis Cluster Setup (Horizontal Scaling)

```
                     [Smart Client with slot map cache]
                      /           |           \
                     /            |            \
           [Master A]        [Master B]        [Master C]
         slots 0-5460      slots 5461-10922  slots 10923-16383
              |                  |                  |
         [Replica A]        [Replica B]        [Replica C]

Key routing:
CRC16("user:1001") % 16384 = 4823 --> Master A
CRC16("order:9901") % 16384 = 7712 --> Master B
```

### 13.4 Rate Limiting Architecture

```
[API Gateway / Middleware]
         |
         | For each request:
         |   key = "rate:{userId}:{window}"
         |   count = INCR key
         |   if new key: EXPIRE key 60
         |   if count > limit: reject 429
         |   else: forward request
         v
[Redis] <--> [App Servers]
         |
     [Backend Services]
```

### 13.5 Uber-Style Rate Limiting (Three Tier)

```
[Request] --> [Token Bucket: Per-User limit]
                    |
                    v (passes local limit?)
             [Sliding Window: Per-Service limit]
                    |
                    v (passes service limit?)
              [Global Redis Rate Limiter: System-wide cap]
                    |
                    v (approved)
              [Backend Handler]

Redis structures used:
- Sorted Set for sliding window (score = timestamp, member = request_id)
- String INCR for token bucket counters
- Lua script for atomic multi-step check
```

### 13.6 Pub/Sub Event Architecture

```
[Order Service] --PUBLISH--> [Redis Channel: order.events]
                                        |
                    +-------------------+-------------------+
                    |                   |                   |
          [Notification Service]  [Analytics Service]  [Inventory Service]
          (SUBSCRIBE order.events)(SUBSCRIBE order.events)(SUBSCRIBE order.events)
          sends email/push         updates dashboards       adjusts stock
```

### 13.7 Write-Behind Caching for High Write Throughput

```
[App Server]
     |
     | WRITE (returns immediately)
     v
[Redis Cache] <-- source of truth for reads
     |
     | Background flush (every N seconds or N writes)
     v
[Message Queue / Worker]
     |
     v
[PostgreSQL / MySQL]
```

Use case: High-frequency writes (gaming scores, IoT sensor data, analytics counters) where DB cannot keep up with write throughput.

---

## 14. 30+ Interview Questions with Answers

---

### Fundamentals

---

**Q1. What is Redis and what makes it different from a traditional database?**

Redis is an in-memory data structure store. It keeps all data in RAM, which makes reads and writes happen at memory speed — sub-millisecond latency. Traditional databases like MySQL or PostgreSQL store data on disk and use buffer pools to cache hot data in RAM. Redis treats RAM as the primary storage and disk as optional backup.

The other major difference is data model. Redis is not relational. It has no tables, no joins, no SQL. Instead it offers rich data structures — strings, hashes, lists, sets, sorted sets, streams — each optimized for specific access patterns.

---

**Q2. Redis is single-threaded. How can it handle 100,000+ requests per second?**

The single thread handles command execution only. Network I/O is handled by an event loop using epoll (Linux) or kqueue (macOS), which is non-blocking and handles thousands of concurrent connections without blocking on any one of them.

The bottleneck in Redis is almost never CPU. It is network bandwidth and memory bandwidth. A single Redis command executes in nanoseconds. The event loop can process hundreds of thousands of these per second on a single core.

Redis 6.0 introduced I/O threads for reading and writing network data while keeping command execution single-threaded.

---

**Q3. What is the difference between RDB and AOF persistence?**

RDB takes periodic snapshots of the entire dataset and saves to a `.rdb` file. It is compact, fast to load on restart, and has low runtime overhead. The downside is data loss between snapshots — if Redis crashes 4 minutes into a 5-minute snapshot window, you lose 4 minutes of writes.

AOF logs every write command to an `.aof` file. With `appendfsync everysec`, you lose at most 1 second of data. The downside is larger file size and slower restart because Redis must replay every command.

In production, use both. RDB for fast restart and backups. AOF for durability.

---

**Q4. What happens when Redis runs out of memory?**

Redis uses the eviction policy configured in `maxmemory-policy`. If set to `noeviction`, Redis returns an error on write commands. If set to `allkeys-lru`, it evicts the least recently used key to make space.

For a cache (where data can be reloaded from DB), `allkeys-lru` or `allkeys-lfu` is the right policy. For a session store where you cannot lose active sessions, `volatile-lru` evicts only keys that have a TTL set, preserving session keys that have no TTL.

---

**Q5. What is a Redis pipeline and when should you use it?**

A pipeline batches multiple commands and sends them to Redis in one network round trip. Instead of:

```
SEND SET key1 val1 --> WAIT --> RECEIVE OK
SEND SET key2 val2 --> WAIT --> RECEIVE OK
SEND SET key3 val3 --> WAIT --> RECEIVE OK
```

A pipeline sends all three commands, waits once, receives all three responses. This reduces network round trips from N to 1.

Use pipelining when you need to execute many commands in sequence and do not need the response of command N to decide command N+1.

---

**Q6. What is the difference between EXPIRE and EXPIREAT?**

`EXPIRE key seconds` sets a TTL relative to the current time. Key expires N seconds from now.

`EXPIREAT key timestamp` sets an absolute Unix timestamp when the key should expire.

`PEXPIRE` and `PEXPIREAT` are millisecond versions of the same.

---

### Caching

---

**Q7. Explain cache-aside pattern. What are its failure modes?**

In cache-aside, the application manages the cache manually. On read: check cache, on miss query DB, write result to cache. On write: update DB, invalidate the cache key.

Failure modes:

Cache stampede — when a popular key expires, thousands of requests simultaneously miss cache and hit DB. Solution: mutex lock or probabilistic early expiration.

Stale data — if a write fails to invalidate the cache, reads return stale data. Solution: short TTLs as a safety net.

Cold start — after a cache restart, all requests miss cache initially. Solution: pre-warming the cache on startup.

---

**Q8. What is cache penetration and how do you fix it?**

Cache penetration happens when requests are made for keys that do not exist in cache or database. Every request bypasses the cache and hits the DB. A malicious actor can use this to overwhelm the DB with requests for non-existent IDs.

Two solutions.

Cache null results: when DB returns nothing, store a null sentinel in cache with a short TTL (60 seconds). Next request for the same key hits cache and gets null without touching DB.

Bloom filter: a probabilistic data structure that can definitively say a key does NOT exist. Before hitting cache or DB, check the bloom filter. If it says the key definitely does not exist, return null immediately.

---

**Q9. What is cache avalanche and how do you prevent it?**

Cache avalanche is when a large number of cache keys expire at the same time, causing a massive simultaneous load on the database.

Common cause: setting all keys with the same TTL during a cache warm-up.

Fixes:

Add random jitter to TTL values — instead of all keys expiring at T+3600, they expire between T+3000 and T+4200.

Use different TTL categories by data type — user profiles expire at 1 hour, product listings at 15 minutes, static config at 24 hours.

Implement circuit breaker — if DB becomes overwhelmed, serve stale cache data rather than letting requests through.

---

**Q10. What is a hot key problem in Redis and how do you solve it?**

A hot key is a single Redis key receiving disproportionately high traffic — so high that one Redis node becomes a bottleneck even though the cluster is otherwise underutilized.

Example: a celebrity's profile page. All requests go to the same key on the same shard.

Solutions:

Local caching: cache the hot key in the application's in-process memory (e.g., Caffeine cache in Java). Reduces Redis calls entirely.

Key replication: store the same data under multiple keys (user:1001:1, user:1001:2 ... user:1001:N) and load-balance reads across them.

Read replicas: route reads for hot keys to replica nodes.

---

### Data Structures

---

**Q11. When would you use a Sorted Set vs a List?**

Use a List when order is insertion-based and you need O(1) push/pop from ends. Message queues, activity feeds, job queues.

Use a Sorted Set when order is determined by a score and you need range queries by rank or score. Leaderboards, rate limiting with sliding window (score = timestamp), priority queues where priority is the score.

The key difference: List does not support efficient lookup by value or range queries by score. Sorted Set does.

---

**Q12. How would you implement a sliding window rate limiter using Redis?**

```lua
-- Lua script for atomic sliding window rate limiter
local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])  -- window size in ms
local limit = tonumber(ARGV[3])

-- Remove requests outside the window
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

-- Count requests in window
local count = redis.call('ZCARD', key)

if count < limit then
    -- Add current request
    redis.call('ZADD', key, now, now .. math.random())
    redis.call('EXPIRE', key, window / 1000 + 1)
    return 1  -- allowed
else
    return 0  -- rejected
end
```

Each request adds itself to a sorted set with the timestamp as score. Old entries outside the window are removed. If the count exceeds the limit, reject. This gives a true sliding window with no step artifacts.

---

**Q13. How does HyperLogLog work and when would you use it?**

HyperLogLog is a probabilistic data structure for counting unique elements (cardinality). It uses ~12KB of memory regardless of how many unique elements you add, and provides estimates with ~0.81% standard error.

It works by hashing each element and using the pattern of leading zeros in the hash to estimate cardinality. More leading zeros = more elements seen.

Use it when:
- You need to count unique visitors, unique IPs, or unique events
- The exact count is not critical (0.81% error is acceptable)
- The number of unique elements is too large to store in a Set

For 1 billion unique visitors, a Set would use ~50GB. HyperLogLog uses 12KB.

---

### Concurrency and Transactions

---

**Q14. How does Redis handle transactions? What is MULTI/EXEC?**

Redis transactions group commands using MULTI and EXEC. All commands between MULTI and EXEC are queued and executed atomically — no other client can interleave commands during execution.

```bash
MULTI
SET account:A 900
SET account:B 1100
EXEC
```

Important: Redis transactions are not like SQL transactions. If one command fails inside EXEC, the others still execute. There is no rollback. DISCARD cancels a queued transaction.

For conditional transactions, use WATCH. WATCH monitors a key. If the key changes between WATCH and EXEC, the transaction is aborted.

```bash
WATCH account:A
MULTI
SET account:A 900
EXEC
-- returns nil if account:A changed since WATCH
```

---

**Q15. What is a Lua script in Redis and why use it instead of MULTI/EXEC?**

A Lua script is executed atomically on the Redis server. The entire script runs as a single unit — no other commands can execute between lines of the script.

The advantage over MULTI/EXEC is that a Lua script can contain conditional logic based on data it reads. MULTI/EXEC queues commands without knowing the values — you cannot say "if the value is X, do Y" inside a MULTI block.

```lua
-- Atomic check-and-set with condition
local val = redis.call('GET', KEYS[1])
if tonumber(val) > 100 then
    redis.call('SET', KEYS[1], 0)
    return 1
end
return 0
```

Lua scripts are registered with SCRIPT LOAD and called with EVALSHA for efficiency.

---

**Q16. What is the difference between SETNX and SET with NX option?**

Both set a key only if it does not already exist. The difference is atomicity with TTL.

`SETNX key value` then `EXPIRE key seconds` is two commands — there is a window between them where the key exists without a TTL. If the process crashes between these two commands, the lock key remains forever.

`SET key value NX EX seconds` does both atomically. This is the correct way to implement distributed locks. Always use the single SET command with NX and EX options together.

---

### Architecture and Cluster

---

**Q17. How does Redis Cluster handle a node failure?**

Each master has at least one replica. When a master fails, its replicas detect the failure via gossip protocol. Replicas start an election among cluster nodes. When a majority of masters agree the master is unreachable, one replica is promoted to master. The promoted replica takes ownership of the hash slots previously owned by the failed master.

Client requests for those slots get MOVED errors until they update their slot map, typically handled automatically by smart client libraries.

---

**Q18. What are hash tags in Redis Cluster and why are they needed?**

Redis Cluster routes each key to a slot based on `CRC16(key) % 16384`. Multi-key operations (MGET, MSET, SMEMBERS across keys, Lua scripts with multiple keys) only work if all keys are on the same slot.

Hash tags force multiple keys to the same slot by specifying the part of the key to hash inside curly braces.

```
user:{1001}:profile   --> hash of "1001" determines slot
user:{1001}:settings  --> hash of "1001" determines slot (same slot)
user:{1001}:orders    --> hash of "1001" determines slot (same slot)
```

All three keys land on the same node, making multi-key operations possible.

---

**Q19. What is the CAP theorem and where does Redis fall?**

CAP theorem states that a distributed system can only guarantee two of three: Consistency, Availability, Partition Tolerance.

Redis Cluster chooses Availability and Partition Tolerance (AP) over strong Consistency.

Replication is asynchronous. If a master fails before replicating recent writes to its replica, those writes are lost when the replica is promoted. The system stays available (the cluster keeps serving requests) but may serve slightly stale data.

This trade-off is acceptable for caches. It is not acceptable for financial transaction systems, which require CP databases like Google Spanner or CockroachDB.

---

**Q20. How do you handle cache warming after a Redis restart?**

Options:

Lazy warming: do nothing. Let the cache fill naturally as users make requests. Acceptable if DB can handle the initial load spike.

Proactive warming: before restarting Redis (or after starting a new instance), run a background job that pre-populates the most frequently accessed keys from DB. Use analytics data or access logs to determine which keys to warm.

RDB snapshot: if you have persistence enabled, Redis loads the RDB snapshot on startup and comes back with most of its data already populated. Only data written between the last snapshot and the crash is missing.

---

### Advanced

---

**Q21. How would you design a distributed lock using Redis? What are the failure modes?**

Basic implementation:

```java
// Acquire: SET key value NX EX 30
// Release: Lua script to check and delete atomically

String lockKey = "lock:" + resourceId;
String lockValue = UUID.randomUUID().toString();  // unique per lock holder

boolean acquired = redis.set(lockKey, lockValue, SetParams.setParams().nx().ex(30)) != null;
```

Failure modes:

Lock expiry before work completes: if processing takes longer than 30 seconds, the lock expires and another process acquires it. Both processes now hold the lock. Solution: extend lock TTL periodically (lock renewal / watchdog pattern).

Redis crash after acquire but before release: the TTL saves you — lock expires automatically.

Network partition: process holds lock, loses connectivity to Redis, Redis expires the lock, another process acquires it. Original process comes back and thinks it still has the lock. Solution: use a unique lock value and verify ownership before any critical operation.

For stronger guarantees, use the Redlock algorithm across multiple independent Redis instances.

---

**Q22. What is Redlock and when should you use it?**

Redlock is a distributed locking algorithm that uses multiple independent Redis instances (typically 5) to provide stronger safety guarantees than a single-instance lock.

Algorithm:
1. Get current timestamp
2. Try to acquire lock on all N instances with the same key and same random value
3. Lock is considered acquired if majority (N/2 + 1) instances granted it within validity time
4. Lock validity = initial TTL minus time elapsed during acquisition
5. To release, send DELETE to all instances

Use Redlock when:
- Your Redis instance itself can fail
- You cannot tolerate a scenario where two processes hold the same lock simultaneously
- The operation being protected has severe consequences if run concurrently (payments, inventory deduction)

For most use cases, a single-instance lock with a unique value and TTL is sufficient.

---

**Q23. How does Redis handle memory fragmentation?**

Memory fragmentation occurs when Redis allocates and frees memory in patterns that leave gaps the allocator cannot reuse efficiently. The fragmentation ratio is `used_memory_rss / used_memory`. A ratio above 1.5 indicates significant fragmentation.

Redis uses jemalloc as its memory allocator, which reduces fragmentation compared to the system allocator. Redis 4.0 introduced `activedefrag yes` which compacts memory online without stopping the server.

Commands to monitor:
```bash
INFO memory
# look for: mem_fragmentation_ratio
# > 1.5 means fragmentation is a concern
# < 1.0 means Redis is using swap (bad)
```

---

**Q24. What are Redis Streams and how are they different from Pub/Sub?**

Pub/Sub in Redis is fire-and-forget. Messages are delivered to current subscribers and immediately discarded. If no subscriber is listening, the message is lost. There is no persistence, no replay, no consumer groups.

Redis Streams is a persistent append-only log. Messages are stored. Consumers can read from any position using an offset. Consumer groups allow multiple workers to process the same stream with at-least-once delivery guarantees. Failed messages can be re-claimed and reprocessed.

Use Pub/Sub for: real-time notifications where missed messages are acceptable.
Use Streams for: event sourcing, reliable message processing, audit logs, anything requiring at-least-once delivery.

---

**Q25. How would you monitor Redis in production?**

Key metrics to watch:

```
INFO all   # comprehensive stats
INFO memory
INFO stats
INFO replication
```

Critical metrics:

- `used_memory` vs `maxmemory` — are you approaching the limit?
- `evicted_keys` — are keys being evicted? High number means cache pressure
- `keyspace_hits` vs `keyspace_misses` — cache hit rate = hits / (hits + misses)
- `connected_clients` — connection pool health
- `blocked_clients` — clients waiting on BLPOP/BRPOP
- `repl_backlog_size` and replication lag — replica health
- `latency` — use `LATENCY HISTORY event` for latency spikes
- `slowlog` — use `SLOWLOG GET 10` to find slow commands

---

**Q26. What is the N+1 problem in Redis and how do you avoid it?**

The N+1 problem in Redis is making N separate GET calls in a loop when a single call could fetch all data.

Bad pattern:
```java
for (String userId : userIds) {
    User user = redis.get("user:" + userId);  // N network round trips
}
```

Solutions:

MGET for strings:
```java
List<String> keys = userIds.stream().map(id -> "user:" + id).collect(toList());
List<String> values = redis.mget(keys.toArray(new String[0]));  // 1 round trip
```

Pipeline for mixed commands:
```java
Pipeline pipeline = redis.pipelined();
for (String userId : userIds) {
    pipeline.get("user:" + userId);
}
List<Object> results = pipeline.syncAndReturnAll();  // 1 round trip
```

---

**Q27. How do you implement a leaderboard that supports millions of players?**

```bash
# Add or update player score
ZADD leaderboard:global 9500 "player:1001"

# Top 10 players
ZRANGE leaderboard:global 0 9 WITHSCORES REV

# Player's rank (0-indexed)
ZREVRANK leaderboard:global "player:1001"

# Players around a specific player (±5 ranks)
ZREVRANK leaderboard:global "player:1001" --> rank 42
ZRANGE leaderboard:global 37 47 WITHSCORES REV  --> players ranked 38-48
```

For millions of players, one global sorted set works — Redis sorted sets scale to hundreds of millions of members. The operations remain O(log N).

For regional leaderboards, use separate sorted sets per region and merge with ZUNIONSTORE when needed.

---

**Q28. How would you use Redis for autocomplete / typeahead?**

Store all possible completions in a Sorted Set with score 0. Lexicographic range queries find all completions for a prefix.

```bash
# Index
ZADD autocomplete 0 "javascript"
ZADD autocomplete 0 "java"
ZADD autocomplete 0 "jakarta"
ZADD autocomplete 0 "jackson"

# Query: all completions starting with "ja"
ZRANGEBYLEX autocomplete "[ja" "[ja\xff"
# Returns: jackson, jakarta, java, javascript
```

For large datasets, shard by first character — autocomplete:j, autocomplete:p, etc.

For ranked completions based on popularity, use the popularity score instead of 0, and use ZRANGE with BYSCORE.

---

**Q29. What is the difference between Redis and Memcached?**

| Property | Redis | Memcached |
|---|---|---|
| Data structures | String, Hash, List, Set, ZSet, Stream, Bitmap, HLL | String only |
| Persistence | RDB + AOF | None |
| Replication | Yes (built-in) | No (third-party) |
| Clustering | Yes (Redis Cluster) | Yes (client-side sharding) |
| Pub/Sub | Yes | No |
| Lua scripting | Yes | No |
| Multi-threading | I/O threads in v6+ | Yes (fully multi-threaded) |
| Memory efficiency | Slightly higher overhead | Slightly more memory efficient for simple strings |

Choose Memcached when: you only need simple string caching, you want maximum multi-core utilization, simplicity is the priority.

Choose Redis when: you need rich data structures, persistence, replication, pub/sub, or any feature beyond basic key-value caching.

---

**Q30. Design a notification system for 100 million users using Redis.**

Requirements: send notifications to users in real-time. Support read/unread status. Support notification history.

Design:

```
Per-user notification list (recent 100):
LPUSH notifications:user:1001 '{"id":"n1","message":"New message","ts":1706789400}'
LTRIM notifications:user:1001 0 99   # keep only last 100

Unread count:
INCR unread:user:1001

Mark as read:
SET unread:user:1001 0

Real-time delivery:
PUBLISH user:1001:channel '{"type":"notification","id":"n1"}'

Client subscribes to their channel via WebSocket:
app server --> SUBSCRIBE user:1001:channel --> push to client WebSocket

Fan-out for broadcast notifications:
SMEMBERS active:users --> for each user: PUBLISH user:{id}:channel <message>
For 100M users, use Kafka for fan-out and Redis only for delivery to active users
```

---

**Q31. What is OBJECT ENCODING and why does it matter?**

Redis automatically uses different internal encodings for the same data type depending on the size of the data. Smaller structures use memory-efficient compact encodings.

```bash
SET counter 42
OBJECT ENCODING counter    # "int"

SET name "Abhishek"
OBJECT ENCODING name       # "embstr" (strings <= 44 bytes)

SET longstr "x" * 100
OBJECT ENCODING longstr    # "raw" (strings > 44 bytes)

HSET small:hash a 1 b 2
OBJECT ENCODING small:hash # "ziplist" (small hashes)

HSET large:hash ...100 fields...
OBJECT ENCODING large:hash # "hashtable" (large hashes)
```

This matters because ziplist encoding stores data as a contiguous block of memory — cache-friendly and memory-efficient. Once a structure exceeds the threshold (configurable via `hash-max-ziplist-entries` etc.), it converts to a more expensive but faster structure.

Tuning these thresholds is a common production optimization to reduce memory usage.

---

**Q32. How would you debug a Redis performance issue in production?**

Step 1: Check latency
```bash
redis-cli --latency -h redis-host
redis-cli LATENCY HISTORY command
```

Step 2: Find slow commands
```bash
redis-cli SLOWLOG GET 20
# Shows commands that took longer than slowlog-log-slower-than (default 10ms)
```

Step 3: Check memory
```bash
redis-cli INFO memory
# Check: used_memory, mem_fragmentation_ratio, evicted_keys
```

Step 4: Find big keys
```bash
redis-cli --bigkeys
# Scans keyspace to find largest keys per type
```

Step 5: Check keyspace
```bash
redis-cli INFO keyspace
# Shows key counts and TTL stats per database
```

Step 6: Monitor real-time commands
```bash
redis-cli MONITOR
# Shows every command in real time — use briefly in production
```

Step 7: Check replication lag
```bash
redis-cli INFO replication
# Check: master_repl_offset vs slave_repl_offset
```

---

## Quick Reference Card

```
Data Structure  | Best Use Case
----------------|--------------------------------------------------
String          | Sessions, counters, simple cache, feature flags
Hash            | User profiles, object storage
List            | Job queues, activity feeds, message queues
Set             | Unique visitors, tags, membership checks
Sorted Set      | Leaderboards, rate limiting, priority queues
Bitmap          | Daily active users, feature rollouts
HyperLogLog     | Approximate unique counts at massive scale
Stream          | Event sourcing, audit logs, reliable queues
Pub/Sub         | Real-time notifications, live feeds

Failure Mode    | Solution
----------------|--------------------------------------------------
Cache Miss      | Cache-aside + TTL
Cache Penetration| Cache null + Bloom filter
Cache Breakdown | Mutex lock + logical TTL
Cache Avalanche | TTL jitter + circuit breaker
Hot Key         | Local cache + key replication

Persistence     | Data Loss | Restart Speed | Use When
----------------|-----------|---------------|----------------------
RDB             | Minutes   | Fast          | Backups, dev
AOF everysec    | 1 second  | Slow          | Production durability
RDB + AOF       | 1 second  | Medium        | Production recommended

Topology        | Use When
----------------|--------------------------------------------------
Standalone      | Dev, single node, small datasets
Sentinel        | HA needed, single master, auto-failover
Cluster         | Data > single node, horizontal scale needed
```

---

*Cover: Redis internals, caching patterns, failure modes, architecture, and 32 interview questions. Enough for any SDE-2 or Senior Engineer Redis interview round.*
