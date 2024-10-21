package com.abhi;

import com.abhi.Factory.ItemFactory;
import com.abhi.Observer.Display;
import com.abhi.PaymentStrategy.CoinPayment;

// Main class
public class Main {
    public static void main(String[] args) {
        Display display = new Display();
        VendingMachine vendingMachine = new VendingMachine(display);

        // Add items to the vending machine
        vendingMachine.addItem(ItemFactory.createItem("snack", "S1", 1.5f, 10));
        vendingMachine.addItem(ItemFactory.createItem("drink", "D1", 2.5f, 5));

        // Select item
        vendingMachine.selectItem("S1");

        // Set payment strategy (coin)
        vendingMachine.setPaymentStrategy(new CoinPayment());
        vendingMachine.insertCoin(1.0f);
        vendingMachine.insertCoin(0.5f);

        // Complete the transaction
        vendingMachine.completeTransaction("S1");

        // Cancel the transaction
        vendingMachine.cancelTransaction();
    }
}