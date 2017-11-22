import Project.SearchedArticle;

import java.util.ArrayList;

public class SearchActivity {

    private ArrayList<SearchedArticle> results;
    private int occurrencesFound = 0;
    private int filesWithOccurrences = 0;
    private String[] searchHist;

    private int articlesLeft;
    private ServerStreamer client;
    private String findStr;
    private boolean done = false;

    public SearchActivity(int articlesLeft, ServerStreamer client, String findStr, String[] searchHist) {
        this.results = new ArrayList<>();
        this.articlesLeft = articlesLeft;
        this.client = client;
        this.findStr = findStr;
        this.searchHist = searchHist;
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

    public synchronized int getArticlesLeft() {
        return articlesLeft;
    }

    public ServerStreamer getClient() {
        return client;
    }

    public String getFindStr() {
        return findStr;
    }

    public void searchStarted(){
        articlesLeft--;
        if (articlesLeft<1)
            done = true;
    }

    public void searchDone(SearchedArticle searchedArticle) {
        if (searchedArticle.getOccurrencesCount() > 0) {
            results.add(searchedArticle);
            occurrencesFound += searchedArticle.getOccurrencesCount();
            filesWithOccurrences++;
        }
    }

    public boolean isDone(){
        return done;
    }
}
