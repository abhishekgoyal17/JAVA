import Store.KeyValueStore;
import Store.KeyValueStoreFactory;

public class KeyValueStoreApp {
    public static void main(String[] args) {
        // Use file-based persistence
        KeyValueStore kvStore = KeyValueStoreFactory.createStore("file", "dataStore.txt");

        kvStore.put("username", "john_doe");
        kvStore.put("email", "john.doe@example.com");

        System.out.println("Username: " + kvStore.get("username"));
        System.out.println("Email: " + kvStore.get("email"));

        kvStore.remove("email");

        System.out.println("Contains email: " + kvStore.containsKey("email"));

        kvStore.close();
    }
}
