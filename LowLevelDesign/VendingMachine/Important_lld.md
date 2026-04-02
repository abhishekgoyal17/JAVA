
## Banking Domain: Gold Loan | UPI | KYC — Rulebook Edition

> **Every design in this file follows the 12-step LLD Rulebook exactly, in order.**
> Same interviewer. Gold Loan team manager. He knows your KYC design from last round.
> Read this the night before. Recite the opening lines out loud.

---

## Table of Contents

1. [GOLD LOAN SERVICE](#gold-loan-service)
2. [UPI PAYMENT SYSTEM](#upi-payment-system)
3. [KYC SERVICE](#kyc-service)
4. [QUICK CROSS-TOPIC CHEAT SHEET](#quick-cross-topic-cheat-sheet)

---

---

# GOLD LOAN SERVICE

---

## STEP 1 — CLARIFY REQUIREMENTS

Say: *"Before jumping into implementation, let me clarify both functional and non-functional requirements."*

---

### Part A — Functional Requirements

Say: *"First, let me understand the core use cases."*

**Actors:** Customer, Loan Officer (Branch), System (Scheduler), Admin

```
Functional Requirements:
- Customer can submit a gold loan application with pledged gold items
- Loan Officer can appraise gold items and record valuation at branch
- System verifies KYC before sanctioning
- System calculates eligible amount using LTV ratio (RBI max 75%)
- Loan Officer can sanction and disburse the loan to customer account
- Customer can make repayment (EMI / Bullet / Vanilla scheme)
- Customer can request foreclosure of the loan at any time
- Customer can request partial gold release after proportionate repayment
- System monitors LTV daily — notifies customer on breach
- System initiates auction on 90+ DPD or unresolved LTV breach
- Admin can view portfolio, overdue buckets, auction pipeline
- System sends notifications on all lifecycle events (disbursed, EMI due, overdue)
```

---

### Part B — Non-Functional Requirements

Say: *"Now let me clarify the quality constraints."*

| Area | Decision for Gold Loan |
|------|------------------------|
| Scale | Moderate write load; read-heavy for EMI schedules / status queries |
| Concurrency | Concurrent repayments + LTV jobs + disbursal on same loan → needs locks |
| Consistency | Strong — LTV check and disbursal cannot have race conditions |
| Latency | No strict SLA for interview scope |
| Availability | High — repayment must always succeed |
| Durability | PostgreSQL in production; ConcurrentHashMap in-memory for interview |
| Idempotency | Payments must be idempotent — same paymentRef = same result |
| Extensibility | Repayment scheme (Bullet/EMI/Vanilla) extensible → Strategy pattern |
| Failure Handling | Partial payment failure → rollback; LTV breach → cure window before auction |
| Scope | Single JVM for interview; PostgreSQL + Redis + Kafka for production |

```
"Based on my understanding:

Functional:
  1. Customer applies with gold → officer appraises → system sanctions → disburses
  2. Customer repays via EMI / Bullet / Vanilla
  3. System monitors LTV daily — auctions on unresolved breach
  4. Partial gold release and foreclosure supported

Non-Functional:
  1. Concurrent access — pessimistic lock on disbursal, optimistic lock on repayment
  2. Idempotent payments — paymentRef dedup via ConcurrentHashMap key set
  3. Repayment scheme extensible — Strategy pattern (InterestCalculator)
  4. Loan lifecycle enforced — State pattern (LoanState)
  5. Valuation pluggable — interface + implementations

Let me proceed unless you'd like to change anything."
```

---

## STEP 2 — CLASS DIAGRAM

Say: *"Let me model the core entities and their relationships."*

```
<<enum>>
+--------------------+
|    LoanStatus      |
+--------------------+
| APPLIED            |
| UNDER_REVIEW       |
| SANCTIONED         |
| DISBURSED          |
| ACTIVE             |
| OVERDUE            |
| FORECLOSED         |
| CLOSED             |
| AUCTIONED          |
+--------------------+

<<enum>>
+--------------------+
|  RepaymentScheme   |
+--------------------+
| BULLET             |
| EMI                |
| VANILLA            |
+--------------------+

<<enum>>
+--------------------+
|   GoldItemType     |
+--------------------+
| JEWELLERY          |
| COIN               |
+--------------------+

<<enum>>
+--------------------+
|   AuctionTrigger   |
+--------------------+
| LTV_BREACH         |
| DEFAULT_90_DPD     |
| CUSTOMER_REQUEST   |
+--------------------+

<<interface>>
+----------------------------------------------+
|           InterestCalculator                 |
+----------------------------------------------+
| + calculate(loan:GoldLoan,                   |
|     date:LocalDate): double                  |
| + generateSchedule(loan:GoldLoan):           |
|     List<EMI>                                |
+----------------------------------------------+
          ^           ^           ^
          |           |           |
  BulletInterest  EmiInterest  VanillaInterest
     Calculator     Calculator    Calculator

<<interface>>
+----------------------------------------------+
|          GoldValuationService                |
+----------------------------------------------+
| + valuate(item:GoldItem,                     |
|     branchId:String): GoldValuation          |
| + getCurrentRate(karat:int): double          |
+----------------------------------------------+
          ^
          |
  StandardGoldValuationService

<<interface>>
+----------------------------------------------+
|              LoanState                       |
+----------------------------------------------+
| + processPayment(ctx:GoldLoan,               |
|     amount:double): void                     |
| + triggerAuction(ctx:GoldLoan): void         |
| + foreclose(ctx:GoldLoan): void              |
| + getStatus(): LoanStatus                    |
+----------------------------------------------+
     ^           ^           ^          ^
     |           |           |          |
 AppliedState ActiveState OverdueState ClosedState ...

<<abstract>>
+----------------------------------------------+
|          BaseNotificationChannel             |
+----------------------------------------------+
| # customerId: String                         |
| # timestamp: Instant                         |
+----------------------------------------------+
| + log(): void                                |
| + send(message:String): void  (abstract)     |
+----------------------------------------------+
          ^           ^
          |           |
  SmsChannel      PushChannel

<<singleton>>
+----------------------------------------------+
|            GoldPriceFeed                    |
+----------------------------------------------+
| - instance: GoldPriceFeed   (static)         |
| - ratesCache: Map<Integer,Double>            |
+----------------------------------------------+
| + getInstance(): GoldPriceFeed  (static)     |
| + getCurrentRate(): double                   |
| + refresh(): void                            |
+----------------------------------------------+

<<factory>>
+----------------------------------------------+
|       InterestCalculatorFactory              |
+----------------------------------------------+
| + get(scheme:RepaymentScheme):               |
|     InterestCalculator  (static)             |
+----------------------------------------------+

+----------------------------------------------+
|                GoldItem                      |
+----------------------------------------------+
| - itemId: String                             |
| - type: GoldItemType                         |
| - grossWeightGrams: double                   |
| - netWeightGrams: double                     |
| - purityKarat: int                           |
| - storagePacketId: String                    |
| - releasedAt: Instant                        |
+----------------------------------------------+
| + getNetWeightGrams(): double                |
| + getPurityKarat(): int                      |
| + isReleased(): boolean                      |
+----------------------------------------------+

+----------------------------------------------+
|              GoldValuation                   |
+----------------------------------------------+
| - valuationId: String                        |
| - goldItemId: String                         |
| - marketRatePerGram: double                  |
| - valuationAmountINR: double                 |
| - valuedAt: Instant                          |
| - expiresAt: Instant                         |
+----------------------------------------------+
| + isExpired(): boolean                       |
| + getValuationAmountINR(): double            |
+----------------------------------------------+

+----------------------------------------------+
|                GoldLoan                      |   <<State Context>>
+----------------------------------------------+
| - loanId: String                             |
| - customerId: String                         |
| - principalINR: double                       |
| - interestRatePercent: double                |
| - repaymentScheme: RepaymentScheme           |
| - tenureMonths: int                          |
| - outstandingINR: double                     |
| - ltvRatio: double                           |
| - loanStatus: LoanStatus                     |
| - currentState: LoanState                    |
| - disbursedAt: Instant                       |
| - dueDate: Instant                           |
| - version: Long                              |   ← optimistic lock
+----------------------------------------------+
| + processPayment(amount:double): void        |
| + triggerAuction(): void                     |
| + foreclose(): void                          |
| + setState(state:LoanState): void            |
| + reduceOutstanding(amount:double): void     |
| + equals(o:Object): boolean                  |
| + hashCode(): int                            |
| + toString(): String                         |
+----------------------------------------------+

+----------------------------------------------+
|              LoanPayment                     |
+----------------------------------------------+
| - paymentId: String                          |
| - loanId: String                             |
| - amountINR: double                          |
| - paymentType: PaymentType                   |
| - paidAt: Instant                            |
| - paymentRef: String                         |   ← idempotency key
+----------------------------------------------+

+----------------------------------------------+
|              AuctionCase                     |
+----------------------------------------------+
| - auctionCaseId: String                      |
| - loanId: String                             |
| - triggerReason: AuctionTrigger              |
| - auctionDate: Instant                       |
| - reservePrice: double                       |
| - soldPrice: double                          |
| - auctionStatus: AuctionStatus               |
+----------------------------------------------+

Relationships:
Customer   <>————————> GoldLoan          Aggregation  (customer exists independently)
GoldLoan   <◆>————————> GoldItem        Composition  (items pledged; belong to loan)
GoldLoan   <◆>————————> LoanPayment     Composition  (payments belong to loan lifecycle)
GoldLoan   ————————> LoanState           Association  (state is a delegate, not owned)
GoldLoan   ————————> RepaymentScheme     Association  (enum)
GoldLoanService - - -> GoldValuationService  Dependency (used per request)
GoldLoanService - - -> KycCheckService       Dependency (used per request)
InterestCalculatorFactory - - -> InterestCalculator  Dependency (creates)
BaseNotificationChannel ————————|> (abstract)
SmsChannel - - - - |> BaseNotificationChannel   Implementation
PushChannel - - - - |> BaseNotificationChannel  Implementation
```

---

## STEP 3 — SEQUENCE DIAGRAM

Say: *"Let me trace the flow for the primary use case — loan application through to disbursal."*

```
Customer   LoanController  GoldLoanService  GoldValuationSvc  KycCheckService  LoanRepository
   |             |                |                 |                |               |
   |—apply()————>|                |                 |                |               |
   |             |—applyForLoan()>|                 |                |               |
   |             |                |—isVerified()—————————————————>|               |
   |             |                |<——— true ———————————————————|               |
   |             |                |—valuate(items)—>|                |               |
   |             |                |<—GoldValuation—|                |               |
   |             |                |—checkLtv()      |                |               |
   |             |                | (amount/value <= 0.75?)          |               |
   |             |                |—save(SANCTIONED)————————————————————————————>|
   |             |<—GoldLoan——————|                 |                |               |
   |<—201 Created|                |                 |                |               |
   |             |                |                 |                |               |
   |—disburse()—>|                |                 |                |               |
   |             |—disburse()————>|                 |                |               |
   |             |                |—findByIdWithLock()——————————————————————————>|
   |             |                |<———— GoldLoan (SANCTIONED) ————————————————|
   |             |                |—creditAccount() (bank call)      |               |
   |             |                |—setState(ACTIVE)                 |               |
   |             |                |—save(ACTIVE)————————————————————————————————>|
   |             |                |—notifyAsync(LOAN_DISBURSED)       |               |
   |             |<—DisbursalReceipt              |                |               |
   |<—200 OK————|                 |                 |                |               |
```

```
[alt: KYC not verified]
  KycCheckService ——> GoldLoanService: return false
  GoldLoanService: throw KycNotVerifiedException
  LoanController ——> Customer: 403 "KYC not complete"
[end]

[alt: LTV > 75%]
  GoldLoanService: throw LtvBreachException(requested, maxEligible)
  LoanController ——> Customer: 422 "Requested amount exceeds 75% LTV. Max eligible: Rs X"
[end]

[alt: Concurrent disbursal (two threads, same sanctioned loan)]
  Thread A: acquires pessimistic lock, checks SANCTIONED → proceeds
  Thread B: blocks on lock → acquires after A commits
  Thread B: reads status = ACTIVE → throws InvalidStateException("ACTIVE", "disburse")
[end]

[alt: Repayment on closed loan]
  GoldLoan.processPayment() delegates to ClosedLoanState
  ClosedLoanState: throw InvalidStateException("CLOSED", "processPayment")
[end]
```

---

## STEP 4 — DEFINE RELATIONSHIPS

Say: *"Let me define the relationships before writing code."*

```
GoldLoan — GoldItem            Composition  — item lifecycle tied to loan; cannot exist alone
GoldLoan — LoanPayment         Composition  — payments are destroyed when loan is purged
Customer — GoldLoan            Aggregation  — customer exists independently of loan
GoldLoan — LoanState           Association  — state is a behaviour delegate (not owned)
GoldLoanService — InterestCalculator    Dependency  — resolved at runtime via factory
GoldLoanService — GoldValuationService  Dependency  — used per valuation request
AbstractNotificationChannel — SmsChannel   Inheritance — SMS IS-A notification channel
```

> "I use an **interface** for `InterestCalculator` because Bullet, EMI, and Vanilla are completely different algorithms across unrelated computation paths — plug-and-play without touching `GoldLoanService`."

> "I use an **abstract class** for `BaseNotificationChannel` because all channels share `customerId`, `timestamp`, and the `log()` method — common state and behaviour. Only `send()` varies per channel."

> "I prefer **composition over inheritance** for the interest calculator — `GoldLoanService` holds an `InterestCalculator` reference rather than extending a base calculator class. If I inherit, I'm locked into one algorithm per class hierarchy."

> "This is a **has-a** relationship between `GoldLoan` and `LoanPayment`, not an **is-a**, so I compose rather than extend."

> "I use a **Singleton** for `GoldPriceFeed` — one shared cache of live gold rates, refreshed every 5 minutes by a background thread. No need for multiple instances."

---

## STEP 5 — FOLDER STRUCTURE

Say: *"Let me lay out the package structure before writing any code."*

```
gold-loan-service/
└── src/main/java/com/kotak/goldloan/
    ├── model/
    │   ├── GoldItem.java
    │   ├── GoldValuation.java
    │   ├── GoldLoan.java
    │   ├── LoanPayment.java
    │   └── AuctionCase.java
    ├── enums/
    │   ├── LoanStatus.java
    │   ├── RepaymentScheme.java
    │   ├── GoldItemType.java
    │   ├── PaymentType.java
    │   └── AuctionTrigger.java
    ├── interfaces/
    │   ├── InterestCalculator.java
    │   ├── GoldValuationService.java
    │   ├── LoanState.java
    │   └── CollateralReleaseService.java
    ├── strategy/
    │   ├── BulletInterestCalculator.java
    │   ├── EmiInterestCalculator.java
    │   └── VanillaInterestCalculator.java
    ├── state/
    │   ├── AppliedState.java
    │   ├── ActiveLoanState.java
    │   ├── OverdueLoanState.java
    │   ├── ClosedLoanState.java
    │   └── AuctionedLoanState.java
    ├── factory/
    │   └── InterestCalculatorFactory.java
    ├── observer/
    │   ├── LoanEventPublisher.java
    │   └── LoanNotificationListener.java
    ├── service/
    │   ├── GoldLoanService.java
    │   ├── StandardGoldValuationService.java
    │   ├── LoanRepaymentService.java
    │   └── LtvMonitoringJob.java
    ├── repository/
    │   └── LoanRepository.java
    ├── exception/
    │   ├── AppException.java
    │   ├── KycNotVerifiedException.java
    │   ├── LtvBreachException.java
    │   └── InvalidStateException.java
    ├── controller/
    │   └── GoldLoanController.java
    └── Application.java          ← main(), demo scenarios
```

---

## STEP 6 — CORE CODE

Say: *"Let me write the core code."*

### Enums

```java
public enum LoanStatus {
    APPLIED, UNDER_REVIEW, SANCTIONED, DISBURSED,
    ACTIVE, OVERDUE, FORECLOSED, CLOSED, AUCTIONED
}

public enum RepaymentScheme { BULLET, EMI, VANILLA }
```

### Custom Exception Hierarchy

```java
// Base — callers catch at the right granularity
public class AppException extends RuntimeException {
    private final String errorCode;
    public AppException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public String getErrorCode() { return errorCode; }
}

public class KycNotVerifiedException extends AppException {
    public KycNotVerifiedException(String customerId) {
        super("KYC_NOT_VERIFIED", "KYC not verified for customer: " + customerId);
    }
}

public class LtvBreachException extends AppException {
    public LtvBreachException(double requested, double maxEligible) {
        super("LTV_BREACH",
              "Requested Rs " + requested + " exceeds max eligible Rs " + maxEligible + " (75% LTV)");
    }
}

public class InvalidStateException extends AppException {
    public InvalidStateException(String currentState, String operation) {
        super("INVALID_STATE",
              "Cannot perform '" + operation + "' in state: " + currentState);
    }
}
```

### Core Model — GoldLoan (State Pattern Context)

```java
/*
 * Pattern: Context for State pattern.
 * Why: Loan behaviour on pay/auction/foreclose changes completely per lifecycle stage.
 *      Externalising to state objects keeps each transition rule in one place.
 * Principle: OCP — add new loan states without touching GoldLoan or GoldLoanService.
 * Principle: Encapsulation — outstandingINR only changes via reduceOutstanding().
 */
public class GoldLoan {

    private final String loanId;
    private final String customerId;
    private final double principalINR;
    private final double interestRatePercent;
    private final RepaymentScheme repaymentScheme;
    private final int tenureMonths;
    private double outstandingINR;
    private double ltvRatio;
    private LoanStatus loanStatus;
    private LoanState currentState;
    private final Instant disbursedAt;
    private Instant dueDate;
    private Long version;        // Optimistic lock — incremented by JPA on each UPDATE

    public GoldLoan(String loanId, String customerId, double principalINR,
                    double interestRate, RepaymentScheme scheme, int tenureMonths) {
        this.loanId = loanId;
        this.customerId = customerId;
        this.principalINR = principalINR;
        this.interestRatePercent = interestRate;
        this.repaymentScheme = scheme;
        this.tenureMonths = tenureMonths;
        this.outstandingINR = principalINR;
        this.loanStatus = LoanStatus.APPLIED;
        this.currentState = new AppliedState();
        this.disbursedAt = Instant.now();
        this.version = 0L;
    }

    // Delegate all lifecycle actions to the current state — callers never switch on status
    public void processPayment(double amount) { currentState.processPayment(this, amount); }
    public void triggerAuction()              { currentState.triggerAuction(this); }
    public void foreclose()                   { currentState.foreclose(this); }

    // Called by state objects to transition
    public void setState(LoanState newState) {
        this.currentState = newState;
        this.loanStatus = newState.getStatus();
    }

    // Encapsulated — only way to reduce outstanding
    public void reduceOutstanding(double amount) {
        this.outstandingINR = Math.max(0, this.outstandingINR - amount);
    }

    // Getters
    public String getLoanId()            { return loanId; }
    public String getCustomerId()        { return customerId; }
    public double getPrincipalINR()      { return principalINR; }
    public double getInterestRatePercent(){ return interestRatePercent; }
    public RepaymentScheme getScheme()   { return repaymentScheme; }
    public int getTenureMonths()         { return tenureMonths; }
    public double getOutstandingINR()    { return outstandingINR; }
    public LoanStatus getLoanStatus()    { return loanStatus; }
    public double getLtvRatio()          { return ltvRatio; }
    public void setLtvRatio(double v)    { this.ltvRatio = v; }
    public Instant getDisbursedAt()      { return disbursedAt; }
    public Instant getDueDate()          { return dueDate; }
    public void setDueDate(Instant d)    { this.dueDate = d; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoldLoan)) return false;
        return loanId.equals(((GoldLoan) o).loanId);
    }

    @Override
    public int hashCode() { return loanId.hashCode(); }

    @Override
    public String toString() {
        return "GoldLoan{loanId='" + loanId + "', status=" + loanStatus
               + ", outstanding=" + outstandingINR + "}";
    }
}
```

### State Pattern — LoanState Interface + Key States

```java
/*
 * Pattern: State
 * Why: processPayment() on ACTIVE reduces outstanding.
 *      processPayment() on CLOSED must throw — not silently succeed.
 *      Without State pattern this devolves into a giant if-else on LoanStatus.
 * Principle: SRP — each state owns exactly its valid transitions.
 * Principle: OCP — add FrozenLoanState without modifying GoldLoan.
 */
public interface LoanState {
    void processPayment(GoldLoan loan, double amount);
    void triggerAuction(GoldLoan loan);
    void foreclose(GoldLoan loan);
    LoanStatus getStatus();
}

public class ActiveLoanState implements LoanState {
    public void processPayment(GoldLoan loan, double amount) {
        loan.reduceOutstanding(amount);
        if (loan.getOutstandingINR() <= 0) {
            loan.setState(new ClosedLoanState());
        }
    }
    public void triggerAuction(GoldLoan loan) {
        loan.setState(new AuctionedLoanState());
    }
    public void foreclose(GoldLoan loan) {
        loan.setState(new ClosedLoanState());
    }
    public LoanStatus getStatus() { return LoanStatus.ACTIVE; }
}

public class OverdueLoanState implements LoanState {
    public void processPayment(GoldLoan loan, double amount) {
        loan.reduceOutstanding(amount);
        loan.setState(new ActiveLoanState());  // cure: move back to active
    }
    public void triggerAuction(GoldLoan loan) {
        loan.setState(new AuctionedLoanState());
    }
    public void foreclose(GoldLoan loan) {
        throw new InvalidStateException("OVERDUE", "foreclose — clear overdue amount first");
    }
    public LoanStatus getStatus() { return LoanStatus.OVERDUE; }
}

public class ClosedLoanState implements LoanState {
    public void processPayment(GoldLoan loan, double amount) {
        throw new InvalidStateException("CLOSED", "processPayment");
    }
    public void triggerAuction(GoldLoan loan) {
        throw new InvalidStateException("CLOSED", "triggerAuction");
    }
    public void foreclose(GoldLoan loan) {
        throw new InvalidStateException("CLOSED", "foreclose");
    }
    public LoanStatus getStatus() { return LoanStatus.CLOSED; }
}
```

### Abstract Class — BaseNotificationChannel

```java
/*
 * Abstract class — all channels share customerId, timestamp, and log() behaviour.
 * Only send() varies per channel (SMS gateway vs FCM push vs email SMTP).
 * Principle: DRY — shared state in base; subclasses implement only what differs.
 */
public abstract class BaseNotificationChannel {

    protected final String customerId;
    protected final Instant timestamp;

    protected BaseNotificationChannel(String customerId) {
        this.customerId = customerId;
        this.timestamp = Instant.now();
    }

    // Common behaviour — subclasses inherit this
    public void log() {
        System.out.println("[" + timestamp + "] Notification for customer: " + customerId);
    }

    // Subclasses must implement channel-specific delivery
    public abstract void send(String message);
}

public class SmsChannel extends BaseNotificationChannel {
    private final SmsGatewayClient smsClient;
    public SmsChannel(String customerId, SmsGatewayClient client) {
        super(customerId);
        this.smsClient = client;
    }
    @Override
    public void send(String message) {
        log();  // inherited
        smsClient.send(customerId, message);
    }
}

public class PushChannel extends BaseNotificationChannel {
    private final FcmClient fcmClient;
    public PushChannel(String customerId, FcmClient client) {
        super(customerId);
        this.fcmClient = client;
    }
    @Override
    public void send(String message) {
        log();
        fcmClient.pushNotification(customerId, message);
    }
}
```

### Singleton — GoldPriceFeed

```java
/*
 * Pattern: Singleton
 * Why: One shared cache of live gold rates across all valuation requests.
 *      Multiple instances would cause inconsistent LTV calculations.
 * Thread-safe: double-checked locking with volatile.
 */
public class GoldPriceFeed {

    private static volatile GoldPriceFeed instance;
    private final Map<Integer, Double> ratesCache = new ConcurrentHashMap<>();

    private GoldPriceFeed() {
        refresh();   // load initial rates
    }

    public static GoldPriceFeed getInstance() {
        if (instance == null) {
            synchronized (GoldPriceFeed.class) {
                if (instance == null) {
                    instance = new GoldPriceFeed();
                }
            }
        }
        return instance;
    }

    public double getCurrentRate() {
        return ratesCache.getOrDefault(24, 6500.0);  // fallback price per gram
    }

    public void refresh() {
        // In production: call external gold price API
        ratesCache.put(24, 6500.0);   // 24K rate per gram in INR
    }
}
```

### Strategy Pattern — InterestCalculator

```java
/*
 * Pattern: Strategy
 * Why: Bullet, EMI, Vanilla are fundamentally different algorithms.
 *      GoldLoanService must not know which one it's using at compile time.
 * Principle: OCP — adding FLEXI_EMI = new class, zero changes to GoldLoanService.
 * Principle: DIP — GoldLoanService depends on InterestCalculator interface.
 */
public interface InterestCalculator {
    double calculate(GoldLoan loan, LocalDate forDate);
    List<EMI> generateSchedule(GoldLoan loan);
}

public class EmiInterestCalculator implements InterestCalculator {
    public double calculate(GoldLoan loan, LocalDate forDate) {
        double p = loan.getPrincipalINR();
        double r = loan.getInterestRatePercent() / (12 * 100);
        int n = loan.getTenureMonths();
        return (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
    }
    public List<EMI> generateSchedule(GoldLoan loan) {
        double emiAmount = calculate(loan, LocalDate.now());
        List<EMI> schedule = new ArrayList<>();
        LocalDate due = LocalDate.now().plusMonths(1);
        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            schedule.add(new EMI(due, emiAmount, i));
            due = due.plusMonths(1);
        }
        return schedule;
    }
}

public class BulletInterestCalculator implements InterestCalculator {
    public double calculate(GoldLoan loan, LocalDate forDate) {
        long months = ChronoUnit.MONTHS.between(
            loan.getDisbursedAt().atZone(ZoneOffset.UTC).toLocalDate(), forDate);
        return loan.getPrincipalINR() * (loan.getInterestRatePercent() / 100) * (months / 12.0);
    }
    public List<EMI> generateSchedule(GoldLoan loan) {
        // Single payment at end: principal + all accumulated interest
        LocalDate maturity = loan.getDueDate().atZone(ZoneOffset.UTC).toLocalDate();
        double totalInterest = calculate(loan, maturity);
        return List.of(new EMI(maturity, loan.getPrincipalINR() + totalInterest, 1));
    }
}
```

### Factory — InterestCalculatorFactory

```java
/*
 * Pattern: Factory
 * Why: Centralise creation decision. GoldLoanService never calls new EmiInterestCalculator().
 *      Adding a new scheme = add a case here; no other class changes.
 * Principle: DIP — callers depend on InterestCalculator interface, not concrete classes.
 */
public class InterestCalculatorFactory {
    public static InterestCalculator get(RepaymentScheme scheme) {
        return switch (scheme) {
            case BULLET  -> new BulletInterestCalculator();
            case EMI     -> new EmiInterestCalculator();
            case VANILLA -> new VanillaInterestCalculator();
        };
    }
}
```

### Decorator Pattern — Logging Repayment Service

```java
/*
 * Pattern: Decorator
 * Why: Adding logging/audit to LoanRepaymentService without modifying it.
 *      Wraps the base service and adds cross-cutting audit trail behaviour.
 * Principle: OCP — base service stays closed for modification.
 */
public interface LoanRepaymentService {
    PaymentReceipt processPayment(String loanId, double amount, String paymentRef);
}

public class LoggingLoanRepaymentService implements LoanRepaymentService {

    private static final Logger log = LoggerFactory.getLogger(LoggingLoanRepaymentService.class);
    private final LoanRepaymentService delegate;  // wraps the real service

    public LoggingLoanRepaymentService(LoanRepaymentService delegate) {
        this.delegate = delegate;
    }

    public PaymentReceipt processPayment(String loanId, double amount, String paymentRef) {
        log.info("Payment.process | loanId={} amount={} ref={}", loanId, amount, paymentRef);
        PaymentReceipt receipt = delegate.processPayment(loanId, amount, paymentRef);
        log.info("Payment.process success | loanId={} receiptId={}", loanId, receipt.getReceiptId());
        return receipt;
    }
}
```

### Template Method — Base Loan Approval Step

```java
/*
 * Pattern: Template Method
 * Why: All approval steps share the same skeleton: validate → execute → record audit.
 *      Only the actual check differs per step (KYC, LTV, Credit Bureau).
 */
public abstract class LoanApprovalStep {

    private LoanApprovalStep next;

    public final LoanApplicationResult process(LoanApplication application) {
        // Template: validate → execute → hand off or complete
        if (!canHandle(application)) {
            return next != null
                ? next.process(application)
                : LoanApplicationResult.approved();
        }
        LoanApplicationResult result = execute(application);
        if (!result.isApproved()) return result;
        return next != null ? next.process(application) : result;
    }

    protected abstract boolean canHandle(LoanApplication application);
    protected abstract LoanApplicationResult execute(LoanApplication application);

    public void setNext(LoanApprovalStep next) { this.next = next; }
}

public class KycApprovalStep extends LoanApprovalStep {
    private final KycCheckService kycService;
    public KycApprovalStep(KycCheckService kycService) { this.kycService = kycService; }
    protected boolean canHandle(LoanApplication app) { return true; }
    protected LoanApplicationResult execute(LoanApplication app) {
        return kycService.isVerifiedForGoldLoan(app.getCustomerId())
            ? LoanApplicationResult.approved()
            : LoanApplicationResult.rejected("KYC not complete");
    }
}

public class LtvApprovalStep extends LoanApprovalStep {
    private final GoldValuationService valuationService;
    protected boolean canHandle(LoanApplication app) { return true; }
    protected LoanApplicationResult execute(LoanApplication app) {
        double goldValue = app.getItems().stream()
            .mapToDouble(i -> valuationService.valuate(i, app.getBranchId()).getValuationAmountINR())
            .sum();
        double maxAmount = goldValue * 0.75;
        return app.getRequestedAmountINR() <= maxAmount
            ? LoanApplicationResult.approved()
            : LoanApplicationResult.rejected("LTV exceeds 75%. Max: Rs " + maxAmount);
    }
}
```

### GoldValuationService — Purity Math

```java
/*
 * Pattern: Strategy (pluggable per branch type / digital gold / physical gold)
 * Why: Online appraisal, branch appraisal, digital-gold valuation have different sources.
 */
public class StandardGoldValuationService implements GoldValuationService {

    private final GoldPriceFeed priceFeed;

    public StandardGoldValuationService() {
        this.priceFeed = GoldPriceFeed.getInstance();   // Singleton
    }

    /*
     * Purity multiplier: 22K = 22/24 = 91.67% pure gold.
     * Price feed gives rate for 24K. Multiply by purity fraction to get effective rate.
     * netWeight (not gross) is used — excludes non-gold alloy weight.
     */
    public GoldValuation valuate(GoldItem item, String branchId) {
        double rateFor24K = priceFeed.getCurrentRate();
        double effectiveRate = rateFor24K * (item.getPurityKarat() / 24.0);
        double valuationAmount = item.getNetWeightGrams() * effectiveRate;

        return new GoldValuation(
            UUID.randomUUID().toString(),
            item.getItemId(),
            rateFor24K,
            valuationAmount,
            Instant.now(),
            Instant.now().plus(30, ChronoUnit.DAYS)   // RBI: valuation valid 30 days
        );
    }

    public double getCurrentRate(int karat) {
        return priceFeed.getCurrentRate() * (karat / 24.0);
    }
}
```

### GoldLoanService — Core Orchestration

```java
/*
 * What it does: Orchestrates gold loan application → sanction → disbursal.
 * Why it exists: Central coordination point — keeps Controller thin.
 * Pattern: Uses Chain of Responsibility for approval pipeline.
 * Principle: SRP — loan origination only; payment in LoanRepaymentService.
 * Principle: DIP — depends on interfaces; concrete classes injected via constructor.
 * Principle: Fail Fast — validate() at the top before any business logic runs.
 */
public class GoldLoanService {

    private static final Logger log = LoggerFactory.getLogger(GoldLoanService.class);

    private final GoldValuationService valuationService;
    private final KycCheckService kycService;
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;

    private static final double RBI_MAX_LTV = 0.75;

    public GoldLoanService(GoldValuationService valuationService,
                           KycCheckService kycService,
                           LoanRepository loanRepository,
                           NotificationService notificationService) {
        this.valuationService = valuationService;
        this.kycService = kycService;
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
    }

    public GoldLoan applyForLoan(LoanApplicationRequest request) {
        String reqId = UUID.randomUUID().toString();
        log.info("GoldLoan.apply | reqId={} customerId={} items={}",
                 reqId, request.getCustomerId(), request.getItems().size());

        // Fail Fast: validate at entry — invalid state never propagates deep
        validateRequest(request);

        // KYC check
        if (!kycService.isVerifiedForGoldLoan(request.getCustomerId())) {
            throw new KycNotVerifiedException(request.getCustomerId());
        }

        // Valuate items
        double totalGoldValue = request.getItems().stream()
            .mapToDouble(item ->
                valuationService.valuate(item, request.getBranchId()).getValuationAmountINR())
            .sum();

        // RBI LTV check
        double maxEligible = totalGoldValue * RBI_MAX_LTV;
        if (request.getRequestedAmountINR() > maxEligible) {
            throw new LtvBreachException(request.getRequestedAmountINR(), maxEligible);
        }

        GoldLoan loan = new GoldLoan(
            UUID.randomUUID().toString(),
            request.getCustomerId(),
            request.getRequestedAmountINR(),
            determineInterestRate(request),
            request.getScheme(),
            request.getTenureMonths()
        );

        loanRepository.save(loan);
        log.info("GoldLoan.apply success | reqId={} loanId={}", reqId, loan.getLoanId());
        return loan;
    }

    /*
     * Fail Fast: check state immediately after acquiring lock.
     * Concurrency: Pessimistic lock — disbursal is rare but catastrophic if run twice.
     */
    @Transactional
    public DisbursalReceipt disburse(String loanId) {
        GoldLoan loan = loanRepository.findByIdWithPessimisticLock(loanId);

        if (loan.getLoanStatus() != LoanStatus.SANCTIONED) {
            throw new InvalidStateException(loan.getLoanStatus().name(), "disburse");
        }

        bankingService.credit(loan.getCustomerId(), loan.getPrincipalINR());
        loan.setState(new ActiveLoanState());
        loanRepository.save(loan);

        // Async — do not block disbursement response on notification
        notificationService.notifyAsync("LOAN_DISBURSED", loan.getCustomerId(), loan.getLoanId());

        return new DisbursalReceipt(loanId, loan.getPrincipalINR(), Instant.now());
    }

    private void validateRequest(LoanApplicationRequest req) {
        if (req.getCustomerId() == null || req.getCustomerId().isBlank())
            throw new AppException("INVALID_INPUT", "customerId is required");
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new AppException("INVALID_INPUT", "At least one gold item required");
        if (req.getRequestedAmountINR() <= 0)
            throw new AppException("INVALID_INPUT", "Requested amount must be positive");
        if (req.getTenureMonths() < 3 || req.getTenureMonths() > 48)
            throw new AppException("INVALID_INPUT", "Tenure must be 3–48 months");
    }

    private double determineInterestRate(LoanApplicationRequest req) {
        return 10.5;  // base rate; in production: driven by scheme, amount, and customer profile
    }
}
```

### Application.java — Demo / Main

```java
/*
 * Entry point — demonstrates the full Gold Loan flow.
 * In production this is replaced by Spring Boot Application.java.
 */
public class Application {
    public static void main(String[] args) {
        // Wire up dependencies manually (Spring does this in production)
        GoldPriceFeed priceFeed = GoldPriceFeed.getInstance();
        GoldValuationService valuationService = new StandardGoldValuationService();
        KycCheckService kycService = new InMemoryKycCheckService();
        LoanRepository loanRepository = new InMemoryLoanRepository();
        NotificationService notificationService = new ConsoleNotificationService();

        // Wrap repayment service with logging decorator
        LoanRepaymentService baseRepayment = new DefaultLoanRepaymentService(loanRepository);
        LoanRepaymentService repaymentService = new LoggingLoanRepaymentService(baseRepayment);

        GoldLoanService loanService = new GoldLoanService(
            valuationService, kycService, loanRepository, notificationService);

        // Demo: apply for loan
        GoldItem ring = new GoldItem("ITEM-001", GoldItemType.JEWELLERY, 15.0, 12.5, 22);
        LoanApplicationRequest req = new LoanApplicationRequest(
            "CUST-001", List.of(ring), 50_000.0, RepaymentScheme.EMI, 12, "BRANCH-BLR");
        GoldLoan loan = loanService.applyForLoan(req);
        System.out.println("Applied: " + loan);

        // Demo: process payment
        PaymentReceipt receipt = repaymentService.processPayment(loan.getLoanId(), 5000.0, "REF-001");
        System.out.println("Payment: " + receipt);

        // Demo: State pattern — try payment on closed loan (throws)
        loan.setState(new ClosedLoanState());
        try {
            loan.processPayment(1000.0);
        } catch (InvalidStateException e) {
            System.out.println("Caught expected: " + e.getMessage());
        }
    }
}
```

---

## STEP 7 — CONCURRENCY

Say: *"Since multiple requests can hit this simultaneously, let me ensure thread safety."*

### Scenario 1 — Concurrent Repayments (High Frequency)

```java
/*
 * Tool: @Version — JPA optimistic lock
 * Why: Repayments are frequent; conflicts are rare.
 *      Optimistic lock avoids holding a DB lock for the full duration — better throughput.
 *      On conflict: OptimisticLockException is thrown; caller retries.
 */
@Entity
public class GoldLoan {
    @Version
    private Long version;  // JPA auto-increments on each UPDATE; throws on concurrent write
}
```

### Scenario 2 — Disbursal (Rare, High Stakes)

```java
/*
 * Tool: SELECT FOR UPDATE — pessimistic lock
 * Why: Disbursement runs once per loan. Sending money twice is catastrophic.
 *      Cost of holding a lock is fully justified for this low-frequency, high-impact operation.
 */
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT l FROM GoldLoan l WHERE l.loanId = :id")
GoldLoan findByIdWithPessimisticLock(@Param("id") String id);
```

### Scenario 3 — Idempotent Payments

```java
/*
 * Tool: ConcurrentHashMap key set
 * Why: Mobile clients retry on timeout. Same paymentRef must never debit twice.
 *      .add() on the set is atomic — only one thread "wins" insertion.
 */
public class DefaultLoanRepaymentService implements LoanRepaymentService {

    // In production: Redis with TTL; here: in-memory for interview scope
    private final Set<String> processedRefs = ConcurrentHashMap.newKeySet();

    public PaymentReceipt processPayment(String loanId, double amount, String paymentRef) {
        if (!processedRefs.add(paymentRef)) {
            // Duplicate — return same result without processing
            return fetchExistingReceipt(paymentRef);
        }
        // Actual processing
        GoldLoan loan = loanRepository.findById(loanId).orElseThrow();
        loan.processPayment(amount);  // delegates to current LoanState
        loanRepository.save(loan);
        return new PaymentReceipt(UUID.randomUUID().toString(), loanId, amount, paymentRef);
    }
}
```

### Scenario 4 — LTV Update (Background Job vs Repayment Thread)

```java
/*
 * Tool: AtomicReference
 * Why: LTV is computed by a nightly job and read by repayment checks.
 *      AtomicReference gives lock-free visibility for a single field update.
 */
private final AtomicReference<Double> currentLtv = new AtomicReference<>(0.0);

public void updateLtv(double newLtv) { currentLtv.set(newLtv); }
public boolean isLtvBreached()        { return currentLtv.get() > 0.75; }
```

> "I use `ConcurrentHashMap` key set for idempotency — segment-level locking means no full-map block on concurrent payment writes."

> "I use `@Version` optimistic lock for repayments — conflicts are rare, so a pessimistic lock would create unnecessary contention on the loan row."

> "Disbursal uses `SELECT FOR UPDATE` — it's rare but catastrophic if it runs twice. The cost of a pessimistic lock here is completely justified."

> "I use `AtomicInteger` with CAS for counters to avoid a `synchronized` block — lock-free and more performant under high contention."

---

## STEP 8 — DESIGN PATTERNS CALLED OUT BY NAME

Call out the pattern BY NAME and explain WHY.

| Pattern | Applied Where | Senior Phrase |
|---------|---------------|---------------|
| **State** | `GoldLoan` lifecycle | "Behaviour on pay/auction/foreclose is completely different per lifecycle stage — State externalises each transition." |
| **Strategy** | `InterestCalculator` | "I use Strategy so I can swap Bullet/EMI/Vanilla without touching GoldLoanService." |
| **Factory** | `InterestCalculatorFactory` | "I use Factory to centralise creation and hide the `new` keyword from callers." |
| **Singleton** | `GoldPriceFeed` | "One shared rate cache — multiple instances would cause inconsistent LTV calculations." |
| **Observer** | `LoanEventPublisher` | "Observer decouples the loan service from notification and audit — add handlers without modifying core logic." |
| **Builder** | `GoldValuation` construction | "Builder prevents telescoping constructors and makes optional-field construction readable." |
| **Decorator** | `LoggingLoanRepaymentService` | "Decorator wraps the base service and adds audit logging without modifying it." |
| **Template Method** | `LoanApprovalStep` | "Template Method defines the approval skeleton in the base class — KYC/LTV steps only implement what differs." |
| **Chain of Responsibility** | Loan approval pipeline | "Each step can short-circuit the chain; steps are added/removed without touching the others." |

---

## STEP 9 — DESIGN PRINCIPLES

Say: *"Let me call out the SOLID principles I'm applying."*

| Principle | Applied in Gold Loan Design |
|-----------|----------------------------|
| **SRP** | `GoldLoanService` handles origination only. `LoanRepaymentService` handles payments. `LtvMonitoringJob` handles LTV. Each has one reason to change. |
| **OCP** | Adding FLEXI_EMI = new `FlexiEmiInterestCalculator`. No changes to `GoldLoanService` or existing factory cases. |
| **LSP** | `ActiveLoanState`, `OverdueLoanState`, `ClosedLoanState` all substitute for `LoanState` without breaking callers. `ClosedLoanState` throwing is correct behaviour, not a violation. |
| **ISP** | `GoldValuationService` has only valuation methods. `CollateralReleaseService` has only release methods. Neither forces callers to depend on unused methods. |
| **DIP** | `GoldLoanService` depends on `GoldValuationService` interface, not `StandardGoldValuationService`. All injected via constructor. |
| **Composition over Inheritance** | `GoldLoanService` composes with `InterestCalculator` rather than extending a base calculator class — avoids rigid hierarchy. |
| **Encapsulation** | All `GoldLoan` fields are private. Outstanding balance only changes via `reduceOutstanding()`. Status only changes via `setState()`. |
| **Fail Fast** | `validateRequest()` runs first — invalid inputs never propagate into KYC checks, valuation, or DB writes. |

---

## STEP 10 — EDGE CASE CHECKLIST

Say: *"Let me quickly validate the failure scenarios."*

```
□ Null customerId                      → validateRequest() throws INVALID_INPUT
□ Empty items list                     → validateRequest() throws "At least one gold item required"
□ Requested amount <= 0               → validateRequest() throws INVALID_INPUT
□ Tenure < 3 or > 48 months          → validateRequest() throws INVALID_INPUT
□ Gold purity invalid (e.g. 25 karat) → GoldItem constructor validates karat in {18, 20, 22, 24}
□ KYC expired                         → isVerifiedForGoldLoan() returns false → KycNotVerifiedException
□ LTV > 75%                           → throws LtvBreachException with maxEligible in message
□ Valuation expired (> 30 days old)   → GoldValuation.isExpired() checked before disbursal
□ Same loan disbursed twice            → Pessimistic lock + SANCTIONED check → InvalidStateException
□ Same payment ref submitted twice     → ConcurrentHashMap.newKeySet().add() returns false → same receipt
□ Payment on CLOSED loan              → ClosedLoanState.processPayment() throws InvalidStateException
□ Payment on AUCTIONED loan           → AuctionedLoanState.processPayment() throws InvalidStateException
□ Foreclosure on OVERDUE loan         → OverdueLoanState.foreclose() throws InvalidStateException
□ Gold price drops after disbursal    → LtvMonitoringJob detects, notifies; escalates to auction after 7 days
□ Partial release — item not in loan  → CollateralReleaseService validates itemId belongs to loanId
□ External bank API down (credit)     → disburse() rolls back; loan stays SANCTIONED; customer retries
```

---

## STEP 11 — LOGGING

Say: *"I'll add structured logging — no sensitive data."*

```java
public GoldLoan applyForLoan(LoanApplicationRequest request) {
    String reqId = UUID.randomUUID().toString();
    log.info("GoldLoan.apply | reqId={} customerId={} itemCount={} requestedAmt={}",
             reqId, request.getCustomerId(), request.getItems().size(),
             request.getRequestedAmountINR());
    try {
        GoldLoan loan = doApply(request);
        log.info("GoldLoan.apply success | reqId={} loanId={} sanctionedAmt={}",
                 reqId, loan.getLoanId(), loan.getPrincipalINR());
        return loan;
    } catch (KycNotVerifiedException e) {
        log.warn("GoldLoan.apply blocked — KYC | reqId={} customerId={}",
                 reqId, request.getCustomerId());
        throw e;
    } catch (LtvBreachException e) {
        log.warn("GoldLoan.apply blocked — LTV | reqId={} errorCode={} reason={}",
                 reqId, e.getErrorCode(), e.getMessage());
        throw e;
    } catch (AppException e) {
        log.error("GoldLoan.apply failed | reqId={} errorCode={} reason={}",
                  reqId, e.getErrorCode(), e.getMessage());
        throw e;
    }
    // NEVER log: account number, Aadhaar, PAN, item photograph URLs, customer address
}
```

---

## STEP 12 — SCALING DISCUSSION

Say: *"If this needs to scale beyond a single JVM..."*

```
Customer App
      │
      ▼
[API Gateway]          ← JWT auth, rate limiting (Rs 1.5 cr cap check), TLS
      │
      ▼
[Load Balancer]        ← Stateless GoldLoanService instances
      │
      ▼
[GoldLoanService]
      │
      ├──> [Redis]         ← Idempotency keys (TTL 24h), valuation cache (TTL 30 days)
      │
      ├──> [PostgreSQL]    ← ACID — GoldLoan with @Version; SELECT FOR UPDATE on disbursal
      │                       Shard by customerId for large portfolio
      │
      ├──> [GoldPriceFeed] ← External vendor; cached in Redis, refreshed every 5 min
      │
      └──> [Kafka]         ← LOAN_DISBURSED, PAYMENT_RECEIVED, LTV_BREACH, AUCTION_TRIGGERED
                │                → Notification, Audit, Risk analytics (independent consumers)
                ▼
           [Workers]       ← LTV monitor, notification sender — scale independently

LTV at scale: Subscribe to gold price change events.
On each price tick, recompute LTV only for loans whose pledged gold type changed.
Reduces job from O(all active loans) to O(affected loans).
```

---
---

# UPI PAYMENT SYSTEM

---

## STEP 1 — CLARIFY REQUIREMENTS

Say: *"Before jumping into implementation, let me clarify both functional and non-functional requirements."*

---

### Part A — Functional Requirements

Say: *"First, let me understand the core use cases."*

**Actors:** User (Sender), User (Receiver), Merchant, NPCI Switch, Issuer Bank, Acquirer Bank

```
Functional Requirements:
- User can register and link a bank account
- User can create a VPA (Virtual Payment Address) e.g. abhishek@kotak
- User can set and verify MPIN (stored as BCrypt hash — never plain text)
- User can resolve a VPA to see the receiver's name before paying
- User can initiate P2P payment (send to UPI ID / mobile / QR)
- User can initiate P2M payment (pay merchant via QR or collect)
- Merchant can create a Collect Request (pull money from a user)
- User can approve or decline a Collect Request
- User can check transaction status
- User can raise a reversal request on a failed transaction
- User can view transaction history (paginated)
- System enforces NPCI limits (Rs 1 lakh per txn, 20 txns/day for unverified users)
```

---

### Part B — Non-Functional Requirements

Say: *"Now let me clarify the quality constraints."*

| Area | Decision for UPI |
|------|-----------------|
| Scale | NPCI peak ~6000 TPS; our PSP layer must handle ~500 TPS per instance |
| Concurrency | Same idempotency key hitting service concurrently — must not double-debit |
| Consistency | Strong — debit and credit must be atomic; partial failure must compensate |
| Latency | < 3 seconds end-to-end (NPCI mandate) |
| Availability | 99.99% — payments cannot go down |
| Durability | PostgreSQL for transactions; Redis for idempotency keys and rate limits |
| Idempotency | Critical — mobile retries are common; same key = same result, no double debit |
| Extensibility | New bank adapters added without touching PaymentService |
| Failure Handling | Credit fails → Saga compensates by reversing debit |
| Scope | Distributed in production; ConcurrentHashMap in-memory for interview |

```
"Based on my understanding:

Functional:
  1. Register, link account, create VPA, set MPIN
  2. P2P and P2M payments; Collect Requests
  3. Status check, reversal, transaction history

Non-Functional:
  1. Idempotency — ConcurrentHashMap dedup + DB UNIQUE constraint on idempotencyKey
  2. Distributed transaction — Saga pattern (debit → credit, compensate on failure)
  3. VPA resolution cached — ConcurrentHashMap (Redis in production)
  4. MPIN — BCrypt, never logged
  5. Rate limiting — AtomicInteger per user per day

Let me proceed unless you'd like to change anything."
```

---

## STEP 2 — CLASS DIAGRAM

Say: *"Let me model the core entities and their relationships."*

```
<<enum>>
+--------------------+
| TransactionStatus  |
+--------------------+
| INITIATED          |
| PENDING_DEBIT      |
| PENDING_CREDIT     |
| SUCCESS            |
| FAILED             |
| REVERSED           |
| TIMED_OUT          |
+--------------------+

<<enum>>
+--------------------+
| TransactionType    |
+--------------------+
| P2P                |
| P2M                |
| COLLECT_PAY        |
| SELF_TRANSFER      |
+--------------------+

<<enum>>
+--------------------+
|   CollectStatus    |
+--------------------+
| PENDING            |
| APPROVED           |
| DECLINED           |
| EXPIRED            |
| PAID               |
+--------------------+

<<enum>>
+--------------------+
|    VpaStatus       |
+--------------------+
| ACTIVE             |
| SUSPENDED          |
| DELETED            |
+--------------------+

<<interface>>
+----------------------------------------------+
|              BankAdapter                     |
+----------------------------------------------+
| + debit(req:DebitRequest): DebitResponse     |
| + credit(req:CreditRequest): CreditResponse  |
| + verifyMpin(accountId:String,               |
|     hashedMpin:String): boolean              |
+----------------------------------------------+
          ^              ^
          |              |
  KotakBankAdapter  IciciBankAdapter

<<interface>>
+----------------------------------------------+
|           NpciSwitchAdapter                  |
+----------------------------------------------+
| + routeTransaction(req:NpciTxnRequest):      |
|     NpciResponse                             |
| + checkSettlement(rrn:String):               |
|     NpciSettlementStatus                     |
+----------------------------------------------+

<<interface>>
+----------------------------------------------+
|               RateLimiter                    |
+----------------------------------------------+
| + allow(userId:String,                       |
|     type:TransactionType): boolean           |
+----------------------------------------------+
          ^
          |
    UpiRateLimiter

<<interface>>
+----------------------------------------------+
|               VpaService                     |
+----------------------------------------------+
| + createVpa(userId,handle,accountId): VPA    |
| + resolve(address:String):                   |
|     Optional<VPA>                            |
| + deleteVpa(vpaId:String): void              |
+----------------------------------------------+

+----------------------------------------------+
|                  User                        |
+----------------------------------------------+
| - userId: String                             |
| - mobileNumber: String                       |
| - name: String                               |
| - kycStatus: KycStatus                       |
| - deviceFingerprint: String                  |
| - mpin: String                               |   ← BCrypt hash
+----------------------------------------------+
| + isKycComplete(): boolean                   |
| + equals(o:Object): boolean                  |
| + hashCode(): int                            |
+----------------------------------------------+

+----------------------------------------------+
|                   VPA                        |
+----------------------------------------------+
| - vpaId: String                              |
| - address: String                            |   e.g. abhishek@kotak
| - userId: String                             |
| - linkedAccountId: String                    |
| - isDefault: boolean                         |
| - status: VpaStatus                          |
+----------------------------------------------+

+----------------------------------------------+
|            LinkedBankAccount                 |
+----------------------------------------------+
| - accountId: String                          |
| - userId: String                             |
| - bankIfsc: String                           |
| - maskedAccountNumber: String                |
| - bankName: String                           |
| - isVerified: boolean                        |
+----------------------------------------------+

+----------------------------------------------+
|               Transaction                    |
+----------------------------------------------+
| - transactionId: String                      |   ← UUID
| - idempotencyKey: String                     |   ← UNIQUE constraint in DB
| - rrn: String                                |   ← NPCI Reference
| - senderVpa: String                          |
| - receiverVpa: String                        |
| - amountINR: double                          |
| - transactionType: TransactionType           |
| - status: TransactionStatus                  |
| - initiatedAt: Instant                       |
| - completedAt: Instant                       |
| - failureReason: String                      |
+----------------------------------------------+
| + markSuccess(rrn:String): void              |
| + markFailed(reason:String): void            |
| + markReversed(): void                       |
| + equals(o:Object): boolean                  |
| + hashCode(): int                            |
| + toString(): String                         |
+----------------------------------------------+

+----------------------------------------------+
|             CollectRequest                   |
+----------------------------------------------+
| - collectId: String                          |
| - payerVpa: String                           |
| - payeeVpa: String                           |
| - amountINR: double                          |
| - note: String                               |
| - expiresAt: Instant                         |
| - status: CollectStatus                      |
+----------------------------------------------+
| + isExpired(): boolean                       |
+----------------------------------------------+

Relationships:
User   <◆>————————> VPA                  Composition  (VPA cannot exist without User)
User   <◆>————————> LinkedBankAccount    Composition  (account linked by User)
User   ————————> Transaction             Association  (as sender via VPA)
Transaction - - -> VPA                   Dependency   (resolves VPA at payment time)
PaymentService - - -> BankAdapter        Dependency   (per payment request)
PaymentService - - -> NpciSwitchAdapter  Dependency   (per routing call)
PaymentService <◆> RateLimiter          Composition  (owned by service)
PaymentService <◆> VpaService           Composition  (owned by service)
KotakBankAdapter - - -|> BankAdapter    Implementation
UpiRateLimiter - - -|> RateLimiter      Implementation
```

---

## STEP 3 — SEQUENCE DIAGRAM

Say: *"Let me trace the P2P payment flow — the primary use case."*

```
UserApp  PaymentController  PaymentService  BankAdapter  NpciAdapter  TxnRepository
  |            |                  |              |             |              |
  |—pay()—————>|                  |              |             |              |
  |            |—initiatePayment()>              |             |              |
  |            |                  |—checkIdem(key)————————————————————————>|
  |            |                  |<—— null (first time) ——————————————————|
  |            |                  |—checkRateLimit(userId)    |             |
  |            |                  |—resolveVpa(senderVpa)     |             |
  |            |                  |—resolveVpa(receiverVpa)   |             |
  |            |                  |—createTxn(INITIATED)——————————————————>|
  |            |                  |              |             |              |
  |            |                  |—debit()——————>|            |              |
  |            |                  |              |—verifyMpin()|             |
  |            |                  |              |—debitAcct() |             |
  |            |                  |<—DebitResponse|            |              |
  |            |                  |—updateTxn(PENDING_CREDIT)—————————————>|
  |            |                  |              |             |              |
  |            |                  |—routeCredit()—————————————>|             |
  |            |                  |              |    |—creditReceiverBank() |
  |            |                  |              |    |<—RRN                 |
  |            |                  |<—NpciResp(RRN)————|             |        |
  |            |                  |—updateTxn(SUCCESS,rrn)————————————————>|
  |            |                  |—storeIdemKey(key, txnId)               |
  |            |                  |—notifyAsync(DEBIT)        |             |
  |            |                  |—notifyAsync(CREDIT)       |             |
  |            |<—TransactionReceipt              |             |              |
  |<—200 OK———|                  |              |             |              |
```

```
[alt: Rate limit exceeded]
  PaymentService: throw RateLimitExceededException
  PaymentController ——> UserApp: 429 "Daily UPI limit reached"
[end]

[alt: MPIN wrong]
  BankAdapter ——> PaymentService: throw MpinMismatchException
  PaymentService ——> TxnRepository: updateTxn(FAILED)
  PaymentController ——> UserApp: 422 "MPIN incorrect"
[end]

[alt: Credit to receiver fails — Saga compensation]
  NpciAdapter ——> PaymentService: throw CreditFailedException
  PaymentService ——> BankAdapter: credit(compensatingCreditReq)  ← reverse the debit
  PaymentService ——> TxnRepository: updateTxn(REVERSED)
  PaymentController ——> UserApp: 202 "Payment reversed. Money is safe."
[end]

[alt: Duplicate idempotencyKey (retry)]
  TxnRepository ——> PaymentService: existing transactionId
  PaymentService ——> UserApp: same receipt, no processing
[end]
```

---

## STEP 4 — DEFINE RELATIONSHIPS

Say: *"Let me define the relationships before writing code."*

```
PaymentService — BankAdapter        Dependency  — used per request; different per bank
PaymentService — NpciSwitchAdapter  Dependency  — routes to NPCI per transaction
PaymentService — RateLimiter        Composition — owned by service, checked on every payment
PaymentService — VpaService         Composition — owned by service
Transaction — User                  Association — via senderVpa / receiverVpa strings
KotakBankAdapter implements BankAdapter     (Implementation)
UpiRateLimiter implements RateLimiter       (Implementation)
```

> "I use an **interface** for `BankAdapter` because Kotak, ICICI, HDFC have completely different API shapes — classic Adapter pattern. `PaymentService` never knows which bank it's calling."

> "I use a **Saga** here instead of 2PC because UPI spans two independent banks and NPCI. 2PC requires locking all participants — impossible at NPCI scale. Saga with compensation is the industry approach."

> "I prefer **composition over inheritance** for `RateLimiter` — `PaymentService` holds a `RateLimiter` reference rather than extending a base rate-limiting class."

> "This is a **has-a** relationship between `User` and `VPA` — `VPA` cannot exist without a `User`, so I use composition."

---

## STEP 5 — FOLDER STRUCTURE

Say: *"Let me lay out the package structure before writing any code."*

```
upi-service/
└── src/main/java/com/kotak/upi/
    ├── model/
    │   ├── User.java
    │   ├── VPA.java
    │   ├── LinkedBankAccount.java
    │   ├── Transaction.java
    │   └── CollectRequest.java
    ├── enums/
    │   ├── TransactionStatus.java
    │   ├── TransactionType.java
    │   ├── CollectStatus.java
    │   └── VpaStatus.java
    ├── interfaces/
    │   ├── BankAdapter.java
    │   ├── NpciSwitchAdapter.java
    │   ├── RateLimiter.java
    │   └── VpaService.java
    ├── adapter/
    │   ├── KotakBankAdapter.java
    │   └── IciciBankAdapter.java
    ├── saga/
    │   └── UpiPaymentSaga.java
    ├── ratelimit/
    │   └── UpiRateLimiter.java
    ├── service/
    │   ├── PaymentService.java
    │   ├── VpaServiceImpl.java
    │   └── CollectRequestService.java
    ├── repository/
    │   └── TransactionRepository.java
    ├── exception/
    │   ├── AppException.java
    │   ├── VpaNotFoundException.java
    │   ├── MpinMismatchException.java
    │   ├── RateLimitExceededException.java
    │   └── PaymentReversedException.java
    ├── controller/
    │   └── PaymentController.java
    └── Application.java
```

---

## STEP 6 — CORE CODE

Say: *"Let me write the core code."*

### Custom Exception Hierarchy

```java
public class AppException extends RuntimeException {
    private final String errorCode;
    public AppException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
    public String getErrorCode() { return errorCode; }
}

public class VpaNotFoundException     extends AppException {
    public VpaNotFoundException(String address) {
        super("VPA_NOT_FOUND", "VPA not found: " + address);
    }
}
public class MpinMismatchException    extends AppException {
    public MpinMismatchException() { super("MPIN_MISMATCH", "MPIN is incorrect"); }
}
public class RateLimitExceededException extends AppException {
    public RateLimitExceededException(String userId) {
        super("RATE_LIMIT", "Daily UPI limit exceeded for user: " + userId);
    }
}
public class PaymentReversedException extends AppException {
    public PaymentReversedException(String msg, Throwable cause) {
        super("PAYMENT_REVERSED", msg);
    }
}
```

### Core Model — Transaction

```java
/*
 * Model: Transaction
 * Immutable key fields (transactionId, idempotencyKey, amounts) — set at creation.
 * Mutable status, rrn, failureReason — mutated only through named methods.
 * Principle: Encapsulation — status never set directly via setter.
 */
public class Transaction {

    private final String transactionId;
    private final String idempotencyKey;   // client-supplied; UNIQUE constraint in DB
    private final String senderVpa;
    private final String receiverVpa;
    private final double amountINR;
    private final TransactionType transactionType;
    private TransactionStatus status;
    private String rrn;
    private String failureReason;
    private final Instant initiatedAt;
    private Instant completedAt;

    public Transaction(String idempotencyKey, String senderVpa, String receiverVpa,
                       double amountINR, TransactionType type) {
        this.transactionId = UUID.randomUUID().toString();
        this.idempotencyKey = idempotencyKey;
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.amountINR = amountINR;
        this.transactionType = type;
        this.status = TransactionStatus.INITIATED;
        this.initiatedAt = Instant.now();
    }

    public void markSuccess(String rrn) {
        this.status = TransactionStatus.SUCCESS;
        this.rrn = rrn;
        this.completedAt = Instant.now();
    }
    public void markFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = Instant.now();
    }
    public void markReversed() {
        this.status = TransactionStatus.REVERSED;
        this.completedAt = Instant.now();
    }

    // Getters
    public String getTransactionId()    { return transactionId; }
    public String getIdempotencyKey()   { return idempotencyKey; }
    public TransactionStatus getStatus(){ return status; }
    public String getRrn()              { return rrn; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transaction)) return false;
        return transactionId.equals(((Transaction) o).transactionId);
    }
    @Override public int hashCode()    { return transactionId.hashCode(); }
    @Override public String toString() {
        return "Transaction{id='" + transactionId + "', status=" + status + "}";
    }
}
```

### Adapter Pattern — BankAdapter

```java
/*
 * Pattern: Adapter
 * Why: Kotak Core Banking, ICICI API, HDFC API are different shapes.
 *      BankAdapter is the single contract — PaymentService never knows which bank it calls.
 * Principle: DIP — PaymentService depends on BankAdapter interface, not KotakBankAdapter.
 */
public interface BankAdapter {
    DebitResponse debit(DebitRequest request);
    CreditResponse credit(CreditRequest request);
    boolean verifyMpin(String linkedAccountId, String hashedMpin);
}

public class KotakBankAdapter implements BankAdapter {
    private final KotakCoreBankingClient client;

    public KotakBankAdapter(KotakCoreBankingClient client) {
        this.client = client;
    }

    public DebitResponse debit(DebitRequest request) {
        // Translate our DebitRequest → Kotak's internal API format
        KotakDebitPayload payload = KotakDebitPayload.from(request);
        KotakApiResponse resp = client.debitAccount(payload);
        if (!resp.isSuccess()) throw new AppException("DEBIT_FAILED", resp.getErrorMessage());
        return DebitResponse.from(resp);
    }

    public CreditResponse credit(CreditRequest request) {
        KotakCreditPayload payload = KotakCreditPayload.from(request);
        return CreditResponse.from(client.creditAccount(payload));
    }

    public boolean verifyMpin(String accountId, String hashedMpin) {
        return client.verifyPin(accountId, hashedMpin).isSuccess();
    }
}
```

### Saga Pattern — UpiPaymentSaga

```java
/*
 * Pattern: Saga (explicit compensation)
 * Why: UPI spans Issuer Bank + NPCI + Acquirer Bank — all independent.
 *      2PC locks all participants; impossible at NPCI scale.
 *      Saga: if credit fails, compensate by reversing the debit.
 * Principle: SRP — saga owns only the distributed transaction coordination.
 */
public class UpiPaymentSaga {

    private static final Logger log = LoggerFactory.getLogger(UpiPaymentSaga.class);

    private final BankAdapter issuerBankAdapter;
    private final NpciSwitchAdapter npciAdapter;
    private final TransactionRepository txnRepository;

    public UpiPaymentSaga(BankAdapter issuerBankAdapter,
                          NpciSwitchAdapter npciAdapter,
                          TransactionRepository txnRepository) {
        this.issuerBankAdapter = issuerBankAdapter;
        this.npciAdapter = npciAdapter;
        this.txnRepository = txnRepository;
    }

    public TransactionReceipt execute(PaymentRequest request, Transaction txn) {
        log.info("UpiSaga.execute | txnId={} amt={}", txn.getTransactionId(), request.getAmountINR());

        // Step 1: Debit sender at Issuer Bank
        DebitResponse debitResponse;
        try {
            debitResponse = issuerBankAdapter.debit(
                new DebitRequest(request.getSenderAccountId(), request.getAmountINR(), txn.getTransactionId()));
            txn.setStatus(TransactionStatus.PENDING_CREDIT);
            txnRepository.save(txn);
        } catch (Exception e) {
            txn.markFailed("Debit failed: " + e.getMessage());
            txnRepository.save(txn);
            throw new AppException("DEBIT_FAILED", e.getMessage());
        }

        // Step 2: Credit receiver via NPCI
        try {
            NpciResponse npciResp = npciAdapter.routeTransaction(
                NpciTransactionRequest.of(request, debitResponse.getDebitRef()));
            txn.markSuccess(npciResp.getRrn());
            txnRepository.save(txn);
            log.info("UpiSaga.execute success | txnId={} rrn={}", txn.getTransactionId(), npciResp.getRrn());
            return TransactionReceipt.from(txn);

        } catch (Exception e) {
            // Compensating transaction — reverse the debit
            log.warn("UpiSaga credit failed, reversing | txnId={}", txn.getTransactionId());
            try {
                issuerBankAdapter.credit(CreditRequest.compensate(debitResponse.getDebitRef(), request));
            } catch (Exception reverseEx) {
                log.error("CRITICAL: Reversal failed | txnId={} — manual intervention needed",
                          txn.getTransactionId());
                // In production: push to dead-letter queue, alert ops team
            }
            txn.markReversed();
            txnRepository.save(txn);
            throw new PaymentReversedException("Payment reversed — money is safe", e);
        }
    }
}
```

### Rate Limiter

```java
/*
 * Pattern: Strategy (interface — swap Sliding Window / Token Bucket without touching PaymentService)
 * Tool: ConcurrentHashMap + AtomicInteger for lock-free per-user counter
 * Why: New users capped at 20 txns/day per NPCI rule.
 *      AtomicInteger.incrementAndGet() is a CAS operation — no synchronized needed.
 * Principle: OCP — swap to TokenBucketRateLimiter without modifying PaymentService.
 */
public class UpiRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public UpiRateLimiter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean allow(String userId, TransactionType type) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.isKycComplete()) return true;  // KYC-verified: no PSP-level limit

        String key = userId + ":" + LocalDate.now();
        AtomicInteger count = counters.computeIfAbsent(key, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= 20;
        // Cleanup: scheduled job removes yesterday's keys; or use a TTL wrapper
    }
}
```

### PaymentService — Orchestration

```java
/*
 * What it does: Entry point for all payments. Validates, deduplicates, delegates to Saga.
 * Why it exists: Keeps Controller thin; owns rate limit + idempotency + VPA resolution.
 * Principle: SRP — validation/dedup only; bank I/O in UpiPaymentSaga.
 * Principle: DIP — depends on RateLimiter, VpaService, BankAdapter interfaces.
 * Principle: Fail Fast — validate() runs before any expensive operation.
 */
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final VpaService vpaService;
    private final RateLimiter rateLimiter;
    private final UpiPaymentSaga saga;
    private final TransactionRepository txnRepository;

    // In-memory dedup (Redis in production with 24h TTL)
    private final Map<String, String> idempotencyStore = new ConcurrentHashMap<>();

    public PaymentService(VpaService vpaService, RateLimiter rateLimiter,
                          UpiPaymentSaga saga, TransactionRepository txnRepository) {
        this.vpaService = vpaService;
        this.rateLimiter = rateLimiter;
        this.saga = saga;
        this.txnRepository = txnRepository;
    }

    public TransactionReceipt initiatePayment(PaymentRequest request) {
        String reqId = UUID.randomUUID().toString();
        log.info("Payment.initiate | reqId={} senderVpa={} amt={}",
                 reqId, request.getSenderVpa(), request.getAmountINR());

        // Fail Fast: validate at entry
        validatePaymentRequest(request);

        // Idempotency check — atomic via ConcurrentHashMap
        String existingTxnId = idempotencyStore.get(request.getIdempotencyKey());
        if (existingTxnId != null) {
            log.info("Payment.initiate duplicate | reqId={} key={}", reqId, request.getIdempotencyKey());
            return txnRepository.findById(existingTxnId).toReceipt();
        }

        // Rate limit
        if (!rateLimiter.allow(request.getSenderUserId(), request.getType())) {
            throw new RateLimitExceededException(request.getSenderUserId());
        }

        // Resolve VPAs
        vpaService.resolve(request.getSenderVpa())
            .orElseThrow(() -> new VpaNotFoundException(request.getSenderVpa()));
        vpaService.resolve(request.getReceiverVpa())
            .orElseThrow(() -> new VpaNotFoundException(request.getReceiverVpa()));

        // Create transaction
        Transaction txn = new Transaction(
            request.getIdempotencyKey(), request.getSenderVpa(),
            request.getReceiverVpa(), request.getAmountINR(), request.getType());
        txnRepository.save(txn);

        // Execute Saga
        TransactionReceipt receipt = saga.execute(request, txn);

        // Store dedup key only after success
        idempotencyStore.put(request.getIdempotencyKey(), txn.getTransactionId());

        log.info("Payment.initiate success | reqId={} txnId={}", reqId, receipt.getTransactionId());
        return receipt;
    }

    private void validatePaymentRequest(PaymentRequest req) {
        if (req.getAmountINR() <= 0)
            throw new AppException("INVALID_AMOUNT", "Amount must be positive");
        if (req.getAmountINR() > 100_000)
            throw new AppException("LIMIT_EXCEEDED", "UPI max Rs 1 lakh per transaction");
        if (req.getSenderVpa() == null || req.getReceiverVpa() == null)
            throw new AppException("INVALID_VPA", "Sender and receiver VPA required");
        if (req.getSenderVpa().equals(req.getReceiverVpa()))
            throw new AppException("SELF_PAYMENT", "Cannot pay yourself via P2P");
    }
}
```

### Application.java — Demo

```java
public class Application {
    public static void main(String[] args) {
        // Wire dependencies
        UserRepository userRepo = new InMemoryUserRepository();
        TransactionRepository txnRepo = new InMemoryTransactionRepository();
        VpaService vpaService = new InMemoryVpaService();
        RateLimiter rateLimiter = new UpiRateLimiter(userRepo);
        BankAdapter kotakAdapter = new MockKotakBankAdapter();
        NpciSwitchAdapter npciAdapter = new MockNpciAdapter();
        UpiPaymentSaga saga = new UpiPaymentSaga(kotakAdapter, npciAdapter, txnRepo);
        PaymentService paymentService = new PaymentService(vpaService, rateLimiter, saga, txnRepo);

        // Demo: initiate payment
        PaymentRequest req = new PaymentRequest(
            "IDEM-001", "abhishek@kotak", "rahul@hdfc", 500.0, TransactionType.P2P, "USER-001");
        TransactionReceipt receipt = paymentService.initiatePayment(req);
        System.out.println("Payment: " + receipt);

        // Demo: duplicate request returns same receipt
        TransactionReceipt dup = paymentService.initiatePayment(req);
        System.out.println("Duplicate handled: " + dup.getTransactionId().equals(receipt.getTransactionId()));
    }
}
```

---

## STEP 7 — CONCURRENCY

Say: *"Since multiple requests can hit this simultaneously, let me ensure thread safety."*

### Scenario 1 — Concurrent Same Idempotency Key (Mobile Retry Storm)

```java
/*
 * Tool: ConcurrentHashMap.putIfAbsent — atomic
 * Why: Two threads from the same retry hit the service simultaneously.
 *      putIfAbsent is atomic — only one thread "wins" insertion; other returns cached result.
 */
String marker = idempotencyStore.putIfAbsent(request.getIdempotencyKey(), "PROCESSING");
if (marker != null) {
    return waitForOrFetchResult(request.getIdempotencyKey());
}
```

### Scenario 2 — VPA Cache (Read-Heavy, Race on First Resolution)

```java
/*
 * Tool: ConcurrentHashMap.computeIfAbsent — atomic get-or-create
 * Why: Same popular VPA (e.g. merchant@kotak) resolved millions of times.
 *      computeIfAbsent is atomic — no duplicate DB lookups on cache miss.
 */
private final ConcurrentHashMap<String, VPA> vpaCache = new ConcurrentHashMap<>();

public Optional<VPA> resolve(String address) {
    VPA cached = vpaCache.computeIfAbsent(address,
        key -> vpaRepository.findByAddress(key).orElse(null));
    return Optional.ofNullable(cached);
}
```

### Scenario 3 — Rate Limit Counter (Lock-Free)

```java
/*
 * Tool: AtomicInteger.incrementAndGet()
 * Why: Multiple threads increment the daily counter simultaneously.
 *      AtomicInteger uses CAS — lock-free, more performant under high contention
 *      than synchronized on the counter.
 */
AtomicInteger count = counters.computeIfAbsent(key, k -> new AtomicInteger(0));
int current = count.incrementAndGet();  // atomic CAS — no synchronized needed
return current <= 20;
```

### Scenario 4 — Transaction Status Update (ReentrantLock for reversal)

```java
/*
 * Tool: ReentrantLock with tryLock
 * Why: Reversal and status-check can race. ReentrantLock gives us tryLock()
 *      so the status-check thread does not block indefinitely.
 */
private final ReentrantLock reversalLock = new ReentrantLock();

public void reverseTransaction(String txnId) {
    if (!reversalLock.tryLock(1, TimeUnit.SECONDS)) {
        throw new AppException("LOCK_TIMEOUT", "Reversal already in progress for: " + txnId);
    }
    try {
        // safe to reverse
    } finally {
        reversalLock.unlock();  // always in finally
    }
}
```

> "I use `ConcurrentHashMap` for idempotency store — segment-level locking means no full-map block on concurrent payment writes."

> "I use `AtomicInteger` with CAS for the rate limit counter — lock-free and more performant under high contention than a `synchronized` block."

> "I use `computeIfAbsent` for VPA cache — atomic get-or-create so two threads resolving the same new VPA don't trigger duplicate DB queries."

> "I use `ReentrantLock` with `tryLock` for reversal — it gives me a timeout so a blocked caller fails fast instead of hanging."

---

## STEP 8 — DESIGN PATTERNS CALLED OUT BY NAME

| Pattern | Applied Where | Senior Phrase |
|---------|---------------|---------------|
| **Saga** | `UpiPaymentSaga` | "Distributed debit + credit across independent systems — compensation on failure instead of 2PC." |
| **Adapter** | `KotakBankAdapter`, `IciciBankAdapter` | "Different bank APIs, single `BankAdapter` contract — PaymentService never knows which bank it's calling." |
| **Strategy** | `RateLimiter` implementations | "I use Strategy so I can swap Sliding Window to Token Bucket without touching PaymentService." |
| **Builder** | `NpciTransactionRequest.of(...)` | "Builder prevents telescoping constructor — P2P and P2M have different optional fields." |
| **Observer** | `TransactionEventPublisher` | "Observer decouples PaymentService from Notification and Fraud — add fraud listener without modifying core logic." |
| **Factory** | `BankAdapterFactory.getFor(ifsc)` | "Route to right bank adapter based on IFSC — callers never call `new KotakBankAdapter()`." |
| **Decorator** | `AuditingPaymentService` wraps `PaymentService` | "Add audit trail without modifying PaymentService." |

---

## STEP 9 — DESIGN PRINCIPLES

Say: *"Let me call out the SOLID principles I'm applying."*

| Principle | Applied in UPI Design |
|-----------|----------------------|
| **SRP** | `PaymentService` handles validation + dedup. `UpiPaymentSaga` handles bank I/O. `UpiRateLimiter` handles limits. Each has one reason to change. |
| **OCP** | Adding Token Bucket rate limiting = new `TokenBucketRateLimiter` class. No changes to `PaymentService`. |
| **LSP** | `KotakBankAdapter` and `IciciBankAdapter` both substitute for `BankAdapter` without breaking `UpiPaymentSaga`. |
| **ISP** | `BankAdapter` has only payment methods. `VpaService` has only VPA methods. `PaymentService` imports only what it uses — no fat interface. |
| **DIP** | `PaymentService` depends on `RateLimiter`, `VpaService`, `UpiPaymentSaga` — all interfaces or clearly injectable. Concrete classes injected via constructor. |
| **Composition over Inheritance** | `PaymentService` composes with `RateLimiter` and `VpaService` rather than extending base classes. |
| **Encapsulation** | `Transaction.status` only changes via `markSuccess()`, `markFailed()`, `markReversed()` — never via a raw setter. |
| **Fail Fast** | `validatePaymentRequest()` runs first — invalid amounts never reach the bank adapter. |

---

## STEP 10 — EDGE CASE CHECKLIST

Say: *"Let me quickly validate the failure scenarios."*

```
□ Amount <= 0                          → validatePaymentRequest() throws INVALID_AMOUNT
□ Amount > Rs 1 lakh                   → throws LIMIT_EXCEEDED (NPCI rule)
□ Sender VPA = Receiver VPA           → throws SELF_PAYMENT
□ Null sender or receiver VPA         → throws INVALID_VPA
□ VPA not registered                  → vpaService.resolve() returns empty → VpaNotFoundException
□ MPIN wrong                          → BankAdapter throws MpinMismatchException → FAILED
□ MPIN wrong 3 times                  → Bank locks account — BankAdapter throws account-locked error
□ Duplicate idempotencyKey (retry)    → ConcurrentHashMap.get() returns existing txnId → same receipt
□ Concurrent same key (two threads)   → putIfAbsent — only one thread processes; other waits/fetches
□ Rate limit exceeded (new user)      → UpiRateLimiter throws RateLimitExceededException → 429
□ Credit fails after debit success    → Saga compensates: reverse debit → REVERSED
□ Reversal also fails                 → Log CRITICAL, push to dead-letter queue, ops alert
□ Collect request expired             → CollectRequest.isExpired() → CollectExpiredException
□ NPCI timeout                        → Transaction goes TIMED_OUT; client polls /status
□ User not found                      → UpiRateLimiter.allow() throws; caught as 404
```

---

## STEP 11 — LOGGING

Say: *"I'll add structured logging — no sensitive data."*

```java
public TransactionReceipt initiatePayment(PaymentRequest request) {
    String reqId = UUID.randomUUID().toString();
    log.info("Payment.initiate | reqId={} senderVpa={} receiverVpa={} amt={}",
             reqId, request.getSenderVpa(), request.getReceiverVpa(), request.getAmountINR());
    try {
        TransactionReceipt receipt = doInitiatePayment(request);
        log.info("Payment.initiate success | reqId={} txnId={} rrn={}",
                 reqId, receipt.getTransactionId(), receipt.getRrn());
        return receipt;
    } catch (RateLimitExceededException e) {
        log.warn("Payment.initiate rate-limited | reqId={} userId={}", reqId, request.getSenderUserId());
        throw e;
    } catch (PaymentReversedException e) {
        log.warn("Payment.initiate reversed | reqId={} txnId={}", reqId, request.getIdempotencyKey());
        throw e;
    } catch (AppException e) {
        log.error("Payment.initiate failed | reqId={} errorCode={} reason={}",
                  reqId, e.getErrorCode(), e.getMessage());
        throw e;
    }
    // NEVER log: MPIN (even hashed), full account number, raw OTP, device fingerprint
}
```

---

## STEP 12 — SCALING DISCUSSION

Say: *"If this needs to scale beyond a single JVM..."*

```
UPI App
    │
    ▼
[API Gateway]           ← JWT auth, TLS, MPIN never re-transmitted after registration
    │
    ▼
[Load Balancer]         ← Stateless PaymentService instances
    │
    ▼
[PaymentService]
    │
    ├──> [Redis]            ← VPA cache (TTL 1h), idempotency keys (TTL 24h),
    │                           rate limit counters (TTL 1 day, reset at midnight)
    │
    ├──> [PostgreSQL]       ← Transactions (UNIQUE on idempotencyKey), VPAs, LinkedAccounts
    │                           Read replicas for /history queries
    │
    ├──> [NPCI Switch]      ← External — circuit breaker to prevent cascading failures
    │
    └──> [Kafka]            ← TXN_SUCCESS, TXN_FAILED, TXN_REVERSED events
                │                → Notification, Fraud detection, Settlement reconciliation
                ▼
           [Workers]        ← Independent scaling per consumer group

Scale numbers: NPCI peak ~6,000 TPS.
Our PSP handles ~500 TPS per instance; scale horizontally.
VPA cache hit rate >95% — Redis serves most lookups; DB only on miss.
Partition Kafka by senderVpa hash for per-user ordered event processing.
```

---
---

# KYC SERVICE

---

## STEP 1 — CLARIFY REQUIREMENTS

Say: *"Before jumping into implementation, let me clarify both functional and non-functional requirements."*

---

### Part A — Functional Requirements

Say: *"First, let me understand the core use cases."*

**Actors:** Customer, KYC Officer, System (Scheduler), Admin

```
Functional Requirements:
- Customer can submit KYC application with documents (Aadhaar, PAN, Passport, etc.)
- System performs OCR on uploaded documents and extracts data
- System validates documents against government APIs (UIDAI, ITD, Passport Seva)
- KYC Officer can manually review low-confidence or flagged documents
- System approves or rejects the KYC application
- System expires KYC after 2 years (RBI mandate) and triggers re-KYC workflow
- Downstream services (GoldLoan, UPI) can query KYC status via KycCheckService
- Admin can trigger bulk re-KYC campaigns for an expiry cohort
- Customer can update address (triggers partial re-KYC only)
- System records full audit trail: who approved, what document, when
```

---

### Part B — Non-Functional Requirements

Say: *"Now let me clarify the quality constraints."*

| Area | Decision for KYC |
|------|-----------------|
| Scale | Moderate write (submissions); heavy read (status queries from GoldLoan, UPI) |
| Concurrency | Concurrent document uploads for same application; concurrent finalization (OCR + manual) |
| Consistency | Strong — GoldLoan cannot disburse to unverified customer |
| Latency | Async OCR — do not block HTTP response waiting for OCR |
| Availability | High — query status must always succeed |
| Durability | PostgreSQL for KYC records; S3 for documents |
| Idempotency | Same document uploaded twice = same document record (file hash dedup) |
| Extensibility | New document types (Voter ID, Driving License) → Strategy + Factory |
| Failure Handling | External API down → circuit breaker → manual review queue |
| Scope | Distributed; event-driven (Kafka for async OCR + notification) |

```
"Based on my understanding:

Functional:
  1. Submit docs → OCR → validate → approve/reject
  2. KYC expires in 2 years — re-KYC workflow
  3. Downstream services query status (GoldLoan, UPI)

Non-Functional:
  1. Async OCR — event-driven (Observer pattern / Kafka)
  2. External API resilience — Circuit Breaker
  3. Extensible doc types — Strategy + Factory
  4. PII never logged — Aadhaar, PAN encrypted at rest
  5. File hash dedup — ConcurrentHashMap.putIfAbsent

Let me proceed unless you'd like to change anything."
```

---

## STEP 2 — CLASS DIAGRAM

Say: *"Let me model the core entities and their relationships."*

```
<<enum>>
+---------------------+
|      KycStatus      |
+---------------------+
| PENDING             |
| DOCS_SUBMITTED      |
| UNDER_REVIEW        |
| VERIFIED            |
| REJECTED            |
| EXPIRED             |
| RE_KYC_REQUIRED     |
+---------------------+

<<enum>>
+---------------------+
|    DocumentType     |
+---------------------+
| AADHAAR             |
| PAN                 |
| PASSPORT            |
| DRIVING_LICENSE     |
| VOTER_ID            |
+---------------------+

<<enum>>
+---------------------+
| VerificationStatus  |
+---------------------+
| PENDING             |
| OCR_COMPLETE        |
| API_VERIFIED        |
| MANUAL_REVIEW       |
| APPROVED            |
| REJECTED            |
+---------------------+

<<interface>>
+----------------------------------------------+
|            DocumentValidator                 |
+----------------------------------------------+
| + validate(doc:KycDocument):                 |
|     ValidationResult                         |
| + supports(type:DocumentType): boolean       |
+----------------------------------------------+
     ^              ^               ^
     |              |               |
AadhaarValidator  PanValidator  PassportValidator

<<interface>>
+----------------------------------------------+
|               OcrService                     |
+----------------------------------------------+
| + extractData(fileUrl:String):               |
|     Map<String,String>                       |
+----------------------------------------------+

<<interface>>
+----------------------------------------------+
|            KycCheckService                   |
+----------------------------------------------+
| + getStatus(customerId:String):              |
|     KycStatus                                |
| + isVerifiedForGoldLoan(customerId:String):  |
|     boolean                                  |
| + needsReKyc(customerId:String): boolean     |
| + getMissingDocuments(customerId:String):    |
|     List<DocumentType>                       |
+----------------------------------------------+

<<abstract>>
+----------------------------------------------+
|          BaseDocumentValidator               |
+----------------------------------------------+
| # circuitBreaker: CircuitBreaker             |
+----------------------------------------------+
| + validate(doc:KycDocument):                 |
|     ValidationResult                         |
| # callExternalApi(doc):                      |
|     ValidationResult   (abstract)            |
| # fallbackToManualReview(): ValidationResult |
+----------------------------------------------+
     ^              ^
     |              |
AadhaarValidator  PanValidator

<<factory>>
+----------------------------------------------+
|        DocumentValidatorFactory              |
+----------------------------------------------+
| + getFor(type:DocumentType):                 |
|     DocumentValidator                        |
+----------------------------------------------+

+----------------------------------------------+
|             KycApplication                   |
+----------------------------------------------+
| - applicationId: String                      |
| - customerId: String                         |
| - kycType: KycType                           |
| - documents: List<KycDocument>               |
| - status: KycStatus                          |
| - verifiedAt: Instant                        |
| - expiresAt: Instant                         |
| - rejectionReason: String                    |
| - reviewedBy: String                         |
+----------------------------------------------+
| + isExpired(): boolean                       |
| + isComplete(): boolean                      |
| + hasDocument(type:DocumentType): boolean    |
| + findDocument(docId:String): KycDocument    |
| + equals(o:Object): boolean                  |
| + hashCode(): int                            |
| + toString(): String                         |
+----------------------------------------------+

+----------------------------------------------+
|              KycDocument                     |
+----------------------------------------------+
| - documentId: String                         |
| - documentType: DocumentType                 |
| - fileUrl: String                            |
| - fileHash: String                           |   ← SHA-256; for dedup
| - ocrExtractedData: Map<String,String>       |
| - verificationStatus: VerificationStatus     |
+----------------------------------------------+

+----------------------------------------------+
|             KycAuditEvent                    |
+----------------------------------------------+
| - eventId: String                            |
| - applicationId: String                      |
| - fromStatus: KycStatus                      |
| - toStatus: KycStatus                        |
| - changedBy: String                          |
| - changedAt: Instant                         |
| - reason: String                             |
+----------------------------------------------+

Relationships:
KycApplication <◆>————————> KycDocument       Composition (docs exist only within application)
KycApplication ————————> KycAuditEvent        Association (events logged per transition)
KycService - - -> DocumentValidatorFactory    Dependency  (creates validators)
KycService - - -> OcrService                  Dependency  (async OCR call)
KycCheckServiceImpl - - -> KycRepository      Dependency  (read-only queries)
BaseDocumentValidator ————————|> (abstract)
AadhaarValidator - - - -|> BaseDocumentValidator   Implementation
PanValidator - - - - - -|> BaseDocumentValidator   Implementation
```

---

## STEP 3 — SEQUENCE DIAGRAM

Say: *"Let me trace the document submission and async verification flow."*

```
Customer  KycController  KycService  KafkaPublisher  OcrConsumer  DocValidator  KycRepository
   |           |              |             |               |             |             |
   |—submit()—>|              |             |               |             |             |
   |           |—submitDocs()>|             |               |             |             |
   |           |              |—save(DOCS_SUBMITTED)—————————————————————————————————>|
   |           |              |—publish(KYC_DOCS_SUBMITTED)>|             |             |
   |           |<—applicationId|             |               |             |             |
   |<—202 Accepted            |             |               |             |             |
   |           |              |             |               |             |             |
   |   [async — OcrConsumer picks up event] |               |             |             |
   |           |              |             |               |—extractData()|            |
   |           |              |—onOcrComplete(appId, ocrData)             |             |
   |           |              |—updateDoc(OCR_COMPLETE)——————————————————————————————>|
   |           |              |—validate(doc)—————————————————————————>|             |
   |           |              |<——ValidationResult—————————————————————|             |
   |           |              |—updateDoc(API_VERIFIED)—————————————————————————————>|
   |           |              |—tryFinalizeApp()         |              |             |
   |           |              |—updateApp(VERIFIED)—————————————————————————————————>|
   |           |              |—publish(KYC_VERIFIED)———>|              |             |
   |<—Push notification "KYC Approved"                   |             |             |
```

```
[alt: OCR confidence low]
  OcrConsumer ——> KycService: LOW_CONFIDENCE result
  KycService ——> KycRepository: updateApp(UNDER_REVIEW)
  KycService ——> KycRepository: assignToOfficer(appId)
[end]

[alt: Government API (ITD/UIDAI) down]
  DocumentValidator ——> CircuitBreaker: OPEN
  DocumentValidator: return ValidationResult.manualReview("API unavailable")
  KycService ——> KycRepository: updateDoc(MANUAL_REVIEW)
[end]

[alt: KYC expires (nightly job)]
  KycExpiryJob ——> KycRepository: findExpiredApplications()
  KycExpiryJob ——> KycService: markReKycRequired(customerId)
  NotificationService ——> Customer: "Your KYC expired. Please re-submit."
[end]

[alt: Concurrent finalization (OCR thread + manual review thread)]
  Thread A: synchronized(this) → re-fetch → all verified → marks VERIFIED → exits
  Thread B: synchronized(this) → re-fetch → status already VERIFIED → returns early
[end]
```

---

## STEP 4 — DEFINE RELATIONSHIPS

Say: *"Let me define the relationships before writing code."*

```
KycApplication — KycDocument           Composition — docs are destroyed with application
KycApplication — KycAuditEvent         Association — events reference applicationId
KycService — DocumentValidatorFactory  Dependency  — creates validator per document type
KycService — OcrService                Dependency  — async; used per document
KycCheckServiceImpl — KycRepository    Dependency  — read-only queries for downstream services
BaseDocumentValidator — AadhaarValidator   Inheritance — IS-A validator
```

> "I use an **interface** for `DocumentValidator` because Aadhaar (UIDAI), PAN (ITD), and Passport (Passport Seva) use completely different API shapes and auth tokens — plug-and-play per type."

> "I use an **abstract class** for `BaseDocumentValidator` because all validators share the circuit breaker logic and the `fallbackToManualReview()` method. Only `callExternalApi()` differs per document type."

> "I use a **Factory** for `DocumentValidatorFactory` — `KycService` never calls `new AadhaarValidator()`. The factory centralises creation, so adding Voter ID support is a one-line factory change."

> "I prefer **composition over inheritance** for `KycService` — it holds `DocumentValidatorFactory` as a field rather than extending a base service class. Avoids the rigid hierarchy."

---

## STEP 5 — FOLDER STRUCTURE

Say: *"Let me lay out the package structure before writing any code."*

```
kyc-service/
└── src/main/java/com/kotak/kyc/
    ├── model/
    │   ├── KycApplication.java
    │   ├── KycDocument.java
    │   └── KycAuditEvent.java
    ├── enums/
    │   ├── KycStatus.java
    │   ├── DocumentType.java
    │   ├── VerificationStatus.java
    │   └── KycType.java
    ├── interfaces/
    │   ├── DocumentValidator.java
    │   ├── OcrService.java
    │   └── KycCheckService.java
    ├── strategy/
    │   ├── AadhaarValidator.java
    │   ├── PanValidator.java
    │   └── PassportValidator.java
    ├── factory/
    │   └── DocumentValidatorFactory.java
    ├── observer/
    │   ├── KycEventPublisher.java
    │   ├── OcrEventConsumer.java
    │   └── KycNotificationConsumer.java
    ├── service/
    │   ├── KycService.java
    │   ├── KycCheckServiceImpl.java
    │   └── KycExpiryJob.java
    ├── repository/
    │   └── KycRepository.java
    ├── exception/
    │   ├── AppException.java
    │   ├── KycNotVerifiedException.java
    │   ├── KycExpiredException.java
    │   └── DocumentAlreadySubmittedException.java
    ├── controller/
    │   └── KycController.java
    └── Application.java
```

---

## STEP 6 — CORE CODE

Say: *"Let me write the core code."*

### Custom Exception Hierarchy

```java
public class AppException extends RuntimeException {
    private final String errorCode;
    public AppException(String code, String msg) { super(msg); this.errorCode = code; }
    public String getErrorCode() { return errorCode; }
}
public class KycNotVerifiedException extends AppException {
    public KycNotVerifiedException(String customerId) {
        super("KYC_NOT_VERIFIED", "KYC not verified for: " + customerId);
    }
}
public class KycExpiredException extends AppException {
    public KycExpiredException(String customerId) {
        super("KYC_EXPIRED", "KYC expired for: " + customerId + ". Re-KYC required.");
    }
}
public class DocumentAlreadySubmittedException extends AppException {
    public DocumentAlreadySubmittedException(String fileHash) {
        super("DOC_DUPLICATE", "Document already submitted with hash: " + fileHash);
    }
}
```

### Core Model — KycApplication

```java
/*
 * Model: KycApplication
 * Immutable: applicationId, customerId, kycType — set at creation.
 * Mutable: status, verifiedAt, expiresAt — changed via service methods only.
 * Principle: Encapsulation — status never set via raw setter.
 */
public class KycApplication {

    private final String applicationId;
    private final String customerId;
    private final KycType kycType;
    private final List<KycDocument> documents;
    private KycStatus status;
    private Instant verifiedAt;
    private Instant expiresAt;
    private String rejectionReason;
    private String reviewedBy;

    public KycApplication(String applicationId, String customerId,
                          KycType kycType, List<KycDocument> documents) {
        this.applicationId = applicationId;
        this.customerId = customerId;
        this.kycType = kycType;
        this.documents = new ArrayList<>(documents);
        this.status = KycStatus.PENDING;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isComplete() {
        return status == KycStatus.VERIFIED && !isExpired();
    }

    public boolean hasDocument(DocumentType type) {
        return documents.stream().anyMatch(d -> d.getDocumentType() == type);
    }

    public KycDocument findDocument(String documentId) {
        return documents.stream()
            .filter(d -> d.getDocumentId().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new AppException("DOC_NOT_FOUND", "Document: " + documentId));
    }

    // Getters
    public String getApplicationId() { return applicationId; }
    public String getCustomerId()    { return customerId; }
    public KycStatus getStatus()     { return status; }
    public List<KycDocument> getDocuments() { return Collections.unmodifiableList(documents); }
    public Instant getExpiresAt()    { return expiresAt; }

    // Controlled setters
    public void setStatus(KycStatus s)       { this.status = s; }
    public void setVerifiedAt(Instant t)     { this.verifiedAt = t; }
    public void setExpiresAt(Instant t)      { this.expiresAt = t; }
    public void setRejectionReason(String r) { this.rejectionReason = r; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KycApplication)) return false;
        return applicationId.equals(((KycApplication) o).applicationId);
    }
    @Override public int hashCode()    { return applicationId.hashCode(); }
    @Override public String toString() {
        return "KycApplication{id='" + applicationId + "', status=" + status + "}";
    }
}
```

### Abstract Class — BaseDocumentValidator

```java
/*
 * Abstract class: all validators share circuit breaker logic and fallback.
 * Only callExternalApi() differs per document type.
 * Pattern: Template Method — base defines the validation skeleton.
 * Principle: DRY — circuit breaker + fallback in one place, not duplicated per validator.
 */
public abstract class BaseDocumentValidator implements DocumentValidator {

    protected final CircuitBreaker circuitBreaker;

    protected BaseDocumentValidator(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    // Template Method — defines the skeleton; subclasses fill in callExternalApi()
    public final ValidationResult validate(KycDocument document) {
        String extracted = document.getOcrExtractedData().get(getKeyField());
        if (extracted == null) {
            return ValidationResult.failed(getKeyField() + " not extracted by OCR");
        }
        try {
            return circuitBreaker.execute(() -> callExternalApi(document, extracted));
        } catch (CircuitBreakerOpenException e) {
            return fallbackToManualReview();
        }
    }

    protected abstract String getKeyField();
    protected abstract ValidationResult callExternalApi(KycDocument doc, String extracted);

    protected ValidationResult fallbackToManualReview() {
        return ValidationResult.manualReview("External API unavailable — queued for officer review");
    }
}

public class PanValidator extends BaseDocumentValidator {
    private final ItdApiClient itdClient;

    public PanValidator(CircuitBreaker cb, ItdApiClient client) {
        super(cb);
        this.itdClient = client;
    }
    protected String getKeyField() { return "panNumber"; }
    protected ValidationResult callExternalApi(KycDocument doc, String panNumber) {
        ItdResponse resp = itdClient.verify(panNumber, doc.getOcrExtractedData().get("name"));
        return resp.isValid()
            ? ValidationResult.success(panNumber)
            : ValidationResult.failed("PAN not found in ITD records");
    }
    public boolean supports(DocumentType type) { return type == DocumentType.PAN; }
}

public class AadhaarValidator extends BaseDocumentValidator {
    private final UidaiApiClient uidaiClient;

    public AadhaarValidator(CircuitBreaker cb, UidaiApiClient client) {
        super(cb);
        this.uidaiClient = client;
    }
    protected String getKeyField() { return "aadhaarNumber"; }
    protected ValidationResult callExternalApi(KycDocument doc, String aadhaarNumber) {
        // Note: only last 4 digits used in API — full Aadhaar never stored
        UidaiResponse resp = uidaiClient.verify(aadhaarNumber.substring(8));
        return resp.isValid() ? ValidationResult.success("AADHAAR_OK") : ValidationResult.failed("Aadhaar mismatch");
    }
    public boolean supports(DocumentType type) { return type == DocumentType.AADHAAR; }
}
```

### Factory — DocumentValidatorFactory

```java
/*
 * Pattern: Factory
 * Why: KycService never calls new AadhaarValidator() — centralise creation.
 *      Adding Voter ID = register a new VoterIdValidator here; zero other changes.
 * Principle: OCP — factory is the only class that changes when a new doc type is added.
 */
public class DocumentValidatorFactory {

    private final List<DocumentValidator> validators;

    // Constructor injection — validators registered at startup (Spring beans in production)
    public DocumentValidatorFactory(List<DocumentValidator> validators) {
        this.validators = validators;
    }

    public DocumentValidator getFor(DocumentType type) {
        return validators.stream()
            .filter(v -> v.supports(type))
            .findFirst()
            .orElseThrow(() -> new AppException("NO_VALIDATOR", "No validator for: " + type));
    }
}
```

### KycService — Core Logic

```java
/*
 * What it does: Accepts KYC submissions; triggers async OCR + validation pipeline.
 * Why it exists: Single coordination point for the write side of KYC lifecycle.
 * Pattern: Observer (event-driven OCR); Factory (validator selection); Template Method (validation).
 * Principle: SRP — orchestration only; OCR in OcrConsumer; validation in DocumentValidator.
 * Principle: DIP — depends on OcrService, DocumentValidatorFactory interfaces.
 * Principle: Fail Fast — validate() at entry before saving anything.
 */
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);

    private final KycRepository kycRepository;
    private final DocumentValidatorFactory validatorFactory;
    private final KycEventPublisher eventPublisher;

    private static final long KYC_VALIDITY_YEARS = 2;

    // File hash dedup — prevents same document saved twice (double-upload)
    private final Set<String> submittedHashes = ConcurrentHashMap.newKeySet();

    public KycService(KycRepository kycRepository,
                      DocumentValidatorFactory validatorFactory,
                      KycEventPublisher eventPublisher) {
        this.kycRepository = kycRepository;
        this.validatorFactory = validatorFactory;
        this.eventPublisher = eventPublisher;
    }

    public KycApplication submitDocuments(KycSubmissionRequest request) {
        String reqId = UUID.randomUUID().toString();
        log.info("KYC.submit | reqId={} customerId={} docTypes={}",
                 reqId, request.getCustomerId(), docTypeNames(request.getDocuments()));

        // Fail Fast: validate at entry
        validateSubmission(request);

        // Idempotency: return existing if already VERIFIED
        Optional<KycApplication> existing = kycRepository.findLatestByCustomerId(request.getCustomerId());
        if (existing.isPresent() && existing.get().isComplete()) {
            log.info("KYC.submit — already verified | customerId={}", request.getCustomerId());
            return existing.get();
        }

        // File hash dedup
        for (KycDocument doc : request.getDocuments()) {
            if (!submittedHashes.add(doc.getFileHash())) {
                throw new DocumentAlreadySubmittedException(doc.getFileHash());
            }
        }

        KycApplication application = new KycApplication(
            UUID.randomUUID().toString(),
            request.getCustomerId(),
            request.getKycType(),
            request.getDocuments()
        );
        application.setStatus(KycStatus.DOCS_SUBMITTED);
        kycRepository.save(application);

        // Publish event — OCR and notification happen asynchronously (Observer)
        eventPublisher.publish(new KycDocsSubmittedEvent(application.getApplicationId()));

        log.info("KYC.submit queued | reqId={} applicationId={}", reqId, application.getApplicationId());
        return application;
    }

    // Called by OcrEventConsumer after OCR completes for a document
    public void onOcrComplete(String applicationId, String documentId, Map<String, String> ocrData) {
        KycApplication app = kycRepository.findById(applicationId).orElseThrow(
            () -> new AppException("NOT_FOUND", "Application: " + applicationId));

        KycDocument doc = app.findDocument(documentId);
        doc.setOcrExtractedData(ocrData);
        doc.setVerificationStatus(VerificationStatus.OCR_COMPLETE);
        kycRepository.save(app);

        // Validate via government API (Strategy + Factory)
        DocumentValidator validator = validatorFactory.getFor(doc.getDocumentType());
        ValidationResult result = validator.validate(doc);

        if (result.isApproved()) {
            doc.setVerificationStatus(VerificationStatus.API_VERIFIED);
        } else if (result.needsManualReview()) {
            doc.setVerificationStatus(VerificationStatus.MANUAL_REVIEW);
            app.setStatus(KycStatus.UNDER_REVIEW);
        } else {
            doc.setVerificationStatus(VerificationStatus.REJECTED);
        }

        kycRepository.save(app);
        tryFinalizeApplication(app);
    }

    /*
     * Synchronized + re-fetch to prevent double-finalization.
     * OCR thread and manual-review thread may both call this for the same app.
     */
    private synchronized void tryFinalizeApplication(KycApplication stale) {
        // Re-fetch from DB — both threads may have stale in-memory copies
        KycApplication app = kycRepository.findById(stale.getApplicationId()).orElseThrow();
        if (app.getStatus() == KycStatus.VERIFIED) return;  // already done by other thread

        boolean allVerified = app.getDocuments().stream()
            .allMatch(d -> d.getVerificationStatus() == VerificationStatus.API_VERIFIED
                        || d.getVerificationStatus() == VerificationStatus.APPROVED);

        if (allVerified) {
            app.setStatus(KycStatus.VERIFIED);
            app.setVerifiedAt(Instant.now());
            app.setExpiresAt(Instant.now().plus(KYC_VALIDITY_YEARS * 365, ChronoUnit.DAYS));
            kycRepository.save(app);
            eventPublisher.publish(new KycVerifiedEvent(app.getApplicationId(), app.getCustomerId()));
            log.info("KYC verified | customerId={} appId={}", app.getCustomerId(), app.getApplicationId());
        }
    }

    private void validateSubmission(KycSubmissionRequest req) {
        if (req.getCustomerId() == null || req.getCustomerId().isBlank())
            throw new AppException("INVALID_INPUT", "customerId required");
        if (req.getDocuments() == null || req.getDocuments().isEmpty())
            throw new AppException("INVALID_INPUT", "At least one document required");
    }

    private String docTypeNames(List<KycDocument> docs) {
        return docs.stream().map(d -> d.getDocumentType().name()).collect(Collectors.joining(","));
    }
}
```

### KycCheckService — Read Interface for Downstream

```java
/*
 * What it does: Read-only KYC status queries for GoldLoan, UPI, and other services.
 * Why separate from KycService: KycService is write-heavy (submissions, finalization).
 *                               KycCheckService is read-heavy — different scaling needs.
 * Principle: ISP — GoldLoanService imports only KycCheckService (4 methods),
 *                   not the full KycService (many methods, write operations).
 */
public class KycCheckServiceImpl implements KycCheckService {

    private final KycRepository kycRepository;

    public KycCheckServiceImpl(KycRepository kycRepository) {
        this.kycRepository = kycRepository;
    }

    public KycStatus getStatus(String customerId) {
        return kycRepository.findLatestByCustomerId(customerId)
            .map(KycApplication::getStatus)
            .orElse(KycStatus.PENDING);
    }

    public boolean isVerifiedForGoldLoan(String customerId) {
        return kycRepository.findLatestByCustomerId(customerId)
            .map(app -> app.getStatus() == KycStatus.VERIFIED
                     && !app.isExpired()
                     && app.hasDocument(DocumentType.PAN))   // PAN mandatory for Gold Loan
            .orElse(false);
    }

    public boolean needsReKyc(String customerId) {
        return kycRepository.findLatestByCustomerId(customerId)
            .map(app -> app.isExpired() || app.getStatus() == KycStatus.RE_KYC_REQUIRED)
            .orElse(false);
    }

    public List<DocumentType> getMissingDocuments(String customerId) {
        List<DocumentType> required = List.of(DocumentType.AADHAAR, DocumentType.PAN);
        List<DocumentType> submitted = kycRepository.findLatestByCustomerId(customerId)
            .map(app -> app.getDocuments().stream()
                .map(KycDocument::getDocumentType)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
        return required.stream().filter(d -> !submitted.contains(d)).collect(Collectors.toList());
    }
}
```

### Application.java — Demo

```java
public class Application {
    public static void main(String[] args) {
        KycRepository kycRepo = new InMemoryKycRepository();
        CircuitBreaker cb = new NoopCircuitBreaker();
        List<DocumentValidator> validators = List.of(
            new AadhaarValidator(cb, new MockUidaiClient()),
            new PanValidator(cb, new MockItdClient())
        );
        DocumentValidatorFactory factory = new DocumentValidatorFactory(validators);
        KycEventPublisher publisher = new ConsoleKycEventPublisher();
        KycService kycService = new KycService(kycRepo, factory, publisher);
        KycCheckService checkService = new KycCheckServiceImpl(kycRepo);

        // Demo: submit docs
        KycDocument pan = new KycDocument("DOC-001", DocumentType.PAN, "s3://pan-123.jpg", "hash-abc");
        KycSubmissionRequest req = new KycSubmissionRequest("CUST-001", KycType.FULL, List.of(pan));
        KycApplication app = kycService.submitDocuments(req);
        System.out.println("Submitted: " + app);

        // Demo: duplicate doc — throws
        try {
            kycService.submitDocuments(req);
        } catch (DocumentAlreadySubmittedException e) {
            System.out.println("Caught expected: " + e.getMessage());
        }

        // Demo: query status downstream (as GoldLoan would)
        System.out.println("Eligible for gold loan: " + checkService.isVerifiedForGoldLoan("CUST-001"));
    }
}
```

---

## STEP 7 — CONCURRENCY

Say: *"Since multiple requests can hit this simultaneously, let me ensure thread safety."*

### Scenario 1 — Document Hash Dedup (Same File Uploaded Twice)

```java
/*
 * Tool: ConcurrentHashMap.newKeySet() — thread-safe Set
 * Why: User double-taps submit — same document uploaded twice concurrently.
 *      .add() on ConcurrentHashMap-backed Set is atomic — only one thread wins.
 */
private final Set<String> submittedHashes = ConcurrentHashMap.newKeySet();

if (!submittedHashes.add(doc.getFileHash())) {
    throw new DocumentAlreadySubmittedException(doc.getFileHash());
}
```

### Scenario 2 — Concurrent Finalization (OCR Thread + Manual Review Thread)

```java
/*
 * Tool: synchronized + re-fetch from DB
 * Why: OcrConsumer thread and ManualReviewThread may both call tryFinalizeApplication()
 *      for the same application with stale in-memory copies.
 *      synchronized prevents both from emitting two KYC_VERIFIED events.
 *      Re-fetch inside lock ensures we act on the latest DB state.
 */
private synchronized void tryFinalizeApplication(KycApplication stale) {
    KycApplication app = kycRepository.findById(stale.getApplicationId()).orElseThrow();
    if (app.getStatus() == KycStatus.VERIFIED) return;  // already done — return early
    // ... proceed with finalization
}
```

### Scenario 3 — Bulk Re-KYC Job (Read-Heavy)

```java
/*
 * Tool: ReadWriteLock
 * Why: KycExpiryJob reads all applications. Simultaneously, individual KYC updates happen.
 *      ReadWriteLock allows multiple readers; exclusive writer — no lock on read path.
 */
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

public List<KycApplication> findExpired() {
    rwLock.readLock().lock();
    try {
        return kycRepository.findByStatusAndExpiredBefore(KycStatus.VERIFIED, Instant.now());
    } finally {
        rwLock.readLock().unlock();
    }
}
```

### Scenario 4 — Idempotency Store for Re-KYC Trigger

```java
/*
 * Tool: AtomicReference for current KYC status snapshot
 * Why: Status is set by finalization thread and read by check-service thread.
 *      AtomicReference gives lock-free visibility across threads.
 */
private final AtomicReference<KycStatus> cachedStatus = new AtomicReference<>(KycStatus.PENDING);

public void updateCachedStatus(KycStatus status) { cachedStatus.set(status); }
public KycStatus getCachedStatus()               { return cachedStatus.get(); }
```

> "I use `ConcurrentHashMap.newKeySet()` for hash dedup — `.add()` is atomic, so concurrent double-uploads don't both slip through."

> "I `synchronized` the `tryFinalizeApplication` method and re-fetch from DB inside the lock — this is the check-then-act pattern, preventing double VERIFIED events even with stale in-memory state."

> "I use `ReadWriteLock` for the bulk re-KYC job — multiple readers for the status snapshot, exclusive write lock during finalization."

---

## STEP 8 — DESIGN PATTERNS CALLED OUT BY NAME

| Pattern | Applied Where | Senior Phrase |
|---------|---------------|---------------|
| **Strategy** | `DocumentValidator` (Aadhaar/PAN/Passport) | "I use Strategy so I can swap UIDAI/ITD/Passport Seva without touching KycService." |
| **Factory** | `DocumentValidatorFactory.getFor(type)` | "I use Factory to centralise validator creation — callers never call `new AadhaarValidator()`." |
| **Template Method** | `BaseDocumentValidator.validate()` | "Template Method defines the validation skeleton in the base class — subclasses implement only `callExternalApi()`." |
| **Observer** | `KycEventPublisher` → OcrConsumer, NotificationConsumer | "Observer decouples submission from OCR and notification — add a fraud-check consumer without touching KycService." |
| **State** | `KycApplication` status transitions | "VERIFIED → RE_KYC_REQUIRED on expiry blocks GoldLoan disbursement automatically." |
| **Builder** | `KycApplication` with optional rejectionReason, reviewedBy | "Builder prevents telescoping constructor — optional fields set only when needed." |
| **Decorator** | `AuditingKycService` wraps `KycService` | "Add RBI audit logging without modifying KycService." |

---

## STEP 9 — DESIGN PRINCIPLES

Say: *"Let me call out the SOLID principles I'm applying."*

| Principle | Applied in KYC Design |
|-----------|----------------------|
| **SRP** | `KycService` handles workflow orchestration. `KycCheckServiceImpl` handles read queries. `KycExpiryJob` handles expiry. `DocumentValidatorFactory` handles creation. One reason to change each. |
| **OCP** | Adding Voter ID validation = new `VoterIdValidator` class + register in factory. Zero changes to `KycService`. |
| **LSP** | `AadhaarValidator`, `PanValidator`, `PassportValidator` all substitute for `DocumentValidator` without breaking `KycService`. |
| **ISP** | `GoldLoanService` uses `KycCheckService` (4 methods). It does not import `KycService` (many methods, write operations). Split by usage. |
| **DIP** | `KycService` depends on `OcrService` and `DocumentValidatorFactory` — both are interfaces/injectable. Never `new TesseractOcrService()` inside service. |
| **Composition over Inheritance** | `KycService` composes with `DocumentValidatorFactory` rather than extending a base service. |
| **Encapsulation** | `KycApplication.status` only set via `setStatus()`. Documents list exposed as unmodifiable view. |
| **Fail Fast** | `validateSubmission()` runs first — invalid inputs never reach OCR, DB, or event publisher. |

---

## STEP 10 — EDGE CASE CHECKLIST

Say: *"Let me quickly validate the failure scenarios."*

```
□ Null customerId on submit               → validateSubmission() throws INVALID_INPUT
□ Empty documents list                    → validateSubmission() throws "At least one document required"
□ Same document uploaded twice           → fileHash dedup via ConcurrentHashMap.newKeySet() → throws
□ Already VERIFIED — submits again       → KycService returns existing application unchanged
□ OCR confidence low                     → MANUAL_REVIEW state; assigned to officer
□ ITD/UIDAI API down                     → CircuitBreaker OPEN → fallbackToManualReview()
□ KYC expires after 2 years             → KycExpiryJob → RE_KYC_REQUIRED → notify customer
□ Gold Loan checks expired KYC          → isVerifiedForGoldLoan() checks isExpired() → false
□ Missing PAN for Gold Loan             → getMissingDocuments() returns [PAN]
□ Concurrent finalization               → synchronized + re-fetch → only one KYC_VERIFIED event
□ Re-KYC customer already in progress   → findLatestByCustomerId → merge with existing in-progress app
□ Manual review officer rejects doc     → doc status = REJECTED → app status = REJECTED → notify customer
□ Admin bulk re-KYC on expired cohort   → KycExpiryJob processes batch; ReadWriteLock during reads
□ Customer updates address              → partial re-KYC: only address proof re-submitted
```

---

## STEP 11 — LOGGING

Say: *"I'll add structured logging — no sensitive data."*

```java
public KycApplication submitDocuments(KycSubmissionRequest request) {
    String reqId = UUID.randomUUID().toString();
    log.info("KYC.submit | reqId={} customerId={} docTypes={}",
             reqId, request.getCustomerId(), docTypeNames(request.getDocuments()));
    try {
        KycApplication app = doSubmit(request);
        log.info("KYC.submit queued | reqId={} applicationId={}", reqId, app.getApplicationId());
        return app;
    } catch (DocumentAlreadySubmittedException e) {
        log.warn("KYC.submit duplicate doc | reqId={} reason={}", reqId, e.getMessage());
        throw e;
    } catch (AppException e) {
        log.error("KYC.submit failed | reqId={} errorCode={} reason={}",
                  reqId, e.getErrorCode(), e.getMessage());
        throw e;
    }
    // NEVER log: Aadhaar number (not even masked), PAN number, date of birth,
    //            photograph S3 URL, address details
    // SAFE to log: customerId, applicationId, documentType (not document content)
}
```

---

## STEP 12 — SCALING DISCUSSION

Say: *"If this needs to scale beyond a single JVM..."*

```
Customer App
     │
     ▼
[API Gateway]          ← Auth, TLS, PII masking in access logs
     │
     ▼
[KycService]           ← Stateless, horizontally scalable (write side only)
     │
     ├──> [S3]               ← Pre-signed upload URLs — KycService never handles file bytes
     │                           Avoids memory pressure from large scan uploads
     │
     ├──> [Kafka]            ← KYC_DOCS_SUBMITTED → OcrConsumer, NotificationConsumer
     │         │
     │         ├──> [OcrWorkers]         ← Scale by OCR queue depth (CPU-heavy)
     │         └──> [ValidationWorkers]  ← Scale independently; circuit breaker on gov APIs
     │
     ├──> [PostgreSQL]       ← KYC applications, documents, audit events (write primary)
     │                           Read replicas for KycCheckService queries (GoldLoan, UPI)
     │
     └──> [Redis]            ← Doc hash dedup cache, status cache for downstream hot reads

S3 Pre-signed URL pattern:
  KycService returns presigned URL to client.
  Client uploads directly to S3.
  S3 triggers KYC_DOC_UPLOADED event to Kafka.
  OcrConsumer picks it up — KycService never streams document bytes.
```

---
---

# QUICK CROSS-TOPIC CHEAT SHEET

---

## Opening Lines (Memorise These Exactly)

| Moment | What to Say |
|--------|-------------|
| Start | *"Before jumping into implementation, let me clarify both functional and non-functional requirements."* |
| Actors | *"First, let me understand the core use cases. Who are the actors?"* |
| NFR | *"Now let me clarify the quality constraints."* |
| Confirm | *"Based on my understanding... Let me proceed unless you'd like to change anything."* |
| Class Diagram | *"Let me model the core entities and their relationships."* |
| Sequence | *"Let me trace the flow for the primary use case."* |
| Relationships | *"Let me define the relationships before writing code."* |
| Folder | *"Let me lay out the package structure before writing any code."* |
| Concurrency | *"Since multiple requests can hit this simultaneously, let me ensure thread safety."* |
| Patterns | *"I'm using [Pattern Name] here because..."* |
| SOLID | *"Let me call out the SOLID principles I'm applying."* |
| Edge Cases | *"Let me quickly validate the failure scenarios."* |
| Scale | *"If this needs to scale beyond a single JVM..."* |

---

## Pattern → Banking Use Case (30-Second Recall)

| Pattern | Gold Loan | UPI | KYC |
|---------|-----------|-----|-----|
| **State** | Loan lifecycle (ACTIVE/OVERDUE/AUCTIONED) | Transaction status | KYC application status |
| **Strategy** | `InterestCalculator` (Bullet/EMI/Vanilla) | `RateLimiter` | `DocumentValidator` |
| **Factory** | `InterestCalculatorFactory` | `BankAdapterFactory` | `DocumentValidatorFactory` |
| **Adapter** | `BankAdapter` (disbursal) | `KotakBankAdapter`, `IciciBankAdapter` | `GovApiAdapter` |
| **Saga** | — | `UpiPaymentSaga` (debit+credit) | — |
| **Observer** | `LoanEventPublisher` | `TransactionEventPublisher` | `KycEventPublisher` |
| **Builder** | `GoldValuation.builder()` | `NpciTransactionRequest.of(...)` | `KycApplication` optional fields |
| **Singleton** | `GoldPriceFeed` | — | — |
| **Decorator** | `LoggingLoanRepaymentService` | `AuditingPaymentService` | `AuditingKycService` |
| **Template Method** | `LoanApprovalStep` | — | `BaseDocumentValidator.validate()` |
| **Chain of Resp** | Loan approval pipeline | — | Doc validation pipeline |

---

## Concurrency Tool → When (30-Second Recall)

| Tool | Use When | Banking Example |
|------|----------|-----------------|
| `ConcurrentHashMap` | Thread-safe map | Idempotency store, VPA cache, rate limit map |
| `ConcurrentHashMap.newKeySet()` | Thread-safe Set | Payment ref dedup, doc hash dedup |
| `computeIfAbsent` | Atomic get-or-create | VPA cache miss, rate limit counter init |
| `putIfAbsent` | Atomic create-if-absent | Idempotency key insertion |
| `AtomicInteger` | Lock-free counter | UPI daily txn count per user |
| `AtomicReference<T>` | CAS on single object | LTV ratio update, KYC status cache |
| `synchronized` + re-fetch | Concurrent state finalization | `tryFinalizeApplication()` |
| `@Version` (JPA) | Optimistic lock, frequent writes | GoldLoan repayment updates |
| `SELECT FOR UPDATE` | Pessimistic lock, rare + critical | Gold Loan disbursal |
| `ReadWriteLock` | Read-heavy, infrequent write | Bulk re-KYC job reads |
| `ReentrantLock` + `tryLock` | Need timeout / fairness | Transaction reversal |
| `volatile` | Single variable visibility | Singleton instance in double-checked lock |

---

## Exception Hierarchy — Use Across All Three Systems

```java
// Base — shared
class AppException extends RuntimeException {
    private final String errorCode;
    AppException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
    public String getErrorCode() { return errorCode; }
}

// shared
class InvalidStateException extends AppException {
    InvalidStateException(String state, String op) {
        super("INVALID_STATE", "Cannot '" + op + "' in state: " + state);
    }
}

// Gold Loan
class KycNotVerifiedException     extends AppException { ... }
class LtvBreachException          extends AppException { ... }

// UPI
class VpaNotFoundException        extends AppException { ... }
class RateLimitExceededException  extends AppException { ... }
class MpinMismatchException       extends AppException { ... }
class PaymentReversedException    extends AppException { ... }

// KYC
class KycExpiredException         extends AppException { ... }
class DocumentAlreadySubmittedException extends AppException { ... }
```

---

## SOLID → One-Liner Per Principle (Memorise)

| Principle | One-liner |
|-----------|-----------|
| **SRP** | One class, one reason to change. `GoldLoanService` ≠ `LoanRepaymentService` ≠ `LtvMonitoringJob`. |
| **OCP** | New requirement = new class. Never edit existing working code. Add `FlexiEmiInterestCalculator`, never modify `EmiInterestCalculator`. |
| **LSP** | Any subclass substitutes its parent without breaking callers. `ClosedLoanState` throwing is correct — not a violation. |
| **ISP** | `GoldLoanService` uses `KycCheckService` (4 methods), not the full `KycService` (20 methods). |
| **DIP** | Depend on interface. Inject concrete via constructor. Never `new ConcreteClass()` inside a service. |
| **Composition over Inheritance** | `GoldLoanService` has-a `InterestCalculator`, not extends. `PaymentService` has-a `RateLimiter`, not extends. |
| **Encapsulation** | All fields private. State changes only via named methods — `markSuccess()`, `reduceOutstanding()`, `setState()`. |
| **Fail Fast** | `validateRequest()` runs first. Invalid state never propagates to DB, bank APIs, or external systems. |

---

## Domain Facts to Drop (Impresses the Interviewer)

| Fact | Use When |
|------|----------|
| RBI max LTV = 75% for gold loans | Step 1 NFR or LTV check code |
| Gold valuation valid 30 days | `GoldValuation.expiresAt` |
| KYC expires every 2 years (RBI mandate) | Step 1 NFR for KYC |
| PAN mandatory if Gold Loan or income > Rs 5L | KYC-Gold Loan integration |
| UPI max Rs 1 lakh per transaction | Step 1 NFR, validatePaymentRequest() |
| New users: 20 UPI txns/day (NPCI rule) | UpiRateLimiter |
| NPCI peak ~6,000 TPS | Step 12 scaling |
| Kotak Gold Loan: Rs 20,000 to Rs 1.5 crore | Domain intro |
| Tenure: 3–48 months | validateRequest() |
| Repayment: Bullet, EMI, Vanilla | Step 1 FR — drives Strategy pattern |
| Foreclosure charges: 2.25% before minimum tenure | Edge case list |
| Aadhaar: only last 4 digits in API (VID masking) | AadhaarValidator code comment |

---

*Kotak SDE-2 LLD Round — Gold Loan Team Manager | April 2026*
*Follow every step. Call out every step name. Show the code comment block on every class.*
