# Tic-Tac-Toe Game

A simple command-line Tic-Tac-Toe game implemented in Java. This game allows two players to play Tic-Tac-Toe by taking turns to place their pieces on the board. The first player to align three of their pieces in a row, column, or diagonal wins the game.

## Project Structure

- **com.tictactoe.Model**
    - `Board`: Represents the game board, managing the placement of pieces and tracking the board's state.
    - `PieceType`: Enum for the different types of pieces ("X" and "O") used in the game.
    - `Player`: Represents a player, storing player information such as name and assigned piece type.
    - `PlayingPiece`: Abstract class representing a generic game piece.
    - `PlayingPieceX` and `PlayingPieceO`: Extend `PlayingPiece` to represent specific piece types, "X" and "O" respectively.
    - `Main`: Entry point for the program, initializes and starts the game.
    - `TicTacToeGame`: Controls the game logic, managing turns, win conditions, and coordinating game components.

## How to Run the Game

1. Clone this repository or download the source code.
2. Make sure you have Java installed (version 8 or above).
3. Compile the Java files:
   ```bash
   javac -d bin src/com/tictactoe/Model/*.java src/Main.java
