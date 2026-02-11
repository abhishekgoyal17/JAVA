package LowLevelDesign.DesignBookMyShow.entities;

/**
 * Represents a user of the BookMyShow system.
 * Contains a unique userId and a user name.
 */
public class User {

    private final String userId;
    private final String name;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
