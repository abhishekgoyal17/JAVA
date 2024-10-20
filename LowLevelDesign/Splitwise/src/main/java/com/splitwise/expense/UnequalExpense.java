package com.splitwise.expense;

import com.splitwise.model.User;
import com.splitwise.strategy.UnequalSplitStrategy;

import java.util.List;

public class UnequalExpense extends Expense {
    public UnequalExpense(double totalAmount, List<User> participants) {
        super(totalAmount, participants, new UnequalSplitStrategy());
    }

    public void calculateShares() {
        shares = splitStrategy.splitExpense(totalAmount, participants);
        System.out.println("Unequal expense calculated: " + shares);
    }
}
