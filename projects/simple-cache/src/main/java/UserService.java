public class UserService {
    private final Cache<String, String> cache;

    public UserService(Cache<String, String> cache) {
        this.cache = cache;
    }

    // Method to get data from cache or fetch from database
    public String getUserData(String userId) {
        if (cache.containsKey(userId)) {
            return cache.get(userId); // Cache hit
        } else {
            // Cache miss: Fetch from the database and store in the cache
            String data = fetchFromDatabase(userId);
            cache.put(userId, data); // Store the fetched data in cache
            return data;
        }
    }

    // Method to put data into the cache
    public void cacheUserData(String userId, String userData) {
        cache.put(userId, userData);
    }

    // Method to evict data from the cache
    public void evictUserData(String userId) {
        cache.evict(userId);
    }

    // Method to check if data exists in the cache
    public boolean isUserDataInCache(String userId) {
        return cache.containsKey(userId);
    }

    // Method to fetch data from the "database"
    private String fetchFromDatabase(String userId) {
        // Simulating a database fetch
        return "User data for " + userId;
    }
}
