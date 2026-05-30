package template;

import composite.File;
import composite.Folder;

// CreateFileOperation.java
public class CreateFileOperation extends FileOperation {
    private Folder folder;
    private String fileName;

    public CreateFileOperation(Folder folder, String fileName) {
        this.folder = folder;
        this.fileName = fileName;
    }

    @Override
    protected boolean validate() {
        return folder.getChild(fileName) == null;
    }

    @Override
    protected void performOperation() {
        folder.add(new File(fileName));
    }
}

