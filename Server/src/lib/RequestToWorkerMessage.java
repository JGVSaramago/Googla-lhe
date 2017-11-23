package lib;

import java.io.Serializable;

public class RequestToWorkerMessage implements Serializable {

    private final ArticleToSearch articleToSearch;
    private final int WORKER_ID;

    public RequestToWorkerMessage(ArticleToSearch articleToSearch, int WORKER_ID) {
        this.articleToSearch = articleToSearch;
        this.WORKER_ID = WORKER_ID;
    }

    public ArticleToSearch getArticleToSearch() {
        return articleToSearch;
    }

    public int getWORKER_ID() {
        return WORKER_ID;
    }
}
