package Factory;

import Observer.Board;

public class HumanPlayer extends Player {
    public HumanPlayer(char symbol) {
        this.symbol = symbol;
    }

    @Override
    public void makeMove(Board board) {
        // Implementation for making a move
    }

    public void update(Board board) {
        // Implementation to update player with board state
    }
}