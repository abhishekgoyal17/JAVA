package service;

import model.ValueWithTTL;
import util.CleanUpTask;

import java.util.concurrent.*;

public class KeyValueStore<K, V> {
    private final ConcurrentHashMap<K, ValueWithTTL<V>> store;
    private final ScheduledExecutorService scheduler;

    public KeyValueStore() {
        this.store = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        CleanUpTask<K, V> cleanupTask = new CleanUpTask<>(store);
        scheduler.scheduleAtFixedRate(cleanupTask, 1, 1, TimeUnit.SECONDS);
    }

    public void put(K key, V value, long ttl) {
        long expirationTime = ttl > 0 ? System.currentTimeMillis() + ttl : -1;
        store.put(key, new ValueWithTTL<>(value, expirationTime));
    }

    public V get(K key) {
        ValueWithTTL<V> valueWithTTL = store.get(key);
        if (valueWithTTL == null || valueWithTTL.isExpired()) {
            store.remove(key);
            return null;
        }
        return valueWithTTL.getValue();
    }

    public void delete(K key) {
        store.remove(key);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
