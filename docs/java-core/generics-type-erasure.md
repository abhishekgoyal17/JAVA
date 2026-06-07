# Java Generics and Type Erasure - Complete Interview Deep Dive

> A detailed guide to Java generics for interviews: generic classes, methods, bounded types, wildcards, PECS, invariance, type erasure, bridge methods, heap pollution, and common traps.

---

## Table of Contents

1. [Why Generics Exist](#1-why-generics-exist)
2. [Generic Classes](#2-generic-classes)
3. [Generic Methods](#3-generic-methods)
4. [Bounded Type Parameters](#4-bounded-type-parameters)
5. [Multiple Bounds](#5-multiple-bounds)
6. [Invariance](#6-invariance)
7. [Wildcards](#7-wildcards)
8. [PECS Rule](#8-pecs-rule)
9. [Type Erasure](#9-type-erasure)
10. [Bridge Methods](#10-bridge-methods)
11. [Heap Pollution and Raw Types](#11-heap-pollution-and-raw-types)
12. [Generic Restrictions](#12-generic-restrictions)
13. [Common Interview Examples](#13-common-interview-examples)
14. [Interview Questions and Answers](#14-interview-questions-and-answers)
15. [Quick Reference Cheat Sheet](#15-quick-reference-cheat-sheet)

---

## 1. Why Generics Exist

Before generics, Java collections stored `Object`.

```java
List list = new ArrayList();
list.add("Java");
list.add(100);

String value = (String) list.get(0); // manual cast
String bad = (String) list.get(1);   // ClassCastException at runtime
```

Problems:

- No compile-time type safety
- Manual casts everywhere
- Runtime failures that compiler could have prevented
- APIs were less expressive

Generics move type checking to compile time:

```java
List<String> list = new ArrayList<>();
list.add("Java");
// list.add(100); // compile error

String value = list.get(0); // no cast needed
```

Mental model:

```
Without generics:
  Compiler: "Everything is Object. Good luck."
  Runtime:  ClassCastException possible

With generics:
  Compiler: "This list is only String."
  Runtime:  Safer code, fewer casts
```

---

## 2. Generic Classes

A generic class defines type parameters at class level.

```java
public class Box<T> {
    private T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
```

Usage:

```java
Box<String> stringBox = new Box<>();
stringBox.set("Java");
String value = stringBox.get();

Box<Integer> intBox = new Box<>();
intBox.set(10);
Integer number = intBox.get();
```

ASCII:

```
Source code:
Box<T>
  T value
  set(T)
  get(): T

At compile time:
Box<String> -> only String allowed
Box<Integer> -> only Integer allowed
```

Common type parameter names:

| Name | Meaning |
|---|---|
| `T` | Type |
| `E` | Element |
| `K` | Key |
| `V` | Value |
| `N` | Number |
| `R` | Result |

These are conventions, not rules.

---

## 3. Generic Methods

A generic method declares its own type parameter before return type.

```java
public static <T> T first(List<T> list) {
    if (list.isEmpty()) {
        throw new IllegalArgumentException("List cannot be empty");
    }
    return list.get(0);
}
```

Usage:

```java
String s = first(List.of("A", "B"));
Integer i = first(List.of(1, 2, 3));
```

Syntax:

```
public static <T> T methodName(...)
              |
              +-- declares T for this method
```

Generic method with two types:

```java
public static <K, V> Map<K, V> singletonMap(K key, V value) {
    Map<K, V> map = new HashMap<>();
    map.put(key, value);
    return map;
}
```

Interview trap:

```java
public <T> void print(T value) { }
```

Here `T` belongs to method.

```java
class Printer<T> {
    public void print(T value) { }
}
```

Here `T` belongs to class.

---

## 4. Bounded Type Parameters

Bounds restrict what type can be used.

```java
public static <T extends Number> double sum(List<T> numbers) {
    double total = 0;
    for (T number : numbers) {
        total += number.doubleValue();
    }
    return total;
}
```

Allowed:

```java
sum(List.of(1, 2, 3));       // Integer
sum(List.of(1.5, 2.5));      // Double
```

Not allowed:

```java
// sum(List.of("A", "B"));   // compile error
```

Why bound helps:

Without bound:

```java
public static <T> double sum(List<T> values) {
    // values.get(0).doubleValue(); // compile error
}
```

Compiler only knows `T` is Object.

With bound:

```
T extends Number
Compiler knows T has Number methods:
  intValue()
  longValue()
  doubleValue()
```

### `extends` means upper bound

In generics, `extends` is used for classes and interfaces.

```java
<T extends Number>
<T extends Runnable>
```

It means:

```
T must be Number or a subtype of Number
T must implement Runnable or be a subtype of Runnable
```

---

## 5. Multiple Bounds

You can require a type to satisfy multiple constraints.

```java
public static <T extends Number & Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

Rules:

- At most one class bound
- Class bound must come first
- Any number of interface bounds after that

Valid:

```java
<T extends Number & Comparable<T> & Serializable>
```

Invalid:

```java
// <T extends Comparable<T> & Number> // class must come first
```

ASCII:

```
T must be:
  Number
  +
  Comparable<T>
  +
  Serializable
```

---

## 6. Invariance

This is one of the most important interview topics.

Even though `Integer` extends `Number`:

```java
Integer is-a Number
```

This does not mean:

```java
List<Integer> is-a List<Number>
```

This code does not compile:

```java
List<Integer> ints = new ArrayList<>();
// List<Number> nums = ints; // compile error
```

Why?

If it were allowed:

```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = ints;   // imagine this was allowed
nums.add(3.14);             // Double is a Number

Integer value = ints.get(0); // would break type safety
```

ASCII:

```
Integer  ----is-a----> Number

List<Integer> --NOT is-a--> List<Number>
```

This is called invariance.

Arrays are different and covariant:

```java
Integer[] intArray = new Integer[10];
Number[] numArray = intArray; // allowed
numArray[0] = 3.14;           // ArrayStoreException at runtime
```

Generics avoid this runtime problem by rejecting it at compile time.

---

## 7. Wildcards

Wildcards represent unknown types.

### Unbounded wildcard

```java
public static void printAll(List<?> list) {
    for (Object value : list) {
        System.out.println(value);
    }
}
```

This accepts:

```java
printAll(List.of("A", "B"));
printAll(List.of(1, 2, 3));
printAll(List.of(1.5, 2.5));
```

But you cannot add non-null values:

```java
public static void broken(List<?> list) {
    // list.add("A"); // compile error
    list.add(null);   // only null is allowed
}
```

Why?

The compiler does not know actual element type.

```
List<?> might be List<String>
List<?> might be List<Integer>
List<?> might be List<User>

Adding "A" is not safe for all possible lists.
```

### Upper-bounded wildcard

```java
public static double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number number : numbers) {
        total += number.doubleValue();
    }
    return total;
}
```

Accepts:

```java
List<Integer>
List<Double>
List<Long>
List<Number>
```

Cannot safely add:

```java
public static void addBad(List<? extends Number> numbers) {
    // numbers.add(10);   // compile error
    // numbers.add(3.14); // compile error
}
```

Why?

If actual list is `List<Integer>`, adding `Double` is unsafe. If actual list is `List<Double>`, adding `Integer` is unsafe.

You can read as `Number`:

```java
Number n = numbers.get(0);
```

### Lower-bounded wildcard

```java
public static void addIntegers(List<? super Integer> values) {
    values.add(1);
    values.add(2);
}
```

Accepts:

```java
List<Integer>
List<Number>
List<Object>
```

Can add `Integer` safely because every accepted list can hold an Integer.

But reading is only safe as Object:

```java
Object value = values.get(0);
// Integer i = values.get(0); // compile error
```

ASCII:

```
? extends Number:
  read Number safely
  cannot add specific Number subtype safely

? super Integer:
  add Integer safely
  read only Object safely
```

---

## 8. PECS Rule

PECS:

```
Producer Extends, Consumer Super
```

If a structure produces values for you to read, use `extends`.

If a structure consumes values you write into it, use `super`.

### Producer example

```java
public static double total(List<? extends Number> source) {
    double sum = 0;
    for (Number n : source) {
        sum += n.doubleValue();
    }
    return sum;
}
```

The list produces numbers. Use `extends`.

```
source ----produces----> Number values
```

### Consumer example

```java
public static void copyIntegers(List<Integer> source, List<? super Integer> destination) {
    for (Integer value : source) {
        destination.add(value);
    }
}
```

Destination consumes integers. Use `super`.

```
Integer values ----consumed by----> destination
```

### Full copy example

```java
public static <T> void copy(List<? extends T> source, List<? super T> destination) {
    for (T item : source) {
        destination.add(item);
    }
}
```

Usage:

```java
List<Integer> ints = List.of(1, 2, 3);
List<Number> numbers = new ArrayList<>();

copy(ints, numbers);
```

ASCII:

```
List<Integer> source
      |
      | produces T
      v
T = Number or Integer
      |
      | consumed by
      v
List<Number> destination
```

---

## 9. Type Erasure

Generics mostly exist at compile time. At runtime, generic type information is erased.

Source:

```java
List<String> names = new ArrayList<>();
names.add("A");
String name = names.get(0);
```

After compilation, conceptually:

```java
List names = new ArrayList();
names.add("A");
String name = (String) names.get(0);
```

This is type erasure.

### Why Java uses erasure

Main reason: backward compatibility.

Generics were added in Java 5, but old non-generic libraries and bytecode had to keep working.

```
Java 1.4 code:
List list = new ArrayList();

Java 5+ code:
List<String> list = new ArrayList<>();

Both must run on the JVM.
```

### Erasure of unbounded type

```java
class Box<T> {
    T value;
    T get() { return value; }
}
```

Erases to:

```java
class Box {
    Object value;
    Object get() { return value; }
}
```

### Erasure of bounded type

```java
class NumberBox<T extends Number> {
    T value;
    T get() { return value; }
}
```

Erases to:

```java
class NumberBox {
    Number value;
    Number get() { return value; }
}
```

Bound becomes the erased type.

### Runtime consequence

This is illegal:

```java
// if (list instanceof List<String>) { } // compile error
```

At runtime, JVM sees only:

```java
List
```

Not:

```java
List<String>
List<Integer>
```

Both have same runtime class:

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();

System.out.println(strings.getClass() == integers.getClass()); // true
```

ASCII:

```
Compile time:
  ArrayList<String>
  ArrayList<Integer>

Runtime:
  ArrayList
  ArrayList
```

---

## 10. Bridge Methods

Bridge methods are compiler-generated methods that preserve polymorphism after type erasure.

Example:

```java
class Parent<T> {
    T get() {
        return null;
    }
}

class Child extends Parent<String> {
    @Override
    String get() {
        return "Java";
    }
}
```

After erasure:

```java
class Parent {
    Object get() {
        return null;
    }
}

class Child extends Parent {
    String get() {
        return "Java";
    }
}
```

Problem:

`Object get()` and `String get()` need to behave polymorphically.

Compiler adds a bridge:

```java
class Child extends Parent {
    String get() {
        return "Java";
    }

    // generated bridge method
    Object get() {
        return get(); // calls String get()
    }
}
```

Conceptual only: exact bytecode details differ, but this explains why bridge methods exist.

Interview answer:

> Bridge methods are synthetic methods generated by the compiler to maintain method overriding and polymorphism after generic type erasure.

---

## 11. Heap Pollution and Raw Types

Heap pollution means a variable of parameterized type refers to an object that is not actually of that parameterized type.

```java
List<String> strings = new ArrayList<>();
List raw = strings;
raw.add(100); // compiler warning, but allowed

String value = strings.get(0); // ClassCastException
```

ASCII:

```
List<String> reference
      |
      v
ArrayList containing:
  [Integer 100]

Compiler expected String.
Runtime contains Integer.
```

### Raw types

Raw type:

```java
List list = new ArrayList();
```

Parameterized type:

```java
List<String> list = new ArrayList<>();
```

Avoid raw types unless interacting with legacy APIs.

### @SuppressWarnings

If unavoidable:

```java
@SuppressWarnings("unchecked")
public List<String> legacyCall() {
    return (List<String>) oldLibrary.getValues();
}
```

Use it narrowly. Do not place it on the entire class unless truly necessary.

---

## 12. Generic Restrictions

### Cannot create generic array

```java
// List<String>[] arr = new List<String>[10]; // compile error
```

Reason:

Arrays know their runtime component type. Generics are erased. The combination would break type safety.

### Cannot instantiate T directly

```java
class Box<T> {
    // T value = new T(); // compile error
}
```

At runtime, `T` is erased, so JVM does not know which class to instantiate.

Use supplier:

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

### Cannot use primitive type directly

```java
// List<int> numbers = new ArrayList<>(); // compile error
List<Integer> numbers = new ArrayList<>();
```

Generics work with reference types only. Use wrapper classes.

### Cannot catch generic exception type

```java
// class MyException<T> extends Exception { } // illegal
```

Generic exception classes are not allowed because exception handling requires runtime type checks, and generic type is erased.

### Static fields cannot use class type parameter

```java
class Box<T> {
    // static T value; // compile error
}
```

Reason:

There is one static field per class, not one per generic instantiation.

```
Box<String>
Box<Integer>

Runtime class for both: Box
One static field cannot be both String and Integer.
```

---

## 13. Common Interview Examples

### Example 1: Read-only number list

```java
public static void printNumbers(List<? extends Number> numbers) {
    for (Number number : numbers) {
        System.out.println(number);
    }
}
```

Accepts:

```java
printNumbers(List.of(1, 2, 3));
printNumbers(List.of(1.5, 2.5));
```

### Example 2: Add integers to flexible destination

```java
public static void addDefaults(List<? super Integer> output) {
    output.add(10);
    output.add(20);
}
```

Accepts:

```java
List<Integer> ints = new ArrayList<>();
List<Number> nums = new ArrayList<>();
List<Object> objs = new ArrayList<>();
```

### Example 3: Generic repository

```java
interface Repository<ID, T> {
    T findById(ID id);
    void save(T entity);
}

class UserRepository implements Repository<Long, User> {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User findById(Long id) {
        return users.get(id);
    }

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }
}
```

Why this is useful:

```
Repository<Long, User>
  ID type is fixed to Long
  Entity type is fixed to User
  Compiler prevents accidental misuse
```

### Example 4: Comparator with super

```java
public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
    list.sort(comparator);
}
```

Why `? super T`?

A comparator of a parent type can compare child objects.

```java
Comparator<Object> byString = (a, b) -> a.toString().compareTo(b.toString());
List<String> names = new ArrayList<>(List.of("B", "A"));

sort(names, byString); // allowed because Comparator<Object> can compare Strings
```

---

## 14. Interview Questions and Answers

### Q1. What are generics in Java?

Generics allow classes, interfaces, and methods to operate on typed parameters while preserving compile-time type safety. They remove the need for manual casts and prevent many `ClassCastException` scenarios.

### Q2. What is type erasure?

Type erasure is the process by which the Java compiler removes generic type information after compile-time checks. `List<String>` and `List<Integer>` both become raw `List` at runtime. The compiler inserts casts where needed.

### Q3. Why did Java choose type erasure?

Backward compatibility. Java generics were added in Java 5, and existing non-generic bytecode and libraries had to continue working on the same JVM.

### Q4. Difference between `List<Object>` and `List<?>`?

`List<Object>` means a list that can hold any Object, and you can add any Object to it.

`List<?>` means a list of some unknown type. You can read values as Object, but cannot add non-null values because the compiler does not know the actual element type.

### Q5. Why is `List<Integer>` not assignable to `List<Number>`?

Generics are invariant. If `List<Integer>` were assignable to `List<Number>`, someone could add a `Double` into a list that is actually meant to contain only `Integer`, breaking type safety.

### Q6. What is PECS?

PECS means Producer Extends, Consumer Super.

Use `? extends T` when you only read values from a source. Use `? super T` when you write values into a destination.

### Q7. Difference between `<T extends Number>` and `<? extends Number>`?

`<T extends Number>` declares a named type parameter that can be reused across parameters and return type.

`<? extends Number>` declares an unknown subtype of Number, usually for flexible input.

Example:

```java
public static <T extends Number> T same(T value) { return value; }
public static void print(List<? extends Number> values) { }
```

### Q8. Why can we not create `new T()`?

Because `T` is erased at runtime. The JVM does not know which concrete class to instantiate.

### Q9. What is heap pollution?

Heap pollution occurs when a parameterized variable refers to an object that contains values not matching its declared generic type, often caused by raw types or unchecked casts.

### Q10. What are bridge methods?

Bridge methods are synthetic methods generated by the compiler to preserve polymorphism after type erasure.

### Q11. Can generic types be used with primitives?

No. Generics require reference types. Use wrapper classes such as `Integer`, `Long`, and `Double`.

### Q12. What is a bounded wildcard?

A bounded wildcard limits an unknown type.

```java
List<? extends Number> // Number or subtype
List<? super Integer>  // Integer or supertype
```

### Q13. Why can we add to `List<? super Integer>` but not to `List<? extends Number>`?

For `? super Integer`, the actual list can be `List<Integer>`, `List<Number>`, or `List<Object>`. All can safely accept Integer.

For `? extends Number`, the actual list may be `List<Integer>` or `List<Double>`. Adding a specific subtype could be unsafe.

### Q14. What is the difference between raw type and wildcard?

Raw type disables generic type checking:

```java
List raw;
```

Wildcard keeps type safety while representing unknown type:

```java
List<?> safeUnknown;
```

Prefer wildcard over raw type.

### Q15. What does this mean: `Class<T>`?

`Class<T>` carries runtime class metadata for type `T`. It is commonly used when type erasure would otherwise remove type information.

```java
public static <T> T create(Class<T> type) throws Exception {
    return type.getDeclaredConstructor().newInstance();
}
```

---

## 15. Quick Reference Cheat Sheet

### Wildcard rules

| Syntax | Meaning | Can read as | Can add |
|---|---|---|---|
| `List<T>` | Exact T | T | T |
| `List<?>` | Unknown | Object | null only |
| `List<? extends Number>` | Unknown subtype of Number | Number | null only |
| `List<? super Integer>` | Unknown supertype of Integer | Object | Integer and subtype |

### PECS

```
Producer Extends:
  read from List<? extends T>

Consumer Super:
  write to List<? super T>
```

### Erasure summary

```
T                    -> Object
T extends Number     -> Number
List<String>         -> List
Generic casts        -> inserted by compiler
Bridge methods       -> generated when needed for polymorphism
```

### Strong interview summary

> Generics give compile-time type safety, remove manual casts, and make APIs expressive. Java implements generics using type erasure for backward compatibility, which means generic type information mostly disappears at runtime. The hardest interview areas are invariance, wildcards, PECS, heap pollution, and explaining why some generic operations like `new T()` or `instanceof List<String>` are not allowed.

