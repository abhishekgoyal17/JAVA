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

