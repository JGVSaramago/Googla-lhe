package Project;

import Project.Server.SearchActivity;

import java.io.Serializable;

public class WorkerResultMessage implements Serializable {

    private SearchedArticle searchedArticle;
    private SearchActivity searchActivity;

    public WorkerResultMessage(SearchedArticle searchedArticle, SearchActivity searchActivity) {
        this.searchedArticle = searchedArticle;
        this.searchActivity = searchActivity;
    }

    public SearchedArticle getSearchedArticle() {
        return searchedArticle;
    }

    public SearchActivity getSearchActivity() {
        return searchActivity;
    }
}
