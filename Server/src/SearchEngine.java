import lib.*;

import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SearchEngine {

    //cache searches so if the same search is requested again the SearchEngine already knows the solution
    private boolean useCache = true;
    private final Map<String, CacheSearch> cacheSearchMap = new HashMap<>();

    private Scheduler scheduler;

    private long startTime;
    private long endTime;

    private int searchActivityIDcounter = 0;
    private int workerManagerIDcounter = 0;

    private final ArrayList<WorkerManager> workerManagers = new ArrayList<>();

    private final ArrayList<Article> articles = new ArrayList<>();
    private final ArrayList<SearchActivity> searchActivities = new ArrayList<>();


    public SearchEngine() {
        txtToObject(); // Os ficheiros são todos transformados em objetos assim sempre que for feita uma pesquisa não é preciso ir ler tudo outra vez
        scheduler = new Scheduler(searchActivities);
    }

    public boolean isUsingCache() {
        return useCache;
    }

    public void clearCache() {
        cacheSearchMap.clear();
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    private CacheSearch checkAndGetCache(String findStr) {
        if (!cacheSearchMap.isEmpty() && cacheSearchMap.containsKey(findStr)) {
            return cacheSearchMap.get(findStr);
        }
        return null;
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
        System.out.println("Total files: "+articles.size());
    }

    public ArticleToSearch getArticleToSearch(){
        synchronized (searchActivities) {
            if (!searchActivities.isEmpty()) {
                SearchActivity s = scheduler.next();
                int artLeft = s.getArticlesLeft();
                if (artLeft < 0) {
                    ArrayList<ArticleToSearch> arrATS = s.getPendingSearches();
                    if (arrATS.isEmpty()) {
                        return null;
                    } else {
                        System.out.println("- Searching pending article...");
                        ArticleToSearch ats = arrATS.get(0);
                        arrATS.remove(ats);
                        System.out.println("-- Pending searches left: "+arrATS.size());
                        return ats;
                    }
                } else {
                    s.decrementArticesLeft();
                    return new ArticleToSearch(s.getSearchActivityID(), articles.get(artLeft), s.getFindStr());
                }
            }
            return null;
        }
    }

    private synchronized void sendToClient(SearchActivity s) {
        if (useCache)
            cacheSearchMap.put(s.getFindStr(), new CacheSearch(s.getResults(), s.getOccurrencesFound(), s.getFilesWithOccurrences()));
        searchActivities.removeIf(sa -> sa.equals(s)); //removes SearchActivity 'sa' if it is equal to the SearchActivity given 's'
        endTime = System.nanoTime();
        s.getClient().sendServerMessage( new SearchResultMessage( s.getResults(), s.getOccurrencesFound(), s.getFilesWithOccurrences(), s.getSearchHist()));
        System.out.println("<- Search result sent to client (done in: "+(endTime-startTime)/1E6+"ms, active searches: "+searchActivities.size()+")");
    }

    public synchronized void search(String findStr, String[] searchHist, ServerStreamer client) {
        startTime = System.nanoTime();
        if (useCache) {
            CacheSearch cacheSearch = checkAndGetCache(findStr);
            if (cacheSearch != null) {
                endTime = System.nanoTime();
                System.out.println("Search done in: "+(endTime-startTime)/1E6);
                client.sendServerMessage(new SearchResultMessage(cacheSearch.getResults(), cacheSearch.getOccurrencesFound(), cacheSearch.getFilesWithOccurrences(), searchHist));
                return;
            }
        }
        searchActivities.add(new SearchActivity(searchActivityIDcounter++, articles.size(), client, findStr, searchHist));
        notifyAll();
    }

    public synchronized void addResultFromWorker(WorkerResultMessage wrm) {
        for (SearchActivity s: searchActivities)
            if (s.getSearchActivityID() == wrm.getSearchActivityID())
                if (!s.addResult(wrm.getSearchedArticle())) {
                    sendToClient(s); // esta funcao edita o arraylist por isso quando executada o ciclo foreach deve ser logo parado
                    return; // encontrou a searchActivity pertendida por isso já nao precisa de continuar o ciclo
                }
    }

    public void addPendingSearchToActivity(RequestToWorkerMessage rtwm) {
        for (SearchActivity s: searchActivities){
            if (s.getSearchActivityID() == rtwm.getArticleToSearch().getSearchActivityID()) {
                s.addPendingSearch(rtwm.getArticleToSearch());
                return; // encontrou a searchActivity pertendida por isso já nao precisa de continuar o ciclo
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
                if (!s.incrementArticlesReceived()) { //devolve false se o numero de articos recebidos for igual ao de enviados
                    sendToClient(s);
                    return; // encontrou a searchActivity pertendida por isso já nao precisa de continuar o ciclo
                }

    }

}
