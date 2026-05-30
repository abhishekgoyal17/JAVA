package util;

import model.ValueWithTTL;

import java.util.concurrent.ConcurrentHashMap;

public class CleanUpTask<K, V> implements Runnable {
    private final ConcurrentHashMap<K, ValueWithTTL<V>> store;



    public CleanUpTask(ConcurrentHashMap<K, ValueWithTTL<V>> store) {
        this.store = store;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        store.forEach((key, valueWithTTL) -> {
            if (valueWithTTL.isExpired()) {
                store.remove(key);
            }
        });
    }
}
