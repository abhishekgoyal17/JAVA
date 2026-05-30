package multithreading.Locks.Example;

import multithreading.Locks.MonitorLock.MonitorLockExample;

public class ExampleMain {

    public static void main(String[] args) {

        SharedResource obj= new SharedResource();

        Thread producerThread= new Thread(new Producer(obj));
        Thread consumerThread= new Thread(new Consumer(obj));
        //Thread t1= new Thread(()-> {obj.addItem();});
        //Thread t2= new Thread(()-> {obj.consumeItem();}); //lambda expressions
        //t1.start();
        //t2.start();
        producerThread.start();
        consumerThread.start();


    }
}
