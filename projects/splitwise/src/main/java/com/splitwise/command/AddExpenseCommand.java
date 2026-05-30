package com.splitwise.command;

import com.splitwise.expense.Expense;

public class AddExpenseCommand {
    private Expense expense;

    public AddExpenseCommand(Expense expense) {
        this.expense = expense;
    }

    public void execute() {
        expense.calculateShares();
    }

}
