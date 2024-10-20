package com.splitwise.expense;

import com.splitwise.model.User;
import com.splitwise.strategy.EqualSplitStrategy;


import java.util.List;


public class EqualExpense extends Expense {
    public EqualExpense(double totalAmount, List<User> participants) {
        super(totalAmount, participants, new EqualSplitStrategy());
    }

    public void calculateShares() {
        shares = splitStrategy.splitExpense(totalAmount, participants);
        System.out.println("Equal expense calculated: " + shares);
    }
}


