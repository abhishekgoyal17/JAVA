package com.splitwise.facade;

import com.splitwise.command.AddExpenseCommand;
import com.splitwise.expense.Expense;
import com.splitwise.factory.EqualExpenseFactory;
import com.splitwise.factory.ExpenseFactory;
import com.splitwise.factory.UnequalExpenseFactory;
import com.splitwise.manager.UserManager;
import com.splitwise.model.User;
import com.splitwise.observer.ExpenseObserver;
import com.splitwise.observer.Observe;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class SplitwiseFacade {
    private UserManager userManager;
    private List<Expense> expenses;
    private List<Observe> observers;

    public SplitwiseFacade() {
        userManager = UserManager.getInstance();
        expenses = new ArrayList<>();
        observers = new ArrayList<>();
    }

    public void addObserver(ExpenseObserver observer) {
        observers.add(observer);
    }

    public void addUser(User user) {
        userManager.addUser(user);
    }

    public void addEqualExpense(double totalAmount, List<User> participants) {
        ExpenseFactory factory = new EqualExpenseFactory();
        Expense expense = factory.createExpense(totalAmount, participants);
        expenses.add(expense);

        AddExpenseCommand command = new AddExpenseCommand(expense);
        command.execute(); // Calculate the shares when an expense is added

        notifyObservers(); // Notify observers when an expense is added
    }

    public void addUnequalExpense(double totalAmount, List<User> participants) {
        ExpenseFactory factory = new UnequalExpenseFactory();
        Expense expense = factory.createExpense(totalAmount, participants);
        expenses.add(expense);

        AddExpenseCommand command = new AddExpenseCommand(expense);
        command.execute(); // Calculate the shares when an expense is added

        notifyObservers(); // Notify observers when an expense is added
    }

    private void notifyObservers() {
        for (Observe observer : observers) {
            observer.update();
        }
    }
}

