import lib.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity {

    private final int searchActivityID;
    private ArrayList<SearchedArticle> results = new ArrayList<>();
    private ArrayList<RequestToWorkerMessage> pendingSearches = new ArrayList<>();
    private volatile int occurrencesFound = 0;
    private AtomicInteger filesWithOccurrences = new AtomicInteger(0);
    private String[] searchHist;

    private final int articlesCount;
    private AtomicInteger articlesLeft;
    private AtomicInteger articlesReceived;
    private ServerStreamer client;
    private String findStr;

    public SearchActivity(int searchActivityID, int articlesLeft, ServerStreamer client, String findStr, String[] searchHist) {
        this.searchActivityID = searchActivityID;
        this.articlesLeft = new AtomicInteger(articlesLeft);
        articlesReceived = new AtomicInteger(articlesLeft);
        this.client = client;
        this.findStr = findStr;
        this.searchHist = searchHist;
        articlesCount = articlesLeft;
    }

    public int getSearchActivityID() {
        return searchActivityID;
    }

    public int getArticlesReceived() {
        return articlesReceived.get();
    }

    public ArrayList<SearchedArticle> getResults() {
        return results;
    }

    public ArrayList<RequestToWorkerMessage> getPendingSearches() {
        return pendingSearches;
    }

    public int getOccurrencesFound() {
        return occurrencesFound;
    }

    public int getFilesWithOccurrences() {
        return filesWithOccurrences.get();
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
            System.out.println("SearchActivity "+ searchActivityID +": articlesLeft is "+getArticlesLeft());
    }

    public void incrementArticlesReceived() {
        if (articlesReceived.incrementAndGet() > articlesCount)
            System.out.println("SearchActivity: This should be impossible "+articlesReceived.get());
    }

    public synchronized void addResult(SearchedArticle sa) {
        results.add(sa);
        occurrencesFound += sa.getOccurrencesCount();
        filesWithOccurrences.incrementAndGet();
        incrementArticlesReceived();
    }

    public void addPendingSearch(RequestToWorkerMessage rtwm) {
        pendingSearches.add(rtwm);
    }


}
