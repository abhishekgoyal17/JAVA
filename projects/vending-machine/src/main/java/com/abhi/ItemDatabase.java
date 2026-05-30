package com.abhi;

import com.abhi.Factory.Item;

import java.util.HashMap;
import java.util.Map;

// Item Database
class ItemDatabase {
    private Map<String, Item> items = new HashMap<>();

    public void addItem(Item item) {
        items.put(item.getCode(), item);
    }

    public Item getItem(String code) {
        return items.get(code);
    }
}
