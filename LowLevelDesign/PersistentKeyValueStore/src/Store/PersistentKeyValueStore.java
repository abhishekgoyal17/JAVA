package Store;

import strategy.PersistenceStrategy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PersistentKeyValueStore implements KeyValueStore {

    private final Map<String, String> store;
    private final PersistenceStrategy persistenceStrategy;
    private final File storageFile;
    private static PersistentKeyValueStore instance;

    private PersistentKeyValueStore(PersistenceStrategy persistenceStrategy, String filePath) {
        this.store = new HashMap<>();
        this.persistenceStrategy = persistenceStrategy;
        this.storageFile = new File(filePath);
        loadData();
    }

    // Singleton pattern to ensure only one instance of PersistentKeyValueStore
    public static synchronized PersistentKeyValueStore getInstance(PersistenceStrategy persistenceStrategy, String filePath) {
        if (instance == null) {
            instance = new PersistentKeyValueStore(persistenceStrategy, filePath);
        }
        return instance;
    }

    @Override
    public void put(String key, String value) {
        store.put(key, value);
        saveData();
    }

    @Override
    public String get(String key) {
        return store.get(key);
    }

    @Override
    public void remove(String key) {
        store.remove(key);
        saveData();
    }

    @Override
    public boolean containsKey(String key) {
        return store.containsKey(key);
    }

    @Override
    public void close() {
        saveData();
    }

    private void loadData() {
        try {
            store.putAll(persistenceStrategy.load(storageFile));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            persistenceStrategy.save(store, storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
