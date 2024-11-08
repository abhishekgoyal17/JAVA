package Strategy;

import Observer.Board;

public interface MoveStrategy {
    boolean isValidMove(Board board, int x, int y);
}