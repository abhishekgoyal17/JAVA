package com.splitwise.app;

import com.splitwise.facade.SplitwiseFacade;
import com.splitwise.model.User;
import com.splitwise.observer.ExpenseObserver;

import java.util.Arrays;

public class app {
    public static void main(String[] args) {
        SplitwiseFacade splitwise = new SplitwiseFacade();

        User user1 = new User("1", "Alice");
        User user2 = new User("2", "Bob");
        User user3 = new User("3", "Charlie");

        splitwise.addUser(user1);
        splitwise.addUser(user2);
        splitwise.addUser(user3);

        splitwise.addObserver(new ExpenseObserver());

        System.out.println("Adding an equal expense:");
        splitwise.addEqualExpense(300.0, Arrays.asList(user1, user2, user3));

        System.out.println("Adding an unequal expense:");
        splitwise.addUnequalExpense(300.0, Arrays.asList(user1, user2, user3));
    }
}

