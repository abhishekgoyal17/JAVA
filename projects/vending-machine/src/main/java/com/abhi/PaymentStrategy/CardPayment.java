package com.abhi.PaymentStrategy;

public class CardPayment implements PaymentStrategy {
    private float balance = 0;

    @Override
    public void addPayment(float amount) {
        balance += amount;
    }

    @Override
    public float getBalance() {
        return balance;
    }

    @Override
    public void resetBalance() {
        balance = 0;
    }

    @Override
    public void processPayment(float amount) {
        balance -= amount;
    }
}
