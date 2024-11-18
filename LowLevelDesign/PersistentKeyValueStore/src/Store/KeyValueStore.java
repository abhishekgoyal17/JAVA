package Store;

public interface KeyValueStore {
    void put(String key, String value);   // Store a key-value pair
    String get(String key);               // Retrieve value for a given key
    void remove(String key);              // Remove a key-value pair
    boolean containsKey(String key);      // Check if key exists
    void close();                         // Close the store (flush data if needed)
}

