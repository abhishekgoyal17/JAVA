# Java Interview Preparation Repository

A structured Java learning and interview-preparation workspace covering Java core concepts, concurrency, low-level design, design patterns, Spring Boot, and runnable projects.

## Repository Layout

```text
docs/       Concept notes and interview preparation material
examples/   Focused examples such as multithreading and design patterns
projects/   Runnable low-level-design and Spring Boot projects
```

## Learning Path

1. **Java Core** — exceptions, JIT compiler, memory management.
2. **Concurrency** — volatile, thread coordination, executors, locks, barriers, countdown latches.
3. **Design Patterns** — adapter, builder, composite, flyweight, chain of responsibility.
4. **Low-Level Design** — design principles, diagrams, interview rulebook, common machine-coding rounds.
5. **Projects** — cache, TTL cache, Splitwise, Vending Machine, BookMyShow, Snake and Ladder, Tic Tac Toe, Google Drive, URL shortener, Todo API.
6. **Spring Boot** — application context, REST APIs, JPA, validation, actuator, OpenAPI.

## Useful Commands

```bash
./gradlew projects
./gradlew build
./gradlew :projects:bookmyshow:build
./gradlew :examples:multithreading:build
./gradlew :projects:todo-api:build
```

## Notes

- Generated files such as `build/`, `bin/`, `.gradle/`, IDE metadata, and OS files are intentionally ignored.
- The root Gradle wrapper is the only wrapper kept in the repository.
- Each important module should have its own `README.md` describing the problem, design, and how to run it.
- Spring Boot modules use the Spring Boot dependency BOM. Apply the `org.springframework.boot` plugin later if you install/run Gradle with JDK 17+ and want `bootRun`/executable jars.
