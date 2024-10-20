package com.splitwise.strategy;

import com.splitwise.model.User;

import java.util.List;
import java.util.Map;

public interface SplitStrategy {
    Map<User,Double> splitExpense(double totalAmount, List<User> participants);
}
