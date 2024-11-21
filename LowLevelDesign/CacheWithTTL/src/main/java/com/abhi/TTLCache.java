package com.abhi;

import java.util.*;

public class TTLCache {
    private final Map<String, CacheEntry> cache;

    public TTLCache() {
        this.cache = new HashMap<>();
    }

    // Add an entry to the cache with a TTL
    public void put(String key, String value, long ttlInMillis) {
        cache.put(key, new CacheEntry(value, ttlInMillis));
    }

    // Retrieve a value from the cache
    public String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null; // Key does not exist
        }
        if (entry.isExpired()) {
            cache.remove(key); // Remove expired entry
            return null;
        }
        return entry.getValue();
    }

    // Evict an entry manually
    public void evict(String key) {
        cache.remove(key);
    }

    // Clean up all expired entries
    public void cleanUp() {
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (String key : keysToRemove) {
            cache.remove(key);
        }
    }

    // Helper method to check if a key is in the cache and valid
    public boolean contains(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            return false;
        }
        return true;
    }
}

