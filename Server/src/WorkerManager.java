import Project.Article;
import Project.ArticleToSearch;

import java.util.ArrayList;
import java.util.Random;

public class WorkerManager extends Thread{

    private ServerStreamer worker;
    private ArrayList<SearchActivity> searchActivities;
    private ArrayList<Article> articles;

    public WorkerManager(ServerStreamer worker, ArrayList<SearchActivity> searchActivities, ArrayList<Article> articles) {
        this.worker = worker;
        this.searchActivities = searchActivities;
        this.articles = articles;
    }

    @Override
    public void run() {
        while (true){
            if (searchActivities.isEmpty()) {
                /*try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                    int activityIndex = new Random().nextInt(searchActivities.size());
                    System.out.println("activityIndex: " + activityIndex);
                    SearchActivity searchActivity = searchActivities.get(activityIndex);
                    if (!searchActivity.isDone()) {
                        System.out.println("Articles left: " + searchActivity.getArticlesLeft());
                        ArticleToSearch articleToSearch = new ArticleToSearch(articles.get(searchActivity.getArticlesLeft()), searchActivity.getFindStr());
                        searchActivity.searchStarted();
                        worker.sendServerMessage(articleToSearch);
                        System.out.println("Enviado para worker");
                    }
            }
        }
    }
}
