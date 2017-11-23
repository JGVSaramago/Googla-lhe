import lib.*;

import java.util.ArrayList;

public class WorkerManager extends Thread{

    private SearchEngine searchEngine;
    private ServerStreamer worker;
    private ArrayList<SearchActivity> searchActivities;
    private ArrayList<Article> articles;

    private int activityIndex = 0;

    public WorkerManager(SearchEngine searchEngine, ServerStreamer worker) {
        this.searchEngine = searchEngine;
        this.worker = worker;
        this.searchActivities = searchEngine.getSearchActivities();
        this.articles = searchEngine.getArticles();
    }

    private synchronized void waitingMode() {
        synchronized (searchEngine){
            try {
                System.out.println("WorkerManager going to sleep...");
                searchEngine.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void incrementActivityIndex() {
        int arraySize = searchActivities.size();
        if (activityIndex < (arraySize-1))
            activityIndex++;
        else
            activityIndex = 0;
    }

    @Override
    public void run() {
        while (true){
            if (searchActivities.isEmpty()) {
                waitingMode();
            } else {
                SearchActivity searchActivity = searchActivities.get(activityIndex);
                if (!searchActivity.isDone()) {
                    System.out.println("Articles left: " + searchActivity.getArticlesLeft());
                    ArticleToSearch articleToSearch = new ArticleToSearch(searchActivity.getID(), articles.get(searchActivity.getArticlesLeft()), searchActivity.getFindStr());
                    searchActivity.searchStarted();
                    worker.sendServerMessage(articleToSearch);
                    System.out.println("Enviado para worker");
                } else {
                    searchEngine.searchCompleted(searchActivity);
                    System.out.println("Search completed");
                }
                incrementActivityIndex();
            }
        }
    }
}
