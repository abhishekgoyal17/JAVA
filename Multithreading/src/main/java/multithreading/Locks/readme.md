Locks
The synchronized keyword in Java provides basic thread-safety but has limitations:
it locks the entire method or block, leading to potential performance issues. 
It lacks a try-lock mechanism, causing threads to block indefinitely,
increasing the risk of deadlocks. Additionally, synchronized doesn't
support multiple condition variables, offering only a single monitor per object with basic
wait/notify mechanisms. In contrast, explicit locks (Lock interface) offer finer-grained control,
try-lock capabilities to avoid blocking, and more sophisticated thread coordination through
multiple condition variables, making them more flexible and powerful for complex concurrency
scenarios.

Demerit of synchronized(implicit lock) over explicit Lock
- Fairness is not maintained in case of synchronized.
- Indefinite blocking in synchronized
- Interruptibilty is also not possible in synchronized
- cannot differentiate between read and write

if an interruptExeption or ThreadDeath error is not handled properly,the information that the thread was interuppted will be lost.
Handling this exception means either to re-throw it or manually re-interrupt the current thread by calling Thread.interrupt()
--Potential Impact: Failing to interrupt the thread(or to rethrow) risks delaying the thread shutdown and losing the information that the thread was
interuppted probably without finishing its task


Reentrant Lock
A Reentrant Lock in Java is a type of lock that allows a thread to acquire
the same lock multiple times without causing a deadlock. 
If a thread already holds the lock, it can re-enter the lock without being blocked. 
This is useful when a thread needs to repeatedly enter synchronized blocks or methods
within the same execution flow. The ReentrantLock class from the java.util.concurrent.
locks package provides this functionality, offering more flexibility than the synchronized 
keyword, including try-locking, timed locking, and multiple condition variables for advanced
thread coordination.


** Fairness in the context of locks refers to the order in which 
threads acquire a lock. A fair lock ensures that threads acquire 
the lock in the order they requested it, preventing thread starvation.
With a fair lock, if multiple threads are waiting, the longest-waiting 
thread is granted the lock next. However, fairness can lead to lower throughput 
due to the overhead of maintaining the order. Non-fair locks, in contrast, allow 
threads to “cut in line,” potentially offering better performance but at the risk 
of some threads waiting indefinitely if others frequently acquire the lock.

Read Write Lock
A Read-Write Lock is a concurrency control mechanism that allows multiple threads
to read shared data simultaneously while restricting write access to a single thread 
at a time. This lock type, provided by the ReentrantReadWriteLock class in Java, 
optimizes performance in scenarios with frequent read operations and infrequent writes.
Multiple readers can acquire the read lock without blocking each other, but when a thread 
needs to write, it must acquire the write lock, ensuring exclusive access. This prevents 
data inconsistency while improving read efficiency compared to traditional locks, which block 
all access during write operations.