package com.abhi.Factory;

// Factory for Item creation
public class ItemFactory {
    public static Item createItem(String type, String code, float price, int quantity) {
        if (type.equalsIgnoreCase("snack")) {
            return new SnackItem(code, price, quantity);
        } else if (type.equalsIgnoreCase("drink")) {
            return new DrinkItem(code, price, quantity);
        } else {
            throw new IllegalArgumentException("Invalid item type");
        }
    }
}
