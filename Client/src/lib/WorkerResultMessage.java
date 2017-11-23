package lib;

import java.io.Serializable;

public class WorkerResultMessage implements Serializable {

    private SearchedArticle searchedArticle;
    //private SearchActivity searchActivity;
    private final int searchActivityID;
    private final int articlesLeft;

    public WorkerResultMessage(SearchedArticle searchedArticle, int searchActivityID, int articlesLeft) {
        this.searchedArticle = searchedArticle;
        this.searchActivityID = searchActivityID;
        this.articlesLeft = articlesLeft;
    }

    public SearchedArticle getSearchedArticle() {
        return searchedArticle;
    }

    public int getSearchActivityID() {
        return searchActivityID;
    }

    public int getArticlesLeft() {
        return articlesLeft;
    }
}
