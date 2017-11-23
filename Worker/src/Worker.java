import lib.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Worker {

    private int counter = 0;
    private int counter2 = 0;
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private ObjectOutputStream out;
    private boolean workerOnline = false;

    public Worker() {
        try {
            startClient();
        } finally {
            if (workerOnline) closeConnections();
        }
    }

    public boolean isOnline() {
        return workerOnline;
    }

    public void resetCounters(){
        counter = 0;
        counter2 = 0;
    }

    private void closeConnections() {
        try {
            socket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Unable to disconnect Worker.");
        }
    }

    private void startClient() {
        doConnections();
        if (workerOnline) {
            System.out.println("Connections established");
            startSearchResultsReceiver();
            System.out.println("Receiver started");
            while (workerOnline) {}
            closeConnections();
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
        } catch ( IOException e ) {
            System.out.println("Worker failed to connect with server.");
        }
    }

    public void searchArticle(Article article, String findStr, int searchActivityID, int WORKER_ID) {
        System.out.println("Searching article");
        boolean occurrenceFound = false;
        SearchedArticle searchedArticle = new SearchedArticle(new ArticleTitle(article.getID(), article.getTitle()), searchActivityID);
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
        int titleOffset = searchedArticle.getTitle().length()+2; //+2 é para os dois enter
        text = article.getBody().toLowerCase();
        while(lastIndex != -1){
            lastIndex = text.indexOf(findStr.toLowerCase(),lastIndex);
            if(lastIndex != -1){
                occurrenceFound = true;
                searchedArticle.addOccurrence(lastIndex + titleOffset);
                lastIndex += findStr.length();
            }
        }
        System.out.println("  Searched "+(++counter)+" articles.");
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
            System.out.println("        Sent "+(++counter2)+" articles.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

