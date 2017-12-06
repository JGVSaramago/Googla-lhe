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
            System.out.println("  Connections established");
            startSearchResultsReceiver();
            System.out.println("    Streamer started");
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
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            doConnections();
        } catch ( IOException e ) {
            System.out.println("  Worker failed to complete connection with server.");
        }
    }

    public synchronized void searchArticle(Article article, String findStr, int searchActivityID) {
        System.out.println("-> Searching article");
        boolean occurrenceFound = false;
        SearchedArticle searchedArticle = new SearchedArticle(new ArticleTitle(article.getID(), article.getTitle()));
        int lastIndex = 0;
        String text = article.getTitle().toLowerCase()+"\n\n"+article.getBody().toLowerCase();
        while(lastIndex != -1){
            lastIndex = text.indexOf(findStr.toLowerCase(),lastIndex);
            if(lastIndex != -1){
                occurrenceFound = true;
                searchedArticle.addOccurrence(lastIndex);
                lastIndex += findStr.length();
            }
        }
        if (occurrenceFound) {
            System.out.println("  - File has occurrences");
            sendResult(searchedArticle, searchActivityID);
        } else
            sendWorkerDisponibleMessage();
    }

    private synchronized void sendWorkerDisponibleMessage() {
        try {
            out.writeObject(new SetWorkerDisponibleMessage());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendResult(SearchedArticle searchedArticle, int searchActivityID){
        try {
            out.writeObject(new WorkerResultMessage(searchedArticle, searchActivityID));
            out.flush();
            System.out.println("    <- Worker: Result sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

