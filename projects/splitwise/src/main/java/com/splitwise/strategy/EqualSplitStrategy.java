package com.splitwise.strategy;

import com.splitwise.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqualSplitStrategy implements SplitStrategy {

    public Map<User, Double> splitExpense(double totalAmount, List<User> participants) {
        Map<User, Double> shares = new HashMap<>();
        double share = totalAmount / participants.size();
        for (User participant : participants) {
            shares.put(participant, share);
        }
        return shares;
    }
}
