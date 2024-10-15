package multithreading.Locks.MonitorLock;

public class Main {
    public static void main(String[] args) {
        MonitorLockExample obj= new MonitorLockExample();

        MonitorRunnableThread1 runnableObj= new MonitorRunnableThread1(obj);
        Thread t1= new Thread(runnableObj);


        //Thread t1= new Thread(()-> {obj.task1();});
        Thread t2= new Thread(()-> {obj.task2();});
        Thread t3= new Thread(()-> {obj.task3();});

        t1.start();
        t2.start();
        t3.start();
    }


}
