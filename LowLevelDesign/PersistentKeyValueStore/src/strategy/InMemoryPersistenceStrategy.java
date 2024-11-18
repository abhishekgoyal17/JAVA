package strategy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InMemoryPersistenceStrategy implements PersistenceStrategy {



    @Override
    public Map<String, String> load(File file) {
        return new HashMap<>(); // No data from file, just empty map
    }

    @Override
    public void save(Map<String, String> store, File file) throws IOException {

    }
}

