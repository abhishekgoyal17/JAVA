# Java Core Interview Question Bank
### 100 Questions Covering OOP, JVM, Memory, Exceptions, Collections, Generics, Strings, and Modern Java

> A focused interview-prep question bank for Java Core rounds. Each answer is written the way you should explain it in an interview: direct answer first, then internals, example, and trap where needed.

---

## Table of Contents

1. [Section 1: Java Fundamentals and OOP](#section-1-java-fundamentals-and-oop)
2. [Section 2: Object Class, equals, hashCode, and String](#section-2-object-class-equals-hashcode-and-string)
3. [Section 3: Collections Framework](#section-3-collections-framework)
4. [Section 4: Generics and Type Erasure](#section-4-generics-and-type-erasure)
5. [Section 5: Exceptions](#section-5-exceptions)
6. [Section 6: JVM, Memory, GC, and JIT](#section-6-jvm-memory-gc-and-jit)
7. [Section 7: Java 8 to Java 21](#section-7-java-8-to-java-21)
8. [Section 8: Mixed Tricky Questions](#section-8-mixed-tricky-questions)
9. [Quick Revision Tables](#quick-revision-tables)
10. [Answer Frameworks](#answer-frameworks)

---

# SECTION 1: JAVA FUNDAMENTALS AND OOP

## Q1. [EASY] Is Java pass-by-value or pass-by-reference?

Java is always pass-by-value.

For primitives, the value itself is copied.

```java
static void change(int x) {
    x = 20;
}

public static void main(String[] args) {
    int a = 10;
    change(a);
    System.out.println(a); // 10
}
```

For objects, the reference value is copied. Both references point to the same object, but the reference variable itself is still passed by value.

```java
static void update(User user) {
    user.name = "Neha";
}

static void reassign(User user) {
    user = new User("Amit");
}
```

ASCII:

```
Before method call:
main.user ----> User{name="Old"}

Inside update(user):
copy ----> same User object

Changing object state affects same object.
Reassigning copy does not change main.user.
```

Interview trap:

> Java passes object references by value, not by reference.

---

## Q2. [EASY] What are the four pillars of OOP?

The four pillars are:

| Pillar | Meaning |
|---|---|
| Encapsulation | Hide internal state and expose controlled methods |
| Abstraction | Expose what an object does, hide how it does it |
| Inheritance | Reuse/extend behavior through IS-A relationship |
| Polymorphism | Same interface, different implementations |

Example:

```java
interface PaymentMethod {
    void pay(int amount);
}

class CardPayment implements PaymentMethod {
    public void pay(int amount) {
        System.out.println("Paid by card");
    }
}

class UpiPayment implements PaymentMethod {
    public void pay(int amount) {
        System.out.println("Paid by UPI");
    }
}
```

Polymorphism:

```java
PaymentMethod method = new UpiPayment();
method.pay(100);
```

The caller depends on the abstraction, not the implementation.

---

## Q3. [MID] Difference between abstraction and encapsulation?

Encapsulation is about hiding data. Abstraction is about hiding implementation complexity.

| Concept | Question it answers | Example |
|---|---|---|
| Encapsulation | Who can access/modify data? | private fields + public methods |
| Abstraction | What operations are exposed? | interface/API |

Example:

```java
class BankAccount {
    private int balance; // encapsulated

    public void deposit(int amount) { // abstracted operation
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        balance += amount;
    }
}
```

Interview line:

> Encapsulation protects state. Abstraction simplifies usage.

---

## Q4. [MID] Interface vs abstract class?

Use an interface when unrelated classes share a capability. Use an abstract class when related classes share common state or partial implementation.

| Feature | Interface | Abstract class |
|---|---|---|
| Relationship | Capability | Family/base type |
| Multiple inheritance | Class can implement many | Class can extend only one |
| Fields | constants only | instance fields allowed |
| Constructors | no | yes |
| Default methods | yes | concrete methods yes |

Example:

```java
interface Payable {
    void pay();
}

abstract class Employee {
    protected final String id;

    protected Employee(String id) {
        this.id = id;
    }

    abstract int salary();
}
```

Strong interview answer:

> If the relationship is "can do", prefer interface. If the relationship is "is a kind of" with shared state/logic, consider abstract class.

---

## Q5. [EASY] What is method overloading?

Overloading means same method name with different parameter lists in the same class.

```java
void print(String value) { }
void print(int value) { }
void print(String value, int count) { }
```

It is compile-time polymorphism.

Return type alone is not enough:

```java
// int get() { return 1; }
// String get() { return "x"; } // compile error
```

Why?

Caller would be ambiguous:

```java
get();
```

---

## Q6. [EASY] What is method overriding?

Overriding means a subclass provides a new implementation for a superclass method with the same signature.

```java
class Animal {
    void speak() {
        System.out.println("sound");
    }
}

class Dog extends Animal {
    @Override
    void speak() {
        System.out.println("bark");
    }
}
```

Runtime polymorphism:

```java
Animal animal = new Dog();
animal.speak(); // bark
```

Method call is resolved based on runtime object type.

---

## Q7. [MID] Overloading vs overriding?

| Feature | Overloading | Overriding |
|---|---|---|
| Where | Same class or subclass | Subclass |
| Signature | Must differ | Must be same |
| Return type | Can differ only if params differ | Same or covariant |
| Resolution | Compile time | Runtime |
| Polymorphism | Compile-time | Runtime |

Trap:

Overloaded method selection is based on reference type at compile time.

```java
class Test {
    void print(Object o) { System.out.println("Object"); }
    void print(String s) { System.out.println("String"); }
}

Object value = "Java";
new Test().print(value); // Object
```

Even though runtime object is String, overload resolution used compile-time type `Object`.

---

## Q8. [MID] What is constructor chaining?

Constructor chaining means one constructor calls another constructor using `this()` or superclass constructor using `super()`.

```java
class User {
    private final String name;
    private final int age;

    User(String name) {
        this(name, 18);
    }

    User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

Rules:

- `this()` or `super()` must be the first statement.
- If you do not call `super()`, compiler inserts `super()` automatically.

Object construction flow:

```
Object constructor
  -> Parent constructor
      -> Child constructor
```

---

## Q9. [MID] What is the difference between final, finally, and finalize?

| Keyword/method | Meaning |
|---|---|
| `final` | Prevent reassignment, overriding, or inheritance depending on use |
| `finally` | Block that runs after try/catch for cleanup |
| `finalize()` | Old GC callback method, deprecated and should not be used |

Example:

```java
final int x = 10;
// x = 20; // compile error

try {
    risky();
} finally {
    cleanup();
}
```

Interview warning:

> Do not rely on `finalize()` for cleanup. Use try-with-resources and `AutoCloseable`.

---

## Q10. [MID] What does final mean for variables, methods, and classes?

```java
final int value = 10;        // cannot reassign
final class Utility { }      // cannot extend
final void process() { }     // cannot override
```

For object references:

```java
final List<String> list = new ArrayList<>();
list.add("A"); // allowed
// list = new ArrayList<>(); // not allowed
```

`final` prevents reassignment of the reference, not mutation of the object.

ASCII:

```
final list ----> ArrayList object
                   |
                   +-- contents can change

list cannot point to another object.
```

---

## Q11. [MID] What is immutability?

An immutable object cannot change state after construction.

Example:

```java
public final class Money {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }
}
```

Rules:

```
1. Make class final or restrict subclassing
2. Make fields private final
3. Do not expose setters
4. Defensively copy mutable inputs/outputs
5. Ensure contained objects are immutable or protected
```

Benefits:

- Thread-safe for reads
- Safe as HashMap keys
- Easier reasoning
- No defensive synchronization for state changes

---

## Q12. [HARD] Why can inheritance break encapsulation?

Subclass can depend on superclass internals and override behavior in unexpected ways.

Example:

```java
class CounterSet<E> extends HashSet<E> {
    int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }
}
```

Problem:

`HashSet.addAll()` internally calls `add()`. Count may double.

Interview lesson:

> Prefer composition over inheritance when you only want to reuse behavior. Inheritance is best when there is a true substitutable IS-A relationship.

---

# SECTION 2: OBJECT CLASS, EQUALS, HASHCODE, AND STRING

## Q13. [EASY] What is the difference between `==` and `equals()`?

For primitives, `==` compares values.

For objects, `==` compares references.

`equals()` compares logical equality if overridden.

```java
String a = new String("java");
String b = new String("java");

System.out.println(a == b);      // false
System.out.println(a.equals(b)); // true
```

ASCII:

```
a -> String("java")
b -> String("java")

Different objects, same content.
```

---

## Q14. [MID] What is the equals contract?

`equals()` must be:

```
1. Reflexive: x.equals(x)
2. Symmetric: x.equals(y) == y.equals(x)
3. Transitive: x==y and y==z means x==z
4. Consistent: repeated calls give same result if state unchanged
5. Null-safe: x.equals(null) is false
```

If you violate this, collections like HashSet and HashMap can behave incorrectly.

---

## Q15. [MID] What is the hashCode contract?

Rules:

```
If a.equals(b), then a.hashCode() == b.hashCode()
If a.hashCode() == b.hashCode(), a and b may still be unequal
hashCode should stay stable while object is used in hash collections
```

Hash collision is allowed:

```
different objects -> same hash bucket -> equals decides exact match
```

Contract violation:

```java
user1.equals(user2) == true
user1.hashCode() != user2.hashCode() // broken
```

---

## Q16. [HARD] Why must equals and hashCode be overridden together?

HashMap first uses `hashCode()` to find bucket, then uses `equals()` inside that bucket.

```
key -> hashCode -> bucket index -> equals -> exact key
```

If equal objects have different hash codes:

```
put equal key A -> bucket 3
get equal key B -> bucket 8

HashMap never checks bucket 3.
Result: lookup fails.
```

Example:

```java
class User {
    int id;

    @Override
    public boolean equals(Object o) {
        return o instanceof User && id == ((User) o).id;
    }

    // hashCode missing: broken for HashMap/HashSet
}
```

---

## Q17. [MID] Why should HashMap keys be immutable?

HashMap stores entries based on hash at insertion time.

If key fields used in hash change later, lookup may search a different bucket.

```java
User key = new User("a@test.com");
map.put(key, "Amit");

key.email = "b@test.com";
map.get(key); // usually null
```

ASCII:

```
Before mutation:
hash("a@test.com") -> bucket 4

After mutation:
hash("b@test.com") -> bucket 9

Entry still lives in bucket 4.
Lookup searches bucket 9.
```

---

## Q18. [EASY] Why is String immutable?

String is immutable for:

- String pool safety
- Security
- Thread-safety
- Hash code caching
- Class loading reliability

String pool:

```java
String a = "admin";
String b = "admin";
```

Both can point to same object:

```
a ----+
      v
   "admin"
      ^
b ----+
```

If String were mutable, changing `a` would unexpectedly change `b`.

---

## Q19. [MID] String literal vs new String?

```java
String a = "java";
String b = "java";
String c = new String("java");
```

Results:

```java
a == b      // true
a == c      // false
a.equals(c) // true
```

ASCII:

```
String Pool:
  "java" <---- a
     ^
     |
     b

Heap:
  new String("java") <---- c
```

---

## Q20. [MID] What does intern() do?

`intern()` returns the canonical pooled String.

```java
String a = "java";
String b = new String("java").intern();

System.out.println(a == b); // true
```

If an equal String exists in the pool, `intern()` returns that reference. Otherwise, a pooled representation is created/used.

---

## Q21. [MID] Why is StringBuilder preferred for loop concatenation?

String is immutable, so repeated `+=` can create many intermediate objects.

Bad:

```java
String result = "";
for (int i = 0; i < 10_000; i++) {
    result += i;
}
```

Better:

```java
StringBuilder builder = new StringBuilder();
for (int i = 0; i < 10_000; i++) {
    builder.append(i);
}
String result = builder.toString();
```

Interview nuance:

Simple string concatenation is fine:

```java
String message = "Hello " + name;
```

The problem is repeated concatenation in loops/hot paths.

---

## Q22. [MID] StringBuilder vs StringBuffer?

| Type | Thread-safe | Performance | Use |
|---|---|---|---|
| `StringBuilder` | No | Faster | Normal local string building |
| `StringBuffer` | Yes, synchronized | Slower | Legacy synchronized need |

Use `StringBuilder` by default.

---

## Q23. [MID] Explain Integer caching.

Java caches small wrapper values.

```java
Integer a = 127;
Integer b = 127;
System.out.println(a == b); // true

Integer x = 128;
Integer y = 128;
System.out.println(x == y); // false usually
```

Default `Integer` cache range:

```
-128 to 127
```

Use `equals()` for wrapper value comparison.

---

## Q24. [MID] How can unboxing cause NullPointerException?

```java
Integer value = null;
int x = value; // NullPointerException
```

Compiler inserts:

```java
int x = value.intValue();
```

Calling a method on null causes NPE.

---

# SECTION 3: COLLECTIONS FRAMEWORK

## Q25. [EASY] Collection vs Collections?

`Collection` is an interface.

`Collections` is a utility class.

```java
Collection<String> values = new ArrayList<>();
Collections.sort(list);
```

`Map` is not a subtype of `Collection`.

Hierarchy:

```
Iterable
  |
  +-- Collection
        +-- List
        +-- Set
        +-- Queue

Map is separate.
```

---

## Q26. [EASY] List vs Set vs Map?

| Type | Meaning |
|---|---|
| `List` | Ordered, duplicates allowed, index-based |
| `Set` | Unique elements |
| `Map` | Key-value pairs, unique keys |

Example:

```java
List<String> list = List.of("A", "A"); // duplicates allowed
Set<String> set = Set.of("A", "B");    // unique
Map<String, Integer> map = Map.of("A", 1);
```

---

## Q27. [MID] ArrayList vs LinkedList?

`ArrayList` is backed by a dynamic array. `LinkedList` is backed by doubly linked nodes.

| Operation | ArrayList | LinkedList |
|---|---:|---:|
| get(index) | O(1) | O(n) |
| add end | amortized O(1) | O(1) |
| add middle | O(n) shift | O(n) traverse + O(1) link |
| remove middle | O(n) shift | O(n) traverse + O(1) unlink |
| memory | lower | higher |
| iteration | faster | slower |

Interview line:

> ArrayList is the default choice for most List use cases. LinkedList is rarely better unless you already have iterator/node position or need deque-like behavior.

---

## Q28. [MID] How does ArrayList resizing work?

ArrayList uses an internal array.

When the array is full:

```
1. Create bigger array
2. Copy old elements
3. Add new element
4. Replace internal array reference
```

ASCII:

```
Before:
[A][B][C]

Resize:
[A][B][C] ----copy----> [A][B][C][_][_]

After add:
[A][B][C][D][_]
```

`add()` at end is amortized O(1), but resizing itself is O(n).

---

## Q29. [HARD] Is LinkedList insertion O(1)?

Only if the node or iterator position is already known.

If you insert by index:

```java
list.add(5000, value);
```

Java must first traverse to index 5000:

```
head -> node1 -> node2 -> ... -> node5000
```

Total cost:

```
O(n) traversal + O(1) link update = O(n)
```

This is a common interview trap.

---

## Q30. [MID] How does HashMap work internally?

HashMap stores key-value pairs in a bucket array.

Flow:

```
key -> hashCode() -> spread hash -> bucket index -> node chain/tree
```

Node contains:

```java
hash
key
value
next
```

Lookup:

```
1. Compute hash
2. Find bucket
3. Compare hash
4. Compare key using equals()
5. Return value
```

---

## Q31. [HARD] How does HashMap handle collisions?

Collision means multiple keys land in the same bucket.

Before Java 8:

```
bucket -> linked list
```

Java 8+:

```
small bucket -> linked list
large bucket -> balanced tree
```

ASCII:

```
bucket 5:
A -> B -> C -> D

If too large:
        C
       / \
      A   D
       \
        B
```

Treeification improves worst-case lookup from O(n) to O(log n) within that bucket.

---

## Q32. [MID] What is load factor in HashMap?

Load factor controls when HashMap resizes.

Default:

```
capacity = 16
loadFactor = 0.75
threshold = 12
```

Formula:

```
threshold = capacity * loadFactor
```

When size exceeds threshold, HashMap resizes and redistributes entries.

---

## Q33. [MID] Can HashMap store null?

Yes.

```java
map.put(null, "value");
map.put("key", null);
```

HashMap allows:

- One null key
- Multiple null values

ConcurrentHashMap does not allow null keys or null values.

---

## Q34. [MID] HashMap vs Hashtable vs ConcurrentHashMap?

| Feature | HashMap | Hashtable | ConcurrentHashMap |
|---|---|---|---|
| Thread-safe | No | Yes | Yes |
| Locking | none | whole map | finer-grained/bin-level |
| Null keys/values | allowed | not allowed | not allowed |
| Performance | fast single-thread | slower | good concurrency |
| Legacy | no | yes | no |

Use `ConcurrentHashMap` for concurrent maps.

---

## Q35. [HARD] Why does ConcurrentHashMap not allow null?

In concurrent code, `get(key) == null` must clearly mean key is absent.

If null values were allowed:

```
null could mean:
1. key absent
2. key present with null value
3. key removed by another thread during check
```

To avoid ambiguity, null keys and values are forbidden.

---

## Q36. [MID] HashSet vs HashMap?

HashSet stores unique elements. HashMap stores key-value pairs.

Internally, HashSet uses HashMap.

```java
set.add("A");
```

Conceptually:

```java
map.put("A", PRESENT);
```

ASCII:

```
HashSet element -> HashMap key
dummy object    -> HashMap value
```

---

## Q37. [MID] HashMap vs LinkedHashMap vs TreeMap?

| Map | Ordering | Complexity |
|---|---|---|
| HashMap | no guaranteed order | O(1) average |
| LinkedHashMap | insertion/access order | O(1) average |
| TreeMap | sorted by key | O(log n) |

Use `LinkedHashMap` for predictable iteration order.
Use `TreeMap` for sorted keys/range queries.

---

## Q38. [MID] What is fail-fast iterator?

Fail-fast iterator detects structural modification outside the iterator and throws `ConcurrentModificationException`.

Example:

```java
for (String item : list) {
    if (item.equals("A")) {
        list.remove(item); // ConcurrentModificationException
    }
}
```

Mechanism:

```
collection.modCount
iterator.expectedModCount

if different -> throw ConcurrentModificationException
```

Correct removal:

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("A")) {
        it.remove();
    }
}
```

---

## Q39. [HARD] Is ConcurrentModificationException guaranteed?

No. Fail-fast behavior is best-effort.

It is designed to detect bugs during iteration, not to provide a concurrency guarantee.

Interview line:

> You should not write logic that depends on ConcurrentModificationException. Use proper synchronization or concurrent collections.

---

## Q40. [MID] Comparable vs Comparator?

`Comparable` defines natural ordering inside the class.

```java
class User implements Comparable<User> {
    int age;

    public int compareTo(User other) {
        return Integer.compare(this.age, other.age);
    }
}
```

`Comparator` defines external/custom ordering.

```java
Comparator<User> byName = Comparator.comparing(User::getName);
Comparator<User> byAge = Comparator.comparingInt(User::getAge);
```

Use Comparator when:

- Multiple sort orders exist
- You cannot modify the class
- Sorting logic should stay outside domain model

---

# SECTION 4: GENERICS AND TYPE ERASURE

## Q41. [EASY] What are generics?

Generics allow classes, interfaces, and methods to use type parameters with compile-time type safety.

```java
List<String> names = new ArrayList<>();
names.add("Java");
// names.add(10); // compile error
```

Benefits:

- Compile-time type checking
- No manual casts
- More reusable APIs

---

## Q42. [MID] What is type erasure?

Type erasure means generic type information is removed at compile time.

Source:

```java
List<String> names = new ArrayList<>();
String name = names.get(0);
```

Conceptually after erasure:

```java
List names = new ArrayList();
String name = (String) names.get(0);
```

At runtime:

```
List<String> -> List
List<Integer> -> List
```

Reason: backward compatibility with pre-Java-5 code.

---

## Q43. [MID] Why is `List<Integer>` not a subtype of `List<Number>`?

Generics are invariant.

Even though:

```java
Integer extends Number
```

This is not true:

```java
List<Integer> extends List<Number>
```

If it were allowed:

```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = ints; // imagine allowed
nums.add(3.14);           // Double added to Integer list
```

That breaks type safety.

---

## Q44. [MID] What is wildcard `?`?

`?` means unknown type.

```java
void print(List<?> values) {
    for (Object value : values) {
        System.out.println(value);
    }
}
```

You can read values as Object, but cannot add non-null values because actual element type is unknown.

```java
// values.add("A"); // compile error
values.add(null);   // allowed
```

---

## Q45. [HARD] Explain `? extends T`.

`? extends T` means unknown subtype of T.

```java
void printNumbers(List<? extends Number> numbers) {
    Number n = numbers.get(0); // safe
    // numbers.add(10);        // not safe
}
```

Can accept:

```
List<Integer>
List<Double>
List<Number>
```

Cannot add specific Number subtype because actual list might be `List<Double>` or `List<Integer>`.

Use when the list produces values.

---

## Q46. [HARD] Explain `? super T`.

`? super T` means unknown supertype of T.

```java
void addIntegers(List<? super Integer> values) {
    values.add(10); // safe
    Object x = values.get(0); // only Object safely
}
```

Can accept:

```
List<Integer>
List<Number>
List<Object>
```

Use when the list consumes values.

---

## Q47. [MID] What is PECS?

PECS means:

```
Producer Extends, Consumer Super
```

If you read from a source:

```java
List<? extends Number>
```

If you write into a destination:

```java
List<? super Integer>
```

Copy example:

```java
static <T> void copy(List<? extends T> source, List<? super T> destination) {
    for (T item : source) {
        destination.add(item);
    }
}
```

---

## Q48. [MID] Why can we not create `new T()`?

Because generic type `T` is erased at runtime.

```java
class Box<T> {
    // T value = new T(); // compile error
}
```

JVM does not know which concrete class to instantiate.

Use `Supplier<T>`:

```java
class Factory<T> {
    private final Supplier<T> supplier;

    Factory(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    T create() {
        return supplier.get();
    }
}
```

---

## Q49. [HARD] What is heap pollution?

Heap pollution occurs when a parameterized variable points to data of an incompatible type.

```java
List<String> strings = new ArrayList<>();
List raw = strings;
raw.add(100);

String value = strings.get(0); // ClassCastException
```

Cause:

- Raw types
- Unchecked casts
- Varargs with generics

Avoid raw types unless dealing with legacy code.

---

## Q50. [HARD] What are bridge methods?

Bridge methods are compiler-generated methods used to preserve polymorphism after type erasure.

Example:

```java
class Parent<T> {
    T get() { return null; }
}

class Child extends Parent<String> {
    @Override
    String get() { return "Java"; }
}
```

After erasure, parent method returns Object, child returns String. Compiler adds a synthetic bridge so overridden calls still work polymorphically.

Interview line:

> Bridge methods are generated by the compiler to maintain method overriding after generic type erasure.

---

# SECTION 5: EXCEPTIONS

## Q51. [EASY] What is an exception?

An exception is an event that disrupts normal program flow.

```
method1 -> method2 -> method3 -> exception
                              |
                              v
                    stack unwinds until catch found
```

If no handler is found, the thread terminates and stack trace is printed.

---

## Q52. [EASY] Explain exception hierarchy.

```
Throwable
├── Error
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── IOException
    ├── SQLException
    └── RuntimeException
        ├── NullPointerException
        ├── IllegalArgumentException
        └── IllegalStateException
```

Rules:

```
Error -> JVM/system problem, generally do not catch
Checked Exception -> compiler forces handling
RuntimeException -> unchecked, usually programming bug
```

---

## Q53. [MID] Checked vs unchecked exceptions?

| Type | Compiler forces handling? | Examples | Use for |
|---|---|---|---|
| Checked | Yes | IOException, SQLException | recoverable external failures |
| Unchecked | No | NullPointerException, IllegalArgumentException | programming errors, invalid arguments |

Example checked:

```java
void read() throws IOException {
    Files.readString(Path.of("config.txt"));
}
```

Example unchecked:

```java
void withdraw(int amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("amount must be positive");
    }
}
```

---

## Q54. [MID] throw vs throws?

`throw` actually throws an exception object.

```java
throw new IllegalArgumentException("bad input");
```

`throws` declares that a method may throw an exception.

```java
void readFile() throws IOException {
    Files.readString(Path.of("a.txt"));
}
```

Mnemonic:

```
throw  -> action
throws -> declaration
```

---

## Q55. [MID] finally block behavior?

`finally` runs whether exception occurs or not.

```java
try {
    risky();
} catch (Exception e) {
    handle(e);
} finally {
    cleanup();
}
```

Use for cleanup.

But prefer try-with-resources for resources:

```java
try (BufferedReader reader = Files.newBufferedReader(path)) {
    return reader.readLine();
}
```

---

## Q56. [HARD] Can finally override return?

Yes, and it is a bad practice.

```java
static int test() {
    try {
        return 1;
    } finally {
        return 2;
    }
}
```

Output:

```java
2
```

Avoid returning from finally because it hides exceptions and confuses control flow.

---

## Q57. [MID] What is try-with-resources?

It automatically closes resources implementing `AutoCloseable`.

```java
try (Connection connection = dataSource.getConnection();
     PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.executeQuery();
}
```

Equivalent idea:

```
open resource
try work
finally close resource
```

It also handles suppressed exceptions correctly.

---

## Q58. [HARD] What are suppressed exceptions?

Suppressed exceptions happen when both try block and close operation throw exceptions.

```java
try (Resource r = new Resource()) {
    throw new RuntimeException("work failed");
}
```

If `r.close()` also throws, the close exception is attached as suppressed to the primary exception.

```java
Throwable[] suppressed = exception.getSuppressed();
```

This prevents losing cleanup failures while preserving the original failure.

---

## Q59. [MID] When should you create custom exceptions?

Create custom exceptions when they represent meaningful domain/application failures.

```java
class InsufficientBalanceException extends RuntimeException {
    InsufficientBalanceException(String message) {
        super(message);
    }
}
```

Good:

```java
throw new InsufficientBalanceException("Balance is too low");
```

Bad:

```java
throw new RuntimeException("error");
```

Custom exceptions improve API clarity and error handling.

---

## Q60. [MID] Exception best practices?

Rules:

```
1. Catch specific exceptions
2. Do not swallow exceptions silently
3. Preserve cause using exception chaining
4. Use unchecked for programming/domain validation failures
5. Use checked only when caller can reasonably recover
6. Use try-with-resources for cleanup
7. Do not catch Throwable/Error unless at very controlled boundaries
```

Exception chaining:

```java
throw new OrderProcessingException("Failed to process order", e);
```

---

# SECTION 6: JVM, MEMORY, GC, AND JIT

## Q61. [EASY] What is JVM, JRE, and JDK?

| Term | Meaning |
|---|---|
| JVM | Runs Java bytecode |
| JRE | JVM + runtime libraries |
| JDK | JRE + compiler/tools for development |

Flow:

```
Java source (.java)
  |
  javac
  v
Bytecode (.class)
  |
  JVM
  v
Machine execution
```

---

## Q62. [MID] How does Java code execute?

Execution flow:

```
.java source
  |
  javac compiler
  v
.class bytecode
  |
  ClassLoader loads classes
  |
  Bytecode verifier checks safety
  |
  Interpreter starts execution
  |
  JIT compiles hot methods to native code
```

The JVM first interprets bytecode. Frequently executed code becomes "hot" and is compiled by JIT into optimized machine code.

---

## Q63. [MID] Stack vs heap memory?

| Feature | Stack | Heap |
|---|---|---|
| Scope | per-thread | shared across threads |
| Stores | frames, local primitives, references | objects and arrays |
| Lifecycle | method call/return | garbage collected |
| Error | StackOverflowError | OutOfMemoryError |
| Thread safety | private by default | needs synchronization for shared mutable state |

ASCII:

```
Thread stack:
  main frame
    int x = 10
    user ref ----+
                 |
Heap:            v
              User object
```

---

## Q64. [MID] What is garbage collection?

Garbage collection automatically reclaims memory from unreachable objects.

Object is eligible for GC when no live reference can reach it.

```
GC roots
  |
  +--> reachable object A
  +--> reachable object B

unreachable object C -> eligible for GC
```

GC roots include:

- Local variables in active stack frames
- Static fields
- Active threads
- JNI references

---

## Q65. [MID] Minor GC vs Major GC vs Full GC?

| GC type | Area | Meaning |
|---|---|---|
| Minor GC | Young generation | Collects short-lived objects |
| Major GC | Old generation | Collects long-lived area |
| Full GC | Whole heap/metaspace depending collector | Most expensive, often stop-the-world |

Young generation:

```
Eden -> Survivor -> Old Gen
```

Most objects die young, so young generation collection is frequent.

---

## Q66. [HARD] What is stop-the-world pause?

Stop-the-world means application threads are paused while JVM performs certain GC work.

```
App threads running
      |
      v
Stop all application threads
      |
      v
GC work
      |
      v
Resume application threads
```

Why it matters:

- Latency spikes
- User request delays
- Missed SLAs in low-latency systems

Modern collectors like G1, ZGC, and Shenandoah reduce pause times by doing more work concurrently.

---

## Q67. [MID] What is memory leak in Java?

Java memory leak means objects are no longer useful but are still reachable, so GC cannot collect them.

Example:

```java
static List<byte[]> cache = new ArrayList<>();

void leak() {
    cache.add(new byte[1024 * 1024]);
}
```

The list keeps references alive.

Common leak sources:

- Static collections
- Unbounded caches
- Listener not removed
- ThreadLocal not removed in thread pools
- ClassLoader leaks

---

## Q68. [MID] What is JIT compiler?

JIT converts frequently executed bytecode into optimized native machine code at runtime.

Flow:

```
Bytecode
  |
  Interpreter starts running
  |
  JVM profiles hot methods
  |
  JIT compiles hot methods
  |
  Native CPU instructions run faster
```

Optimizations:

- Method inlining
- Escape analysis
- Dead code elimination
- Lock elimination
- Loop optimizations

---

## Q69. [HARD] What is escape analysis?

Escape analysis checks whether an object escapes the method/thread where it was created.

```java
void process() {
    Point p = new Point(1, 2);
    int sum = p.x + p.y;
}
```

If `p` does not escape, JIT may optimize allocation:

```
No real heap allocation
Fields may become local variables
Less GC pressure
```

This is one reason short-lived objects can be cheaper than expected in optimized code.

---

## Q70. [MID] What causes StackOverflowError and OutOfMemoryError?

`StackOverflowError` usually happens due to deep/infinite recursion.

```java
void recurse() {
    recurse();
}
```

Each call pushes a stack frame until stack is full.

`OutOfMemoryError` happens when JVM cannot allocate memory, commonly heap or metaspace.

```java
List<byte[]> list = new ArrayList<>();
while (true) {
    list.add(new byte[1024 * 1024]);
}
```

---

# SECTION 7: JAVA 8 TO JAVA 21

## Q71. [EASY] What are the major Java 8 features?

Major Java 8 features:

- Lambdas
- Functional interfaces
- Streams
- Optional
- Default methods in interfaces
- New Date/Time API
- CompletableFuture improvements

These changed Java toward more functional and declarative programming.

---

## Q72. [EASY] What is a lambda?

A lambda is a concise implementation of a functional interface.

```java
Runnable r = () -> System.out.println("run");
Comparator<String> c = (a, b) -> a.length() - b.length();
```

Mental model:

```
lambda -> implementation of single abstract method
```

---

## Q73. [MID] What is a functional interface?

An interface with exactly one abstract method.

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}
```

It can have default and static methods.

Common built-ins:

| Interface | Method | Use |
|---|---|---|
| Predicate | test | condition |
| Function | apply | transform |
| Consumer | accept | consume |
| Supplier | get | supply |

---

## Q74. [MID] What is effectively final?

A local variable captured by a lambda must be final or effectively final.

```java
int factor = 10;
Function<Integer, Integer> f = x -> x * factor;
// factor = 20; // not allowed
```

Effectively final means:

```
not declared final, but never reassigned
```

Reason:

Local variables live on stack, and lambdas may outlive the method call. Java captures stable values only.

---

## Q75. [MID] What is a Stream?

A Stream is a pipeline for processing data.

```java
List<String> result = names.stream()
    .filter(n -> n.length() > 3)
    .map(String::toUpperCase)
    .toList();
```

Pipeline:

```
source -> intermediate operations -> terminal operation
```

Intermediate operations are lazy. Terminal operation triggers execution.

---

## Q76. [MID] map vs flatMap?

`map` transforms one element into one output.

```java
names.stream()
    .map(String::length)
    .toList();
```

`flatMap` transforms one element into many elements and flattens.

```java
groups.stream()
    .flatMap(List::stream)
    .toList();
```

ASCII:

```
map:
[A, B] -> List object
[C, D] -> List object

flatMap:
[A, B], [C, D] -> A, B, C, D
```

---

## Q77. [MID] Are streams lazy?

Yes, intermediate operations are lazy.

```java
Stream<String> stream = names.stream()
    .filter(name -> {
        System.out.println(name);
        return true;
    });

// Nothing printed yet.

stream.count(); // now filter runs
```

Terminal operations:

```java
count()
collect()
toList()
forEach()
reduce()
findFirst()
```

---

## Q78. [HARD] When should you avoid parallel streams?

Avoid parallel streams when:

- Data set is small
- Work is blocking I/O
- Operation mutates shared state
- Ordering matters
- Code runs inside a server where common ForkJoinPool impact is unknown
- Task is not CPU-heavy enough to justify overhead

Parallel streams are best for large, CPU-heavy, independent operations.

---

## Q79. [MID] What is Optional?

`Optional<T>` represents a value that may be present or absent.

```java
Optional<User> user = repository.findById(id);

String email = user
    .map(User::getEmail)
    .orElse("unknown");
```

Best use:

```
Return type for possibly missing value
```

Usually avoid:

```
Optional fields
Optional method parameters
Calling get() without checking
```

---

## Q80. [MID] orElse vs orElseGet?

`orElse` evaluates default immediately.

```java
optional.orElse(expensiveDefault());
```

`orElseGet` evaluates lazily only if empty.

```java
optional.orElseGet(() -> expensiveDefault());
```

Use `orElseGet` for expensive defaults.

---

## Q81. [MID] What are default methods in interfaces?

Default methods allow interfaces to provide method implementations.

```java
interface Vehicle {
    void start();

    default void stop() {
        System.out.println("stop");
    }
}
```

Why added:

```
To evolve interfaces without breaking existing implementations.
```

If two interfaces provide same default method, implementing class must override and resolve conflict.

---

## Q82. [MID] What is var?

`var` is local variable type inference.

```java
var name = "Java"; // String
var count = 10;    // int
```

Java remains statically typed. The compiler infers a fixed type.

Not allowed:

```java
// var x;
// var y = null;
// class A { var field; }
```

---

## Q83. [MID] What are records?

Records are concise immutable data carriers.

```java
public record UserDto(long id, String email) { }
```

Compiler generates:

- final fields
- constructor
- accessors
- equals
- hashCode
- toString

Good for DTOs, value objects, API responses.

Not ideal for mutable JPA entities or complex lifecycle objects.

---

## Q84. [HARD] What are sealed classes?

Sealed classes restrict which classes can extend or implement them.

```java
sealed interface Payment permits CardPayment, UpiPayment { }

final class CardPayment implements Payment { }
final class UpiPayment implements Payment { }
```

Use for closed domain hierarchies:

```
Payment can only be CardPayment or UpiPayment.
```

Benefits:

- Better domain modeling
- Exhaustive switch
- Prevent unknown subclasses

---

## Q85. [MID] What is pattern matching for instanceof?

It combines type check and cast.

Before:

```java
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}
```

After:

```java
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

Less boilerplate, safer casting.

---

## Q86. [MID] What are switch expressions?

Switch expressions return a value and avoid fall-through.

```java
String label = switch (status) {
    case 1 -> "NEW";
    case 2 -> "PAID";
    default -> "UNKNOWN";
};
```

Block syntax uses `yield`:

```java
String result = switch (code) {
    case 400 -> {
        log.warn("bad request");
        yield "BAD";
    }
    default -> "OK";
};
```

---

## Q87. [HARD] What are virtual threads?

Virtual threads are lightweight JVM-managed threads finalized in Java 21.

Traditional:

```
Java thread -> OS thread
```

Virtual:

```
Virtual thread -> JVM scheduler -> carrier OS thread
```

They are excellent for blocking I/O workloads.

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> callRemoteService());
}
```

Important:

> Virtual threads improve concurrency for blocking I/O. They do not make CPU-bound work faster.

---

## Q88. [HARD] thenApply vs thenCompose?

`thenApply` transforms a value:

```java
CompletableFuture<String> name =
    userFuture.thenApply(User::getName);
```

`thenCompose` flattens nested futures:

```java
CompletableFuture<Order> order =
    userFuture.thenCompose(user -> fetchOrder(user.id()));
```

Mnemonic:

```
thenApply:   T -> R
thenCompose: T -> CompletableFuture<R>
```

---

# SECTION 8: MIXED TRICKY QUESTIONS

## Q89. [TRAP] What is printed?

```java
String a = "ja" + "va";
String b = "java";
System.out.println(a == b);
```

Answer:

```java
true
```

Reason:

`"ja" + "va"` is a compile-time constant and is folded to `"java"`, so both references point to same pooled String.

---

## Q90. [TRAP] What is printed?

```java
String part = "ja";
String a = part + "va";
String b = "java";
System.out.println(a == b);
```

Answer:

```java
false
```

Reason:

This is runtime concatenation because `part` is a variable. It creates a different String object unless interned.

---

## Q91. [TRAP] What is printed?

```java
Integer a = 100;
Integer b = 100;
Integer x = 200;
Integer y = 200;

System.out.println(a == b);
System.out.println(x == y);
```

Answer:

```java
true
false
```

Reason:

100 is inside Integer cache range. 200 is outside default range.

---

## Q92. [TRAP] What happens here?

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");

for (String value : list) {
    list.remove(value);
}
```

Usually throws:

```java
ConcurrentModificationException
```

Reason:

Enhanced for loop uses Iterator internally. Removing directly from list changes `modCount`, but iterator has old `expectedModCount`.

Use `Iterator.remove()` or `removeIf()`.

---

## Q93. [TRAP] What is the output?

```java
class Test {
    void print(Object o) { System.out.println("Object"); }
    void print(String s) { System.out.println("String"); }
}

Object value = "Java";
new Test().print(value);
```

Answer:

```java
Object
```

Reason:

Overloading is compile-time. The compile-time type of `value` is Object.

---

## Q94. [TRAP] What is the output?

```java
class Parent {
    void show() { System.out.println("Parent"); }
}

class Child extends Parent {
    void show() { System.out.println("Child"); }
}

Parent p = new Child();
p.show();
```

Answer:

```java
Child
```

Reason:

Overriding is runtime polymorphism. Method dispatch uses actual object type.

---

## Q95. [TRAP] Can static methods be overridden?

No. Static methods are hidden, not overridden.

```java
class Parent {
    static void show() { System.out.println("Parent"); }
}

class Child extends Parent {
    static void show() { System.out.println("Child"); }
}

Parent p = new Child();
p.show(); // Parent
```

Static method resolution uses reference type.

---

## Q96. [TRAP] Can private methods be overridden?

No. Private methods are not visible to subclasses.

A method with same signature in subclass is a new method, not an override.

```java
class Parent {
    private void test() { }
}

class Child extends Parent {
    private void test() { } // not overriding
}
```

---

## Q97. [TRAP] Is Java fully object-oriented?

Not strictly, because Java has primitives like:

```java
int
boolean
char
double
```

But Java is strongly object-oriented for most application design. Wrappers exist for object representation:

```java
Integer
Boolean
Character
Double
```

---

## Q98. [TRAP] Can an interface have private methods?

Yes, modern Java allows private methods in interfaces to share code between default/static methods.

```java
interface Logger {
    default void info(String message) {
        log("INFO", message);
    }

    private void log(String level, String message) {
        System.out.println(level + ": " + message);
    }
}
```

Private interface methods are helper methods, not part of public contract.

---

## Q99. [TRAP] Difference between shallow copy and deep copy?

Shallow copy copies object fields as-is, including references.

Deep copy copies referenced objects too.

ASCII:

```
Shallow:
original.user ----+
                  v
                Address
copy.user -------+

Deep:
original.user -> Address1
copy.user     -> Address2
```

Shallow copy can cause shared mutable state bugs.

---

## Q100. [TRAP] What is the difference between == and equals for enums?

For enums, `==` is safe and recommended.

```java
enum Status { NEW, PAID }

Status s = Status.NEW;
System.out.println(s == Status.NEW); // true
```

Why:

Enum constants are singletons.

Also, `==` is null-safe in one direction:

```java
Status s = null;
System.out.println(s == Status.NEW); // false
// s.equals(Status.NEW); // NullPointerException
```

---

# QUICK REVISION TABLES

## Core Comparison Table

| Topic | Key interview line |
|---|---|
| Java parameter passing | Always pass-by-value |
| Overloading | Compile-time polymorphism |
| Overriding | Runtime polymorphism |
| Interface | Capability contract |
| Abstract class | Shared base with state/logic |
| final reference | Cannot reassign, object may mutate |
| String | Immutable and pooled |
| equals/hashCode | Must be consistent for hash collections |
| HashMap | hashCode finds bucket, equals finds key |
| ArrayList | Dynamic array |
| LinkedList | Doubly linked nodes |
| Generics | Compile-time type safety |
| Erasure | Generic info mostly removed at runtime |
| Optional | For absent return values |
| Records | Immutable data carriers |
| Virtual threads | Scalable blocking I/O |

## Complexity Table

| Structure | Lookup | Insert | Delete | Notes |
|---|---:|---:|---:|---|
| ArrayList by index | O(1) | end amortized O(1), middle O(n) | O(n) | best default List |
| LinkedList by index | O(n) | O(1) if node known | O(1) if node known | traversal dominates |
| HashMap by key | O(1) avg | O(1) avg | O(1) avg | collision can degrade |
| TreeMap by key | O(log n) | O(log n) | O(log n) | sorted keys |
| HashSet | O(1) avg | O(1) avg | O(1) avg | backed by HashMap |

## Java 8+ Feature Table

| Feature | Use |
|---|---|
| Lambda | Pass behavior concisely |
| Functional interface | Single abstract method target |
| Stream | Declarative data processing |
| Optional | Represent missing return value |
| CompletableFuture | Async composition |
| var | Local type inference |
| text block | Multi-line strings |
| record | Immutable data carrier |
| sealed class | Closed hierarchy |
| pattern matching | Type check + cast |
| switch expression | Value-returning switch |
| virtual thread | Lightweight blocking I/O concurrency |

---

# ANSWER FRAMEWORKS

## Framework 1: Answering "Difference between X and Y"

Use this structure:

```
1. One-line difference
2. Internal working
3. Complexity/performance
4. Use case
5. Interview trap
```

Example:

> ArrayList is array-backed and fast for index access. LinkedList is node-backed and fast only for insertion/removal when node position is already known. In most real applications, ArrayList is preferred because of cache locality and lower memory overhead.

## Framework 2: Answering "How does HashMap work?"

Use this structure:

```
1. Key-value storage
2. hashCode computes bucket
3. equals resolves exact key
4. collisions use list/tree
5. resizing controlled by load factor
6. mutable keys and bad hashCode are traps
```

## Framework 3: Answering "Is this thread-safe?"

Even in Java Core rounds, this question appears often.

```
1. Is there shared mutable state?
2. Can multiple threads access it?
3. Are operations atomic?
4. Is visibility guaranteed?
5. Is ordering guaranteed?
6. Which tool fixes it? synchronized, volatile, concurrent collection, immutable object
```

## Framework 4: Answering "Why did Java add this feature?"

Use this structure:

```
1. Problem before feature
2. Feature syntax
3. Benefit
4. Limitation/trap
5. Real backend use case
```

Example for records:

> Before records, DTOs required a lot of boilerplate. Records provide compact immutable data carriers with generated constructor, accessors, equals, hashCode, and toString. They are excellent for DTOs and value objects, but not ideal for mutable JPA entities.

## Final Revision Advice

For Java Core interviews, master these deeply:

```
1. equals/hashCode/String
2. HashMap and collections complexity
3. Generics, wildcards, PECS, erasure
4. Exception hierarchy and best practices
5. JVM memory model, GC basics, JIT basics
6. Java 8 streams, Optional, lambdas
7. Java 17/21 records, sealed classes, virtual threads
```

