import lib.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientStreamer extends Thread {

    private ClientGUI gui;
    private Client client;
    private Socket socket;
    private ObjectInputStream in;

    public ClientStreamer(Socket socket, ClientGUI gui, Client client) {
        this.gui = gui;
        this.client = client;
        this.socket = socket;
        setupReceiver();
    }

    private void setupReceiver(){
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (client.isClientOnline()){
            try {
                Object message = in.readObject();
                if (message instanceof SearchResultMessage) {
                    System.out.println("-> Received search result");
                    client.receivedSearchAnswer();
                    gui.showSearchResults((SearchResultMessage) message);
                } else if (message instanceof ArticleBody){
                    gui.setArticleBody((ArticleBody) message);
                } else if (message instanceof ClientHistoryMessage){
                    gui.createClientHistory(((ClientHistoryMessage) message).getClientHistory());
                } else if (message instanceof ServerUnavailableMessage){
                    client.receivedSearchAnswer();
                    System.out.println("ClientStreamer: Server unavailable.");
                }
            } catch (IOException e) {
                System.out.println("ClientStreamer: Client disconnected from the server.");
                if (client.isClientOnline())
                    client.startClient();
                break;
            } catch (ClassNotFoundException e) {
                System.out.println("ClientStreamer: Class unknown or not the same as the one in the server.");
            }
        }
        System.out.println("ClientStreamer: Closing...");
    }
}
