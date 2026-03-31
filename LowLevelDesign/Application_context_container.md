# Application Context Container — Deep Dive

> A complete learning reference for understanding the Context Container pattern in Java.
> Covers the problem it solves, every Java concept involved, design patterns, architecture diagrams,
> and production-grade code examples throughout.

---

## Table of Contents

1. [The Core Problem — Why Does This Pattern Exist?](#1-the-core-problem--why-does-this-pattern-exist)
2. [Mental Model — The Application Backpack](#2-mental-model--the-application-backpack)
3. [Java Concepts Involved](#3-java-concepts-involved)
4. [Design Patterns Involved](#4-design-patterns-involved)
5. [Building a Context Container From Scratch](#5-building-a-context-container-from-scratch)
6. [Typed Context Lookup — How It Works Internally](#6-typed-context-lookup--how-it-works-internally)
7. [Lifecycle Management — AutoCloseable](#7-lifecycle-management--autocloseable)
8. [Startup Sequencing and Events](#8-startup-sequencing-and-events)
9. [Backward Compatibility — The Static Bridge](#9-backward-compatibility--the-static-bridge)
10. [Context Container vs Other Patterns — Full Comparison](#10-context-container-vs-other-patterns--full-comparison)
11. [Dependency Injection — Deep Explanation](#11-dependency-injection--deep-explanation)
12. [Service Locator Pattern — What It Is and the Danger](#12-service-locator-pattern--what-it-is-and-the-danger)
13. [Before and After — Code Transformation Examples](#13-before-and-after--code-transformation-examples)
14. [SOLID Principles in This Design](#14-solid-principles-in-this-design)
15. [When Should You Use a Context Container?](#15-when-should-you-use-a-context-container)
16. [Common Mistakes and Anti-Patterns](#16-common-mistakes-and-anti-patterns)
17. [Full Architecture Diagram](#17-full-architecture-diagram)
18. [One-Line Summary of Every Concept](#18-one-line-summary-of-every-concept)
19. [Quick Reference Card](#19-quick-reference-card)

---

## 1. The Core Problem — Why Does This Pattern Exist?

### The situation every large Java application faces

As a system grows beyond a handful of classes, many parts of the application need access to the
same shared runtime objects:

- the current environment (OS info, deployment mode, config)
- the thread pool / scheduler
- a JSON serializer (`ObjectMapper`, `Jsonb`)
- database connection pools
- HTTP client instances
- metrics / monitoring collectors
- feature flag evaluators

Every class that does real work needs one or more of these. The question is:
**how do those classes get access to them?**

There are three naive answers. All three fail at scale.

---

### Naive approach A — Pass everything through constructors

```java
new OrderService(
    environment,
    threadPool,
    objectMapper,
    dataSource,
    httpClient,
    metricsCollector,
    featureFlagClient
);
```

This is called **constructor over-injection**. It becomes unmanageable fast.

Problems:
- Constructor signatures become enormous
- Adding one new shared dependency means updating every call site in the codebase
- The constructor tells you nothing about what the class actually does
- Violates Single Responsibility — the class has to know about too many things

---

### Naive approach B — Static globals

```java
public class AppGlobals {
    public static Environment environment;
    public static ThreadPoolExecutor threadPool;
    public static ObjectMapper objectMapper;
    public static DataSource dataSource;
}
```

Easy to write. Painful to live with.

Problems:

**Hidden dependencies** — a class that calls `AppGlobals.objectMapper` does not declare
that dependency anywhere visible. You cannot tell from the class signature what it needs.

**Hard to test** — you cannot swap `AppGlobals.environment` for a test double without
affecting every other test running in the same JVM. Tests become entangled.

**Initialization ordering** — if class A reads `AppGlobals.environment` before class B
has set it, you get a `NullPointerException` that is very hard to trace in production.

**No lifecycle** — nothing owns the shutdown of these globals. Thread pools do not get
shut down cleanly. Connections leak.

---

### Naive approach C — Recreate shared objects wherever needed

```java
class OrderProcessor {
    public void process(Order order) {
        ObjectMapper mapper = new ObjectMapper();   // new instance every call
        DataSource ds = DataSourceFactory.create(); // new pool every call
        HttpClient http = HttpClient.newHttpClient(); // new client every call
    }
}
```

Problems:
- Wastes memory — `ObjectMapper` alone is heavy; creating one per call is expensive
- Inconsistent state — two different parts of the app may have slightly different
  configuration if they each construct their own instances
- Connection pools cannot be shared — each call gets its own pool which defeats the
  purpose of pooling entirely

---

### The solution — A Context Container

A context container is a **single object that holds application-scoped runtime services**
and lets any component retrieve what it needs without constructing it again.

```java
// At startup — build the container once
AppContext context = new AppContext(services, objectMapper);

// Anywhere in the app — retrieve what you need
Environment env         = context.get(Environment.class);
ThreadPoolExecutor pool = context.get(ThreadPoolExecutor.class);
ObjectMapper mapper     = context.getObjectMapper();
```

This solves all three problems:
- No enormous constructor signatures
- No static globals with hidden dependencies
- No repeated construction of shared objects

---

## 2. Mental Model — The Application Backpack

The clearest mental model for an Application Context Container is a
**backpack that the running application carries throughout its lifetime**.

```
When the app starts:
┌────────────────────────────────────────────────────┐
│                  AppContext                        │
│  ┌──────────────────────────────────────────────┐ │
│  │  Environment              (OS, config, mode) │ │
│  │  ThreadPoolExecutor       (shared pool)      │ │
│  │  DataSourceContext        (DB connections)   │ │
│  │  HttpClientContext        (outbound HTTP)    │ │
│  │  MetricsContext           (monitoring)       │ │
│  │  FeatureFlagContext       (flag evaluation)  │ │
│  │  ObjectMapper             (shared JSON)      │ │
│  └──────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────┘

Any class in the app:
context.get(Environment.class)         --> shared environment
context.get(ThreadPoolExecutor.class)  --> shared thread pool
context.getObjectMapper()              --> shared JSON serializer
```

The backpack:
- is packed once at startup
- is passed to components that need it
- is unpacked at shutdown (closes everything cleanly)
- does not do business logic — it holds what others need

---

## 2. Java Concepts Involved

This pattern touches many important Java language and platform features.
Understanding each one makes the design much clearer.

---

### 3.1 Generics and Type Erasure

```java
public <C extends AppService> C get(Class<C> type) {
    return type.cast(services.get(type.getName()));
}
```

**What generics do here:**

The method signature `<C extends AppService>` says:
- `C` is a type parameter local to this method
- `C` must be a subtype of `AppService`
- the method returns exactly `C` — not just any `AppService`

This gives callers compile-time type safety:

```java
Environment env = context.get(Environment.class);
// env is already typed as Environment — no cast needed by the caller
```

Without generics, the caller would write:

```java
Environment env = (Environment) context.get("Environment");
// manual cast — can throw ClassCastException at runtime
```

**Type erasure:**

Java generics exist only at compile time. At runtime, all type parameters are erased —
`List<String>` and `List<Integer>` are both just `List` in bytecode. So the method
cannot simply check "what is C at runtime" — the information is gone.

This is why the method requires `Class<C>` to be passed explicitly. The `Class` object
carries the type information into runtime. This pattern is called a **type token**.

```java
Class<Environment> token = Environment.class;
token.getName()   // "com.example.app.Environment" — used as map key
token.cast(obj)   // runtime-checked cast to Environment
```

---

### 3.2 Bounded Wildcards

```java
private final Map<String, ? extends AppService> services;
```

`? extends AppService` means: a map whose values are of some type that is a subtype of
`AppService`, but the compiler does not need to know which specific subtype.

This is the **upper bounded wildcard** in Java.

Why use it instead of `Map<String, AppService>`?

It allows the container to be constructed with a map declared as a more specific type,
for example `Map<String, Environment>`, and still compile correctly. The container only
needs to know it holds some kind of `AppService` — not which specific one.

The trade-off: you cannot add to a `? extends` collection after construction.
This is intentional — the container is populated once and then read-only.

---

### 3.3 AutoCloseable and try-with-resources

```java
public final class AppContext implements AutoCloseable {

    @Override
    public void close() {
        services.forEach((k, v) -> v.close());
    }
}
```

`AutoCloseable` is a Java standard library interface with one method: `close()`.
Implementing it signals that this object holds resources that must be released.

It enables the **try-with-resources** construct:

```java
try (AppContext context = buildContext()) {
    runApplication(context);
}
// context.close() is called automatically here
// even if runApplication() throws an exception
```

This guarantees that every registered service is closed at shutdown — thread pools
are terminated, database connections are returned, file handles are released.

In a long-running server process, forgetting to close these resources causes leaks that
eventually exhaust OS limits and crash the process.

---

### 3.4 final fields and the Java Memory Model

```java
private final ObjectMapper objectMapper;
private final Map<String, ? extends AppService> services;
```

Both fields are `final`.

`final` on an instance field means:
- the field is assigned exactly once, inside the constructor
- it cannot be reassigned after construction
- the Java Memory Model guarantees that any thread which sees the constructed object
  also sees the correctly initialized field values

This last point is critical. The container is a shared object accessed by many threads.
Without `final`, there is a theoretical risk of another thread seeing an incompletely
constructed container. `final` eliminates this risk entirely.

---

### 3.5 volatile and cross-thread visibility

```java
private static volatile AppContext mainInstance;
```

`volatile` on a static field tells the JVM:
- every write to this field must be flushed to main memory immediately
- every read of this field must come from main memory, not a CPU-local cache

Without `volatile`, the following race condition is possible:

```
Thread 1 (startup):   mainInstance = new AppContext(...);
Thread 2 (worker):    if (mainInstance != null) ... // may still see null
                      // because Thread 2 reads from its CPU cache
                      // which has not been invalidated yet
```

`volatile` provides a **happens-before guarantee** — any write to `mainInstance`
by Thread 1 is visible to Thread 2 on the next read.

---

### 3.6 Class.cast() vs the cast operator

```java
return type.cast(services.get(type.getName()));
```

versus

```java
return (C) services.get(type.getName());
```

Both look equivalent but behave differently due to type erasure:

- `type.cast(obj)` performs a real runtime check. If the object is not actually an
  instance of `C`, it throws `ClassCastException` immediately with a useful message
  including the actual and expected types.

- `(C) obj` is erased at runtime to `(Object) obj` — it passes silently even if the
  type is wrong. The `ClassCastException` may appear somewhere else entirely when
  the object is actually used, making the bug very hard to trace.

`Class.cast()` is strictly safer for this kind of type-based registry pattern.

---

### 3.7 Class name as map key

```java
services.get(type.getName())
```

`type.getName()` returns the fully qualified class name —
`"com.example.app.Environment"`.

This is used as the map key. When the container is built, services are stored
with their class name:

```java
map.put(Environment.class.getName(),   new Environment(...));
map.put(DataSourceContext.class.getName(), new DataSourceContext(...));
```

When retrieved:
```java
context.get(Environment.class)
// internally: map.get("com.example.app.Environment")
```

Using fully qualified class names as keys:
- avoids collisions between classes with the same simple name in different packages
- is stable — class names do not change at runtime
- works across serialization boundaries
- avoids identity issues in environments with multiple classloaders (OSGi, app servers)

---

### 3.8 Lambdas and forEach

```java
services.forEach((k, v) -> v.close());
```

This is equivalent to:

```java
for (Map.Entry<String, ? extends AppService> entry : services.entrySet()) {
    entry.getValue().close();
}
```

The lambda `(k, v) -> v.close()` is a `BiConsumer<String, AppService>`.
`k` is the key (unused here), `v` is the service value. This is idiomatic modern Java —
concise and expressive for iteration with a side effect.

---

## 4. Design Patterns Involved

### 4.1 Service Locator

An `AppContext` is a **Service Locator** — a registry of services that returns them
by type on demand.

```java
Environment env = context.get(Environment.class);
```

The container locates and returns the service. The consumer does not know how
`Environment` was built, what its implementation class is, or where it lives.

This is powerful because it decouples consumers from construction. It is dangerous
if overused because dependencies become hidden (see Section 12).

---

### 4.2 Registry Pattern

The internal map is a **typed registry**:

```java
Map<String, ? extends AppService> services
```

A registry associates names with objects, supports lookup by name, and is populated
once at startup. The difference from a plain `Map` is conceptual — a registry implies
controlled registration, stable keys, and read-heavy access after startup.

---

### 4.3 Facade Pattern

`AppContext` provides a unified interface to a set of sub-systems. Instead of knowing
about each service class separately, callers use one entry point:

```java
// Without facade — caller knows about each registry separately
Environment env   = EnvironmentRegistry.current();
ObjectMapper json = JsonPool.sharedInstance();
DataSource ds     = ConnectionPoolHolder.get();

// With facade — one unified entry point
Environment env   = context.get(Environment.class);
ObjectMapper json = context.getObjectMapper();
DataSource ds     = context.get(DataSourceContext.class).getDataSource();
```

---

### 4.4 Composite / Aggregate Pattern

The container is a composite of services. Each service is self-contained.
The container owns their collective lifecycle.

```
AppContext
├── Environment          (AppService)
├── ThreadPoolContext    (AppService)
├── DataSourceContext    (AppService)
├── HttpClientContext    (AppService)
├── MetricsContext       (AppService)
└── FeatureFlagContext   (AppService)
```

---

### 4.5 Observer Pattern (Startup Event)

When the container is fully initialized, it fires a startup event:

```java
fireStartupEvent();
```

Registered listeners react — for example, starting background jobs, warming caches,
or initializing components that themselves need the fully-built container.

This decouples startup sequencing. The container does not need to know about
background jobs or cache warmers. It just says: "I am ready." Whoever cares reacts.

```java
// Observer registered at build time
context.onStartup(ctx -> {
    CacheWarmer warmer = new CacheWarmer(ctx.get(DataSourceContext.class));
    warmer.warmUp();
});
```

---

### 4.6 Template Method (through AppService interface)

All services implement `AppService` which defines a `close()` contract.
The container's shutdown iterates all services and calls `close()` on each.
Each service provides its own concrete close logic. The container invokes them
through the common interface — a classic Template Method combination.

```java
// AppService interface
public interface AppService extends AutoCloseable {
    void close();
}

// Each implementation defines its own cleanup
public class ThreadPoolContext implements AppService {
    @Override
    public void close() {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }
}

public class DataSourceContext implements AppService {
    @Override
    public void close() {
        dataSource.close(); // returns connections to pool, closes pool
    }
}
```

---

## 5. Building a Context Container From Scratch

Here is a complete, production-quality implementation to make every concept concrete.

### 5.1 The AppService interface

```java
/**
 * Marker interface for all application-scoped runtime services.
 * Every service registered in AppContext must implement this.
 */
public interface AppService extends AutoCloseable {
    @Override
    void close();
}
```

---

### 5.2 Example service implementations

```java
public class Environment implements AppService {
    private final String platform;
    private final String deploymentMode; // "production", "staging", "dev"
    private final Map<String, String> config;

    public Environment(String platform, String deploymentMode, Map<String, String> config) {
        this.platform = platform;
        this.deploymentMode = deploymentMode;
        this.config = Collections.unmodifiableMap(config);
    }

    public String getPlatform()        { return platform; }
    public String getDeploymentMode()  { return deploymentMode; }
    public String getConfig(String key){ return config.get(key); }

    @Override
    public void close() {
        // no resources to release for environment
    }
}
```

```java
public class ThreadPoolContext implements AppService {
    private final ThreadPoolExecutor executor;

    public ThreadPoolContext(int corePoolSize, int maxPoolSize) {
        this.executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public ThreadPoolExecutor getExecutor() { return executor; }

    public void submit(Runnable task) { executor.submit(task); }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

```java
public class MetricsContext implements AppService {
    private final MeterRegistry registry;

    public MetricsContext(MeterRegistry registry) {
        this.registry = registry;
    }

    public void increment(String name)              { registry.counter(name).increment(); }
    public void record(String name, long millis)    { registry.timer(name).record(millis, TimeUnit.MILLISECONDS); }
    public MeterRegistry getRegistry()              { return registry; }

    @Override
    public void close() {
        registry.close();
    }
}
```

---

### 5.3 The AppContext container itself

```java
/**
 * Application-scoped context container.
 *
 * Holds all shared runtime services for the lifetime of the application.
 * Built once at startup by the entry point. Passed to components via constructor.
 * Closed at shutdown — which closes all registered services.
 *
 * Usage:
 *   AppContext ctx = AppContext.builder()
 *       .register(new Environment(...))
 *       .register(new ThreadPoolContext(...))
 *       .register(new MetricsContext(...))
 *       .objectMapper(new ObjectMapper())
 *       .build();
 *
 *   Environment env = ctx.get(Environment.class);
 */
public final class AppContext implements AutoCloseable {

    private final Map<String, AppService> services;
    private final ObjectMapper objectMapper;
    private static volatile AppContext mainInstance;

    private AppContext(Map<String, AppService> services, ObjectMapper objectMapper) {
        this.services     = Collections.unmodifiableMap(services);
        this.objectMapper = objectMapper;
        mainInstance      = this;
    }

    /**
     * Retrieves a registered service by its type.
     *
     * @param type  the Class token of the service to retrieve
     * @param <C>   the service type — must extend AppService
     * @return      the registered service instance, or null if not registered
     */
    public <C extends AppService> C get(Class<C> type) {
        AppService service = services.get(type.getName());
        if (service == null) return null;
        return type.cast(service);
    }

    /**
     * Returns the shared ObjectMapper. Separate from service map because
     * ObjectMapper does not implement AppService — it is a third-party class.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Checks whether a service of the given type is registered.
     */
    public boolean has(Class<? extends AppService> type) {
        return services.containsKey(type.getName());
    }

    /**
     * Closes all registered services in registration order.
     * Called automatically when used in try-with-resources.
     */
    @Override
    public void close() {
        services.forEach((name, service) -> {
            try {
                service.close();
            } catch (Exception e) {
                // log and continue — do not let one failed close block others
                System.err.println("Failed to close service " + name + ": " + e.getMessage());
            }
        });
    }

    /**
     * Static accessor for legacy code that cannot receive AppContext via constructor.
     * Use only for backward compatibility during migration.
     * All new code should receive AppContext through constructor injection.
     */
    public static <C extends AppService> C legacyGet(Class<C> type) {
        if (mainInstance != null) {
            return mainInstance.get(type);
        }
        return null;
    }

    public static AppContext getInstance() {
        return mainInstance;
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, AppService> services = new LinkedHashMap<>();
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder register(AppService service) {
            services.put(service.getClass().getName(), service);
            return this;
        }

        public Builder objectMapper(ObjectMapper mapper) {
            this.objectMapper = mapper;
            return this;
        }

        public AppContext build() {
            return new AppContext(new LinkedHashMap<>(services), objectMapper);
        }
    }
}
```

---

### 5.4 Application entry point — how to build and use it

```java
public class Application {

    public static void main(String[] args) {

        // Build the shared context once
        AppContext context = AppContext.builder()
            .register(new Environment(
                System.getProperty("os.name"),
                System.getenv().getOrDefault("DEPLOY_MODE", "production"),
                loadConfig()
            ))
            .register(new ThreadPoolContext(4, 16))
            .register(new DataSourceContext("jdbc:postgresql://localhost/mydb", 10))
            .register(new MetricsContext(new SimpleMeterRegistry()))
            .register(new FeatureFlagContext(System.getenv("FEATURE_FLAG_API_KEY")))
            .objectMapper(new ObjectMapper().findAndRegisterModules())
            .build();

        // Register shutdown hook — runs on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));

        // Pass context to the runtime layer
        RequestRouter router = new RequestRouter(context);
        router.start();
    }

    private static Map<String, String> loadConfig() {
        // load from properties file, env vars, etc.
        return Map.of(
            "server.port", "8080",
            "cache.ttl",   "3600"
        );
    }
}
```

---

## 6. Typed Context Lookup — How It Works Internally

This is the core mechanism. Understanding it requires understanding generics,
type tokens, and runtime reflection.

### The method

```java
public <C extends AppService> C get(Class<C> type) {
    AppService service = services.get(type.getName());
    if (service == null) return null;
    return type.cast(service);
}
```

### Step by step walkthrough

**Step 1: Caller invokes**
```java
Environment env = context.get(Environment.class);
```

**Step 2: Java infers C = Environment**

The compiler sees `Environment.class` which is of type `Class<Environment>`.
So `C` is inferred as `Environment`. The return type is therefore `Environment`.

**Step 3: type.getName() produces the map key**

```java
Environment.class.getName()
// returns: "com.example.app.Environment"
```

**Step 4: services.get("com.example.app.Environment")**

Returns the `AppService` stored under that key.

**Step 5: type.cast(service)**

```java
Environment.class.cast(appServiceObj)
```

At runtime: is `appServiceObj` actually an instance of `Environment`?
If yes — return it typed as `Environment`.
If no — throw `ClassCastException` immediately with a clear message.

**Step 6: Caller receives Environment**

No manual cast in calling code. Full compile-time safety.

---

### How services get registered — the map key contract

```java
// Registration (in builder):
services.put(Environment.class.getName(), new Environment(...));

// Retrieval:
context.get(Environment.class)
// internally: services.get(Environment.class.getName())
```

Registration key = `Environment.class.getName()`
Retrieval key = `Environment.class.getName()`

They are identical. This is the contract. As long as both sides use the same class,
the lookup always works.

---

### Visual walkthrough

```
context.get(Environment.class)
           │
           │  type = Environment.class
           │  type.getName() = "com.example.app.Environment"
           │
           ▼
Map<String, AppService>
┌─────────────────────────────────────┬─────────────────────┐
│ "com.example.app.Environment"       │ Environment obj      │ <-- found
│ "com.example.app.ThreadPoolContext" │ ThreadPoolContext obj │
│ "com.example.app.MetricsContext"    │ MetricsContext obj    │
└─────────────────────────────────────┴─────────────────────┘
           │
           │  type.cast(environmentObj)
           │
           ▼
     Environment  <-- returned, fully typed, no manual cast
```

---

## 7. Lifecycle Management — AutoCloseable

### Why this matters in production

In a long-running server process, resource leaks cause crashes.

Thread pools that are not shut down keep threads alive, consuming memory and OS
thread handles. Database connection pools that are not closed exhaust the maximum
connection limit on the DB server. HTTP clients that are not closed leave sockets open.

Java garbage collection handles memory but does not close OS resources.
`AutoCloseable` is Java's mechanism for **deterministic resource cleanup**.

### How it works in AppContext

```java
@Override
public void close() {
    services.forEach((name, service) -> {
        try {
            service.close();
        } catch (Exception e) {
            System.err.println("Failed to close service " + name + ": " + e.getMessage());
        }
    });
}
```

Every service's `close()` is called. Errors are caught per-service so one failed
close does not block cleanup of the remaining services.

### Correct production usage

```java
// Option 1: try-with-resources (for short-lived applications or tests)
try (AppContext context = buildContext()) {
    runApplication(context);
}
// context.close() guaranteed here — even if exception thrown

// Option 2: shutdown hook (for long-running servers)
AppContext context = buildContext();
Runtime.getRuntime().addShutdownHook(new Thread(context::close));
runApplication(context);
// close() called when JVM receives SIGTERM or SIGINT
```

### Shutdown ordering matters

If `MetricsContext` depends on `ThreadPoolContext` being alive (e.g., it flushes
metrics on a background thread), closing `ThreadPoolContext` first would break
the metrics flush.

`LinkedHashMap` preserves insertion order. Register services in reverse dependency
order so that dependents are closed before their dependencies:

```java
AppContext context = AppContext.builder()
    .register(new MetricsContext(...))      // registered first
    .register(new ThreadPoolContext(...))   // registered second
    // close() iterates in insertion order:
    // MetricsContext.close() runs first  (can still flush via thread pool)
    // ThreadPoolContext.close() runs second (pool shuts down after metrics done)
    .build();
```

---

## 8. Startup Sequencing and Events

### The problem

Some components cannot be initialized until the context is fully ready,
because they themselves call `context.get(...)` during their initialization.

Example: a `CacheWarmer` that pre-populates an in-memory cache needs to query
the database. It needs `DataSourceContext` to be registered first. If `CacheWarmer`
starts before the context is built, its `context.get(DataSourceContext.class)` returns null.

### The solution — fire a startup event after construction

```java
public AppContext build() {
    AppContext ctx = new AppContext(services, objectMapper);
    ctx.fireStartupEvent(); // fires only after container is fully built
    return ctx;
}
```

```java
private void fireStartupEvent() {
    startupListeners.forEach(listener -> listener.onStartup(this));
}
```

```java
// Register listeners during build
AppContext context = AppContext.builder()
    .register(new DataSourceContext(...))
    .register(new CacheContext(...))
    .onStartup(ctx -> {
        CacheWarmer warmer = new CacheWarmer(ctx.get(DataSourceContext.class));
        warmer.warmUp();   // safe — DataSourceContext is registered
    })
    .build();
```

### Sequence

```
AppContext.build() called
    │
    │  All services registered in map
    │  ObjectMapper stored
    │  mainInstance set
    │
    ▼
fireStartupEvent()
    │
    │  Each startup listener called with the fully-built context
    │  ctx.get(DataSourceContext.class) --> returns registered service (safe)
    │  CacheWarmer.warmUp()             --> can now access DB
    │
    ▼
Application.main() continues
    │
    ▼
RequestRouter.start() -- context is fully operational
```

### Why not run startup logic in each service's constructor?

Because at the time a service's constructor runs, the other services it might
depend on may not be registered yet. The startup event fires only after ALL
services are in the map — guaranteeing all dependencies are available.

---

## 9. Backward Compatibility — The Static Bridge

### The problem in real codebases

In large, long-lived codebases, not all code can be refactored at once.
Some legacy classes:

- use static initializer blocks that run before the context is ever passed to them
- are deeply nested utility classes that do not easily accept constructor parameters
- were written years ago when the project used static globals everywhere

Forcing all of these to use constructor injection immediately requires a large,
risky refactor. The static bridge is a **temporary escape hatch** that allows
gradual migration.

### The implementation

```java
private static volatile AppContext mainInstance;

AppContext(Map<String, AppService> services, ObjectMapper objectMapper) {
    this.services     = Collections.unmodifiableMap(services);
    this.objectMapper = objectMapper;
    mainInstance      = this; // set on construction
}

/**
 * @deprecated Use constructor injection instead.
 * This method exists only for backward compatibility with legacy code.
 * All new code must receive AppContext through its constructor.
 */
@Deprecated
public static <C extends AppService> C legacyGet(Class<C> type) {
    if (mainInstance != null) {
        return mainInstance.get(type);
    }
    return null;
}
```

Legacy code that cannot be immediately refactored:

```java
// Old utility class — cannot be changed right now
public class LegacyReportGenerator {
    public void generate(ReportRequest req) {
        // uses static access — declared as deprecated, targeted for refactor
        Environment env = AppContext.legacyGet(Environment.class);
        // ... uses env
    }
}
```

New code — the right way:

```java
// New class — receives context via constructor
public class ReportGenerator {
    private final Environment env;
    private final MetricsContext metrics;

    ReportGenerator(AppContext ctx) {
        this.env     = ctx.get(Environment.class);
        this.metrics = ctx.get(MetricsContext.class);
    }

    public void generate(ReportRequest req) {
        metrics.increment("report.generated");
        // ... uses env and metrics
    }
}
```

### The migration plan

```
Phase 1: Legacy code uses static globals everywhere.

Phase 2: AppContext introduced. Static bridge provides backward compatibility.
         Legacy code still compiles and works unchanged.

Phase 3: Classes are gradually refactored one by one.
         Each refactored class moves from legacyGet() to constructor injection.

Phase 4: All classes use constructor injection.
         legacyGet() and mainInstance removed entirely.
```

This incremental approach is far safer than a big-bang refactor of an entire codebase.

---

## 10. Context Container vs Other Patterns — Full Comparison

| Pattern | What it is | Key difference |
|---|---|---|
| Singleton | One shared instance of a class | No type registry; just one instance of one specific class |
| Static globals | Static fields on a class | No lifecycle, no type safety, hidden dependencies |
| Service Locator | Registry of services looked up by type/name | AppContext IS a typed service locator |
| DI Container (Spring) | Framework that auto-wires dependencies | DI container does wiring automatically; AppContext requires manual registration |
| Registry Pattern | Named collection of shared objects | AppContext IS a typed registry |
| Facade Pattern | Simplified interface to a sub-system | AppContext acts as facade over all services |
| Context Object Pattern | Object that carries contextual state | AppContext IS the context object for the application |
| Object Pool | Reuse expensive objects | AppContext holds pools (like DataSourceContext) but is not itself a pool |

### Is AppContext a DI container?

Not quite — but it is adjacent.

A DI container (Spring's `ApplicationContext`) automatically discovers and wires
dependencies. It figures out: "class A needs a B, so I will create a B, inject it
into A, and manage both." It resolves the full dependency graph automatically.

`AppContext` does none of this. The wiring — which services go into the container —
is written by the developer at the entry point. The container is a holder, not a wirer.

```
DI Container:  builds dependency graph automatically, injects at creation time
AppContext:     manually assembled at startup, components pull what they need
```

Both patterns solve the same root problem (shared dependencies) through different mechanisms.

---

## 11. Dependency Injection — Deep Explanation

### What DI means

Dependency Injection means: instead of a class creating its own dependencies,
the dependencies are provided to the class from outside.

```java
// Without DI — class creates its own dependencies
class OrderService {
    private Environment env        = new Environment(...); // creates it
    private ObjectMapper mapper    = new ObjectMapper();   // creates it
    private ThreadPoolExecutor pool = Executors.newFixedThreadPool(4); // creates it
}

// With DI — dependencies provided from outside
class OrderService {
    private final Environment env;
    private final ObjectMapper mapper;
    private final ThreadPoolExecutor pool;

    OrderService(Environment env, ObjectMapper mapper, ThreadPoolExecutor pool) {
        this.env    = env;    // receives it
        this.mapper = mapper; // receives it
        this.pool   = pool;   // receives it
    }
}
```

### Three forms of DI

**Constructor injection** (preferred — dependencies are explicit and visible):

```java
class OrderService {
    private final AppContext ctx;

    OrderService(AppContext ctx) {
        this.ctx = ctx;
    }
}
```

**Setter injection** (flexible but allows partially constructed objects):

```java
class OrderService {
    private AppContext ctx;

    void setContext(AppContext ctx) {
        this.ctx = ctx;
    }
}
```

**Field injection** (avoid — makes dependencies completely hidden):

```java
class OrderService {
    @Inject
    AppContext ctx; // injected by a framework; invisible unless you read the code
}
```

### Why constructor injection is superior for testing

```java
// In production:
AppContext prodContext = buildProductionContext();
OrderService service = new OrderService(prodContext);

// In a test:
AppContext testContext = AppContext.builder()
    .register(new FakeEnvironment("Linux", "test"))
    .register(new FakeDataSourceContext(inMemoryDB))
    .objectMapper(new ObjectMapper())
    .build();

OrderService service = new OrderService(testContext);
service.processOrder(testOrder);

// The same OrderService code runs against test doubles
// No mocking frameworks needed for basic cases
// No static state to reset between tests
```

The service is completely unaware it is being tested. Same code path. Different context.

---

## 12. Service Locator Pattern — What It Is and the Danger

### What it is

A Service Locator is an object that holds a registry of services and returns them
to any code that asks. `context.get(SomeService.class)` is a service locator API.

### Why it can become an anti-pattern

The danger is **hidden dependencies**. Consider:

```java
class PaymentProcessor {

    void process(Payment payment) {
        Environment env     = AppContext.legacyGet(Environment.class);
        MetricsContext met  = AppContext.legacyGet(MetricsContext.class);
        DataSourceContext db = AppContext.legacyGet(DataSourceContext.class);
        // ^ three hidden dependencies — none visible in constructor or method signature
    }
}
```

From the outside, `PaymentProcessor` looks like it needs nothing.
In reality it has three dependencies. You cannot tell without reading the method body.

This makes:
- testing hard — you have to know which statics to set up
- refactoring risky — you might remove a service that classes secretly depend on
- architecture understanding difficult — the dependency graph is invisible

### Why it is acceptable at the framework boundary

The key is **where** you use it.

```
Entry Point (main method)
    builds AppContext
        passes AppContext to RequestRouter (constructor — declared dependency)
            RequestRouter extracts Environment from context
            RequestRouter passes Environment to PlatformValidator (constructor)
                PlatformValidator uses Environment only — no context knowledge
```

- `RequestRouter` has one declared dependency: `AppContext`
- `PlatformValidator` has one declared dependency: `Environment`
- Neither has hidden dependencies

The service locator access stays at the top of the call stack, at the framework layer.
Business logic classes receive exactly the specific objects they need — not the full context.

---

## 13. Before and After — Code Transformation Examples

### Example 1: Order processing service

**Before (bad — static access + repeated construction)**

```java
class OrderProcessor {

    public OrderResult process(Order order) {
        // hidden dependency — uses global static
        Environment env = AppGlobals.getEnvironment();

        // creates new mapper on every call — expensive
        ObjectMapper mapper = new ObjectMapper();

        // creates new thread pool on every call — extremely wasteful
        ExecutorService pool = Executors.newFixedThreadPool(4);

        PlatformChecker checker = new PlatformChecker();
        checker.setEnvironment(env);
        checker.verify();

        // ... process order using mapper and pool
        pool.shutdown(); // has to manage its own pool — wrong responsibility
    }
}
```

Problems:
- Three hidden dependencies (env, mapper, pool)
- `ObjectMapper` constructed on every call — expensive; not thread-safe configuration
- Thread pool created and destroyed per call — completely defeats its purpose
- Cannot test without modifying global state

**After (good — context injection)**

```java
class OrderProcessor {

    private final AppContext ctx;

    OrderProcessor(AppContext ctx) {
        this.ctx = ctx; // one declared dependency
    }

    public OrderResult process(Order order) {
        Environment env         = ctx.get(Environment.class);
        ObjectMapper mapper     = ctx.getObjectMapper();         // shared, not recreated
        ThreadPoolContext pool   = ctx.get(ThreadPoolContext.class); // shared, managed externally

        PlatformChecker checker = new PlatformChecker(env);     // env injected, not hidden
        checker.verify();

        // ... process order
    }
}
```

Benefits:
- One declared dependency: `AppContext`
- Shared `ObjectMapper` — no repeated construction
- Thread pool managed by the context — not by the service
- Fully testable — pass a test context

---

### Example 2: Testing with a test context

```java
class OrderProcessorTest {

    @Test
    void testProcessOnLinux() {
        // Build a focused test context — only what OrderProcessor needs
        AppContext testCtx = AppContext.builder()
            .register(new Environment("Linux", "test", Map.of()))
            .register(new ThreadPoolContext(1, 1))
            .objectMapper(new ObjectMapper())
            .build();

        OrderProcessor processor = new OrderProcessor(testCtx);
        OrderResult result = processor.process(new Order("item-1", 2));

        assertNotNull(result);
        assertEquals(OrderStatus.SUCCESS, result.getStatus());

        testCtx.close();
    }

    @Test
    void testProcessOnWindows() {
        // Different environment — same processor code
        AppContext testCtx = AppContext.builder()
            .register(new Environment("Windows 11", "test", Map.of()))
            .register(new ThreadPoolContext(1, 1))
            .objectMapper(new ObjectMapper())
            .build();

        OrderProcessor processor = new OrderProcessor(testCtx);
        // test Windows-specific behavior
        testCtx.close();
    }
}
```

Without the context pattern, both tests would have to modify static globals,
creating test interference. With the context, each test is completely isolated.

---

### Example 3: Different deployment modes with same code

```java
// Minimal mode — lightweight deployment, fewer services
AppContext minimalCtx = AppContext.builder()
    .register(new Environment("Linux", "minimal", config))
    .register(new ThreadPoolContext(2, 4))
    // no MetricsContext  — metrics not needed in this mode
    // no FeatureFlagContext — flags not needed
    .objectMapper(new ObjectMapper())
    .build();

// Full production mode — all services
AppContext fullCtx = AppContext.builder()
    .register(new Environment("Linux", "production", config))
    .register(new ThreadPoolContext(8, 32))
    .register(new MetricsContext(prometheusRegistry))
    .register(new FeatureFlagContext(launchDarklyKey))
    .register(new DataSourceContext(jdbcUrl, 20))
    .objectMapper(new ObjectMapper().findAndRegisterModules())
    .build();

// Same OrderProcessor code — works in both modes
// In minimal mode: ctx.get(MetricsContext.class) returns null
// OrderProcessor checks for null and skips metrics recording
```

---

### Example 4: Validator that does NOT need the full context

This is an important pattern — business logic classes should receive only
what they specifically need, not the full context.

```java
class PlatformValidator {

    private final Environment env;

    // receives only Environment — not AppContext
    PlatformValidator(Environment env) {
        this.env = env;
    }

    public void validate() {
        String platform = env.getPlatform();
        Set<String> allowed = Set.of("Linux", "Windows 11", "macOS");
        if (!allowed.contains(platform)) {
            throw new UnsupportedPlatformException(
                "Platform '" + platform + "' is not supported. Allowed: " + allowed
            );
        }
    }
}

// The executor layer extracts Environment and passes it down — context stays at executor level
class RequestExecutor {

    private final AppContext ctx;

    RequestExecutor(AppContext ctx) {
        this.ctx = ctx;
    }

    public void execute(Request req) {
        Environment env = ctx.get(Environment.class);

        // Pass only Environment to the validator — not the whole context
        PlatformValidator validator = new PlatformValidator(env);
        validator.validate();

        // Continue processing
    }
}
```

`PlatformValidator` is completely clean — it has no knowledge of `AppContext`.
It is easy to test, easy to reuse, and easy to understand.

---

## 14. SOLID Principles in This Design

### Single Responsibility Principle

`AppContext` has exactly one responsibility: hold and provide access to
application-scoped runtime services.

It does not process orders, validate platforms, schedule tasks, or collect metrics.
It holds the things that do those jobs and makes them retrievable.

Each service also has one responsibility:
- `Environment` — runtime environment information
- `ThreadPoolContext` — thread pool lifecycle
- `DataSourceContext` — database connections
- `MetricsContext` — metrics collection

---

### Open/Closed Principle

`AppContext` is open for extension and closed for modification.

Adding a new service type requires zero changes to `AppContext` source code:

```java
// New service added — AppContext code unchanged
AppContext context = AppContext.builder()
    .register(new AuditLogContext(auditConfig))  // new service
    .build();

// Works automatically
AuditLogContext audit = context.get(AuditLogContext.class);
```

The startup event pattern follows OCP too. New startup actions are added as
listeners — the container itself does not change.

---

### Liskov Substitution Principle

Every service implements `AppService`. The container treats all of them uniformly
through the interface. `close()` can be called on any service without knowing
its concrete type.

```java
services.forEach((k, v) -> v.close()); // works for ALL service types
```

A `ThreadPoolContext`, `MetricsContext`, or `DataSourceContext` can all substitute
for `AppService` in the container — they all satisfy the contract.

---

### Interface Segregation Principle

`PlatformValidator` does not receive `AppContext`. It receives only `Environment`.
It is insulated from knowing about thread pools, metrics, databases, or anything
else in the context.

Each class gets exactly the dependencies it needs — no fat interfaces, no unused dependencies.

---

### Dependency Inversion Principle

High-level modules (`OrderProcessor`, `RequestExecutor`) depend on the `AppContext`
abstraction and on service interfaces (`Environment`, `ThreadPoolContext`), not on
concrete infrastructure details.

```java
// OrderProcessor depends on AppContext (abstraction) and Environment (interface)
// It does NOT depend on EnvironmentConfig, ThreadPoolManager, or any concrete setup class
```

The concrete wiring — which implementation of `Environment` is used — is decided
at the entry point, not inside the business classes.

---

## 15. When Should You Use a Context Container?

### Use it when:

**Many components need the same shared runtime services.**
If 20+ classes all need `Environment` and `ThreadPoolContext`, a container
wraps them cleanly. Passing them individually adds noise.

**Dependencies are application-scoped.**
Application-scoped means one instance for the entire process lifetime.
For request-scoped data (per HTTP request), use a separate per-request context object.

**Initialization order matters and is complex.**
When service A depends on service B being initialized first, a container
built in stages manages this cleanly.

**Clean resource shutdown is required.**
Thread pools, DB connections, and file handles must be closed reliably on shutdown.
A container that owns the lifecycle guarantees this.

**Multiple deployment modes need different subsets of services.**
A minimal container vs a full production container — same code, different configuration.

**Legacy migration is in progress.**
The static bridge pattern allows gradual migration from static globals to
constructor injection without a big-bang refactor.

---

### Do NOT use it when:

**The system is small.**
Three classes with two shared dependencies do not need a container. Direct
constructor injection is cleaner.

**You put non-service data into it.**
A context container is not a global variable bag. Request parameters, user data,
domain objects, configuration values — these do not belong in an application context.

**Every class at every level uses it.**
If all classes everywhere call `context.get(...)` for their dependencies,
you have a hidden-dependency anti-pattern that is worse than static globals.
The container belongs at the framework/runtime boundary.

---

## 16. Common Mistakes and Anti-Patterns

### Mistake 1: The junk drawer

```java
// BAD — stuffing everything in
context.register(currentUser);          // request-scoped, not app-scoped
context.register(requestId);            // request-scoped
context.register(pageSize);             // a primitive value, not a service
context.register(temporaryWorkBuffer);  // transient state
```

The container is for app-scoped runtime services with a defined lifecycle.
Everything else should be passed directly where it is needed.

---

### Mistake 2: Every class uses context to retrieve all its dependencies

```java
// BAD — hidden dependencies everywhere
class InvoiceGenerator {
    private AppContext ctx;

    InvoiceGenerator(AppContext ctx) {
        this.ctx = ctx;
    }

    void generate(Invoice invoice) {
        Environment env         = ctx.get(Environment.class);        // hidden
        ObjectMapper mapper     = ctx.getObjectMapper();              // hidden
        MetricsContext metrics  = ctx.get(MetricsContext.class);     // hidden
        DataSourceContext db    = ctx.get(DataSourceContext.class);  // hidden
        // 4 hidden dependencies — none visible from constructor
    }
}
```

Better — extract in the layer above, inject specifically:

```java
// BETTER — explicit dependencies
class InvoiceGenerator {
    private final ObjectMapper mapper;
    private final MetricsContext metrics;

    InvoiceGenerator(ObjectMapper mapper, MetricsContext metrics) {
        this.mapper  = mapper;
        this.metrics = metrics;
        // exactly what is needed — nothing more
    }
}
```

---

### Mistake 3: Registering mutable state that changes at runtime

```java
// BAD — application config can change; container is build-once
context.register(new DynamicConfig()); // if DynamicConfig changes its state, all
                                        // callers see different values unexpectedly
```

Services in the container should be stable for the application lifetime.
Dynamic or changing state belongs in a dedicated cache or state store, not in the context.

---

### Mistake 4: Not catching exceptions in close()

```java
// BAD — if MetricsContext.close() throws, ThreadPoolContext never closes
@Override
public void close() {
    services.forEach((k, v) -> v.close()); // uncaught exception stops the loop
}

// GOOD — catch per service, log, continue
@Override
public void close() {
    services.forEach((name, service) -> {
        try {
            service.close();
        } catch (Exception e) {
            log.error("Failed to close service {}: {}", name, e.getMessage());
        }
    });
}
```

---

### Mistake 5: Using the static bridge in new code

```java
// BAD — new code using the legacy static bridge
public class NewPaymentService {
    public void pay(Payment p) {
        Environment env = AppContext.legacyGet(Environment.class); // wrong — this is for legacy only
    }
}
```

Any class written after the context container was introduced should receive
the context through its constructor. The static bridge is a migration tool,
not a permanent feature.

---

## 17. Full Architecture Diagram

```
Application Entry Point (main method)
         │
         │  Step 1: Build individual services
         │
         ├── new Environment(platform, mode, config)
         ├── new ThreadPoolContext(coreSize, maxSize)
         ├── new DataSourceContext(jdbcUrl, poolSize)
         ├── new MetricsContext(meterRegistry)
         └── new FeatureFlagContext(apiKey)
         │
         │  Step 2: Assemble into AppContext
         │
         ▼
┌──────────────────────────────────────────────────────────────┐
│                       AppContext                             │
│                                                              │
│  Map<String, AppService>:                                    │
│  ┌───────────────────────┬──────────────────────────────┐   │
│  │ "...Environment"      │ Environment instance         │   │
│  │ "...ThreadPoolContext"│ ThreadPoolContext instance    │   │
│  │ "...DataSourceContext"│ DataSourceContext instance    │   │
│  │ "...MetricsContext"   │ MetricsContext instance       │   │
│  │ "...FeatureFlagCtx"   │ FeatureFlagContext instance   │   │
│  └───────────────────────┴──────────────────────────────┘   │
│  ObjectMapper: shared instance                               │
│  mainInstance: volatile static ref (for legacy bridge)       │
└───────────────────────────┬──────────────────────────────────┘
                            │
         Step 3: Fire startup event
                            │
         ┌──────────────────▼──────────────────┐
         │  startupListeners.forEach(...)       │
         │  CacheWarmer.warmUp(dataSourceCtx)   │
         │  BackgroundJobScheduler.start(pool)  │
         └─────────────────────────────────────┘
                            │
         Step 4: Pass context to runtime layer
                            │
              ┌─────────────┴──────────────┐
              │                            │
     RequestRouter(ctx)           AdminHandler(ctx)
              │                            │
              │ ctx.get(Environment.class) │ ctx.get(MetricsContext.class)
              │                            │
         RequestExecutor(ctx)       MetricsDashboard(metricsCtx)
              │
              │ Extracts what sub-components need
              │
    ┌─────────┴──────────┐
    │                    │
PlatformValidator(env)  OrderProcessor(mapper, pool)
   (no ctx needed)       (no ctx needed)


Legacy code path (temporary, migration only):
LegacyReportGenerator ──► AppContext.legacyGet(Environment.class)
                                    │
                                    ▼
                            mainInstance.get(Environment.class)


Shutdown (JVM shutdown hook or try-with-resources):
appContext.close()
    │
    ├── FeatureFlagContext.close()   (disconnect from flags service)
    ├── MetricsContext.close()       (flush pending metrics)
    ├── DataSourceContext.close()    (drain and close connection pool)
    ├── ThreadPoolContext.close()    (shutdown(), awaitTermination())
    └── Environment.close()          (nothing to release)
```

---

## 18. One-Line Summary of Every Concept

| Concept | One-line summary |
|---|---|
| AppContext | Holds all app-scoped runtime services in one controlled, typed, lifecycle-managed place. |
| AppService | Interface all registered services implement; defines the `close()` contract. |
| Type token (`Class<C>`) | Passing a `Class` object to carry generic type information into runtime despite erasure. |
| Type erasure | Java removes generic type parameters at runtime; `List<String>` becomes `List` in bytecode. |
| Bounded wildcard (`? extends T`) | Allows a collection of unknown subtypes of T; used to accept any `AppService` subtype. |
| `Class.cast()` | Runtime-checked cast using a `Class` token; throws immediately with clear error if wrong type. |
| `AutoCloseable` | Interface enabling try-with-resources; guarantees `close()` is called for resource cleanup. |
| `final` fields | Assigned once at construction; provide thread safety via Java Memory Model guarantees. |
| `volatile` | Ensures writes to a field are immediately visible to all threads via main memory. |
| Happens-before | JMM guarantee: a `volatile` write by Thread A is visible to Thread B on next read. |
| Service Locator | An object that holds a registry of services and returns them by type or name. |
| Dependency Injection | Providing dependencies to a class from outside rather than having the class create them. |
| Constructor injection | Declaring dependencies as constructor parameters — visible, testable, immutable. |
| Hidden dependency | A dependency obtained via static or locator without being declared — makes code opaque. |
| Registry Pattern | A named collection of objects populated at startup and looked up at runtime. |
| Facade Pattern | A simplified unified interface over a set of sub-systems. |
| Observer Pattern | Subject fires an event; registered listeners react — used here for the startup event. |
| Template Method | Supertype defines structure of an algorithm; subtypes fill in the concrete steps. |
| SRP | A class should have one reason to change — AppContext's is holding runtime services. |
| OCP | Open for extension (new services), closed for modification (no AppContext source changes). |
| LSP | Any `AppService` subtype can substitute for `AppService` in the container. |
| ISP | Classes receive only the specific services they need — not the full context. |
| DIP | High-level classes depend on abstractions (`AppContext`, `Environment`) not on concretions. |
| Static bridge | A temporary static accessor that lets legacy code use the context during migration. |
| Lifecycle management | The container owns startup and shutdown of all services it holds. |
| Startup event | Fires after container is fully built; allows components that need the context to initialize safely. |
| Shutdown hook | Registered with `Runtime.getRuntime()` — ensures `close()` runs on JVM exit. |

---

## 19. Quick Reference Card

```
What is AppContext?
  A typed registry of application-scoped runtime services
  that also owns their lifecycle (startup event, close).

What belongs in it?
  Environment, ThreadPoolContext, DataSourceContext,
  HttpClientContext, MetricsContext, FeatureFlagContext,
  ObjectMapper (shared JSON serializer)

What does NOT belong in it?
  Per-request data, user sessions, domain objects,
  primitive config values, transient business state

How to retrieve a service:
  Environment env = context.get(Environment.class);
  ObjectMapper m  = context.getObjectMapper();

How to build it:
  AppContext ctx = AppContext.builder()
      .register(new Environment(...))
      .register(new ThreadPoolContext(...))
      .objectMapper(new ObjectMapper())
      .build();

How to close it:
  context.close();
  // or: try (AppContext ctx = build()) { }
  // or: Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));

Legacy access (migration only, new code must NOT use this):
  AppContext.legacyGet(Environment.class);

The typed lookup in one sentence:
  get(Class<C>) uses class name as map key, retrieves the service,
  and casts it to C using type.cast() for runtime safety.

Why final fields?
  Assigned once; Java Memory Model guarantees cross-thread visibility.

Why volatile on mainInstance?
  Writes must be immediately visible to all threads — not cached per CPU.

SOLID in this design:
  SRP: AppContext has one job — hold runtime services
  OCP: new services need no AppContext source changes
  LSP: all AppService subtypes substitutable through the interface
  ISP: validators/processors receive only what they specifically need
  DIP: business classes depend on service abstractions, not concretions

When to use:
  Many shared app-scoped services + lifecycle management needed
  + multiple deployment modes + legacy migration in progress

When NOT to use:
  Small system, request-scoped data, random business state dump
```

---

*This document covers the Application Context Container pattern from first principles
through every Java language concept, design pattern, and architectural decision involved.
All examples use generic production code — no framework-specific or proprietary references.*
