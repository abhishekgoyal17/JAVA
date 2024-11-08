package Strategy;

import Observer.Board;

public interface WinStrategy {
    boolean checkWin(Board board, char symbol);
}
