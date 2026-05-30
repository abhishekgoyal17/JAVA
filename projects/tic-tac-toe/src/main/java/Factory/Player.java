package Factory;

import Observer.Board;

public abstract class Player {
    protected char symbol;

    public char getSymbol() {
        return symbol;
    }

    public abstract void makeMove(Board board);

    public void update(Board board) {
    }
}





