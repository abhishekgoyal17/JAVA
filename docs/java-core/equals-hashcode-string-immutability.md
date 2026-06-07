# equals, hashCode, String Immutability and Wrapper Caching - Complete Interview Deep Dive

> A detailed guide for Java interviews covering object equality, identity, `equals()` and `hashCode()` contracts, HashMap/HashSet behavior, String pool, immutability, StringBuilder, wrapper caching, autoboxing traps, and common Q&A.

---

## Table of Contents

1. [Identity vs Equality](#1-identity-vs-equality)
2. [Default Object Methods](#2-default-object-methods)
3. [equals Contract](#3-equals-contract)
4. [hashCode Contract](#4-hashcode-contract)
5. [equals and hashCode in HashMap](#5-equals-and-hashcode-in-hashmap)
6. [How to Implement equals and hashCode](#6-how-to-implement-equals-and-hashcode)
7. [Common Equality Traps](#7-common-equality-traps)
8. [String Immutability](#8-string-immutability)
9. [String Pool](#9-string-pool)
10. [String Concatenation and StringBuilder](#10-string-concatenation-and-stringbuilder)
11. [Wrapper Caching and Autoboxing](#11-wrapper-caching-and-autoboxing)
12. [Interview Questions and Answers](#12-interview-questions-and-answers)
13. [Quick Reference Cheat Sheet](#13-quick-reference-cheat-sheet)

---

## 1. Identity vs Equality

Java has two common comparison ideas:

```
Identity: Are these references pointing to the exact same object?
Equality: Do these objects represent the same logical value?
```

`==` checks identity for objects:

```java
User a = new User(1, "Amit");
User b = new User(1, "Amit");

System.out.println(a == b); // false
```

Even if values are same, objects are different:

```
Stack:
a ----+
      |
      v
Heap: User{id=1,name=Amit}

b ----+
      |
      v
Heap: User{id=1,name=Amit}
```

`equals()` checks logical equality if the class overrides it:

```java
System.out.println(a.equals(b)); // true if implemented by id/name
```

For primitives, `==` compares values:

```java
int x = 10;
int y = 10;
System.out.println(x == y); // true
```

---

## 2. Default Object Methods

Every Java class ultimately extends `Object`.

Important methods:

```java
public boolean equals(Object obj)
public int hashCode()
public String toString()
```

Default `equals()` in Object behaves like `==`.

Conceptually:

```java
public boolean equals(Object obj) {
    return this == obj;
}
```

Default `hashCode()` is usually derived from object identity, though exact implementation is JVM-specific.

If you do not override `equals()`:

```java
new User(1).equals(new User(1)) // false by default
```

because they are different objects.

---

## 3. equals Contract

The `equals()` method must follow five rules.

### 1. Reflexive

An object must equal itself.

```java
x.equals(x) == true
```

### 2. Symmetric

If x equals y, y must equal x.

```java
x.equals(y) == true
y.equals(x) == true
```

### 3. Transitive

If x equals y and y equals z, x must equal z.

```java
x.equals(y) == true
y.equals(z) == true
x.equals(z) == true
```

### 4. Consistent

Repeated calls should return same result if object state does not change.

```java
x.equals(y) // true
x.equals(y) // true again
```

### 5. Null comparison

Any non-null object must not equal null.

```java
x.equals(null) == false
```

ASCII:

```
equals must behave like stable logical equality:

same object        -> true
same logical value -> true
different value    -> false
null               -> false
```

---

## 4. hashCode Contract

`hashCode()` returns an integer used by hash-based collections.

Contract:

1. If two objects are equal by `equals()`, they must have same `hashCode()`.
2. If two objects have same `hashCode()`, they do not necessarily have to be equal.
3. `hashCode()` should remain consistent as long as fields used in equality do not change.

Critical rule:

```
equals true  -> hashCode must be same
hashCode same -> equals may be true or false
```

Example collision:

```java
User a = new User(1, "Amit");
User b = new User(2, "Neha");
```

They can have same hash:

```
a.hashCode() == b.hashCode()
```

This is allowed. HashMap handles collisions using `equals()`.

Broken rule:

```java
a.equals(b) == true
a.hashCode() != b.hashCode() // illegal contract violation
```

---

## 5. equals and hashCode in HashMap

HashMap uses both.

```java
map.put(key, value);
```

Flow:

```
key.hashCode()
  |
  v
bucket index
  |
  v
compare keys inside bucket using equals()
```

ASCII:

```
HashMap table

bucket 0: null
bucket 1: [hash=101, key=User(1), value=A]
bucket 2: null
bucket 3: [hash=203, key=User(2), value=B] -> [hash=203, key=User(9), value=C]
```

Lookup:

```java
map.get(new User(1));
```

Steps:

```
1. hashCode finds bucket
2. equals finds exact matching key
3. return value
```

If you override `equals()` but not `hashCode()`, HashMap can fail:

```java
class User {
    private final int id;

    User(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User && this.id == ((User) o).id;
    }
}

Map<User, String> map = new HashMap<>();
map.put(new User(1), "Amit");

System.out.println(map.get(new User(1))); // often null
```

Why:

```
put key User(1) -> identity hash maybe bucket 4
get key User(1) -> different identity hash maybe bucket 9
equals never even gets a chance
```

---

## 6. How to Implement equals and hashCode

Good implementation:

```java
import java.util.Objects;

public final class User {
    private final long id;
    private final String email;

    public User(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
```

Step-by-step:

```
1. Same reference? return true
2. Wrong type or null? return false
3. Cast
4. Compare significant fields
5. hashCode uses the same significant fields
```

### Using getClass vs instanceof

`instanceof` allows subclass equality:

```java
if (!(o instanceof User)) return false;
```

`getClass()` requires exact same runtime class:

```java
if (o == null || getClass() != o.getClass()) return false;
```

For value objects, `getClass()` is often safer because inheritance can break symmetry.

### Records handle this automatically

```java
public record User(long id, String email) { }
```

Records automatically generate:

- Constructor
- Accessors
- `equals()`
- `hashCode()`
- `toString()`

Records are good for immutable data carriers.

---

## 7. Common Equality Traps

### Trap 1: Using `==` for String content

```java
String a = new String("java");
String b = new String("java");

System.out.println(a == b);      // false
System.out.println(a.equals(b)); // true
```

Use `equals()` for content.

### Trap 2: Mutable HashMap key

```java
class Account {
    String number;

    Account(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Account && Objects.equals(number, ((Account) o).number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}

Map<Account, String> map = new HashMap<>();
Account account = new Account("A1");
map.put(account, "active");

account.number = "A2";

System.out.println(map.get(account)); // usually null
```

Rule:

> Fields used in `equals()` and `hashCode()` should be immutable while the object is used as a key.

### Trap 3: BigDecimal equals vs compareTo

```java
BigDecimal a = new BigDecimal("1.0");
BigDecimal b = new BigDecimal("1.00");

System.out.println(a.equals(b));     // false
System.out.println(a.compareTo(b));  // 0
```

`equals()` considers scale. `compareTo()` compares numeric value.

This matters in `HashSet` vs `TreeSet`:

```java
Set<BigDecimal> hashSet = new HashSet<>();
hashSet.add(new BigDecimal("1.0"));
hashSet.add(new BigDecimal("1.00"));
System.out.println(hashSet.size()); // 2

Set<BigDecimal> treeSet = new TreeSet<>();
treeSet.add(new BigDecimal("1.0"));
treeSet.add(new BigDecimal("1.00"));
System.out.println(treeSet.size()); // 1
```

### Trap 4: Arrays do not override equals by content

```java
int[] a = {1, 2};
int[] b = {1, 2};

System.out.println(a.equals(b));        // false
System.out.println(Arrays.equals(a, b)); // true
```

For nested arrays:

```java
Arrays.deepEquals(arr1, arr2);
```

---

## 8. String Immutability

`String` is immutable. Once created, its content cannot change.

```java
String s = "Java";
s.concat(" 21");
System.out.println(s); // Java

s = s.concat(" 21");
System.out.println(s); // Java 21
```

ASCII:

```
Before:
s -> "Java"

s.concat(" 21") creates:
     "Java 21"

But s still points to:
s -> "Java"
```

After assignment:

```
s -> "Java 21"
```

### Why String is immutable

1. String pool safety
2. Security
3. Thread-safety
4. Hash code caching
5. Class loading reliability

### String pool safety

```java
String a = "admin";
String b = "admin";
```

Both may point to same pooled object:

```
a ----+
      v
   "admin"
      ^
b ----+
```

If String were mutable:

```java
a.changeTo("guest");
```

Then `b` would also appear changed. That would break everything.

### Security

Strings are used for:

- File paths
- URLs
- Class names
- Database usernames
- Tokens
- Network host names

If strings could mutate after validation, security checks could be bypassed.

### Thread-safety

Immutable objects are naturally thread-safe for reading.

```java
String shared = "config";
```

Multiple threads can read `shared` safely without synchronization.

### Hash code caching

String hash code can be cached because content never changes.

This makes String efficient as a HashMap key.

---

## 9. String Pool

String literals go into the String pool.

```java
String a = "java";
String b = "java";

System.out.println(a == b); // true
```

ASCII:

```
String Pool:
  "java" <---- a
     ^
     |
     b
```

Using `new` creates a separate object:

```java
String a = "java";
String b = new String("java");

System.out.println(a == b);      // false
System.out.println(a.equals(b)); // true
```

ASCII:

```
String Pool:
  "java" <---- a

Heap:
  new String("java") <---- b
```

### intern()

`intern()` returns the canonical pooled string.

```java
String a = "java";
String b = new String("java").intern();

System.out.println(a == b); // true
```

### Compile-time constants

```java
String a = "ja" + "va";
String b = "java";

System.out.println(a == b); // true
```

Compiler folds constants:

```
"ja" + "va" -> "java"
```

Runtime concatenation is different:

```java
String part = "ja";
String a = part + "va";
String b = "java";

System.out.println(a == b); // false
System.out.println(a.equals(b)); // true
```

---

## 10. String Concatenation and StringBuilder

Because String is immutable, repeated concatenation can create many objects.

Bad in loops:

```java
String result = "";
for (int i = 0; i < 10_000; i++) {
    result += i;
}
```

Conceptual:

```
iteration 1: "" + "1"       -> new String
iteration 2: "1" + "2"      -> new String
iteration 3: "12" + "3"     -> new String
...
```

Use `StringBuilder`:

```java
StringBuilder builder = new StringBuilder();
for (int i = 0; i < 10_000; i++) {
    builder.append(i);
}
String result = builder.toString();
```

### StringBuilder vs StringBuffer

| Type | Thread-safe | Performance | Use |
|---|---|---|---|
| `StringBuilder` | No | Faster | Single-threaded local building |
| `StringBuffer` | Yes, synchronized | Slower | Rare legacy synchronized need |

Interview answer:

> Use `StringBuilder` for repeated concatenation in a single thread. Use normal `+` for simple expressions because the compiler/JVM can optimize it. Avoid `+=` inside large loops.

---

## 11. Wrapper Caching and Autoboxing

Wrapper classes convert primitives into objects:

```java
int primitive = 10;
Integer wrapper = Integer.valueOf(10);
```

Autoboxing:

```java
Integer x = 10; // compiler uses Integer.valueOf(10)
```

Unboxing:

```java
int y = x; // compiler uses x.intValue()
```

### Integer cache

Java caches small wrapper values.

```java
Integer a = 127;
Integer b = 127;
System.out.println(a == b); // true

Integer x = 128;
Integer y = 128;
System.out.println(x == y); // false usually
```

Default Integer cache range:

```
-128 to 127
```

ASCII:

```
Integer cache:
-128 ... 0 ... 127

Integer.valueOf(127) -> cached object
Integer.valueOf(128) -> new object usually
```

Use `equals()` for wrapper value comparison:

```java
System.out.println(x.equals(y)); // true
```

### Wrapper caches

Common caches:

| Wrapper | Cache behavior |
|---|---|
| `Byte` | all values |
| `Short` | usually -128 to 127 |
| `Integer` | usually -128 to 127 |
| `Long` | usually -128 to 127 |
| `Character` | usually 0 to 127 |
| `Boolean` | `TRUE` and `FALSE` |
| `Float` | no cache |
| `Double` | no cache |

### Autoboxing trap in loops

```java
Long sum = 0L;
for (long i = 0; i < 1_000_000; i++) {
    sum += i;
}
```

Conceptually:

```
unbox Long -> long
add i
box long -> Long
repeat many times
```

This creates unnecessary objects.

Use primitive:

```java
long sum = 0L;
for (long i = 0; i < 1_000_000; i++) {
    sum += i;
}
```

### Null unboxing trap

```java
Integer value = null;
int x = value; // NullPointerException
```

Because compiler inserts:

```java
int x = value.intValue();
```

---

## 12. Interview Questions and Answers

### Q1. Difference between `==` and `equals()`?

For primitives, `==` compares values. For objects, `==` compares references, meaning whether both variables point to the same object. `equals()` compares logical equality if the class overrides it.

### Q2. What is the equals contract?

It must be reflexive, symmetric, transitive, consistent, and return false for null.

### Q3. What is the hashCode contract?

If two objects are equal by `equals()`, they must have the same hash code. Unequal objects may still have the same hash code because collisions are allowed.

### Q4. Why override hashCode when overriding equals?

Hash-based collections use `hashCode()` to find the bucket and `equals()` to find the exact key. If equal objects have different hash codes, HashMap/HashSet may fail to find logically equal keys.

### Q5. Can two unequal objects have same hashCode?

Yes. That is called a hash collision. HashMap handles it by checking `equals()` inside the bucket.

### Q6. Why should HashMap keys be immutable?

If fields used in `hashCode()` change after insertion, the key may be stored in one bucket but searched in another. This makes lookup fail.

### Q7. Why is String immutable?

String is immutable for pool safety, security, thread-safety, reliable class loading, and hash code caching.

### Q8. Difference between String literal and new String?

String literal uses the String pool:

```java
String a = "java";
```

`new String("java")` creates a separate heap object:

```java
String b = new String("java");
```

So `a == b` is false but `a.equals(b)` is true.

### Q9. What does intern() do?

`intern()` returns the canonical pooled representation of a String. If an equal string exists in the pool, it returns that reference; otherwise it adds/uses a pooled version.

### Q10. Why is `Integer a = 127; Integer b = 127; a == b` true?

Because `Integer.valueOf()` caches values from -128 to 127 by default. Both references point to the same cached object.

### Q11. Why is `Integer a = 128; Integer b = 128; a == b` usually false?

128 is outside the default Integer cache range, so separate objects may be created. Use `equals()` to compare wrapper values.

### Q12. What is autoboxing?

Autoboxing is automatic conversion from primitive to wrapper, such as `int` to `Integer`. Unboxing is automatic conversion from wrapper to primitive.

### Q13. How can unboxing cause NullPointerException?

If a wrapper reference is null and Java tries to unbox it, the compiler-generated method call fails.

```java
Integer x = null;
int y = x; // x.intValue() -> NullPointerException
```

### Q14. StringBuilder vs StringBuffer?

`StringBuilder` is mutable and not synchronized, so it is faster for local single-threaded string building. `StringBuffer` is synchronized and usually only relevant for legacy thread-safe APIs.

### Q15. Are records good for equals/hashCode?

Yes. Records automatically generate value-based `equals()`, `hashCode()`, and `toString()` using record components. They are excellent for immutable data carriers.

---

## 13. Quick Reference Cheat Sheet

### Comparison rules

| Use case | Use |
|---|---|
| Primitive comparison | `==` |
| Object identity | `==` |
| Object logical equality | `equals()` |
| String content | `equals()` |
| Wrapper value | `equals()` |
| Null-safe equality | `Objects.equals(a, b)` |

### HashMap key rules

```
1. Override equals and hashCode together
2. Use the same fields in both
3. Prefer immutable key fields
4. Avoid mutable objects as keys
5. Equal objects must have same hash code
```

### String rules

```
String is immutable.
String literals are pooled.
new String creates a separate object.
Use equals for content.
Use StringBuilder for repeated loop concatenation.
```

### Wrapper rules

```
Integer cache default: -128 to 127
Use equals, not ==, for wrapper values
Prefer primitives in hot loops
Watch for NullPointerException during unboxing
```

### Strong interview summary

> `equals()` defines logical equality, while `hashCode()` supports bucket-based lookup in hash collections. Equal objects must have equal hash codes. String is immutable to make pooling, security, thread-safety, and hash caching safe. Wrapper objects introduce caching and autoboxing behavior, so `==` can produce surprising results. In interviews, always connect equality rules back to HashMap and HashSet behavior.

