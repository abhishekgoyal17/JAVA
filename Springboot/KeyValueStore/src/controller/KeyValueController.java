package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.KeyValueStore;

@RestController
@RequestMapping("/api/kvstore")
public class KeyValueController {
    private final KeyValueStore<String, String> keyValueStore;

    @Autowired
    public KeyValueController() {
        this.keyValueStore = new KeyValueStore<>();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addKeyValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false, defaultValue = "-1") long ttl) {
        keyValueStore.put(key, value, ttl);
        return ResponseEntity.ok("Key-Value pair added successfully");
    }

    @GetMapping("/get")
    public ResponseEntity<String> getValue(@RequestParam String key) {
        String value = keyValueStore.get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteKey(@RequestParam String key) {
        keyValueStore.delete(key);
        return ResponseEntity.ok("Key deleted successfully");
    }

    @PostMapping("/shutdown")
    public ResponseEntity<String> shutdownStore() {
        keyValueStore.shutdown();
        return ResponseEntity.ok("KeyValueStore shutdown complete");
    }
}
