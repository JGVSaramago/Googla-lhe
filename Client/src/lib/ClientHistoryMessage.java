package lib;

import java.io.Serializable;
import java.util.ArrayList;

public class ClientHistoryMessage implements Serializable{

    private final ArrayList<String[]> clientHistory;

    public ClientHistoryMessage(ArrayList<String[]> clientHistory) {
        this.clientHistory = clientHistory;
    }

    public ArrayList<String[]> getClientHistory() {
        return clientHistory;
    }

}
