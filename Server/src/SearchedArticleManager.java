import lib.WorkerResultMessage;

public class SearchedArticleManager extends Thread {

    private Server server;
    private Object message;

    public SearchedArticleManager(Server server, Object message) {
        this.server = server;
        this.message = message;
    }

    @Override
    public void run() {
        server.addResultFromWorker((WorkerResultMessage) message);
    }
}
