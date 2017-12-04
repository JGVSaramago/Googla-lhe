import lib.*;

import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
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
    private volatile boolean waitingForResult = false;
    private ClientStreamer textReceiver;

    public Client(String CLIENTE_NAME) {
        this.gui = new ClientGUI(this);
        this.CLIENTE_NAME = CLIENTE_NAME;
        startClient();
        System.out.println("Main thread closing...");
    }

    public void disconnect() {
        System.out.println("Disconnecting client from server...");
        clientOnline = false;
        closeConnections();
        System.out.println("Exiting program...");
        System.exit(0);
    }

    public boolean isClientOnline() {
        return clientOnline;
    }

    public void receivedSearchAnswer() {
        waitingForResult = false;
    }

    public void closeConnections() {
            try {
                out.writeObject(new OtherRequestMessage(MessageType.CLOSE, null));
                socket.close();
            } catch (IOException | NullPointerException e) {
                System.out.println("Unable to properly disconnect Client.");
            }
    }

    public void startClient() {
        waitingForResult = false;
        clientOnline = false;
        System.out.println("Trying to connect with server...");
        doConnections();
        if (clientOnline) {
            System.out.println("  Connections established");
            startSearchResultsReceiver();
            getClientHistory();
        }
    }

    private void startSearchResultsReceiver() {
        textReceiver = new ClientStreamer(socket, gui,this);
        textReceiver.start();
        System.out.println("    Receiver started");
    }

    public synchronized void sendSearchRequest(String findStr) {
        try {
            if (clientOnline) {
                if (!waitingForResult) {
                    notifyAll();
                    out.writeObject(new SearchRequestMessage(CLIENTE_NAME, findStr));
                    out.flush();
                    waitingForResult = true;
                    System.out.println("- Search called");
                } else
                    System.out.println("Already waiting for a search result.");
            } else
                System.out.println("Unable to send message, client not connected to the server.");
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
            out.writeObject(new OtherRequestMessage(MessageType.CLIENT, CLIENTE_NAME));
            clientOnline = true;
        } catch ( ConnectException e) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            doConnections();
        } catch ( IOException e ) {
            System.out.println("Client failed to complete connection with server.");
            JOptionPane.showMessageDialog(null, "Client failed to complete connection with server.");
            gui.disposeGUI();
            System.exit(0);
        }
    }

    public  void getClientHistory() {
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
