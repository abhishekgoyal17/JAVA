package com.splitwise.observer;

public class ExpenseObserver implements Observe {

    public void update(){
        System.out.println("an expense added or modified");
    }
}
