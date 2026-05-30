package strategy;

import composite.File;

public interface StorageStrategy {
    void saveFile(File file);
    File loadFile(String fileName);
}