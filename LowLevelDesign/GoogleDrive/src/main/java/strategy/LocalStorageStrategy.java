package strategy;

import composite.File;

public class LocalStorageStrategy implements StorageStrategy {
    @Override
    public void saveFile(File file) {
        // Implement local storage saving logic
        System.out.println("Saving file locally: " + file.getName());
    }

    @Override
    public File loadFile(String fileName) {
        // Implement local storage loading logic
        System.out.println("Loading file locally: " + fileName);
        return new File(fileName);
    }
}