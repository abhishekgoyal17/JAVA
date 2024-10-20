package com.splitwise.strategy;

import com.splitwise.model.User;

import java.util.List;
import java.util.Map;

public class UnequalSplitStrategy implements SplitStrategy {

    public Map<User, Double> splitExpense(double totalAmount, List<User> participants) {
        // Custom logic for unequal split (Here, we'll just use equal splitting for simplicity)
        return new EqualSplitStrategy().splitExpense(totalAmount, participants);
    }
}
