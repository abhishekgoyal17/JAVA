package strategy;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface PersistenceStrategy {
    void save(Map<String, String> store, File file) throws IOException;
    Map<String, String> load(File file) throws IOException, ClassNotFoundException;
}
