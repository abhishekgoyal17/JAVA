# Java Design Patterns — Complete Revision Guide

> One-stop reference for all 23 GoF design patterns with ASCII UML diagrams, full Java code (including `main`), real-world analogies, and when-to-use notes.

---

## Table of Contents

### 🏗️ Creational Patterns
| # | Pattern | One-liner |
|---|---------|-----------|
| 1 | [Singleton](#1-singleton) | One instance, globally accessible |
| 2 | [Factory Method](#2-factory-method) | Subclass decides what to create |
| 3 | [Abstract Factory](#3-abstract-factory) | Factory of factories |
| 4 | [Builder](#4-builder) | Step-by-step object construction |
| 5 | [Prototype](#5-prototype) | Clone existing objects |

### 🏛️ Structural Patterns
| # | Pattern | One-liner |
|---|---------|-----------|
| 6 | [Adapter](#6-adapter) | Makes incompatible interfaces work together |
| 7 | [Bridge](#7-bridge) | Separate abstraction from implementation |
| 8 | [Composite](#8-composite) | Tree structure of objects |
| 9 | [Decorator](#9-decorator) | Add behavior without subclassing |
| 10 | [Facade](#10-facade) | Simple interface to a complex subsystem |
| 11 | [Flyweight](#11-flyweight) | Share common state among many objects |
| 12 | [Proxy](#12-proxy) | Placeholder that controls access |

### 🎭 Behavioral Patterns
| # | Pattern | One-liner |
|---|---------|-----------|
| 13 | [Chain of Responsibility](#13-chain-of-responsibility) | Pass request along a handler chain |
| 14 | [Command](#14-command) | Encapsulate a request as an object |
| 15 | [Iterator](#15-iterator) | Traverse a collection without knowing its internals |
| 16 | [Mediator](#16-mediator) | Centralize communication between objects |
| 17 | [Memento](#17-memento) | Save and restore object state |
| 18 | [Observer](#18-observer) | Notify dependents on state change |
| 19 | [State](#19-state) | Change behavior based on internal state |
| 20 | [Strategy](#20-strategy) | Swap algorithms at runtime |
| 21 | [Template Method](#21-template-method) | Define skeleton, let subclasses fill steps |
| 22 | [Visitor](#22-visitor) | Add operations without changing classes |
| 23 | [Interpreter](#23-interpreter) | Grammar rules as classes |

---

## Quick Mental Model

```
CREATIONAL  → "How do I create objects?"
STRUCTURAL  → "How do I compose objects?"
BEHAVIORAL  → "How do objects talk to each other?"
```

---

# 🏗️ CREATIONAL PATTERNS

---

## 1. Singleton

### Intent
Ensure a class has **only one instance** and provide a global access point to it.

### Real-world analogy
Your country has one President at a time. Everyone refers to *the* President, not *a* President.

### When to use
- Logger, Configuration manager, Thread pool, Cache, Connection pool

### UML Diagram
```
┌─────────────────────────────┐
│         Singleton           │
├─────────────────────────────┤
│ - instance: Singleton       │
│ - Singleton()               │  ← private constructor
├─────────────────────────────┤
│ + getInstance(): Singleton  │  ← static, thread-safe
│ + doWork(): void            │
└─────────────────────────────┘
         ▲
         │ refers to itself
         └──────────────────
```

### Full Code

```java
// Thread-safe Singleton using double-checked locking
public class DatabaseConnection {

    // volatile ensures visibility across threads
    private static volatile DatabaseConnection instance;

    private String url;
    private int connectionCount = 0;

    // Private constructor — no one can call new DatabaseConnection()
    private DatabaseConnection() {
        this.url = "jdbc:mysql://localhost:3306/mydb";
        System.out.println("DatabaseConnection created (this should print ONCE)");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {                          // First check (no lock)
            synchronized (DatabaseConnection.class) {
                if (instance == null) {                  // Second check (with lock)
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public void query(String sql) {
        connectionCount++;
        System.out.println("[Connection #" + connectionCount + "] Executing: " + sql);
    }

    public String getUrl() {
        return url;
    }

    // ─── Main ────────────────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {
        // Simulate multiple threads grabbing the singleton
        Runnable task = () -> {
            DatabaseConnection db = DatabaseConnection.getInstance();
            db.query("SELECT * FROM users");
            System.out.println("Same instance? " + (db == DatabaseConnection.getInstance()));
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");
        Thread t3 = new Thread(task, "Thread-3");

        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();

        // All three should use the SAME instance
        System.out.println("\nTotal connections URL: " + DatabaseConnection.getInstance().getUrl());
    }
}
```

### ⚡ Pro tip — Enum Singleton (safest)
```java
public enum AppConfig {
    INSTANCE;

    private final String env = "production";

    public String getEnv() { return env; }

    public static void main(String[] args) {
        System.out.println(AppConfig.INSTANCE.getEnv()); // production
    }
}
```
Enum Singleton is serialization-safe and reflection-safe by default.

---

## 2. Factory Method

### Intent
Define an interface for creating an object, but **let subclasses decide which class to instantiate**.

### Real-world analogy
A logistics company (Creator) says "I'll deliver your package" but whether it uses a Truck or Ship depends on the subclass (RoadLogistics vs SeaLogistics).

### When to use
- When you don't know ahead of time which class to instantiate
- When subclasses should control what gets created

### UML Diagram
```
        ┌──────────────────────┐
        │   Creator (abstract)  │
        ├──────────────────────┤
        │ + createProduct()    │ ← factory method (abstract)
        │ + deliver(): void    │ ← uses createProduct internally
        └──────────────────────┘
               ▲          ▲
               │          │
   ┌───────────┐    ┌──────────────┐
   │RoadLogistics│  │SeaLogistics  │
   ├─────────────┤  ├──────────────┤
   │+createProduct│  │+createProduct│
   │ → Truck    │  │  → Ship      │
   └─────────────┘  └──────────────┘

        ┌──────────────┐
        │  Product      │ ← interface
        ├──────────────┤
        │ + deliver()  │
        └──────────────┘
          ▲        ▲
          │        │
       Truck      Ship
```

### Full Code

```java
// ── Product interface ─────────────────────────────────────
interface Transport {
    void deliver(String cargo);
}

// ── Concrete Products ─────────────────────────────────────
class Truck implements Transport {
    @Override
    public void deliver(String cargo) {
        System.out.println("🚚 Truck delivering [" + cargo + "] by road");
    }
}

class Ship implements Transport {
    @Override
    public void deliver(String cargo) {
        System.out.println("🚢 Ship delivering [" + cargo + "] by sea");
    }
}

class Drone implements Transport {
    @Override
    public void deliver(String cargo) {
        System.out.println("🚁 Drone delivering [" + cargo + "] by air");
    }
}

// ── Creator (abstract) ────────────────────────────────────
abstract class Logistics {
    // THE factory method — subclasses override this
    public abstract Transport createTransport();

    // Business logic uses the factory method
    public void planDelivery(String cargo) {
        Transport transport = createTransport();  // polymorphic creation
        System.out.println("Planning delivery...");
        transport.deliver(cargo);
        System.out.println("Delivery complete!\n");
    }
}

// ── Concrete Creators ─────────────────────────────────────
class RoadLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Truck();
    }
}

class SeaLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Ship();
    }
}

class AirLogistics extends Logistics {
    @Override
    public Transport createTransport() {
        return new Drone();
    }
}

// ── Main ──────────────────────────────────────────────────
class FactoryMethodDemo {
    public static void main(String[] args) {
        Logistics road = new RoadLogistics();
        road.planDelivery("Electronics");

        Logistics sea = new SeaLogistics();
        sea.planDelivery("Bulk grain");

        Logistics air = new AirLogistics();
        air.planDelivery("Medical supplies");

        // Client code works with any Logistics subclass
        // without knowing which Transport will be used
    }
}
```

**Output:**
```
Planning delivery...
🚚 Truck delivering [Electronics] by road
Delivery complete!

Planning delivery...
🚢 Ship delivering [Bulk grain] by sea
Delivery complete!

Planning delivery...
🚁 Drone delivering [Medical supplies] by air
Delivery complete!
```

---

## 3. Abstract Factory

### Intent
Provide an interface for creating **families of related objects** without specifying their concrete classes.

### Real-world analogy
IKEA (Abstract Factory) produces matching families of furniture — ModernSofa + ModernTable, or VictorianSofa + VictorianTable. You don't mix and match families.

### When to use
- When your system must be independent of how its products are created
- When products must be used together (themes, OS-specific UI kits)

### UML Diagram
```
  «interface»                    «interface»
  GUIFactory                     Button
  ─────────────                  ──────────
  createButton()                 render()
  createCheckbox()               onClick()
       ▲
       │
  ┌────┴──────────┐
  │               │
WinFactory    MacFactory
──────────    ──────────
createButton()→WinButton
createCheckbox()→WinCheckbox
```

### Full Code

```java
// ── Abstract Products ─────────────────────────────────────
interface Button {
    void render();
    void onClick();
}

interface Checkbox {
    void render();
    void onCheck();
}

// ── Windows Family ────────────────────────────────────────
class WindowsButton implements Button {
    @Override public void render()  { System.out.println("[Windows] Rendering a flat rectangle button"); }
    @Override public void onClick() { System.out.println("[Windows] Button clicked — ripple effect"); }
}

class WindowsCheckbox implements Checkbox {
    @Override public void render()  { System.out.println("[Windows] Rendering a square checkbox"); }
    @Override public void onCheck() { System.out.println("[Windows] Checkbox checked — tick animation"); }
}

// ── Mac Family ────────────────────────────────────────────
class MacButton implements Button {
    @Override public void render()  { System.out.println("[Mac] Rendering a rounded glossy button"); }
    @Override public void onClick() { System.out.println("[Mac] Button clicked — bounce effect"); }
}

class MacCheckbox implements Checkbox {
    @Override public void render()  { System.out.println("[Mac] Rendering a rounded checkbox"); }
    @Override public void onCheck() { System.out.println("[Mac] Checkbox checked — slide animation"); }
}

// ── Abstract Factory ──────────────────────────────────────
interface GUIFactory {
    Button   createButton();
    Checkbox createCheckbox();
}

// ── Concrete Factories ────────────────────────────────────
class WindowsFactory implements GUIFactory {
    @Override public Button   createButton()   { return new WindowsButton(); }
    @Override public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}

class MacFactory implements GUIFactory {
    @Override public Button   createButton()   { return new MacButton(); }
    @Override public Checkbox createCheckbox() { return new MacCheckbox(); }
}

// ── Client code (knows nothing about concrete classes) ────
class Application {
    private Button   button;
    private Checkbox checkbox;

    public Application(GUIFactory factory) {
        button   = factory.createButton();
        checkbox = factory.createCheckbox();
    }

    public void renderUI() {
        button.render();
        checkbox.render();
    }

    public void interact() {
        button.onClick();
        checkbox.onCheck();
    }
}

// ── Main ──────────────────────────────────────────────────
class AbstractFactoryDemo {
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        GUIFactory factory;

        if (os.contains("win")) {
            factory = new WindowsFactory();
            System.out.println("Running on Windows\n");
        } else {
            factory = new MacFactory();
            System.out.println("Running on Mac\n");
        }

        Application app = new Application(factory);
        app.renderUI();
        System.out.println();
        app.interact();

        // You can also force a factory for testing
        System.out.println("\n--- Force Mac UI ---");
        Application macApp = new Application(new MacFactory());
        macApp.renderUI();
    }
}
```

---

## 4. Builder

### Intent
Construct a complex object **step by step**. The same construction process can create different representations.

### Real-world analogy
Ordering a burger at a restaurant — you say "add cheese, skip onions, add extra patty." The cashier (Director) tells the kitchen (Builder) what to build. You get the final burger.

### When to use
- Telescoping constructor problem (10+ constructor params)
- When you need the same steps but different results

### UML Diagram
```
   Director                   Builder «interface»
   ────────                   ──────────────────
   builder: Builder           setSize()
   construct(): void ─────►   setRam()
                              setStorage()
                              build(): Computer
                                   ▲
                              GamingComputerBuilder
                              OfficeComputerBuilder
```

### Full Code

```java
// ── Product ───────────────────────────────────────────────
class Computer {
    private String cpu;
    private String ram;
    private String storage;
    private String gpu;
    private String cooler;
    private boolean hasWifi;
    private boolean hasBluetooth;

    // Private constructor — only Builder can call this
    private Computer() {}

    @Override
    public String toString() {
        return String.format(
            "\n╔══ Computer Spec ══════════════════╗" +
            "\n║  CPU       : %-22s║" +
            "\n║  RAM       : %-22s║" +
            "\n║  Storage   : %-22s║" +
            "\n║  GPU       : %-22s║" +
            "\n║  Cooler    : %-22s║" +
            "\n║  WiFi      : %-22s║" +
            "\n║  Bluetooth : %-22s║" +
            "\n╚═══════════════════════════════════╝",
            cpu, ram, storage, gpu, cooler, hasWifi, hasBluetooth
        );
    }

    // ── Builder (static inner class) ──────────────────────
    public static class Builder {
        private String cpu        = "Intel i3";
        private String ram        = "8GB";
        private String storage    = "256GB SSD";
        private String gpu        = "Integrated";
        private String cooler     = "Stock Cooler";
        private boolean hasWifi   = true;
        private boolean hasBluetooth = false;

        public Builder cpu(String cpu)         { this.cpu = cpu; return this; }
        public Builder ram(String ram)         { this.ram = ram; return this; }
        public Builder storage(String storage) { this.storage = storage; return this; }
        public Builder gpu(String gpu)         { this.gpu = gpu; return this; }
        public Builder cooler(String cooler)   { this.cooler = cooler; return this; }
        public Builder wifi(boolean b)         { this.hasWifi = b; return this; }
        public Builder bluetooth(boolean b)    { this.hasBluetooth = b; return this; }

        public Computer build() {
            // Could add validation here
            if (gpu.contains("RTX") && cooler.equals("Stock Cooler")) {
                System.out.println("⚠ Warning: RTX GPU with stock cooler — consider upgrading cooling!");
            }
            Computer computer = new Computer();
            computer.cpu         = this.cpu;
            computer.ram         = this.ram;
            computer.storage     = this.storage;
            computer.gpu         = this.gpu;
            computer.cooler      = this.cooler;
            computer.hasWifi     = this.hasWifi;
            computer.hasBluetooth = this.hasBluetooth;
            return computer;
        }
    }

    // ── Main ──────────────────────────────────────────────
    public static void main(String[] args) {
        // Gaming PC
        Computer gaming = new Computer.Builder()
            .cpu("AMD Ryzen 9 7900X")
            .ram("64GB DDR5")
            .storage("2TB NVMe SSD")
            .gpu("NVIDIA RTX 4090")
            .cooler("360mm AIO Liquid")
            .wifi(true)
            .bluetooth(true)
            .build();

        System.out.println("Gaming PC:" + gaming);

        // Office PC — only set what you need, rest use defaults
        Computer office = new Computer.Builder()
            .cpu("Intel i5-12400")
            .ram("16GB DDR4")
            .storage("512GB SSD")
            .build();

        System.out.println("\nOffice PC:" + office);

        // Server — no wifi, no bluetooth
        Computer server = new Computer.Builder()
            .cpu("Intel Xeon Gold")
            .ram("256GB ECC")
            .storage("8TB RAID-5")
            .gpu("None")
            .wifi(false)
            .build();

        System.out.println("\nServer:" + server);
    }
}
```

---

## 5. Prototype

### Intent
Create new objects by **cloning** an existing object (the prototype) instead of creating from scratch.

### Real-world analogy
A cell divides (clones itself) to create new cells. A Xerox copy of a document.

### When to use
- Object creation is expensive (DB fetch, API call)
- You need many objects that are slight variations of each other

### UML Diagram
```
    «interface»
    Cloneable / Prototype
    ─────────────────────
    clone(): Prototype
           ▲
    ┌──────┴──────┐
    │             │
 Circle        Rectangle
 ───────        ─────────
 color          width
 radius         height
 clone()        clone()
```

### Full Code

```java
import java.util.HashMap;
import java.util.Map;

// ── Prototype interface ───────────────────────────────────
abstract class Shape implements Cloneable {
    protected String color;
    protected String type;

    public Shape() {}

    // Copy constructor — used by clone()
    public Shape(Shape source) {
        this.color = source.color;
        this.type  = source.type;
    }

    public abstract Shape clone();
    public abstract double area();

    @Override
    public String toString() {
        return String.format("%s [color=%s, area=%.2f]", type, color, area());
    }
}

// ── Concrete Prototypes ───────────────────────────────────
class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        this.color  = color;
        this.radius = radius;
        this.type   = "Circle";
    }

    // Copy constructor
    public Circle(Circle source) {
        super(source);
        this.radius = source.radius;
    }

    public void setRadius(double r) { this.radius = r; }

    @Override
    public Shape clone() { return new Circle(this); }

    @Override
    public double area() { return Math.PI * radius * radius; }
}

class Rectangle extends Shape {
    private double width, height;

    public Rectangle(String color, double width, double height) {
        this.color  = color;
        this.width  = width;
        this.height = height;
        this.type   = "Rectangle";
    }

    public Rectangle(Rectangle source) {
        super(source);
        this.width  = source.width;
        this.height = source.height;
    }

    @Override
    public Shape clone() { return new Rectangle(this); }

    @Override
    public double area() { return width * height; }
}

// ── Prototype Registry ────────────────────────────────────
class ShapeCache {
    private final Map<String, Shape> cache = new HashMap<>();

    public void register(String key, Shape shape) {
        cache.put(key, shape);
    }

    public Shape get(String key) {
        Shape prototype = cache.get(key);
        if (prototype == null) throw new RuntimeException("No prototype for key: " + key);
        return prototype.clone(); // return a CLONE, not the original
    }
}

// ── Main ──────────────────────────────────────────────────
class PrototypeDemo {
    public static void main(String[] args) {
        ShapeCache cache = new ShapeCache();

        // Register prototypes (imagine these were loaded from DB)
        cache.register("small-red-circle",   new Circle("Red",   5.0));
        cache.register("large-blue-rect",    new Rectangle("Blue", 20.0, 10.0));

        // Clone from cache — fast, no new construction cost
        Shape s1 = cache.get("small-red-circle");
        Shape s2 = cache.get("small-red-circle");
        Shape s3 = cache.get("large-blue-rect");

        System.out.println("s1: " + s1);
        System.out.println("s2: " + s2);
        System.out.println("s3: " + s3);

        // s1 and s2 are DIFFERENT objects
        System.out.println("\ns1 == s2? " + (s1 == s2));       // false
        System.out.println("s1.equals(s2)? " + s1.equals(s2)); // depends on equals()

        // Modify clone without affecting prototype
        ((Circle) s1).setRadius(999);
        Shape original = cache.get("small-red-circle");
        System.out.println("\nAfter modifying s1 radius:");
        System.out.println("s1 (modified): " + s1);
        System.out.println("new clone    : " + original); // still 5.0 radius
    }
}
```

---

# 🏛️ STRUCTURAL PATTERNS

---

## 6. Adapter

### Intent
Convert the interface of a class into another interface that clients expect. Lets incompatible interfaces work together.

### Real-world analogy
A power adapter lets your Indian 3-pin plug work in a European 2-pin socket.

### When to use
- Integrating a third-party library whose interface doesn't match yours
- Reusing legacy code with a new interface

### UML Diagram
```
  Client ──► Target (interface)
              ─────────────────
              request(): void
                    ▲
              ┌─────┴──────────────┐
              │                    │
         ConcreteTarget        Adapter
         (existing)            ────────────────
                               adaptee: Adaptee
                               request() {
                                 adaptee.specificRequest()
                               }
                                    │
                                    ▼
                               Adaptee (legacy)
                               ──────────────────
                               specificRequest()
```

### Full Code

```java
// ── Target interface (what client expects) ────────────────
interface MediaPlayer {
    void play(String fileName);
}

// ── Adaptee (incompatible 3rd-party library) ──────────────
class VlcPlayer {
    public void playVlc(String fileName) {
        System.out.println("🎬 VLC Player: Playing VLC file → " + fileName);
    }
}

class Mp4AdvancedPlayer {
    public void playMp4(String fileName) {
        System.out.println("🎥 MP4 Advanced Player: Playing MP4 file → " + fileName);
    }
}

// ── Adapter ───────────────────────────────────────────────
class MediaAdapter implements MediaPlayer {
    private VlcPlayer       vlcPlayer;
    private Mp4AdvancedPlayer mp4Player;
    private String audioType;

    public MediaAdapter(String audioType) {
        this.audioType = audioType;
        if (audioType.equalsIgnoreCase("vlc")) {
            vlcPlayer = new VlcPlayer();
        } else if (audioType.equalsIgnoreCase("mp4")) {
            mp4Player = new Mp4AdvancedPlayer();
        }
    }

    @Override
    public void play(String fileName) {
        if (audioType.equalsIgnoreCase("vlc")) {
            vlcPlayer.playVlc(fileName);
        } else if (audioType.equalsIgnoreCase("mp4")) {
            mp4Player.playMp4(fileName);
        }
    }
}

// ── Concrete Target ───────────────────────────────────────
class AudioPlayer implements MediaPlayer {
    private MediaAdapter adapter;

    @Override
    public void play(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        if (ext.equals("mp3")) {
            System.out.println("🎵 AudioPlayer: Playing MP3 natively → " + fileName);
        } else if (ext.equals("vlc") || ext.equals("mp4")) {
            adapter = new MediaAdapter(ext);
            adapter.play(fileName);
        } else {
            System.out.println("❌ Format not supported: " + ext);
        }
    }

    // ── Main ──────────────────────────────────────────────
    public static void main(String[] args) {
        AudioPlayer player = new AudioPlayer();

        player.play("song.mp3");          // native
        player.play("movie.vlc");         // via adapter
        player.play("tutorial.mp4");      // via adapter
        player.play("podcast.aac");       // unsupported
    }
}
```

---

## 7. Bridge

### Intent
**Decouple** an abstraction from its implementation so the two can vary independently.

### Real-world analogy
A TV remote (abstraction) can control any brand of TV (implementation). You can have a basic remote or advanced remote, and a Sony or LG TV — they're independent.

### When to use
- When you want to avoid a permanent binding between abstraction and implementation
- When both abstraction and implementation should be extensible via subclassing

### UML Diagram
```
   Abstraction                 Implementor «interface»
   ───────────                 ──────────────────────
   impl: Implementor  ──────►  operationImpl()
   operation()                       ▲
        ▲                     ┌──────┴───────┐
        │               ConcreteImplA    ConcreteImplB
   RefinedAbstraction
```

### Full Code

```java
// ── Implementor (device interface) ───────────────────────
interface Device {
    boolean isEnabled();
    void enable();
    void disable();
    int getVolume();
    void setVolume(int percent);
    int getChannel();
    void setChannel(int channel);
    String getName();
}

// ── Concrete Implementors ─────────────────────────────────
class TV implements Device {
    private boolean on = false;
    private int volume = 30;
    private int channel = 1;

    @Override public boolean isEnabled()          { return on; }
    @Override public void enable()                { on = true;  System.out.println("TV: powered ON"); }
    @Override public void disable()               { on = false; System.out.println("TV: powered OFF"); }
    @Override public int getVolume()              { return volume; }
    @Override public void setVolume(int v)        { volume = Math.min(100, Math.max(0, v)); System.out.println("TV volume → " + volume); }
    @Override public int getChannel()             { return channel; }
    @Override public void setChannel(int c)       { channel = c; System.out.println("TV channel → " + channel); }
    @Override public String getName()             { return "Samsung TV"; }
}

class Radio implements Device {
    private boolean on = false;
    private int volume = 50;
    private int channel = 1;

    @Override public boolean isEnabled()          { return on; }
    @Override public void enable()                { on = true;  System.out.println("Radio: powered ON"); }
    @Override public void disable()               { on = false; System.out.println("Radio: powered OFF"); }
    @Override public int getVolume()              { return volume; }
    @Override public void setVolume(int v)        { volume = v; System.out.println("Radio volume → " + volume); }
    @Override public int getChannel()             { return channel; }
    @Override public void setChannel(int c)       { channel = c; System.out.println("Radio channel (FM) → " + channel); }
    @Override public String getName()             { return "Sony Radio"; }
}

// ── Abstraction ───────────────────────────────────────────
class Remote {
    protected Device device;

    public Remote(Device device) {
        this.device = device;
    }

    public void togglePower() {
        if (device.isEnabled()) device.disable();
        else                    device.enable();
    }

    public void volumeUp()   { device.setVolume(device.getVolume() + 10); }
    public void volumeDown() { device.setVolume(device.getVolume() - 10); }
    public void channelUp()  { device.setChannel(device.getChannel() + 1); }
    public void channelDown(){ device.setChannel(device.getChannel() - 1); }
}

// ── Refined Abstraction ───────────────────────────────────
class AdvancedRemote extends Remote {
    public AdvancedRemote(Device device) {
        super(device);
    }

    public void mute() {
        System.out.println("Advanced Remote: MUTE");
        device.setVolume(0);
    }

    public void setChannel(int channel) {
        System.out.println("Advanced Remote: jumping to channel " + channel);
        device.setChannel(channel);
    }
}

// ── Main ──────────────────────────────────────────────────
class BridgeDemo {
    public static void main(String[] args) {
        System.out.println("=== Basic Remote + TV ===");
        Remote remote = new Remote(new TV());
        remote.togglePower();
        remote.volumeUp();
        remote.channelUp();

        System.out.println("\n=== Advanced Remote + Radio ===");
        AdvancedRemote advanced = new AdvancedRemote(new Radio());
        advanced.togglePower();
        advanced.volumeUp();
        advanced.mute();
        advanced.setChannel(98);

        // The key: Remote and Device vary independently
        // Add a new Device (SmartTV) → no change to Remote
        // Add a new Remote (VoiceRemote) → no change to Device
    }
}
```

---

## 8. Composite

### Intent
Compose objects into **tree structures** to represent part-whole hierarchies. Clients treat individual objects and compositions uniformly.

### Real-world analogy
A file system: a Folder can contain Files or other Folders. You can `delete()` a File or a Folder with the same call.

### When to use
- When you have tree-structured data
- When clients should ignore the difference between leaf and composite nodes

### UML Diagram
```
       Component «interface»
       ───────────────────
       getName()
       getSize()
       display(depth)
           ▲
     ┌─────┴──────┐
   File          Folder
   ─────         ──────────────────
   name          name
   size          children: List<Component>
                 add(Component)
                 remove(Component)
```

### Full Code

```java
import java.util.ArrayList;
import java.util.List;

// ── Component ─────────────────────────────────────────────
interface FileSystemComponent {
    String getName();
    long getSize();
    void display(int depth);
}

// ── Leaf ──────────────────────────────────────────────────
class File implements FileSystemComponent {
    private final String name;
    private final long   size; // bytes

    public File(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override public String getName() { return name; }
    @Override public long getSize()   { return size; }

    @Override
    public void display(int depth) {
        System.out.println("  ".repeat(depth) + "📄 " + name + " (" + size + " bytes)");
    }
}

// ── Composite ─────────────────────────────────────────────
class Folder implements FileSystemComponent {
    private final String name;
    private final List<FileSystemComponent> children = new ArrayList<>();

    public Folder(String name) {
        this.name = name;
    }

    public void add(FileSystemComponent c)    { children.add(c); }
    public void remove(FileSystemComponent c) { children.remove(c); }

    @Override public String getName() { return name; }

    @Override
    public long getSize() {
        // Recursively sum children
        return children.stream().mapToLong(FileSystemComponent::getSize).sum();
    }

    @Override
    public void display(int depth) {
        System.out.println("  ".repeat(depth) + "📁 " + name + "/ (" + getSize() + " bytes)");
        for (FileSystemComponent child : children) {
            child.display(depth + 1);
        }
    }
}

// ── Main ──────────────────────────────────────────────────
class CompositeDemo {
    public static void main(String[] args) {
        // Build tree
        Folder root = new Folder("root");

        Folder src = new Folder("src");
        src.add(new File("Main.java",     2048));
        src.add(new File("Utils.java",    1024));

        Folder test = new Folder("test");
        test.add(new File("MainTest.java", 512));

        Folder resources = new Folder("resources");
        resources.add(new File("config.yml",  256));
        resources.add(new File("schema.sql", 4096));

        root.add(src);
        root.add(test);
        root.add(resources);
        root.add(new File("pom.xml", 1500));

        // Display tree
        root.display(0);

        System.out.println("\nTotal project size: " + root.getSize() + " bytes");
        System.out.println("src folder size:    " + src.getSize() + " bytes");

        // Client treats File and Folder the same way
        System.out.println("\nAll component sizes:");
        List<FileSystemComponent> all = List.of(root, src, test, resources);
        for (FileSystemComponent c : all) {
            System.out.printf("  %-12s → %d bytes%n", c.getName(), c.getSize());
        }
    }
}
```

---

## 9. Decorator

### Intent
Attach additional responsibilities to an object **dynamically**. Decorators provide a flexible alternative to subclassing for extending functionality.

### Real-world analogy
Coffee shop: start with a plain coffee (Component), then wrap it in MilkDecorator, then SugarDecorator, then WhipDecorator. Each wrapper adds behavior.

### When to use
- To add behavior without modifying the original class
- When subclassing would lead to an explosion of classes

### UML Diagram
```
    Component «interface»
    ─────────────────────
    cost(): double
    description(): String
           ▲
    ┌──────┴──────────────┐
  PlainCoffee           Decorator (abstract)
  ───────────           ─────────────────────
  cost()                component: Component
  description()         cost() → component.cost()
                               ▲
                        ┌──────┴──────┐
                      Milk          Sugar
```

### Full Code

```java
// ── Component ─────────────────────────────────────────────
interface Coffee {
    double getCost();
    String getDescription();
}

// ── Concrete Component ────────────────────────────────────
class SimpleCoffee implements Coffee {
    @Override public double getCost()        { return 10.0; }
    @Override public String getDescription() { return "Simple Coffee"; }
}

class Espresso implements Coffee {
    @Override public double getCost()        { return 15.0; }
    @Override public String getDescription() { return "Espresso"; }
}

// ── Base Decorator ────────────────────────────────────────
abstract class CoffeeDecorator implements Coffee {
    protected final Coffee coffee;

    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }

    @Override public double getCost()        { return coffee.getCost(); }
    @Override public String getDescription() { return coffee.getDescription(); }
}

// ── Concrete Decorators ───────────────────────────────────
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) { super(coffee); }

    @Override public double getCost()        { return super.getCost() + 3.0; }
    @Override public String getDescription() { return super.getDescription() + ", Milk"; }
}

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) { super(coffee); }

    @Override public double getCost()        { return super.getCost() + 1.5; }
    @Override public String getDescription() { return super.getDescription() + ", Sugar"; }
}

class WhipDecorator extends CoffeeDecorator {
    public WhipDecorator(Coffee coffee) { super(coffee); }

    @Override public double getCost()        { return super.getCost() + 5.0; }
    @Override public String getDescription() { return super.getDescription() + ", Whip Cream"; }
}

class VanillaDecorator extends CoffeeDecorator {
    public VanillaDecorator(Coffee coffee) { super(coffee); }

    @Override public double getCost()        { return super.getCost() + 7.0; }
    @Override public String getDescription() { return super.getDescription() + ", Vanilla Syrup"; }
}

// ── Main ──────────────────────────────────────────────────
class DecoratorDemo {
    static void printOrder(Coffee coffee) {
        System.out.printf("  %-45s ₹%.1f%n", coffee.getDescription(), coffee.getCost());
    }

    public static void main(String[] args) {
        System.out.println("☕ Coffee Shop Orders:");
        System.out.println("─".repeat(55));

        // Plain coffee
        Coffee c1 = new SimpleCoffee();
        printOrder(c1);

        // Coffee + milk
        Coffee c2 = new MilkDecorator(new SimpleCoffee());
        printOrder(c2);

        // Coffee + milk + sugar + sugar (double sugar)
        Coffee c3 = new SugarDecorator(new SugarDecorator(new MilkDecorator(new SimpleCoffee())));
        printOrder(c3);

        // Espresso + whip + vanilla
        Coffee c4 = new VanillaDecorator(new WhipDecorator(new Espresso()));
        printOrder(c4);

        // The "kitchen sink" — all decorators
        Coffee c5 = new WhipDecorator(
                        new VanillaDecorator(
                            new SugarDecorator(
                                new MilkDecorator(
                                    new Espresso()))));
        printOrder(c5);
    }
}
```

**Output:**
```
☕ Coffee Shop Orders:
───────────────────────────────────────────────────────
  Simple Coffee                                  ₹10.0
  Simple Coffee, Milk                            ₹13.0
  Simple Coffee, Milk, Sugar, Sugar              ₹16.0
  Espresso, Whip Cream, Vanilla Syrup            ₹27.0
  Espresso, Milk, Sugar, Vanilla Syrup, Whip Cream ₹31.5
```

---

## 10. Facade

### Intent
Provide a **simplified interface** to a complex subsystem.

### Real-world analogy
When you order food on Swiggy, you click one button. Behind the scenes: payment gateway, restaurant system, delivery tracking, notifications — all handled. You only see the "Order" button (Facade).

### When to use
- To simplify a complex API
- To layer a subsystem (reduce coupling between layers)

### UML Diagram
```
   Client
     │
     ▼
  HomeTheaterFacade   ◄────────────────────────────
  ─────────────────   │         Subsystems         │
  watchMovie()  ──────┤  Amplifier    DVDPlayer     │
  endMovie()    ──────┤  Projector    Lights        │
                      │  PopcornMaker Screen        │
                      └─────────────────────────────
```

### Full Code

```java
// ── Subsystem classes ─────────────────────────────────────
class Amplifier {
    private int volume;
    public void on()              { System.out.println("Amplifier: ON"); }
    public void off()             { System.out.println("Amplifier: OFF"); }
    public void setVolume(int v)  { volume = v; System.out.println("Amplifier: volume → " + v); }
    public void setSurroundMode() { System.out.println("Amplifier: 5.1 Surround mode"); }
}

class DVDPlayer {
    private String currentMovie;
    public void on()              { System.out.println("DVD Player: ON"); }
    public void off()             { System.out.println("DVD Player: OFF"); }
    public void play(String movie){ currentMovie = movie; System.out.println("DVD Player: Playing '" + movie + "'"); }
    public void stop()            { System.out.println("DVD Player: Stopped"); }
    public void eject()           { System.out.println("DVD Player: Ejected '" + currentMovie + "'"); }
}

class Projector {
    public void on()              { System.out.println("Projector: ON"); }
    public void off()             { System.out.println("Projector: OFF"); }
    public void wideScreenMode()  { System.out.println("Projector: 16:9 widescreen mode"); }
}

class TheaterLights {
    private int dim;
    public void on()              { System.out.println("Theater Lights: ON (100%)"); }
    public void dim(int level)    { dim = level; System.out.println("Theater Lights: dimmed to " + level + "%"); }
    public void off()             { System.out.println("Theater Lights: OFF"); }
}

class PopcornMaker {
    public void on()              { System.out.println("Popcorn Maker: ON"); }
    public void pop()             { System.out.println("Popcorn Maker: Popping... 🍿"); }
    public void off()             { System.out.println("Popcorn Maker: OFF"); }
}

// ── Facade ────────────────────────────────────────────────
class HomeTheaterFacade {
    private final Amplifier    amp;
    private final DVDPlayer    dvd;
    private final Projector    projector;
    private final TheaterLights lights;
    private final PopcornMaker popcorn;

    public HomeTheaterFacade(Amplifier amp, DVDPlayer dvd,
                             Projector projector, TheaterLights lights,
                             PopcornMaker popcorn) {
        this.amp      = amp;
        this.dvd      = dvd;
        this.projector = projector;
        this.lights   = lights;
        this.popcorn  = popcorn;
    }

    // Complex workflow hidden behind ONE method
    public void watchMovie(String movie) {
        System.out.println("\n🎬 Get ready to watch: " + movie);
        System.out.println("─".repeat(40));
        popcorn.on();
        popcorn.pop();
        lights.dim(10);
        projector.on();
        projector.wideScreenMode();
        amp.on();
        amp.setSurroundMode();
        amp.setVolume(50);
        dvd.on();
        dvd.play(movie);
        System.out.println("─".repeat(40));
        System.out.println("✅ Movie started! Enjoy!\n");
    }

    public void endMovie() {
        System.out.println("\n⏹ Shutting down theater...");
        popcorn.off();
        lights.on();
        projector.off();
        amp.off();
        dvd.stop();
        dvd.eject();
        dvd.off();
        System.out.println("✅ Theater shut down.\n");
    }

    // ── Main ──────────────────────────────────────────────
    public static void main(String[] args) {
        Amplifier amp         = new Amplifier();
        DVDPlayer dvd         = new DVDPlayer();
        Projector projector   = new Projector();
        TheaterLights lights  = new TheaterLights();
        PopcornMaker popcorn  = new PopcornMaker();

        HomeTheaterFacade theater = new HomeTheaterFacade(amp, dvd, projector, lights, popcorn);

        theater.watchMovie("Interstellar");
        // ... watch movie ...
        theater.endMovie();
    }
}
```

---

## 11. Flyweight

### Intent
Use sharing to support a **large number of fine-grained objects** efficiently by storing common state externally.

### Real-world analogy
A forest with 1 million trees. Each tree has a type (Oak/Pine/Birch) — that's shared (intrinsic). Each tree has a position and age — that's unique (extrinsic).

### When to use
- When you have a huge number of objects consuming too much memory
- When most of the object state can be made external

### UML Diagram
```
  TreeFactory
  ──────────────────
  cache: Map<String, TreeType>
  getTreeType(name, color, texture) → TreeType (shared)

  TreeType (Flyweight — shared)    Tree (extrinsic state)
  ────────────────────────────     ──────────────────────
  name                             x, y
  color                            age
  texture                          treeType: TreeType ◄── shared ref
  draw(x, y, age)
```

### Full Code

```java
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// ── Flyweight — shared (intrinsic) state ──────────────────
class TreeType {
    private final String name;
    private final String color;
    private final String texture;

    public TreeType(String name, String color, String texture) {
        this.name    = name;
        this.color   = color;
        this.texture = texture;
        System.out.println("Creating TreeType: " + name); // prints only ONCE per type
    }

    public void draw(int x, int y, int age) {
        System.out.printf("  Drawing %s (color=%s) at (%d,%d) age=%d%n",
            name, color, x, y, age);
    }

    @Override public String toString() { return name; }
}

// ── Flyweight Factory ─────────────────────────────────────
class TreeFactory {
    private static final Map<String, TreeType> cache = new HashMap<>();

    public static TreeType getTreeType(String name, String color, String texture) {
        String key = name + color + texture;
        return cache.computeIfAbsent(key, k -> new TreeType(name, color, texture));
    }

    public static int getCacheSize() { return cache.size(); }
}

// ── Context — holds extrinsic state ───────────────────────
class Tree {
    private final int x, y, age;
    private final TreeType type; // shared reference

    public Tree(int x, int y, int age, TreeType type) {
        this.x = x; this.y = y; this.age = age;
        this.type = type;
    }

    public void draw() {
        type.draw(x, y, age);
    }
}

// ── Forest (client) ───────────────────────────────────────
class Forest {
    private final List<Tree> trees = new ArrayList<>();

    public void plantTree(int x, int y, int age, String name, String color, String texture) {
        TreeType type = TreeFactory.getTreeType(name, color, texture);
        trees.add(new Tree(x, y, age, type));
    }

    public void draw() {
        System.out.println("Drawing forest with " + trees.size() + " trees:");
        for (Tree tree : trees) tree.draw();
    }
}

// ── Main ──────────────────────────────────────────────────
class FlyweightDemo {
    public static void main(String[] args) {
        Forest forest = new Forest();

        System.out.println("=== Planting trees ===");
        // Plant 6 trees, but only 3 unique types
        forest.plantTree(10, 20, 5,  "Oak",   "Dark Green", "rough");
        forest.plantTree(30, 45, 12, "Pine",  "Light Green", "smooth");
        forest.plantTree(55, 10, 3,  "Birch", "White",       "papery");
        forest.plantTree(80, 90, 8,  "Oak",   "Dark Green", "rough");  // reused!
        forest.plantTree(15, 60, 20, "Pine",  "Light Green", "smooth"); // reused!
        forest.plantTree(70, 30, 1,  "Oak",   "Dark Green", "rough");  // reused!

        System.out.println("\n=== Rendering ===");
        forest.draw();

        System.out.println("\n=== Memory stats ===");
        System.out.println("Total trees: 6");
        System.out.println("Unique TreeType objects: " + TreeFactory.getCacheSize()); // only 3!
        System.out.println("Memory saved: 3 heavy objects vs 6");
    }
}
```

---

## 12. Proxy

### Intent
Provide a **surrogate or placeholder** for another object to control access to it.

### Real-world analogy
A bank cheque is a proxy for actual cash. A celebrity's manager is a proxy — you can't directly reach the celebrity.

### When to use (3 types)
- **Virtual Proxy**: Lazy initialization of heavy objects
- **Protection Proxy**: Access control
- **Remote Proxy**: Object in another address space (RMI)

### UML Diagram
```
   Client ──► Subject «interface»
              ─────────────────
              request()
                 ▲
          ┌──────┴──────┐
     RealSubject      Proxy
     ───────────       ──────────────────
     request()         realSubject: RealSubject
                       request() {
                         // access control / lazy init / logging
                         realSubject.request()
                       }
```

### Full Code

```java
// ── Subject interface ─────────────────────────────────────
interface Image {
    void display();
    String getFileName();
}

// ── Real Subject (heavy object) ───────────────────────────
class RealImage implements Image {
    private final String fileName;
    private byte[] imageData; // simulate heavy data

    public RealImage(String fileName) {
        this.fileName = fileName;
        loadFromDisk(); // expensive!
    }

    private void loadFromDisk() {
        System.out.println("💿 [Disk I/O] Loading image from disk: " + fileName);
        // Simulate expensive loading
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        imageData = new byte[1024 * 1024]; // simulate 1MB
        System.out.println("✅ Image loaded: " + fileName);
    }

    @Override
    public void display() {
        System.out.println("🖼 Displaying: " + fileName);
    }

    @Override
    public String getFileName() { return fileName; }
}

// ── Virtual Proxy (lazy loading) ──────────────────────────
class ImageProxy implements Image {
    private final String fileName;
    private RealImage realImage; // null until needed

    public ImageProxy(String fileName) {
        this.fileName = fileName;
        System.out.println("📋 Proxy created for: " + fileName + " (not loaded yet)");
    }

    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(fileName); // load only when first needed
        }
        realImage.display();
    }

    @Override
    public String getFileName() { return fileName; }
}

// ── Protection Proxy ──────────────────────────────────────
interface DatabaseService {
    String executeQuery(String query);
}

class RealDatabaseService implements DatabaseService {
    @Override
    public String executeQuery(String query) {
        return "Result of: " + query;
    }
}

class ProtectionProxy implements DatabaseService {
    private final RealDatabaseService service = new RealDatabaseService();
    private final String userRole;

    public ProtectionProxy(String userRole) {
        this.userRole = userRole;
    }

    @Override
    public String executeQuery(String query) {
        if (query.toLowerCase().startsWith("drop") && !userRole.equals("ADMIN")) {
            System.out.println("🚫 Access denied for role [" + userRole + "]: DROP is not allowed");
            return "ACCESS DENIED";
        }
        System.out.println("✅ [" + userRole + "] Query allowed: " + query);
        return service.executeQuery(query);
    }
}

// ── Main ──────────────────────────────────────────────────
class ProxyDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Virtual Proxy (Lazy Loading) ===");
        Image[] gallery = {
            new ImageProxy("photo1.jpg"),
            new ImageProxy("photo2.jpg"),
            new ImageProxy("photo3.jpg")
        };

        System.out.println("\nUser scrolls to image 2:");
        gallery[1].display(); // loads now
        System.out.println("\nUser views image 2 again:");
        gallery[1].display(); // already loaded — no disk I/O!

        System.out.println("\n=== Protection Proxy ===");
        DatabaseService adminDb = new ProtectionProxy("ADMIN");
        DatabaseService userDb  = new ProtectionProxy("USER");

        adminDb.executeQuery("SELECT * FROM users");
        adminDb.executeQuery("DROP TABLE logs");

        userDb.executeQuery("SELECT * FROM products");
        userDb.executeQuery("DROP TABLE orders"); // blocked!
    }
}
```

---

# 🎭 BEHAVIORAL PATTERNS

---

## 13. Chain of Responsibility

### Intent
Pass a request along a **chain of handlers**. Each handler decides to process or pass to next.

### Real-world analogy
Customer support: Bot → Agent → Manager → Director. Each level handles what they can, escalates the rest.

### UML Diagram
```
   Handler «abstract»
   ──────────────────
   next: Handler
   setNext(Handler)
   handle(request): void
         ▲
   ┌─────┼──────┐
 BotHandler  AgentHandler  ManagerHandler
```

### Full Code

```java
// ── Abstract Handler ──────────────────────────────────────
abstract class SupportHandler {
    protected SupportHandler next;

    public SupportHandler setNext(SupportHandler next) {
        this.next = next;
        return next; // fluent chaining
    }

    public abstract void handle(int level, String issue);
}

// ── Concrete Handlers ─────────────────────────────────────
class BotHandler extends SupportHandler {
    @Override
    public void handle(int level, String issue) {
        if (level <= 1) {
            System.out.println("🤖 Bot resolved: \"" + issue + "\"");
        } else {
            System.out.println("🤖 Bot: Above my pay grade, escalating...");
            if (next != null) next.handle(level, issue);
        }
    }
}

class AgentHandler extends SupportHandler {
    @Override
    public void handle(int level, String issue) {
        if (level <= 2) {
            System.out.println("👷 Agent resolved: \"" + issue + "\"");
        } else {
            System.out.println("👷 Agent: This needs a manager, escalating...");
            if (next != null) next.handle(level, issue);
        }
    }
}

class ManagerHandler extends SupportHandler {
    @Override
    public void handle(int level, String issue) {
        if (level <= 3) {
            System.out.println("👔 Manager resolved: \"" + issue + "\"");
        } else {
            System.out.println("👔 Manager: Escalating to Director...");
            if (next != null) next.handle(level, issue);
        }
    }
}

class DirectorHandler extends SupportHandler {
    @Override
    public void handle(int level, String issue) {
        System.out.println("🎯 Director resolved: \"" + issue + "\" (final escalation)");
    }
}

// ── Main ──────────────────────────────────────────────────
class ChainDemo {
    public static void main(String[] args) {
        // Build the chain
        BotHandler bot = new BotHandler();
        bot.setNext(new AgentHandler())
           .setNext(new ManagerHandler())
           .setNext(new DirectorHandler());

        System.out.println("=== Issue 1 (level 1) ===");
        bot.handle(1, "How do I reset my password?");

        System.out.println("\n=== Issue 2 (level 2) ===");
        bot.handle(2, "My order is delayed by 3 days");

        System.out.println("\n=== Issue 3 (level 3) ===");
        bot.handle(3, "I was charged twice for my subscription");

        System.out.println("\n=== Issue 4 (level 4) ===");
        bot.handle(4, "I want to sue the company");
    }
}
```

---

## 14. Command

### Intent
Encapsulate a request as an object, allowing **undo/redo**, queuing, and logging.

### Real-world analogy
A restaurant order slip is a command — the waiter (invoker) doesn't cook (receiver), just passes the slip. You can cancel the order (undo).

### UML Diagram
```
   Invoker                    Command «interface»
   ────────                   ─────────────────
   history: Stack             execute()
   execute(cmd)               undo()
   undo()                          ▲
                               ┌───┴──────┐
                           TurnOnCmd   TurnOffCmd
                               │              │
                               ▼              ▼
                              Light (Receiver)
```

### Full Code

```java
import java.util.Stack;

// ── Command interface ─────────────────────────────────────
interface Command {
    void execute();
    void undo();
}

// ── Receiver ──────────────────────────────────────────────
class SmartLight {
    private boolean on = false;
    private int     brightness = 100;
    private String  color = "white";
    private final String name;

    public SmartLight(String name) { this.name = name; }

    public void turnOn()  { on = true;  System.out.println(name + ": 💡 ON  (brightness=" + brightness + "%, color=" + color + ")"); }
    public void turnOff() { on = false; System.out.println(name + ": 🔦 OFF"); }
    public void setBrightness(int b) { brightness = b; System.out.println(name + ": brightness → " + b + "%"); }
    public void setColor(String c)   { color = c;      System.out.println(name + ": color → " + c); }

    public boolean isOn() { return on; }
    public int getBrightness() { return brightness; }
    public String getColor() { return color; }
}

// ── Concrete Commands ─────────────────────────────────────
class TurnOnCommand implements Command {
    private final SmartLight light;
    public TurnOnCommand(SmartLight light) { this.light = light; }

    @Override public void execute() { light.turnOn(); }
    @Override public void undo()    { light.turnOff(); }
}

class TurnOffCommand implements Command {
    private final SmartLight light;
    public TurnOffCommand(SmartLight light) { this.light = light; }

    @Override public void execute() { light.turnOff(); }
    @Override public void undo()    { light.turnOn(); }
}

class ChangeBrightnessCommand implements Command {
    private final SmartLight light;
    private final int newBrightness;
    private int previousBrightness;

    public ChangeBrightnessCommand(SmartLight light, int brightness) {
        this.light = light;
        this.newBrightness = brightness;
    }

    @Override
    public void execute() {
        previousBrightness = light.getBrightness();
        light.setBrightness(newBrightness);
    }

    @Override
    public void undo() {
        light.setBrightness(previousBrightness);
    }
}

// ── Macro Command (composite) ─────────────────────────────
class MovieModeCommand implements Command {
    private final SmartLight[] lights;

    public MovieModeCommand(SmartLight... lights) { this.lights = lights; }

    @Override
    public void execute() {
        System.out.println("🎬 Movie Mode: dimming all lights");
        for (SmartLight l : lights) {
            l.setBrightness(20);
            l.setColor("amber");
        }
    }

    @Override
    public void undo() {
        System.out.println("↩ Undoing Movie Mode");
        for (SmartLight l : lights) {
            l.setBrightness(100);
            l.setColor("white");
        }
    }
}

// ── Invoker ───────────────────────────────────────────────
class RemoteControl {
    private final Stack<Command> history = new Stack<>();

    public void execute(Command cmd) {
        cmd.execute();
        history.push(cmd);
    }

    public void undo() {
        if (!history.isEmpty()) {
            System.out.println("↩ Undoing last command...");
            history.pop().undo();
        } else {
            System.out.println("Nothing to undo");
        }
    }
}

// ── Main ──────────────────────────────────────────────────
class CommandDemo {
    public static void main(String[] args) {
        SmartLight living = new SmartLight("Living Room");
        SmartLight bedroom = new SmartLight("Bedroom");

        RemoteControl remote = new RemoteControl();

        remote.execute(new TurnOnCommand(living));
        remote.execute(new ChangeBrightnessCommand(living, 60));
        remote.execute(new TurnOnCommand(bedroom));
        remote.execute(new MovieModeCommand(living, bedroom));

        System.out.println("\n--- Undo 3 times ---");
        remote.undo(); // undo movie mode
        remote.undo(); // undo bedroom turn on
        remote.undo(); // undo brightness change
    }
}
```

---

## 15. Iterator

### Intent
Provide a way to **sequentially access elements** of a collection without exposing its underlying structure.

### Real-world analogy
A TV remote's channel-up button iterates through channels. You don't care if channels are stored in an array or linked list.

### Full Code

```java
import java.util.NoSuchElementException;

// ── Iterator interface ────────────────────────────────────
interface Iterator<T> {
    boolean hasNext();
    T next();
}

// ── Aggregate interface ───────────────────────────────────
interface SongCollection {
    Iterator<String> createIterator();
    void addSong(String song);
}

// ── Concrete Aggregate: Playlist (array-backed) ───────────
class Playlist implements SongCollection {
    private String[] songs;
    private int count = 0;

    public Playlist(int capacity) {
        songs = new String[capacity];
    }

    @Override
    public void addSong(String song) {
        if (count < songs.length) songs[count++] = song;
    }

    @Override
    public Iterator<String> createIterator() {
        return new PlaylistIterator();
    }

    // ── Inner Iterator ────────────────────────────────────
    private class PlaylistIterator implements Iterator<String> {
        private int position = 0;

        @Override
        public boolean hasNext() { return position < count; }

        @Override
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            return songs[position++];
        }
    }
}

// ── Another Aggregate: RadioStation (linked list) ─────────
class RadioStation implements SongCollection {
    private static class Node {
        String song;
        Node next;
        Node(String s) { song = s; }
    }

    private Node head;
    private int size = 0;

    @Override
    public void addSong(String song) {
        Node n = new Node(song);
        if (head == null) { head = n; return; }
        Node cur = head;
        while (cur.next != null) cur = cur.next;
        cur.next = n;
        size++;
    }

    @Override
    public Iterator<String> createIterator() {
        return new RadioIterator(head);
    }

    private static class RadioIterator implements Iterator<String> {
        private Node current;
        RadioIterator(Node head) { current = head; }

        @Override public boolean hasNext() { return current != null; }

        @Override
        public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            String song = current.song;
            current = current.next;
            return song;
        }
    }
}

// ── Client ────────────────────────────────────────────────
class IteratorDemo {
    static void playSongs(String title, SongCollection collection) {
        System.out.println("\n🎵 " + title + ":");
        Iterator<String> it = collection.createIterator();
        int i = 1;
        while (it.hasNext()) {
            System.out.println("  " + i++ + ". " + it.next());
        }
    }

    public static void main(String[] args) {
        Playlist playlist = new Playlist(10);
        playlist.addSong("Bohemian Rhapsody");
        playlist.addSong("Hotel California");
        playlist.addSong("Stairway to Heaven");

        RadioStation radio = new RadioStation();
        radio.addSong("Blinding Lights");
        radio.addSong("Shape of You");
        radio.addSong("Uptown Funk");

        // Same client code works with both collections!
        playSongs("My Playlist (Array)", playlist);
        playSongs("Radio Station (LinkedList)", radio);
    }
}
```

---

## 16. Mediator

### Intent
Define an object that **encapsulates how objects interact**. Reduces direct references (many-to-many → many-to-one).

### Real-world analogy
Air traffic control tower. Planes don't talk to each other directly — they all communicate through the tower (Mediator).

### Full Code

```java
import java.util.ArrayList;
import java.util.List;

// ── Mediator ──────────────────────────────────────────────
interface ChatMediator {
    void sendMessage(String message, User sender);
    void addUser(User user);
}

// ── Colleague ─────────────────────────────────────────────
abstract class User {
    protected final ChatMediator mediator;
    protected final String name;

    public User(ChatMediator mediator, String name) {
        this.mediator = mediator;
        this.name = name;
    }

    public String getName() { return name; }

    public abstract void send(String message);
    public abstract void receive(String message, String from);
}

// ── Concrete Mediator ─────────────────────────────────────
class ChatRoom implements ChatMediator {
    private final List<User> users = new ArrayList<>();

    @Override
    public void addUser(User user) {
        users.add(user);
        System.out.println("  [" + user.getName() + " joined the chat]");
    }

    @Override
    public void sendMessage(String message, User sender) {
        for (User user : users) {
            if (user != sender) { // don't send to yourself
                user.receive(message, sender.getName());
            }
        }
    }
}

// ── Concrete Colleagues ───────────────────────────────────
class ChatUser extends User {
    public ChatUser(ChatMediator mediator, String name) {
        super(mediator, name);
    }

    @Override
    public void send(String message) {
        System.out.println("[" + name + "] → " + message);
        mediator.sendMessage(message, this);
    }

    @Override
    public void receive(String message, String from) {
        System.out.println("  [" + name + " received from " + from + "]: " + message);
    }
}

// ── Main ──────────────────────────────────────────────────
class MediatorDemo {
    public static void main(String[] args) {
        ChatMediator room = new ChatRoom();

        User alice = new ChatUser(room, "Alice");
        User bob   = new ChatUser(room, "Bob");
        User carol = new ChatUser(room, "Carol");
        User dave  = new ChatUser(room, "Dave");

        room.addUser(alice);
        room.addUser(bob);
        room.addUser(carol);
        room.addUser(dave);

        System.out.println();
        alice.send("Hey everyone!");
        System.out.println();
        bob.send("Hi Alice!");
        System.out.println();
        carol.send("What's the plan for today?");
    }
}
```

---

## 17. Memento

### Intent
Capture and restore an object's **internal state** without violating encapsulation.

### Real-world analogy
Ctrl+Z (undo) in any editor. The editor saves its state before each change.

### Full Code

```java
import java.util.Stack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ── Memento ───────────────────────────────────────────────
class EditorMemento {
    private final String content;
    private final int    cursorPosition;
    private final String timestamp;

    public EditorMemento(String content, int cursorPosition) {
        this.content        = content;
        this.cursorPosition = cursorPosition;
        this.timestamp      = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Only the Originator should access these
    String getContent()        { return content; }
    int    getCursorPosition() { return cursorPosition; }
    String getTimestamp()      { return timestamp; }
}

// ── Originator ────────────────────────────────────────────
class TextEditor {
    private StringBuilder content = new StringBuilder();
    private int cursorPosition = 0;

    public void type(String text) {
        content.insert(cursorPosition, text);
        cursorPosition += text.length();
    }

    public void delete(int chars) {
        int from = Math.max(0, cursorPosition - chars);
        content.delete(from, cursorPosition);
        cursorPosition = from;
    }

    // Save state to Memento
    public EditorMemento save() {
        System.out.println("💾 Saved state: \"" + content + "\"");
        return new EditorMemento(content.toString(), cursorPosition);
    }

    // Restore from Memento
    public void restore(EditorMemento memento) {
        content = new StringBuilder(memento.getContent());
        cursorPosition = memento.getCursorPosition();
        System.out.println("↩ Restored [" + memento.getTimestamp() + "]: \"" + content + "\"");
    }

    public String getContent() { return content.toString(); }

    public void showState() {
        System.out.println("📝 Editor: \"" + content + "\" (cursor=" + cursorPosition + ")");
    }
}

// ── Caretaker ─────────────────────────────────────────────
class History {
    private final Stack<EditorMemento> undoStack = new Stack<>();
    private final Stack<EditorMemento> redoStack = new Stack<>();
    private final TextEditor editor;

    public History(TextEditor editor) { this.editor = editor; }

    public void save() {
        undoStack.push(editor.save());
        redoStack.clear(); // new action clears redo history
    }

    public void undo() {
        if (undoStack.isEmpty()) { System.out.println("Nothing to undo"); return; }
        redoStack.push(editor.save());
        editor.restore(undoStack.pop());
    }

    public void redo() {
        if (redoStack.isEmpty()) { System.out.println("Nothing to redo"); return; }
        undoStack.push(editor.save());
        editor.restore(redoStack.pop());
    }
}

// ── Main ──────────────────────────────────────────────────
class MementoDemo {
    public static void main(String[] args) throws InterruptedException {
        TextEditor editor  = new TextEditor();
        History    history = new History(editor);

        history.save(); // save empty state

        editor.type("Hello");
        history.save();
        editor.showState();

        editor.type(", World");
        history.save();
        editor.showState();

        editor.type("!!!");
        history.save();
        editor.showState();

        System.out.println("\n--- Undo ---");
        history.undo();
        editor.showState();

        history.undo();
        editor.showState();

        System.out.println("\n--- Redo ---");
        history.redo();
        editor.showState();
    }
}
```

---

## 18. Observer

### Intent
Define a **one-to-many dependency** so that when one object changes state, all its dependents are notified automatically.

### Real-world analogy
YouTube subscriptions. When you (Subject) upload a video, all subscribers (Observers) get notified.

### UML Diagram
```
  Subject «interface»            Observer «interface»
  ───────────────────            ────────────────────
  subscribe(Observer)            update(event)
  unsubscribe(Observer)
  notify()
       ▲
  WeatherStation ─────────────► EmailAlert
                                MobileAlert
                                DisplayBoard
```

### Full Code

```java
import java.util.ArrayList;
import java.util.List;

// ── Observer interface ────────────────────────────────────
interface WeatherObserver {
    void update(float temperature, float humidity, float pressure);
}

// ── Subject interface ─────────────────────────────────────
interface WeatherSubject {
    void subscribe(WeatherObserver observer);
    void unsubscribe(WeatherObserver observer);
    void notifyObservers();
}

// ── Concrete Subject ──────────────────────────────────────
class WeatherStation implements WeatherSubject {
    private final List<WeatherObserver> observers = new ArrayList<>();
    private float temperature, humidity, pressure;

    @Override
    public void subscribe(WeatherObserver o)   { observers.add(o);    System.out.println("+ Subscribed: " + o.getClass().getSimpleName()); }
    @Override
    public void unsubscribe(WeatherObserver o) { observers.remove(o); System.out.println("- Unsubscribed: " + o.getClass().getSimpleName()); }

    @Override
    public void notifyObservers() {
        System.out.println("\n📡 Notifying " + observers.size() + " observers...");
        for (WeatherObserver o : observers) {
            o.update(temperature, humidity, pressure);
        }
    }

    public void setMeasurements(float temp, float humidity, float pressure) {
        System.out.println("\n🌡 Weather station: new reading");
        this.temperature = temp;
        this.humidity    = humidity;
        this.pressure    = pressure;
        notifyObservers();
    }
}

// ── Concrete Observers ────────────────────────────────────
class CurrentConditionsDisplay implements WeatherObserver {
    @Override
    public void update(float temp, float humidity, float pressure) {
        System.out.printf("  📺 Display: %.1f°C | %.0f%% humidity%n", temp, humidity);
    }
}

class HeatIndexDisplay implements WeatherObserver {
    @Override
    public void update(float temp, float humidity, float pressure) {
        // Simple heat index formula
        float heatIndex = computeHeatIndex(temp, humidity);
        System.out.printf("  🌡 Heat Index: %.1f°C (feels like)%n", heatIndex);
    }

    private float computeHeatIndex(float t, float h) {
        return (float)(t - (0.55 - 0.0055 * h) * (t - 14.5));
    }
}

class StormAlert implements WeatherObserver {
    @Override
    public void update(float temp, float humidity, float pressure) {
        if (pressure < 1000) {
            System.out.println("  ⚠️  STORM ALERT! Low pressure: " + pressure + " hPa");
        } else {
            System.out.println("  ✅ Storm Alert: All clear (" + pressure + " hPa)");
        }
    }
}

class WeatherLogger implements WeatherObserver {
    private int count = 0;
    @Override
    public void update(float temp, float humidity, float pressure) {
        System.out.printf("  📝 Log #%d: temp=%.1f, humidity=%.1f, pressure=%.1f%n",
            ++count, temp, humidity, pressure);
    }
}

// ── Main ──────────────────────────────────────────────────
class ObserverDemo {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();

        WeatherObserver display   = new CurrentConditionsDisplay();
        WeatherObserver heatIndex = new HeatIndexDisplay();
        WeatherObserver alert     = new StormAlert();
        WeatherObserver logger    = new WeatherLogger();

        station.subscribe(display);
        station.subscribe(heatIndex);
        station.subscribe(alert);
        station.subscribe(logger);

        station.setMeasurements(25.5f, 65f, 1013f);
        station.setMeasurements(30.0f, 80f, 995f); // low pressure!

        System.out.println("\n--- Unsubscribing alert ---");
        station.unsubscribe(alert);
        station.setMeasurements(22.0f, 70f, 980f); // alert won't fire
    }
}
```

---

## 19. State

### Intent
Allow an object to **alter its behavior** when its internal state changes. The object appears to change its class.

### Real-world analogy
A traffic light: Green→Yellow→Red. Each state has different behavior on the same `tick()` event.

### Full Code

```java
// ── State interface ───────────────────────────────────────
interface VendingMachineState {
    void insertCoin(VendingMachine machine);
    void selectProduct(VendingMachine machine, String product);
    void dispense(VendingMachine machine);
    void returnCoin(VendingMachine machine);
    String getStateName();
}

// ── Context ───────────────────────────────────────────────
class VendingMachine {
    // States
    public final VendingMachineState IDLE;
    public final VendingMachineState HAS_COIN;
    public final VendingMachineState DISPENSING;
    public final VendingMachineState OUT_OF_STOCK;

    private VendingMachineState currentState;
    private String selectedProduct;
    private int stock;

    public VendingMachine(int initialStock) {
        IDLE        = new IdleState();
        HAS_COIN    = new HasCoinState();
        DISPENSING  = new DispensingState();
        OUT_OF_STOCK = new OutOfStockState();

        this.stock = initialStock;
        this.currentState = (stock > 0) ? IDLE : OUT_OF_STOCK;
    }

    public void setState(VendingMachineState state) {
        System.out.println("  [State: " + currentState.getStateName() + " → " + state.getStateName() + "]");
        currentState = state;
    }

    public void insertCoin()                    { currentState.insertCoin(this); }
    public void selectProduct(String product)   { currentState.selectProduct(this, product); }
    public void dispense()                      { currentState.dispense(this); }
    public void returnCoin()                    { currentState.returnCoin(this); }

    public void setSelectedProduct(String p)    { selectedProduct = p; }
    public String getSelectedProduct()          { return selectedProduct; }
    public int getStock()                       { return stock; }
    public void decrementStock()                { stock--; }
}

// ── Concrete States ───────────────────────────────────────
class IdleState implements VendingMachineState {
    @Override public String getStateName() { return "IDLE"; }

    @Override public void insertCoin(VendingMachine m) {
        System.out.println("  💰 Coin accepted!");
        m.setState(m.HAS_COIN);
    }
    @Override public void selectProduct(VendingMachine m, String p) { System.out.println("  ❌ Please insert coin first"); }
    @Override public void dispense(VendingMachine m)                { System.out.println("  ❌ No coin inserted"); }
    @Override public void returnCoin(VendingMachine m)              { System.out.println("  ❌ No coin to return"); }
}

class HasCoinState implements VendingMachineState {
    @Override public String getStateName() { return "HAS_COIN"; }

    @Override public void insertCoin(VendingMachine m) { System.out.println("  ❌ Coin already inserted"); }
    @Override public void selectProduct(VendingMachine m, String p) {
        System.out.println("  🛒 Selected: " + p);
        m.setSelectedProduct(p);
        m.setState(m.DISPENSING);
        m.dispense(); // trigger dispense immediately
    }
    @Override public void dispense(VendingMachine m) { System.out.println("  ❌ Please select a product first"); }
    @Override public void returnCoin(VendingMachine m) {
        System.out.println("  💰 Coin returned");
        m.setState(m.IDLE);
    }
}

class DispensingState implements VendingMachineState {
    @Override public String getStateName() { return "DISPENSING"; }

    @Override public void insertCoin(VendingMachine m)              { System.out.println("  ❌ Wait, dispensing..."); }
    @Override public void selectProduct(VendingMachine m, String p) { System.out.println("  ❌ Wait, dispensing..."); }
    @Override public void returnCoin(VendingMachine m)              { System.out.println("  ❌ Cannot return during dispense"); }
    @Override public void dispense(VendingMachine m) {
        System.out.println("  ✅ Dispensing: " + m.getSelectedProduct() + " 🥤");
        m.decrementStock();
        m.setState(m.getStock() > 0 ? m.IDLE : m.OUT_OF_STOCK);
    }
}

class OutOfStockState implements VendingMachineState {
    @Override public String getStateName() { return "OUT_OF_STOCK"; }

    @Override public void insertCoin(VendingMachine m)              { System.out.println("  ❌ Out of stock! Coin returned."); }
    @Override public void selectProduct(VendingMachine m, String p) { System.out.println("  ❌ Out of stock!"); }
    @Override public void dispense(VendingMachine m)                { System.out.println("  ❌ Out of stock!"); }
    @Override public void returnCoin(VendingMachine m)              { System.out.println("  ❌ No coin to return"); }
}

// ── Main ──────────────────────────────────────────────────
class StateDemo {
    public static void main(String[] args) {
        VendingMachine machine = new VendingMachine(2); // 2 items in stock

        System.out.println("=== Transaction 1 ===");
        machine.selectProduct("Coke");   // no coin
        machine.insertCoin();
        machine.selectProduct("Coke");   // dispenses!

        System.out.println("\n=== Transaction 2 ===");
        machine.insertCoin();
        machine.returnCoin();            // change mind

        System.out.println("\n=== Transaction 3 (last item) ===");
        machine.insertCoin();
        machine.selectProduct("Pepsi");  // last one, goes to OUT_OF_STOCK

        System.out.println("\n=== Transaction 4 (out of stock) ===");
        machine.insertCoin();            // returns coin
    }
}
```

---

## 20. Strategy

### Intent
Define a **family of algorithms**, encapsulate each one, and make them interchangeable. Strategy lets the algorithm vary independently from clients that use it.

### Real-world analogy
Google Maps: you choose "route strategy" — Drive, Walk, or Cycle. Same destination, different algorithm.

### UML Diagram
```
  Context (Navigator)
  ──────────────────────────
  strategy: RouteStrategy
  setStrategy(RouteStrategy)
  buildRoute(from, to) ─────► «interface» RouteStrategy
                               ────────────────────────
                               buildRoute(from, to)
                                     ▲
                              ┌──────┼──────┐
                           CarStrategy  WalkStrategy  CycleStrategy
```

### Full Code

```java
import java.util.Arrays;
import java.util.List;

// ── Strategy interface ────────────────────────────────────
interface SortStrategy {
    void sort(int[] array);
    String getName();
}

// ── Concrete Strategies ───────────────────────────────────
class BubbleSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j]; arr[j] = arr[j+1]; arr[j+1] = tmp;
                }
    }
    @Override public String getName() { return "Bubble Sort O(n²)"; }
}

class MergeSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] arr) {
        mergeSort(arr, 0, arr.length - 1);
    }

    private void mergeSort(int[] arr, int l, int r) {
        if (l >= r) return;
        int mid = (l + r) / 2;
        mergeSort(arr, l, mid);
        mergeSort(arr, mid + 1, r);
        merge(arr, l, mid, r);
    }

    private void merge(int[] arr, int l, int mid, int r) {
        int[] left  = Arrays.copyOfRange(arr, l, mid + 1);
        int[] right = Arrays.copyOfRange(arr, mid + 1, r + 1);
        int i = 0, j = 0, k = l;
        while (i < left.length && j < right.length)
            arr[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
        while (i < left.length)  arr[k++] = left[i++];
        while (j < right.length) arr[k++] = right[j++];
    }

    @Override public String getName() { return "Merge Sort O(n log n)"; }
}

class JavaBuiltinSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] arr) { Arrays.sort(arr); }
    @Override public String getName() { return "Java Arrays.sort (Tim Sort)"; }
}

// ── Context ───────────────────────────────────────────────
class Sorter {
    private SortStrategy strategy;

    public Sorter(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SortStrategy strategy) {
        System.out.println("Switching strategy to: " + strategy.getName());
        this.strategy = strategy;
    }

    public int[] sort(int[] array) {
        int[] copy = Arrays.copyOf(array, array.length);
        long start = System.nanoTime();
        strategy.sort(copy);
        long elapsed = System.nanoTime() - start;
        System.out.printf("  [%s] sorted in %d ns → %s%n",
            strategy.getName(), elapsed, Arrays.toString(copy));
        return copy;
    }
}

// ── Main ──────────────────────────────────────────────────
class StrategyDemo {
    public static void main(String[] args) {
        int[] data = {64, 34, 25, 12, 22, 11, 90, 42, 7};

        System.out.println("Original: " + Arrays.toString(data));
        System.out.println();

        Sorter sorter = new Sorter(new BubbleSortStrategy());
        sorter.sort(data);

        sorter.setStrategy(new MergeSortStrategy());
        sorter.sort(data);

        sorter.setStrategy(new JavaBuiltinSortStrategy());
        sorter.sort(data);

        // Real-world: pick strategy based on data size
        System.out.println("\n--- Smart strategy selection ---");
        int[] small = {5, 2, 8, 1};
        int[] large = new int[10000];
        for (int i = 0; i < large.length; i++) large[i] = (int)(Math.random() * 10000);

        Sorter smartSorter = new Sorter(chooseStrategy(small.length));
        smartSorter.sort(small);

        smartSorter.setStrategy(chooseStrategy(large.length));
        smartSorter.sort(large);
    }

    static SortStrategy chooseStrategy(int size) {
        if (size < 10)    return new BubbleSortStrategy();
        else              return new JavaBuiltinSortStrategy();
    }
}
```

---

## 21. Template Method

### Intent
Define the **skeleton of an algorithm** in a base class, deferring some steps to subclasses.

### Real-world analogy
Making tea vs coffee — same skeleton: boil water → brew → pour → add condiments. Steps 2 and 4 differ.

### UML Diagram
```
  DataProcessor (abstract)
  ─────────────────────────────────────
  process() {         ← template method (final)
    readData()        ← abstract
    transformData()   ← abstract
    saveData()        ← concrete (with hook)
    onComplete()      ← hook (optional override)
  }
       ▲
  ┌────┴────┐
CsvProcessor  JsonProcessor
```

### Full Code

```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ── Abstract Class with Template Method ───────────────────
abstract class DataProcessor {
    // THE template method — final so subclasses can't break the skeleton
    public final void process() {
        System.out.println("\n▶ Starting: " + getClass().getSimpleName());
        List<String> rawData     = readData();
        List<String> transformed = transformData(rawData);
        saveData(transformed);
        onComplete(); // hook
        System.out.println("✅ Done: " + getClass().getSimpleName());
    }

    // Abstract steps — subclasses MUST implement
    protected abstract List<String> readData();
    protected abstract List<String> transformData(List<String> data);

    // Concrete step — same for everyone
    protected void saveData(List<String> data) {
        System.out.println("  💾 Saving " + data.size() + " records to DB: " + data);
    }

    // Hook — subclasses CAN override (but don't have to)
    protected void onComplete() {
        // default: do nothing
    }
}

// ── Concrete Class 1: CSV Processor ───────────────────────
class CsvDataProcessor extends DataProcessor {
    @Override
    protected List<String> readData() {
        System.out.println("  📂 Reading CSV file...");
        // Simulate reading CSV
        return Arrays.asList("alice,25,engineer", "bob,30,manager", "carol,28,designer");
    }

    @Override
    protected List<String> transformData(List<String> data) {
        System.out.println("  🔄 Transforming CSV rows...");
        return data.stream()
            .map(row -> {
                String[] parts = row.split(",");
                return "User{name=" + parts[0] + ", age=" + parts[1] + ", role=" + parts[2] + "}";
            })
            .collect(Collectors.toList());
    }

    @Override
    protected void onComplete() {
        System.out.println("  📧 Sending CSV import completion email...");
    }
}

// ── Concrete Class 2: JSON Processor ──────────────────────
class JsonDataProcessor extends DataProcessor {
    @Override
    protected List<String> readData() {
        System.out.println("  📂 Fetching JSON from API...");
        return Arrays.asList(
            "{\"id\":1,\"product\":\"Laptop\",\"price\":999}",
            "{\"id\":2,\"product\":\"Phone\",\"price\":499}"
        );
    }

    @Override
    protected List<String> transformData(List<String> data) {
        System.out.println("  🔄 Parsing JSON and normalizing prices...");
        // Simulate JSON parsing (simplified)
        return data.stream()
            .map(json -> json.replace("{", "").replace("}", "")
                             .replace("\"", "").replace(":", "="))
            .collect(Collectors.toList());
    }
    // No onComplete override — default hook (do nothing) is fine
}

// ── Concrete Class 3: XML Processor with validation ───────
class XmlDataProcessor extends DataProcessor {
    @Override
    protected List<String> readData() {
        System.out.println("  📂 Parsing XML document...");
        return Arrays.asList("<item>Apple</item>", "<item>Banana</item>", "<item>Cherry</item>");
    }

    @Override
    protected List<String> transformData(List<String> data) {
        System.out.println("  🔄 Stripping XML tags...");
        return data.stream()
            .map(xml -> xml.replaceAll("<[^>]+>", "").trim().toUpperCase())
            .collect(Collectors.toList());
    }

    @Override
    protected void onComplete() {
        System.out.println("  📋 Logging XML audit trail...");
    }
}

// ── Main ──────────────────────────────────────────────────
class TemplateMethodDemo {
    public static void main(String[] args) {
        List<DataProcessor> processors = Arrays.asList(
            new CsvDataProcessor(),
            new JsonDataProcessor(),
            new XmlDataProcessor()
        );

        // Same process() call, different behavior — Open/Closed Principle!
        for (DataProcessor p : processors) {
            p.process();
        }
    }
}
```

---

## 22. Visitor

### Intent
Add **new operations** to objects without modifying their classes. Separates algorithm from the object structure.

### Real-world analogy
A tax inspector (Visitor) visits different types of businesses (Elements) — a Restaurant, Hotel, Factory — and applies different tax rules to each.

### Full Code

```java
// ── Visitor interface ─────────────────────────────────────
interface ShapeVisitor {
    void visit(CircleShape circle);
    void visit(RectangleShape rectangle);
    void visit(TriangleShape triangle);
}

// ── Element interface ─────────────────────────────────────
interface Shape {
    void accept(ShapeVisitor visitor);
}

// ── Concrete Elements ─────────────────────────────────────
class CircleShape implements Shape {
    public final double radius;
    public CircleShape(double r) { this.radius = r; }

    @Override public void accept(ShapeVisitor v) { v.visit(this); }
}

class RectangleShape implements Shape {
    public final double width, height;
    public RectangleShape(double w, double h) { width = w; height = h; }

    @Override public void accept(ShapeVisitor v) { v.visit(this); }
}

class TriangleShape implements Shape {
    public final double base, height;
    public TriangleShape(double b, double h) { base = b; height = h; }

    @Override public void accept(ShapeVisitor v) { v.visit(this); }
}

// ── Concrete Visitors ─────────────────────────────────────
class AreaCalculator implements ShapeVisitor {
    private double totalArea = 0;

    @Override public void visit(CircleShape c)    { double a = Math.PI * c.radius * c.radius; totalArea += a; System.out.printf("  Circle area    : %.2f%n", a); }
    @Override public void visit(RectangleShape r) { double a = r.width * r.height;            totalArea += a; System.out.printf("  Rectangle area : %.2f%n", a); }
    @Override public void visit(TriangleShape t)  { double a = 0.5 * t.base * t.height;       totalArea += a; System.out.printf("  Triangle area  : %.2f%n", a); }

    public double getTotalArea() { return totalArea; }
}

class PerimeterCalculator implements ShapeVisitor {
    @Override public void visit(CircleShape c)    { System.out.printf("  Circle perimeter    : %.2f%n", 2 * Math.PI * c.radius); }
    @Override public void visit(RectangleShape r) { System.out.printf("  Rectangle perimeter : %.2f%n", 2 * (r.width + r.height)); }
    @Override public void visit(TriangleShape t)  { System.out.printf("  Triangle perimeter  : %.2f (approx base+height sides)%n", t.base + 2 * Math.sqrt(Math.pow(t.base/2,2) + Math.pow(t.height,2))); }
}

class XmlExporter implements ShapeVisitor {
    private final StringBuilder xml = new StringBuilder("<shapes>\n");

    @Override public void visit(CircleShape c)    { xml.append("  <circle radius=\"").append(c.radius).append("\"/>\n"); }
    @Override public void visit(RectangleShape r) { xml.append("  <rectangle width=\"").append(r.width).append("\" height=\"").append(r.height).append("\"/>\n"); }
    @Override public void visit(TriangleShape t)  { xml.append("  <triangle base=\"").append(t.base).append("\" height=\"").append(t.height).append("\"/>\n"); }

    public String getXml() { return xml.append("</shapes>").toString(); }
}

// ── Main ──────────────────────────────────────────────────
class VisitorDemo {
    public static void main(String[] args) {
        List<Shape> shapes = Arrays.asList(
            new CircleShape(5),
            new RectangleShape(4, 6),
            new TriangleShape(3, 8),
            new CircleShape(2)
        );

        System.out.println("=== Area Calculation ===");
        AreaCalculator areaCalc = new AreaCalculator();
        for (Shape s : shapes) s.accept(areaCalc);
        System.out.printf("  TOTAL area: %.2f%n", areaCalc.getTotalArea());

        System.out.println("\n=== Perimeter Calculation ===");
        PerimeterCalculator perimCalc = new PerimeterCalculator();
        for (Shape s : shapes) s.accept(perimCalc);

        System.out.println("\n=== XML Export ===");
        XmlExporter exporter = new XmlExporter();
        for (Shape s : shapes) s.accept(exporter);
        System.out.println(exporter.getXml());
    }
}
```

---

## 23. Interpreter

### Intent
Define a grammar and provide an interpreter to deal with that grammar. Represent each grammar rule as a class.

### Real-world analogy
A SQL engine interprets `SELECT * FROM users WHERE age > 25`.

### Full Code

```java
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

// ── Context ───────────────────────────────────────────────
class Context {
    private final Map<String, Integer> variables = new HashMap<>();

    public void setVariable(String name, int value) { variables.put(name, value); }
    public int  getVariable(String name) {
        if (!variables.containsKey(name))
            throw new RuntimeException("Unknown variable: " + name);
        return variables.get(name);
    }
}

// ── Expression interface ──────────────────────────────────
interface Expression {
    int interpret(Context context);
}

// ── Terminal Expressions ──────────────────────────────────
class NumberExpression implements Expression {
    private final int number;
    public NumberExpression(int n) { this.number = n; }
    @Override public int interpret(Context ctx) { return number; }
}

class VariableExpression implements Expression {
    private final String name;
    public VariableExpression(String name) { this.name = name; }
    @Override public int interpret(Context ctx) { return ctx.getVariable(name); }
}

// ── Non-Terminal Expressions ──────────────────────────────
class AddExpression implements Expression {
    private final Expression left, right;
    public AddExpression(Expression l, Expression r) { left = l; right = r; }
    @Override public int interpret(Context ctx) { return left.interpret(ctx) + right.interpret(ctx); }
}

class SubtractExpression implements Expression {
    private final Expression left, right;
    public SubtractExpression(Expression l, Expression r) { left = l; right = r; }
    @Override public int interpret(Context ctx) { return left.interpret(ctx) - right.interpret(ctx); }
}

class MultiplyExpression implements Expression {
    private final Expression left, right;
    public MultiplyExpression(Expression l, Expression r) { left = l; right = r; }
    @Override public int interpret(Context ctx) { return left.interpret(ctx) * right.interpret(ctx); }
}

// ── Simple Parser (builds the expression tree) ────────────
class SimpleParser {
    // Parses postfix notation: "a b + 3 *" = (a+b)*3
    public static Expression parse(String expression) {
        Stack<Expression> stack = new Stack<>();
        String[] tokens = expression.trim().split("\\s+");

        for (String token : tokens) {
            switch (token) {
                case "+" -> {
                    Expression r = stack.pop(), l = stack.pop();
                    stack.push(new AddExpression(l, r));
                }
                case "-" -> {
                    Expression r = stack.pop(), l = stack.pop();
                    stack.push(new SubtractExpression(l, r));
                }
                case "*" -> {
                    Expression r = stack.pop(), l = stack.pop();
                    stack.push(new MultiplyExpression(l, r));
                }
                default -> {
                    try {
                        stack.push(new NumberExpression(Integer.parseInt(token)));
                    } catch (NumberFormatException e) {
                        stack.push(new VariableExpression(token));
                    }
                }
            }
        }
        return stack.pop();
    }
}

// ── Main ──────────────────────────────────────────────────
class InterpreterDemo {
    public static void main(String[] args) {
        Context ctx = new Context();
        ctx.setVariable("x", 10);
        ctx.setVariable("y", 5);
        ctx.setVariable("z", 3);

        // Postfix: "x y +" = x + y = 15
        Expression expr1 = SimpleParser.parse("x y +");
        System.out.println("x + y = " + expr1.interpret(ctx));

        // Postfix: "x y - z *" = (x - y) * z = 15
        Expression expr2 = SimpleParser.parse("x y - z *");
        System.out.println("(x - y) * z = " + expr2.interpret(ctx));

        // Manual tree: ((x + y) * z) - 10
        Expression manual = new SubtractExpression(
            new MultiplyExpression(
                new AddExpression(
                    new VariableExpression("x"),
                    new VariableExpression("y")),
                new VariableExpression("z")),
            new NumberExpression(10));
        System.out.println("((x + y) * z) - 10 = " + manual.interpret(ctx));

        // Change context — same tree, new result
        ctx.setVariable("x", 20);
        System.out.println("After x=20: ((x + y) * z) - 10 = " + manual.interpret(ctx));
    }
}
```

---

# Quick Reference Cheat Sheet

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                      PATTERN DECISION TREE                                    │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  "How do I create objects?"                                                  │
│    ├─ One instance ever           → Singleton                                │
│    ├─ Subclass decides type       → Factory Method                           │
│    ├─ Family of objects           → Abstract Factory                         │
│    ├─ Step-by-step construction   → Builder                                  │
│    └─ Clone existing              → Prototype                                │
│                                                                              │
│  "How do I compose objects?"                                                 │
│    ├─ Incompatible interface      → Adapter                                  │
│    ├─ Decouple abstraction/impl   → Bridge                                   │
│    ├─ Tree structure              → Composite                                │
│    ├─ Add behavior dynamically    → Decorator                                │
│    ├─ Simplify complex API        → Facade                                   │
│    ├─ Millions of similar objects → Flyweight                                │
│    └─ Control access             → Proxy                                     │
│                                                                              │
│  "How do objects talk to each other?"                                        │
│    ├─ Pass request down chain     → Chain of Responsibility                  │
│    ├─ Encapsulate request/undo    → Command                                  │
│    ├─ Traverse without knowing    → Iterator                                 │
│    ├─ Centralize communication    → Mediator                                 │
│    ├─ Save/restore state          → Memento                                  │
│    ├─ Notify on state change      → Observer                                 │
│    ├─ Behavior changes with state → State                                    │
│    ├─ Swap algorithms             → Strategy                                 │
│    ├─ Algorithm skeleton          → Template Method                          │
│    ├─ Add operations without edit → Visitor                                  │
│    └─ Interpret a grammar         → Interpreter                              │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

## SOLID Principles — How Patterns Enforce Them

| SOLID | Full Name | Patterns that enforce it |
|-------|-----------|--------------------------|
| **S** | Single Responsibility | Command, Facade, Iterator |
| **O** | Open/Closed | Strategy, Template Method, Visitor |
| **L** | Liskov Substitution | Factory Method, Composite |
| **I** | Interface Segregation | Adapter, Facade |
| **D** | Dependency Inversion | Abstract Factory, Builder, all Factory patterns |

## Common Interview Pattern Combos

| Combo | Where you see it |
|-------|-----------------|
| Strategy + Factory | Spring's `BeanFactory`, sorting algorithms |
| Observer + Command | Event-driven systems, GUI frameworks |
| Decorator + Builder | Java I/O Streams, Lombok builders |
| Proxy + Singleton | Spring `@Transactional`, connection pools |
| Composite + Visitor | Compiler ASTs, XML/JSON parsers |
| Facade + Adapter | SDK wrappers, microservice clients |

---

> 💡 **Study tip**: Code each pattern from memory once. Then explain it without code — if you can, you truly know it.
