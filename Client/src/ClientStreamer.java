import lib.ArticleBody;
import lib.ClientHistoryMessage;
import lib.OtherRequestMessage;
import lib.SearchResultMessage;

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
                    System.out.println("Received search result");
                    gui.showSearchResults((SearchResultMessage) message);
                } else if (message instanceof ArticleBody){
                    gui.setArticleBody((ArticleBody) message);
                } else if (message instanceof ClientHistoryMessage){
                    gui.createClientHistory(((ClientHistoryMessage) message).getClientHistory());
                } else if (message instanceof OtherRequestMessage){
                    switch (((OtherRequestMessage) message).getType()){
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                System.out.println("ClientStreamer: Client disconnected from the server.");
                return;
            } catch (ClassNotFoundException e) {
                System.out.println("ClientStreamer: Class unknown or not the same as the one in the server.");
            }
        }
        System.out.println("ClientStreamer: Closing...");
    }
}
