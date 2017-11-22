import Project.*;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Client {

    private ClientGUI gui;
    private final String CLIENTE_NAME;
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private ObjectOutputStream out;
    private volatile boolean clientOnline = false;

    public Client(String CLIENTE_NAME) {
        this.gui = new ClientGUI(this);
        this.CLIENTE_NAME = CLIENTE_NAME;
        startClient();
        closeConnections();
    }

    public void setOffline() {
        clientOnline = false;
        System.out.println("setOffline()");
    }

    public boolean isClientOnline() {
        return clientOnline;
    }

    public void closeConnections() {
        try {
            System.out.println("closeConnections()");
            out.writeObject(new OtherRequestMessage(MessageType.CLOSE, null));
            socket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Unable to properly disconnect Client.");
        }
    }

    private void startClient() {
        doConnections();
        if (clientOnline) {
            System.out.println("Connections established");
            startSearchResultsReceiver();
            System.out.println("Receiver started");
            getClientHistory();
            while (clientOnline);
            System.out.println("offline");
        }
    }

    private void startSearchResultsReceiver() {
        ClientStreamer textReceiver = new ClientStreamer(socket, gui,this);
        textReceiver.start();
    }

    public void sendSearchRequest(String findStr) {
        try {
            out.writeObject(new SearchRequestMessage(CLIENTE_NAME, findStr));
        } catch (IOException | NullPointerException e) {
            System.out.println("sendSearchRequest(): Unable to send request, check connection to the server.");
            JOptionPane.showMessageDialog(null,"sendSearchRequest(): Unable to send request, check connection to the server.");
        }
    }

    private void doConnections() {
        try {
            InetAddress address = InetAddress.getByName(SERVER_NAME);
            socket = new Socket(address, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            clientOnline = true;
        } catch ( IOException e ) {
            System.out.println("Client failed to connect with server.");
            JOptionPane.showMessageDialog(null, "Client failed to connect with server.");
            gui.disposeGUI();
            System.exit(0);
        }
    }

    public void getClientHistory() {
        try {
            out.writeObject(new OtherRequestMessage(MessageType.HISTORY, CLIENTE_NAME));
        } catch (IOException | NullPointerException e) {
            System.out.println("getClientHistory(): Unable to send request, check connection to the server.");
            JOptionPane.showMessageDialog(null,"getClientHistory(): Unable to send request, check connection to the server.");
        }
    }

    public void cleanClientHistory() {
        try {
            out.writeObject(new OtherRequestMessage(MessageType.CLEANHISTORY, CLIENTE_NAME));
        } catch (IOException | NullPointerException e) {
            System.out.println("cleanClientHistory(): Unable to send request, check connection to the server.");
            JOptionPane.showMessageDialog(null,"cleanClientHistory(): Unable to send request, check connection to the server.");
        }
    }

    public void requestArticleBody(int id) {
        try {
            out.writeObject(new BodyRequestMessage(id));
        } catch (IOException | NullPointerException e) {
            System.out.println("requestArticleBody(): Unable to send request, check connection to the server.");
            JOptionPane.showMessageDialog(null,"requestArticleBody(): Unable to send request, check connection to the server.");
        }
    }
}
