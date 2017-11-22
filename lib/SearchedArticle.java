package Project;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchedArticle implements Serializable{

    private ArticleTitle articleTitle;
    private ArrayList<Integer> occurrences;

    public SearchedArticle(ArticleTitle articleTitle) {
        this.articleTitle = articleTitle;
        occurrences = new ArrayList<>();
    }

    public String getTitle() {
        return articleTitle.getTitle();
    }

    public ArrayList<Integer> getOccurrences() {
        return occurrences;
    }

    public int getOccurrencesCount() {
        return occurrences.size();
    }

    public void addOccurrence(int start) {
        occurrences.add(start);
    }

    public int getID() {
        return articleTitle.getID();
    }
}
