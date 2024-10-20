package com.splitwise.expense;

import com.splitwise.model.User;
import com.splitwise.strategy.SplitStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//factory pattern
public abstract class Expense {
    protected double totalAmount;
    protected List<User> participants;
    protected Map<User, Double> shares;
    protected SplitStrategy splitStrategy;

    public Expense(double totalAmount, List<User> participants, SplitStrategy splitStrategy) {
        this.totalAmount = totalAmount;
        this.participants = participants;
        this.splitStrategy = splitStrategy;
        shares = new HashMap<>();
    }
    public abstract void calculateShares();
}

