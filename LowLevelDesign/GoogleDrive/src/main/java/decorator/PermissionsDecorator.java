package decorator;
// PermissionsDecorator.java

import command.User;
import composite.FileSystemComponent;

import java.util.HashMap;

import java.util.Map;

public class PermissionsDecorator {
    private Map<User, Permission> userPermissions;


    public PermissionsDecorator() {
        this.userPermissions = new HashMap<>();
    }

    public void setPermission(User user, Permission permission) {
        userPermissions.put(user, permission);
    }

    public Permission getPermission(User user) {
        return userPermissions.getOrDefault(user, Permission.NONE);
    }




    public enum Permission {
        READ, WRITE, NONE
    }

}


