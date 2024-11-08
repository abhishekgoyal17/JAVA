# Splitwise-like Expense Management System

This project is an implementation of a simplified **Splitwise**-like system using various design patterns in Java. It supports managing users, adding expenses, and splitting expenses equally or unequally among participants.

## Table of Contents
- [Overview](#overview)
- [Design Patterns Used](#design-patterns-used)
    - [Singleton Pattern](#singleton-pattern)
    - [Factory Pattern](#factory-pattern)
    - [Observer Pattern](#observer-pattern)
    - [Strategy Pattern](#strategy-pattern)
    - [Command Pattern](#command-pattern)
    - [Facade Pattern](#facade-pattern)
- [Classes and Responsibilities](#classes-and-responsibilities)
    - [command.User and UserManager](#user-and-usermanager)
    - [Expense Classes](#expense-classes)
    - [Split Strategies](#split-strategies)
    - [Expense Command](#expense-command)
    - [SplitwiseFacade](#splitwisefacade)
    - [Observer for Expense Updates](#observer-for-expense-updates)
- [Getting Started](#getting-started)
- [Example Usage](#example-usage)

## Overview
This project allows users to:
- Add users and manage them through a singleton `UserManager`.
- Create expenses and split them among participants using either equal or custom split strategies.
- Update displays and manage state changes using observers.
- Use a `SplitwiseFacade` to simplify operations like adding users and expenses.

## Design Patterns Used

### Singleton Pattern
- **`UserManager`**: Ensures a single instance manages users throughout the application's lifecycle.

### Factory Pattern
- **`ExpenseFactory` Interface**: Defines a method for creating different types of expenses.
- **`EqualExpenseFactory` and `UnequalExpenseFactory`**: Implement the creation logic for equal and unequal expenses.

### Observer Pattern
- **`Observer` Interface**: Allows classes like `ExpenseObserver` to listen for updates when new expenses are added.
- **`ExpenseObserver`**: Example observer that can be extended for different update logic.

### Strategy Pattern
- **`SplitStrategy` Interface**: Defines a method for splitting expenses among participants.
- **`EqualSplitStrategy`**: Splits the total amount equally among participants.
- **`UnequalSplitStrategy`**: Allows for custom splits (demonstrated with equal splitting).

### Command Pattern
- **`ExpenseCommand` Interface**: Encapsulates the action of adding an expense.
- **`AddExpenseCommand`**: Executes the addition of an expense and calculates the shares.

### Facade Pattern
- **`SplitwiseFacade`**: Simplifies interactions with the `UserManager`, `Expense` creation, and observer management. It provides methods to add users, add expenses, and notify observers.

## Classes and Responsibilities

### command.User and UserManager
- **`command.User`**: Represents a user with `userId` and `name`.
- **`UserManager`**: Singleton class that manages users using a `HashMap` for quick access.

### Expense Classes
- **`Expense`**: Abstract class representing common properties of expenses such as `totalAmount`, `participants`, and `shares`.
- **`EqualExpense`**: Uses `EqualSplitStrategy` to split expenses equally.
- **`UnequalExpense`**: Uses `UnequalSplitStrategy` for custom splitting logic.

### Split Strategies
- **`SplitStrategy` Interface**: Defines a method for splitting the total amount.
- **`EqualSplitStrategy`**: Distributes the total amount evenly.
- **`UnequalSplitStrategy`**: Allows for custom logic (uses equal splitting in this example).

### Expense Command
- **`ExpenseCommand` Interface**: Declares an `execute` method.
- **`AddExpenseCommand`**: Handles adding an expense and calculating the shares.

### SplitwiseFacade
- **`SplitwiseFacade`**: Simplifies adding users and expenses, maintaining a list of observers, and notifying them when expenses are updated.

### Observer for Expense Updates
- **`Observer` Interface**: Provides a method to update observers when expenses change.
- **`ExpenseObserver`**: An observer that can react to changes in expense data.

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/splitwise-like-expense-manager.git
