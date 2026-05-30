package strategy;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileSerializationStrategy implements PersistenceStrategy {

    @Override
    public void save(Map<String, String> store, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(store);
        }
    }

    @Override
    public Map<String, String> load(File file) throws IOException, ClassNotFoundException {
        if (!file.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, String>) ois.readObject();
        }
    }
}

