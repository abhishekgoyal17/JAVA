# 🧠 LLD Complete Interview Guide : One Stop Preparation

> **The gap most people have:** They know design patterns in isolation but freeze when facing a blank problem. This guide fixes exactly that — with a repeatable thinking system + end-to-end Java code for the top 10 problems.

---

## Table of Contents

1. [The Entity Identification Framework](#1-the-entity-identification-framework)
2. [The 5-Step Interview Process](#2-the-5-step-interview-process)
3. [Design Pattern → Problem Mapping Cheat Sheet](#3-design-pattern--problem-mapping-cheat-sheet)
4. [Problem 1: Parking Lot](#4-problem-1-parking-lot)
5. [Problem 2: Movie Ticket Booking (BookMyShow)](#5-problem-2-movie-ticket-booking-bookmyshow)
6. [Problem 3: Snake & Ladder](#6-problem-3-snake--ladder)
7. [Problem 4: Elevator System](#7-problem-4-elevator-system)
8. [Problem 5: Library Management System](#8-problem-5-library-management-system)
9. [Problem 6: Ride Sharing (Uber/Ola)](#9-problem-6-ride-sharing-uberola)
10. [Problem 7: Vending Machine](#10-problem-7-vending-machine)
11. [Problem 8: LRU Cache](#11-problem-8-lru-cache)
12. [Problem 9: Hotel Management System](#12-problem-9-hotel-management-system)
13. [Problem 10: Splitwise / Expense Sharing](#13-problem-10-splitwise--expense-sharing)
14. [Common Mistakes & Anti-Patterns](#14-common-mistakes--anti-patterns)
15. [Quick Entity Identification Practice Table](#15-quick-entity-identification-practice-table)

    ## Part 2

    
11. [Problem 11: KYC Service System (Bank)](#11-problem-11-kyc-service-system-bank)
12. [Problem 12: Metering Service (Adobe Acrobat / Quota System)](#12-problem-12-metering-service-adobe-acrobat--quota-system)
13. [Problem 13: Job Scheduler](#13-problem-13-job-scheduler)
14. [Problem 14: API Rate Limiter](#14-problem-14-api-rate-limiter)
15. [Problem 15: Notification Service](#15-problem-15-notification-service)
16. [Problem 16: Chess Game](#16-problem-16-chess-game)
17. [Problem 17: ATM Machine](#17-problem-17-atm-machine)
18. [Problem 18: E-commerce Order Management](#18-problem-18-e-commerce-order-management)
19. [Problem 19: Log Aggregation & Monitoring System](#19-problem-19-log-aggregation--monitoring-system)
20. [Problem 20: Distributed Cache with Invalidation](#20-problem-20-distributed-cache-with-invalidation)

---

## 1. The Entity Identification Framework

This is the **core skill** you're struggling with. Here's a repeatable algorithm.

### Step 1 — Noun Extraction Filter

Read the problem statement and underline every noun. Then apply this 3-question filter to each:

| Question | If YES → | If NO → |
|---|---|---|
| Does it have its own **identity** (unique ID)? | Strong Entity candidate | Might be a value object / field |
| Does it have **state that changes** over time? | Definite Entity | Might be just an enum / constant |
| Does it have **behavior** (does things or things happen to it)? | Needs its own class | Might be a field on another entity |

### Step 2 — The 4 Entity Archetypes

Every entity in any LLD problem falls into one of these four buckets:

```
┌─────────────────────────────────────────────────────────────────────┐
│  ARCHETYPE 1: The ACTOR                                             │
│  Who uses/interacts with the system?                                │
│  Examples: User, Customer, Driver, Admin, Agent                     │
│  Always has: id, name, contact info, role/status                    │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 2: The RESOURCE                                          │
│  What is being managed / allocated / consumed?                      │
│  Examples: ParkingSpot, Seat, Room, Book, Ticket                    │
│  Always has: id, availability status, type/category                 │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 3: The TRANSACTION                                       │
│  What happens when Actor interacts with Resource?                   │
│  Examples: Booking, Payment, Ticket, Ride, Order                    │
│  Always has: id, timestamp, actorRef, resourceRef, status           │
├─────────────────────────────────────────────────────────────────────┤
│  ARCHETYPE 4: The COORDINATOR / MANAGER                             │
│  What orchestrates the system? (Often a Singleton)                  │
│  Examples: ParkingLot, Library, VendingMachine, ElevatorController  │
│  Always has: collection of Resources, business rules, state mgmt    │
└─────────────────────────────────────────────────────────────────────┘
```

### Step 3 — Relationship Identification

After finding entities, ask these 3 questions for every pair:

1. **Has-A or Is-A?**
   - `ParkingLot` has `ParkingFloor`s → Composition
   - `Car` is-a `Vehicle` → Inheritance
   - `Booking` has-a reference to `User` → Association

2. **Multiplicity?**
   - One ParkingLot → Many ParkingFloors (1:N)
   - One User → Many Bookings (1:N)
   - One Show → Many Seats (1:N)
   - One Booking → One Seat (1:1)

3. **Lifecycle dependency?**
   - If parent dies, does child die? → **Composition** (strong)
   - Child can exist independently? → **Aggregation** (weak)

### Step 4 — The "What Varies?" Question

This is how you identify where design patterns are needed:

```
Ask: "What might change in future or vary across cases?"

 ↓ The FEE CALCULATION varies? → Strategy Pattern
 ↓ The OBJECT CREATION varies? → Factory / Abstract Factory
 ↓ The NOTIFICATION mechanism varies? → Observer Pattern
 ↓ The OBJECT STATE changes significantly? → State Pattern
 ↓ Only ONE instance should exist? → Singleton
 ↓ You want to ADD behavior dynamically? → Decorator
 ↓ You need to DECOUPLE complex subsystems? → Facade
```

---

## 2. The 5-Step Interview Process

> Use this as a checklist in the actual interview. Each step has a time budget.

### ⏱ Step 1: Gather Requirements (5 min)

Ask clarifying questions. Don't assume. Show structured thinking.

**Questions to always ask:**
- "What are the functional requirements? What can users do?"
- "What are the non-functional requirements? Scale? Concurrency needs?"
- "What's out of scope? (Payment gateway details, auth, notifications?)"
- "Any specific constraints I should keep in mind?"

**Output:** Write down 5-7 bullet requirements before touching design.

### ⏱ Step 2: Identify Entities (5 min)

Apply the 4-archetype framework. Say out loud:
> "Let me scan for the actors, resources, transactions, and coordinators..."

**Output:** List of 5-8 classes with one-line responsibility each.

### ⏱ Step 3: Design Class Structure (10 min)

For each entity, define:
- Attributes (fields)
- Relationships (HAS-A, IS-A)
- Key methods (behavior)
- Enums needed

Sketch a rough class diagram — even if it's 5 boxes connected with arrows.

### ⏱ Step 4: Identify & Apply Patterns (5 min)

Ask the "What Varies?" question. Apply patterns where justified.
State your reasoning: *"Fee calculation will vary by vehicle type, so I'm using Strategy here."*

### ⏱ Step 5: Code the Core (15 min)

Implement 2-3 core classes fully. The interviewer wants to see:
- How entities hold references to each other
- How state transitions work
- How you handle concurrent access (if relevant)
- Clean method names and SRP adherence

---

## 3. Design Pattern → Problem Mapping Cheat Sheet

```
PROBLEM SCENARIO                          PATTERN TO USE
─────────────────────────────────────────────────────────
Fee/Price calculation varies              Strategy
Different vehicle/user types              Factory Method
Complex object construction               Builder  
System-wide single instance              Singleton
State machine (states + transitions)      State
Add features without modifying class      Decorator
Notify multiple objects on event          Observer
Wrap complex subsystem                    Facade
Object creation family                    Abstract Factory
Undo/Redo / Queued operations            Command
Decouple abstraction from impl            Bridge

COMMON COMBOS IN LLD PROBLEMS:
─────────────────────────────────────────────────────────
Parking Lot    → Singleton + Strategy + Factory
BookMyShow     → Singleton + Observer + State
Elevator       → State + Strategy + Observer  
Vending Machine→ State + Factory
Ride Sharing   → Strategy + Observer + Factory
Library        → Observer + Strategy + Factory
```

---

## 4. Problem 1: Parking Lot

### Problem Statement
Design a multi-floor parking lot that supports multiple vehicle types (Car, Bike, Truck), different spot sizes, entry/exit management, ticket generation, and fee calculation.

---

### 🧠 Entity Identification Walk-Through

**Step 1 — Noun Extraction:**
parking lot, floor, spot, vehicle, car, bike, truck, ticket, gate, payment, fee

**Step 2 — Apply 4 Archetypes:**
```
ACTOR:        Vehicle (Car/Bike/Truck inherit from it)
RESOURCE:     ParkingSpot (has id, type, isOccupied)
TRANSACTION:  ParkingTicket (holds entryTime, spot, vehicle)
COORDINATOR:  ParkingLot, ParkingFloor (manager + sub-manager)
EXTRA:        Gate (Entry/Exit), Payment, ParkingFeeStrategy
```

**Step 3 — What Varies?**
- Fee calculation varies by vehicle type → **Strategy Pattern**
- Spot assignment strategy varies → **Strategy Pattern**
- Only ONE ParkingLot exists → **Singleton**

---

### Class Diagram (ASCII)
```
ParkingLot (Singleton)
    │ 1..* 
    ├── ParkingFloor
    │       │ 1..*
    │       └── ParkingSpot
    │               │
    │           SpotType (SMALL/MEDIUM/LARGE)
    │
    ├── EntryGate
    ├── ExitGate
    │
    └── ParkingTicket
            │ 1..1
            ├── Vehicle ◄── Car, Bike, Truck
            ├── ParkingSpot (ref)
            └── Payment
                    │
                    └── <<interface>> ParkingFeeStrategy
                                  ◄── HourlyFeeStrategy
                                  ◄── DayFeeStrategy
```

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum VehicleType { BIKE, CAR, TRUCK }

public enum SpotType { SMALL, MEDIUM, LARGE }

public enum SpotStatus { FREE, OCCUPIED }

public enum PaymentStatus { PENDING, COMPLETED }

// ─── Vehicle Hierarchy ───────────────────────────────────────────

public abstract class Vehicle {
    protected String licensePlate;
    protected VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public VehicleType getType() { return type; }
    public String getLicensePlate() { return licensePlate; }
}

public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }
}

public class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate, VehicleType.BIKE);
    }
}

public class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }
}

// ─── ParkingSpot ─────────────────────────────────────────────────

public class ParkingSpot {
    private final String spotId;
    private final SpotType spotType;
    private SpotStatus status;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.status = SpotStatus.FREE;
    }

    public boolean isAvailable() {
        return status == SpotStatus.FREE;
    }

    public void parkVehicle(Vehicle vehicle) {
        if (!isAvailable()) throw new IllegalStateException("Spot already occupied");
        this.parkedVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }

    public void removeVehicle() {
        this.parkedVehicle = null;
        this.status = SpotStatus.FREE;
    }

    public boolean canFit(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE  -> spotType == SpotType.SMALL || spotType == SpotType.MEDIUM;
            case CAR   -> spotType == SpotType.MEDIUM || spotType == SpotType.LARGE;
            case TRUCK -> spotType == SpotType.LARGE;
        };
    }

    public String getSpotId() { return spotId; }
    public SpotType getSpotType() { return spotType; }
    public SpotStatus getStatus() { return status; }
}

// ─── Fee Strategy ────────────────────────────────────────────────

public interface ParkingFeeStrategy {
    double calculateFee(long durationInMinutes, VehicleType vehicleType);
}

public class HourlyFeeStrategy implements ParkingFeeStrategy {
    private static final Map<VehicleType, Double> RATES = Map.of(
        VehicleType.BIKE,  10.0,
        VehicleType.CAR,   20.0,
        VehicleType.TRUCK, 40.0
    );

    @Override
    public double calculateFee(long durationInMinutes, VehicleType vehicleType) {
        double hours = Math.ceil(durationInMinutes / 60.0);
        return hours * RATES.get(vehicleType);
    }
}

// ─── Payment ─────────────────────────────────────────────────────

public class Payment {
    private final String paymentId;
    private final double amount;
    private PaymentStatus status;

    public Payment(String paymentId, double amount) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public void process() {
        // integrate with payment gateway
        this.status = PaymentStatus.COMPLETED;
    }

    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
}

// ─── Parking Ticket ──────────────────────────────────────────────

public class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Payment payment;

    public ParkingTicket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
    }

    public void checkOut(ParkingFeeStrategy feeStrategy) {
        this.exitTime = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(entryTime, exitTime);
        double fee = feeStrategy.calculateFee(minutes, vehicle.getType());
        this.payment = new Payment(UUID.randomUUID().toString(), fee);
    }

    public Payment getPayment() { return payment; }
    public String getTicketId() { return ticketId; }
    public ParkingSpot getSpot() { return spot; }
    public Vehicle getVehicle() { return vehicle; }
    public LocalDateTime getEntryTime() { return entryTime; }
}

// ─── Spot Assignment Strategy ────────────────────────────────────

public interface SpotAssignmentStrategy {
    Optional<ParkingSpot> findSpot(List<ParkingFloor> floors, VehicleType vehicleType);
}

public class NearestSpotStrategy implements SpotAssignmentStrategy {
    @Override
    public Optional<ParkingSpot> findSpot(List<ParkingFloor> floors, VehicleType vehicleType) {
        for (ParkingFloor floor : floors) {
            Optional<ParkingSpot> spot = floor.getAvailableSpot(vehicleType);
            if (spot.isPresent()) return spot;
        }
        return Optional.empty();
    }
}

// ─── ParkingFloor ────────────────────────────────────────────────

public class ParkingFloor {
    private final String floorId;
    private final List<ParkingSpot> spots;

    public ParkingFloor(String floorId, List<ParkingSpot> spots) {
        this.floorId = floorId;
        this.spots = spots;
    }

    public Optional<ParkingSpot> getAvailableSpot(VehicleType vehicleType) {
        return spots.stream()
            .filter(s -> s.isAvailable() && s.canFit(vehicleType))
            .findFirst();
    }

    public long getAvailableCount() {
        return spots.stream().filter(ParkingSpot::isAvailable).count();
    }

    public String getFloorId() { return floorId; }
}

// ─── Entry & Exit Gates ──────────────────────────────────────────

public class EntryGate {
    private final String gateId;
    private final ParkingLot parkingLot;

    public EntryGate(String gateId, ParkingLot parkingLot) {
        this.gateId = gateId;
        this.parkingLot = parkingLot;
    }

    public ParkingTicket enter(Vehicle vehicle) {
        return parkingLot.parkVehicle(vehicle);
    }
}

public class ExitGate {
    private final String gateId;
    private final ParkingLot parkingLot;

    public ExitGate(String gateId, ParkingLot parkingLot) {
        this.gateId = gateId;
        this.parkingLot = parkingLot;
    }

    public Payment exit(ParkingTicket ticket) {
        return parkingLot.processExit(ticket);
    }
}

// ─── ParkingLot (Singleton + Facade) ─────────────────────────────

public class ParkingLot {
    private static ParkingLot instance;

    private final String name;
    private final List<ParkingFloor> floors;
    private final SpotAssignmentStrategy assignmentStrategy;
    private final ParkingFeeStrategy feeStrategy;
    private final Map<String, ParkingTicket> activeTickets; // ticketId → ticket

    private ParkingLot(String name, List<ParkingFloor> floors,
                       SpotAssignmentStrategy assignmentStrategy,
                       ParkingFeeStrategy feeStrategy) {
        this.name = name;
        this.floors = floors;
        this.assignmentStrategy = assignmentStrategy;
        this.feeStrategy = feeStrategy;
        this.activeTickets = new ConcurrentHashMap<>();
    }

    public static synchronized ParkingLot getInstance(String name,
                                                       List<ParkingFloor> floors,
                                                       SpotAssignmentStrategy strategy,
                                                       ParkingFeeStrategy feeStrategy) {
        if (instance == null) {
            instance = new ParkingLot(name, floors, strategy, feeStrategy);
        }
        return instance;
    }

    public synchronized ParkingTicket parkVehicle(Vehicle vehicle) {
        Optional<ParkingSpot> spot = assignmentStrategy.findSpot(floors, vehicle.getType());
        if (spot.isEmpty()) throw new IllegalStateException("Parking lot is full!");

        spot.get().parkVehicle(vehicle);
        ParkingTicket ticket = new ParkingTicket(vehicle, spot.get());
        activeTickets.put(ticket.getTicketId(), ticket);
        System.out.println("Ticket issued: " + ticket.getTicketId() + " → Spot: " + spot.get().getSpotId());
        return ticket;
    }

    public synchronized Payment processExit(ParkingTicket ticket) {
        ticket.checkOut(feeStrategy);
        ticket.getSpot().removeVehicle();
        activeTickets.remove(ticket.getTicketId());
        ticket.getPayment().process();
        System.out.println("Fee: ₹" + ticket.getPayment().getAmount());
        return ticket.getPayment();
    }

    public boolean isFull() {
        return floors.stream().allMatch(f -> f.getAvailableCount() == 0);
    }

    public long totalAvailable() {
        return floors.stream().mapToLong(ParkingFloor::getAvailableCount).sum();
    }
}

// ─── Demo / Main ─────────────────────────────────────────────────

/*
public class ParkingLotDemo {
    public static void main(String[] args) {
        List<ParkingSpot> spots = List.of(
            new ParkingSpot("S1-01", SpotType.SMALL),
            new ParkingSpot("M1-01", SpotType.MEDIUM),
            new ParkingSpot("L1-01", SpotType.LARGE)
        );
        ParkingFloor floor1 = new ParkingFloor("F1", spots);

        ParkingLot lot = ParkingLot.getInstance(
            "Central Parking",
            List.of(floor1),
            new NearestSpotStrategy(),
            new HourlyFeeStrategy()
        );

        EntryGate entry = new EntryGate("G1", lot);
        ExitGate exit = new ExitGate("G2", lot);

        Car car = new Car("KA-01-1234");
        ParkingTicket ticket = entry.enter(car);

        // simulate some time...

        exit.exit(ticket);
    }
}
*/
```

**Design Decisions Explained:**
- `ParkingLot` is Singleton because there's only one physical lot
- `ParkingFeeStrategy` follows Strategy — can swap HourlyFeeStrategy / DayFeeStrategy
- `SpotAssignmentStrategy` follows Strategy — can swap NearestFirst / LargestFirst
- `Vehicle` uses Inheritance — Car/Bike/Truck share licensePlate but differ in VehicleType
- `ConcurrentHashMap` for activeTickets handles concurrent entry gates
- `synchronized` on parkVehicle/processExit prevents race condition on spot allocation

**Follow-up Questions to Prepare:**
- "How would you handle concurrent bookings?" → `synchronized` on spot allocation, or pessimistic lock per spot
- "What if lot has 10 entry gates?" → Each gate runs its own thread; `ConcurrentHashMap` + `synchronized` block
- "How to add monthly pass holders?" → New `VehicleType` or decorator on `ParkingTicket`
- "How to persist ticket data?" → Add `TicketRepository` interface with DB implementation

---

## 5. Problem 2: Movie Ticket Booking (BookMyShow)

### Problem Statement
Design a movie ticket booking system where users can search movies, view shows, select seats, book tickets, and cancel bookings.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
user, movie, cinema hall, screen, show, seat, booking, ticket, payment, city

**Apply 4 Archetypes:**
```
ACTOR:        User
RESOURCE:     Movie, Screen, Seat, Show (a scheduled showing)
TRANSACTION:  Booking, Ticket, Payment
COORDINATOR:  BookingService (manages the booking lifecycle)
EXTRA:        City (for search), SeatLock (for concurrent seat hold)
```

**Key Insight:** `Show` is the pivot entity — it joins Movie + Screen + Time. A Seat belongs to Screen but a ShowSeat belongs to Show (availability is per-show, not per-seat globally).

**What Varies?**
- Seat locking strategy → can change (in-memory vs Redis) → Strategy/Interface
- Payment processing → Strategy / external service
- Notification when booking confirmed → Observer

---

### Class Diagram (ASCII)
```
City
  └─1:N── Cinema
              └─1:N── Screen
                          └─1:N── Seat (row, col, seatType)
                          └─1:N── Show
                                    │
                        Movie ──────┘ (many shows for one movie)
                                    │
                                  ShowSeat (joins Show + Seat, has status)
                                    │
                                  Booking ────── User
                                    │
                                  Ticket (1 per seat in booking)
                                    │
                                  Payment
```

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum SeatType { SILVER, GOLD, PLATINUM }

public enum SeatStatus { AVAILABLE, LOCKED, BOOKED }

public enum BookingStatus { PENDING, CONFIRMED, CANCELLED }

// ─── Core Entities ────────────────────────────────────────────────

public class Movie {
    private final String movieId;
    private final String title;
    private final int durationMinutes;
    private final String language;

    public Movie(String movieId, String title, int durationMinutes, String language) {
        this.movieId = movieId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.language = language;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
}

public class Seat {
    private final String seatId;
    private final int row;
    private final int col;
    private final SeatType seatType;

    public Seat(String seatId, int row, int col, SeatType seatType) {
        this.seatId = seatId;
        this.row = row;
        this.col = col;
        this.seatType = seatType;
    }

    public String getSeatId() { return seatId; }
    public SeatType getSeatType() { return seatType; }
}

public class Screen {
    private final String screenId;
    private final String name;
    private final List<Seat> seats;

    public Screen(String screenId, String name, List<Seat> seats) {
        this.screenId = screenId;
        this.name = name;
        this.seats = seats;
    }

    public List<Seat> getSeats() { return Collections.unmodifiableList(seats); }
    public String getScreenId() { return screenId; }
}

// ShowSeat = availability of a specific seat for a specific show
public class ShowSeat {
    private final Seat seat;
    private volatile SeatStatus status;
    private final double price;

    public ShowSeat(Seat seat, double price) {
        this.seat = seat;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public synchronized boolean lock() {
        if (status == SeatStatus.AVAILABLE) {
            status = SeatStatus.LOCKED;
            return true;
        }
        return false;
    }

    public synchronized void confirm() {
        if (status != SeatStatus.LOCKED) throw new IllegalStateException("Must be LOCKED before confirming");
        status = SeatStatus.BOOKED;
    }

    public synchronized void release() {
        status = SeatStatus.AVAILABLE;
    }

    public boolean isAvailable() { return status == SeatStatus.AVAILABLE; }
    public Seat getSeat() { return seat; }
    public double getPrice() { return price; }
    public SeatStatus getStatus() { return status; }
}

public class Show {
    private final String showId;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final Map<String, ShowSeat> showSeats; // seatId → ShowSeat

    public Show(String showId, Movie movie, Screen screen, LocalDateTime startTime) {
        this.showId = showId;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.showSeats = new ConcurrentHashMap<>();
        // initialize show seats from screen seats
        for (Seat seat : screen.getSeats()) {
            double price = getPriceForSeatType(seat.getSeatType());
            showSeats.put(seat.getSeatId(), new ShowSeat(seat, price));
        }
    }

    private double getPriceForSeatType(SeatType type) {
        return switch (type) {
            case SILVER -> 150.0;
            case GOLD -> 250.0;
            case PLATINUM -> 400.0;
        };
    }

    public Optional<ShowSeat> getShowSeat(String seatId) {
        return Optional.ofNullable(showSeats.get(seatId));
    }

    public List<ShowSeat> getAvailableSeats() {
        return showSeats.values().stream()
            .filter(ShowSeat::isAvailable)
            .toList();
    }

    public String getShowId() { return showId; }
    public Movie getMovie() { return movie; }
    public LocalDateTime getStartTime() { return startTime; }
}

// ─── User ─────────────────────────────────────────────────────────

public class User {
    private final String userId;
    private final String name;
    private final String email;
    private final String phone;
    private final List<Booking> bookings;

    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bookings = new ArrayList<>();
    }

    public void addBooking(Booking booking) { bookings.add(booking); }
    public List<Booking> getBookings() { return Collections.unmodifiableList(bookings); }
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
}

// ─── Booking + Ticket ─────────────────────────────────────────────

public class Ticket {
    private final String ticketId;
    private final ShowSeat showSeat;

    public Ticket(ShowSeat showSeat) {
        this.ticketId = UUID.randomUUID().toString();
        this.showSeat = showSeat;
    }

    public String getTicketId() { return ticketId; }
    public ShowSeat getShowSeat() { return showSeat; }
}

public class Booking {
    private final String bookingId;
    private final User user;
    private final Show show;
    private final List<Ticket> tickets;
    private BookingStatus status;
    private Payment payment;

    public Booking(User user, Show show, List<ShowSeat> selectedSeats) {
        this.bookingId = UUID.randomUUID().toString();
        this.user = user;
        this.show = show;
        this.status = BookingStatus.PENDING;
        this.tickets = selectedSeats.stream().map(Ticket::new).toList();
    }

    public double getTotalAmount() {
        return tickets.stream()
            .mapToDouble(t -> t.getShowSeat().getPrice())
            .sum();
    }

    public void confirm(Payment payment) {
        this.payment = payment;
        this.status = BookingStatus.CONFIRMED;
        tickets.forEach(t -> t.getShowSeat().confirm());
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        tickets.forEach(t -> t.getShowSeat().release());
    }

    public String getBookingId() { return bookingId; }
    public BookingStatus getStatus() { return status; }
    public List<Ticket> getTickets() { return tickets; }
}

// ─── Booking Service (Facade / Coordinator) ───────────────────────

public class BookingService {
    private static BookingService instance;

    private final Map<String, Show> shows; // showId → Show

    private BookingService() {
        this.shows = new ConcurrentHashMap<>();
    }

    public static synchronized BookingService getInstance() {
        if (instance == null) instance = new BookingService();
        return instance;
    }

    public void addShow(Show show) {
        shows.put(show.getShowId(), show);
    }

    public Optional<Show> getShow(String showId) {
        return Optional.ofNullable(shows.get(showId));
    }

    // The critical section: lock seats atomically before booking
    public Booking createBooking(User user, String showId, List<String> seatIds) {
        Show show = shows.get(showId);
        if (show == null) throw new IllegalArgumentException("Show not found: " + showId);

        List<ShowSeat> lockedSeats = new ArrayList<>();

        try {
            for (String seatId : seatIds) {
                ShowSeat showSeat = show.getShowSeat(seatId)
                    .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

                if (!showSeat.lock()) {
                    // failed to lock — release already locked seats and fail
                    lockedSeats.forEach(ShowSeat::release);
                    throw new IllegalStateException("Seat " + seatId + " is not available");
                }
                lockedSeats.add(showSeat);
            }

            Booking booking = new Booking(user, show, lockedSeats);
            user.addBooking(booking);

            // Process payment (simplified)
            Payment payment = new Payment(UUID.randomUUID().toString(), booking.getTotalAmount());
            payment.process();
            booking.confirm(payment);

            System.out.println("Booking confirmed: " + booking.getBookingId()
                + " | Amount: ₹" + booking.getTotalAmount());
            return booking;

        } catch (Exception e) {
            // ensure seats are released on failure
            lockedSeats.forEach(ShowSeat::release);
            throw e;
        }
    }

    public void cancelBooking(Booking booking) {
        booking.cancel();
        System.out.println("Booking cancelled: " + booking.getBookingId());
    }
}
```

**Design Decisions Explained:**
- `ShowSeat` is separate from `Seat` — because availability is per-show, not globally
- `ShowSeat.lock()` is `synchronized` — prevents two users booking same seat simultaneously
- `BookingService.createBooking` uses try-catch to rollback locks on failure (atomicity)
- `volatile` on `SeatStatus` ensures memory visibility across threads
- `Booking` holds list of `Ticket`s — each ticket maps to one ShowSeat

**Follow-up Questions:**
- "How do you handle seat lock expiry (user doesn't pay in 10 min)?" → `ScheduledExecutorService` to release locks after timeout
- "How to scale to millions of users?" → Move to Redis-based distributed locks per ShowSeat
- "How to show available seat map?" → `show.getAvailableSeats()` returns current snapshot

---

## 6. Problem 3: Snake & Ladder

### Problem Statement
Design a Snake & Ladder game that supports multiple players, configurable board size, snakes and ladders at specific positions, dice rolling, and win detection.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
game, board, player, dice, snake, ladder, cell/position, turn

**Apply 4 Archetypes:**
```
ACTOR:        Player (name, current position)
RESOURCE:     Board (cells, snakes, ladders mapping)
TRANSACTION:  Turn (roll dice, move, check special cell)
COORDINATOR:  Game (orchestrates everything, tracks state)
EXTRA:        Dice, BoardEntity (abstract for Snake + Ladder)
```

**Key Insight:**
- Snake and Ladder are structurally the same — both have `start` and `end` positions
- The only difference is: Snake `start > end`, Ladder `start < end`
- → **Inheritance**: `BoardEntity` abstract class with `Snake` and `Ladder` subclasses

**What Varies?**
- Number of dice? → Constructor param
- Board size? → Constructor param  
- Win condition? → Could be Strategy, but usually just "first to 100"
- Turn order? → `Queue<Player>` — rotate after each turn

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum GameStatus { NOT_STARTED, RUNNING, FINISHED }

// ─── BoardEntity (Abstract) ────────────────────────────────────────

public abstract class BoardEntity {
    protected final int start;
    protected final int end;

    public BoardEntity(int start, int end) {
        validatePositions(start, end);
        this.start = start;
        this.end = end;
    }

    protected abstract void validatePositions(int start, int end);

    public int getStart() { return start; }
    public int getEnd() { return end; }
}

public class Snake extends BoardEntity {
    public Snake(int head, int tail) {
        super(head, tail);
    }

    @Override
    protected void validatePositions(int start, int end) {
        if (start <= end) throw new IllegalArgumentException("Snake head must be > tail. head=" + start + " tail=" + end);
    }

    @Override
    public String toString() { return "Snake[" + start + "→" + end + "]"; }
}

public class Ladder extends BoardEntity {
    public Ladder(int bottom, int top) {
        super(bottom, top);
    }

    @Override
    protected void validatePositions(int start, int end) {
        if (start >= end) throw new IllegalArgumentException("Ladder bottom must be < top. bottom=" + start + " top=" + end);
    }

    @Override
    public String toString() { return "Ladder[" + start + "→" + end + "]"; }
}

// ─── Dice ─────────────────────────────────────────────────────────

public class Dice {
    private final int numberOfDice;
    private final Random random;

    public Dice(int numberOfDice) {
        this.numberOfDice = numberOfDice;
        this.random = new Random();
    }

    public int roll() {
        int total = 0;
        for (int i = 0; i < numberOfDice; i++) {
            total += random.nextInt(6) + 1;
        }
        return total;
    }
}

// ─── Player ───────────────────────────────────────────────────────

public class Player {
    private final String playerId;
    private final String name;
    private int currentPosition;

    public Player(String playerId, String name) {
        this.playerId = playerId;
        this.name = name;
        this.currentPosition = 0; // off-board, needs to roll to enter
    }

    public void moveTo(int position) {
        this.currentPosition = position;
    }

    public int getCurrentPosition() { return currentPosition; }
    public String getName() { return name; }
    public String getPlayerId() { return playerId; }
}

// ─── Board ────────────────────────────────────────────────────────

public class Board {
    private final int size;
    private final Map<Integer, BoardEntity> boardEntityMap; // position → entity

    public Board(int size, List<Snake> snakes, List<Ladder> ladders) {
        this.size = size;
        this.boardEntityMap = new HashMap<>();

        for (Snake snake : snakes) {
            if (boardEntityMap.containsKey(snake.getStart()))
                throw new IllegalArgumentException("Position " + snake.getStart() + " already occupied");
            boardEntityMap.put(snake.getStart(), snake);
        }

        for (Ladder ladder : ladders) {
            if (boardEntityMap.containsKey(ladder.getStart()))
                throw new IllegalArgumentException("Position " + ladder.getStart() + " already occupied");
            boardEntityMap.put(ladder.getStart(), ladder);
        }
    }

    /**
     * Returns the final position after applying snake/ladder if present.
     */
    public int getFinalPosition(int rawPosition) {
        if (rawPosition > size) return -1; // overshoot — stay put (signal)
        if (boardEntityMap.containsKey(rawPosition)) {
            BoardEntity entity = boardEntityMap.get(rawPosition);
            System.out.println("  " + entity + " triggered!");
            return entity.getEnd();
        }
        return rawPosition;
    }

    public int getSize() { return size; }

    public boolean isWinningPosition(int position) {
        return position == size;
    }
}

// ─── Game (Orchestrator) ──────────────────────────────────────────

public class Game {
    private final Board board;
    private final Dice dice;
    private final Queue<Player> playerQueue;
    private GameStatus status;
    private Player winner;

    public Game(Board board, Dice dice, List<Player> players) {
        if (players == null || players.isEmpty())
            throw new IllegalArgumentException("Need at least 1 player");

        this.board = board;
        this.dice = dice;
        this.playerQueue = new LinkedList<>(players);
        this.status = GameStatus.NOT_STARTED;
    }

    public void start() {
        if (status != GameStatus.NOT_STARTED)
            throw new IllegalStateException("Game already started");
        status = GameStatus.RUNNING;
        System.out.println("Game started! Players: " + playerQueue.stream()
            .map(Player::getName).toList());
    }

    public void playTurn() {
        if (status != GameStatus.RUNNING) throw new IllegalStateException("Game not running");

        Player currentPlayer = playerQueue.poll();
        int diceRoll = dice.roll();
        int newPosition = currentPlayer.getCurrentPosition() + diceRoll;

        System.out.printf("%s rolled %d. Position: %d → ",
            currentPlayer.getName(), diceRoll, currentPlayer.getCurrentPosition());

        int finalPosition = board.getFinalPosition(newPosition);

        if (finalPosition == -1) {
            // overshoot — player stays
            finalPosition = currentPlayer.getCurrentPosition();
            System.out.println(finalPosition + " (overshoot, stay)");
        } else {
            currentPlayer.moveTo(finalPosition);
            System.out.println(finalPosition);
        }

        if (board.isWinningPosition(finalPosition)) {
            status = GameStatus.FINISHED;
            winner = currentPlayer;
            System.out.println("🎉 " + currentPlayer.getName() + " WINS!");
            return;
        }

        // rotate player to back of queue
        playerQueue.offer(currentPlayer);
    }

    public void play() {
        start();
        while (status == GameStatus.RUNNING) {
            playTurn();
        }
    }

    public Player getWinner() { return winner; }
    public GameStatus getStatus() { return status; }
}

// ─── Demo ─────────────────────────────────────────────────────────

/*
public class SnakeLadderDemo {
    public static void main(String[] args) {
        List<Snake> snakes = List.of(
            new Snake(99, 5),
            new Snake(75, 32),
            new Snake(50, 12)
        );

        List<Ladder> ladders = List.of(
            new Ladder(3, 22),
            new Ladder(8, 43),
            new Ladder(20, 64)
        );

        Board board = new Board(100, snakes, ladders);
        Dice dice = new Dice(1);

        List<Player> players = List.of(
            new Player("P1", "Abhishek"),
            new Player("P2", "Rohan")
        );

        Game game = new Game(board, dice, players);
        game.play();
    }
}
*/
```

**Design Decisions Explained:**
- `BoardEntity` is abstract — `Snake` and `Ladder` share `start/end` but enforce different constraints
- `Board.getFinalPosition()` is the single point of board logic — clean separation
- `Queue<Player>` naturally handles turn rotation
- `-1` as sentinel value for overshoot — simple and readable
- `GameStatus` enum makes state machine explicit

**Follow-up Questions:**
- "What if player who rolls 6 gets an extra turn?" → Add boolean `shouldPlayAgain = (diceRoll == 6)` in `playTurn()`
- "What if multiple dice?" → `Dice` already supports it via `numberOfDice`
- "Save and resume game?" → Serialize `playerQueue` and `currentPositions` to JSON

---

## 7. Problem 4: Elevator System

### Problem Statement
Design an elevator control system for a building with multiple elevators. The system should receive floor requests (both inside and outside elevators), schedule elevators efficiently, and handle concurrent requests.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
building, elevator, floor, request, door, display, controller, passenger

**Apply 4 Archetypes:**
```
ACTOR:        Passenger (implicit — triggers requests)
RESOURCE:     Elevator (has state: current floor, direction, door status)
TRANSACTION:  ElevatorRequest (internal button press OR external hall call)
COORDINATOR:  ElevatorController (dispatches elevators to requests)
EXTRA:        Direction enum, ElevatorStatus, SchedulingStrategy
```

**Key Insight:**
- There are 2 types of requests: **External** (hall call — floor + direction) and **Internal** (destination press inside elevator)
- The `Elevator` is a state machine: IDLE, MOVING_UP, MOVING_DOWN, DOORS_OPEN
- Elevator scheduling is the complex part → use **Strategy Pattern**

**What Varies?**
- Scheduling algorithm (SCAN, LOOK, nearest elevator) → **Strategy Pattern**
- Elevator state transitions → **State Pattern** (or simple enum + switch)

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum Direction { UP, DOWN, IDLE }

public enum ElevatorStatus { IDLE, MOVING, DOORS_OPEN, MAINTENANCE }

public enum RequestType { EXTERNAL, INTERNAL } // hall call vs inside press

// ─── Elevator Request ─────────────────────────────────────────────

public class ElevatorRequest {
    private final int floor;
    private final Direction direction; // null for internal requests
    private final RequestType type;

    // External request (hall call): "I'm on floor 5 going UP"
    public static ElevatorRequest external(int floor, Direction direction) {
        return new ElevatorRequest(floor, direction, RequestType.EXTERNAL);
    }

    // Internal request (inside elevator): "Take me to floor 10"
    public static ElevatorRequest internal(int floor) {
        return new ElevatorRequest(floor, null, RequestType.INTERNAL);
    }

    private ElevatorRequest(int floor, Direction direction, RequestType type) {
        this.floor = floor;
        this.direction = direction;
        this.type = type;
    }

    public int getFloor() { return floor; }
    public Direction getDirection() { return direction; }
    public RequestType getType() { return type; }
}

// ─── Elevator ─────────────────────────────────────────────────────

public class Elevator {
    private final String elevatorId;
    private int currentFloor;
    private Direction direction;
    private ElevatorStatus status;
    private final TreeSet<Integer> destinationFloors; // sorted set for efficient SCAN

    public Elevator(String elevatorId, int startFloor) {
        this.elevatorId = elevatorId;
        this.currentFloor = startFloor;
        this.direction = Direction.IDLE;
        this.status = ElevatorStatus.IDLE;
        this.destinationFloors = new TreeSet<>();
    }

    public synchronized void addDestination(int floor) {
        destinationFloors.add(floor);
        if (status == ElevatorStatus.IDLE) {
            direction = (floor > currentFloor) ? Direction.UP : Direction.DOWN;
            status = ElevatorStatus.MOVING;
        }
    }

    // Called by controller to simulate one step of movement
    public synchronized void step() {
        if (destinationFloors.isEmpty()) {
            status = ElevatorStatus.IDLE;
            direction = Direction.IDLE;
            return;
        }

        // Move one floor toward next destination
        if (direction == Direction.UP) {
            currentFloor++;
        } else if (direction == Direction.DOWN) {
            currentFloor--;
        }

        System.out.println("Elevator " + elevatorId + " → Floor " + currentFloor);

        // Check if we've reached a destination
        if (destinationFloors.contains(currentFloor)) {
            destinationFloors.remove(currentFloor);
            openDoors();

            // Decide next direction
            if (!destinationFloors.isEmpty()) {
                int nextFloor = destinationFloors.first();
                direction = (nextFloor > currentFloor) ? Direction.UP : Direction.DOWN;
            } else {
                direction = Direction.IDLE;
                status = ElevatorStatus.IDLE;
            }
        }
    }

    private void openDoors() {
        status = ElevatorStatus.DOORS_OPEN;
        System.out.println("Elevator " + elevatorId + " — Doors OPEN at floor " + currentFloor);
        // In real system: wait, then close
        status = ElevatorStatus.MOVING;
    }

    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public ElevatorStatus getStatus() { return status; }
    public String getElevatorId() { return elevatorId; }

    // Distance metric for scheduling
    public int distanceTo(int floor) {
        return Math.abs(currentFloor - floor);
    }
}

// ─── Scheduling Strategy ─────────────────────────────────────────

public interface ElevatorSchedulingStrategy {
    Elevator selectElevator(List<Elevator> elevators, ElevatorRequest request);
}

// LOOK algorithm: prefer elevator moving toward requested floor
public class LOOKSchedulingStrategy implements ElevatorSchedulingStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, ElevatorRequest request) {
        Elevator best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (elevator.getStatus() == ElevatorStatus.MAINTENANCE) continue;

            int score = calculateScore(elevator, request);
            if (score < bestScore) {
                bestScore = score;
                best = elevator;
            }
        }

        return best;
    }

    private int calculateScore(Elevator elevator, ElevatorRequest request) {
        int distance = elevator.distanceTo(request.getFloor());
        ElevatorStatus status = elevator.getStatus();
        Direction elevDir = elevator.getDirection();

        // Idle elevator: just use distance
        if (status == ElevatorStatus.IDLE) return distance;

        // Moving in same direction AND hasn't passed the floor yet: prefer this
        if (request.getType() == RequestType.EXTERNAL && request.getDirection() == elevDir) {
            boolean onTheWay = (elevDir == Direction.UP && elevator.getCurrentFloor() <= request.getFloor())
                || (elevDir == Direction.DOWN && elevator.getCurrentFloor() >= request.getFloor());
            if (onTheWay) return distance; // best case
        }

        // Moving in opposite direction: penalize heavily
        return distance + 100;
    }
}

// ─── Elevator Controller (Singleton + Facade) ─────────────────────

public class ElevatorController {
    private static ElevatorController instance;

    private final List<Elevator> elevators;
    private final ElevatorSchedulingStrategy strategy;

    private ElevatorController(List<Elevator> elevators, ElevatorSchedulingStrategy strategy) {
        this.elevators = elevators;
        this.strategy = strategy;
    }

    public static synchronized ElevatorController getInstance(List<Elevator> elevators,
                                                               ElevatorSchedulingStrategy strategy) {
        if (instance == null) {
            instance = new ElevatorController(elevators, strategy);
        }
        return instance;
    }

    // External call: person on floor presses UP or DOWN
    public void handleExternalRequest(int floor, Direction direction) {
        ElevatorRequest request = ElevatorRequest.external(floor, direction);
        dispatch(request);
    }

    // Internal call: person inside elevator presses destination floor
    public void handleInternalRequest(String elevatorId, int destinationFloor) {
        elevators.stream()
            .filter(e -> e.getElevatorId().equals(elevatorId))
            .findFirst()
            .ifPresent(e -> e.addDestination(destinationFloor));
    }

    private void dispatch(ElevatorRequest request) {
        Elevator selected = strategy.selectElevator(elevators, request);
        if (selected == null) {
            System.out.println("No elevators available");
            return;
        }
        selected.addDestination(request.getFloor());
        System.out.println("Assigned to Elevator " + selected.getElevatorId());
    }

    // Simulate one tick of the system
    public void tick() {
        elevators.forEach(Elevator::step);
    }

    public List<Elevator> getElevators() { return Collections.unmodifiableList(elevators); }
}

// ─── Demo ─────────────────────────────────────────────────────────

/*
public class ElevatorDemo {
    public static void main(String[] args) throws InterruptedException {
        List<Elevator> elevators = List.of(
            new Elevator("E1", 0),
            new Elevator("E2", 5)
        );

        ElevatorController controller = ElevatorController.getInstance(
            elevators, new LOOKSchedulingStrategy()
        );

        controller.handleExternalRequest(3, Direction.UP);
        controller.handleExternalRequest(7, Direction.DOWN);
        controller.handleInternalRequest("E1", 10);

        // Simulate 15 ticks
        for (int i = 0; i < 15; i++) {
            controller.tick();
            Thread.sleep(200);
        }
    }
}
*/
```

**Design Decisions Explained:**
- `TreeSet<Integer>` for destinations — sorted, O(log n) insertion, supports SCAN efficiently
- `ElevatorSchedulingStrategy` is Strategy — can swap SCAN, LOOK, Round-Robin
- `LOOKSchedulingStrategy` scores by direction alignment + distance
- `ElevatorController` is Singleton — only one controller per building
- `step()` simulates a tick-based model — clean for testing and simulation

**Follow-up Questions:**
- "How to handle real-time?" → Replace `tick()` with thread-per-elevator running continuously
- "Emergency stop / maintenance?" → `ElevatorStatus.MAINTENANCE` already modeled, scheduler skips it
- "Overload detection?" → Add `maxCapacity` and `currentLoad` to `Elevator`

---

## 8. Problem 5: Library Management System

### Problem Statement
Design a library management system where members can search, borrow, and return books. Handle reservations, fine calculation for late returns, and librarian admin operations.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
library, book, book copy, member, librarian, catalog, borrowing record, reservation, fine, search

**Apply 4 Archetypes:**
```
ACTOR:        Member (borrows), Librarian (admin)
RESOURCE:     Book (metadata), BookItem (physical copy with barcode)
TRANSACTION:  BookLending (borrow record), Reservation, Fine
COORDINATOR:  Library (Singleton), Catalog (search facade)
```

**Key Insight:**
- `Book` ≠ `BookItem`. A book (ISBN, title, author) can have multiple physical copies. This is the most commonly missed distinction.
- A `Member` borrows a `BookItem`, not a `Book`
- `Reservation` is needed when all copies are checked out

**What Varies?**
- Fine calculation logic → **Strategy Pattern**
- Search (by title, author, ISBN) → **Strategy or polymorphism**
- Notification when reserved book becomes available → **Observer Pattern**

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum BookStatus { AVAILABLE, BORROWED, RESERVED, LOST }

public enum MemberStatus { ACTIVE, SUSPENDED, EXPIRED }

public enum ReservationStatus { PENDING, FULFILLED, CANCELLED }

// ─── Book (Metadata) ──────────────────────────────────────────────

public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final String category;
    private final int publicationYear;

    public Book(String isbn, String title, String author, String category, int publicationYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publicationYear = publicationYear;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
}

// ─── BookItem (Physical Copy) ──────────────────────────────────────

public class BookItem {
    private final String barcode;
    private final Book book;
    private BookStatus status;
    private String location; // shelf location

    public BookItem(String barcode, Book book, String location) {
        this.barcode = barcode;
        this.book = book;
        this.location = location;
        this.status = BookStatus.AVAILABLE;
    }

    public boolean isAvailable() { return status == BookStatus.AVAILABLE; }

    public void checkout() {
        if (!isAvailable()) throw new IllegalStateException("BookItem not available: " + barcode);
        status = BookStatus.BORROWED;
    }

    public void returnBook() { status = BookStatus.AVAILABLE; }

    public void reserve() { status = BookStatus.RESERVED; }

    public String getBarcode() { return barcode; }
    public Book getBook() { return book; }
    public BookStatus getStatus() { return status; }
}

// ─── Member ───────────────────────────────────────────────────────

public class Member {
    private final String memberId;
    private final String name;
    private final String email;
    private MemberStatus status;
    private int borrowedCount;
    private static final int MAX_BORROW_LIMIT = 5;

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.status = MemberStatus.ACTIVE;
        this.borrowedCount = 0;
    }

    public boolean canBorrow() {
        return status == MemberStatus.ACTIVE && borrowedCount < MAX_BORROW_LIMIT;
    }

    public void incrementBorrowCount() { borrowedCount++; }
    public void decrementBorrowCount() { borrowedCount--; }

    public String getMemberId() { return memberId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
}

// ─── Fine Strategy ────────────────────────────────────────────────

public interface FineStrategy {
    double calculateFine(long overdueDays);
}

public class PerDayFineStrategy implements FineStrategy {
    private static final double FINE_PER_DAY = 5.0; // ₹5 per day

    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * FINE_PER_DAY;
    }
}

// ─── Book Lending (Transaction) ───────────────────────────────────

public class BookLending {
    private final String lendingId;
    private final Member member;
    private final BookItem bookItem;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;

    public BookLending(Member member, BookItem bookItem, int loanDurationDays) {
        this.lendingId = UUID.randomUUID().toString();
        this.member = member;
        this.bookItem = bookItem;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(loanDurationDays);
    }

    public double processReturn(FineStrategy fineStrategy) {
        this.returnDate = LocalDate.now();
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        if (overdueDays > 0) {
            fineAmount = fineStrategy.calculateFine(overdueDays);
            System.out.println("Fine: ₹" + fineAmount + " for " + overdueDays + " days overdue");
        }
        return fineAmount;
    }

    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }

    public String getLendingId() { return lendingId; }
    public Member getMember() { return member; }
    public BookItem getBookItem() { return bookItem; }
    public LocalDate getDueDate() { return dueDate; }
}

// ─── Reservation ──────────────────────────────────────────────────

public class Reservation {
    private final String reservationId;
    private final Member member;
    private final Book book; // reserve a book (not specific copy)
    private final LocalDate reservationDate;
    private ReservationStatus status;

    public Reservation(Member member, Book book) {
        this.reservationId = UUID.randomUUID().toString();
        this.member = member;
        this.book = book;
        this.reservationDate = LocalDate.now();
        this.status = ReservationStatus.PENDING;
    }

    public void fulfill() { status = ReservationStatus.FULFILLED; }
    public void cancel() { status = ReservationStatus.CANCELLED; }

    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public ReservationStatus getStatus() { return status; }
}

// ─── Catalog (Search) ─────────────────────────────────────────────

public class Catalog {
    private final Map<String, Book> booksByIsbn;           // isbn → Book
    private final Map<String, List<Book>> booksByAuthor;   // author → Books
    private final Map<String, List<Book>> booksByTitle;    // title keyword → Books
    private final Map<String, List<BookItem>> itemsByIsbn; // isbn → BookItems

    public Catalog() {
        this.booksByIsbn = new HashMap<>();
        this.booksByAuthor = new HashMap<>();
        this.booksByTitle = new HashMap<>();
        this.itemsByIsbn = new HashMap<>();
    }

    public void addBook(Book book, List<BookItem> items) {
        booksByIsbn.put(book.getIsbn(), book);
        booksByAuthor.computeIfAbsent(book.getAuthor().toLowerCase(), k -> new ArrayList<>()).add(book);
        booksByTitle.computeIfAbsent(book.getTitle().toLowerCase(), k -> new ArrayList<>()).add(book);
        itemsByIsbn.computeIfAbsent(book.getIsbn(), k -> new ArrayList<>()).addAll(items);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(booksByIsbn.get(isbn));
    }

    public List<Book> searchByTitle(String titleKeyword) {
        return booksByTitle.entrySet().stream()
            .filter(e -> e.getKey().contains(titleKeyword.toLowerCase()))
            .flatMap(e -> e.getValue().stream())
            .toList();
    }

    public List<Book> searchByAuthor(String author) {
        return booksByAuthor.getOrDefault(author.toLowerCase(), Collections.emptyList());
    }

    public Optional<BookItem> getAvailableCopy(String isbn) {
        return itemsByIsbn.getOrDefault(isbn, Collections.emptyList()).stream()
            .filter(BookItem::isAvailable)
            .findFirst();
    }

    public List<BookItem> getAllCopies(String isbn) {
        return Collections.unmodifiableList(itemsByIsbn.getOrDefault(isbn, Collections.emptyList()));
    }
}

// ─── Library (Singleton + Facade) ─────────────────────────────────

public class Library {
    private static Library instance;

    private final Catalog catalog;
    private final FineStrategy fineStrategy;
    private final Map<String, BookLending> activeLenings;      // lendingId → lending
    private final Map<String, List<Reservation>> reservations; // isbn → reservations queue

    private static final int LOAN_DAYS = 14;

    private Library(FineStrategy fineStrategy) {
        this.catalog = new Catalog();
        this.fineStrategy = fineStrategy;
        this.activeLenings = new HashMap<>();
        this.reservations = new HashMap<>();
    }

    public static synchronized Library getInstance(FineStrategy fineStrategy) {
        if (instance == null) instance = new Library(fineStrategy);
        return instance;
    }

    public void addBookToCatalog(Book book, List<BookItem> items) {
        catalog.addBook(book, items);
    }

    public BookLending borrowBook(Member member, String isbn) {
        if (!member.canBorrow())
            throw new IllegalStateException("Member cannot borrow: " + member.getMemberId());

        BookItem item = catalog.getAvailableCopy(isbn)
            .orElseThrow(() -> new IllegalStateException("No available copies for ISBN: " + isbn));

        item.checkout();
        member.incrementBorrowCount();

        BookLending lending = new BookLending(member, item, LOAN_DAYS);
        activeLenings.put(lending.getLendingId(), lending);

        System.out.println(member.getName() + " borrowed: " + item.getBook().getTitle()
            + " | Due: " + lending.getDueDate());
        return lending;
    }

    public double returnBook(String lendingId) {
        BookLending lending = activeLenings.get(lendingId);
        if (lending == null) throw new IllegalArgumentException("Lending not found: " + lendingId);

        double fine = lending.processReturn(fineStrategy);
        lending.getBookItem().returnBook();
        lending.getMember().decrementBorrowCount();
        activeLenings.remove(lendingId);

        // Fulfill pending reservations
        checkAndFulfillReservation(lending.getBookItem().getBook().getIsbn());
        return fine;
    }

    public Reservation reserveBook(Member member, String isbn) {
        Book book = catalog.findByIsbn(isbn)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));

        Reservation reservation = new Reservation(member, book);
        reservations.computeIfAbsent(isbn, k -> new ArrayList<>()).add(reservation);
        System.out.println(member.getName() + " reserved: " + book.getTitle());
        return reservation;
    }

    private void checkAndFulfillReservation(String isbn) {
        List<Reservation> pending = reservations.getOrDefault(isbn, Collections.emptyList());
        if (pending.isEmpty()) return;

        Reservation nextReservation = pending.stream()
            .filter(r -> r.getStatus() == ReservationStatus.PENDING)
            .findFirst().orElse(null);

        if (nextReservation != null) {
            nextReservation.fulfill();
            System.out.println("Notifying " + nextReservation.getMember().getName()
                + " — reserved book is now available: " + nextReservation.getBook().getTitle());
            // In real system: send email/SMS via Observer
        }
    }

    public List<Book> searchByTitle(String title) { return catalog.searchByTitle(title); }
    public List<Book> searchByAuthor(String author) { return catalog.searchByAuthor(author); }
    public Catalog getCatalog() { return catalog; }
}
```

**Design Decisions Explained:**
- `Book` (metadata) vs `BookItem` (physical copy) — the most critical distinction
- `Catalog` separates search concern from Library's management concern (SRP)
- `FineStrategy` follows Strategy — easy to swap per-day / per-week / tiered fine logic
- `reservations` is a queue per ISBN — FIFO fulfillment
- `checkAndFulfillReservation` is called after each return — where Observer could notify members

---

## 9. Problem 6: Ride Sharing (Uber/Ola)

### Problem Statement
Design a ride-sharing system where riders can request rides, drivers can accept rides, and the system matches them based on proximity. Handle real-time tracking, pricing, and ride lifecycle.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
rider, driver, ride, vehicle, location, price, payment, rating, trip, match

**Apply 4 Archetypes:**
```
ACTOR:        Rider, Driver (both are users with different roles)
RESOURCE:     Driver (available drivers), Vehicle
TRANSACTION:  Ride (a trip from point A to B, has lifecycle)
COORDINATOR:  RideService (matches, dispatches, manages state)
EXTRA:        Location, PricingStrategy, DriverMatcher strategy
```

**Key Insight:**
- `Driver` has-a `Vehicle` and has-a `Location` (changes over time)
- `Ride` is a state machine: REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED / CANCELLED
- Driver matching is a policy → **Strategy Pattern**

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum RideStatus { REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED }

public enum DriverStatus { AVAILABLE, ON_TRIP, OFFLINE }

public enum RideType { ECONOMY, PREMIUM, POOL }

// ─── Location ─────────────────────────────────────────────────────

public class Location {
    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanceTo(Location other) {
        // Simplified Euclidean — in real system use Haversine formula
        double dlat = this.latitude - other.latitude;
        double dlon = this.longitude - other.longitude;
        return Math.sqrt(dlat * dlat + dlon * dlon) * 111; // ~km
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}

// ─── Vehicle ──────────────────────────────────────────────────────

public class Vehicle {
    private final String vehicleId;
    private final String plateNumber;
    private final String model;
    private final RideType type;
    private final int capacity;

    public Vehicle(String vehicleId, String plateNumber, String model, RideType type, int capacity) {
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.model = model;
        this.type = type;
        this.capacity = capacity;
    }

    public RideType getType() { return type; }
    public String getModel() { return model; }
    public String getPlateNumber() { return plateNumber; }
}

// ─── Rider ────────────────────────────────────────────────────────

public class Rider {
    private final String riderId;
    private final String name;
    private final String phone;
    private Location currentLocation;
    private double rating;

    public Rider(String riderId, String name, String phone, Location location) {
        this.riderId = riderId;
        this.name = name;
        this.phone = phone;
        this.currentLocation = location;
        this.rating = 5.0;
    }

    public String getRiderId() { return riderId; }
    public String getName() { return name; }
    public Location getCurrentLocation() { return currentLocation; }
    public void updateLocation(Location location) { this.currentLocation = location; }
}

// ─── Driver ───────────────────────────────────────────────────────

public class Driver {
    private final String driverId;
    private final String name;
    private final Vehicle vehicle;
    private Location currentLocation;
    private DriverStatus status;
    private double rating;

    public Driver(String driverId, String name, Vehicle vehicle, Location location) {
        this.driverId = driverId;
        this.name = name;
        this.vehicle = vehicle;
        this.currentLocation = location;
        this.status = DriverStatus.AVAILABLE;
        this.rating = 5.0;
    }

    public boolean isAvailable() { return status == DriverStatus.AVAILABLE; }

    public void setStatus(DriverStatus status) { this.status = status; }

    public void updateLocation(Location location) { this.currentLocation = location; }

    public String getDriverId() { return driverId; }
    public String getName() { return name; }
    public Vehicle getVehicle() { return vehicle; }
    public Location getCurrentLocation() { return currentLocation; }
    public double getRating() { return rating; }
}

// ─── Pricing Strategy ─────────────────────────────────────────────

public interface PricingStrategy {
    double calculateFare(double distanceKm, long durationMinutes, RideType rideType);
}

public class SurgePricingStrategy implements PricingStrategy {
    private final double surgeMultiplier;

    public SurgePricingStrategy(double surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(double distanceKm, long durationMinutes, RideType rideType) {
        double baseRate = switch (rideType) {
            case ECONOMY -> 10.0;
            case PREMIUM -> 20.0;
            case POOL    -> 6.0;
        };
        double base = baseRate * distanceKm + 2.0 * durationMinutes;
        return Math.round(base * surgeMultiplier * 100.0) / 100.0;
    }
}

// ─── Driver Matching Strategy ─────────────────────────────────────

public interface DriverMatchingStrategy {
    Optional<Driver> findDriver(List<Driver> drivers, Location pickup, RideType rideType);
}

public class NearestDriverStrategy implements DriverMatchingStrategy {
    private static final double MAX_DISTANCE_KM = 5.0;

    @Override
    public Optional<Driver> findDriver(List<Driver> drivers, Location pickup, RideType rideType) {
        return drivers.stream()
            .filter(Driver::isAvailable)
            .filter(d -> d.getVehicle().getType() == rideType)
            .filter(d -> d.getCurrentLocation().distanceTo(pickup) <= MAX_DISTANCE_KM)
            .min(Comparator.comparingDouble(d -> d.getCurrentLocation().distanceTo(pickup)));
    }
}

// ─── Ride (State Machine) ─────────────────────────────────────────

public class Ride {
    private final String rideId;
    private final Rider rider;
    private Driver driver;
    private final Location pickup;
    private final Location destination;
    private final RideType rideType;
    private RideStatus status;
    private LocalDateTime requestTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double fare;
    private Payment payment;

    public Ride(Rider rider, Location pickup, Location destination, RideType rideType) {
        this.rideId = UUID.randomUUID().toString();
        this.rider = rider;
        this.pickup = pickup;
        this.destination = destination;
        this.rideType = rideType;
        this.status = RideStatus.REQUESTED;
        this.requestTime = LocalDateTime.now();
    }

    public void accept(Driver driver) {
        if (status != RideStatus.REQUESTED) throw new IllegalStateException("Ride not in REQUESTED state");
        this.driver = driver;
        this.status = RideStatus.ACCEPTED;
        driver.setStatus(DriverStatus.ON_TRIP);
        System.out.println("Driver " + driver.getName() + " accepted ride " + rideId);
    }

    public void start() {
        if (status != RideStatus.ACCEPTED) throw new IllegalStateException("Ride must be ACCEPTED first");
        this.status = RideStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
    }

    public void complete(PricingStrategy pricingStrategy) {
        if (status != RideStatus.IN_PROGRESS) throw new IllegalStateException("Ride not in progress");
        this.endTime = LocalDateTime.now();
        this.status = RideStatus.COMPLETED;
        driver.setStatus(DriverStatus.AVAILABLE);

        double distanceKm = pickup.distanceTo(destination);
        long durationMins = ChronoUnit.MINUTES.between(startTime, endTime);
        this.fare = pricingStrategy.calculateFare(distanceKm, durationMins, rideType);

        this.payment = new Payment(UUID.randomUUID().toString(), fare);
        this.payment.process();
        System.out.println("Ride completed. Fare: ₹" + fare);
    }

    public void cancel() {
        if (status == RideStatus.COMPLETED) throw new IllegalStateException("Cannot cancel completed ride");
        this.status = RideStatus.CANCELLED;
        if (driver != null) driver.setStatus(DriverStatus.AVAILABLE);
    }

    public String getRideId() { return rideId; }
    public RideStatus getStatus() { return status; }
    public double getFare() { return fare; }
    public Rider getRider() { return rider; }
    public Driver getDriver() { return driver; }
}

// ─── Ride Service (Coordinator) ───────────────────────────────────

public class RideService {
    private static RideService instance;

    private final List<Driver> allDrivers;
    private final DriverMatchingStrategy matchingStrategy;
    private final PricingStrategy pricingStrategy;
    private final Map<String, Ride> activeRides; // rideId → ride

    private RideService(DriverMatchingStrategy matchingStrategy, PricingStrategy pricingStrategy) {
        this.allDrivers = new ArrayList<>();
        this.matchingStrategy = matchingStrategy;
        this.pricingStrategy = pricingStrategy;
        this.activeRides = new ConcurrentHashMap<>();
    }

    public static synchronized RideService getInstance(DriverMatchingStrategy matching,
                                                        PricingStrategy pricing) {
        if (instance == null) instance = new RideService(matching, pricing);
        return instance;
    }

    public void registerDriver(Driver driver) {
        allDrivers.add(driver);
    }

    public Ride requestRide(Rider rider, Location destination, RideType rideType) {
        Ride ride = new Ride(rider, rider.getCurrentLocation(), destination, rideType);

        Optional<Driver> driver = matchingStrategy.findDriver(allDrivers, rider.getCurrentLocation(), rideType);
        if (driver.isEmpty()) {
            System.out.println("No drivers available near " + rider.getName());
            ride.cancel();
            return ride;
        }

        ride.accept(driver.get());
        activeRides.put(ride.getRideId(), ride);
        return ride;
    }

    public void startRide(String rideId) {
        Ride ride = getActiveRide(rideId);
        ride.start();
    }

    public double completeRide(String rideId) {
        Ride ride = getActiveRide(rideId);
        ride.complete(pricingStrategy);
        activeRides.remove(rideId);
        return ride.getFare();
    }

    public void cancelRide(String rideId) {
        Ride ride = getActiveRide(rideId);
        ride.cancel();
        activeRides.remove(rideId);
    }

    private Ride getActiveRide(String rideId) {
        Ride ride = activeRides.get(rideId);
        if (ride == null) throw new IllegalArgumentException("Ride not found: " + rideId);
        return ride;
    }
}
```

**Design Decisions Explained:**
- `Ride` is a state machine — `REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED/CANCELLED`
- `PricingStrategy` — can switch to SurgePricing, FlatRate, PoolPricing
- `DriverMatchingStrategy` — can switch to NearestFirst, HighestRated, AI-based
- `RideService` is Singleton — single coordinator for all rides
- `Driver.setStatus(ON_TRIP)` is called atomically in `ride.accept()` — prevents double-booking

---

## 10. Problem 7: Vending Machine

### Problem Statement
Design a vending machine that accepts coins, allows product selection, dispenses products, returns change, and handles edge cases like insufficient balance, out-of-stock items.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
machine, product, slot, coin, inventory, payment, change, state

**Key Insight:**
- A Vending Machine is a **classic State Machine problem** — behavior changes completely based on state
- States: IDLE → HAS_MONEY → PRODUCT_SELECTED → DISPENSING → RETURNING_CHANGE
- The State Pattern is the natural fit here

**What Varies?**
- Behavior on button press depends on state → **State Pattern**
- Change calculation algorithm → can vary → **Strategy**

---

### Full Java Implementation

```java
// ─── Product ──────────────────────────────────────────────────────

public class Product {
    private final String productId;
    private final String name;
    private final double price;

    public Product(String productId, String name, double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}

// ─── Inventory ────────────────────────────────────────────────────

public class Inventory {
    private final Map<String, Product> products;    // productId → Product
    private final Map<String, Integer> stockCount;  // productId → count

    public Inventory() {
        this.products = new HashMap<>();
        this.stockCount = new HashMap<>();
    }

    public void addProduct(Product product, int quantity) {
        products.put(product.getProductId(), product);
        stockCount.merge(product.getProductId(), quantity, Integer::sum);
    }

    public boolean isAvailable(String productId) {
        return stockCount.getOrDefault(productId, 0) > 0;
    }

    public Product getProduct(String productId) {
        return products.get(productId);
    }

    public void dispense(String productId) {
        if (!isAvailable(productId)) throw new IllegalStateException("Out of stock: " + productId);
        stockCount.merge(productId, -1, Integer::sum);
    }

    public void displayProducts() {
        System.out.println("\n=== Products ===");
        products.values().forEach(p ->
            System.out.printf("[%s] %s - ₹%.2f (Stock: %d)%n",
                p.getProductId(), p.getName(), p.getPrice(),
                stockCount.getOrDefault(p.getProductId(), 0)));
    }
}

// ─── Vending Machine State Interface ──────────────────────────────

public interface VendingMachineState {
    void insertCoin(VendingMachine machine, double amount);
    void selectProduct(VendingMachine machine, String productId);
    void dispenseProduct(VendingMachine machine);
    void returnChange(VendingMachine machine);
}

// ─── Concrete States ──────────────────────────────────────────────

public class IdleState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachine machine, double amount) {
        machine.addBalance(amount);
        System.out.println("Inserted ₹" + amount + ". Balance: ₹" + machine.getBalance());
        machine.setState(machine.getHasMoneyState());
    }

    @Override
    public void selectProduct(VendingMachine machine, String productId) {
        System.out.println("Please insert coins first.");
    }

    @Override
    public void dispenseProduct(VendingMachine machine) {
        System.out.println("Please insert coins and select a product first.");
    }

    @Override
    public void returnChange(VendingMachine machine) {
        System.out.println("No balance to return.");
    }
}

public class HasMoneyState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachine machine, double amount) {
        machine.addBalance(amount);
        System.out.println("Inserted ₹" + amount + ". Total balance: ₹" + machine.getBalance());
    }

    @Override
    public void selectProduct(VendingMachine machine, String productId) {
        Product product = machine.getInventory().getProduct(productId);
        if (product == null) {
            System.out.println("Product not found: " + productId);
            return;
        }
        if (!machine.getInventory().isAvailable(productId)) {
            System.out.println(product.getName() + " is out of stock.");
            return;
        }
        if (machine.getBalance() < product.getPrice()) {
            System.out.printf("Insufficient balance. Need ₹%.2f, have ₹%.2f%n",
                product.getPrice(), machine.getBalance());
            return;
        }
        machine.setSelectedProduct(product);
        System.out.println("Selected: " + product.getName() + " (₹" + product.getPrice() + ")");
        machine.setState(machine.getProductSelectedState());
    }

    @Override
    public void dispenseProduct(VendingMachine machine) {
        System.out.println("Please select a product first.");
    }

    @Override
    public void returnChange(VendingMachine machine) {
        System.out.printf("Returning ₹%.2f%n", machine.getBalance());
        machine.resetBalance();
        machine.setState(machine.getIdleState());
    }
}

public class ProductSelectedState implements VendingMachineState {
    @Override
    public void insertCoin(VendingMachine machine, double amount) {
        System.out.println("Product already selected. Press dispense.");
    }

    @Override
    public void selectProduct(VendingMachine machine, String productId) {
        System.out.println("Product already selected. Press dispense or return change.");
    }

    @Override
    public void dispenseProduct(VendingMachine machine) {
        Product product = machine.getSelectedProduct();
        machine.getInventory().dispense(product.getProductId());
        machine.deductBalance(product.getPrice());

        System.out.println("Dispensing: " + product.getName());

        if (machine.getBalance() > 0) {
            System.out.printf("Returning change: ₹%.2f%n", machine.getBalance());
        }
        machine.resetBalance();
        machine.setSelectedProduct(null);
        machine.setState(machine.getIdleState());
        System.out.println("Thank you!");
    }

    @Override
    public void returnChange(VendingMachine machine) {
        System.out.printf("Cancelling. Returning ₹%.2f%n", machine.getBalance());
        machine.resetBalance();
        machine.setSelectedProduct(null);
        machine.setState(machine.getIdleState());
    }
}

// ─── Vending Machine (Context) ───────────────────────────────────

public class VendingMachine {
    private VendingMachineState currentState;
    private double balance;
    private Product selectedProduct;
    private final Inventory inventory;

    // State instances (shared — they're stateless)
    private final VendingMachineState idleState = new IdleState();
    private final VendingMachineState hasMoneyState = new HasMoneyState();
    private final VendingMachineState productSelectedState = new ProductSelectedState();

    public VendingMachine(Inventory inventory) {
        this.inventory = inventory;
        this.balance = 0;
        this.currentState = idleState;
    }

    // Context delegates to current state
    public void insertCoin(double amount) { currentState.insertCoin(this, amount); }
    public void selectProduct(String productId) { currentState.selectProduct(this, productId); }
    public void dispenseProduct() { currentState.dispenseProduct(this); }
    public void returnChange() { currentState.returnChange(this); }

    // Package-level state manipulation (called by state objects)
    public void setState(VendingMachineState state) { this.currentState = state; }
    public void addBalance(double amount) { balance += amount; }
    public void deductBalance(double amount) { balance -= amount; }
    public void resetBalance() { balance = 0; }
    public void setSelectedProduct(Product product) { this.selectedProduct = product; }

    public double getBalance() { return balance; }
    public Product getSelectedProduct() { return selectedProduct; }
    public Inventory getInventory() { return inventory; }

    // State getters for transitions
    public VendingMachineState getIdleState() { return idleState; }
    public VendingMachineState getHasMoneyState() { return hasMoneyState; }
    public VendingMachineState getProductSelectedState() { return productSelectedState; }
}

/*
Demo:
VendingMachine vm = new VendingMachine(inventory);
vm.selectProduct("C1");           // "Please insert coins first"
vm.insertCoin(20);                // Balance: ₹20
vm.insertCoin(10);                // Balance: ₹30
vm.selectProduct("C1");          // Selects "Chips" (₹25)
vm.dispenseProduct();            // Dispenses, returns ₹5 change
*/
```

---

## 11. Problem 8: LRU Cache

### Problem Statement
Design and implement an LRU (Least Recently Used) cache with O(1) get and O(1) put operations.

---

### 🧠 Entity Identification Walk-Through

**Key Insight:** This is different from other LLD problems — it's more data-structure-focused.
- Two data structures working together: `HashMap` (O(1) lookup) + `Doubly Linked List` (O(1) move-to-front)
- The `Node` is your core entity: key, value, prev, next pointers

---

### Full Java Implementation

```java
// ─── DLL Node ─────────────────────────────────────────────────────

class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;

    Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

// ─── LRU Cache ───────────────────────────────────────────────────

public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map; // key → Node for O(1) lookup
    // DLL: head = MRU end, tail = LRU end
    private final Node<K, V> head; // dummy head
    private final Node<K, V> tail; // dummy tail

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        // Initialize dummy head and tail
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) return null;
        moveToFront(node);  // Mark as recently used
        return node.value;
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            Node<K, V> node = map.get(key);
            node.value = value;
            moveToFront(node);
        } else {
            if (map.size() == capacity) {
                // Evict LRU (node just before dummy tail)
                Node<K, V> lru = tail.prev;
                removeNode(lru);
                map.remove(lru.key);
            }
            Node<K, V> newNode = new Node<>(key, value);
            insertAtFront(newNode);
            map.put(key, newNode);
        }
    }

    private void moveToFront(Node<K, V> node) {
        removeNode(node);
        insertAtFront(node);
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insertAtFront(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    public int size() { return map.size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<K, V> curr = head.next;
        while (curr != tail) {
            sb.append(curr.key).append(":").append(curr.value);
            if (curr.next != tail) sb.append(", ");
            curr = curr.next;
        }
        sb.append("] (MRU → LRU)");
        return sb.toString();
    }
}

// Thread-safe version using ReadWriteLock
public class ConcurrentLRUCache<K, V> {
    private final LRUCache<K, V> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ConcurrentLRUCache(int capacity) {
        this.cache = new LRUCache<>(capacity);
    }

    public V get(K key) {
        lock.writeLock().lock(); // write because moveToFront modifies structure
        try {
            return cache.get(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

---

## 12. Problem 9: Hotel Management System

### Problem Statement
Design a hotel management system with room booking, check-in/check-out, billing, different room types, and availability management.

---

### 🧠 Entity Identification Walk-Through

```
ACTOR:        Guest
RESOURCE:     Room (has type, floor, amenities, status)
TRANSACTION:  Booking (reservation), CheckIn, Checkout, Bill
COORDINATOR:  Hotel (Singleton), ReservationService
EXTRA:        RoomType, RoomStatus, HousekeepingStatus
```

**Key Insight:**
- Room has two separate concerns: **booking availability** and **housekeeping status** — model separately
- A Booking becomes a StayRecord after check-in

---

### Full Java Implementation

```java
public enum RoomType { STANDARD, DELUXE, SUITE }

public enum RoomStatus { AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE }

public enum HousekeepingStatus { CLEAN, DIRTY, IN_PROGRESS }

public class Room {
    private final String roomId;
    private final int floor;
    private final RoomType type;
    private final double pricePerNight;
    private final List<String> amenities;
    private RoomStatus status;
    private HousekeepingStatus housekeepingStatus;

    public Room(String roomId, int floor, RoomType type, double pricePerNight, List<String> amenities) {
        this.roomId = roomId;
        this.floor = floor;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.amenities = amenities;
        this.status = RoomStatus.AVAILABLE;
        this.housekeepingStatus = HousekeepingStatus.CLEAN;
    }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE && housekeepingStatus == HousekeepingStatus.CLEAN; }

    public void reserve() { status = RoomStatus.RESERVED; }
    public void checkIn() { status = RoomStatus.OCCUPIED; }
    public void checkOut() {
        status = RoomStatus.AVAILABLE;
        housekeepingStatus = HousekeepingStatus.DIRTY; // needs cleaning after checkout
    }

    public String getRoomId() { return roomId; }
    public RoomType getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public RoomStatus getStatus() { return status; }
}

public class Guest {
    private final String guestId;
    private final String name;
    private final String phone;
    private final String idProof;

    public Guest(String guestId, String name, String phone, String idProof) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
        this.idProof = idProof;
    }

    public String getGuestId() { return guestId; }
    public String getName() { return name; }
}

public class HotelBooking {
    private final String bookingId;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private BookingStatus status;

    public HotelBooking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.bookingId = UUID.randomUUID().toString();
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.status = BookingStatus.CONFIRMED;
    }

    public double calculateBill() {
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return nights * room.getPricePerNight();
    }

    public void cancel() { status = BookingStatus.CANCELLED; room.checkOut(); }

    public String getBookingId() { return bookingId; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public BookingStatus getStatus() { return status; }
}

public class Hotel {
    private static Hotel instance;
    private final String name;
    private final List<Room> rooms;
    private final Map<String, HotelBooking> bookings;

    private Hotel(String name, List<Room> rooms) {
        this.name = name;
        this.rooms = rooms;
        this.bookings = new ConcurrentHashMap<>();
    }

    public static synchronized Hotel getInstance(String name, List<Room> rooms) {
        if (instance == null) instance = new Hotel(name, rooms);
        return instance;
    }

    public List<Room> searchAvailableRooms(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        return rooms.stream()
            .filter(r -> r.getType() == type)
            .filter(r -> r.isAvailable())
            // In real system: check against booking table for date ranges
            .toList();
    }

    public HotelBooking bookRoom(Guest guest, String roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = rooms.stream()
            .filter(r -> r.getRoomId().equals(roomId) && r.isAvailable())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Room not available: " + roomId));

        room.reserve();
        HotelBooking booking = new HotelBooking(guest, room, checkIn, checkOut);
        bookings.put(booking.getBookingId(), booking);

        System.out.println("Booking confirmed: " + booking.getBookingId()
            + " | Room: " + roomId + " | Guest: " + guest.getName());
        return booking;
    }

    public void checkIn(String bookingId) {
        HotelBooking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found");
        booking.getRoom().checkIn();
        System.out.println("Checked in: " + booking.getGuest().getName());
    }

    public double checkOut(String bookingId) {
        HotelBooking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found");
        double bill = booking.calculateBill();
        booking.getRoom().checkOut();
        bookings.remove(bookingId);
        System.out.println("Checked out: " + booking.getGuest().getName() + " | Bill: ₹" + bill);
        return bill;
    }
}
```

---

## 13. Problem 10: Splitwise / Expense Sharing

### Problem Statement
Design an expense-sharing application where users can add expenses in a group, split them equally or by percentage or exact amounts, and track who owes whom how much.

---

### 🧠 Entity Identification Walk-Through

```
ACTOR:        User (payer + participants)
RESOURCE:     Group (collection of users)
TRANSACTION:  Expense (who paid, how much, split among whom)
COORDINATOR:  ExpenseService, BalanceSheet (tracks net amounts)
EXTRA:        SplitStrategy (Equal/Exact/Percentage)
```

**Key Insight:**
- Different split types → **Strategy Pattern** for `SplitStrategy`
- The balance between two users is just a net number — use a map `userId → Map<userId, amount>`
- A negative balance means you owe; positive means you're owed

---

### Full Java Implementation

```java
// ─── Split Strategy ───────────────────────────────────────────────

public interface SplitStrategy {
    // Returns map: userId → amount they owe
    Map<String, Double> calculateSplits(double totalAmount, List<String> participantIds, Map<String, Object> params);

    void validate(double totalAmount, List<String> participantIds, Map<String, Object> params);
}

public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<String, Double> calculateSplits(double totalAmount, List<String> participantIds, Map<String, Object> params) {
        double share = Math.round((totalAmount / participantIds.size()) * 100.0) / 100.0;
        Map<String, Double> splits = new HashMap<>();
        participantIds.forEach(id -> splits.put(id, share));
        return splits;
    }

    @Override
    public void validate(double totalAmount, List<String> participantIds, Map<String, Object> params) {
        if (participantIds.isEmpty()) throw new IllegalArgumentException("Need at least 1 participant");
    }
}

public class ExactSplitStrategy implements SplitStrategy {
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> calculateSplits(double totalAmount, List<String> participantIds, Map<String, Object> params) {
        return (Map<String, Double>) params.get("exactAmounts");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(double totalAmount, List<String> participantIds, Map<String, Object> params) {
        Map<String, Double> exactAmounts = (Map<String, Double>) params.get("exactAmounts");
        double sum = exactAmounts.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - totalAmount) > 0.01)
            throw new IllegalArgumentException("Exact amounts must sum to total: " + totalAmount);
    }
}

public class PercentageSplitStrategy implements SplitStrategy {
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> calculateSplits(double totalAmount, List<String> participantIds, Map<String, Object> params) {
        Map<String, Double> percentages = (Map<String, Double>) params.get("percentages");
        Map<String, Double> splits = new HashMap<>();
        percentages.forEach((id, pct) -> splits.put(id, Math.round(totalAmount * pct / 100.0 * 100.0) / 100.0));
        return splits;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(double totalAmount, List<String> participantIds, Map<String, Object> params) {
        Map<String, Double> percentages = (Map<String, Double>) params.get("percentages");
        double sum = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 100.0) > 0.01)
            throw new IllegalArgumentException("Percentages must sum to 100. Got: " + sum);
    }
}

// ─── Expense ──────────────────────────────────────────────────────

public class Expense {
    private final String expenseId;
    private final String paidByUserId;
    private final double amount;
    private final String description;
    private final Map<String, Double> splits; // userId → amount owed
    private final LocalDateTime createdAt;

    public Expense(String paidByUserId, double amount, String description,
                   Map<String, Double> splits) {
        this.expenseId = UUID.randomUUID().toString();
        this.paidByUserId = paidByUserId;
        this.amount = amount;
        this.description = description;
        this.splits = splits;
        this.createdAt = LocalDateTime.now();
    }

    public String getExpenseId() { return expenseId; }
    public String getPaidByUserId() { return paidByUserId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public Map<String, Double> getSplits() { return Collections.unmodifiableMap(splits); }
}

// ─── Group ────────────────────────────────────────────────────────

public class Group {
    private final String groupId;
    private final String name;
    private final List<User> members;
    private final List<Expense> expenses;

    public Group(String groupId, String name) {
        this.groupId = groupId;
        this.name = name;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public void addMember(User user) { members.add(user); }
    public void addExpense(Expense expense) { expenses.add(expense); }

    public String getGroupId() { return groupId; }
    public List<User> getMembers() { return Collections.unmodifiableList(members); }
    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }
}

// ─── Balance Sheet ────────────────────────────────────────────────

public class BalanceSheet {
    // balances.get(A).get(B) = how much A owes B (positive = A owes B, negative = B owes A)
    private final Map<String, Map<String, Double>> balances;

    public BalanceSheet() {
        this.balances = new HashMap<>();
    }

    public void updateBalance(String paidBy, Map<String, Double> splits) {
        for (Map.Entry<String, Double> entry : splits.entrySet()) {
            String owerId = entry.getKey();
            double amount = entry.getValue();

            if (owerId.equals(paidBy)) continue; // payer doesn't owe themselves

            // owerId owes paidBy this amount
            balances.computeIfAbsent(owerId, k -> new HashMap<>())
                .merge(paidBy, amount, Double::sum);

            // Mirror: paidBy is owed by owerId
            balances.computeIfAbsent(paidBy, k -> new HashMap<>())
                .merge(owerId, -amount, Double::sum);
        }
    }

    public double getBalance(String fromUserId, String toUserId) {
        return balances.getOrDefault(fromUserId, Collections.emptyMap())
            .getOrDefault(toUserId, 0.0);
    }

    public void printBalances(Map<String, String> userNames) {
        System.out.println("\n=== Balances ===");
        balances.forEach((fromId, toMap) ->
            toMap.forEach((toId, amount) -> {
                if (amount > 0.01) {
                    System.out.printf("%s owes %s ₹%.2f%n",
                        userNames.get(fromId), userNames.get(toId), amount);
                }
            })
        );
    }
}

// ─── Expense Service ──────────────────────────────────────────────

public class ExpenseService {
    private static ExpenseService instance;

    private final Map<String, User> users;
    private final Map<String, Group> groups;
    private final BalanceSheet balanceSheet;

    private ExpenseService() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.balanceSheet = new BalanceSheet();
    }

    public static synchronized ExpenseService getInstance() {
        if (instance == null) instance = new ExpenseService();
        return instance;
    }

    public void registerUser(User user) { users.put(user.getUserId(), user); }
    public void registerGroup(Group group) { groups.put(group.getGroupId(), group); }

    public Expense addExpense(String groupId, String paidByUserId, double amount,
                              String description, List<String> participantIds,
                              SplitStrategy strategy, Map<String, Object> params) {
        strategy.validate(amount, participantIds, params);
        Map<String, Double> splits = strategy.calculateSplits(amount, participantIds, params);

        Expense expense = new Expense(paidByUserId, amount, description, splits);

        Group group = groups.get(groupId);
        if (group != null) group.addExpense(expense);

        balanceSheet.updateBalance(paidByUserId, splits);

        System.out.printf("Expense added: \"%s\" ₹%.2f paid by %s%n",
            description, amount, users.get(paidByUserId).getName());
        return expense;
    }

    public void printBalances() {
        Map<String, String> names = new HashMap<>();
        users.forEach((id, user) -> names.put(id, user.getName()));
        balanceSheet.printBalances(names);
    }
}

// ─── User (simple) ────────────────────────────────────────────────
public class User {
    private final String userId;
    private final String name;

    public User(String userId, String name) { this.userId = userId; this.name = name; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
}
```

---

## 14. Common Mistakes & Anti-Patterns

### ❌ Mistake 1: Making Everything a God Class
```java
// BAD: ParkingLot doing everything
class ParkingLot {
    void park(Vehicle v) { ... }
    double calculateFee(Vehicle v) { ... }    // should be in Strategy
    void sendNotification(String msg) { ... } // should be in Notifier
    void saveToDb() { ... }                   // should be in Repository
}
```

### ❌ Mistake 2: Missing the Book vs BookItem Distinction
Every system with a "thing" that can have multiple copies needs this pattern:
- Book ↔ BookItem (Library)
- Movie ↔ Show ↔ ShowSeat (BookMyShow)  
- Product ↔ ProductSlot (Vending Machine)

### ❌ Mistake 3: Using Inheritance When Composition Fits Better
```java
// BAD: Car IS-A Vehicle IS-A ParkingSpot (wrong!)
// GOOD: Car IS-A Vehicle, ParkingSpot HAS-A SpotType
```

### ❌ Mistake 4: Forgetting Enums for State
Always model state as enums. Never use `String` or `boolean` for multi-state entities.

### ❌ Mistake 5: No Thread Safety on Shared State
For any booking system, seat allocation must be `synchronized` or use atomic operations.

### ❌ Mistake 6: Premature Pattern Application
Don't force Factory, Builder, etc. unless there's a real need. State your reasoning.

### ❌ Mistake 7: No Edge Case Thinking
Always consider:
- What if the resource is full? (Parking lot, vending machine out of stock)
- What if the transaction fails midway? (Booking, payment fails)
- What if the user cancels? (Ride, booking)
- Concurrent requests for the same resource

---

## 15. Quick Entity Identification Practice Table

Use this as a warm-up exercise. Fill in the archetypes before looking at the implementations:

| Problem | Actor | Resource | Transaction | Coordinator |
|---|---|---|---|---|
| Parking Lot | Vehicle | ParkingSpot | ParkingTicket | ParkingLot |
| BookMyShow | User | ShowSeat | Booking | BookingService |
| Snake & Ladder | Player | Board | Turn | Game |
| Elevator | Passenger | Elevator | ElevatorRequest | ElevatorController |
| Library | Member | BookItem | BookLending | Library |
| Uber/Ola | Rider, Driver | Driver (available) | Ride | RideService |
| Vending Machine | Customer | ProductSlot | Purchase | VendingMachine |
| LRU Cache | Client | CacheEntry | Get/Put | LRUCache |
| Hotel | Guest | Room | HotelBooking | Hotel |
| Splitwise | User | — | Expense | ExpenseService |
| Chess | Player | Piece/Board | Move | Game |
| ATM | User | Account | Transaction | ATM |
| Food Delivery | Customer, Restaurant, DeliveryPerson | FoodItem | Order | OrderService |
| Task Manager | User/Team | Task/Board | TaskUpdate | TaskService |

---

## Quick Revision Checklist Before Interview

```
□ Read problem → extract nouns
□ Classify as: Actor / Resource / Transaction / Coordinator
□ Define enums for all states
□ Ask "What Varies?" → pick patterns
□ Sketch class diagram (5 min)
□ Code the most important 2-3 classes fully
□ Add synchronized/locks where concurrent access exists
□ Handle edge cases: full, empty, not found, already exists
□ State design decisions OUT LOUD during interview
□ Suggest extension points at the end
```

---

---

# 🧠 LLD Complete Guide — Part 2 (Problems 11–20)

> Paste this directly after the closing line of Part 1. Problems continue from 11.

---

## Table of Contents (Part 2)

11. [Problem 11: KYC Service System (Bank)](#11-problem-11-kyc-service-system-bank)
12. [Problem 12: Metering Service (Adobe Acrobat / Quota System)](#12-problem-12-metering-service-adobe-acrobat--quota-system)
13. [Problem 13: Job Scheduler](#13-problem-13-job-scheduler)
14. [Problem 14: API Rate Limiter](#14-problem-14-api-rate-limiter)
15. [Problem 15: Notification Service](#15-problem-15-notification-service)
16. [Problem 16: Chess Game](#16-problem-16-chess-game)
17. [Problem 17: ATM Machine](#17-problem-17-atm-machine)
18. [Problem 18: E-commerce Order Management](#18-problem-18-e-commerce-order-management)
19. [Problem 19: Log Aggregation & Monitoring System](#19-problem-19-log-aggregation--monitoring-system)
20. [Problem 20: Distributed Cache with Invalidation](#20-problem-20-distributed-cache-with-invalidation)

---

## 11. Problem 11: KYC Service System (Bank)

### Problem Statement
Design a Know Your Customer (KYC) service for a bank where customers submit identity documents (Aadhaar, PAN, passport, etc.), the system validates them through multiple verification stages, assigns risk levels, approves or rejects KYC applications, and supports re-verification triggers.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
customer, KYC application, document, verification step, verifier, risk score, reviewer, status, audit log

**Apply 4 Archetypes:**
```
ACTOR:        Customer (submits), Reviewer/Analyst (manual review)
RESOURCE:     Document (Aadhaar, PAN, Passport — each has type + status)
TRANSACTION:  KYCApplication (the full lifecycle entity)
              VerificationStep (individual check result within an application)
COORDINATOR:  KYCService (orchestrates pipeline), VerificationPipeline
EXTRA:        RiskScorer, DocumentValidator, AuditLog
```

**Key Insights:**
- A KYC application goes through a **pipeline** of verifications — document check → liveness check → CIBIL/risk check → manual review
- Each stage is an independent step → **Chain of Responsibility Pattern**
- KYC status is a state machine → **State Pattern** (or enum + transitions)
- Different document types have different validation rules → **Strategy Pattern**
- Every state change must be logged → **Observer / Audit Log**

**What Varies?**
- Verification pipeline steps vary by customer type (individual vs business) → **Builder for pipeline**
- Document validation logic varies per document type → **Strategy**
- Risk scoring algorithm can change → **Strategy**

---

### Class Diagram (ASCII)
```
Customer ────────────────┐
                         ▼
                   KYCApplication ──── List<Document>
                         │                    │
                    KYCStatus           DocumentType
                    (state machine)     (AADHAAR/PAN/PASSPORT)
                         │
                   List<VerificationStep>
                         │
                VerificationPipeline
                    │         │
              Handler 1    Handler 2  ...  (Chain of Responsibility)
           DocValidator  LivenessCheck  RiskScorer  ManualReview
                         │
                     AuditLog (Observer)
```

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum DocumentType {
    AADHAAR, PAN, PASSPORT, DRIVING_LICENSE, VOTER_ID
}

public enum DocumentStatus {
    SUBMITTED, UNDER_REVIEW, VERIFIED, REJECTED, EXPIRED
}

public enum KYCStatus {
    INITIATED, DOCUMENTS_SUBMITTED, UNDER_VERIFICATION,
    PENDING_MANUAL_REVIEW, APPROVED, REJECTED, EXPIRED
}

public enum RiskLevel { LOW, MEDIUM, HIGH, VERY_HIGH }

public enum CustomerType { INDIVIDUAL, BUSINESS, NRI }

// ─── Document ─────────────────────────────────────────────────────

public class Document {
    private final String documentId;
    private final DocumentType type;
    private final String documentNumber;
    private final String fileUrl;         // S3 path in real system
    private final LocalDate expiryDate;
    private DocumentStatus status;
    private String rejectionReason;

    public Document(String documentId, DocumentType type, String documentNumber,
                    String fileUrl, LocalDate expiryDate) {
        this.documentId = documentId;
        this.type = type;
        this.documentNumber = documentNumber;
        this.fileUrl = fileUrl;
        this.expiryDate = expiryDate;
        this.status = DocumentStatus.SUBMITTED;
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    public void verify() { this.status = DocumentStatus.VERIFIED; }

    public void reject(String reason) {
        this.status = DocumentStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public String getDocumentId() { return documentId; }
    public DocumentType getType() { return type; }
    public String getDocumentNumber() { return documentNumber; }
    public DocumentStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDate getExpiryDate() { return expiryDate; }
}

// ─── Audit Log Entry ──────────────────────────────────────────────

public class AuditLogEntry {
    private final String applicationId;
    private final String action;
    private final String performedBy;    // system or reviewer ID
    private final String details;
    private final LocalDateTime timestamp;

    public AuditLogEntry(String applicationId, String action, String performedBy, String details) {
        this.applicationId = applicationId;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | App: %s | By: %s | %s",
            timestamp, action, applicationId, performedBy, details);
    }
}

// ─── KYC Application ──────────────────────────────────────────────

public class KYCApplication {
    private final String applicationId;
    private final Customer customer;
    private KYCStatus status;
    private RiskLevel riskLevel;
    private final List<Document> documents;
    private final List<VerificationStepResult> verificationResults;
    private final List<AuditLogEntry> auditTrail;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    public KYCApplication(Customer customer) {
        this.applicationId = UUID.randomUUID().toString();
        this.customer = customer;
        this.status = KYCStatus.INITIATED;
        this.documents = new ArrayList<>();
        this.verificationResults = new ArrayList<>();
        this.auditTrail = new ArrayList<>();
        this.riskLevel = RiskLevel.LOW;
    }

    public void addDocument(Document doc) {
        documents.add(doc);
        if (status == KYCStatus.INITIATED) {
            status = KYCStatus.DOCUMENTS_SUBMITTED;
            submittedAt = LocalDateTime.now();
        }
        log("DOCUMENT_ADDED", "SYSTEM", "Document: " + doc.getType() + " | ID: " + doc.getDocumentId());
    }

    public void transitionTo(KYCStatus newStatus, String performedBy, String reason) {
        log("STATUS_CHANGE", performedBy,
            "From: " + this.status + " → To: " + newStatus + (reason != null ? " | Reason: " + reason : ""));
        this.status = newStatus;
        if (newStatus == KYCStatus.APPROVED || newStatus == KYCStatus.REJECTED) {
            this.completedAt = LocalDateTime.now();
            if (newStatus == KYCStatus.REJECTED) this.rejectionReason = reason;
        }
    }

    public void addVerificationResult(VerificationStepResult result) {
        verificationResults.add(result);
        log("VERIFICATION_STEP", result.getStepName(),
            "Passed: " + result.isPassed() + " | " + result.getDetails());
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        log("RISK_UPDATE", "RISK_ENGINE", "Risk level set to: " + riskLevel);
        this.riskLevel = riskLevel;
    }

    private void log(String action, String performedBy, String details) {
        auditTrail.add(new AuditLogEntry(applicationId, action, performedBy, details));
    }

    public boolean hasDocument(DocumentType type) {
        return documents.stream().anyMatch(d -> d.getType() == type);
    }

    public Optional<Document> getDocument(DocumentType type) {
        return documents.stream().filter(d -> d.getType() == type).findFirst();
    }

    public String getApplicationId() { return applicationId; }
    public Customer getCustomer() { return customer; }
    public KYCStatus getStatus() { return status; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public List<Document> getDocuments() { return Collections.unmodifiableList(documents); }
    public List<AuditLogEntry> getAuditTrail() { return Collections.unmodifiableList(auditTrail); }
    public String getRejectionReason() { return rejectionReason; }
}

// ─── Verification Step Result ─────────────────────────────────────

public class VerificationStepResult {
    private final String stepName;
    private final boolean passed;
    private final String details;
    private final LocalDateTime executedAt;

    public VerificationStepResult(String stepName, boolean passed, String details) {
        this.stepName = stepName;
        this.passed = passed;
        this.details = details;
        this.executedAt = LocalDateTime.now();
    }

    public String getStepName() { return stepName; }
    public boolean isPassed() { return passed; }
    public String getDetails() { return details; }
}

// ─── Verification Handler (Chain of Responsibility) ───────────────

public abstract class VerificationHandler {
    protected VerificationHandler next;

    public VerificationHandler setNext(VerificationHandler next) {
        this.next = next;
        return next; // for chaining: a.setNext(b).setNext(c)
    }

    // Returns false if pipeline should STOP (hard failure)
    public abstract boolean handle(KYCApplication application);

    protected void passToNext(KYCApplication application) {
        if (next != null) next.handle(application);
    }
}

// Handler 1: Document Completeness Check
public class DocumentCompletenessHandler extends VerificationHandler {
    private final Map<CustomerType, Set<DocumentType>> requiredDocs;

    public DocumentCompletenessHandler() {
        requiredDocs = new EnumMap<>(CustomerType.class);
        requiredDocs.put(CustomerType.INDIVIDUAL,
            EnumSet.of(DocumentType.AADHAAR, DocumentType.PAN));
        requiredDocs.put(CustomerType.NRI,
            EnumSet.of(DocumentType.PASSPORT, DocumentType.PAN));
        requiredDocs.put(CustomerType.BUSINESS,
            EnumSet.of(DocumentType.PAN));
    }

    @Override
    public boolean handle(KYCApplication application) {
        Set<DocumentType> required = requiredDocs.getOrDefault(
            application.getCustomer().getType(), Collections.emptySet());

        List<DocumentType> missing = required.stream()
            .filter(dt -> !application.hasDocument(dt))
            .toList();

        if (!missing.isEmpty()) {
            VerificationStepResult result = new VerificationStepResult(
                "DOCUMENT_COMPLETENESS", false,
                "Missing documents: " + missing
            );
            application.addVerificationResult(result);
            application.transitionTo(KYCStatus.REJECTED, "SYSTEM",
                "Required documents not submitted: " + missing);
            return false;
        }

        application.addVerificationResult(
            new VerificationStepResult("DOCUMENT_COMPLETENESS", true, "All required documents present"));
        return next == null || next.handle(application);
    }
}

// Handler 2: Document Expiry Check
public class DocumentExpiryHandler extends VerificationHandler {
    @Override
    public boolean handle(KYCApplication application) {
        List<Document> expiredDocs = application.getDocuments().stream()
            .filter(Document::isExpired)
            .toList();

        if (!expiredDocs.isEmpty()) {
            List<String> expiredTypes = expiredDocs.stream()
                .map(d -> d.getType().name())
                .toList();
            application.addVerificationResult(
                new VerificationStepResult("DOCUMENT_EXPIRY", false,
                    "Expired documents: " + expiredTypes));
            application.transitionTo(KYCStatus.REJECTED, "SYSTEM",
                "Documents expired: " + expiredTypes);
            expiredDocs.forEach(d -> d.reject("Document expired"));
            return false;
        }

        application.addVerificationResult(
            new VerificationStepResult("DOCUMENT_EXPIRY", true, "All documents are valid"));
        return next == null || next.handle(application);
    }
}

// Handler 3: Document Authenticity (simulated — would call external API)
public class DocumentAuthenticityHandler extends VerificationHandler {
    private final Map<DocumentType, DocumentValidator> validators;

    public DocumentAuthenticityHandler() {
        this.validators = new EnumMap<>(DocumentType.class);
        validators.put(DocumentType.AADHAAR, new AadhaarValidator());
        validators.put(DocumentType.PAN, new PanValidator());
        validators.put(DocumentType.PASSPORT, new PassportValidator());
    }

    @Override
    public boolean handle(KYCApplication application) {
        application.transitionTo(KYCStatus.UNDER_VERIFICATION, "SYSTEM", null);

        for (Document doc : application.getDocuments()) {
            DocumentValidator validator = validators.get(doc.getType());
            if (validator != null) {
                boolean valid = validator.validate(doc);
                if (valid) {
                    doc.verify();
                    application.addVerificationResult(
                        new VerificationStepResult("DOC_AUTH_" + doc.getType(),
                            true, "Document verified: " + doc.getDocumentNumber()));
                } else {
                    doc.reject("Authenticity check failed");
                    application.addVerificationResult(
                        new VerificationStepResult("DOC_AUTH_" + doc.getType(),
                            false, "Authenticity failed for: " + doc.getDocumentNumber()));
                    application.transitionTo(KYCStatus.REJECTED, "SYSTEM", "Document authenticity failed");
                    return false;
                }
            }
        }

        return next == null || next.handle(application);
    }
}

// Handler 4: Risk Scoring
public class RiskScoringHandler extends VerificationHandler {
    private final RiskScoringStrategy scoringStrategy;

    public RiskScoringHandler(RiskScoringStrategy strategy) {
        this.scoringStrategy = strategy;
    }

    @Override
    public boolean handle(KYCApplication application) {
        RiskLevel risk = scoringStrategy.calculateRisk(application);
        application.setRiskLevel(risk);

        if (risk == RiskLevel.VERY_HIGH) {
            application.addVerificationResult(
                new VerificationStepResult("RISK_SCORING", false,
                    "Very high risk — auto-rejected"));
            application.transitionTo(KYCStatus.REJECTED, "RISK_ENGINE",
                "Risk level too high: " + risk);
            return false;
        }

        if (risk == RiskLevel.HIGH) {
            application.addVerificationResult(
                new VerificationStepResult("RISK_SCORING", true,
                    "High risk — escalated to manual review"));
            application.transitionTo(KYCStatus.PENDING_MANUAL_REVIEW, "RISK_ENGINE",
                "High risk requires manual review");
            return false; // stop automated pipeline, needs human
        }

        application.addVerificationResult(
            new VerificationStepResult("RISK_SCORING", true, "Risk level: " + risk));
        return next == null || next.handle(application);
    }
}

// Handler 5: Final Approval
public class AutoApprovalHandler extends VerificationHandler {
    @Override
    public boolean handle(KYCApplication application) {
        application.transitionTo(KYCStatus.APPROVED, "SYSTEM", null);
        application.addVerificationResult(
            new VerificationStepResult("AUTO_APPROVAL", true,
                "All checks passed — KYC approved"));
        System.out.println("✅ KYC APPROVED for: " + application.getCustomer().getName());
        return true;
    }
}

// ─── Document Validators (Strategy) ──────────────────────────────

public interface DocumentValidator {
    boolean validate(Document document);
}

public class AadhaarValidator implements DocumentValidator {
    @Override
    public boolean validate(Document document) {
        // Real: call UIDAI API
        String number = document.getDocumentNumber();
        return number != null && number.matches("\\d{12}"); // 12-digit Aadhaar
    }
}

public class PanValidator implements DocumentValidator {
    @Override
    public boolean validate(Document document) {
        // Real: call Income Tax API
        String number = document.getDocumentNumber();
        return number != null && number.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    }
}

public class PassportValidator implements DocumentValidator {
    @Override
    public boolean validate(Document document) {
        String number = document.getDocumentNumber();
        return number != null && number.matches("[A-Z]{1}[0-9]{7}");
    }
}

// ─── Risk Scoring Strategy ────────────────────────────────────────

public interface RiskScoringStrategy {
    RiskLevel calculateRisk(KYCApplication application);
}

public class BasicRiskScoringStrategy implements RiskScoringStrategy {
    @Override
    public RiskLevel calculateRisk(KYCApplication application) {
        int score = 0;
        Customer customer = application.getCustomer();

        // Age-based risk (NRI = higher risk)
        if (customer.getType() == CustomerType.NRI) score += 30;

        // Document count (fewer docs = more risk)
        if (application.getDocuments().size() < 2) score += 20;

        // Example: check customer age
        if (customer.getAge() < 18) score += 50; // underage = very high risk

        if (score >= 70) return RiskLevel.VERY_HIGH;
        if (score >= 50) return RiskLevel.HIGH;
        if (score >= 25) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}

// ─── Customer ─────────────────────────────────────────────────────

public class Customer {
    private final String customerId;
    private final String name;
    private final String email;
    private final CustomerType type;
    private final int age;

    public Customer(String customerId, String name, String email, CustomerType type, int age) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.type = type;
        this.age = age;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public CustomerType getType() { return type; }
    public int getAge() { return age; }
}

// ─── KYC Pipeline Builder ─────────────────────────────────────────

public class KYCPipelineBuilder {
    private final List<VerificationHandler> handlers = new ArrayList<>();

    public KYCPipelineBuilder addDocumentCompletenessCheck() {
        handlers.add(new DocumentCompletenessHandler());
        return this;
    }

    public KYCPipelineBuilder addExpiryCheck() {
        handlers.add(new DocumentExpiryHandler());
        return this;
    }

    public KYCPipelineBuilder addAuthenticityCheck() {
        handlers.add(new DocumentAuthenticityHandler());
        return this;
    }

    public KYCPipelineBuilder addRiskScoring(RiskScoringStrategy strategy) {
        handlers.add(new RiskScoringHandler(strategy));
        return this;
    }

    public KYCPipelineBuilder addAutoApproval() {
        handlers.add(new AutoApprovalHandler());
        return this;
    }

    public VerificationHandler build() {
        if (handlers.isEmpty()) throw new IllegalStateException("Pipeline has no handlers");
        // Wire chain
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
        return handlers.get(0); // return head of chain
    }
}

// ─── KYC Service (Orchestrator / Singleton) ───────────────────────

public class KYCService {
    private static KYCService instance;

    private final Map<String, KYCApplication> applications; // applicationId → app
    private final Map<String, KYCApplication> byCustomerId; // customerId → active app
    private final VerificationHandler pipeline;

    private KYCService(VerificationHandler pipeline) {
        this.applications = new ConcurrentHashMap<>();
        this.byCustomerId = new ConcurrentHashMap<>();
        this.pipeline = pipeline;
    }

    public static synchronized KYCService getInstance(VerificationHandler pipeline) {
        if (instance == null) instance = new KYCService(pipeline);
        return instance;
    }

    public KYCApplication initiateKYC(Customer customer) {
        if (byCustomerId.containsKey(customer.getCustomerId())) {
            KYCApplication existing = byCustomerId.get(customer.getCustomerId());
            if (existing.getStatus() == KYCStatus.APPROVED) {
                throw new IllegalStateException("Customer already KYC approved");
            }
        }

        KYCApplication application = new KYCApplication(customer);
        applications.put(application.getApplicationId(), application);
        byCustomerId.put(customer.getCustomerId(), application);
        System.out.println("KYC Initiated: " + application.getApplicationId());
        return application;
    }

    public void submitDocument(String applicationId, Document document) {
        KYCApplication app = getApplication(applicationId);
        app.addDocument(document);
    }

    public void triggerVerification(String applicationId) {
        KYCApplication app = getApplication(applicationId);
        if (app.getStatus() != KYCStatus.DOCUMENTS_SUBMITTED) {
            throw new IllegalStateException("Application not ready for verification");
        }
        System.out.println("\n=== Starting verification pipeline for: " + applicationId + " ===");
        pipeline.handle(app);
    }

    // Manual review decision by analyst
    public void manualReviewDecision(String applicationId, String reviewerId,
                                      boolean approved, String notes) {
        KYCApplication app = getApplication(applicationId);
        if (app.getStatus() != KYCStatus.PENDING_MANUAL_REVIEW) {
            throw new IllegalStateException("Application not pending manual review");
        }
        app.transitionTo(approved ? KYCStatus.APPROVED : KYCStatus.REJECTED,
            reviewerId, notes);
        System.out.println("Manual review decision for " + applicationId + ": "
            + (approved ? "APPROVED" : "REJECTED") + " by " + reviewerId);
    }

    public KYCApplication getKYCStatus(String customerId) {
        return byCustomerId.get(customerId);
    }

    private KYCApplication getApplication(String applicationId) {
        KYCApplication app = applications.get(applicationId);
        if (app == null) throw new IllegalArgumentException("Application not found: " + applicationId);
        return app;
    }

    public void printAuditTrail(String applicationId) {
        KYCApplication app = getApplication(applicationId);
        System.out.println("\n=== Audit Trail: " + applicationId + " ===");
        app.getAuditTrail().forEach(System.out::println);
    }
}

/*
Demo:
Customer customer = new Customer("C001", "Abhishek Goyal", "a@g.com", CustomerType.INDIVIDUAL, 26);

VerificationHandler pipeline = new KYCPipelineBuilder()
    .addDocumentCompletenessCheck()
    .addExpiryCheck()
    .addAuthenticityCheck()
    .addRiskScoring(new BasicRiskScoringStrategy())
    .addAutoApproval()
    .build();

KYCService kycService = KYCService.getInstance(pipeline);

KYCApplication app = kycService.initiateKYC(customer);
kycService.submitDocument(app.getApplicationId(),
    new Document("D1", DocumentType.AADHAAR, "123456789012", "s3://...", LocalDate.of(2030,1,1)));
kycService.submitDocument(app.getApplicationId(),
    new Document("D2", DocumentType.PAN, "ABCDE1234F", "s3://...", null));

kycService.triggerVerification(app.getApplicationId());
kycService.printAuditTrail(app.getApplicationId());
*/
```

**Design Decisions Explained:**
- **Chain of Responsibility** — each handler decides to pass forward or stop the chain; clean way to add/remove steps without touching other handlers
- **Builder for pipeline** — `KYCPipelineBuilder` lets you compose different pipelines for Individual vs Business vs NRI
- **Strategy for validators** — `AadhaarValidator`, `PanValidator` are swappable without touching the handler
- **AuditLog** embedded in `KYCApplication` — every state change auto-logged; no separate observer needed since KYC is tightly controlled
- **ManualReview as a terminal state** — when risk is HIGH, automated pipeline stops and queues for human

**Follow-up Questions:**
- "How to handle re-KYC (existing customer's docs expired)?" → `initiateKYC` detects existing APPROVED app, creates a new one with `RE_VERIFICATION` flag
- "How to support parallel document verification?" → Replace `for` loop in `DocumentAuthenticityHandler` with `CompletableFuture.allOf()`
- "What if external Aadhaar API is down?" → Add retry with `ScheduledExecutorService`; fallback to PENDING_MANUAL_REVIEW

---

## 12. Problem 12: Metering Service (Adobe Acrobat / Quota System)

### Problem Statement
Design a metering service for an application like Adobe Acrobat where:
- **Free tier** users can do at most 5 exports/edits per day, resetting at midnight
- **Premium tier** users have unlimited usage
- The system must track per-user feature usage, enforce limits, reset quotas daily, and support different quota policies per feature
- Extension: support team/org-level quotas, overage alerts, and usage analytics

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
user, plan, feature, usage, quota, limit, reset, billing cycle, overage, alert

**Apply 4 Archetypes:**
```
ACTOR:        User (performs actions)
RESOURCE:     FeatureQuota (the allowance for a specific feature)
TRANSACTION:  UsageEvent (each time a user uses a feature)
COORDINATOR:  MeteringService (checks + records usage, enforces limits)
EXTRA:        Plan (FREE/PREMIUM), QuotaPolicy (Strategy for limits),
              QuotaResetScheduler (daily reset), AlertService (overage)
```

**Key Insights:**
- Quota checking + recording must be **atomic** — check-then-act race condition
- Different features can have different quotas on the same plan → quota is per `(userId, featureId, date)`
- Reset at midnight → use `LocalDate` as the key component in quota tracking
- **Strategy Pattern** for quota policies — free/premium/enterprise have different rules

---

### Class Diagram (ASCII)
```
User ──── Plan (FREE/PREMIUM/ENTERPRISE)
              │
         QuotaPolicy (Strategy)
              │
   ┌──────────┴────────────┐
FreePolicyStrategy   PremiumPolicyStrategy

MeteringService
      │
      ├── Map<userId+featureId+date, FeatureUsage>
      └── UsageEventRepository (store events for analytics)

FeatureUsage
      │  usedCount
      │  limit
      │  date
      └── Feature (EXPORT, EDIT, COMPRESS, OCR, SIGN)
```

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum PlanType { FREE, PREMIUM, ENTERPRISE }

public enum Feature {
    PDF_EXPORT, PDF_EDIT, PDF_COMPRESS, OCR, E_SIGN, COMBINE_PDF
}

// ─── Plan + Quota Policy (Strategy) ───────────────────────────────

public interface QuotaPolicy {
    // Returns max allowed uses per day for this feature; -1 = unlimited
    int getDailyLimit(Feature feature);
    boolean isUnlimited(Feature feature);
}

public class FreePlanPolicy implements QuotaPolicy {
    // Free tier: 5 exports/day, 5 edits/day, no OCR, no e-sign
    private static final Map<Feature, Integer> LIMITS = Map.of(
        Feature.PDF_EXPORT,   5,
        Feature.PDF_EDIT,     5,
        Feature.PDF_COMPRESS, 2,
        Feature.OCR,          0,   // blocked on free
        Feature.E_SIGN,       0,   // blocked on free
        Feature.COMBINE_PDF,  2
    );

    @Override
    public int getDailyLimit(Feature feature) {
        return LIMITS.getOrDefault(feature, 0);
    }

    @Override
    public boolean isUnlimited(Feature feature) { return false; }
}

public class PremiumPlanPolicy implements QuotaPolicy {
    @Override
    public int getDailyLimit(Feature feature) { return Integer.MAX_VALUE; }

    @Override
    public boolean isUnlimited(Feature feature) { return true; }
}

public class EnterprisePlanPolicy implements QuotaPolicy {
    // Enterprise: high but not unlimited (for billing)
    private static final Map<Feature, Integer> LIMITS = Map.of(
        Feature.PDF_EXPORT,   1000,
        Feature.PDF_EDIT,     1000,
        Feature.PDF_COMPRESS, 1000,
        Feature.OCR,          500,
        Feature.E_SIGN,       200,
        Feature.COMBINE_PDF,  500
    );

    @Override
    public int getDailyLimit(Feature feature) {
        return LIMITS.getOrDefault(feature, 100);
    }

    @Override
    public boolean isUnlimited(Feature feature) { return false; }
}

// ─── User + Plan ──────────────────────────────────────────────────

public class Plan {
    private final PlanType type;
    private final QuotaPolicy quotaPolicy;
    private final LocalDate validUntil;

    public Plan(PlanType type, QuotaPolicy quotaPolicy, LocalDate validUntil) {
        this.type = type;
        this.quotaPolicy = quotaPolicy;
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return validUntil == null || !LocalDate.now().isAfter(validUntil);
    }

    public PlanType getType() { return type; }
    public QuotaPolicy getQuotaPolicy() { return quotaPolicy; }
}

public class MeteringUser {
    private final String userId;
    private final String email;
    private Plan currentPlan;

    public MeteringUser(String userId, String email, Plan plan) {
        this.userId = userId;
        this.email = email;
        this.currentPlan = plan;
    }

    public void upgradePlan(Plan newPlan) { this.currentPlan = newPlan; }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public Plan getCurrentPlan() { return currentPlan; }
}

// ─── Feature Usage (per user, per feature, per day) ───────────────

public class FeatureUsage {
    private final String userId;
    private final Feature feature;
    private final LocalDate date;
    private int usedCount;
    private final int dailyLimit;

    public FeatureUsage(String userId, Feature feature, LocalDate date, int dailyLimit) {
        this.userId = userId;
        this.feature = feature;
        this.date = date;
        this.usedCount = 0;
        this.dailyLimit = dailyLimit;
    }

    public synchronized boolean canUse() {
        if (dailyLimit == Integer.MAX_VALUE) return true; // unlimited
        if (dailyLimit == 0) return false; // blocked feature
        return usedCount < dailyLimit;
    }

    public synchronized void increment() {
        if (!canUse()) throw new QuotaExceededException(feature, usedCount, dailyLimit);
        usedCount++;
    }

    public int getRemainingCount() {
        if (dailyLimit == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return Math.max(0, dailyLimit - usedCount);
    }

    public int getUsedCount() { return usedCount; }
    public int getDailyLimit() { return dailyLimit; }
    public Feature getFeature() { return feature; }
    public LocalDate getDate() { return date; }
}

// ─── Custom Exception ─────────────────────────────────────────────

public class QuotaExceededException extends RuntimeException {
    private final Feature feature;
    private final int used;
    private final int limit;

    public QuotaExceededException(Feature feature, int used, int limit) {
        super(String.format("Quota exceeded for feature %s: used=%d, limit=%d", feature, used, limit));
        this.feature = feature;
        this.used = used;
        this.limit = limit;
    }

    public Feature getFeature() { return feature; }
    public int getUsed() { return used; }
    public int getLimit() { return limit; }
}

// ─── Usage Event (for analytics) ──────────────────────────────────

public class UsageEvent {
    private final String eventId;
    private final String userId;
    private final Feature feature;
    private final boolean allowed;
    private final LocalDateTime timestamp;
    private final String metadata; // optional: file size, file name, etc.

    public UsageEvent(String userId, Feature feature, boolean allowed, String metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.feature = feature;
        this.allowed = allowed;
        this.timestamp = LocalDateTime.now();
        this.metadata = metadata;
    }

    // Getters...
    public String getUserId() { return userId; }
    public Feature getFeature() { return feature; }
    public boolean isAllowed() { return allowed; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

// ─── Metering Service (Core — Singleton) ──────────────────────────

public class MeteringService {
    private static MeteringService instance;

    // Key: userId:featureId:date → FeatureUsage
    private final Map<String, FeatureUsage> usageRegistry;
    private final Map<String, MeteringUser> users;
    private final List<UsageEvent> eventLog; // in prod: push to Kafka / DB
    private final List<UsageObserver> observers;

    private MeteringService() {
        this.usageRegistry = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.eventLog = Collections.synchronizedList(new ArrayList<>());
        this.observers = new ArrayList<>();
    }

    public static synchronized MeteringService getInstance() {
        if (instance == null) instance = new MeteringService();
        return instance;
    }

    public void registerUser(MeteringUser user) {
        users.put(user.getUserId(), user);
    }

    public void addObserver(UsageObserver observer) {
        observers.add(observer);
    }

    /**
     * Core method: attempt to use a feature.
     * Returns true if allowed and records usage.
     * Throws QuotaExceededException if limit reached.
     */
    public UsageResult checkAndRecord(String userId, Feature feature, String metadata) {
        MeteringUser user = users.get(userId);
        if (user == null) throw new IllegalArgumentException("User not found: " + userId);

        Plan plan = user.getCurrentPlan();
        if (!plan.isActive()) {
            // Plan expired — downgrade to free
            plan = new Plan(PlanType.FREE, new FreePlanPolicy(), null);
            user.upgradePlan(plan);
        }

        QuotaPolicy policy = plan.getQuotaPolicy();

        if (policy.getDailyLimit(feature) == 0) {
            // Feature blocked on this plan
            notifyObservers(new UsageEvent(userId, feature, false, metadata));
            return new UsageResult(false, 0, 0,
                "Feature '" + feature + "' is not available on " + plan.getType() + " plan");
        }

        String key = buildKey(userId, feature, LocalDate.now());
        FeatureUsage usage = usageRegistry.computeIfAbsent(key,
            k -> new FeatureUsage(userId, feature, LocalDate.now(), policy.getDailyLimit(feature)));

        if (!usage.canUse()) {
            UsageEvent event = new UsageEvent(userId, feature, false, metadata);
            eventLog.add(event);
            notifyObservers(event);
            return new UsageResult(false, usage.getUsedCount(), usage.getDailyLimit(),
                "Daily quota exceeded. Resets at midnight.");
        }

        usage.increment();
        UsageEvent event = new UsageEvent(userId, feature, true, metadata);
        eventLog.add(event);
        notifyObservers(event);

        // Alert when 80% of quota used
        if (!policy.isUnlimited(feature)) {
            double usagePercent = (double) usage.getUsedCount() / usage.getDailyLimit() * 100;
            if (usagePercent >= 80) {
                System.out.printf("⚠️ User %s has used %.0f%% of their %s quota (%d/%d)%n",
                    userId, usagePercent, feature, usage.getUsedCount(), usage.getDailyLimit());
            }
        }

        return new UsageResult(true, usage.getUsedCount(), usage.getDailyLimit(), "OK");
    }

    public UsageStats getUsageStats(String userId, Feature feature) {
        String key = buildKey(userId, feature, LocalDate.now());
        FeatureUsage usage = usageRegistry.get(key);
        if (usage == null) {
            MeteringUser user = users.get(userId);
            int limit = user.getCurrentPlan().getQuotaPolicy().getDailyLimit(feature);
            return new UsageStats(userId, feature, 0, limit, LocalDate.now());
        }
        return new UsageStats(userId, feature, usage.getUsedCount(),
            usage.getDailyLimit(), usage.getDate());
    }

    // Called daily by scheduler (e.g. cron at midnight)
    public void resetDailyQuotas() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayStr = yesterday.toString();
        usageRegistry.entrySet().removeIf(e -> e.getKey().endsWith(yesterdayStr));
        System.out.println("Daily quotas reset at midnight.");
    }

    private String buildKey(String userId, Feature feature, LocalDate date) {
        return userId + ":" + feature.name() + ":" + date;
    }

    private void notifyObservers(UsageEvent event) {
        observers.forEach(o -> o.onUsageEvent(event));
    }
}

// ─── Usage Result ─────────────────────────────────────────────────

public class UsageResult {
    private final boolean allowed;
    private final int currentUsage;
    private final int limit;
    private final String message;

    public UsageResult(boolean allowed, int currentUsage, int limit, String message) {
        this.allowed = allowed;
        this.currentUsage = currentUsage;
        this.limit = limit;
        this.message = message;
    }

    public boolean isAllowed() { return allowed; }
    public int getCurrentUsage() { return currentUsage; }
    public int getLimit() { return limit; }
    public int getRemaining() {
        if (limit == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return Math.max(0, limit - currentUsage);
    }
    public String getMessage() { return message; }
}

// ─── Usage Stats ──────────────────────────────────────────────────

public class UsageStats {
    private final String userId;
    private final Feature feature;
    private final int used;
    private final int limit;
    private final LocalDate date;

    public UsageStats(String userId, Feature feature, int used, int limit, LocalDate date) {
        this.userId = userId;
        this.feature = feature;
        this.used = used;
        this.limit = limit;
        this.date = date;
    }

    @Override
    public String toString() {
        String limitStr = limit == Integer.MAX_VALUE ? "∞" : String.valueOf(limit);
        return String.format("User: %s | Feature: %s | Used: %d/%s | Date: %s",
            userId, feature, used, limitStr, date);
    }
}

// ─── Observer Interface ───────────────────────────────────────────

public interface UsageObserver {
    void onUsageEvent(UsageEvent event);
}

// Example: send alert when quota is exceeded
public class QuotaAlertObserver implements UsageObserver {
    @Override
    public void onUsageEvent(UsageEvent event) {
        if (!event.isAllowed()) {
            System.out.println("🚨 ALERT: User " + event.getUserId()
                + " hit quota limit for " + event.getFeature()
                + ". Consider upgrade prompt.");
        }
    }
}

// ─── Daily Reset Scheduler ────────────────────────────────────────

public class QuotaResetScheduler {
    private final MeteringService meteringService;
    private final ScheduledExecutorService scheduler;

    public QuotaResetScheduler(MeteringService meteringService) {
        this.meteringService = meteringService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        // Calculate initial delay to next midnight
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long initialDelaySeconds = ChronoUnit.SECONDS.between(now, nextMidnight);

        scheduler.scheduleAtFixedRate(
            meteringService::resetDailyQuotas,
            initialDelaySeconds,
            86400, // 24 hours in seconds
            TimeUnit.SECONDS
        );
        System.out.println("Quota reset scheduler started. Next reset in "
            + initialDelaySeconds + " seconds.");
    }

    public void stop() {
        scheduler.shutdown();
    }
}

/*
Demo:
Plan freePlan = new Plan(PlanType.FREE, new FreePlanPolicy(), null);
MeteringUser user = new MeteringUser("U1", "abhishek@g.com", freePlan);

MeteringService ms = MeteringService.getInstance();
ms.registerUser(user);
ms.addObserver(new QuotaAlertObserver());

for (int i = 1; i <= 6; i++) {
    UsageResult result = ms.checkAndRecord("U1", Feature.PDF_EXPORT, "file.pdf");
    System.out.println("Attempt " + i + ": " + result.getMessage() 
        + " | Remaining: " + result.getRemaining());
}
// Output: Attempts 1-5 succeed; attempt 6 triggers QuotaExceededException
*/
```

**Design Decisions Explained:**
- `FeatureUsage.increment()` is `synchronized` — prevents two concurrent requests both passing the `canUse()` check (TOCTOU bug)
- Key format `userId:feature:date` — date-scoped means reset is just key deletion by date prefix
- **Observer** for usage events — decouples alerting, analytics, upgrade-prompts from core metering logic
- **Strategy** for quota policy — can add `TeamPlanPolicy` or `TrialPlanPolicy` without touching `MeteringService`
- `resetDailyQuotas()` deletes yesterday's keys — clean O(n) reset
- `QuotaResetScheduler` uses `ScheduledExecutorService` — calculates exact seconds to next midnight for accurate reset

**Follow-up Questions to Prepare:**
- "How to handle 1M concurrent users?" → Move to Redis with `INCR` + `EXPIRE` for atomic quota tracking; `INCR` is single-threaded in Redis
- "How to support monthly quotas (Adobe's actual model)?" → Change key to `userId:feature:yearMonth`; `resetMonthlyQuotas()` runs on 1st of month
- "How to add team/org-level quotas?" → Add `OrgFeatureUsage` with org-level key; check org quota before individual
- "Overage billing?" → Instead of blocking at limit, record overage events and bill at end of month; `UsageEvent.isOverage` flag

---

## 13. Problem 13: Job Scheduler

### Problem Statement
Design a job scheduler system that can schedule jobs to run:
- At a fixed time (one-shot)
- Repeatedly at fixed intervals (periodic)
- With cron expressions (e.g., "every Monday at 9am")

Support job priority, cancellation, retry on failure, and concurrent job execution.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
job, task, schedule, trigger, executor, worker thread, queue, retry, priority

**Apply 4 Archetypes:**
```
ACTOR:        Job submitter (application/user)
RESOURCE:     Worker thread (from thread pool)
TRANSACTION:  JobExecution (one run of a job)
COORDINATOR:  JobScheduler (manages scheduling, dispatch, retry)
EXTRA:        Trigger (schedule definition), JobQueue (priority queue),
              RetryPolicy, JobStatus state machine
```

**Key Insights:**
- `Job` (what to run) vs `Trigger` (when to run) — these are separate concerns
- Priority queue for dispatch order
- **Command Pattern** — each job is a command with `execute()`
- State machine: SCHEDULED → RUNNING → COMPLETED / FAILED → RETRYING

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum JobStatus { SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED, RETRYING }

public enum JobPriority { LOW(3), MEDIUM(2), HIGH(1), CRITICAL(0);
    private final int value;
    JobPriority(int value) { this.value = value; }
    public int getValue() { return value; }
}

// ─── Job Interface (Command Pattern) ──────────────────────────────

public interface Job {
    String getJobId();
    String getJobName();
    void execute() throws Exception;
    JobPriority getPriority();
}

// ─── Abstract Base Job ────────────────────────────────────────────

public abstract class BaseJob implements Job {
    private final String jobId;
    private final String jobName;
    private final JobPriority priority;

    public BaseJob(String jobName, JobPriority priority) {
        this.jobId = UUID.randomUUID().toString();
        this.jobName = jobName;
        this.priority = priority;
    }

    @Override public String getJobId() { return jobId; }
    @Override public String getJobName() { return jobName; }
    @Override public JobPriority getPriority() { return priority; }
}

// Example concrete job
public class ReportGenerationJob extends BaseJob {
    private final String reportType;

    public ReportGenerationJob(String reportType, JobPriority priority) {
        super("ReportGeneration-" + reportType, priority);
        this.reportType = reportType;
    }

    @Override
    public void execute() throws Exception {
        System.out.println("Generating report: " + reportType);
        Thread.sleep(1000); // simulate work
        System.out.println("Report generated: " + reportType);
    }
}

// ─── Trigger (Schedule definition) ───────────────────────────────

public interface Trigger {
    LocalDateTime getNextFireTime();
    boolean hasNextFire();
    TriggerType getType();
}

public enum TriggerType { ONE_SHOT, FIXED_RATE, CRON }

public class OneShotTrigger implements Trigger {
    private final LocalDateTime fireAt;
    private boolean fired;

    public OneShotTrigger(LocalDateTime fireAt) {
        this.fireAt = fireAt;
        this.fired = false;
    }

    @Override
    public LocalDateTime getNextFireTime() { return fired ? null : fireAt; }

    @Override
    public boolean hasNextFire() { return !fired; }

    @Override
    public TriggerType getType() { return TriggerType.ONE_SHOT; }

    public void markFired() { this.fired = true; }
}

public class FixedRateTrigger implements Trigger {
    private final long intervalSeconds;
    private LocalDateTime nextFireTime;

    public FixedRateTrigger(long initialDelaySeconds, long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
        this.nextFireTime = LocalDateTime.now().plusSeconds(initialDelaySeconds);
    }

    @Override
    public LocalDateTime getNextFireTime() { return nextFireTime; }

    @Override
    public boolean hasNextFire() { return true; } // infinite

    public void advance() {
        this.nextFireTime = nextFireTime.plusSeconds(intervalSeconds);
    }

    @Override
    public TriggerType getType() { return TriggerType.FIXED_RATE; }
}

// Simplified cron trigger (supports minute-level patterns)
public class CronTrigger implements Trigger {
    private final String cronExpression; // simplified: "HH:mm" for now
    private LocalDateTime nextFireTime;

    public CronTrigger(String cronExpression) {
        this.cronExpression = cronExpression;
        this.nextFireTime = calculateNext(LocalDateTime.now());
    }

    private LocalDateTime calculateNext(LocalDateTime from) {
        // Simplified: parse "HH:mm" and schedule for today or next day
        String[] parts = cronExpression.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        LocalDateTime candidate = from.toLocalDate().atTime(hour, minute);
        if (!candidate.isAfter(from)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    @Override
    public LocalDateTime getNextFireTime() { return nextFireTime; }

    @Override
    public boolean hasNextFire() { return true; }

    public void advance() {
        nextFireTime = calculateNext(nextFireTime);
    }

    @Override
    public TriggerType getType() { return TriggerType.CRON; }
}

// ─── Retry Policy ─────────────────────────────────────────────────

public class RetryPolicy {
    private final int maxRetries;
    private final long retryDelaySeconds;
    private final boolean exponentialBackoff;

    public static final RetryPolicy NO_RETRY = new RetryPolicy(0, 0, false);
    public static final RetryPolicy RETRY_3_TIMES = new RetryPolicy(3, 5, true);

    public RetryPolicy(int maxRetries, long retryDelaySeconds, boolean exponentialBackoff) {
        this.maxRetries = maxRetries;
        this.retryDelaySeconds = retryDelaySeconds;
        this.exponentialBackoff = exponentialBackoff;
    }

    public long getDelayForAttempt(int attemptNumber) {
        if (exponentialBackoff) {
            return retryDelaySeconds * (long) Math.pow(2, attemptNumber - 1);
        }
        return retryDelaySeconds;
    }

    public int getMaxRetries() { return maxRetries; }
}

// ─── Scheduled Job (wrapper: Job + Trigger + state) ───────────────

public class ScheduledJob implements Comparable<ScheduledJob> {
    private final Job job;
    private final Trigger trigger;
    private final RetryPolicy retryPolicy;
    private JobStatus status;
    private int retryCount;
    private LocalDateTime lastRunAt;
    private String lastError;

    public ScheduledJob(Job job, Trigger trigger, RetryPolicy retryPolicy) {
        this.job = job;
        this.trigger = trigger;
        this.retryPolicy = retryPolicy;
        this.status = JobStatus.SCHEDULED;
        this.retryCount = 0;
    }

    public boolean isReadyToRun() {
        if (status == JobStatus.CANCELLED) return false;
        LocalDateTime next = trigger.getNextFireTime();
        return next != null && !LocalDateTime.now().isBefore(next);
    }

    public void markRunning() { status = JobStatus.RUNNING; lastRunAt = LocalDateTime.now(); }
    public void markCompleted() { status = JobStatus.COMPLETED; }

    public void markFailed(String error) {
        this.lastError = error;
        if (retryCount < retryPolicy.getMaxRetries()) {
            retryCount++;
            status = JobStatus.RETRYING;
            // Reschedule: advance trigger by retry delay
            if (trigger instanceof FixedRateTrigger frt) {
                // advance to retry delay
            }
            System.out.printf("Job %s failed. Retry %d/%d in %ds%n",
                job.getJobName(), retryCount, retryPolicy.getMaxRetries(),
                retryPolicy.getDelayForAttempt(retryCount));
        } else {
            status = JobStatus.FAILED;
            System.out.println("Job " + job.getJobName() + " permanently failed: " + error);
        }
    }

    public void cancel() { status = JobStatus.CANCELLED; }

    // Reschedule recurring jobs after completion
    public void advance() {
        if (trigger instanceof FixedRateTrigger frt) frt.advance();
        else if (trigger instanceof CronTrigger ct) ct.advance();
        else if (trigger instanceof OneShotTrigger ost) ost.markFired();

        if (trigger.hasNextFire()) status = JobStatus.SCHEDULED;
    }

    @Override
    public int compareTo(ScheduledJob other) {
        // First by next fire time, then by priority
        LocalDateTime myNext = trigger.getNextFireTime();
        LocalDateTime otherNext = other.trigger.getNextFireTime();
        if (myNext == null) return 1;
        if (otherNext == null) return -1;
        int timeCmp = myNext.compareTo(otherNext);
        if (timeCmp != 0) return timeCmp;
        return Integer.compare(
            job.getPriority().getValue(),
            other.job.getPriority().getValue()
        );
    }

    public Job getJob() { return job; }
    public JobStatus getStatus() { return status; }
    public Trigger getTrigger() { return trigger; }
    public int getRetryCount() { return retryCount; }
    public LocalDateTime getLastRunAt() { return lastRunAt; }
}

// ─── Job Scheduler (Singleton) ────────────────────────────────────

public class JobScheduler {
    private static JobScheduler instance;

    private final PriorityBlockingQueue<ScheduledJob> jobQueue;
    private final ExecutorService workerPool;
    private final ScheduledExecutorService dispatcherThread;
    private final Map<String, ScheduledJob> jobRegistry; // jobId → ScheduledJob
    private volatile boolean running;

    private JobScheduler(int workerThreads) {
        this.jobQueue = new PriorityBlockingQueue<>();
        this.workerPool = Executors.newFixedThreadPool(workerThreads);
        this.dispatcherThread = Executors.newSingleThreadScheduledExecutor();
        this.jobRegistry = new ConcurrentHashMap<>();
        this.running = false;
    }

    public static synchronized JobScheduler getInstance(int workerThreads) {
        if (instance == null) instance = new JobScheduler(workerThreads);
        return instance;
    }

    public void start() {
        running = true;
        // Poll every second for ready jobs
        dispatcherThread.scheduleAtFixedRate(this::dispatch, 0, 1, TimeUnit.SECONDS);
        System.out.println("JobScheduler started with " +
            ((ThreadPoolExecutor) workerPool).getCorePoolSize() + " workers");
    }

    public String schedule(Job job, Trigger trigger, RetryPolicy retryPolicy) {
        ScheduledJob scheduledJob = new ScheduledJob(job, trigger, retryPolicy);
        jobRegistry.put(job.getJobId(), scheduledJob);
        jobQueue.offer(scheduledJob);
        System.out.println("Scheduled job: " + job.getJobName() + " | Next fire: "
            + trigger.getNextFireTime());
        return job.getJobId();
    }

    public boolean cancel(String jobId) {
        ScheduledJob scheduledJob = jobRegistry.get(jobId);
        if (scheduledJob == null) return false;
        scheduledJob.cancel();
        System.out.println("Cancelled job: " + scheduledJob.getJob().getJobName());
        return true;
    }

    private void dispatch() {
        if (!running) return;

        List<ScheduledJob> readyJobs = new ArrayList<>();
        // Drain ready jobs from the priority queue
        ScheduledJob peeked;
        while ((peeked = jobQueue.peek()) != null && peeked.isReadyToRun()) {
            ScheduledJob job = jobQueue.poll();
            if (job != null && job.isReadyToRun()) {
                readyJobs.add(job);
            }
        }

        for (ScheduledJob scheduledJob : readyJobs) {
            workerPool.submit(() -> executeJob(scheduledJob));
        }
    }

    private void executeJob(ScheduledJob scheduledJob) {
        if (scheduledJob.getStatus() == JobStatus.CANCELLED) return;

        scheduledJob.markRunning();
        System.out.println("▶ Running: " + scheduledJob.getJob().getJobName());

        try {
            scheduledJob.getJob().execute();
            scheduledJob.markCompleted();
            System.out.println("✅ Completed: " + scheduledJob.getJob().getJobName());

            // Re-enqueue recurring jobs
            scheduledJob.advance();
            if (scheduledJob.getTrigger().hasNextFire()
                && scheduledJob.getStatus() == JobStatus.SCHEDULED) {
                jobQueue.offer(scheduledJob);
            }

        } catch (Exception e) {
            scheduledJob.markFailed(e.getMessage());
            if (scheduledJob.getStatus() == JobStatus.RETRYING) {
                // Re-enqueue for retry
                jobQueue.offer(scheduledJob);
            }
        }
    }

    public JobStatus getJobStatus(String jobId) {
        ScheduledJob job = jobRegistry.get(jobId);
        return job != null ? job.getStatus() : null;
    }

    public void shutdown() {
        running = false;
        dispatcherThread.shutdown();
        workerPool.shutdown();
        System.out.println("JobScheduler shut down.");
    }
}

/*
Demo:
JobScheduler scheduler = JobScheduler.getInstance(4);
scheduler.start();

// One-shot job in 2 seconds
String jobId1 = scheduler.schedule(
    new ReportGenerationJob("MONTHLY", JobPriority.HIGH),
    new OneShotTrigger(LocalDateTime.now().plusSeconds(2)),
    RetryPolicy.RETRY_3_TIMES
);

// Recurring job every 10 seconds
String jobId2 = scheduler.schedule(
    new ReportGenerationJob("DAILY", JobPriority.MEDIUM),
    new FixedRateTrigger(0, 10),
    RetryPolicy.NO_RETRY
);

Thread.sleep(30_000);
scheduler.shutdown();
*/
```

**Design Decisions Explained:**
- `PriorityBlockingQueue<ScheduledJob>` — thread-safe priority queue; `compareTo` orders by next-fire-time then priority
- `ScheduledJob` wraps `Job` + `Trigger` + `RetryPolicy` — SRP separation of what/when/how-many-retries
- Dispatcher runs every 1 second — polls queue head; if ready, submits to worker pool
- `advance()` on `FixedRateTrigger` / `CronTrigger` — re-enqueues recurring jobs automatically
- Exponential backoff in `RetryPolicy.getDelayForAttempt()` — standard retry pattern

**Follow-up Questions:**
- "How to persist jobs across restarts?" → Add `JobRepository` interface; serialize `ScheduledJob` to DB
- "How to distribute across nodes?" → Replace in-memory queue with Redis sorted set (score = nextFireTime epoch); each node polls
- "What if two nodes pick the same job?" → Distributed lock (Redis `SET NX EX`) before executing

---

## 14. Problem 14: API Rate Limiter

### Problem Statement
Design an API rate limiter that supports multiple algorithms (Token Bucket, Fixed Window, Sliding Window). The limiter should work per API key, per user, or per IP. Support configurable limits per endpoint.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
request, client, API key, limit, window, bucket, token, rule, endpoint

**Apply 4 Archetypes:**
```
ACTOR:        API Client (makes requests)
RESOURCE:     Rate limit state (bucket / window counters)
TRANSACTION:  API Request (allowed or denied)
COORDINATOR:  RateLimiterService (checks limits, applies rules)
EXTRA:        RateLimitRule (limit + window per client/endpoint),
              RateLimitAlgorithm (Strategy), RateLimitResult
```

**Key Insight — 3 Algorithms to Know:**
```
1. FIXED WINDOW:  Count requests in a fixed time window (e.g. per minute).
   Weakness: burst at window boundary (100 at 0:59, 100 at 1:00 = 200 in 2 sec)

2. SLIDING WINDOW LOG: Track timestamp of each request. Count those in last 60s.
   Precise but memory-heavy for high traffic.

3. TOKEN BUCKET: Bucket holds N tokens. Each request consumes 1. Tokens refill
   at rate R/sec. Allows bursting up to bucket capacity.
   Best for real-world use (what AWS/Stripe use).
```

---

### Full Java Implementation

```java
// ─── Rate Limit Rule ──────────────────────────────────────────────

public class RateLimitRule {
    private final String ruleId;
    private final int maxRequests;      // token bucket capacity / window max
    private final long windowSeconds;   // window duration
    private final double refillRate;    // tokens per second (for token bucket)

    public RateLimitRule(String ruleId, int maxRequests, long windowSeconds) {
        this.ruleId = ruleId;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.refillRate = (double) maxRequests / windowSeconds;
    }

    public String getRuleId() { return ruleId; }
    public int getMaxRequests() { return maxRequests; }
    public long getWindowSeconds() { return windowSeconds; }
    public double getRefillRate() { return refillRate; }
}

// ─── Rate Limit Result ────────────────────────────────────────────

public class RateLimitResult {
    private final boolean allowed;
    private final int remainingTokens;
    private final long retryAfterSeconds; // 0 if allowed
    private final String reason;

    public static RateLimitResult allowed(int remaining) {
        return new RateLimitResult(true, remaining, 0, "OK");
    }

    public static RateLimitResult denied(long retryAfter, String reason) {
        return new RateLimitResult(false, 0, retryAfter, reason);
    }

    private RateLimitResult(boolean allowed, int remaining, long retryAfter, String reason) {
        this.allowed = allowed;
        this.remainingTokens = remaining;
        this.retryAfterSeconds = retryAfter;
        this.reason = reason;
    }

    public boolean isAllowed() { return allowed; }
    public int getRemainingTokens() { return remainingTokens; }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
    public String getReason() { return reason; }
}

// ─── Algorithm Interface (Strategy) ──────────────────────────────

public interface RateLimitAlgorithm {
    RateLimitResult isAllowed(String clientKey, RateLimitRule rule);
}

// ─── Algorithm 1: Fixed Window ────────────────────────────────────

public class FixedWindowAlgorithm implements RateLimitAlgorithm {
    // key → [windowStart, count]
    private final Map<String, long[]> windows = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult isAllowed(String clientKey, RateLimitRule rule) {
        long now = System.currentTimeMillis() / 1000;
        long windowStart = (now / rule.getWindowSeconds()) * rule.getWindowSeconds();

        windows.compute(clientKey, (k, curr) -> {
            if (curr == null || curr[0] != windowStart) {
                return new long[]{windowStart, 0};
            }
            return curr;
        });

        long[] window = windows.get(clientKey);

        synchronized (window) {
            if (window[0] != windowStart) {
                window[0] = windowStart;
                window[1] = 0;
            }

            if (window[1] < rule.getMaxRequests()) {
                window[1]++;
                return RateLimitResult.allowed((int)(rule.getMaxRequests() - window[1]));
            }

            long retryAfter = (windowStart + rule.getWindowSeconds()) - now;
            return RateLimitResult.denied(retryAfter,
                "Rate limit exceeded. Window resets in " + retryAfter + "s");
        }
    }
}

// ─── Algorithm 2: Sliding Window Log ─────────────────────────────

public class SlidingWindowLogAlgorithm implements RateLimitAlgorithm {
    // clientKey → sorted timestamps of requests
    private final Map<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();

    @Override
    public synchronized RateLimitResult isAllowed(String clientKey, RateLimitRule rule) {
        long now = System.currentTimeMillis();
        long windowStart = now - (rule.getWindowSeconds() * 1000);

        Deque<Long> log = requestLogs.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        // Remove expired entries
        while (!log.isEmpty() && log.peekFirst() <= windowStart) {
            log.pollFirst();
        }

        if (log.size() < rule.getMaxRequests()) {
            log.addLast(now);
            return RateLimitResult.allowed(rule.getMaxRequests() - log.size());
        }

        // When will the oldest request expire?
        long oldestRequest = log.peekFirst();
        long retryAfter = (oldestRequest - windowStart) / 1000;
        return RateLimitResult.denied(retryAfter, "Rate limit exceeded");
    }
}

// ─── Algorithm 3: Token Bucket ────────────────────────────────────

public class TokenBucketAlgorithm implements RateLimitAlgorithm {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult isAllowed(String clientKey, RateLimitRule rule) {
        TokenBucket bucket = buckets.computeIfAbsent(clientKey,
            k -> new TokenBucket(rule.getMaxRequests(), rule.getRefillRate()));
        return bucket.consume();
    }

    // Inner class: the actual bucket state
    private static class TokenBucket {
        private final int capacity;
        private final double refillRatePerMs; // tokens per millisecond
        private double tokens;
        private long lastRefillTime;

        TokenBucket(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerMs = refillRatePerSecond / 1000.0;
            this.tokens = capacity; // start full
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized RateLimitResult consume() {
            refill();
            if (tokens >= 1) {
                tokens--;
                return RateLimitResult.allowed((int) tokens);
            }
            // Calculate when next token will be available
            long retryAfterMs = (long) ((1 - tokens) / refillRatePerMs);
            return RateLimitResult.denied(retryAfterMs / 1000 + 1,
                "Rate limit exceeded. Retry after " + (retryAfterMs / 1000 + 1) + "s");
        }

        private void refill() {
            long now = System.currentTimeMillis();
            double tokensToAdd = (now - lastRefillTime) * refillRatePerMs;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}

// ─── Rate Limiter Service ─────────────────────────────────────────

public class RateLimiterService {
    private static RateLimiterService instance;

    // endpoint → rule (can have different limits per endpoint)
    private final Map<String, RateLimitRule> endpointRules;
    // algorithm per rule type
    private final RateLimitAlgorithm defaultAlgorithm;
    // default rule if endpoint not configured
    private final RateLimitRule defaultRule;

    private RateLimiterService(RateLimitAlgorithm algorithm, RateLimitRule defaultRule) {
        this.endpointRules = new ConcurrentHashMap<>();
        this.defaultAlgorithm = algorithm;
        this.defaultRule = defaultRule;
    }

    public static synchronized RateLimiterService getInstance(
            RateLimitAlgorithm algorithm, RateLimitRule defaultRule) {
        if (instance == null) instance = new RateLimiterService(algorithm, defaultRule);
        return instance;
    }

    public void addEndpointRule(String endpoint, RateLimitRule rule) {
        endpointRules.put(endpoint, rule);
    }

    /**
     * Check if request from clientKey to endpoint is allowed.
     * clientKey can be: userId, apiKey, IP, or "userId:endpoint" for per-user-per-endpoint
     */
    public RateLimitResult checkLimit(String clientKey, String endpoint) {
        RateLimitRule rule = endpointRules.getOrDefault(endpoint, defaultRule);
        // Scope key to endpoint for per-endpoint limits
        String scopedKey = clientKey + ":" + endpoint;
        return defaultAlgorithm.isAllowed(scopedKey, rule);
    }

    // Convenience: just check by client (no endpoint scoping)
    public RateLimitResult checkLimit(String clientKey) {
        return defaultAlgorithm.isAllowed(clientKey, defaultRule);
    }
}

/*
Demo:
RateLimitRule defaultRule = new RateLimitRule("default", 10, 60); // 10 req/min

RateLimiterService limiter = RateLimiterService.getInstance(
    new TokenBucketAlgorithm(), defaultRule
);

// More restrictive rule for /api/export
limiter.addEndpointRule("/api/export", new RateLimitRule("export", 3, 60));

// Simulate 15 requests
for (int i = 1; i <= 15; i++) {
    RateLimitResult result = limiter.checkLimit("user-123", "/api/export");
    System.out.printf("Request %d: %s (remaining: %d)%n",
        i, result.isAllowed() ? "ALLOWED" : "DENIED", result.getRemainingTokens());
}
*/
```

**Design Decisions Explained:**
- **Strategy Pattern** for algorithm — swap FixedWindow/SlidingWindow/TokenBucket without touching service
- `TokenBucket` is inner class — encapsulates per-client state cleanly
- `synchronized` on `consume()` and `SlidingWindowLogAlgorithm` — prevents race on shared state
- Scoped key `clientKey:endpoint` — allows different limits per endpoint per client
- `refill()` called lazily on each request — no background thread needed

**Algorithm Comparison Table:**

| Algorithm | Burst Handling | Memory | Precision | Use Case |
|---|---|---|---|---|
| Fixed Window | Allows boundary burst | Low | Low | Simple APIs |
| Sliding Window Log | Precise | High | High | Low-volume, strict APIs |
| Token Bucket | Allows controlled burst | Medium | High | Production APIs (Stripe, AWS) |

---

## 15. Problem 15: Notification Service

### Problem Statement
Design a notification service that sends notifications via multiple channels (Email, SMS, Push, Slack). Support routing rules (e.g., critical alerts go to all channels; low-priority go to email only), retry on failure, and per-user notification preferences.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
notification, user, channel, email, SMS, push, template, preference, delivery, retry

**Apply 4 Archetypes:**
```
ACTOR:        User (recipient), Service (sender/trigger)
RESOURCE:     NotificationChannel (Email/SMS/Push/Slack)
TRANSACTION:  NotificationDelivery (one send attempt per channel)
COORDINATOR:  NotificationService (routes, dispatches, retries)
EXTRA:        NotificationTemplate, UserPreference, DeliveryStatus,
              RoutingRule (which channels for which priority)
```

**What Varies?**
- How each channel sends → **Strategy Pattern** per channel
- Which channels to use → **Routing Rule / Strategy**
- Template rendering → **Template Method** or simple `String.format`

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum NotificationChannel { EMAIL, SMS, PUSH, SLACK }

public enum NotificationPriority { LOW, MEDIUM, HIGH, CRITICAL }

public enum DeliveryStatus { PENDING, SENT, FAILED, RETRYING }

// ─── Notification ─────────────────────────────────────────────────

public class Notification {
    private final String notificationId;
    private final String recipientId;
    private final String subject;
    private final String body;
    private final NotificationPriority priority;
    private final Map<String, String> metadata; // extra data for template
    private final LocalDateTime createdAt;

    public Notification(String recipientId, String subject, String body,
                        NotificationPriority priority) {
        this.notificationId = UUID.randomUUID().toString();
        this.recipientId = recipientId;
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.metadata = new HashMap<>();
        this.createdAt = LocalDateTime.now();
    }

    public void addMetadata(String key, String value) { metadata.put(key, value); }

    public String getNotificationId() { return notificationId; }
    public String getRecipientId() { return recipientId; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationPriority getPriority() { return priority; }
    public Map<String, String> getMetadata() { return metadata; }
}

// ─── Delivery Record ──────────────────────────────────────────────

public class DeliveryRecord {
    private final String recordId;
    private final String notificationId;
    private final NotificationChannel channel;
    private DeliveryStatus status;
    private int attemptCount;
    private LocalDateTime lastAttemptAt;
    private String errorMessage;

    public DeliveryRecord(String notificationId, NotificationChannel channel) {
        this.recordId = UUID.randomUUID().toString();
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = DeliveryStatus.PENDING;
        this.attemptCount = 0;
    }

    public void recordAttempt(boolean success, String error) {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
        if (success) {
            this.status = DeliveryStatus.SENT;
        } else {
            this.errorMessage = error;
            this.status = attemptCount < 3 ? DeliveryStatus.RETRYING : DeliveryStatus.FAILED;
        }
    }

    public boolean shouldRetry() { return status == DeliveryStatus.RETRYING; }
    public int getAttemptCount() { return attemptCount; }
    public DeliveryStatus getStatus() { return status; }
    public NotificationChannel getChannel() { return channel; }
}

// ─── User Preference ──────────────────────────────────────────────

public class UserNotificationPreference {
    private final String userId;
    private final Set<NotificationChannel> enabledChannels;
    private boolean doNotDisturb;
    private LocalTime dndStart; // e.g. 22:00
    private LocalTime dndEnd;   // e.g. 07:00

    public UserNotificationPreference(String userId, Set<NotificationChannel> channels) {
        this.userId = userId;
        this.enabledChannels = new HashSet<>(channels);
        this.doNotDisturb = false;
    }

    public void setDND(LocalTime start, LocalTime end) {
        this.doNotDisturb = true;
        this.dndStart = start;
        this.dndEnd = end;
    }

    public boolean isChannelEnabled(NotificationChannel channel) {
        return enabledChannels.contains(channel);
    }

    public boolean isDNDActive() {
        if (!doNotDisturb) return false;
        LocalTime now = LocalTime.now();
        return now.isAfter(dndStart) || now.isBefore(dndEnd);
    }

    public String getUserId() { return userId; }
}

// ─── Channel Handler (Strategy) ───────────────────────────────────

public interface ChannelHandler {
    NotificationChannel getChannel();
    boolean send(Notification notification, String recipientAddress);
}

public class EmailChannelHandler implements ChannelHandler {
    @Override
    public NotificationChannel getChannel() { return NotificationChannel.EMAIL; }

    @Override
    public boolean send(Notification notification, String email) {
        try {
            // Real: use JavaMail / SendGrid API
            System.out.printf("[EMAIL] To: %s | Subject: %s | Body: %s%n",
                email, notification.getSubject(), notification.getBody());
            return true;
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
            return false;
        }
    }
}

public class SMSChannelHandler implements ChannelHandler {
    @Override
    public NotificationChannel getChannel() { return NotificationChannel.SMS; }

    @Override
    public boolean send(Notification notification, String phoneNumber) {
        try {
            // Real: Twilio / AWS SNS
            String smsBody = notification.getBody().substring(0,
                Math.min(160, notification.getBody().length())); // SMS limit
            System.out.printf("[SMS] To: %s | Body: %s%n", phoneNumber, smsBody);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

public class PushChannelHandler implements ChannelHandler {
    @Override
    public NotificationChannel getChannel() { return NotificationChannel.PUSH; }

    @Override
    public boolean send(Notification notification, String deviceToken) {
        try {
            // Real: FCM / APNs
            System.out.printf("[PUSH] Token: %s | Title: %s%n",
                deviceToken, notification.getSubject());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

public class SlackChannelHandler implements ChannelHandler {
    @Override
    public NotificationChannel getChannel() { return NotificationChannel.SLACK; }

    @Override
    public boolean send(Notification notification, String webhookUrl) {
        try {
            // Real: HTTP POST to Slack webhook
            System.out.printf("[SLACK] Webhook: %s | Message: %s%n",
                webhookUrl, notification.getBody());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

// ─── Routing Strategy ─────────────────────────────────────────────

public interface RoutingStrategy {
    Set<NotificationChannel> determineChannels(NotificationPriority priority,
                                                UserNotificationPreference userPreference);
}

public class PriorityBasedRoutingStrategy implements RoutingStrategy {
    @Override
    public Set<NotificationChannel> determineChannels(NotificationPriority priority,
                                                       UserNotificationPreference pref) {
        Set<NotificationChannel> channels = new HashSet<>();

        switch (priority) {
            case CRITICAL -> {
                // Critical: all channels, ignore DND
                channels.addAll(pref.enabledChannels); // override DND for critical
            }
            case HIGH -> {
                if (!pref.isDNDActive()) {
                    channels.add(NotificationChannel.EMAIL);
                    channels.add(NotificationChannel.SMS);
                    channels.add(NotificationChannel.PUSH);
                }
            }
            case MEDIUM -> {
                if (!pref.isDNDActive()) {
                    channels.add(NotificationChannel.EMAIL);
                    channels.add(NotificationChannel.PUSH);
                }
            }
            case LOW -> {
                if (!pref.isDNDActive()) {
                    channels.add(NotificationChannel.EMAIL);
                }
            }
        }

        // Filter to only user-enabled channels
        channels.retainAll(pref.enabledChannels);
        return channels;
    }
}

// ─── User Contact Info ────────────────────────────────────────────

public class UserContactInfo {
    private final String userId;
    private String email;
    private String phoneNumber;
    private String deviceToken;
    private String slackWebhook;

    public UserContactInfo(String userId, String email, String phoneNumber, String deviceToken) {
        this.userId = userId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceToken = deviceToken;
    }

    public String getAddressForChannel(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> email;
            case SMS -> phoneNumber;
            case PUSH -> deviceToken;
            case SLACK -> slackWebhook;
        };
    }

    public String getUserId() { return userId; }
}

// ─── Notification Service ─────────────────────────────────────────

public class NotificationService {
    private static NotificationService instance;

    private final Map<NotificationChannel, ChannelHandler> channelHandlers;
    private final RoutingStrategy routingStrategy;
    private final Map<String, UserNotificationPreference> userPreferences;
    private final Map<String, UserContactInfo> userContacts;
    private final ExecutorService asyncExecutor;

    private NotificationService(RoutingStrategy routingStrategy) {
        this.channelHandlers = new EnumMap<>(NotificationChannel.class);
        this.routingStrategy = routingStrategy;
        this.userPreferences = new ConcurrentHashMap<>();
        this.userContacts = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newFixedThreadPool(10);

        // Register default handlers
        registerHandler(new EmailChannelHandler());
        registerHandler(new SMSChannelHandler());
        registerHandler(new PushChannelHandler());
        registerHandler(new SlackChannelHandler());
    }

    public static synchronized NotificationService getInstance(RoutingStrategy strategy) {
        if (instance == null) instance = new NotificationService(strategy);
        return instance;
    }

    public void registerHandler(ChannelHandler handler) {
        channelHandlers.put(handler.getChannel(), handler);
    }

    public void setUserPreference(UserNotificationPreference pref) {
        userPreferences.put(pref.getUserId(), pref);
    }

    public void setUserContactInfo(UserContactInfo contactInfo) {
        userContacts.put(contactInfo.getUserId(), contactInfo);
    }

    public void send(Notification notification) {
        // Async dispatch
        asyncExecutor.submit(() -> dispatchNotification(notification));
    }

    private void dispatchNotification(Notification notification) {
        String userId = notification.getRecipientId();
        UserNotificationPreference pref = userPreferences.get(userId);
        UserContactInfo contact = userContacts.get(userId);

        if (pref == null || contact == null) {
            System.err.println("No preference/contact info for user: " + userId);
            return;
        }

        Set<NotificationChannel> targetChannels =
            routingStrategy.determineChannels(notification.getPriority(), pref);

        System.out.printf("Dispatching notification '%s' via %s%n",
            notification.getSubject(), targetChannels);

        for (NotificationChannel channel : targetChannels) {
            ChannelHandler handler = channelHandlers.get(channel);
            String address = contact.getAddressForChannel(channel);

            if (handler == null || address == null) continue;

            DeliveryRecord record = new DeliveryRecord(notification.getNotificationId(), channel);
            sendWithRetry(notification, handler, address, record);
        }
    }

    private void sendWithRetry(Notification notification, ChannelHandler handler,
                                String address, DeliveryRecord record) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            boolean success = handler.send(notification, address);
            record.recordAttempt(success, success ? null : "Send failed on attempt " + attempt);

            if (success) return;

            if (record.shouldRetry()) {
                try {
                    long delay = (long) Math.pow(2, attempt) * 1000; // exponential backoff
                    System.out.printf("Retrying %s in %dms (attempt %d/%d)%n",
                        handler.getChannel(), delay, attempt + 1, maxAttempts);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}

/*
Demo:
NotificationService ns = NotificationService.getInstance(new PriorityBasedRoutingStrategy());

UserNotificationPreference pref = new UserNotificationPreference("U1",
    EnumSet.of(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH));
ns.setUserPreference(pref);

UserContactInfo contact = new UserContactInfo("U1",
    "abhishek@gmail.com", "+91-9999999999", "fcm-token-xyz");
ns.setUserContactInfo(contact);

Notification critical = new Notification("U1", "Security Alert",
    "Login from new device detected. If not you, secure your account.", NotificationPriority.CRITICAL);
ns.send(critical);
*/
```

---

## 16. Problem 16: Chess Game

### Problem Statement
Design a two-player chess game with full move validation, check/checkmate detection, and turn management.

---

### 🧠 Entity Identification Walk-Through

**Noun Extraction:**
board, piece, player, move, square/cell, king, queen, rook, bishop, knight, pawn, check, checkmate

**Apply 4 Archetypes:**
```
ACTOR:        Player (White/Black)
RESOURCE:     Piece (6 types × 2 colors = 12 unique types)
              Square (8×8 = 64 squares on board)
TRANSACTION:  Move (from square, to square, captures, special moves)
COORDINATOR:  Game (manages turns, win detection)
EXTRA:        MoveValidator (per-piece movement rules), Board
```

**Key Insights:**
- Piece hierarchy: `Piece` abstract → `King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn`
- Each piece knows its own valid moves → **Polymorphism / Template Method**
- Move validation is the most complex part — each piece has different movement rules
- Check detection: after any move, verify if own king is under attack

---

### Full Java Implementation

```java
// ─── Enums ───────────────────────────────────────────────────────

public enum Color { WHITE, BLACK }

public enum PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

public enum GameStatus { ACTIVE, WHITE_WINS, BLACK_WINS, DRAW, STALEMATE }

// ─── Position ─────────────────────────────────────────────────────

public class Position {
    private final int row; // 0-7 (0 = rank 1, 7 = rank 8)
    private final int col; // 0-7 (0 = file a, 7 = file h)

    public Position(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7)
            throw new IllegalArgumentException("Invalid position: " + row + "," + col);
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isValid() { return row >= 0 && row < 8 && col >= 0 && col < 8; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position p)) return false;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() { return 8 * row + col; }

    @Override
    public String toString() {
        return "" + (char)('a' + col) + (row + 1); // e.g. "e4"
    }
}

// ─── Piece Hierarchy ──────────────────────────────────────────────

public abstract class Piece {
    protected final Color color;
    protected final PieceType type;
    protected boolean hasMoved;

    public Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    // Returns all positions this piece can move to (without check validation)
    public abstract List<Position> getPossibleMoves(Position current, Board board);

    public Color getColor() { return color; }
    public PieceType getType() { return type; }
    public boolean hasMoved() { return hasMoved; }
    public void markMoved() { this.hasMoved = true; }

    public String getSymbol() {
        String s = switch (type) {
            case KING -> "K"; case QUEEN -> "Q"; case ROOK -> "R";
            case BISHOP -> "B"; case KNIGHT -> "N"; case PAWN -> "P";
        };
        return color == Color.WHITE ? s : s.toLowerCase();
    }

    // Helper: add move if destination is valid and not occupied by own piece
    protected boolean isValidDestination(Position pos, Board board) {
        if (!pos.isValid()) return false;
        Piece occupant = board.getPiece(pos);
        return occupant == null || occupant.getColor() != this.color;
    }
}

public class Rook extends Piece {
    public Rook(Color color) { super(color, PieceType.ROOK); }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] dir : directions) {
            Position pos = current;
            while (true) {
                pos = new Position(pos.getRow() + dir[0], pos.getCol() + dir[1]);
                if (!pos.isValid()) break;
                if (isValidDestination(pos, board)) moves.add(pos);
                if (board.getPiece(pos) != null) break; // blocked
            }
        }
        return moves;
    }
}

public class Bishop extends Piece {
    public Bishop(Color color) { super(color, PieceType.BISHOP); }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] dir : directions) {
            Position pos = current;
            while (true) {
                try { pos = new Position(pos.getRow() + dir[0], pos.getCol() + dir[1]); }
                catch (IllegalArgumentException e) { break; }
                if (isValidDestination(pos, board)) moves.add(pos);
                if (board.getPiece(pos) != null) break;
            }
        }
        return moves;
    }
}

public class Queen extends Piece {
    private final Rook rookLogic;
    private final Bishop bishopLogic;

    public Queen(Color color) {
        super(color, PieceType.QUEEN);
        rookLogic = new Rook(color);
        bishopLogic = new Bishop(color);
    }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        moves.addAll(rookLogic.getPossibleMoves(current, board));
        moves.addAll(bishopLogic.getPossibleMoves(current, board));
        return moves;
    }
}

public class Knight extends Piece {
    public Knight(Color color) { super(color, PieceType.KNIGHT); }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int[][] offsets = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
        for (int[] off : offsets) {
            try {
                Position pos = new Position(current.getRow() + off[0], current.getCol() + off[1]);
                if (isValidDestination(pos, board)) moves.add(pos);
            } catch (IllegalArgumentException ignored) {}
        }
        return moves;
    }
}

public class King extends Piece {
    public King(Color color) { super(color, PieceType.KING); }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                try {
                    Position pos = new Position(current.getRow() + dr, current.getCol() + dc);
                    if (isValidDestination(pos, board)) moves.add(pos);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return moves;
    }
}

public class Pawn extends Piece {
    public Pawn(Color color) { super(color, PieceType.PAWN); }

    @Override
    public List<Position> getPossibleMoves(Position current, Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = (color == Color.WHITE) ? 1 : -1;

        // Forward move
        try {
            Position forward = new Position(current.getRow() + direction, current.getCol());
            if (board.getPiece(forward) == null) {
                moves.add(forward);
                // Double advance from starting position
                if (!hasMoved) {
                    Position doubleForward = new Position(current.getRow() + 2 * direction, current.getCol());
                    if (board.getPiece(doubleForward) == null) moves.add(doubleForward);
                }
            }
        } catch (IllegalArgumentException ignored) {}

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            try {
                Position diag = new Position(current.getRow() + direction, current.getCol() + dc);
                Piece target = board.getPiece(diag);
                if (target != null && target.getColor() != this.color) moves.add(diag);
            } catch (IllegalArgumentException ignored) {}
        }

        return moves;
    }
}

// ─── Move ─────────────────────────────────────────────────────────

public class Move {
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final Piece capturedPiece; // null if no capture

    public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getPiece() { return piece; }
    public Piece getCapturedPiece() { return capturedPiece; }

    @Override
    public String toString() {
        return piece.getSymbol() + " " + from + " → " + to
            + (capturedPiece != null ? " x" + capturedPiece.getSymbol() : "");
    }
}

// ─── Board ────────────────────────────────────────────────────────

public class Board {
    private final Piece[][] grid; // [row][col]

    public Board() {
        this.grid = new Piece[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        // White pieces (row 0 and 1)
        grid[0][0] = new Rook(Color.WHITE);   grid[0][7] = new Rook(Color.WHITE);
        grid[0][1] = new Knight(Color.WHITE); grid[0][6] = new Knight(Color.WHITE);
        grid[0][2] = new Bishop(Color.WHITE); grid[0][5] = new Bishop(Color.WHITE);
        grid[0][3] = new Queen(Color.WHITE);  grid[0][4] = new King(Color.WHITE);
        for (int c = 0; c < 8; c++) grid[1][c] = new Pawn(Color.WHITE);

        // Black pieces (row 7 and 6)
        grid[7][0] = new Rook(Color.BLACK);   grid[7][7] = new Rook(Color.BLACK);
        grid[7][1] = new Knight(Color.BLACK); grid[7][6] = new Knight(Color.BLACK);
        grid[7][2] = new Bishop(Color.BLACK); grid[7][5] = new Bishop(Color.BLACK);
        grid[7][3] = new Queen(Color.BLACK);  grid[7][4] = new King(Color.BLACK);
        for (int c = 0; c < 8; c++) grid[6][c] = new Pawn(Color.BLACK);
    }

    public Piece getPiece(Position pos) { return grid[pos.getRow()][pos.getCol()]; }

    public void setPiece(Position pos, Piece piece) { grid[pos.getRow()][pos.getCol()] = piece; }

    public Position findKing(Color color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color)
                    return new Position(r, c);
            }
        throw new IllegalStateException("King not found — invalid board state");
    }

    public boolean isUnderAttack(Position pos, Color byColor) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() == byColor) {
                    if (p.getPossibleMoves(new Position(r, c), this).contains(pos))
                        return true;
                }
            }
        return false;
    }

    public void print() {
        System.out.println("  a b c d e f g h");
        for (int r = 7; r >= 0; r--) {
            System.out.print((r + 1) + " ");
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                System.out.print((p == null ? "." : p.getSymbol()) + " ");
            }
            System.out.println();
        }
    }
}

// ─── Player ───────────────────────────────────────────────────────

public class ChessPlayer {
    private final String playerId;
    private final String name;
    private final Color color;

    public ChessPlayer(String playerId, String name, Color color) {
        this.playerId = playerId;
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public Color getColor() { return color; }
}

// ─── Game (Orchestrator) ──────────────────────────────────────────

public class ChessGame {
    private final Board board;
    private final ChessPlayer whitePlayer;
    private final ChessPlayer blackPlayer;
    private Color currentTurn;
    private GameStatus status;
    private final List<Move> moveHistory;

    public ChessGame(ChessPlayer white, ChessPlayer black) {
        this.board = new Board();
        this.whitePlayer = white;
        this.blackPlayer = black;
        this.currentTurn = Color.WHITE;
        this.status = GameStatus.ACTIVE;
        this.moveHistory = new ArrayList<>();
    }

    public Move makeMove(Position from, Position to) {
        if (status != GameStatus.ACTIVE) throw new IllegalStateException("Game is over");

        Piece piece = board.getPiece(from);
        if (piece == null) throw new IllegalArgumentException("No piece at " + from);
        if (piece.getColor() != currentTurn)
            throw new IllegalArgumentException("Not " + currentTurn + "'s turn");

        List<Position> legalMoves = getLegalMoves(from);
        if (!legalMoves.contains(to))
            throw new IllegalArgumentException("Illegal move: " + from + " → " + to);

        Piece captured = board.getPiece(to);
        Move move = new Move(from, to, piece, captured);

        // Execute move
        board.setPiece(to, piece);
        board.setPiece(from, null);
        piece.markMoved();

        moveHistory.add(move);
        System.out.println(currentTurn + " plays: " + move);

        // Check game end conditions
        Color opponent = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        if (isInCheck(opponent)) {
            if (isCheckmate(opponent)) {
                status = (currentTurn == Color.WHITE) ? GameStatus.WHITE_WINS : GameStatus.BLACK_WINS;
                System.out.println("CHECKMATE! " + currentTurn + " wins!");
            } else {
                System.out.println(opponent + " is in CHECK!");
            }
        }

        currentTurn = opponent;
        return move;
    }

    // Returns moves that don't leave own king in check
    public List<Position> getLegalMoves(Position from) {
        Piece piece = board.getPiece(from);
        if (piece == null) return Collections.emptyList();

        List<Position> candidates = piece.getPossibleMoves(from, board);
        List<Position> legal = new ArrayList<>();

        for (Position to : candidates) {
            // Simulate move and check if own king would be in check
            Piece captured = board.getPiece(to);
            board.setPiece(to, piece);
            board.setPiece(from, null);

            if (!isInCheck(piece.getColor())) legal.add(to);

            // Undo simulation
            board.setPiece(from, piece);
            board.setPiece(to, captured);
        }

        return legal;
    }

    private boolean isInCheck(Color color) {
        Position kingPos = board.findKing(color);
        Color opponent = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        return board.isUnderAttack(kingPos, opponent);
    }

    private boolean isCheckmate(Color color) {
        // Checkmate: in check AND no legal moves
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p != null && p.getColor() == color) {
                    if (!getLegalMoves(new Position(r, c)).isEmpty()) return false;
                }
            }
        return true;
    }

    public GameStatus getStatus() { return status; }
    public Color getCurrentTurn() { return currentTurn; }
    public Board getBoard() { return board; }
}
```

**Design Decisions Explained:**
- Each `Piece` subclass encodes its own movement logic — clean **polymorphism** via override
- `Queen` **delegates** to `Rook` + `Bishop` logic — composition over code duplication
- `getLegalMoves()` uses **simulate → check → undo** to filter moves that expose king to check
- `Board.isUnderAttack()` loops all opponent pieces — O(64 × max_moves) which is acceptable for chess
- Move history as `List<Move>` enables undo, replay, and PGN export

---

## 17. Problem 17: ATM Machine

### Problem Statement
Design an ATM that supports card insertion, PIN validation, balance inquiry, cash withdrawal, deposit, and transfer — with State Pattern as the core design.

---

### 🧠 Entity Identification Walk-Through

**Apply 4 Archetypes:**
```
ACTOR:        BankCustomer (card holder)
RESOURCE:     Account (balance), ATM Cash (physical cash in machine)
TRANSACTION:  ATMTransaction (withdrawal/deposit/transfer/inquiry)
COORDINATOR:  ATM (state machine), BankService (account operations)
EXTRA:        Card, ATMState (State Pattern), Receipt
```

**ATM State Machine:**
```
IDLE → CARD_INSERTED → PIN_ENTERED → AUTHENTICATED → TRANSACTION_IN_PROGRESS → IDLE
                          ↓
                    (wrong PIN 3×)
                          ↓
                       CARD_BLOCKED
```

---

### Full Java Implementation

```java
public enum ATMStateType { IDLE, CARD_INSERTED, PIN_VERIFIED, TRANSACTION_IN_PROGRESS }
public enum TransactionType { WITHDRAWAL, DEPOSIT, BALANCE_INQUIRY, TRANSFER }

// ─── Bank Account (simplified) ────────────────────────────────────

public class BankAccount {
    private final String accountId;
    private final String customerId;
    private double balance;
    private boolean blocked;

    public BankAccount(String accountId, String customerId, double balance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.balance = balance;
        this.blocked = false;
    }

    public synchronized boolean debit(double amount) {
        if (blocked || balance < amount) return false;
        balance -= amount;
        return true;
    }

    public synchronized void credit(double amount) { balance += amount; }

    public double getBalance() { return balance; }
    public boolean isBlocked() { return blocked; }
    public void block() { this.blocked = true; }
    public String getAccountId() { return accountId; }
}

// ─── Card ─────────────────────────────────────────────────────────

public class Card {
    private final String cardNumber;
    private final String accountId;
    private final String pin; // hashed in real system
    private final LocalDate expiryDate;
    private int failedPinAttempts;
    private boolean blocked;

    public Card(String cardNumber, String accountId, String pin, LocalDate expiryDate) {
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.pin = pin;
        this.expiryDate = expiryDate;
        this.failedPinAttempts = 0;
        this.blocked = false;
    }

    public boolean verifyPin(String enteredPin) {
        if (blocked) return false;
        if (this.pin.equals(enteredPin)) {
            failedPinAttempts = 0;
            return true;
        }
        failedPinAttempts++;
        if (failedPinAttempts >= 3) blocked = true;
        return false;
    }

    public boolean isValid() {
        return !blocked && !LocalDate.now().isAfter(expiryDate);
    }

    public boolean isBlocked() { return blocked; }
    public String getCardNumber() { return cardNumber; }
    public String getAccountId() { return accountId; }
    public int getFailedAttempts() { return failedPinAttempts; }
}

// ─── ATM State Interface ──────────────────────────────────────────

public interface ATMState {
    void insertCard(ATM atm, Card card);
    void enterPin(ATM atm, String pin);
    void selectTransaction(ATM atm, TransactionType type);
    void withdraw(ATM atm, double amount);
    void deposit(ATM atm, double amount);
    void checkBalance(ATM atm);
    void cancel(ATM atm);
}

// ─── Idle State ───────────────────────────────────────────────────

public class IdleATMState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) {
        if (!card.isValid()) {
            System.out.println("Card is blocked or expired. Please contact your bank.");
            return;
        }
        atm.setCurrentCard(card);
        atm.setState(atm.getCardInsertedState());
        System.out.println("Card inserted. Please enter your PIN.");
    }

    @Override public void enterPin(ATM atm, String pin)        { System.out.println("Please insert card first."); }
    @Override public void selectTransaction(ATM atm, TransactionType t) { System.out.println("Please insert card first."); }
    @Override public void withdraw(ATM atm, double amount)     { System.out.println("Please insert card first."); }
    @Override public void deposit(ATM atm, double amount)      { System.out.println("Please insert card first."); }
    @Override public void checkBalance(ATM atm)                { System.out.println("Please insert card first."); }
    @Override public void cancel(ATM atm)                      { System.out.println("No active session."); }
}

// ─── Card Inserted State ──────────────────────────────────────────

public class CardInsertedATMState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) { System.out.println("Card already inserted."); }

    @Override
    public void enterPin(ATM atm, String pin) {
        Card card = atm.getCurrentCard();
        if (card.verifyPin(pin)) {
            atm.setState(atm.getPinVerifiedState());
            System.out.println("PIN verified. Please select a transaction.");
        } else {
            if (card.isBlocked()) {
                System.out.println("Card blocked after 3 wrong PIN attempts. Card retained.");
                atm.ejectCard();
                atm.setState(atm.getIdleState());
            } else {
                System.out.println("Wrong PIN. Attempts remaining: " + (3 - card.getFailedAttempts()));
            }
        }
    }

    @Override public void selectTransaction(ATM atm, TransactionType t) { System.out.println("Please enter PIN first."); }
    @Override public void withdraw(ATM atm, double amount) { System.out.println("Please enter PIN first."); }
    @Override public void deposit(ATM atm, double amount)  { System.out.println("Please enter PIN first."); }
    @Override public void checkBalance(ATM atm)            { System.out.println("Please enter PIN first."); }

    @Override
    public void cancel(ATM atm) {
        System.out.println("Transaction cancelled. Ejecting card.");
        atm.ejectCard();
        atm.setState(atm.getIdleState());
    }
}

// ─── PIN Verified State ───────────────────────────────────────────

public class PinVerifiedATMState implements ATMState {
    @Override public void insertCard(ATM atm, Card card) { System.out.println("Card already in use."); }
    @Override public void enterPin(ATM atm, String pin)  { System.out.println("PIN already verified."); }

    @Override
    public void selectTransaction(ATM atm, TransactionType type) {
        atm.setCurrentTransaction(type);
        atm.setState(atm.getTransactionState());
        System.out.println("Selected: " + type + ". Proceed.");
    }

    @Override
    public void withdraw(ATM atm, double amount) {
        selectTransaction(atm, TransactionType.WITHDRAWAL);
        atm.withdraw(amount);
    }

    @Override public void deposit(ATM atm, double amount) { checkBalance(atm); }
    @Override public void checkBalance(ATM atm) {
        atm.checkBalance();
    }

    @Override
    public void cancel(ATM atm) {
        System.out.println("Session cancelled. Ejecting card.");
        atm.ejectCard();
        atm.setState(atm.getIdleState());
    }
}

// ─── Transaction In Progress State ───────────────────────────────

public class TransactionInProgressATMState implements ATMState {
    @Override public void insertCard(ATM atm, Card card) { System.out.println("Transaction in progress."); }
    @Override public void enterPin(ATM atm, String pin)  { System.out.println("Transaction in progress."); }
    @Override public void selectTransaction(ATM atm, TransactionType t) { System.out.println("Transaction already selected."); }

    @Override
    public void withdraw(ATM atm, double amount) {
        boolean success = atm.getBankService().withdraw(atm.getCurrentCard().getAccountId(), amount);
        if (success) {
            atm.dispenseCash(amount);
            System.out.printf("Dispensed ₹%.2f. Remaining balance: ₹%.2f%n",
                amount, atm.getBankService().getBalance(atm.getCurrentCard().getAccountId()));
        } else {
            System.out.println("Withdrawal failed. Insufficient funds or account blocked.");
        }
        atm.setState(atm.getPinVerifiedState()); // back to menu
    }

    @Override
    public void deposit(ATM atm, double amount) {
        atm.getBankService().deposit(atm.getCurrentCard().getAccountId(), amount);
        System.out.printf("Deposited ₹%.2f successfully.%n", amount);
        atm.setState(atm.getPinVerifiedState());
    }

    @Override
    public void checkBalance(ATM atm) {
        double balance = atm.getBankService().getBalance(atm.getCurrentCard().getAccountId());
        System.out.printf("Account Balance: ₹%.2f%n", balance);
        atm.setState(atm.getPinVerifiedState());
    }

    @Override
    public void cancel(ATM atm) {
        System.out.println("Transaction cancelled. Ejecting card.");
        atm.ejectCard();
        atm.setState(atm.getIdleState());
    }
}

// ─── Bank Service ─────────────────────────────────────────────────

public class BankService {
    private final Map<String, BankAccount> accounts = new HashMap<>();

    public void registerAccount(BankAccount account) {
        accounts.put(account.getAccountId(), account);
    }

    public boolean withdraw(String accountId, double amount) {
        BankAccount account = accounts.get(accountId);
        return account != null && account.debit(amount);
    }

    public void deposit(String accountId, double amount) {
        BankAccount account = accounts.get(accountId);
        if (account != null) account.credit(amount);
    }

    public double getBalance(String accountId) {
        BankAccount account = accounts.get(accountId);
        return account != null ? account.getBalance() : 0;
    }
}

// ─── ATM (Context) ────────────────────────────────────────────────

public class ATM {
    private final String atmId;
    private final BankService bankService;
    private double cashAvailable;

    // State instances
    private final ATMState idleState = new IdleATMState();
    private final ATMState cardInsertedState = new CardInsertedATMState();
    private final ATMState pinVerifiedState = new PinVerifiedATMState();
    private final ATMState transactionState = new TransactionInProgressATMState();

    private ATMState currentState;
    private Card currentCard;
    private TransactionType currentTransaction;

    public ATM(String atmId, BankService bankService, double cashAvailable) {
        this.atmId = atmId;
        this.bankService = bankService;
        this.cashAvailable = cashAvailable;
        this.currentState = idleState;
    }

    // Delegate to state
    public void insertCard(Card card)           { currentState.insertCard(this, card); }
    public void enterPin(String pin)            { currentState.enterPin(this, pin); }
    public void selectTransaction(TransactionType t) { currentState.selectTransaction(this, t); }
    public void withdraw(double amount)         { currentState.withdraw(this, amount); }
    public void deposit(double amount)          { currentState.deposit(this, amount); }
    public void checkBalance()                  { currentState.checkBalance(this); }
    public void cancel()                        { currentState.cancel(this); }

    public void ejectCard() {
        System.out.println("Card ejected.");
        this.currentCard = null;
    }

    public void dispenseCash(double amount) {
        if (cashAvailable < amount) throw new IllegalStateException("ATM has insufficient cash");
        cashAvailable -= amount;
    }

    public void setState(ATMState state)                    { this.currentState = state; }
    public void setCurrentCard(Card card)                   { this.currentCard = card; }
    public void setCurrentTransaction(TransactionType type) { this.currentTransaction = type; }

    public Card getCurrentCard()             { return currentCard; }
    public BankService getBankService()      { return bankService; }
    public ATMState getIdleState()           { return idleState; }
    public ATMState getCardInsertedState()   { return cardInsertedState; }
    public ATMState getPinVerifiedState()    { return pinVerifiedState; }
    public ATMState getTransactionState()    { return transactionState; }
}
```

---

## 18. Problem 18: E-commerce Order Management

### Problem Statement
Design an order management system for an e-commerce platform covering product catalog, cart, order placement, payment, fulfilment, and order tracking.

---

### 🧠 Entity Identification Walk-Through

```
ACTOR:        Customer, Seller, DeliveryAgent
RESOURCE:     Product, Inventory (stock per product)
TRANSACTION:  Cart, Order, Payment, Shipment
COORDINATOR:  OrderService, InventoryService, PaymentService
EXTRA:        OrderStatus (state machine), PricingStrategy (discount/tax)
```

**Key Insight:** `Cart` → `Order` → `Shipment` is a pipeline. An `Order` is frozen snapshot of cart; it doesn't reference live product prices.

---

### Full Java Implementation

```java
public enum OrderStatus {
    PENDING_PAYMENT, PAYMENT_CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
}

public class Product {
    private final String productId;
    private final String name;
    private final double basePrice;
    private final String category;

    public Product(String productId, String name, double basePrice, String category) {
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.category = category;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
}

public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public void increaseQuantity(int delta) { this.quantity += delta; }
    public double getSubtotal() { return product.getBasePrice() * quantity; }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}

public class Cart {
    private final String cartId;
    private final String customerId;
    private final Map<String, CartItem> items; // productId → CartItem

    public Cart(String customerId) {
        this.cartId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = new LinkedHashMap<>();
    }

    public void addItem(Product product, int quantity) {
        items.merge(product.getProductId(),
            new CartItem(product, quantity),
            (existing, newItem) -> { existing.increaseQuantity(quantity); return existing; });
    }

    public void removeItem(String productId) { items.remove(productId); }

    public double getTotalAmount() {
        return items.values().stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public List<CartItem> getItems() { return new ArrayList<>(items.values()); }
    public String getCustomerId() { return customerId; }
    public boolean isEmpty() { return items.isEmpty(); }
}

public class OrderItem {
    private final String productId;
    private final String productName;
    private final double priceAtOrderTime; // snapshot
    private final int quantity;

    public OrderItem(CartItem cartItem) {
        this.productId = cartItem.getProduct().getProductId();
        this.productName = cartItem.getProduct().getName();
        this.priceAtOrderTime = cartItem.getProduct().getBasePrice();
        this.quantity = cartItem.getQuantity();
    }

    public double getSubtotal() { return priceAtOrderTime * quantity; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}

public class Order {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final double totalAmount;
    private OrderStatus status;
    private final LocalDateTime placedAt;
    private String trackingId;

    public Order(String customerId, List<OrderItem> items, double totalAmount) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = Collections.unmodifiableList(items);
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.placedAt = LocalDateTime.now();
    }

    public void transitionStatus(OrderStatus newStatus) {
        System.out.printf("Order %s: %s → %s%n", orderId, status, newStatus);
        this.status = newStatus;
    }

    public void assignTracking(String trackingId) { this.trackingId = trackingId; }

    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public String getTrackingId() { return trackingId; }
}

// ─── Inventory Service ────────────────────────────────────────────

public class InventoryService {
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public void addStock(String productId, int quantity) {
        stock.merge(productId, quantity, Integer::sum);
    }

    public synchronized boolean reserveStock(List<CartItem> items) {
        // Check all first (all-or-nothing)
        for (CartItem item : items) {
            int available = stock.getOrDefault(item.getProduct().getProductId(), 0);
            if (available < item.getQuantity()) {
                System.out.println("Insufficient stock for: " + item.getProduct().getName());
                return false;
            }
        }
        // Deduct
        for (CartItem item : items) {
            stock.merge(item.getProduct().getProductId(), -item.getQuantity(), Integer::sum);
        }
        return true;
    }

    public void releaseStock(List<OrderItem> items) {
        items.forEach(item -> stock.merge(item.getProductId(), item.getQuantity(), Integer::sum));
    }

    public int getStock(String productId) { return stock.getOrDefault(productId, 0); }
}

// ─── Order Service ────────────────────────────────────────────────

public class OrderService {
    private static OrderService instance;

    private final InventoryService inventoryService;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    private OrderService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public static synchronized OrderService getInstance(InventoryService inventoryService) {
        if (instance == null) instance = new OrderService(inventoryService);
        return instance;
    }

    public Order placeOrder(Cart cart) {
        if (cart.isEmpty()) throw new IllegalStateException("Cannot place empty order");

        List<CartItem> items = cart.getItems();

        if (!inventoryService.reserveStock(items)) {
            throw new IllegalStateException("Stock unavailable for one or more items");
        }

        List<OrderItem> orderItems = items.stream().map(OrderItem::new).toList();
        Order order = new Order(cart.getCustomerId(), orderItems, cart.getTotalAmount());
        orders.put(order.getOrderId(), order);

        System.out.println("Order placed: " + order.getOrderId() + " | Total: ₹" + order.getTotalAmount());
        return order;
    }

    public void confirmPayment(String orderId) {
        Order order = getOrder(orderId);
        order.transitionStatus(OrderStatus.PAYMENT_CONFIRMED);
        order.transitionStatus(OrderStatus.PROCESSING);
    }

    public void shipOrder(String orderId, String trackingId) {
        Order order = getOrder(orderId);
        order.assignTracking(trackingId);
        order.transitionStatus(OrderStatus.SHIPPED);
    }

    public void deliverOrder(String orderId) {
        getOrder(orderId).transitionStatus(OrderStatus.DELIVERED);
    }

    public void cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel delivered order");
        order.transitionStatus(OrderStatus.CANCELLED);
        inventoryService.releaseStock(order.getItems());
    }

    public Order getOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Order not found: " + orderId);
        return order;
    }
}
```

---

## 19. Problem 19: Log Aggregation & Monitoring System

### Problem Statement
Design a log aggregation system where multiple services emit logs, the system collects, filters, routes them to appropriate sinks (file, database, alerting), and supports real-time alerting on error patterns.

---

### 🧠 Entity Identification Walk-Through

```
ACTOR:        LogProducer (any service emitting logs)
RESOURCE:     LogSink (file, DB, alert system)
TRANSACTION:  LogEntry (one log record)
COORDINATOR:  LogAggregator (collects, filters, routes)
EXTRA:        LogFilter (Strategy), LogRouter, AlertRule (Observer trigger)
```

**Key Insights:**
- **Observer Pattern** — log producers emit logs; aggregator observes and routes
- **Chain of Responsibility / Filter Chain** for log processing pipeline
- **Strategy** for routing rules

---

### Full Java Implementation

```java
public enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR, FATAL }

public class LogEntry {
    private final String logId;
    private final String serviceName;
    private final LogLevel level;
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, String> context; // traceId, requestId, etc.
    private final Throwable throwable;

    public LogEntry(String serviceName, LogLevel level, String message,
                    Map<String, String> context, Throwable throwable) {
        this.logId = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.level = level;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.context = context != null ? context : new HashMap<>();
        this.throwable = throwable;
    }

    public String getServiceName() { return serviceName; }
    public LogLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getContext() { return context; }

    @Override
    public String toString() {
        return String.format("[%s] [%s] [%s] %s",
            timestamp, level, serviceName, message);
    }
}

// ─── Log Filter (Strategy) ────────────────────────────────────────

public interface LogFilter {
    boolean accept(LogEntry entry);
}

public class LevelFilter implements LogFilter {
    private final LogLevel minimumLevel;

    public LevelFilter(LogLevel minimumLevel) { this.minimumLevel = minimumLevel; }

    @Override
    public boolean accept(LogEntry entry) {
        return entry.getLevel().ordinal() >= minimumLevel.ordinal();
    }
}

public class ServiceFilter implements LogFilter {
    private final Set<String> allowedServices;

    public ServiceFilter(Set<String> allowedServices) {
        this.allowedServices = allowedServices;
    }

    @Override
    public boolean accept(LogEntry entry) {
        return allowedServices.contains(entry.getServiceName());
    }
}

// ─── Log Sink (Strategy) ──────────────────────────────────────────

public interface LogSink {
    void write(LogEntry entry);
    String getSinkName();
}

public class ConsoleLogSink implements LogSink {
    @Override
    public void write(LogEntry entry) { System.out.println("[CONSOLE] " + entry); }

    @Override
    public String getSinkName() { return "CONSOLE"; }
}

public class FileLogSink implements LogSink {
    private final String filePath;

    public FileLogSink(String filePath) { this.filePath = filePath; }

    @Override
    public void write(LogEntry entry) {
        // Real: append to rotating log file
        System.out.println("[FILE:" + filePath + "] " + entry);
    }

    @Override
    public String getSinkName() { return "FILE:" + filePath; }
}

public class AlertingLogSink implements LogSink {
    private final List<AlertRule> alertRules;

    public AlertingLogSink(List<AlertRule> rules) { this.alertRules = rules; }

    @Override
    public void write(LogEntry entry) {
        alertRules.stream()
            .filter(rule -> rule.matches(entry))
            .forEach(rule -> rule.trigger(entry));
    }

    @Override
    public String getSinkName() { return "ALERTING"; }
}

// ─── Alert Rule ───────────────────────────────────────────────────

public class AlertRule {
    private final String ruleName;
    private final LogLevel triggerLevel;
    private final String messagePattern;
    private final String alertChannel; // slack/email/pagerduty

    public AlertRule(String ruleName, LogLevel triggerLevel,
                     String messagePattern, String alertChannel) {
        this.ruleName = ruleName;
        this.triggerLevel = triggerLevel;
        this.messagePattern = messagePattern;
        this.alertChannel = alertChannel;
    }

    public boolean matches(LogEntry entry) {
        return entry.getLevel().ordinal() >= triggerLevel.ordinal()
            && (messagePattern == null || entry.getMessage().contains(messagePattern));
    }

    public void trigger(LogEntry entry) {
        System.out.printf("🚨 ALERT [%s] via %s: %s%n", ruleName, alertChannel, entry.getMessage());
        // Real: call PagerDuty/Slack API
    }
}

// ─── Log Pipeline Entry ───────────────────────────────────────────

public class LogRoute {
    private final List<LogFilter> filters;
    private final LogSink sink;

    public LogRoute(List<LogFilter> filters, LogSink sink) {
        this.filters = filters;
        this.sink = sink;
    }

    public void process(LogEntry entry) {
        boolean accepted = filters.stream().allMatch(f -> f.accept(entry));
        if (accepted) sink.write(entry);
    }
}

// ─── Log Aggregator (Observer + Coordinator) ──────────────────────

public class LogAggregator {
    private static LogAggregator instance;

    private final List<LogRoute> routes;
    private final BlockingQueue<LogEntry> buffer;
    private final ExecutorService processor;

    private LogAggregator() {
        this.routes = new ArrayList<>();
        this.buffer = new LinkedBlockingQueue<>(10_000);
        this.processor = Executors.newFixedThreadPool(4);
        startProcessing();
    }

    public static synchronized LogAggregator getInstance() {
        if (instance == null) instance = new LogAggregator();
        return instance;
    }

    public void addRoute(LogRoute route) { routes.add(route); }

    // Called by log producers (thread-safe)
    public void ingest(LogEntry entry) {
        if (!buffer.offer(entry)) {
            System.err.println("Log buffer full — dropping log from: " + entry.getServiceName());
        }
    }

    private void startProcessing() {
        // Single background thread drains the buffer
        processor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    LogEntry entry = buffer.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) routes.forEach(route -> route.process(entry));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void shutdown() { processor.shutdown(); }
}

// ─── Logger (used by services) ────────────────────────────────────

public class Logger {
    private final String serviceName;
    private final LogAggregator aggregator;

    public Logger(String serviceName) {
        this.serviceName = serviceName;
        this.aggregator = LogAggregator.getInstance();
    }

    public void info(String message)  { log(LogLevel.INFO, message, null); }
    public void warn(String message)  { log(LogLevel.WARN, message, null); }
    public void error(String message, Throwable t) { log(LogLevel.ERROR, message, t); }
    public void fatal(String message, Throwable t) { log(LogLevel.FATAL, message, t); }

    private void log(LogLevel level, String message, Throwable t) {
        aggregator.ingest(new LogEntry(serviceName, level, message, null, t));
    }
}

/*
Demo:
LogAggregator aggregator = LogAggregator.getInstance();

// Route 1: All errors to console
aggregator.addRoute(new LogRoute(
    List.of(new LevelFilter(LogLevel.ERROR)),
    new ConsoleLogSink()
));

// Route 2: Payment service logs to file
aggregator.addRoute(new LogRoute(
    List.of(new ServiceFilter(Set.of("payment-service"))),
    new FileLogSink("/logs/payment.log")
));

// Route 3: Fatal alerts via PagerDuty
AlertRule fatalRule = new AlertRule("FatalAlert", LogLevel.FATAL, null, "pagerduty");
aggregator.addRoute(new LogRoute(
    List.of(new LevelFilter(LogLevel.FATAL)),
    new AlertingLogSink(List.of(fatalRule))
));

Logger paymentLogger = new Logger("payment-service");
paymentLogger.info("Payment processed for order ORD-123");
paymentLogger.error("Payment gateway timeout", new RuntimeException("Timeout"));
*/
```

---

## 20. Problem 20: Distributed Cache with Invalidation

### Problem Statement
Design a distributed cache system that supports multiple eviction strategies (LRU, LFU, TTL), cache invalidation (on update/delete), write-through vs write-behind modes, and cache-aside pattern for client use.

---

### 🧠 Entity Identification Walk-Through

```
ACTOR:        CacheClient (application code using cache)
RESOURCE:     CacheEntry (key, value, metadata: TTL, access count, last-accessed)
TRANSACTION:  CacheOperation (GET/PUT/INVALIDATE/EVICT)
COORDINATOR:  CacheManager (manages entries, eviction, invalidation)
EXTRA:        EvictionPolicy (Strategy: LRU/LFU/TTL),
              InvalidationEvent (Observer), WritePolicy (Write-through/Write-behind)
```

---

### Full Java Implementation

```java
public enum EvictionPolicyType { LRU, LFU, TTL }

public enum WritePolicyType { WRITE_THROUGH, WRITE_BEHIND, CACHE_ASIDE }

// ─── Cache Entry ──────────────────────────────────────────────────

public class CacheEntry<V> {
    private final String key;
    private V value;
    private int accessCount;
    private long lastAccessTime;
    private final long createdAt;
    private final long ttlMillis; // -1 = no expiry

    public CacheEntry(String key, V value, long ttlMillis) {
        this.key = key;
        this.value = value;
        this.ttlMillis = ttlMillis;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessTime = createdAt;
        this.accessCount = 0;
    }

    public void recordAccess() {
        this.accessCount++;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        if (ttlMillis <= 0) return false;
        return System.currentTimeMillis() - createdAt > ttlMillis;
    }

    public void setValue(V value) { this.value = value; }

    public String getKey() { return key; }
    public V getValue() { return value; }
    public int getAccessCount() { return accessCount; }
    public long getLastAccessTime() { return lastAccessTime; }
    public long getCreatedAt() { return createdAt; }
    public long getTtlMillis() { return ttlMillis; }
}

// ─── Eviction Policy (Strategy) ───────────────────────────────────

public interface EvictionPolicy<V> {
    String selectEvictionKey(Map<String, CacheEntry<V>> entries);
}

public class LRUEvictionPolicy<V> implements EvictionPolicy<V> {
    @Override
    public String selectEvictionKey(Map<String, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
            .min(Comparator.comparingLong(e -> e.getValue().getLastAccessTime()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}

public class LFUEvictionPolicy<V> implements EvictionPolicy<V> {
    @Override
    public String selectEvictionKey(Map<String, CacheEntry<V>> entries) {
        return entries.entrySet().stream()
            .min(Comparator.comparingInt(e -> e.getValue().getAccessCount()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}

public class TTLEvictionPolicy<V> implements EvictionPolicy<V> {
    @Override
    public String selectEvictionKey(Map<String, CacheEntry<V>> entries) {
        // Evict oldest (soonest to expire)
        return entries.entrySet().stream()
            .filter(e -> e.getValue().isExpired())
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}

// ─── Invalidation Event (Observer) ────────────────────────────────

public class CacheInvalidationEvent {
    private final String key;
    private final String reason; // UPDATE, DELETE, TTL_EXPIRED, MANUAL
    private final LocalDateTime occurredAt;

    public CacheInvalidationEvent(String key, String reason) {
        this.key = key;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
    }

    public String getKey() { return key; }
    public String getReason() { return reason; }
}

public interface CacheInvalidationListener {
    void onInvalidation(CacheInvalidationEvent event);
}

// ─── Data Source Interface (for write-through) ────────────────────

public interface DataSource<V> {
    V load(String key);
    void save(String key, V value);
    void delete(String key);
}

// ─── Cache Manager ────────────────────────────────────────────────

public class CacheManager<V> {
    private final int maxSize;
    private final Map<String, CacheEntry<V>> store;
    private final EvictionPolicy<V> evictionPolicy;
    private final WritePolicyType writePolicy;
    private DataSource<V> dataSource;
    private final List<CacheInvalidationListener> listeners;
    private final ScheduledExecutorService ttlCleanupExecutor;

    private long hits = 0;
    private long misses = 0;

    public CacheManager(int maxSize, EvictionPolicy<V> evictionPolicy,
                        WritePolicyType writePolicy) {
        this.maxSize = maxSize;
        this.store = new ConcurrentHashMap<>();
        this.evictionPolicy = evictionPolicy;
        this.writePolicy = writePolicy;
        this.listeners = new ArrayList<>();
        this.ttlCleanupExecutor = Executors.newSingleThreadScheduledExecutor();

        // Background TTL cleanup every 30s
        ttlCleanupExecutor.scheduleAtFixedRate(this::evictExpiredEntries,
            30, 30, TimeUnit.SECONDS);
    }

    public void setDataSource(DataSource<V> ds) { this.dataSource = ds; }
    public void addInvalidationListener(CacheInvalidationListener l) { listeners.add(l); }

    public Optional<V> get(String key) {
        CacheEntry<V> entry = store.get(key);

        if (entry == null) {
            misses++;
            // Cache-aside: load from data source
            if (dataSource != null) {
                V value = dataSource.load(key);
                if (value != null) {
                    put(key, value, -1); // cache with no TTL
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }

        if (entry.isExpired()) {
            invalidate(key, "TTL_EXPIRED");
            misses++;
            return Optional.empty();
        }

        entry.recordAccess();
        hits++;
        return Optional.of(entry.getValue());
    }

    public synchronized void put(String key, V value, long ttlMillis) {
        if (store.containsKey(key)) {
            store.get(key).setValue(value);
            store.get(key).recordAccess();
        } else {
            if (store.size() >= maxSize) {
                String evictKey = evictionPolicy.selectEvictionKey(store);
                if (evictKey != null) {
                    store.remove(evictKey);
                    notifyListeners(new CacheInvalidationEvent(evictKey, "EVICTION"));
                    System.out.println("Evicted: " + evictKey);
                }
            }
            store.put(key, new CacheEntry<>(key, value, ttlMillis));
        }

        // Write-through: persist to data source immediately
        if (writePolicy == WritePolicyType.WRITE_THROUGH && dataSource != null) {
            dataSource.save(key, value);
        }
    }

    public void invalidate(String key, String reason) {
        if (store.remove(key) != null) {
            CacheInvalidationEvent event = new CacheInvalidationEvent(key, reason);
            notifyListeners(event);
            System.out.println("Invalidated cache key: " + key + " | Reason: " + reason);
        }
    }

    public void invalidatePattern(String keyPrefix) {
        List<String> keysToRemove = store.keySet().stream()
            .filter(k -> k.startsWith(keyPrefix))
            .toList();
        keysToRemove.forEach(k -> invalidate(k, "PATTERN_INVALIDATION"));
    }

    private void evictExpiredEntries() {
        store.entrySet().stream()
            .filter(e -> e.getValue().isExpired())
            .map(Map.Entry::getKey)
            .forEach(k -> invalidate(k, "TTL_EXPIRED"));
    }

    private void notifyListeners(CacheInvalidationEvent event) {
        listeners.forEach(l -> l.onInvalidation(event));
    }

    public CacheStats getStats() {
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        return new CacheStats(hits, misses, hitRate, store.size());
    }

    public void shutdown() { ttlCleanupExecutor.shutdown(); }
}

public class CacheStats {
    private final long hits;
    private final long misses;
    private final double hitRate;
    private final int currentSize;

    public CacheStats(long hits, long misses, double hitRate, int currentSize) {
        this.hits = hits; this.misses = misses;
        this.hitRate = hitRate; this.currentSize = currentSize;
    }

    @Override
    public String toString() {
        return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, size=%d}",
            hits, misses, hitRate, currentSize);
    }
}

/*
Demo:
CacheManager<String> cache = new CacheManager<>(100, new LRUEvictionPolicy<>(), WritePolicyType.CACHE_ASIDE);

cache.addInvalidationListener(event ->
    System.out.println("Invalidation event: " + event.getKey() + " | " + event.getReason()));

// Set with TTL
cache.put("user:123", "Abhishek Goyal", 3600_000); // 1 hour TTL

// Get
Optional<String> user = cache.get("user:123");
user.ifPresent(u -> System.out.println("Cache hit: " + u));

// Invalidate on update
cache.invalidate("user:123", "UPDATE");

System.out.println(cache.getStats());
*/
```

---

## Updated Quick Entity Identification Practice Table

| # | Problem | Actor | Resource | Transaction | Coordinator | Pattern(s) |
|---|---|---|---|---|---|---|
| 11 | KYC Service | Customer, Reviewer | Document | KYCApplication | KYCService | Chain of Responsibility, Builder, Strategy |
| 12 | Metering Service | User | FeatureQuota | UsageEvent | MeteringService | Strategy, Observer, Singleton |
| 13 | Job Scheduler | Job Submitter | Worker Thread | ScheduledJob | JobScheduler | Command, Strategy, Singleton |
| 14 | API Rate Limiter | API Client | Rate State | API Request | RateLimiterService | Strategy, Singleton |
| 15 | Notification Service | User | Channel | NotificationDelivery | NotificationService | Strategy, Observer, Factory |
| 16 | Chess Game | Player | Piece/Square | Move | ChessGame | Polymorphism, Template Method |
| 17 | ATM Machine | Customer | Account | ATMTransaction | ATM | State, Singleton |
| 18 | E-commerce Orders | Customer, Seller | Product, Inventory | Order, Shipment | OrderService | State, Singleton |
| 19 | Log Aggregation | Log Producer | Log Sink | LogEntry | LogAggregator | Observer, Strategy, Chain of Responsibility |
| 20 | Distributed Cache | Cache Client | CacheEntry | CacheOperation | CacheManager | Strategy, Observer, Singleton |

---

## Patterns That Repeat Across Problems — Frequency Map

```
PATTERN           PROBLEMS WHERE IT APPEARS
──────────────────────────────────────────────────────────────────────
Singleton         Almost every coordinator (KYCService, MeteringService,
                  JobScheduler, RateLimiter, NotificationService, ATM,
                  OrderService, LogAggregator)

Strategy          KYC (validators, risk scoring), Metering (quota policy),
                  Job Scheduler (retry policy), Rate Limiter (algorithm),
                  Notification (routing, channel), Cache (eviction)

Observer          Metering (UsageObserver), Notification (delivery events),
                  Log Aggregation (alerting), Cache (invalidation listeners)

State             ATM (IdleState, CardInsertedState...),
                  KYC (implicitly via status transitions),
                  Ride Sharing (REQUESTED→ACCEPTED→IN_PROGRESS)

Chain of          KYC (verification pipeline),
Responsibility    Log Aggregation (filter pipeline)

Command           Job Scheduler (Job interface = Command)

Builder           KYC Pipeline (KYCPipelineBuilder)

Factory           Notification channels, Document validators
```

---

## Hard Interview Follow-Up Questions — Cross-Problem

| Question | Key Answer |
|---|---|
| "How to make your metering service handle 10M users?" | Move to Redis INCR + EXPIRE. Key = `userId:feature:date`. INCR is atomic. No Java sync needed. |
| "Your job scheduler needs to survive a server restart" | Serialize `ScheduledJob` state to DB/Redis. On startup, reload and recalculate `nextFireTime`. |
| "Rate limiter across 3 servers — how to coordinate?" | Centralized Redis counter with `INCR` and `EXPIRE`. Or gossip protocol for eventual consistency. |
| "KYC doc verification is slow — 30 seconds per doc" | Make `DocumentAuthenticityHandler` async. Use `CompletableFuture.allOf()` for parallel doc checks. |
| "Cache inconsistency between two nodes?" | Event-based invalidation: publish `CacheInvalidationEvent` to a message queue (Kafka/Redis Pub-Sub); all nodes subscribe and evict. |
| "Notification service sends duplicate alerts on retry" | Add idempotency key (`notificationId + channel`) as dedup key in DB before sending. |
| "Chess: how to implement undo?" | Store `List<Move>` as history. Undo = reverse move (swap from/to, restore capturedPiece). |
| "Log buffer fills up under high traffic?" | Back-pressure: block producer or drop with sampling (log 1 in 10 DEBUG logs). Use Disruptor ring buffer. |

---

*Prepared with ❤️ for LLD interview rounds — covers the top 10 most frequently asked problems with full Java implementation and the mental model to crack any new problem.*
