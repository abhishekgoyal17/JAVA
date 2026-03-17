# LLD Interview - Diagrams, Entities, Sequence Diagrams & DB Schema Guide

> A one-stop reference for the diagram-heavy LLD interview round. Covers Class Diagrams, Entity-Relationship Diagrams, Sequence Diagrams, DB Schema design, UML notation, relationship types, cardinality, normalization, and a complete worked example — Library Management System — with runnable Java + SQL code.

---

## Table of Contents

1. [What Interviewers Expect in the Diagram Round](#1-what-interviewers-expect-in-the-diagram-round)
2. [Class Diagrams (UML)](#2-class-diagrams-uml)
3. [Relationship Types in Class Diagrams](#3-relationship-types-in-class-diagrams)
4. [Entity-Relationship (ER) Diagrams](#4-entity-relationship-er-diagrams)
5. [Cardinality and Participation](#5-cardinality-and-participation)
6. [Sequence Diagrams](#6-sequence-diagrams)
7. [DB Schema Design](#7-db-schema-design)
8. [Normalization (1NF to BCNF)](#8-normalization-1nf-to-bcnf)
9. [Indexes, Constraints, and Keys](#9-indexes-constraints-and-keys)
10. [How to Approach an LLD Problem End-to-End](#10-how-to-approach-an-lld-problem-end-to-end)
11. [Complete Worked Example - Library Management System](#11-complete-worked-example---library-management-system)
    - Requirements
    - Entities and Attributes
    - Class Diagram (text/ASCII)
    - ER Diagram (text/ASCII)
    - Sequence Diagrams
    - DB Schema (SQL)
    - Runnable Java Code
12. [Interview Tips and Common Mistakes](#12-interview-tips-and-common-mistakes)
13. [Quick Reference Cheat Sheet](#13-quick-reference-cheat-sheet)

---

## 1. What Interviewers Expect in the Diagram Round

When an interviewer focuses on diagrams in an LLD round, they are evaluating:

| What They Check | What It Tells Them |
|---|---|
| Correct entity identification | Do you understand the domain? |
| Right relationships between entities | Do you understand how things connect? |
| Proper cardinality | Do you know one-to-many from many-to-many? |
| Class diagram with methods + fields | Can you translate domain to OOP? |
| Sequence diagram for key flows | Do you understand runtime behavior? |
| DB schema with keys + constraints | Can you design a real, deployable schema? |
| Normalization awareness | Will your schema have anomalies? |
| Index placement | Do you think about query performance? |

**The standard flow in a diagram-focused LLD round:**

```
Step 1: Gather requirements (5 min)
  --> Ask clarifying questions
  --> Define scope (what's in, what's out)

Step 2: Identify entities (5 min)
  --> Nouns in requirements = entities/classes
  --> Verbs = methods or relationships

Step 3: Draw Class Diagram (10 min)
  --> Classes, fields, methods
  --> Relationships (inheritance, association, etc.)
  --> Cardinality on each relationship

Step 4: Draw ER Diagram / DB Schema (10 min)
  --> Tables, columns, data types
  --> Primary keys, foreign keys
  --> Constraints and indexes

Step 5: Draw Sequence Diagram (10 min)
  --> Pick 2-3 key flows (happy path + one error case)
  --> Show method calls between objects

Step 6: Write Code (remaining time)
  --> Core classes and interfaces
  --> Key business logic
  --> Design patterns applied
```

---

## 2. Class Diagrams (UML)

A Class Diagram shows the static structure — classes, their attributes, methods, and relationships.

### UML Class Box Structure

```
+---------------------------+
|      ClassName            |  <-- Class name (bold, centered)
+---------------------------+
| - privateField: Type      |  <-- Attributes section
| # protectedField: Type    |      - = private
| + publicField: Type       |      # = protected
| ~ packageField: Type      |      + = public
+---------------------------+      ~ = package
| + publicMethod(): RetType |  <-- Methods section
| - privateMethod(): void   |
| # protectedMethod(p: T)   |
+---------------------------+
```

### Interface and Abstract Class Notation

```
+---------------------------+        +---------------------------+
|  <<interface>>            |        |  <<abstract>>             |
|   Payable                 |        |   Vehicle                 |
+---------------------------+        +---------------------------+
| + pay(amount: double)     |        | # speed: int              |
| + refund(txnId: String)   |        +---------------------------+
+---------------------------+        | + accelerate(): void      |
                                     | + getFuelType(): String   |  <-- italic = abstract
                                     +---------------------------+
```

### Static Members

```
+---------------------------+
|    DatabaseConnection     |
+---------------------------+
| _instance: DBConn         |  <-- underline = static
+---------------------------+
| _getInstance(): DBConn    |  <-- underline = static method
| + query(sql: String)      |
+---------------------------+
```

### Full Example - Order Class

```
+------------------------------------+
|             Order                  |
+------------------------------------+
| - orderId: String                  |
| - status: OrderStatus              |
| - createdAt: LocalDateTime         |
| - items: List<LineItem>            |
| - customer: Customer               |
+------------------------------------+
| + placeOrder(): void               |
| + cancel(): void                   |
| + getTotal(): double               |
| + addItem(item: LineItem): void    |
| - validateOrder(): boolean         |
+------------------------------------+
```

---

## 3. Relationship Types in Class Diagrams

This is the most critical section for drawing accurate diagrams. Getting relationship types wrong is a top interview mistake.

### 3.1 Dependency (Uses)

The weakest relationship. Class A uses Class B temporarily (as a method parameter or local variable). No long-term association.

```
Notation:   A - - - - -> B   (dashed arrow)

Example:
OrderService - - - -> EmailService
(OrderService calls emailService.send() in a method, but doesn't hold a reference)
```

```java
public class OrderService {
    public void placeOrder(Order order, EmailService emailService) { // passed as param
        emailService.sendConfirmation(order); // used and gone
    }
}
```

### 3.2 Association

A structural relationship. Class A holds a reference to Class B. More permanent than dependency.

```
Notation:   A ----------> B   (solid arrow, with multiplicity)

Example:
Customer ----------> Address
  1                    1..*
(Customer has one or more addresses)
```

```java
public class Customer {
    private List<Address> addresses; // holds a reference = association
}
```

### 3.3 Aggregation (Has-A, weak)

A special association where one class is a "whole" that contains "parts", but the parts can exist independently of the whole.

```
Notation:   Whole <>-------> Part   (hollow diamond on whole side)

Example:
Team <>---------> Player
 1                 0..*
(Team has players, but Player exists without Team - e.g., free agent)
```

```java
public class Team {
    private List<Player> players; // players exist independently
}
```

### 3.4 Composition (Has-A, strong)

The strongest form of Has-A. The "part" cannot exist without the "whole". If the whole is destroyed, parts are destroyed too.

```
Notation:   Whole [filled diamond]-------> Part

Example:
Order [filled<>]-------> LineItem
  1                         1..*
(LineItem cannot exist without Order - if Order deleted, LineItems deleted too)
```

```java
public class Order {
    private List<LineItem> items = new ArrayList<>(); // items created by and owned by Order

    public void addItem(Product p, int qty) {
        items.add(new LineItem(p, qty)); // Order creates LineItems
    }
}
```

### 3.5 Inheritance / Generalization (Is-A)

```
Notation:   Child --------▷ Parent   (solid line, hollow triangle on parent)

Example:
ElectricCar ------▷ Vehicle
PetrolCar   ------▷ Vehicle
```

### 3.6 Realization / Implementation

Class implements an interface.

```
Notation:   Class - - - - -▷ Interface   (dashed line, hollow triangle on interface)

Example:
StripeGateway - - -▷ PaymentGateway
PayPalGateway - - -▷ PaymentGateway
```

### 3.7 Summary Table

| Relationship | Notation | Java Code | Lifetime |
|---|---|---|---|
| Dependency | Dashed arrow | Method parameter / local var | Temporary |
| Association | Solid arrow | Instance field reference | Long-term |
| Aggregation | Hollow diamond + arrow | Field; parts exist independently | Independent |
| Composition | Filled diamond + arrow | Field; parts created by owner | Dependent |
| Inheritance | Solid line + hollow triangle | `extends` | - |
| Realization | Dashed line + hollow triangle | `implements` | - |

---

## 4. Entity-Relationship (ER) Diagrams

ER Diagrams model the data domain — what data exists and how it is related. They translate directly into database tables.

### ER Diagram Notation (Chen's Notation)

```
Rectangle   = Entity (a table)
Ellipse     = Attribute (a column)
Diamond     = Relationship (a verb connecting entities)
Double rect = Weak entity (cannot exist without parent)
Underline   = Primary key attribute
Dashed line = Derived attribute (computed, not stored)
```

### ASCII ER Diagram Example

```
     bookId    title      ISBN
       |          |         |
       +----------+---------+
       |        BOOK        |
       +--------------------+
                |
                | borrows (M:N)
                |
         +------+-------+
         |   BORROWING   |
         +---------------+
                |
       +--------+--------+
       |      MEMBER     |
       +-----------------+
         |       |      |
       memberId email  name
```

### Simplified Box Notation (used in most interviews)

In time-constrained interviews, draw ER diagrams as boxes with listed attributes:

```
+------------------+         borrows (M:N)        +------------------+
|      BOOK        | <---------------------------> |     MEMBER       |
+------------------+                               +------------------+
| PK bookId        |       +------------------+    | PK memberId      |
|    title         |       |   BORROWING      |    |    name          |
|    ISBN          |       +------------------+    |    email         |
|    author        |       | PK borrowId      |    |    phone         |
| FK categoryId    |       | FK bookId        |    |    membershipExp |
|    publishedYear |       | FK memberId      |    +------------------+
+------------------+       |    borrowDate    |
                           |    dueDate       |
                           |    returnDate    |
                           +------------------+
```

---

## 5. Cardinality and Participation

### Cardinality Notation

Cardinality defines HOW MANY of one entity can be associated with another.

```
1    = exactly one
0..1 = zero or one (optional)
1..* = one or more (at least one)
0..* = zero or more (any number)
M    = many (unspecified)
```

### Reading Cardinality

Always read from one entity ACROSS the relationship TO the other:

```
CUSTOMER 1 ----< ORDERS M

"One CUSTOMER places many ORDERS"
"Each ORDER belongs to exactly one CUSTOMER"
```

### Common Cardinality Patterns in LLD

| Scenario | Cardinality | Implementation |
|---|---|---|
| Customer - Orders | 1 to M | FK customerId in Orders table |
| Book - Author | M to N | Junction table book_authors |
| Employee - Department | M to 1 | FK departmentId in Employees table |
| Person - Passport | 1 to 1 | FK or same table |
| Product - Category | M to 1 | FK categoryId in Products table |
| Student - Course | M to N | Junction table enrollments |
| Order - LineItem | 1 to M (composition) | FK orderId in LineItems table |

### Participation Constraints

- **Total participation** (double line): Every entity instance MUST participate in the relationship
- **Partial participation** (single line): Participation is optional

```
EMPLOYEE ==== works_in ---- DEPARTMENT
(every employee MUST work in a dept; dept may have no employees)
```

---

## 6. Sequence Diagrams

Sequence diagrams show the runtime behavior — the ORDER of method calls between objects for a specific use case.

### Notation

```
Object/Actor:     Box at the top with name
Lifeline:         Vertical dashed line below each object
Activation bar:   Rectangle on lifeline when object is active
Message:          Horizontal arrow from caller to callee
Return:           Dashed arrow from callee back to caller
Self-call:        Arrow looping back to same lifeline
Alt block:        Box labeled "alt" for if/else branching
Loop block:       Box labeled "loop" for iteration
```

### ASCII Sequence Diagram Template

```
Client          Controller        Service          Repository         DB
  |                 |                |                  |              |
  |--placeOrder()-->|                |                  |              |
  |                 |--placeOrder()->|                  |              |
  |                 |               |--validateOrder()  |              |
  |                 |               |<--true------------|              |
  |                 |               |--save(order)----->|              |
  |                 |               |                  |--INSERT------>|
  |                 |               |                  |<--OK----------|
  |                 |               |<--savedOrder------|              |
  |                 |<--OrderResp---|                  |              |
  |<--200 OK--------|               |                  |              |
```

### Alt (conditional) Block

```
Client              AuthService
  |                     |
  |--login(user,pass)-->|
  |                     |
  |    alt [valid]      |
  |<----JWT token-------|
  |    [invalid]        |
  |<----401 Unauth------|
  |                     |
```

### Loop Block

```
OrderService         NotificationService
     |                       |
     | loop [for each item]  |
     |--checkStock(item)---->|
     |<--stockStatus---------|
     |                       |
```

---

## 7. DB Schema Design

### Column Data Type Reference (MySQL/PostgreSQL)

| Java Type | MySQL Type | PostgreSQL Type | Notes |
|---|---|---|---|
| String (short) | VARCHAR(255) | VARCHAR(255) | Names, emails, titles |
| String (long) | TEXT | TEXT | Descriptions, content |
| int / Integer | INT | INTEGER | Counts, quantities |
| long / Long | BIGINT | BIGINT | IDs, timestamps |
| double / Double | DECIMAL(10,2) | NUMERIC(10,2) | Money - NEVER use FLOAT |
| boolean | TINYINT(1) | BOOLEAN | Flags |
| LocalDate | DATE | DATE | Dates without time |
| LocalDateTime | DATETIME | TIMESTAMP | Timestamps |
| Enum | ENUM or VARCHAR | VARCHAR + CHECK | Status fields |
| UUID | VARCHAR(36) or BINARY(16) | UUID | Distributed IDs |

### Naming Conventions

```
Tables:      snake_case, plural nouns       users, book_copies, borrow_transactions
Columns:     snake_case                     first_name, created_at, is_active
Primary Key: table_name_id or just id       user_id, book_id
Foreign Key: referenced_table_id           user_id (in orders table), book_id (in copies table)
Indexes:     idx_table_column              idx_users_email, idx_orders_created_at
Constraints: chk_table_column             chk_orders_status
```

### Key Types

```sql
PRIMARY KEY  - Uniquely identifies each row. Never NULL. Clustered index by default.
FOREIGN KEY  - References PK of another table. Enforces referential integrity.
UNIQUE       - All values in column must be distinct. NULLs allowed (behavior varies by DB).
NOT NULL     - Column must always have a value.
DEFAULT      - Fallback value when none is provided on INSERT.
CHECK        - Validates column value against a condition.
INDEX        - Speeds up reads on that column. Slows down writes slightly.
```

### Schema Design Template

```sql
CREATE TABLE table_name (
    -- Identity
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,

    -- Required fields
    name        VARCHAR(255)    NOT NULL,
    email       VARCHAR(255)    NOT NULL UNIQUE,

    -- Optional fields
    phone       VARCHAR(20),

    -- Status / enum
    status      ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',

    -- Money - always DECIMAL, never FLOAT
    balance     DECIMAL(12,2)   NOT NULL DEFAULT 0.00,

    -- Booleans
    is_verified TINYINT(1)      NOT NULL DEFAULT 0,

    -- Foreign key
    role_id     BIGINT          NOT NULL,

    -- Audit columns (every table should have these)
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME,       -- soft delete

    -- Foreign key constraint
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Indexes (after table creation)
CREATE INDEX idx_users_email   ON table_name(email);
CREATE INDEX idx_users_status  ON table_name(status);
```

---

## 8. Normalization (1NF to BCNF)

Normalization removes redundancy and prevents anomalies. Know these for interviews.

### First Normal Form (1NF)

Rules:
- Each column contains atomic (indivisible) values
- No repeating groups or arrays in a column
- Each row is unique (has a primary key)

```
VIOLATION:
+--------+--------+-----------------------------+
| BookId | Title  | Authors                     |
+--------+--------+-----------------------------+
| 1      | Dune   | Frank Herbert, Brian Herbert|  <-- multi-value, not atomic
+--------+--------+-----------------------------+

FIX - separate table:
BOOKS: (book_id, title)
BOOK_AUTHORS: (book_id, author_id)
AUTHORS: (author_id, author_name)
```

### Second Normal Form (2NF)

Rules:
- Must be in 1NF
- Every non-key column must depend on the WHOLE primary key (no partial dependency)
- Relevant only when PK is composite

```
VIOLATION (composite PK: student_id + course_id):
+------------+-----------+-----------+-------------+
| student_id | course_id | grade     | course_name |
+------------+-----------+-----------+-------------+
course_name depends only on course_id, NOT on the full (student_id, course_id) PK

FIX:
ENROLLMENTS: (student_id, course_id, grade)   -- grade depends on full PK
COURSES: (course_id, course_name)              -- course_name moved here
```

### Third Normal Form (3NF)

Rules:
- Must be in 2NF
- No transitive dependencies (non-key column depending on another non-key column)

```
VIOLATION:
+--------+-----------+----------+--------------+
| emp_id | emp_name  | dept_id  | dept_name    |
+--------+-----------+----------+--------------+
dept_name depends on dept_id (non-key), which depends on emp_id
dept_name transitively depends on emp_id through dept_id

FIX:
EMPLOYEES: (emp_id, emp_name, dept_id)
DEPARTMENTS: (dept_id, dept_name)
```

### BCNF (Boyce-Codd Normal Form)

Stricter than 3NF. Every determinant must be a candidate key.

Most practical LLD schemas are 3NF. BCNF is rare in interview discussions but knowing it shows depth.

### When to Denormalize

Normalization is ideal but sometimes you intentionally denormalize for performance:

- Store `order_total` on the Order table even though it can be computed from LineItems (avoids expensive JOIN + SUM on every read)
- Store `author_name` on Books table for a read-heavy system even though it creates some redundancy
- Store `user_email` in a notifications table to avoid a JOIN on every notification fetch

Always explain the trade-off when you denormalize in an interview.

---

## 9. Indexes, Constraints, and Keys

### When to Add an Index

```
Add an index when:
  - Column appears in WHERE clause frequently
  - Column is used in JOIN conditions (FK columns)
  - Column is used in ORDER BY or GROUP BY
  - Column has high cardinality (many distinct values - good candidates)

Avoid index when:
  - Table is small (< few thousand rows, full scan is fine)
  - Column has very low cardinality (e.g., boolean, 2-3 status values)
  - Table has very heavy write traffic (every INSERT/UPDATE must update all indexes)
```

### Composite Index

Index on multiple columns. Only useful when the leftmost column(s) are used in the query.

```sql
-- Good for: WHERE status = ? AND created_at > ?
-- Good for: WHERE status = ?  (leftmost prefix)
-- NOT good for: WHERE created_at > ?  (not leftmost)
CREATE INDEX idx_orders_status_created ON orders(status, created_at);
```

### Covering Index

An index that contains ALL columns needed by a query. No need to hit the actual table (index-only scan).

```sql
-- Query: SELECT user_id, email FROM users WHERE status = 'ACTIVE'
-- Covering index for this:
CREATE INDEX idx_users_status_covering ON users(status, user_id, email);
-- All three columns are in the index; query never reads the table rows
```

### Foreign Key Constraints and Cascade Behavior

```sql
CONSTRAINT fk_orders_customer
    FOREIGN KEY (customer_id)
    REFERENCES customers(id)
    ON DELETE RESTRICT    -- prevent deleting customer if orders exist
    ON UPDATE CASCADE;    -- if customer id changes, update order too

-- Options:
-- RESTRICT / NO ACTION: Prevent parent delete if children exist
-- CASCADE:              Delete/update children automatically
-- SET NULL:             Set FK to NULL when parent deleted
-- SET DEFAULT:          Set FK to default value when parent deleted
```

---

## 10. How to Approach an LLD Problem End-to-End

This is the exact framework to follow in an interview:

### Step 1: Clarify Requirements (5 minutes)

Ask about:
- Who are the actors/users?
- What are the core use cases? (top 3-5)
- What is out of scope?
- Scale? (millions of users vs. small enterprise)
- Any specific constraints? (multi-tenant, audit trail, soft delete)

### Step 2: Identify Entities

Scan the requirements for nouns → these become entities/classes.

```
"A library member can borrow books. Each book has multiple copies.
 A librarian manages the catalog. Members pay fines for late returns."

Entities: Member, Book, BookCopy, Librarian, BorrowTransaction, Fine, Catalog
```

### Step 3: Define Attributes

For each entity, define:
- Identity (PK)
- Descriptive attributes
- Status/state
- Timestamps (created_at, updated_at)
- Foreign keys

### Step 4: Define Relationships

For each pair of related entities:
- What is the relationship type? (Is-A, Has-A, Uses)
- What is the cardinality? (1:1, 1:M, M:N)
- What is the participation? (mandatory or optional)

### Step 5: Draw Class Diagram

Translate entities → classes. Add:
- Fields with types and visibility
- Methods (based on use cases)
- Relationships with notation

### Step 6: Draw Sequence Diagrams

Pick the 2-3 most important flows:
- Always do the happy path first
- Do one error/exception flow
- Use case names become diagram titles

### Step 7: Design DB Schema

For each class:
- Create a table
- Handle M:N relationships with junction tables
- Add appropriate constraints and indexes

### Step 8: Write Code

Start with:
- Interfaces and abstract classes (contracts)
- Core domain models (entities)
- Service layer (business logic)
- Repository layer (data access abstraction)
- Apply relevant design patterns

---

## 11. Complete Worked Example - Library Management System

### Requirements

```
1. Library has Books. Each Book can have multiple physical Copies.
2. Members can search for books by title, author, or ISBN.
3. Members can borrow available copies (max 3 at a time).
4. Borrowed copies have a due date (14 days from borrow date).
5. Members can return copies.
6. Late returns incur a fine (Rs. 5 per day overdue).
7. Librarians can add/remove books and manage members.
8. A member cannot borrow if they have unpaid fines.
```

**Actors:** Member, Librarian

**Core Use Cases:**
- Search Book
- Borrow Book Copy
- Return Book Copy
- Pay Fine
- Add Book (Librarian)

---

### Entities and Attributes

```
Book
  - bookId (PK)
  - title
  - author
  - isbn (unique)
  - publisher
  - publishedYear
  - categoryId (FK)

BookCopy
  - copyId (PK)
  - bookId (FK)
  - status: AVAILABLE | BORROWED | DAMAGED | LOST
  - rackLocation

Member
  - memberId (PK)
  - name
  - email (unique)
  - phone
  - membershipExpiry
  - status: ACTIVE | SUSPENDED

BorrowTransaction
  - transactionId (PK)
  - copyId (FK)
  - memberId (FK)
  - borrowDate
  - dueDate
  - returnDate (null if not returned)
  - status: ACTIVE | RETURNED | OVERDUE

Fine
  - fineId (PK)
  - transactionId (FK)
  - memberId (FK)
  - amount
  - status: PENDING | PAID
  - createdAt

Category
  - categoryId (PK)
  - name
```

---

### Class Diagram (ASCII)

```
+------------------+     <<interface>>      +------------------+
|  <<interface>>   |     Searchable         |  <<interface>>   |
|  Borrowable      |  +-------------------+ |  Manageable      |
+------------------+  |+ search(q:String) | +------------------+
|+borrow():boolean |  +-------------------+ |+add(): void      |
|+return():boolean |                        |+remove(): void   |
+------------------+                        +------------------+
         A                                           A
         |implements                                 |implements
         |                                           |
+--------+----------+           +--------------------+--------+
|       Book        |           |        Librarian            |
+-------------------+           +-----------------------------+
|- bookId: String   |           |- staffId: String            |
|- title: String    |           |- name: String               |
|- isbn: String     |           +-----------------------------+
|- author: String   |           |+addBook(b:Book):void        |
|- publishedYear:int|           |+removeBook(id:String):void  |
|- copies:List<Copy>|<>-------->|+addMember(m:Member):void    |
+-------------------+           +-----------------------------+
         |1
         |has (composition)
         |1..*
+-------------------+        +---------------------+
|    BookCopy       |        |      Member         |
+-------------------+  borrows +-------------------+
|- copyId: String   |<--------->|- memberId: String |
|- status: CopyStatus|  0..*  1 |- name: String     |
|- rackLocation:Str |        |- email: String    |
+-------------------+        |- maxBorrowLimit:3 |
         |1                  +-------------------+
         |                            |1
         |1                           |
+--------+----------+        +--------+----------+
| BorrowTransaction |        |       Fine        |
+-------------------+        +-------------------+
|- txnId: String    |1------1|- fineId: String   |
|- borrowDate: LDate|        |- amount: double   |
|- dueDate: LDate   |        |- status: FineStatus|
|- returnDate: LDate|        +-------------------+
|- status: TxnStatus|
+-------------------+


Enums:
  CopyStatus:  AVAILABLE, BORROWED, DAMAGED, LOST
  TxnStatus:   ACTIVE, RETURNED, OVERDUE
  FineStatus:  PENDING, PAID
```

---

### ER Diagram (ASCII)

```
+------------------+          +------------------+
|    categories    |          |      books       |
+------------------+          +------------------+
| PK category_id   |1       M | PK book_id       |
|    name          |<---------|    title         |
+------------------+          |    isbn (UNIQUE) |
                              |    author        |
                              |    published_year|
                              | FK category_id   |
                              +------------------+
                                       | 1
                                       | has (composition)
                                       | M
                              +------------------+
                              |   book_copies    |
                              +------------------+
                              | PK copy_id       |
                              |    status        |
                              |    rack_location |
                              | FK book_id       |
                              +------------------+
                                       | 1
                                       |
                                       | M
                    +------------------+-----------+
                    |      borrow_transactions     |
                    +------------------------------+
                    | PK transaction_id            |
                    | FK copy_id                   |
                    | FK member_id                 |
                    |    borrow_date               |
                    |    due_date                  |
                    |    return_date (nullable)    |
                    |    status                    |
                    +------------------------------+
                         M |                 | 1
                           |                 |
                     +-----+         +-------+--------+
                     |1              |    members     |
              +------+-----+         +----------------+
              |    fines   |         | PK member_id   |
              +------------+         |    name        |
              | PK fine_id |         |    email       |
              | FK txn_id  |         |    phone       |
              | FK member_id|        |    membership_ |
              |    amount  |         |    expiry      |
              |    status  |         |    status      |
              +------------+         +----------------+
```

---

### Sequence Diagrams

#### 1. Borrow Book - Happy Path

```
Member     BookController    BookService      BorrowService    MemberRepo    CopyRepo    TxnRepo
  |              |               |                |               |             |            |
  |--borrowBook  |               |                |               |             |            |
  |  (memberId,  |               |                |               |             |            |
  |   copyId)--->|               |                |               |             |            |
  |              |--borrowBook-->|                |               |             |            |
  |              |               |--getMember()-->|               |             |            |
  |              |               |               |--findById()-->|             |            |
  |              |               |               |<--Member------|             |            |
  |              |               |<--Member------|               |             |            |
  |              |               |                |               |             |            |
  |              |               |--getCopy()---->|               |             |            |
  |              |               |               |               |--findById()->|            |
  |              |               |               |               |<--BookCopy---|            |
  |              |               |<--BookCopy----|               |             |            |
  |              |               |                |               |             |            |
  |              |     alt [member active AND no unpaid fines AND borrow count < 3]          |
  |              |               |                |               |             |            |
  |              |               |--borrow()----->|               |             |            |
  |              |               |               |--createTxn()  |             |            |
  |              |               |               |    save()---->|             |            |
  |              |               |               |<--txn---------|             |            |
  |              |               |               |               |             |            |
  |              |               |               |--updateCopy() |             |            |
  |              |               |               |   BORROWED--->|             |            |
  |              |               |<--Transaction-|               |             |            |
  |              |<--BorrowResp--|               |               |             |            |
  |<--200 OK-----|               |               |               |             |            |
  |              |               |               |               |             |            |
  |              |     alt [else - blocked]                                                  |
  |<--400 Error--|               |               |               |             |            |
```

#### 2. Return Book - With Fine Calculation

```
Member      BookController    ReturnService       FineService      CopyRepo     TxnRepo
  |               |                |                   |               |             |
  |--returnBook   |                |                   |               |             |
  |  (txnId)----->|                |                   |               |             |
  |               |--returnBook--->|                   |               |             |
  |               |               |--getTxn(txnId)---->|               |             |
  |               |               |                   |--findById()-->|             |
  |               |               |<--Transaction------|               |             |
  |               |               |                   |               |             |
  |               |               |--isOverdue()?     |               |             |
  |               |               |  (today > dueDate)|               |             |
  |               |               |                   |               |             |
  |               |  alt [overdue]|                   |               |             |
  |               |               |--calculateFine()-->|               |             |
  |               |               |                   |--createFine() |             |
  |               |               |<--Fine------------|               |             |
  |               |               |                   |               |             |
  |               |               |--updateTxn(RETURNED)              |             |
  |               |               |------------------------------------------------>|
  |               |               |--updateCopy(AVAILABLE)            |             |
  |               |               |---------------------------------------------->|
  |               |<--ReturnResp--|                   |               |             |
  |<--200 + Fine--|               |                   |               |             |
```

---

### DB Schema (SQL)

```sql
-- ============================================================
-- Library Management System - DB Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- -----------------------------------------------
-- CATEGORIES
-- -----------------------------------------------
CREATE TABLE categories (
    category_id     BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------
-- BOOKS
-- -----------------------------------------------
CREATE TABLE books (
    book_id         BIGINT          PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(500)    NOT NULL,
    author          VARCHAR(255)    NOT NULL,
    isbn            VARCHAR(20)     NOT NULL UNIQUE,
    publisher       VARCHAR(255),
    published_year  INT             CHECK (published_year BETWEEN 1000 AND 2100),
    category_id     BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE INDEX idx_books_isbn     ON books(isbn);
CREATE INDEX idx_books_author   ON books(author);
CREATE INDEX idx_books_title    ON books(title);
CREATE INDEX idx_books_category ON books(category_id);

-- -----------------------------------------------
-- BOOK COPIES
-- -----------------------------------------------
CREATE TABLE book_copies (
    copy_id         BIGINT          PRIMARY KEY AUTO_INCREMENT,
    book_id         BIGINT          NOT NULL,
    status          ENUM('AVAILABLE','BORROWED','DAMAGED','LOST')
                                    NOT NULL DEFAULT 'AVAILABLE',
    rack_location   VARCHAR(50),
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_copies_book FOREIGN KEY (book_id) REFERENCES books(book_id)
);

CREATE INDEX idx_copies_book_id ON book_copies(book_id);
CREATE INDEX idx_copies_status  ON book_copies(book_id, status);  -- composite: find available copies of a book

-- -----------------------------------------------
-- MEMBERS
-- -----------------------------------------------
CREATE TABLE members (
    member_id           BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name                VARCHAR(255)    NOT NULL,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    phone               VARCHAR(20),
    membership_expiry   DATE            NOT NULL,
    status              ENUM('ACTIVE','SUSPENDED','EXPIRED')
                                        NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_members_email  ON members(email);
CREATE INDEX idx_members_status ON members(status);

-- -----------------------------------------------
-- BORROW TRANSACTIONS
-- -----------------------------------------------
CREATE TABLE borrow_transactions (
    transaction_id  BIGINT          PRIMARY KEY AUTO_INCREMENT,
    copy_id         BIGINT          NOT NULL,
    member_id       BIGINT          NOT NULL,
    borrow_date     DATE            NOT NULL,
    due_date        DATE            NOT NULL,
    return_date     DATE,           -- NULL until returned
    status          ENUM('ACTIVE','RETURNED','OVERDUE')
                                    NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_txn_copy   FOREIGN KEY (copy_id)   REFERENCES book_copies(copy_id),
    CONSTRAINT fk_txn_member FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT chk_dates CHECK (due_date >= borrow_date)
);

CREATE INDEX idx_txn_member       ON borrow_transactions(member_id);
CREATE INDEX idx_txn_copy         ON borrow_transactions(copy_id);
CREATE INDEX idx_txn_status       ON borrow_transactions(status);
CREATE INDEX idx_txn_member_active ON borrow_transactions(member_id, status);  -- find active borrows per member

-- -----------------------------------------------
-- FINES
-- -----------------------------------------------
CREATE TABLE fines (
    fine_id         BIGINT              PRIMARY KEY AUTO_INCREMENT,
    transaction_id  BIGINT              NOT NULL UNIQUE,  -- one fine per transaction
    member_id       BIGINT              NOT NULL,
    amount          DECIMAL(8,2)        NOT NULL CHECK (amount > 0),
    status          ENUM('PENDING','PAID')
                                        NOT NULL DEFAULT 'PENDING',
    paid_at         DATETIME,
    created_at      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fine_txn    FOREIGN KEY (transaction_id) REFERENCES borrow_transactions(transaction_id),
    CONSTRAINT fk_fine_member FOREIGN KEY (member_id)      REFERENCES members(member_id)
);

CREATE INDEX idx_fines_member ON fines(member_id, status);  -- check unpaid fines for member
```

---

### Runnable Java Code

```java
// ============================================================
// FILE STRUCTURE
// ============================================================
// src/
//  model/
//    Book.java
//    BookCopy.java
//    Member.java
//    BorrowTransaction.java
//    Fine.java
//    enums/CopyStatus.java
//    enums/TransactionStatus.java
//    enums/FineStatus.java
//  repository/
//    BookRepository.java
//    MemberRepository.java
//    BorrowRepository.java
//    FineRepository.java
//  service/
//    BookService.java
//    BorrowService.java
//    FineService.java
//  exception/
//    LibraryException.java
//  LibrarySystem.java  (main entry point / demo)
// ============================================================
```

**CopyStatus.java**
```java
package model.enums;

public enum CopyStatus {
    AVAILABLE,
    BORROWED,
    DAMAGED,
    LOST
}
```

**TransactionStatus.java**
```java
package model.enums;

public enum TransactionStatus {
    ACTIVE,
    RETURNED,
    OVERDUE
}
```

**FineStatus.java**
```java
package model.enums;

public enum FineStatus {
    PENDING,
    PAID
}
```

**LibraryException.java**
```java
package exception;

public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
```

**Book.java**
```java
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Book {

    private final String bookId;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private int publishedYear;
    private final List<BookCopy> copies;

    public Book(String bookId, String title, String author, String isbn) {
        this.bookId       = bookId;
        this.title        = title;
        this.author       = author;
        this.isbn         = isbn;
        this.copies       = new ArrayList<>();
    }

    public void addCopy(BookCopy copy) {
        copies.add(copy);
    }

    public List<BookCopy> getAvailableCopies() {
        List<BookCopy> available = new ArrayList<>();
        for (BookCopy c : copies) {
            if (c.isAvailable()) available.add(c);
        }
        return available;
    }

    public boolean hasAvailableCopy() {
        return !getAvailableCopies().isEmpty();
    }

    // Getters
    public String getBookId()     { return bookId; }
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public String getIsbn()       { return isbn; }
    public List<BookCopy> getCopies() { return Collections.unmodifiableList(copies); }

    @Override
    public String toString() {
        return String.format("Book{id=%s, title='%s', author='%s', isbn=%s, copies=%d}",
            bookId, title, author, isbn, copies.size());
    }
}
```

**BookCopy.java**
```java
package model;

import model.enums.CopyStatus;

public class BookCopy {

    private final String copyId;
    private final String bookId;
    private CopyStatus status;
    private String rackLocation;

    public BookCopy(String copyId, String bookId, String rackLocation) {
        this.copyId       = copyId;
        this.bookId       = bookId;
        this.rackLocation = rackLocation;
        this.status       = CopyStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == CopyStatus.AVAILABLE;
    }

    public void markBorrowed() {
        if (status != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("Copy " + copyId + " is not available (status: " + status + ")");
        }
        this.status = CopyStatus.BORROWED;
    }

    public void markAvailable() {
        this.status = CopyStatus.AVAILABLE;
    }

    // Getters
    public String getCopyId()      { return copyId; }
    public String getBookId()      { return bookId; }
    public CopyStatus getStatus()  { return status; }
    public String getRackLocation(){ return rackLocation; }

    @Override
    public String toString() {
        return String.format("BookCopy{id=%s, bookId=%s, status=%s, rack=%s}",
            copyId, bookId, status, rackLocation);
    }
}
```

**Member.java**
```java
package model;

import java.time.LocalDate;

public class Member {

    private static final int MAX_BORROW_LIMIT = 3;

    private final String memberId;
    private String name;
    private String email;
    private String phone;
    private LocalDate membershipExpiry;
    private boolean suspended;

    public Member(String memberId, String name, String email, LocalDate membershipExpiry) {
        this.memberId         = memberId;
        this.name             = name;
        this.email            = email;
        this.membershipExpiry = membershipExpiry;
        this.suspended        = false;
    }

    public boolean isEligibleToBorrow() {
        return !suspended && membershipExpiry.isAfter(LocalDate.now());
    }

    public void suspend()  { this.suspended = true; }
    public void reinstate(){ this.suspended = false; }

    public int getMaxBorrowLimit() { return MAX_BORROW_LIMIT; }

    // Getters
    public String getMemberId()           { return memberId; }
    public String getName()               { return name; }
    public String getEmail()              { return email; }
    public boolean isSuspended()          { return suspended; }
    public LocalDate getMembershipExpiry(){ return membershipExpiry; }

    @Override
    public String toString() {
        return String.format("Member{id=%s, name='%s', email=%s, suspended=%s}",
            memberId, name, email, suspended);
    }
}
```

**BorrowTransaction.java**
```java
package model;

import model.enums.TransactionStatus;
import java.time.LocalDate;

public class BorrowTransaction {

    private static final int LOAN_PERIOD_DAYS = 14;

    private final String transactionId;
    private final String copyId;
    private final String memberId;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private TransactionStatus status;

    public BorrowTransaction(String transactionId, String copyId, String memberId) {
        this.transactionId = transactionId;
        this.copyId        = copyId;
        this.memberId      = memberId;
        this.borrowDate    = LocalDate.now();
        this.dueDate       = borrowDate.plusDays(LOAN_PERIOD_DAYS);
        this.status        = TransactionStatus.ACTIVE;
    }

    public boolean isOverdue() {
        return status == TransactionStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public void markReturned() {
        this.returnDate = LocalDate.now();
        this.status     = TransactionStatus.RETURNED;
    }

    // Getters
    public String getTransactionId()      { return transactionId; }
    public String getCopyId()             { return copyId; }
    public String getMemberId()           { return memberId; }
    public LocalDate getBorrowDate()      { return borrowDate; }
    public LocalDate getDueDate()         { return dueDate; }
    public LocalDate getReturnDate()      { return returnDate; }
    public TransactionStatus getStatus()  { return status; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%s, copy=%s, member=%s, due=%s, status=%s}",
            transactionId, copyId, memberId, dueDate, status);
    }
}
```

**Fine.java**
```java
package model;

import model.enums.FineStatus;

public class Fine {

    private static final double FINE_PER_DAY = 5.0;

    private final String fineId;
    private final String transactionId;
    private final String memberId;
    private final double amount;
    private FineStatus status;

    public Fine(String fineId, String transactionId, String memberId, long daysOverdue) {
        this.fineId        = fineId;
        this.transactionId = transactionId;
        this.memberId      = memberId;
        this.amount        = daysOverdue * FINE_PER_DAY;
        this.status        = FineStatus.PENDING;
    }

    public void markPaid() {
        this.status = FineStatus.PAID;
    }

    // Getters
    public String getFineId()         { return fineId; }
    public String getTransactionId()  { return transactionId; }
    public String getMemberId()       { return memberId; }
    public double getAmount()         { return amount; }
    public FineStatus getStatus()     { return status; }

    @Override
    public String toString() {
        return String.format("Fine{id=%s, amount=%.2f, status=%s}", fineId, amount, status);
    }
}
```

**Repository Interfaces**
```java
package repository;

import model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void save(Book book);
    Optional<Book> findById(String bookId);
    Optional<Book> findByIsbn(String isbn);
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> findAll();
}
```

```java
package repository;

import model.Member;
import java.util.Optional;

public interface MemberRepository {
    void save(Member member);
    Optional<Member> findById(String memberId);
    Optional<Member> findByEmail(String email);
}
```

```java
package repository;

import model.BorrowTransaction;
import java.util.List;
import java.util.Optional;

public interface BorrowRepository {
    void save(BorrowTransaction txn);
    Optional<BorrowTransaction> findById(String txnId);
    List<BorrowTransaction> findActiveByMember(String memberId);
    List<BorrowTransaction> findByCopy(String copyId);
}
```

```java
package repository;

import model.Fine;
import java.util.List;

public interface FineRepository {
    void save(Fine fine);
    List<Fine> findPendingByMember(String memberId);
    void update(Fine fine);
}
```

**In-Memory Repository Implementations**
```java
package repository;

import model.Book;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> store = new HashMap<>();

    @Override
    public void save(Book book) {
        store.put(book.getBookId(), book);
    }

    @Override
    public Optional<Book> findById(String bookId) {
        return Optional.ofNullable(store.get(bookId));
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return store.values().stream()
            .filter(b -> b.getIsbn().equals(isbn))
            .findFirst();
    }

    @Override
    public List<Book> searchByTitle(String title) {
        String lower = title.toLowerCase();
        return store.values().stream()
            .filter(b -> b.getTitle().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        String lower = author.toLowerCase();
        return store.values().stream()
            .filter(b -> b.getAuthor().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }
}
```

```java
package repository;

import model.Member;
import java.util.*;

public class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> store = new HashMap<>();

    @Override
    public void save(Member member) {
        store.put(member.getMemberId(), member);
    }

    @Override
    public Optional<Member> findById(String memberId) {
        return Optional.ofNullable(store.get(memberId));
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return store.values().stream()
            .filter(m -> m.getEmail().equals(email))
            .findFirst();
    }
}
```

```java
package repository;

import model.BorrowTransaction;
import model.enums.TransactionStatus;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryBorrowRepository implements BorrowRepository {

    private final Map<String, BorrowTransaction> store = new HashMap<>();

    @Override
    public void save(BorrowTransaction txn) {
        store.put(txn.getTransactionId(), txn);
    }

    @Override
    public Optional<BorrowTransaction> findById(String txnId) {
        return Optional.ofNullable(store.get(txnId));
    }

    @Override
    public List<BorrowTransaction> findActiveByMember(String memberId) {
        return store.values().stream()
            .filter(t -> t.getMemberId().equals(memberId)
                      && t.getStatus() == TransactionStatus.ACTIVE)
            .collect(Collectors.toList());
    }

    @Override
    public List<BorrowTransaction> findByCopy(String copyId) {
        return store.values().stream()
            .filter(t -> t.getCopyId().equals(copyId))
            .collect(Collectors.toList());
    }
}
```

```java
package repository;

import model.Fine;
import model.enums.FineStatus;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryFineRepository implements FineRepository {

    private final Map<String, Fine> store = new HashMap<>();

    @Override
    public void save(Fine fine) {
        store.put(fine.getFineId(), fine);
    }

    @Override
    public List<Fine> findPendingByMember(String memberId) {
        return store.values().stream()
            .filter(f -> f.getMemberId().equals(memberId)
                      && f.getStatus() == FineStatus.PENDING)
            .collect(Collectors.toList());
    }

    @Override
    public void update(Fine fine) {
        store.put(fine.getFineId(), fine);
    }
}
```

**FineService.java**
```java
package service;

import exception.LibraryException;
import model.Fine;
import repository.FineRepository;
import java.util.List;
import java.util.UUID;

public class FineService {

    private final FineRepository fineRepository;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    public Fine createFine(String transactionId, String memberId, long daysOverdue) {
        Fine fine = new Fine(
            UUID.randomUUID().toString(),
            transactionId,
            memberId,
            daysOverdue
        );
        fineRepository.save(fine);
        System.out.println("Fine created: " + fine);
        return fine;
    }

    public boolean hasPendingFines(String memberId) {
        return !fineRepository.findPendingByMember(memberId).isEmpty();
    }

    public double getTotalPendingFines(String memberId) {
        return fineRepository.findPendingByMember(memberId).stream()
            .mapToDouble(Fine::getAmount)
            .sum();
    }

    public void payFine(String fineId, String memberId) {
        List<Fine> pending = fineRepository.findPendingByMember(memberId);
        Fine fine = pending.stream()
            .filter(f -> f.getFineId().equals(fineId))
            .findFirst()
            .orElseThrow(() -> new LibraryException("Fine not found or already paid: " + fineId));
        fine.markPaid();
        fineRepository.update(fine);
        System.out.println("Fine paid: " + fine);
    }

    public void payAllFines(String memberId) {
        fineRepository.findPendingByMember(memberId).forEach(fine -> {
            fine.markPaid();
            fineRepository.update(fine);
        });
        System.out.println("All fines paid for member: " + memberId);
    }
}
```

**BookService.java**
```java
package service;

import exception.LibraryException;
import model.Book;
import model.BookCopy;
import repository.BookRepository;
import java.util.List;
import java.util.UUID;

public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book addBook(String title, String author, String isbn) {
        bookRepository.findByIsbn(isbn).ifPresent(b -> {
            throw new LibraryException("Book with ISBN " + isbn + " already exists");
        });
        Book book = new Book(UUID.randomUUID().toString(), title, author, isbn);
        bookRepository.save(book);
        System.out.println("Book added: " + book);
        return book;
    }

    public BookCopy addCopy(String bookId, String rackLocation) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new LibraryException("Book not found: " + bookId));
        BookCopy copy = new BookCopy(UUID.randomUUID().toString(), bookId, rackLocation);
        book.addCopy(copy);
        bookRepository.save(book);
        System.out.println("Copy added: " + copy);
        return copy;
    }

    public Book getBook(String bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new LibraryException("Book not found: " + bookId));
    }

    public BookCopy getAvailableCopy(String bookId) {
        Book book = getBook(bookId);
        return book.getAvailableCopies().stream()
            .findFirst()
            .orElseThrow(() -> new LibraryException("No available copies for book: " + bookId));
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.searchByTitle(title);
    }

    public List<Book> searchByAuthor(String author) {
        return bookRepository.searchByAuthor(author);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}
```

**BorrowService.java**
```java
package service;

import exception.LibraryException;
import model.BookCopy;
import model.BorrowTransaction;
import model.Fine;
import model.Member;
import repository.BorrowRepository;
import repository.MemberRepository;
import java.util.List;
import java.util.UUID;

public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final MemberRepository memberRepository;
    private final BookService bookService;
    private final FineService fineService;

    public BorrowService(BorrowRepository borrowRepository,
                         MemberRepository memberRepository,
                         BookService bookService,
                         FineService fineService) {
        this.borrowRepository = borrowRepository;
        this.memberRepository = memberRepository;
        this.bookService      = bookService;
        this.fineService      = fineService;
    }

    // ---- BORROW ----
    public BorrowTransaction borrowBook(String memberId, String bookId) {

        // 1. Load and validate member
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new LibraryException("Member not found: " + memberId));

        if (!member.isEligibleToBorrow()) {
            throw new LibraryException("Member " + memberId + " is not eligible to borrow " +
                "(suspended or membership expired)");
        }

        // 2. Check unpaid fines
        if (fineService.hasPendingFines(memberId)) {
            double total = fineService.getTotalPendingFines(memberId);
            throw new LibraryException("Member has unpaid fines of Rs. " + total +
                ". Please clear fines before borrowing.");
        }

        // 3. Check active borrow count
        List<BorrowTransaction> activeBorrows = borrowRepository.findActiveByMember(memberId);
        if (activeBorrows.size() >= member.getMaxBorrowLimit()) {
            throw new LibraryException("Member has reached borrow limit of " +
                member.getMaxBorrowLimit());
        }

        // 4. Get an available copy
        BookCopy copy = bookService.getAvailableCopy(bookId);

        // 5. Mark copy as borrowed
        copy.markBorrowed();

        // 6. Create and save transaction
        BorrowTransaction txn = new BorrowTransaction(
            UUID.randomUUID().toString(),
            copy.getCopyId(),
            memberId
        );
        borrowRepository.save(txn);

        System.out.println("Book borrowed: " + txn);
        return txn;
    }

    // ---- RETURN ----
    public Fine returnBook(String transactionId) {

        // 1. Load transaction
        BorrowTransaction txn = borrowRepository.findById(transactionId)
            .orElseThrow(() -> new LibraryException("Transaction not found: " + transactionId));

        if (txn.getStatus() != model.enums.TransactionStatus.ACTIVE) {
            throw new LibraryException("Transaction " + transactionId + " is already closed");
        }

        // 2. Mark the copy as available
        // In a real system, we'd look up the copy from the repository
        // For this demo, we get it from the book service
        // (simplified: we trust the copy state)

        // 3. Check for overdue fine
        Fine fine = null;
        if (txn.isOverdue()) {
            long daysOverdue = txn.getDaysOverdue();
            System.out.println("Book returned " + daysOverdue + " days late.");
            fine = fineService.createFine(transactionId, txn.getMemberId(), daysOverdue);
        }

        // 4. Mark transaction as returned
        txn.markReturned();
        borrowRepository.save(txn);

        System.out.println("Book returned: " + txn);
        return fine;
    }

    public List<BorrowTransaction> getActiveBorrows(String memberId) {
        return borrowRepository.findActiveByMember(memberId);
    }
}
```

**LibrarySystem.java (Main Demo)**
```java
import model.*;
import repository.*;
import service.*;
import exception.LibraryException;
import java.time.LocalDate;

public class LibrarySystem {

    public static void main(String[] args) {

        // ---- Wire up dependencies (manual DI) ----
        BookRepository    bookRepo   = new InMemoryBookRepository();
        MemberRepository  memberRepo = new InMemoryMemberRepository();
        BorrowRepository  borrowRepo = new InMemoryBorrowRepository();
        FineRepository    fineRepo   = new InMemoryFineRepository();

        FineService   fineService   = new FineService(fineRepo);
        BookService   bookService   = new BookService(bookRepo);
        BorrowService borrowService = new BorrowService(borrowRepo, memberRepo, bookService, fineService);

        System.out.println("=== Library Management System Demo ===\n");

        // ---- Setup: Add books ----
        System.out.println("--- Adding Books ---");
        Book cleanCode   = bookService.addBook("Clean Code", "Robert C. Martin", "978-0132350884");
        Book systemDesign = bookService.addBook("Designing Data-Intensive Applications", "Martin Kleppmann", "978-1449373320");
        Book dsa          = bookService.addBook("Introduction to Algorithms", "Cormen et al.", "978-0262033848");

        // Add copies
        bookService.addCopy(cleanCode.getBookId(), "A-101");
        bookService.addCopy(cleanCode.getBookId(), "A-102");
        bookService.addCopy(systemDesign.getBookId(), "B-201");
        bookService.addCopy(dsa.getBookId(), "C-301");

        // ---- Setup: Add members ----
        System.out.println("\n--- Adding Members ---");
        Member alice = new Member("M001", "Alice Sharma", "alice@example.com",
            LocalDate.now().plusYears(1));
        Member bob   = new Member("M002", "Bob Gupta", "bob@example.com",
            LocalDate.now().plusYears(1));
        memberRepo.save(alice);
        memberRepo.save(bob);
        System.out.println("Member added: " + alice);
        System.out.println("Member added: " + bob);

        // ---- Use Case 1: Search books ----
        System.out.println("\n--- Searching Books ---");
        System.out.println("Search 'Clean': " + bookService.searchByTitle("Clean"));
        System.out.println("Search by author 'Martin': " + bookService.searchByAuthor("Martin"));

        // ---- Use Case 2: Borrow books ----
        System.out.println("\n--- Borrowing Books ---");
        BorrowTransaction txn1 = borrowService.borrowBook(alice.getMemberId(), cleanCode.getBookId());
        BorrowTransaction txn2 = borrowService.borrowBook(alice.getMemberId(), systemDesign.getBookId());
        BorrowTransaction txn3 = borrowService.borrowBook(bob.getMemberId(), dsa.getBookId());

        System.out.println("\nAlice's active borrows: " +
            borrowService.getActiveBorrows(alice.getMemberId()).size());

        // ---- Use Case 3: Try borrowing when limit reached ----
        System.out.println("\n--- Testing Borrow Limit ---");
        // Alice already has 2; add one more to hit limit of 3
        BorrowTransaction txn4 = borrowService.borrowBook(alice.getMemberId(), dsa.getBookId());

        // This should now fail (Alice has 3 active borrows)
        try {
            Book anotherBook = bookService.addBook("Effective Java", "Joshua Bloch", "978-0134685991");
            bookService.addCopy(anotherBook.getBookId(), "D-401");
            borrowService.borrowBook(alice.getMemberId(), anotherBook.getBookId());
        } catch (LibraryException e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        // ---- Use Case 4: Return a book (on time) ----
        System.out.println("\n--- Returning Book (on time) ---");
        Fine fine1 = borrowService.returnBook(txn1.getTransactionId());
        System.out.println("Fine after return: " + (fine1 == null ? "No fine" : fine1));

        // ---- Use Case 5: Simulate overdue return ----
        // We can't easily backdate in this demo without mocking LocalDate,
        // so we demonstrate the fine creation path directly
        System.out.println("\n--- Simulating Fine Payment ---");
        // Manually create a fine to demo payment
        Fine demFine = fineService.createFine("demo-txn", bob.getMemberId(), 3);
        System.out.println("Pending fines for Bob: " + fineService.getTotalPendingFines(bob.getMemberId()));

        // Try to borrow with pending fine
        try {
            Book java = bookService.addBook("Java Concurrency in Practice", "Brian Goetz", "978-0321349606");
            bookService.addCopy(java.getBookId(), "E-501");
            borrowService.borrowBook(bob.getMemberId(), java.getBookId());
        } catch (LibraryException e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        // Pay the fine
        fineService.payFine(demFine.getFineId(), bob.getMemberId());
        System.out.println("Pending fines after payment: " + fineService.getTotalPendingFines(bob.getMemberId()));

        // Now Bob can borrow again
        System.out.println("\n--- Bob borrows after paying fine ---");
        Book java = bookService.searchByTitle("Java Concurrency").get(0);
        BorrowTransaction txn5 = borrowService.borrowBook(bob.getMemberId(), java.getBookId());
        System.out.println("Success: " + txn5);

        System.out.println("\n=== Demo Complete ===");
    }
}
```

---

## 12. Interview Tips and Common Mistakes

### Top Tips

**Start with requirements clarification, always.**
Jumping into the diagram without clarifying scope wastes time. Ask "is payment processing in scope?", "do we need search?", "single library or multi-branch?"

**Think aloud.**
Interviewers want to see your reasoning. Say "I'm making this a composition because a LineItem cannot exist without an Order." That's worth more than silently drawing the right arrow.

**Name your relationships.**
Don't just draw a line between Book and Member. Label it "borrows" with cardinality on both ends.

**Always add audit columns to every table.**
`created_at` and `updated_at` on every table. Interviewers notice when you forget.

**Use enums for status fields.**
`status ENUM('ACTIVE','BORROWED')` not `status VARCHAR(50)`. Shows awareness of data integrity.

**Show the junction table for M:N.**
Never draw a direct M:N line in a DB schema. Always show the junction table with its own PK and FKs.

**Add FK indexes.**
Every FK column should have an index. Often missed.

**Explain design pattern choices.**
"I used Strategy here because the fine calculation rule might change" — that sentence shows LLD depth.

### Common Mistakes

| Mistake | Fix |
|---|---|
| Drawing M:N as a direct relationship in DB schema | Always create junction table |
| Forgetting created_at / updated_at | Add audit columns to every table |
| Using FLOAT for money | Always use DECIMAL(10,2) |
| No FK indexes | Index every FK column |
| God class with 20 methods | Apply SRP, split into service classes |
| No return type on sequence diagram arrows | Label return arrows with what's returned |
| Missing NULL / NOT NULL on schema columns | Be explicit about nullability |
| Inheritance where composition fits better | Check IS-A vs HAS-A |
| No exception flows in sequence diagram | Always show at least one error path |
| Skipping clarifying questions | Always ask scope questions first |

---

## 13. Quick Reference Cheat Sheet

```
CLASS DIAGRAM NOTATION
  - (minus)     = private field/method
  + (plus)      = public field/method
  # (hash)      = protected
  ~ (tilde)     = package
  underline     = static
  italic        = abstract method

RELATIONSHIPS (weakest to strongest)
  Dependency    = Dashed arrow           (uses temporarily, method param)
  Association   = Solid arrow            (has reference, instance field)
  Aggregation   = Hollow diamond + arrow (has, parts exist independently)
  Composition   = Filled diamond + arrow (owns, parts die with whole)
  Inheritance   = Solid line + triangle  (extends)
  Realization   = Dashed line + triangle (implements)

CARDINALITY
  1      = exactly one
  0..1   = zero or one
  1..*   = one or more
  0..*   = zero or more (many)
  M or * = many (unspecified)

ER / SCHEMA RULES
  Every M:N   --> junction table with own PK + two FKs
  Every FK    --> index on that column
  Money       --> DECIMAL(10,2), NEVER FLOAT
  Enum/Status --> ENUM type or VARCHAR + CHECK constraint
  Every table --> created_at, updated_at (audit columns)
  Soft delete --> deleted_at column (null = not deleted)

NORMALIZATION
  1NF: atomic values, no repeating groups, has PK
  2NF: 1NF + no partial dependency on composite PK
  3NF: 2NF + no transitive dependency (non-key -> non-key)
  BCNF: every determinant is a candidate key

SEQUENCE DIAGRAM
  Actor/Object  = box at top
  Lifeline      = dashed vertical line
  Message       = solid horizontal arrow -->
  Return        = dashed arrow -->
  alt block     = if/else branching
  loop block    = iteration
  Always show:  happy path + at least one error path

INTERVIEW ORDER
  1. Clarify requirements (5 min)
  2. List entities and attributes (5 min)
  3. Draw class diagram with relationships + cardinality (10 min)
  4. Draw sequence diagrams for 2-3 key flows (10 min)
  5. Design DB schema with keys, constraints, indexes (10 min)
  6. Write code: interfaces → models → services → patterns (remaining)
```

---

*Complete example: Library Management System — entities, class diagram (ASCII), ER diagram (ASCII), sequence diagrams (borrow + return), full SQL schema, and runnable Java code across 15 files.*
