package com.abhi.Factory;

// Factory Method for Item creation
public abstract class Item {
    private String code;
    private float price;
    private int quantity;

    public Item(String code, float price, int quantity) {
        this.code = code;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void decrementQuantity() {
        quantity--;
    }
}
