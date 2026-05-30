package Store;

import strategy.FileSerializationStrategy;
import strategy.InMemoryPersistenceStrategy;
import strategy.PersistenceStrategy;

public class KeyValueStoreFactory {

    public static KeyValueStore createStore(String type, String filePath) {
        PersistenceStrategy strategy = null;

        if ("file".equalsIgnoreCase(type)) {
            strategy = new FileSerializationStrategy();
        } else if ("memory".equalsIgnoreCase(type)) {
            strategy = new InMemoryPersistenceStrategy();
        }

        return PersistentKeyValueStore.getInstance(strategy, filePath);
    }
}

