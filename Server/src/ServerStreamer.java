import lib.*;

import java.io.*;
import java.net.Socket;

public class ServerStreamer extends Thread{

    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private WorkerManager workerManager; //used if it's a worker connection
    private MessageType connectionType;
    private String username;

    public ServerStreamer(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public WorkerManager getWorkerManager() {
        return workerManager;
    }

    private synchronized void setWorkerDisponible() {
        workerManager.setAvailable();
        if (workerManager.isAvailable()) {
            System.out.println("       Worker " + workerManager.getID() + " now available");
            notify();
        }
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                try {
                    Object message = in.readObject();
                    if (message instanceof WorkerResultMessage) {
                        setWorkerDisponible();
                        System.out.println("         Received result from worker");
                        server.addResultFromWorker((WorkerResultMessage) message);
                    } else if (message instanceof SetWorkerDisponibleMessage) {
                        setWorkerDisponible();
                        workerManager.articleWithNoOccurrences();
                    } else if (message instanceof SearchRequestMessage) {
                        server.doSearch((SearchRequestMessage) message, this);
                    } else if (message instanceof BodyRequestMessage){
                        int articleID = ((BodyRequestMessage) message).getID();
                        System.out.println("BodyRequestReceived for article "+articleID);
                        sendServerMessage(server.getArticleBody(articleID));
                    } else if (message instanceof OtherRequestMessage){
                        OtherRequestMessage msg = ((OtherRequestMessage) message);
                        MessageType type = msg.getType();
                        switch (type){
                            case WORKER:
                                connectionType = type;
                                workerManager = server.addWorker(this);
                                break;
                            case CLIENT:
                                connectionType = type;
                                username = msg.getUsername();
                                server.addClient(this);
                                break;
                            case HISTORY:
                                sendServerMessage(server.getClientHistory((OtherRequestMessage) message));
                                break;
                            case CLEANHISTORY:
                                server.cleanClientHistory(((OtherRequestMessage) message).getUsername());
                                break;
                            case CLOSE:
                                closeConnections();
                                return;
                            default:
                                System.out.println("ServerStreamer: Unknow MessageType received by the server.");
                        }
                    } else
                        System.out.println("ServerStreamer: Unknow message received by the server.");
                } catch (ClassNotFoundException e) {
                    System.out.println("ServerStreamer: Class not found or code different from the one sent.");
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("ServerStreamer: Object not connected anymore.");
            closeConnections();
        }
    }

    private void closeConnections() {
        try {
            socket.close();
            if (socket.isClosed()) {
                if (connectionType.equals(MessageType.CLIENT)) {
                    server.disconnectClient(this);
                    System.out.println("ServerStreamer: Client "+username+" disconnected from server.");
                } else {
                    server.disconnectWorker(this);
                    System.out.println("ServerStreamer: Worker disconnected from server.");
                }
            } else
                System.out.println("ServerStreamer: Unable to close object socket in the server.");
        } catch (IOException e) {
            if (connectionType.equals(MessageType.CLIENT))
                server.disconnectClient(this);
            else
                server.disconnectWorker(this);
            System.out.println("ServerStreamer: Object not properly disconnected from server.");
        }
    }

    public void sendServerMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("ServerStreamer: Tried to send message to a disconnected worker. ");
            closeConnections();
        }
    }
}
