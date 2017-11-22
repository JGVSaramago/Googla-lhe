import Project.*;
import Project.Client.Client;
import Project.Client.ClientGUI;

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
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("ClientStreamer: Client disconnected from the server.");
                return;
            }
        }
        System.out.println("ClientStreamer: Closing...");
    }
}
