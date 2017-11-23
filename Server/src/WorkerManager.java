import lib.ArticleToSearch;
import lib.RequestToWorkerMessage;

public class WorkerManager extends Thread {

    private final int ID;
    private volatile boolean disponible = true;
    private volatile boolean workerOnline = true;
    private ServerStreamer worker;
    private SearchEngine searchEngine;

    public WorkerManager(int ID, ServerStreamer worker, SearchEngine searchEngine) {
        this.ID = ID;
        this.worker = worker;
        this.searchEngine = searchEngine;
    }

    public int getID() {
        return ID;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible() {
        disponible = true;
    }

    private void waitingMode() {
        synchronized (searchEngine) {
            try {
                System.out.println("WorkerManager " + ID + ": going to sleep...");
                searchEngine.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void sendSearchToWorker(ArticleToSearch a, int id) {
        worker.sendServerMessage(new RequestToWorkerMessage(a, id));
        System.out.println("WorkerManeger "+ID+": sent search to worker ");
    }

    @Override
    public void run() {
        while (workerOnline){
            if (disponible) { //se o worker nao estiver ocupado
                ArticleToSearch a = searchEngine.getArticleToSearch();
                if (a == null) //se nao houver artigos para pesquisar
                    waitingMode();
                else { //pesquisa artigo recebido
                    disponible = false;
                    sendSearchToWorker(a, ID);
                }
            }
        }
    }
}
