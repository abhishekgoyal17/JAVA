package com.splitwise.factory;

import com.splitwise.expense.Expense;
import com.splitwise.model.User;

import java.util.List;


public interface ExpenseFactory {

    Expense createExpense(double totalAmount, List<User> participants);
}
