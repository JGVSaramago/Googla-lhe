package Project;

import java.io.Serializable;

public class ArticleToSearch implements Serializable{

    private Article article;
    private String findStr;

    public ArticleToSearch(Article article, String findStr) {
        this.article = article;
        this.findStr = findStr;
    }

    public Article getArticle() {
        return article;
    }

    public String getFindStr() {
        return findStr;
    }
}
