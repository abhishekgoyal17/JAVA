package command;

// command.User.java
public class User {
    private String username;
    private String password;
    private Role role;

    public enum Role {
        ADMIN, USER
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    // Authentication Method
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}
