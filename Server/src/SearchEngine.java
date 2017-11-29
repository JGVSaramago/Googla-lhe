import lib.*;

import java.io.*;
import java.util.ArrayList;

public class SearchEngine {

    private int searchActivityIDcounter = 0;
    private int workerManagerIDcounter = 0;

    private ArrayList<WorkerManager> workerManagers = new ArrayList<>();

    private ArrayList<Article> articles = new ArrayList<>();
    private volatile ArrayList<SearchActivity> searchActivities = new ArrayList<>();

    private volatile ArrayList<Integer> checkIfRepeatedArticlesSent = new ArrayList<>();


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
        synchronized (searchActivities) {
            if (!searchActivities.isEmpty()) {
                SearchActivity s = searchActivities.get(0);
                int artLeft = s.getArticlesLeft();
                if (artLeft < 0) {
                    if (s.getPendingSearches().isEmpty()) {
                        if (s.getArticlesReceived() >= articles.size())
                            sendToClient(s);
                        // TODO check if there are pending searches
                    }
                    /*if (s.getArticlesReceived() >= articles.size())
                        sendToClient(s);*/
                    return null;
                }
                checkIfRepeatedArticlesSent.add(artLeft);
                s.decrementArticesLeft();
                return new ArticleToSearch(s.getSearchActivityID(), articles.get(artLeft), s.getFindStr());
            }
            return null;
        }
    }

    private synchronized void sendToClient(SearchActivity s) {
        searchActivities.removeIf(sa -> sa.equals(s)); //removes SearchActivity 'sa' if it is equal to the SearchActivity given 's'
        System.out.println("-------Pesquisas no arraylist: "+searchActivities.size());
        int counter = 0;
        for (Integer i: checkIfRepeatedArticlesSent) { //Just to checkIfRepeatedArticlesSent if there is happening repeated searches
            for (Integer e : checkIfRepeatedArticlesSent) {
                if (i.equals(e)) {
                    counter++;
                    if (counter>=2)
                        System.out.println("This should not happen, the same article was searched "+counter+" times");
                }
            }
            counter = 0;
        }
        int count = 0;
        for (SearchedArticle b: s.getResults())
            count+=b.getOccurrencesCount();
        System.out.println("Occurrences found: "+count);
        checkIfRepeatedArticlesSent = new ArrayList<>();
        s.getClient().sendServerMessage( new SearchResultMessage( s.getResults(), s.getOccurrencesFound(), s.getFilesWithOccurrences(), s.getSearchHist()));
    }

    public synchronized void search(String findStr, String[] searchHist, ServerStreamer client) {
        searchActivities.add(new SearchActivity(searchActivityIDcounter++, articles.size()-1, client, findStr, searchHist));
        notifyAll();
    }

    public synchronized void addResultFromWorker(WorkerResultMessage wrm) {
        for (SearchActivity s: searchActivities){
            if (s.getSearchActivityID() == wrm.getSearchActivityID()) {
                s.addResult(wrm.getSearchedArticle());
            }
        }
    }

    public void addPendingSearchToActivity(RequestToWorkerMessage rtwm) {
        for (SearchActivity s: searchActivities){
            if (s.getSearchActivityID() == rtwm.getArticleToSearch().getSearchActivityID()) {
                s.addPendingSearch(rtwm);
            }
        }
    }

    public WorkerManager startWorkerManager(ServerStreamer worker) {
        WorkerManager w = new WorkerManager(workerManagerIDcounter++, worker, this);
        workerManagers.add(w);
        w.start();
        return w;
    }

    public void articleReceived(int searchActivityID) {
        for (SearchActivity s: searchActivities)
            if (s.getSearchActivityID() == searchActivityID)
                s.incrementArticlesReceived();
    }

}
