## CountDownLatch in Java

### Overview
`CountDownLatch` is a synchronization aid that allows one or more threads to wait until a set of operations performed by other threads completes. It is part of the `java.util.concurrent` package.

### How it Works:
- A `CountDownLatch` is initialized with a given count.
- The `countDown()` method is called when a task is finished, which decreases the count by 1.
- The `await()` method is used by the threads that need to wait for the completion of other tasks. These threads remain blocked until the count reaches zero.

### Key Points:
- Once the count reaches zero, all waiting threads are released, and further calls to `await()` return immediately.
- The `CountDownLatch` cannot be reset once the count reaches zero, but for a similar use case where the latch can be reset, consider using `CyclicBarrier`.
- `CountDownLatch` is commonly used to ensure that certain tasks complete before moving on in an application.

### Constructor:

**CountDownLatch(int count)**
**count** : The number of times countDown() must be invoked before threads can proceed.
Key Methods:
- **void await()** throws InterruptedException: Causes the current thread to wait until the latch has counted down to zero.
- **void countDown()**: Decrements the count of the latch, releasing waiting threads when the count reaches zero.
- **long getCount()**: Returns the current count (useful for debugging and monitoring).

### Explanation of Example:
- **CountDownLatch** is initialized with a count of 3.
- Three worker threads are created, each simulating work by calling `Thread.sleep()`.
- Each worker thread calls `countDown()` when it finishes, decreasing the latch count.
- The main thread calls `await()` to wait until the latch count reaches zero. Once all workers are done, the main thread proceeds.

### Real-Life Use Cases:
- **Starting Services**: A `CountDownLatch` can be used to ensure that multiple services are started before accepting any requests.
- **Parallel Processing**: A task is divided among multiple threads, and the main thread waits for all threads to finish before processing the result.
- **Testing**: Used in testing environments to ensure that certain conditions are met before proceeding with further tests.

### Advantages:
- **Thread Coordination**: Simplifies thread coordination by allowing one or more threads to wait for other threads.
- **Task Synchronization**: Useful for task synchronization where one task should not proceed until others complete.
