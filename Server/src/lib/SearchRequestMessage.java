package lib;

import java.io.Serializable;

public class SearchRequestMessage implements Serializable {

    private String username;
    private String findStr;

    public SearchRequestMessage(String username, String findStr) {
        this.username = username;
        this.findStr = findStr;
    }

    public String getUsername() {
        return username;
    }

    public String getFindStr() {
        return findStr;
    }

}
