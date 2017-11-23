package lib;

import java.io.Serializable;

public class BodyRequestMessage implements Serializable{

    private final int ID;

    public BodyRequestMessage(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }
}
