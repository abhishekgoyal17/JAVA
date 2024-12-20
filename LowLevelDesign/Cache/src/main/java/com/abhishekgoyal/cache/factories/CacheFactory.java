package com.abhishekgoyal.cache.factories;

import com.abhishekgoyal.cache.Cache;
import com.abhishekgoyal.cache.policies.LRUEvictionPolicy;
import com.abhishekgoyal.cache.storage.HashMapBasedStorage;

public class CacheFactory<Key, Value> {

    public Cache<Key, Value> defaultCache(final int capacity) {
        return new Cache<Key, Value>(new LRUEvictionPolicy<Key>(),
                new HashMapBasedStorage<Key, Value>(capacity));
    }
}
