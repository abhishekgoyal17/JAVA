package Singleton;

public class GameController {
    private static GameController instance;

    private GameController() {
        // private constructor to prevent instantiation
    }

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }
}
