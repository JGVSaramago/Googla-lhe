package lib;

import java.io.Serializable;

public class ArticleToSearch implements Serializable{

    private Article article;
    private String findStr;
    private final int searchActivityID;
    private final int articlesLeft;

    public ArticleToSearch(int searchActivityID, int articlesLeft, Article article, String findStr) {
        this.searchActivityID = searchActivityID;
        this.articlesLeft = articlesLeft;
        this.article = article;
        this.findStr = findStr;
    }

    public int getSearchActivityID() {
        return searchActivityID;
    }

    public int getArticlesLeft() {
        return articlesLeft;
    }

    public Article getArticle() {
        return article;
    }

    public String getFindStr() {
        return findStr;
    }
}
