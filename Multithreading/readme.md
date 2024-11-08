# Multithreading in Java

## Introduction
Multithreading in Java is a powerful feature that allows the concurrent execution of two or more threads. Each thread runs independently, allowing the program to perform multiple tasks simultaneously, which is particularly useful for resource-intensive operations.

## Why Use Multithreading?
Multithreading helps in making the most efficient use of system resources. Key benefits include:

- **Responsiveness**: Keeps the program responsive, even if one part is waiting for resources.
- **Resource Sharing**: Allows threads to share resources without requiring external communication.
- **Utilizing Multicore Processors**: It takes advantage of multi-core CPUs, allowing parallel execution.
- **Cost-Effective**: Creating new threads is less expensive in terms of memory than creating new processes.

## Key Concepts of Multithreading

### 1. Thread Lifecycle
Java threads have a lifecycle that includes the following states:

- **New**: A thread is created but not yet started.
- **Runnable**: The thread is ready to run and waiting for the CPU to allocate time.
- **Blocked**: The thread is waiting to acquire a lock or resource.
- **Waiting**: The thread is waiting indefinitely until another thread performs a specific action.
- **Timed Waiting**: The thread waits for a specified time before transitioning back to a runnable state.
- **Terminated**: The thread has completed its execution.

### 2. Creating Threads
Java offers two primary methods for creating threads:

- **Extending `Thread` Class**: Useful for simple threading needs but not ideal for shared resource management.
- **Implementing `Runnable` Interface**: Preferred for cases where the task needs to be shared among multiple threads.

### 3. Synchronization
To avoid data inconsistency due to concurrent access, Java provides synchronization mechanisms:

- **Synchronized Methods**: Locks the entire method for a thread.
- **Synchronized Blocks**: Locks only the critical section of code.
- **Lock Interface**: Offers more control over thread synchronization compared to `synchronized`.

### 4. Inter-thread Communication
Java supports inter-thread communication through methods like `wait()`, `notify()`, and `notifyAll()` to coordinate actions between threads. This allows threads to communicate when specific conditions are met, avoiding busy-waiting.

### 5. Thread Pools
Instead of creating new threads for each task, a thread pool manages a set of reusable threads:

- **Executor Framework**: Java's `ExecutorService` provides a more manageable way to execute tasks concurrently.
- **Fixed Thread Pool**: A fixed number of threads are reused for executing tasks.
- **Cached Thread Pool**: Creates new threads as needed but reuses previously created threads when available.

### 6. Concurrency Utilities
Java provides a set of concurrency utilities in the `java.util.concurrent` package:

- **Concurrent Collections**: These collections like `ConcurrentHashMap` are thread-safe and provide high performance.
- **CountDownLatch**: Allows a thread to wait until a set of operations being performed by other threads completes.
- **CyclicBarrier**: A synchronization aid that allows a set of threads to all wait for each other to reach a common barrier point.

## Best Practices for Multithreading

- **Minimize the Use of `synchronized`**: Overuse can lead to performance bottlenecks.
- **Use Concurrency Utilities**: Prefer classes in the `java.util.concurrent` package for higher-level thread management.
- **Handle Exceptions**: Always handle exceptions in threads to avoid unexpected terminations.
- **Avoid Using `Thread.stop()`**: It’s deprecated and can lead to resource inconsistencies. Instead, use interruption.
- **Keep Threads Short-Lived**: Threads should perform their task and finish quickly to free up system resources.

## Conclusion
Multithreading in Java is an essential concept for building efficient, high-performance applications. By leveraging Java’s threading and concurrency features, you can create robust applications that take full advantage of modern CPU architectures.

## References
- [Article on Multithreading](https://engineeringdigest.medium.com/multithreading-in-java-39f34724bbf6)
-  [Inteview QUestions](https://medium.com/@chandantechie/tricky-java-multithreading-interview-question-and-answer-with-examples-79e420ab4a46)
## Shrayansh- jain notes
- [Part 1](https://notebook.zohopublic.in/public/notes/74tdo52a4834de5554f09bc9ec3f11572cd11)
- [Part 2](https://notebook.zohopublic.in/public/notes/74tdo47dcfcf05e644cf192c411a6d78ec1dc)
- [Thread Joining, Daemon Thread, Thread Priority](https://notebook.zohopublic.in/public/notes/74tdob1b1f9bf584f44228974656f4d93519d)
- [Lock and Condition](https://notebook.zohopublic.in/public/notes/74tdo36e9543c1b8941ea9099d9f04b190404)
- [Lock free concurrency](https://notebook.zohopublic.in/public/notes/74tdof6d5236d9b234442a814277c786bc6a2)
- [Threapool and Executor](https://notebook.zohopublic.in/public/notes/74tdo0e297bb7d6dd4d45a837d13f60fedc3f)
- [Future, CompletableFuture and Callable](https://notebook.zohopublic.in/public/notes/74tdo4a0bd722efc748d0909fbb498fa21b18)
- [Java : ScheduledThreadPoolExecutor](https://notebook.zohopublic.in/public/notes/74tdo985cdf57a9bc40bdb37a4468a09b9554)
- [Java - Fork/Join Pool, Single, Fixed, CachedPool](https://notebook.zohopublic.in/public/notes/74tdo77c24fe4d4df41c582e9041230f2cd6e)
- [Java: VirtualThreads and ThreadLoca](https://notebook.zohopublic.in/public/notes/74tdo3074d38916474ae096fe57ad9f2ff54b)

