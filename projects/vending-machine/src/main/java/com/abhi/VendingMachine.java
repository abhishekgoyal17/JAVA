package com.abhi;

import com.abhi.Factory.Item;
import com.abhi.Observer.Display;
import com.abhi.Observer.DisplayObserver;
import com.abhi.PaymentStrategy.CardPayment;
import com.abhi.PaymentStrategy.CoinPayment;
import com.abhi.PaymentStrategy.PaymentStrategy;
import com.abhi.activity.Dispenser;

// Vending Machine with State and Observer
class VendingMachine {
    private ItemDatabase itemDatabase;
    private PaymentStrategy paymentStrategy;
    private Dispenser dispenser;
    private Display display;
    private DisplayObserver displayObserver;

    public VendingMachine(DisplayObserver displayObserver) {
        itemDatabase = new ItemDatabase();
        dispenser = new Dispenser();
        this.displayObserver = displayObserver;
    }

    public void addItem(Item item) {
        itemDatabase.addItem(item);
    }

    public void selectItem(String itemCode) {
        Item item = itemDatabase.getItem(itemCode);
        if (item == null) {
            displayObserver.update("Invalid item code");
            return;
        }
        if (item.getQuantity() == 0) {
            displayObserver.update("Item out of stock");
            return;
        }
        displayObserver.update("Price: " + item.getPrice());
    }

    public void insertCoin(float coinValue) {
        if (paymentStrategy instanceof CoinPayment) {
            paymentStrategy.addPayment(coinValue);
            displayObserver.update("Balance: " + paymentStrategy.getBalance());
        } else {
            displayObserver.update("Invalid payment method.");
        }
    }

    public void insertCard(float amount) {
        if (paymentStrategy instanceof CardPayment) {
            paymentStrategy.addPayment(amount);
            displayObserver.update("Balance: " + paymentStrategy.getBalance());
        } else {
            displayObserver.update("Invalid payment method.");
        }
    }

    public void cancelTransaction() {
        paymentStrategy.resetBalance();
        displayObserver.update("Transaction canceled");
    }

    public void completeTransaction(String itemCode) {
        Item item = itemDatabase.getItem(itemCode);
        if (item == null) {
            displayObserver.update("Invalid item code");
            return;
        }
        if (item.getQuantity() == 0) {
            displayObserver.update("Item out of stock");
            return;
        }
        if (paymentStrategy.getBalance() < item.getPrice()) {
            displayObserver.update("Insufficient funds");
            return;
        }
        paymentStrategy.processPayment(item.getPrice());
        item.decrementQuantity();
        dispenser.dispenseItem(item);
        displayObserver.update("Transaction complete");
    }

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
}
