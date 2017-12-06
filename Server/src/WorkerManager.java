import lib.ArticleToSearch;
import lib.RequestToWorkerMessage;

public class WorkerManager extends Thread {

    private final int ID;
    private volatile boolean available = true;
    private volatile boolean workerOnline = true;
    private final ServerStreamer worker;
    private final SearchEngine searchEngine;
    private int lastSearchActivityID;

    private RequestToWorkerMessage lastMessageSent; //if worker is killed during search this will go to an unfinished searches so be searched by other worker

    public WorkerManager(int ID, ServerStreamer worker, SearchEngine searchEngine) {
        this.ID = ID;
        this.worker = worker;
        this.searchEngine = searchEngine;
    }

    public int getID() {
        return ID;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable() {
        available = true;
    }

    public void articleWithNoOccurrences() {
        searchEngine.articleReceived(lastSearchActivityID);
    }

    public void setOffline() {
        workerOnline = false;
    }

    private void waitForArticles() {
        synchronized (searchEngine) {
            try {
                System.out.println("WorkerManager " + ID + ": No articles to search, going to sleep...");
                searchEngine.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForWorker() {
        synchronized (worker) {
            try {
                System.out.println("     WorkerManager " + ID + ": Worker busy, going to sleep...");
                worker.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void sendSearchToWorker(ArticleToSearch a) {
        lastMessageSent = new RequestToWorkerMessage(a);
        worker.sendServerMessage(lastMessageSent);
        System.out.println("  WorkerManeger "+ID+": sent search to worker ");
    }

    @Override
    public void run() {
        while (workerOnline){
            if (available) { //se o worker nao estiver ocupado
                ArticleToSearch a = searchEngine.getArticleToSearch();
                if (a == null) //se nao houver artigos para pesquisar
                    waitForArticles();
                else { //pesquisa artigo recebido
                    available = false;
                    lastSearchActivityID = a.getSearchActivityID();
                    sendSearchToWorker(a);
                }
            } else
                waitForWorker();
        }
        if (!available) //worker isn't online and server didn't got the answer after sending article to be searched
            searchEngine.addPendingSearchToActivity(lastMessageSent);
        System.out.println("WorkerManager "+ID+" closing...");
    }

}
