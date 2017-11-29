package lib;

import java.io.Serializable;

public class ArticleToSearch implements Serializable{

    private Article article;
    private String findStr;
    private final int searchActivityID;

    public ArticleToSearch(int searchActivityID, Article article, String findStr) {
        this.searchActivityID = searchActivityID;
        this.article = article;
        this.findStr = findStr;
    }

    public int getSearchActivityID() {
        return searchActivityID;
    }

    public Article getArticle() {
        return article;
    }

    public String getFindStr() {
        return findStr;
    }
}
