import Factory.Player;
import Factory.PlayerFactory;
import Observer.Board;
import Singleton.GameController;
import Strategy.DefaultMoveStrategy;
import Strategy.DefaultWinStrategy;
import Strategy.MoveStrategy;
import Strategy.WinStrategy;

import java.util.Scanner;

public class TicTacToe {
    private Board board;
    private Player currentPlayer;
    private Player player1;
    private Player player2;
    private MoveStrategy moveStrategy;
    private WinStrategy winStrategy;

    public TicTacToe() {
        GameController gameController = GameController.getInstance();
        board = new Board(3);
        player1 = PlayerFactory.createPlayer('X');
        player2 = PlayerFactory.createPlayer('O');
        currentPlayer = player1;
        moveStrategy = new DefaultMoveStrategy();
        winStrategy = new DefaultWinStrategy();
        board.addObserver(player1);
        board.addObserver(player2);
    }

    public void playGame() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Player " + currentPlayer.getSymbol() + "'s turn");
            System.out.println("Enter row (0, 1, or 2): ");
            int x = scanner.nextInt();
            System.out.println("Enter column (0, 1, or 2): ");
            int y = scanner.nextInt();

            // Validate move
            if (moveStrategy.isValidMove(board, x, y)) {
                // Update board
                board.updateBoard(x, y, currentPlayer.getSymbol());

                // Check for win
                if (winStrategy.checkWin(board, currentPlayer.getSymbol())) {
                    System.out.println("Player " + currentPlayer.getSymbol() + " wins!");
                    break;
                }

                // Check for draw
                if (isDraw()) {
                    System.out.println("Game is a draw!");
                    break;
                }

                // Switch player
                switchPlayer();
            } else {
                System.out.println("Invalid move! Try again.");
            }
        }
        scanner.close();
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private boolean isDraw() {
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getCell(i, j) == '\0') {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();
        game.playGame();
    }
}
