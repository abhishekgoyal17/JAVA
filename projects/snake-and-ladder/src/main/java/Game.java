import command.Command;
import command.MoveCommand;
import observer.Player;
import singleton.Board;
import strategy.DiceStrategy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Game class with multithreading support
public class Game {
    private final List<Player> players;
    private final ExecutorService executorService;
    private volatile boolean gameWon;

    public Game() {
        this.players = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newFixedThreadPool(4);
        this.gameWon = false;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void notifyPlayers(String message) {
        for (Player player : players) {
            player.update(message);
        }
    }

    public void startGame(Board board, DiceStrategy dice) {
        for (Player player : players) {
            executorService.submit(() -> playTurn(player, board, dice));
        }
        executorService.shutdown();
    }

    private void playTurn(Player player, Board board, DiceStrategy dice) {
        while (!gameWon) {
            int diceRoll = dice.rollDice();
            Command moveCommand = new MoveCommand(player, diceRoll, board);
            moveCommand.execute();

            notifyPlayers(player.getName() + " rolled a " + diceRoll + " and moved to " + player.getPosition());

            if (player.getPosition() == board.getSize()) {
                gameWon = true;
                notifyPlayers(player.getName() + " wins!");
                break;
            }
            try {
                Thread.sleep(1000); // Adding delay to simulate real-time gameplay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}