package com.abhi.activity;

import com.abhi.PaymentStrategy.PaymentStrategy;

// Coin Acceptor
class CoinAcceptor {
    public void acceptCoin(float coinValue, PaymentStrategy paymentStrategy) {
        paymentStrategy.addPayment(coinValue);
    }
}

