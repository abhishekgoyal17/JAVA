package singleton;

import composite.Folder;
import strategy.LocalStorageStrategy;
import strategy.StorageStrategy;

// FileSystemManager.java
public class FileSystemManager {
    private static FileSystemManager instance;
    private Folder root;
    private StorageStrategy storageStrategy;

    private FileSystemManager() {
        root = new Folder("root");
        storageStrategy = new LocalStorageStrategy(); // Default Strategy
    }

    public static synchronized FileSystemManager getInstance() {
        if (instance == null) {
            instance = new FileSystemManager();
        }
        return instance;
    }

    public Folder getRoot() {
        return root;
    }

    public void setStorageStrategy(StorageStrategy strategy) {
        this.storageStrategy = strategy;
    }

    public StorageStrategy getStorageStrategy() {
        return storageStrategy;
    }
}
