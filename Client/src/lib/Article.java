package lib;

import java.io.Serializable;

/**
 * .txt article transformed into an object
 */

public class Article implements Serializable{

    private final int ID;
    private final String title;
    private final String body;

    public Article(int ID, String title, String body) {
        this.ID = ID;
        this.title = title;
        this.body = body;
    }

    public int getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
