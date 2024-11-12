package com.abhishekgoyal.cache.storage;

import com.abhishekgoyal.cache.exceptions.NotFoundException;
import com.abhishekgoyal.cache.exceptions.StorageFullException;

public interface Storage<Key, Value> {
    public void add(Key key, Value value) throws StorageFullException;

    void remove(Key key) throws NotFoundException;

    Value get(Key key) throws NotFoundException;
}
