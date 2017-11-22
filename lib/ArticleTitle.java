package Project;

import java.io.Serializable;

public class ArticleTitle implements Serializable {

    private final int ID;
    private final String title;

    public ArticleTitle(int ID, String title) {
        this.ID = ID;
        this.title = title;
    }

    public int getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

}
