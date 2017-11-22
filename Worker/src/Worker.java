

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Worker {

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

    public SearchedArticle searchArticle(Article article, String findStr) {
        SearchedArticle searchedArticle = new SearchedArticle(new ArticleTitle(article.getID(), article.getTitle()));
        int lastIndex = 0;
        String text = article.getTitle().toLowerCase();
        while(lastIndex != -1){
            lastIndex = text.indexOf(findStr.toLowerCase(),lastIndex);
            if(lastIndex != -1){
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
                searchedArticle.addOccurrence(lastIndex + titleOffset);
                lastIndex += findStr.length();
            }
        }
        return searchedArticle;
    }

}

