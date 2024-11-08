package singleton;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Board class (Singleton)
public class Board {
    private static Board instance;
    private final int size;
    private final Map<Integer, Integer> snakes;
    private final Map<Integer, Integer> ladders;

    private Board(int size) {
        this.size = size;
        this.snakes = new ConcurrentHashMap<>();
        this.ladders = new ConcurrentHashMap<>();
    }

    public static synchronized Board getInstance(int size) {
        if (instance == null) {
            instance = new Board(size);
        }
        return instance;
    }

    public int getSize() {
        return size;
    }

    public Map<Integer, Integer> getSnakes() {
        return snakes;
    }

    public Map<Integer, Integer> getLadders() {
        return ladders;
    }

    public void addSnake(int start, int end) {
        snakes.put(start, end);
    }

    public void addLadder(int start, int end) {
        ladders.put(start, end);
    }
}