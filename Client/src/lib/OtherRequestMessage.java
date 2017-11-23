package lib;

import java.io.Serializable;

public class OtherRequestMessage implements Serializable {

    private MessageType type;
    private String username;

    public OtherRequestMessage(MessageType type, String username) {
        this.type = type;
        this.username = username;
    }

    public MessageType getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

}
