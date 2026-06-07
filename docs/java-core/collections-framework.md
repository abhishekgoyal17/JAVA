# Java Collections Framework - Complete Interview Deep Dive

> A one-stop reference for Java collections interviews: List, Set, Map, HashMap internals, ConcurrentHashMap, ArrayList vs LinkedList, HashSet, fail-fast iterators, time complexity, memory trade-offs, and common traps.

---

## Table of Contents

1. [Why Collections Exist](#1-why-collections-exist)
2. [Collection Framework Map](#2-collection-framework-map)
3. [Big-O Complexity Cheat Sheet](#3-big-o-complexity-cheat-sheet)
4. [ArrayList Deep Dive](#4-arraylist-deep-dive)
5. [LinkedList Deep Dive](#5-linkedlist-deep-dive)
6. [ArrayList vs LinkedList](#6-arraylist-vs-linkedlist)
7. [HashMap Deep Dive](#7-hashmap-deep-dive)
8. [HashMap Collision Handling](#8-hashmap-collision-handling)
9. [HashMap Resizing](#9-hashmap-resizing)
10. [HashMap Interview Traps](#10-hashmap-interview-traps)
11. [HashSet Deep Dive](#11-hashset-deep-dive)
12. [ConcurrentHashMap Deep Dive](#12-concurrenthashmap-deep-dive)
13. [Fail-Fast Iterators](#13-fail-fast-iterators)
14. [Choosing the Right Collection](#14-choosing-the-right-collection)
15. [Common Interview Questions and Answers](#15-common-interview-questions-and-answers)
16. [Quick Reference Cheat Sheet](#16-quick-reference-cheat-sheet)

---

## 1. Why Collections Exist

Arrays are fixed-size and low-level:

```java
int[] numbers = new int[3];
numbers[0] = 10;
numbers[1] = 20;
numbers[2] = 30;
// numbers[3] = 40; // ArrayIndexOutOfBoundsException
```

Collections solve problems arrays do not solve cleanly:

- Dynamic resizing
- Ready-made searching, insertion, deletion, sorting, iteration
- Key-value lookup
- Set semantics with no duplicates
- Queue and stack behavior
- Thread-safe variants
- Rich algorithms in `Collections` utility class

Mental model:

```
Problem type                         Best family
---------------------------------------------------------------
Ordered sequence                     List
Unique values                        Set
Key -> value lookup                  Map
First-in-first-out processing        Queue
Priority-based removal               PriorityQueue
Concurrent access                    java.util.concurrent
```

Important interview point:

`Collection` and `Collections` are different.

| Name | What it is |
|---|---|
| `Collection` | Root interface for List, Set, Queue |
| `Collections` | Utility class with static methods like `sort`, `reverse`, `unmodifiableList` |
| `Map` | Separate hierarchy, not a subtype of `Collection` |

---

## 2. Collection Framework Map

High-level hierarchy:

```
Iterable
  |
  +-- Collection
        |
        +-- List
        |     +-- ArrayList
        |     +-- LinkedList
        |     +-- Vector
        |
        +-- Set
        |     +-- HashSet
        |     +-- LinkedHashSet
        |     +-- TreeSet
        |
        +-- Queue
              +-- PriorityQueue
              +-- Deque
                    +-- ArrayDeque
                    +-- LinkedList

Map
  |
  +-- HashMap
  +-- LinkedHashMap
  +-- TreeMap
  +-- Hashtable
  +-- ConcurrentHashMap
```

Core interface contracts:

| Interface | Contract |
|---|---|
| `List` | Ordered, index-based, duplicates allowed |
| `Set` | No duplicates |
| `Queue` | Processing order abstraction |
| `Deque` | Double-ended queue |
| `Map` | Key-value pairs, unique keys |

---

## 3. Big-O Complexity Cheat Sheet

| Collection | Access by index | Search by value | Insert end | Insert middle | Delete | Notes |
|---|---:|---:|---:|---:|---:|---|
| `ArrayList` | O(1) | O(n) | Amortized O(1) | O(n) | O(n) | Backed by array |
| `LinkedList` | O(n) | O(n) | O(1) if node known | O(1) if node known | O(1) if node known | Traversal cost dominates |
| `HashSet` | N/A | Average O(1) | Average O(1) | N/A | Average O(1) | Backed by HashMap |
| `LinkedHashSet` | N/A | Average O(1) | Average O(1) | N/A | Average O(1) | Maintains insertion order |
| `TreeSet` | N/A | O(log n) | O(log n) | N/A | O(log n) | Sorted |
| `HashMap` | N/A | key O(1) avg | put O(1) avg | N/A | remove O(1) avg | No ordering |
| `LinkedHashMap` | N/A | key O(1) avg | put O(1) avg | N/A | remove O(1) avg | Insertion/access order |
| `TreeMap` | N/A | O(log n) | O(log n) | N/A | O(log n) | Sorted keys |
| `ConcurrentHashMap` | N/A | key O(1) avg | put O(1) avg | N/A | remove O(1) avg | Thread-safe |

Interview warning:

O(1) for `HashMap` and `HashSet` means average case, not guaranteed always. Bad hash distribution can degrade performance.

---

## 4. ArrayList Deep Dive

`ArrayList` is a resizable array.

```
ArrayList<Integer> list = new ArrayList<>();

Logical list:
[10, 20, 30]

Internal array:
index:   0   1   2   3   4   5   6   7 ...
       +---+---+---+---+---+---+---+---+
value: |10 |20 |30 |   |   |   |   |   |
       +---+---+---+---+---+---+---+---+
size = 3
capacity = internal array length
```

### What happens on add?

```java
List<String> names = new ArrayList<>();
names.add("A");
names.add("B");
names.add("C");
```

If there is capacity:

```
array[size] = element
size++
```

If array is full:

```
1. Create a bigger array
2. Copy old elements
3. Add new element
4. Point ArrayList to new array
```

ASCII flow:

```
Before:
[A][B][C] capacity full

Resize:
[A][B][C] ----copy----> [A][B][C][_][_]

After add D:
[A][B][C][D][_]
```

### Why add is amortized O(1)

Most `add()` operations are simple writes. Occasionally, an add triggers resizing and copying, which is O(n). Spread over many inserts, average cost is O(1).

```
add #1     O(1)
add #2     O(1)
add #3     O(1)
resize     O(n)
add #4     O(1)
...

Average over many adds = amortized O(1)
```

### Why middle insert is O(n)

```java
list.add(1, "X");
```

Before:

```
index: 0   1   2   3
      [A] [B] [C] [D]
```

Insert X at index 1:

```
Move D right
Move C right
Move B right
Put X at index 1

      [A] [X] [B] [C] [D]
```

This shifting is O(n).

### When ArrayList is excellent

- Read-heavy data
- Frequent random access by index
- Append-heavy workloads
- Iterating all elements
- Cache-friendly memory access

CPU cache advantage:

```
ArrayList internal array:
[obj0][obj1][obj2][obj3][obj4]

Elements are references stored contiguously.
CPU can prefetch nearby references efficiently.
```

### When ArrayList is weak

- Frequent insert/delete near beginning or middle
- Very large arrays with repeated resizing
- Multi-threaded mutation without external synchronization

### Practical tip

If you know the approximate size, set initial capacity:

```java
List<Order> orders = new ArrayList<>(10_000);
```

This avoids repeated resizing.

---

## 5. LinkedList Deep Dive

`LinkedList` in Java is a doubly linked list.

Each node stores:

- Previous node reference
- Element value
- Next node reference

```
null <- [prev|A|next] <-> [prev|B|next] <-> [prev|C|next] -> null
```

Simplified node:

```java
class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;
}
```

### Why index access is O(n)

```java
list.get(5000);
```

There is no direct address calculation like arrays. Java must walk node by node:

```
head -> node0 -> node1 -> node2 -> ... -> node5000
```

That is O(n).

### Why insert/delete can be O(1) if node is known

Suppose we already have reference to node B:

```
A <-> B <-> C
```

Remove B:

```
A.next = C
C.prev = A

A <-> C
```

Pointer rewiring is O(1).

But finding B by value or index is still O(n).

### Interview trap

Many candidates say:

> LinkedList insertion is O(1), ArrayList insertion is O(n).

Correct answer:

> LinkedList insertion is O(1) only when you already have the node or iterator position. If you first need to find the position by index or value, total cost is O(n).

### Memory overhead

For each element:

```
ArrayList:
  one reference in array

LinkedList:
  Node object
  item reference
  next reference
  prev reference
  object header
```

This means `LinkedList` uses significantly more memory.

### When LinkedList makes sense

- You need frequent add/remove at both ends
- You are using it as `Deque`
- You already have iterator position and mutate near it

But in modern Java, for queue/deque use cases, prefer `ArrayDeque` over `LinkedList` unless you specifically need null elements or List operations.

---

## 6. ArrayList vs LinkedList

| Operation | ArrayList | LinkedList |
|---|---:|---:|
| `get(index)` | O(1) | O(n) |
| `add(element)` at end | Amortized O(1) | O(1) |
| `add(index, element)` | O(n) shift | O(n) traverse + O(1) link |
| `remove(index)` | O(n) shift | O(n) traverse + O(1) unlink |
| Iteration | Fast, cache-friendly | Slower, pointer chasing |
| Memory usage | Lower | Higher |
| Best use | Most general lists | Deque-like operations |

Decision:

```
Need index access?                  ArrayList
Need compact memory?                ArrayList
Need fastest iteration?             ArrayList
Need many end inserts?              ArrayList
Need queue/deque behavior?          ArrayDeque usually, LinkedList rarely
Need frequent middle mutation?      Only LinkedList if iterator/node known
```

Interview answer:

> In real Java applications, `ArrayList` is the default choice for `List`. `LinkedList` sounds attractive theoretically, but its poor cache locality and high memory overhead often make it slower unless the use case truly benefits from node-level insertion or deque operations.

---

## 7. HashMap Deep Dive

`HashMap<K, V>` stores key-value pairs.

```java
Map<String, Integer> marks = new HashMap<>();
marks.put("Amit", 90);
marks.put("Neha", 95);
```

Mental model:

```
Key -> hashCode() -> hash spreading -> bucket index -> store Entry/Node
```

Internal structure:

```
HashMap table array

index 0: null
index 1: Node(hash, key, value, next) -> Node(...)
index 2: null
index 3: Node(...)
index 4: null
...
```

Simplified node:

```java
static class Node<K, V> {
    final int hash;
    final K key;
    V value;
    Node<K, V> next;
}
```

### put() flow

```java
map.put("Amit", 90);
```

Step-by-step:

```
1. Compute key.hashCode()
2. Spread hash bits
3. Convert hash to bucket index
4. If bucket empty, insert new node
5. If bucket has nodes:
   - compare hash
   - compare key using equals()
   - if same key, replace value
   - else append/tree-insert as collision
6. If size crosses threshold, resize
```

ASCII:

```
"Amit"
  |
  v
hashCode() = 2044556
  |
  v
spread hash
  |
  v
index = hash & (capacity - 1)
  |
  v
table[index]
```

### get() flow

```java
Integer score = map.get("Amit");
```

```
1. Compute hash of lookup key
2. Find bucket
3. Walk bucket
4. Match by hash and equals()
5. Return value
```

Important:

`hashCode()` finds the bucket. `equals()` finds the exact key inside the bucket.

---

## 8. HashMap Collision Handling

A collision happens when two different keys land in the same bucket.

```
table[5]
  |
  v
[hash=10,key=A,value=1,next] -> [hash=42,key=B,value=2,next] -> null
```

Before Java 8:

```
bucket = linked list
worst-case lookup = O(n)
```

Java 8+:

If a bucket becomes too large, it can become a balanced tree.

```
Small bucket:
A -> B -> C

Large bucket:
        D
       / \
      B   F
     / \ / \
    A  C E  G
```

This improves worst-case lookup in that bucket from O(n) to O(log n), assuming keys are comparable or tie-breaking can be applied.

Common thresholds:

| Concept | Typical value |
|---|---:|
| Treeify threshold | 8 |
| Untreeify threshold | 6 |
| Minimum capacity for treeification | 64 |
| Default load factor | 0.75 |

Interview nuance:

If table capacity is still small, HashMap prefers resizing over treeifying. Why? Because collisions may be caused by too few buckets, not bad keys.

---

## 9. HashMap Resizing

HashMap has:

```
capacity = number of buckets
size = number of key-value entries
load factor = how full table is allowed to get
threshold = capacity * load factor
```

Default:

```
capacity = 16
load factor = 0.75
threshold = 12
```

When size exceeds threshold:

```
1. Create new table, usually double capacity
2. Move nodes into new buckets
3. Recompute placement based on new capacity
```

ASCII:

```
Old table capacity 16:
index = hash & 15

New table capacity 32:
index = hash & 31
```

Why resize matters:

- More buckets means fewer collisions
- Fewer collisions means faster lookup
- But resizing is expensive

### Initial capacity tip

If you expect 1,000 entries:

```java
Map<String, User> users = new HashMap<>(1340);
```

Why not 1000?

Because threshold is capacity * 0.75. To store 1000 entries without resize:

```
needed capacity = expected size / load factor
needed capacity = 1000 / 0.75 = 1333.33
```

HashMap rounds capacity to a power of two internally.

---

## 10. HashMap Interview Traps

### Trap 1: Mutable keys

Never mutate fields used in `hashCode()` or `equals()` after insertion.

```java
class User {
    String email;

    User(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

Map<User, String> map = new HashMap<>();
User user = new User("a@test.com");
map.put(user, "Amit");

user.email = "b@test.com";

System.out.println(map.get(user)); // usually null
```

What happened:

```
Inserted using hash("a@test.com") -> bucket 3
Lookup uses hash("b@test.com")   -> bucket 9
Object is still in bucket 3, but lookup searches bucket 9
```

### Trap 2: Overriding equals but not hashCode

If two objects are equal by `equals()`, they must have same `hashCode()`.

```java
// Broken:
class Employee {
    int id;

    @Override
    public boolean equals(Object o) {
        return o instanceof Employee && this.id == ((Employee) o).id;
    }

    // hashCode not overridden
}
```

Two equal employees may go into different buckets.

### Trap 3: Null key and null values

`HashMap` allows:

```java
map.put(null, "value");
map.put("key", null);
```

Only one null key is allowed because keys are unique.

`ConcurrentHashMap` does not allow null keys or null values.

### Trap 4: HashMap is not thread-safe

Concurrent mutation can corrupt logical behavior or lose updates.

```java
Map<String, Integer> map = new HashMap<>();
// Multiple threads calling put/remove without synchronization is unsafe.
```

Use:

```java
Map<String, Integer> map = new ConcurrentHashMap<>();
```

or:

```java
Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());
```

But synchronized map locks the entire map for most operations; `ConcurrentHashMap` is usually better.

---

## 11. HashSet Deep Dive

`HashSet<E>` is backed by a `HashMap<E, Object>`.

Conceptually:

```java
set.add("A");
```

internally acts like:

```java
map.put("A", PRESENT);
```

ASCII:

```
HashSet
  |
  v
HashMap
  |
  +-- key = element
  +-- value = constant dummy object
```

This is why HashSet depends on `equals()` and `hashCode()`.

### Duplicate detection

```java
Set<String> set = new HashSet<>();
set.add("Java");
set.add("Java");

System.out.println(set.size()); // 1
```

Flow:

```
First add:
  no matching key -> insert

Second add:
  same hash + equals true -> duplicate -> do not insert
```

### HashSet ordering

`HashSet` does not guarantee insertion order.

```java
Set<Integer> set = new HashSet<>();
set.add(10);
set.add(1);
set.add(5);
```

Output may not be:

```
10 1 5
```

If order matters:

| Need | Use |
|---|---|
| Insertion order | `LinkedHashSet` |
| Sorted order | `TreeSet` |
| Fast uniqueness, no order | `HashSet` |

---

## 12. ConcurrentHashMap Deep Dive

`ConcurrentHashMap` is a thread-safe hash table optimized for concurrent reads and updates.

Basic usage:

```java
ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
counts.put("java", 1);
counts.put("spring", 2);
```

### Why not Hashtable?

`Hashtable` synchronizes most methods at the object level.

```
Hashtable:

Thread A put()  ----locks whole table----
Thread B get()  ----waits---------------
Thread C put()  ----waits---------------
```

`ConcurrentHashMap` allows much better concurrency.

High-level mental model:

```
ConcurrentHashMap:

Reads: usually non-blocking
Writes: lock only affected bucket/bin when needed
Table resizing: cooperative among threads
```

### Java 7 vs Java 8 design

Java 7 used segment-based locking:

```
ConcurrentHashMap
  |
  +-- Segment 0
  +-- Segment 1
  +-- Segment 2
  +-- Segment 3

Each segment was like a smaller locked HashMap.
```

Java 8+ moved away from fixed segments and uses bucket-level synchronization plus CAS-style operations.

Simplified Java 8+ picture:

```
table
  |
  +-- bucket 0: Node...
  +-- bucket 1: Node...  <- only this bin may be locked during update
  +-- bucket 2: Node...
```

### Why null keys and values are not allowed

```java
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
map.put(null, "x"); // NullPointerException
map.put("x", null); // NullPointerException
```

Reason:

In concurrent code, `null` from `get()` must mean "key absent".

If null values were allowed:

```java
String value = map.get("x");
```

`value == null` would be ambiguous:

```
Case 1: key does not exist
Case 2: key exists with null value
Case 3: another thread removed it
```

So ConcurrentHashMap forbids nulls.

### Atomic methods

Use atomic operations instead of get-then-put.

Broken:

```java
Integer old = counts.get("java");
if (old == null) {
    counts.put("java", 1);
} else {
    counts.put("java", old + 1);
}
```

Race:

```
Thread A reads 1
Thread B reads 1
Thread A writes 2
Thread B writes 2
Expected 3, got 2
```

Correct:

```java
counts.merge("java", 1, Integer::sum);
```

or:

```java
counts.compute("java", (key, oldValue) -> oldValue == null ? 1 : oldValue + 1);
```

For high contention counters:

```java
ConcurrentHashMap<String, LongAdder> counts = new ConcurrentHashMap<>();

counts.computeIfAbsent("java", key -> new LongAdder()).increment();
```

### Weakly consistent iterators

`ConcurrentHashMap` iterators are not fail-fast. They are weakly consistent.

They may reflect some changes made during iteration, but they do not throw `ConcurrentModificationException`.

```java
for (String key : map.keySet()) {
    map.put("new-key", 100); // allowed
}
```

---

## 13. Fail-Fast Iterators

Fail-fast means an iterator detects structural modification and throws `ConcurrentModificationException`.

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

for (String value : list) {
    if (value.equals("B")) {
        list.remove(value); // ConcurrentModificationException
    }
}
```

### Why it happens

Collections maintain a modification count.

```
ArrayList:
  modCount = number of structural modifications

Iterator:
  expectedModCount = modCount at iterator creation
```

ASCII:

```
list.modCount = 3
iterator.expectedModCount = 3

list.remove("B")
list.modCount = 4

iterator.next()
expectedModCount != modCount
throw ConcurrentModificationException
```

### Correct way to remove during iteration

```java
Iterator<String> iterator = list.iterator();
while (iterator.hasNext()) {
    String value = iterator.next();
    if (value.equals("B")) {
        iterator.remove();
    }
}
```

Why this works:

`iterator.remove()` updates the iterator's expected modification count.

### Alternative: removeIf

```java
list.removeIf(value -> value.equals("B"));
```

### Fail-fast is best-effort

Important interview sentence:

> Fail-fast behavior is not a correctness guarantee. It is a bug-detection mechanism. In unsynchronized concurrent modification, the JVM does not guarantee that `ConcurrentModificationException` will always be thrown.

### Fail-fast vs fail-safe vs weakly consistent

| Iterator type | Example | Behavior |
|---|---|---|
| Fail-fast | `ArrayList`, `HashMap` | Throws on structural modification |
| Snapshot/fail-safe style | `CopyOnWriteArrayList` | Iterates over snapshot |
| Weakly consistent | `ConcurrentHashMap` | Allows concurrent modification, may reflect some updates |

---

## 14. Choosing the Right Collection

### Scenario table

| Requirement | Best choice |
|---|---|
| General-purpose ordered list | `ArrayList` |
| Frequent random access | `ArrayList` |
| Queue/deque operations | `ArrayDeque` |
| Unique values, no ordering | `HashSet` |
| Unique values, insertion order | `LinkedHashSet` |
| Unique values, sorted | `TreeSet` |
| Key-value lookup | `HashMap` |
| Key-value lookup with insertion order | `LinkedHashMap` |
| Sorted key-value lookup | `TreeMap` |
| Thread-safe high-concurrency map | `ConcurrentHashMap` |
| Many readers, rare writes list | `CopyOnWriteArrayList` |
| Blocking producer-consumer queue | `BlockingQueue` |

### Interview decision framework

Ask these in order:

```
1. Do I need key-value lookup?
   yes -> Map

2. Do I need uniqueness?
   yes -> Set

3. Do I need ordering?
   insertion order -> LinkedHashMap/LinkedHashSet
   sorted order    -> TreeMap/TreeSet

4. Do I need index access?
   yes -> ArrayList

5. Do multiple threads mutate it?
   yes -> concurrent collection or external synchronization

6. Is read/write pattern special?
   read-heavy -> CopyOnWriteArrayList can be good
   producer-consumer -> BlockingQueue
```

---

## 15. Common Interview Questions and Answers

### Q1. What is the difference between ArrayList and LinkedList?

`ArrayList` is backed by a dynamic array. It gives O(1) random access and fast iteration because references are contiguous. Insert/delete in the middle is O(n) because elements must shift.

`LinkedList` is backed by doubly linked nodes. It has O(n) random access because it must traverse nodes. Insert/delete is O(1) only if the node or iterator position is already known; otherwise finding the position is O(n). It also uses more memory per element.

In most real applications, `ArrayList` is the default choice.

### Q2. How does HashMap work internally?

HashMap computes the key's hash code, spreads the hash bits, maps it to a bucket index, and stores a node containing hash, key, value, and next reference. On lookup, it computes the same bucket and checks nodes using hash and `equals()`.

```
key -> hashCode -> spread -> index -> bucket -> equals -> value
```

### Q3. What happens when two keys have the same hash?

They land in the same bucket. HashMap stores multiple nodes in that bucket. Java 8+ can convert a long bucket chain into a balanced tree to improve worst-case lookup from O(n) to O(log n).

### Q4. Why should keys in HashMap be immutable?

Because the key's hash determines its bucket. If a key's fields used in `hashCode()` change after insertion, future lookup may search a different bucket and fail to find the entry.

### Q5. Difference between HashMap and ConcurrentHashMap?

`HashMap` is not thread-safe. `ConcurrentHashMap` is designed for concurrent access. It allows concurrent reads and locks only affected bins during some updates. It also provides atomic methods such as `compute`, `computeIfAbsent`, and `merge`.

### Q6. Why does ConcurrentHashMap not allow null?

Because in concurrent code, `get(key) == null` must unambiguously mean the key is absent. If null values were allowed, it would be impossible to distinguish "absent" from "present with null" safely under concurrent updates.

### Q7. What is a fail-fast iterator?

A fail-fast iterator detects structural modification outside the iterator and throws `ConcurrentModificationException`. It works using `modCount` and `expectedModCount`.

### Q8. How do you remove elements while iterating?

Use `Iterator.remove()`:

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("x")) {
        it.remove();
    }
}
```

Or use:

```java
list.removeIf(x -> x.equals("x"));
```

### Q9. What is load factor in HashMap?

Load factor controls how full the table can get before resizing. Default is 0.75. Threshold is:

```
threshold = capacity * loadFactor
```

When size exceeds threshold, HashMap resizes.

### Q10. Difference between HashSet and HashMap?

`HashMap` stores key-value pairs. `HashSet` stores unique values and internally uses a `HashMap` where the set element is stored as the map key and all values point to a dummy object.

### Q11. HashMap vs LinkedHashMap vs TreeMap?

| Map | Ordering | Complexity |
|---|---|---|
| `HashMap` | No guaranteed order | O(1) average |
| `LinkedHashMap` | Insertion/access order | O(1) average |
| `TreeMap` | Sorted by key | O(log n) |

### Q12. Can HashMap have duplicate keys?

No. If you put the same key again, the old value is replaced.

```java
map.put("A", 1);
map.put("A", 2);
System.out.println(map.get("A")); // 2
```

### Q13. What happens if hashCode returns constant?

All keys land in the same bucket.

```java
@Override
public int hashCode() {
    return 1;
}
```

Performance degrades because HashMap loses distribution. Java 8 treeification can help, but this is still a bad implementation.

### Q14. Is HashMap ordered?

No. Any observed order is implementation detail and must not be relied on.

Use `LinkedHashMap` for insertion order and `TreeMap` for sorted order.

### Q15. What is the difference between capacity and size?

`size` is the number of entries currently stored.

`capacity` is the number of buckets in the internal table.

---

## 16. Quick Reference Cheat Sheet

### Default choices

```
List           -> ArrayList
Set            -> HashSet
Map            -> HashMap
Deque          -> ArrayDeque
Concurrent Map -> ConcurrentHashMap
Sorted Set     -> TreeSet
Sorted Map     -> TreeMap
```

### Most important traps

| Trap | Correct understanding |
|---|---|
| LinkedList insert is always O(1) | Only if node/iterator is already known |
| HashMap is always O(1) | Average O(1), worst case can degrade |
| HashMap preserves order | No guaranteed order |
| ConcurrentHashMap allows null | It does not |
| Fail-fast is guaranteed | Best-effort bug detection |
| equals alone is enough for HashMap key | Must override hashCode too |
| Mutable key is fine | Dangerous if hash/equals fields mutate |

### Strong interview summary

> The Java Collections Framework is a set of data structures with different contracts: List for ordered indexed data, Set for uniqueness, Map for key-value lookup, Queue/Deque for processing order. The best choice depends on access pattern, ordering, uniqueness, mutation frequency, and concurrency. For interviews, the most important internals are ArrayList resizing, LinkedList node traversal, HashMap hashing/collisions/resizing, HashSet as a HashMap wrapper, ConcurrentHashMap concurrency behavior, and fail-fast iterator mechanics.

