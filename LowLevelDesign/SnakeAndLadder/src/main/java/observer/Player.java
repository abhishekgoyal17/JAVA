package observer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Player class implementing Observer
public class Player implements Observer {
    private final String name;
    private int position;
    private final Lock positionLock;

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.positionLock = new ReentrantLock();
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        positionLock.lock();
        try {
            return position;
        } finally {
            positionLock.unlock();
        }
    }

    public void setPosition(int position) {
        positionLock.lock();
        try {
            this.position = position;
        } finally {
            positionLock.unlock();
        }
    }

    @Override
    public void update(String message) {
        System.out.println(name + ": " + message);
    }
}
