package strategy;

import java.util.Random;

public class LoadedDice implements DiceStrategy {
    private final Random random;

    public LoadedDice() {
        this.random = new Random();
    }

    @Override
    public int rollDice() {
        return random.nextInt(3) + 4;  // Rolls between 4 and 6
    }
}
