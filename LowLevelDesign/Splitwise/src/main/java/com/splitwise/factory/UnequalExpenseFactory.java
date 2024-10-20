package com.splitwise.factory;

import com.splitwise.expense.Expense;
import com.splitwise.expense.UnequalExpense;
import com.splitwise.model.User;

import java.util.List;

public class UnequalExpenseFactory implements ExpenseFactory {
    @Override
    public Expense createExpense(double totalAmount, List<User> participants) {
        return new UnequalExpense(totalAmount, participants);
    }
}