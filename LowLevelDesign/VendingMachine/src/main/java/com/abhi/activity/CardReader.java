package com.abhi.activity;

import com.abhi.PaymentStrategy.PaymentStrategy;

class CardReader {
    public void readCard(float amount, PaymentStrategy paymentStrategy) {
        paymentStrategy.addPayment(amount);
    }
}
