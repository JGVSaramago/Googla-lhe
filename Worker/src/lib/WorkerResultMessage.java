package lib;

import java.io.Serializable;

public class WorkerResultMessage implements Serializable {

    private SearchedArticle searchedArticle;
    //private SearchActivity searchActivity;
    private final int searchActivityID;

    public WorkerResultMessage(SearchedArticle searchedArticle, int searchActivityID) {
        this.searchedArticle = searchedArticle;
        this.searchActivityID = searchActivityID;
    }

    public SearchedArticle getSearchedArticle() {
        return searchedArticle;
    }

    public int getSearchActivityID() {
        return searchActivityID;
    }
}
