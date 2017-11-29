package lib;

public enum MessageType {

    HISTORY(1), WORKER(2), CLEANHISTORY(3), CLIENT(4), CLOSE(10);

    private final int messageType;

    MessageType(int messageType) {
        this.messageType = messageType;
    }
}
