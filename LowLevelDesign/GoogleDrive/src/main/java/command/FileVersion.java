package command;// command.FileVersion.java


public class FileVersion {
    private String content;
    private long timestamp;

    public FileVersion(String content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

