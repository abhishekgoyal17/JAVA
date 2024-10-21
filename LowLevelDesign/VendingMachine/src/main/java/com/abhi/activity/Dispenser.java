package com.abhi.activity;

import com.abhi.Factory.Item;

// Dispenser class
public class Dispenser {
    public void dispenseItem(Item item) {
        System.out.println("Dispensing item: " + item.getCode());
    }
}
