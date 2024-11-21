public class Main {
    public static void main(String[] args) {
        // Initialize Cache and UserService
        Cache<String, String> cache = new InMemoryCache<>();
        UserService userService = new UserService(cache);

        // Fetch user data (this will populate the cache)
        System.out.println(userService.getUserData("user1")); // Cache miss, fetches from DB
        System.out.println(userService.getUserData("user1")); // Cache hit, fetches from cache

        // Cache additional user data
        userService.cacheUserData("user2", "User data for user2");
        System.out.println(userService.getUserData("user2")); // Fetches from cache

        // Evict a user's data from the cache
        userService.evictUserData("user1");
        System.out.println(userService.getUserData("user1")); // Cache miss, fetches from DB again

        // Check if data exists in cache
        System.out.println("Is user1 in cache? " + userService.isUserDataInCache("user1"));
        System.out.println("Is user4 in cache? " + userService.isUserDataInCache("user4"));

        // Should be false
    }
}
