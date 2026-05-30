package com.abhi;

public class CacheEntry {
    private final String value;
    private final long expiryTime;

    public CacheEntry(String value, long ttlInMillis) {
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + ttlInMillis;
    }

    public String getValue() {
        return value;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
