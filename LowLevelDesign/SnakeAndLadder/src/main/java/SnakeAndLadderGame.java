import singleton.Board;
import factory.ObstacleFactory;
import observer.Player;
import strategy.DiceStrategy;
import strategy.NormalDice;

import java.util.Arrays;

public class SnakeAndLadderGame {
    public static void main(String[] args) {
        Board board = Board.getInstance(100);
        ObstacleFactory.createSnakes(board, Arrays.asList(new int[][]{
                {16, 6}, {48, 26}, {49, 11}, {56, 53}, {62, 19}, {64, 60}, {87, 24}, {93, 73}, {95, 75}, {98, 78}}));
        ObstacleFactory.createLadders(board, Arrays.asList(new int[][]{
                {1, 38}, {4, 14}, {9, 31}, {21, 42}, {28, 84}, {36, 44}, {51, 67}, {71, 91}, {80, 100}}));

        Game game = new Game();
        Player player1 = new Player("Alice");
        Player player2 = new Player("Bob");

        game.addPlayer(player1);
        game.addPlayer(player2);

        DiceStrategy dice = (DiceStrategy) new NormalDice();
        game.startGame(board, dice);
    }
}
