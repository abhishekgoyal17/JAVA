# Executor Framework

The Executors framework was introduced in Java 5 as part of the java.util.concurrent package
to simplify the development of concurrent applications by abstracting away many of the complexities involved in creating and managing threads.

It will help in
- Avoiding Manual Thread management
- Resource management
- Scalability
- Thread reuse
- Error handling

Three core interfaces:
- Executor
- ExecutorService
- Scheduler Service

An Executor Framework in Java is part of the java.util.concurrent package and provides a high-level mechanism for managing threads and asynchronous task execution. Instead of manually managing thread creation, the framework allows developers to focus on task submission and management, making concurrency more manageable.

# Key Components:
Executor: The base interface with a single method execute(Runnable command). It allows task execution in a separate thread.
ExecutorService: A more advanced subinterface of Executor, which includes lifecycle management methods (e.g., shutting down the executor) and supports both Runnable and Callable tasks.
ScheduledExecutorService: A specialized version of ExecutorService that supports scheduling tasks to run after a delay or at regular intervals.
ThreadPoolExecutor: The most commonly used implementation that manages a pool of worker threads for concurrent task execution.
ForkJoinPool: A framework for parallelism, ideal for dividing tasks into smaller tasks (fork) and combining their results (join).
Important Methods:
execute(Runnable command): Executes a task asynchronously in a separate thread.

submit(Callable<T> task): Submits a Callable task for execution and returns a Future object that can be used to retrieve the result or check the task's completion status.

submit(Runnable task): Submits a Runnable task for execution and returns a Future representing the task.

invokeAll(Collection<? extends Callable<T>> tasks): Executes a collection of Callable tasks and returns a list of Future objects representing the tasks' results.

shutdown(): Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks are accepted.

shutdownNow(): Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the tasks that were awaiting execution.

awaitTermination(long timeout, TimeUnit unit): Blocks until all tasks are completed, the timeout occurs, or the executor is shut down.

schedule(Runnable command, long delay, TimeUnit unit) (in ScheduledExecutorService): Schedules a task to execute after a delay.

Benefits:
Thread Pool Management: Handles thread pool creation, reuse, and shutdown efficiently.
Task Lifecycle Management: Simplifies handling tasks' life cycles, such as submitting, executing, waiting for completion, and shutting down.
Concurrency Control: Provides built-in mechanisms to control task execution, scheduling, and parallelism.
By using the Executor Framework, developers can write highly concurrent programs more easily, improving resource management and performance.