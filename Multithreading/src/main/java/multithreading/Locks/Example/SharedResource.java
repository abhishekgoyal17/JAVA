package multithreading.Locks.Example;

import org.w3c.dom.ls.LSOutput;

import java.sql.SQLOutput;

public class SharedResource {

    boolean itemAvailable = false;

    public synchronized void addItem() {
        itemAvailable = true;
        System.out.println("item added by" + Thread.currentThread().getName() + "and involing all the threads whcih are waiting");
        notifyAll();
    }

    public synchronized void consumeItem() {
        System.out.println("Consume item method invoked" + Thread.currentThread().getName());

        while (!itemAvailable) {
            try {
                System.out.println("Thread" + Thread.currentThread().getName() + "is waiting now");

                wait();
            } catch (Exception e) {
            }
        }
        System.out.println("Item consumed by"+ Thread.currentThread().getName());
    }

}

