package composite;

// File.java
import command.FileVersion;
import decorator.PermissionsDecorator;

import java.util.ArrayList;
import java.util.List;

public class File implements FileSystemComponent {
    private String name;
    private String content;
    private List<FileVersion> versions;
    private PermissionsDecorator permissions;

    public File(String name) {
        this.name = name;
        this.content = "";
        this.versions = new ArrayList<>();
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
        throw new UnsupportedOperationException("Cannot add to a file.");
    }

    @Override
    public void remove(FileSystemComponent component) {
        throw new UnsupportedOperationException("Cannot remove from a file.");
    }

    @Override
    public FileSystemComponent getChild(String name) {
        throw new UnsupportedOperationException("Files do not contain children.");
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "File: " + name);
    }

    // File-Specific Methods
    public void writeContent(String content) {
        this.content = content;
        versions.add(new FileVersion(content));
    }

    public String readContent() {
        return content;
    }

    public List<FileVersion> getVersions() {
        return versions;
    }

    public PermissionsDecorator getPermissions() {
        return permissions;
    }
}

