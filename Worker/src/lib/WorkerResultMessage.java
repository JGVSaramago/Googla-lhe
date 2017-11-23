package lib;

import java.io.Serializable;

public class WorkerResultMessage implements Serializable {

    private SearchedArticle searchedArticle;
    private final int searchActivityID;
    private final int WORKER_ID;

    public WorkerResultMessage(SearchedArticle searchedArticle, int searchActivityID, int WORKER_ID) {
        this.searchedArticle = searchedArticle;
        this.searchActivityID = searchActivityID;
        this.WORKER_ID = WORKER_ID;
    }

    public SearchedArticle getSearchedArticle() {
        return searchedArticle;
    }

    public int getSearchActivityID() {
        return searchActivityID;
    }

    public int getWORKER_ID() {
        return WORKER_ID;
    }
}
