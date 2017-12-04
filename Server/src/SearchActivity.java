import lib.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity {

    int counter = 0;

    private final int searchActivityID;
    private ArrayList<SearchedArticle> results = new ArrayList<>();
    private ArrayList<ArticleToSearch> pendingSearches = new ArrayList<>();
    private volatile int occurrencesFound = 0;
    private AtomicInteger filesWithOccurrences = new AtomicInteger(0);
    private String[] searchHist;

    private final int articlesCount;
    private AtomicInteger articlesLeft;
    private AtomicInteger articlesReceived;
    private ServerStreamer client;
    private String findStr;
    private volatile boolean waitingForAnswers = false;

    public SearchActivity(int searchActivityID, int articlesLeft, ServerStreamer client, String findStr, String[] searchHist) {
        this.searchActivityID = searchActivityID;
        this.articlesLeft = new AtomicInteger(articlesLeft-1);
        articlesReceived = new AtomicInteger(0);
        this.client = client;
        this.findStr = findStr;
        this.searchHist = searchHist;
        articlesCount = articlesLeft;
    }

    public void waitingForAnswers() {
        waitingForAnswers = true;
    }

    public boolean isWaitingForAnswers() {
        return waitingForAnswers;
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

    public ArrayList<ArticleToSearch> getPendingSearches() {
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

    public synchronized boolean incrementArticlesReceived() {
        counter++;
        System.out.println("Incremented "+counter+" times.");
        if (articlesReceived.incrementAndGet() > articlesCount)
            System.out.println("SearchActivity: This should be impossible "+articlesReceived.get()+">"+articlesCount);
        if (articlesReceived.get() >= articlesCount)
            return false;
        return true;
    }

    public synchronized boolean addResult(SearchedArticle sa) {
        results.add(sa);
        occurrencesFound += sa.getOccurrencesCount();
        filesWithOccurrences.incrementAndGet();
        return incrementArticlesReceived();
    }

    public void addPendingSearch(ArticleToSearch ats) {
        pendingSearches.add(ats);
    }



}
