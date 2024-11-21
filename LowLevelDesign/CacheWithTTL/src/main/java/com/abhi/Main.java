package com.abhi;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TTLCache cache = new TTLCache();

        // Add entries with TTLs
        cache.put("key1", "value1", 2000); // 2 seconds
        cache.put("key2", "value2", 4000); // 4 seconds

        System.out.println("Initial cache:");
        System.out.println("key1: " + cache.get("key1")); // value1
        System.out.println("key2: " + cache.get("key2")); // value2

        // Wait for 3 seconds
        Thread.sleep(3000);

        System.out.println("\nAfter 3 seconds:");
        System.out.println("key1: " + cache.get("key1")); // null (expired)
        System.out.println("key2: " + cache.get("key2")); // value2

        // Clean up expired entries
        cache.cleanUp();

        System.out.println("\nAfter cleanup:");
        System.out.println("key1 exists: " + cache.contains("key1")); // false
        System.out.println("key2 exists: " + cache.contains("key2")); // true

        // Wait for 2 more seconds
        Thread.sleep(2000);

        System.out.println("\nAfter 5 seconds:");
        System.out.println("key2: " + cache.get("key2")); // null (expired)
    }
}
