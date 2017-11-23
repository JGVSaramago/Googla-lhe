import lib.*;

import java.io.*;
import java.util.ArrayList;

public class SearchEngine {

    private int searchActivityIDcounter = 0;
    private int workerManagerIDcounter = 0;

    private ArrayList<WorkerManager> workerManagers = new ArrayList<>();

    private ArrayList<Article> articles = new ArrayList<>();
    private volatile ArrayList<SearchActivity> searchActivities = new ArrayList<>();


    public SearchEngine() {
        txtToObject(); // Os ficheiros são todos transformados em objetos assim sempre que for feita uma pesquisa não é preciso ir ler tudo outra vez
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
        System.out.println("files: "+filesList.length);
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
        System.out.println("files in array: "+articles.size());
    }

    public ArticleToSearch getArticleToSearch(){
        synchronized (searchActivities){
            if (!searchActivities.isEmpty()){
                SearchActivity s = searchActivities.get(0);
                int artLeft = s.getArticlesLeft();
                if (artLeft < 0){
                    sendToClient(s);
                    return null;
                }
                s.decrementArticesLeft();
                return new ArticleToSearch(s.getID(), articles.get(artLeft), s.getFindStr());
            }
        }
        return null;
    }

    private synchronized void sendToClient(SearchActivity s) {
        searchActivities.removeIf(sa -> sa.equals(s)); //removes SearchActivity 'sa' if it is equal to the SearchActivity given 's'
        s.getClient().sendServerMessage( new SearchResultMessage( s.getResults(), s.getOccurrencesFound(), s.getFilesWithOccurrences(), s.getSearchHist()));
    }

    public synchronized void search(String findStr, String[] searchHist, ServerStreamer client) {
        searchActivities.add(new SearchActivity(searchActivityIDcounter++, articles.size()-1, client, findStr, searchHist));
        notifyAll();
    }

    public synchronized void addResultFromWorker(WorkerResultMessage wrm) {
        for (SearchActivity s: searchActivities){
            if (s.getID() == wrm.getSearchActivityID()) {
                s.addResult(wrm.getSearchedArticle());
                setWorkerDisponible(wrm.getWORKER_ID());
            }
        }
    }

    public synchronized void setWorkerDisponible(int WORKER_ID) {
        for (WorkerManager wm: workerManagers)
            if (wm.getID() == WORKER_ID) {
                wm.setDisponible();
                if (wm.isDisponible())
                    System.out.println("Worker "+WORKER_ID+" now disponible");
            }
    }

    public void startWorkerManager(ServerStreamer worker) {
        WorkerManager w = new WorkerManager(workerManagerIDcounter++, worker, this);
        workerManagers.add(w);
        w.start();
    }

}
