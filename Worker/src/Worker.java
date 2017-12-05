import lib.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

public class Worker {

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private ObjectOutputStream out;
    private boolean workerOnline = false;

    public Worker() {
        startWorker();
        System.out.println("Worker main closing...");
    }

    public boolean isOnline() {
        return workerOnline;
    }

    public synchronized void startWorker() {
        System.out.println("Trying to connect with server...");
        doConnections();
        if (workerOnline) {
            System.out.println("Connections established");
            startSearchResultsReceiver();
            System.out.println("Receiver started");
            System.out.println(workerOnline);
        }
    }

    private void startSearchResultsReceiver() {
        WorkerStreamer workerStreamer = new WorkerStreamer(socket, this);
        workerStreamer.start();
    }

    private void doConnections() {
        try {
            InetAddress address = InetAddress.getByName(SERVER_NAME);
            socket = new Socket(address, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(new OtherRequestMessage(MessageType.WORKER, null));
            workerOnline = true;
        } catch ( ConnectException e) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            doConnections();
        } catch ( IOException e ) {
            System.out.println("Worker failed to complete connection with server.");
        }
    }

    public synchronized void searchArticle(Article article, String findStr, int searchActivityID, int WORKER_ID) {
        System.out.println("Searching article");
        boolean occurrenceFound = false;
        SearchedArticle searchedArticle = new SearchedArticle(new ArticleTitle(article.getID(), article.getTitle()));
        int lastIndex = 0;
        String text = article.getTitle().toLowerCase();
        while(lastIndex != -1){
            lastIndex = text.indexOf(findStr.toLowerCase(),lastIndex);
            if(lastIndex != -1){
                occurrenceFound = true;
                searchedArticle.addOccurrence(lastIndex);
                lastIndex += findStr.length();
            }
        }
        lastIndex = 0;
        int titleOffset = searchedArticle.getTitle().length()+2; //+2 é para os dois enter's
        text = article.getBody().toLowerCase();
        while(lastIndex != -1){
            lastIndex = text.indexOf(findStr.toLowerCase(),lastIndex);
            if(lastIndex != -1){
                occurrenceFound = true;
                searchedArticle.addOccurrence(lastIndex + titleOffset);
                lastIndex += findStr.length();
            }
        }
        if (occurrenceFound) {
            System.out.println("    occurrenceFound");
            sendResult(searchedArticle, searchActivityID, WORKER_ID);
        } else
            sendWorkerDisponibleMessage(WORKER_ID);
    }

    public synchronized void sendWorkerDisponibleMessage(int WORKER_ID) {
        try {
            out.writeObject(new SetWorkerDisponibleMessage(WORKER_ID));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendResult(SearchedArticle searchedArticle, int searchActivityID, int WORKER_ID){
        try {
            out.writeObject(new WorkerResultMessage(searchedArticle, searchActivityID, WORKER_ID));
            out.flush();
            System.out.println("      Worker: object sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

