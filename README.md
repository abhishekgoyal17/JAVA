# Multithreading-in-java

Program

A program is a set of instructions written in a programming language that tells the computer how to perform a specific task

Microsoft Word is a program that allows users to create and edit documents.

Process

A process is an instance of a program that is being executed. When a program runs, the operating system creates a process to manage its execution.

When we open Microsoft Word, it becomes a process in the operating system.

Thread

A thread is the smallest unit of execution within a process. A process can have multiple threads, which share the same resources but can run independently.

A web browser like Google Chrome might use multiple threads for different tabs, with each tab running as a separate thread.

Multitasking

Multitasking allows an operating system to run multiple processes simultaneously. On single-core CPUs, this is done through time-sharing, rapidly switching between tasks. On multi-core CPUs, true parallel execution occurs, with tasks distributed across cores. The OS scheduler balances the load, ensuring efficient and responsive system performance.

Example: We are browsing the internet while listening to music and downloading a file.

Multitasking utilizes the capabilities of a CPU and its cores. When an operating system performs multitasking, it can assign different tasks to different cores. This is more efficient than assigning all tasks to a single core.

Multithreading

Multithreading refers to the ability to execute multiple threads within a single process concurrently.

A web browser can use multithreading by having separate threads for rendering the page, running JavaScript, and managing user inputs. This makes the browser more responsive and efficient.

Multithreading enhances the efficiency of multitasking by breaking down individual tasks into smaller sub-tasks or threads. These threads can be processed simultaneously, making better use of the CPU’s capabilities.

In a single-core system:

Both threads and processes are managed by the OS scheduler through time slicing and context switching to create the illusion of simultaneous execution.

In a multi-core system:

Both threads and processes can run in true parallel on different cores, with the OS scheduler distributing tasks across the cores to optimize performance.

Time Slicing

Definition: Time slicing divides CPU time into small intervals called time slices or quanta.
Function: The OS scheduler allocates these time slices to different processes and threads, ensuring each gets a fair share of CPU time.
Purpose: This prevents any single process or thread from monopolizing the CPU, improving responsiveness and enabling concurrent execution.
Context Switching

Definition: Context switching is the process of saving the state of a currently running process or thread and loading the state of the next one to be executed.
Function: When a process or thread’s time slice expires, the OS scheduler performs a context switch to move the CPU’s focus to another process or thread.
Purpose: This allows multiple processes and threads to share the CPU, giving the appearance of simultaneous execution on a single-core CPU or improving parallelism on multi-core CPUs.
Multitasking can be achieved through multithreading where each task is divided into threads that are managed concurrently.

While multitasking typically refers to the running of multiple applications, multithreading is more granular, dealing with multiple threads within the same application or process.

Multithreading in Java
Java provides robust support for multithreading, allowing developers to create applications that can perform multiple tasks simultaneously, improving performance and responsiveness.

In Java, multithreading is the concurrent execution of two or more threads to maximize the utilization of the CPU. Java’s multithreading capabilities are part of the java.lang package, making it easy to implement concurrent execution.

In a single-core environment, Java’s multithreading is managed by the JVM and the OS, which switch between threads to give the illusion of concurrency.

The threads share the single core, and time-slicing is used to manage thread execution.

In a multi-core environment, Java’s multithreading can take full advantage of the available cores.

The JVM can distribute threads across multiple cores, allowing true parallel execution of threads.

A thread is a lightweight process, the smallest unit of processing. Java supports multithreading through its java.lang.Thread class and the java.lang.Runnable interface
