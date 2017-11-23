import lib.*;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

public class SearchEngine {
    private int searchActivityIDcounter = 0;
    private ArrayList<Article> articles;
    private ArrayList<SearchActivity> searchActivities;
    private  ArrayList<SearchActivity> searchesCompleted;
    private boolean lock = false;
    private Cleaner cleaner;

    public SearchEngine() {
        articles = new ArrayList<>();
        searchActivities = new ArrayList<>();
        searchesCompleted = new ArrayList<>();
        txtToObject(); // Os ficheiros são todos transformados em objetos assim sempre que for feita uma pesquisa não é preciso ir ler tudo outra vez
        cleaner = new Cleaner(this, searchesCompleted);
        cleaner.start();
    }

    public boolean isLock() {
        return lock;
    }

    public boolean lock() {
        if (!lock)
            return lock = true;
        else
            return false;
    }

    public void unlock() {
        lock = false;
    }

    public synchronized void searchCompleted(SearchActivity searchActivity){
        searchesCompleted.add(searchActivity);
        searchActivities.remove(searchActivity);
        notifyAll();
        System.out.println("searches completed: "+searchesCompleted.size());
    }

    public ArrayList<Article> getArticles() {
        return articles;
    }

    public ArrayList<SearchActivity> getSearchActivities() {
        return searchActivities;
    }

    public ArticleBody getArticleBody(int id){
        return new ArticleBody(id, articles.get(id).getBody());
    }

    private void txtToObject() {
        File dir = new File("news29out");
        File[] filesList = dir.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file.getPath()));
                } catch (FileNotFoundException e) {
                    System.out.println("txtToObject(): Unable to open the file "+file.getPath());
                }
                try {
                    String line;
                    String title = null;
                    String history = null;
                    for (int l=0; (line=reader.readLine())!=null; l++){
                        if (l==0)
                            title = line;
                        else {
                            if (history == null)
                                history = line;
                            else
                                history.concat(line);
                        }
                    }
                    Article article = new Article(articles.size(), title, history);
                    articles.add(article);
                } catch (IOException e) {
                    System.out.println("txtToObject(): Unable to read the file "+file.getPath());
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.out.println("txtToObject(): Unable to close the file "+file.getPath());
                    }
                }
            }
        } else {
            System.out.println("No files in the directory.");
        }
    }

    public synchronized void search(String findStr, String[] searchHist, ServerStreamer client) {
        searchActivities.add(new SearchActivity(searchActivityIDcounter,articles.size()-1, client, findStr, searchHist));
        searchActivityIDcounter++;
        this.notifyAll();
        System.out.println("added search to arraylist");
    }

    public synchronized void addResultFromWorker(WorkerResultMessage workerResultMessage) {
        for (SearchActivity s: searchActivities){
            if (s.getID() == workerResultMessage.getSearchActivityID())
                System.out.println("SearchEngine: adding result");
                s.searchDone(workerResultMessage.getSearchedArticle());
        }
    }

    public void startWorkerManager(ServerStreamer worker) {
        new WorkerManager(this, worker).start();
    }

}
