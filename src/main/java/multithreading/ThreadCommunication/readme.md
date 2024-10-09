The process of testing a condition repeatedly till it becomes true is known as polling. Polling is usually implemented with the help of loops to check whether a particular condition is true or not. If it is true, a certain action is taken. This wastes many CPU cycles and makes the implementation inefficient.

For example, in a classic queuing problem where one thread is producing data, and the other is consuming it.

# How Java Multi-Threading tackles this problem?
To avoid polling, Java uses three methods, namely, wait(), notify(), and notifyAll(). All these methods belong to object class as final so that all classes have them. They must be used within a synchronized block only.

- wait(): It tells the calling thread to give up the lock and go to sleep until some other thread enters the same monitor and calls notify().
- notify(): It wakes up one single thread called wait() on the same object. It should be noted that calling notify() does not give up a lock on a resource.
- notifyAll(): It wakes up all the threads called wait() on the same object.


# Thread safety
It is a programming concept that ensures that a function can be accessed by multiple threads simultaneously without causing unexpected behavior or data corruption.
Here are some ways to achieve thread safety:
Avoid sharing data: When sharing is not intended, give each thread its own copy of the data.
Use synchronization: When sharing is important, use synchronization to ensure that the program behaves consistently.
Use locks: Use locks to protect shared resources from concurrent access.
Encapsulate global data: Maintain global data per thread or encapsulate it so that access can be serialized.
Use the volatile keyword: Use the volatile keyword with variables to ensure that every thread reads data from memory, not the thread cache.
Use thread-safe collection classes: Use thread-safe collection classes, such as ConcurrentHashMap. 
