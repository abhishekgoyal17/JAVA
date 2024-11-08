package proxy;

import command.User;
import composite.FileSystemComponent;
import composite.Folder;
import decorator.PermissionsDecorator;

import java.net.URLConnection;

// FileSystemProxy.java
public class FileSystemProxy implements FileSystemComponent {
    private FileSystemComponent realComponent;
    private User currentUser;

    public FileSystemProxy(FileSystemComponent component, User user) {
        this.realComponent = component;
        this.currentUser = user;
    }

    public FileSystemProxy(Folder documents, User user1) {
    }

    @Override
    public String getName() {
        return realComponent.getName();
    }

    @Override
    public void setName(String name) {
        if (hasWritePermission()) {
            realComponent.setName(name);
        } else {
            throw new SecurityException("No write permission for user: " + currentUser.getUsername());
        }
    }

    @Override
    public void add(FileSystemComponent component) {
        if (hasWritePermission()) {
            realComponent.add(component);
        } else {
            throw new SecurityException("No write permission for user: " + currentUser.getUsername());
        }
    }

    @Override
    public void remove(FileSystemComponent component) {
        if (hasWritePermission()) {
            realComponent.remove(component);
        } else {
            throw new SecurityException("No write permission for user: " + currentUser.getUsername());
        }
    }

    @Override
    public FileSystemComponent getChild(String name) {
        if (hasReadPermission()) {
            return realComponent.getChild(name);
        } else {
            throw new SecurityException("No read permission for user: " + currentUser.getUsername());
        }
    }

    @Override
    public PermissionsDecorator getPermissions() {
        return null;
    }

    @Override
    public void display(String indent) {
        if (hasReadPermission()) {
            realComponent.display(indent);
        } else {
            throw new SecurityException("No read permission for user: " + currentUser.getUsername());
        }
    }


    private boolean hasReadPermission() {
        PermissionsDecorator.Permission permission = realComponent.getPermissions().getPermission(currentUser);
        return permission == PermissionsDecorator.Permission.READ || permission == PermissionsDecorator.Permission.WRITE;
    }

    private boolean hasWritePermission() {
        PermissionsDecorator.Permission permission = realComponent.getPermissions().getPermission(currentUser);
        return permission == PermissionsDecorator.Permission.WRITE;
    }
}
