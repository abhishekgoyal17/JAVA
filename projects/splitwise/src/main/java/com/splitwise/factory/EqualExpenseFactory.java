package com.splitwise.factory;

import com.splitwise.expense.EqualExpense;
import com.splitwise.expense.Expense;
import com.splitwise.model.User;

import java.util.List;

public class EqualExpenseFactory implements ExpenseFactory {
    @Override
    public Expense createExpense(double totalAmount, List<User> participants) {
        return new EqualExpense(totalAmount, participants);
    }
}
