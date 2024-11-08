import command.FileSystemSearch;
import command.User;
import composite.File;
import composite.FileSystemComponent;
import composite.Folder;
import decorator.PermissionsDecorator;
import observer.Observer;
import observer.Subject;
import proxy.FileSystemProxy;
import singleton.FileSystemManager;
import strategy.LocalStorageStrategy;
import template.CreateFileOperation;
import template.FileOperation;

// Main.java
public class Main {
    public static void main(String[] args) {
        // Create Users
        User admin = new User("admin", "admin123", User.Role.ADMIN);
        User user1 = new User("user1", "password1", User.Role.USER);
        User user2 = new User("user2", "password2", User.Role.USER);

        // Get FileSystemManager Instance
        FileSystemManager fsManager = FileSystemManager.getInstance();

        // Create Folders and Files
        Folder root = fsManager.getRoot();
        root.getPermissions().setPermission(admin, PermissionsDecorator.Permission.WRITE);
        root.getPermissions().setPermission(user1, PermissionsDecorator.Permission.WRITE);

        // Create Files and Folders using Template Method Pattern
        FileOperation createFolderOperation = new CreateFileOperation(root, "Documents");
        createFolderOperation.execute();

        Folder documents = (Folder) root.getChild("Documents");
        documents.getPermissions().setPermission(user1, PermissionsDecorator.Permission.WRITE);

        FileOperation createFileOperation = new CreateFileOperation(documents, "Resume.docx");
        createFileOperation.execute();

        // Use Proxy Pattern for Access Control
        FileSystemProxy user1Proxy = new FileSystemProxy(documents, user1);
        user1Proxy.display("");

        // Version Control
        File resume = (File) documents.getChild("Resume.docx");
        resume.writeContent("Version 1 of Resume");
        resume.writeContent("Version 2 of Resume");

        // Observer Pattern for Notifications
        Subject subject = new Subject();
        Observer user2Observer = new Observer() {
            @Override
            public void update(String message) {
                System.out.println("User2 received notification: " + message);
            }
        };
        subject.attach(user2Observer);
        subject.notifyObservers("Resume.docx has been updated.");

        // Search Functionality
        FileSystemSearch search = new FileSystemSearch();
        FileSystemComponent found = search.search(root, "Resume.docx");
        if (found != null) {
            System.out.println("Found: " + found.getName());
        } else {
            System.out.println("File not found.");
        }

        // Storage Strategy
        fsManager.setStorageStrategy(new LocalStorageStrategy());
        fsManager.getStorageStrategy().saveFile(resume);
    }
}
