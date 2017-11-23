package lib;

import java.io.Serializable;

public class ArticleBody implements Serializable {

    private final int ID;
    private final String body;

    public ArticleBody(int ID, String body) {
        this.ID = ID;
        this.body = body;
    }

    public int getID() {
        return ID;
    }

    public String getBody() {
        return body;
    }

}
