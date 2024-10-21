# Vending Machine System

This project implements a **Vending Machine** using the **Strategy Pattern**, **Observer Pattern**, and **Factory Method Pattern** in Java. The system allows users to select items, make payments through different methods, and dispense items after a successful transaction.

## Table of Contents
- [Overview](#overview)
- [Design Patterns Used](#design-patterns-used)
    - [Strategy Pattern](#strategy-pattern)
    - [Observer Pattern](#observer-pattern)
    - [Factory Method Pattern](#factory-method-pattern)
- [Classes and Responsibilities](#classes-and-responsibilities)
    - [Payment Strategy](#payment-strategy)
    - [Observer Pattern for Display](#observer-pattern-for-display)
    - [Factory for Item Creation](#factory-for-item-creation)
    - [Dispenser, CoinAcceptor, and CardReader](#dispenser-coinacceptor-and-cardreader)
    - [Vending Machine](#vending-machine)
    - [Item Database](#item-database)
- [Getting Started](#getting-started)
- [Example Usage](#example-usage)

## Overview
The Vending Machine System allows the user to:
- Select an item.
- Add payments using coins or card.
- Complete or cancel the transaction.
- Dispense items if the payment is successful.
- Display messages and balances through an observer pattern.

## Design Patterns Used

### Strategy Pattern
The **Strategy Pattern** is used for implementing different payment methods:
- `PaymentStrategy` interface allows dynamic selection of payment methods (e.g., coin or card).
- `CoinPayment` and `CardPayment` implement the strategy for handling specific payment logic.

### Observer Pattern
The **Observer Pattern** is used to keep the **Display** updated with the vending machine’s state:
- `DisplayObserver` interface allows `Display` to be notified of changes in messages (e.g., balance, transaction status).
- `Display` implements `DisplayObserver` and provides methods to display price and balance.

### Factory Method Pattern
The **Factory Method Pattern** simplifies the creation of different items like snacks and drinks:
- `Item` abstract class represents common properties of vending items.
- `SnackItem` and `DrinkItem` extend `Item` for specific item types.
- `ItemFactory` class provides a static method to create items dynamically based on type.

## Classes and Responsibilities

### Payment Strategy
- **`PaymentStrategy` Interface**: Defines methods for adding payments, checking balances, and processing payments.
- **`CoinPayment`**: Handles payments made using coins.
- **`CardPayment`**: Handles payments made using card transactions.

### Observer Pattern for Display
- **`DisplayObserver` Interface**: Declares a method for updating the display with new messages.
- **`Display`**: Implements `DisplayObserver` and shows messages like item prices, balances, and transaction status.

### Factory for Item Creation
- **`Item`**: Abstract class for items with `code`, `price`, and `quantity`.
- **`SnackItem`**: Represents a snack item.
- **`DrinkItem`**: Represents a drink item.
- **`ItemFactory`**: Creates instances of `SnackItem` or `DrinkItem`.

### Dispenser, CoinAcceptor, and CardReader
- **`Dispenser`**: Simulates dispensing of items.
- **`CoinAcceptor`**: Simulates accepting coins and adding the value to the current balance.
- **`CardReader`**: Simulates reading card payments and adding the amount to the balance.

### Vending Machine
- **`VendingMachine`**: Main class that manages item selection, payment, and transactions.
    - Manages item inventory through `ItemDatabase`.
    - Uses a `PaymentStrategy` for handling payments.
    - Updates the `Display` using `DisplayObserver`.
    - Dispenses items through the `Dispenser`.

### Item Database
- **`ItemDatabase`**: Manages available items using a `HashMap` to store items by their code.

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/vending-machine-system.git
