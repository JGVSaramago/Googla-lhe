import lib.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity {

    private int counter = 0;

    private final int ID;
    private ArrayList<SearchedArticle> results = new ArrayList<>();
    private int occurrencesFound = 0;
    private int filesWithOccurrences = 0;
    private String[] searchHist;

    private AtomicInteger articlesLeft;
    private ServerStreamer client;
    private String findStr;

    public SearchActivity(int ID, int articlesLeft, ServerStreamer client, String findStr, String[] searchHist) {
        this.ID = ID;
        this.articlesLeft = new AtomicInteger(articlesLeft);
        this.client = client;
        this.findStr = findStr;
        this.searchHist = searchHist;
    }

    public int getID() {
        return ID;
    }

    public ArrayList<SearchedArticle> getResults() {
        return results;
    }

    public int getOccurrencesFound() {
        return occurrencesFound;
    }

    public int getFilesWithOccurrences() {
        return filesWithOccurrences;
    }

    public String[] getSearchHist() {
        return searchHist;
    }

    public int getArticlesLeft() {
        return articlesLeft.get();
    }

    public ServerStreamer getClient() {
        return client;
    }

    public String getFindStr() {
        return findStr;
    }

    public void decrementArticesLeft() {
        if (articlesLeft.decrementAndGet() < 0)
            System.out.println("SearchActivity "+ID+": articlesLeft is "+getArticlesLeft());
    }

    public void addResult(SearchedArticle sa) {
        results.add(sa);
        occurrencesFound += sa.getOccurrencesCount();
        filesWithOccurrences++;
    }


}
