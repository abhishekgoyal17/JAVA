package com.abhi.Observer;




public class Display implements DisplayObserver {
    @Override
    public void update(String message) {
        System.out.println("Display: " + message);
    }

    public void displayPrice(float price) {
        System.out.println("Price: " + price);
    }

    public void displayBalance(float balance) {
        System.out.println("Balance: " + balance);
    }
}

