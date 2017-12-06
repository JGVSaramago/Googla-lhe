package lib;

import java.io.Serializable;

public class RequestToWorkerMessage implements Serializable {

    private final ArticleToSearch articleToSearch;

    public RequestToWorkerMessage(ArticleToSearch articleToSearch) {
        this.articleToSearch = articleToSearch;
    }

    public ArticleToSearch getArticleToSearch() {
        return articleToSearch;
    }
}
