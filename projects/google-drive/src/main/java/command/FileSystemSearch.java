package command;

import composite.FileSystemComponent;
import composite.Folder;

// command.FileSystemSearch.java
public class FileSystemSearch {
    public FileSystemComponent search(FileSystemComponent component, String name) {
        if (component.getName().equals(name)) {
            return component;
        }

        if (component instanceof Folder) {
            Folder folder = (Folder) component;
            for (FileSystemComponent child : folder.getChildren().values()) {
                FileSystemComponent found = search(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
