package Multithreading.multithreading.Daemon;

public class MyThread5 extends Thread {
    @Override
    public void run() {
        int i=100;
       while(i!=0){
           System.out.println("Hello world");
           i--;
       }
    }

    public static void main(String[] args) {

        MyThread5 t1= new MyThread5();
        MyThread5 t2= new MyThread5();
        t1.setDaemon(true);
        t1.start();
        t2.start();
        System.out.println("main done");
    }
}

//DAEMON THREADS
//jvm doesnot wait for daemon

// daemons example: Logging, Garbage collector
