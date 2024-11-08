package Strategy;

import Observer.Board;

public class DefaultMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(Board board, int x, int y) {
        return board.getCell(x, y) == '\0';
    }
}

