package model;

public class ValueWithTTL<V> {
    private final V value;
    private final long expirationTime;

    public ValueWithTTL(V value, long expirationTime) {
        this.value = value;
        this.expirationTime = expirationTime;
    }

    public V getValue() {
        return value;
    }

    public boolean isExpired() {
        return expirationTime != -1 && System.currentTimeMillis() > expirationTime;
    }
}
