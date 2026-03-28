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

*Prepared with ❤️ for LLD interview rounds — covers the top 10 most frequently asked problems with full Java implementation and the mental model to crack any new problem.*
