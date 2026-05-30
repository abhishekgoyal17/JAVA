package composite;

// Folder.java
import decorator.PermissionsDecorator;

import java.util.HashMap;
import java.util.Map;

public class Folder implements FileSystemComponent {
    private String name;
    private Map<String, FileSystemComponent> children;
    private PermissionsDecorator permissions;

    public Folder(String name) {
        this.name = name;
        this.children = new HashMap<>();
        this.permissions = new PermissionsDecorator();
    }

    // Composite Pattern Methods
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void add(FileSystemComponent component) {
        children.put(component.getName(), component);
    }

    @Override
    public void remove(FileSystemComponent component) {
        children.remove(component.getName());
    }

    @Override
    public FileSystemComponent getChild(String name) {
        return children.get(name);
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "Folder: " + name);
        for (FileSystemComponent component : children.values()) {
            component.display(indent + "    ");
        }
    }

    public Map<String, FileSystemComponent> getChildren() {
        return children;
    }

    public PermissionsDecorator getPermissions() {
        return permissions;
    }
}
