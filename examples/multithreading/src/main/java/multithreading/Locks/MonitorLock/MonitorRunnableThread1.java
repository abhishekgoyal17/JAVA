package multithreading.Locks.MonitorLock;

public class MonitorRunnableThread1 implements  Runnable {

    MonitorLockExample obj;

    MonitorRunnableThread1(MonitorLockExample obj){
        this.obj=obj;
    }

    @Override
    public void run(){
         obj.task1();;
    }
}
