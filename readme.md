# Multithreading in Java

## Overview

This project explains key concepts related to **Multithreading in Java** and its practical applications.

### Program
A **Program** is a set of instructions written in a programming language that tells the computer how to perform a specific task.

- Example: **Microsoft Word** is a program that allows users to create and edit documents.

### Process
A **Process** is an instance of a program that is being executed. When a program runs, the operating system creates a process to manage its execution.

- Example: When we open **Microsoft Word**, it becomes a process in the operating system.

### Thread
A **Thread** is the smallest unit of execution within a process. A process can have multiple threads, which share the same resources but can run independently.

- Example: A web browser like **Google Chrome** might use multiple threads for different tabs, with each tab running as a separate thread.

## Key Concepts

### Multitasking
**Multitasking** allows an operating system to run multiple processes simultaneously.
- On **single-core CPUs**, this is done through time-sharing, rapidly switching between tasks.
- On **multi-core CPUs**, true parallel execution occurs, with tasks distributed across cores.

**Example**: Browsing the internet while listening to music and downloading a file.

### Multithreading
**Multithreading** refers to the ability to execute multiple threads within a single process concurrently. It enhances the efficiency of multitasking by breaking down individual tasks into smaller sub-tasks or threads.

- Example: A web browser can use multithreading by having separate threads for rendering the page, running JavaScript, and managing user inputs.

#### In a Single-Core System:
- Both threads and processes are managed by the OS scheduler through **time slicing** and **context switching** to create the illusion of simultaneous execution.

#### In a Multi-Core System:
- Both threads and processes can run in true parallel on different cores, with the OS scheduler distributing tasks across the cores to optimize performance.

### Time Slicing
- **Definition**: Time slicing divides CPU time into small intervals called **time slices** or **quanta**.
- **Function**: The OS scheduler allocates these time slices to different processes and threads, ensuring each gets a fair share of CPU time.
- **Purpose**: Prevents any single process or thread from monopolizing the CPU, improving responsiveness and enabling concurrent execution.

### Context Switching
- **Definition**: Context switching is the process of saving the state of a currently running process or thread and loading the state of the next one to be executed.
- **Function**: When a process or thread’s time slice expires, the OS scheduler performs a context switch to move the CPU’s focus to another process or thread.
- **Purpose**: This allows multiple processes and threads to share the CPU, giving the appearance of simultaneous execution on a single-core CPU or improving parallelism on multi-core CPUs.

### Multithreading in Java
Java provides robust support for **multithreading**, allowing developers to create applications that can perform multiple tasks simultaneously, improving performance and responsiveness.

- In a **single-core environment**, Java’s multithreading is managed by the JVM and the OS, which switch between threads to give the illusion of concurrency.
- In a **multi-core environment**, Java’s multithreading can take full advantage of the available cores, allowing true parallel execution of threads.

### Key Java Classes for Multithreading:
- `java.lang.Thread`: A class that provides constructors and methods for creating and managing threads.
- `java.lang.Runnable`: An interface that should be implemented by any class whose instances are intended to be executed by a thread.

## Additional Resources
- [Multithreading in Java](https://engineeringdigest.medium.com/multithreading-in-java-39f34724bbf6)
- [Lambda Expressions in Java](https://www.programiz.com/java-programming/lambda-expression)
