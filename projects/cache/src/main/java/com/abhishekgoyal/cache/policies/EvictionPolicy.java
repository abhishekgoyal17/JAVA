package com.abhishekgoyal.cache.policies;

public interface EvictionPolicy<Key> {

    void keyAccessed(Key key);

    /**
     * Evict key from eviction policy and return it.
     */
    Key evictKey();
}
