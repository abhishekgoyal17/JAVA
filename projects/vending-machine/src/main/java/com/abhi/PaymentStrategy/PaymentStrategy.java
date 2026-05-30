package com.abhi.PaymentStrategy;

public interface PaymentStrategy {
    void addPayment(float amount);
    float getBalance();
    void resetBalance();
    void processPayment(float amount);
}
