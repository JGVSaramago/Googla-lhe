import Project.*;

import java.io.*;
import java.net.Socket;

public class ServerStreamer extends Thread{

    private Socket socket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ServerStreamer(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                try {
                    Object message = in.readObject();
                    if (message instanceof WorkerResultMessage)
                        server.addResultFromWorker((WorkerResultMessage) message);
                    if (message instanceof SearchRequestMessage) {
                        server.doSearch((SearchRequestMessage) message, this);
                    } else if (message instanceof BodyRequestMessage){
                        System.out.println("BodyRequestReceived");
                        sendServerMessage(server.getArticleBody(((BodyRequestMessage) message).getID()));
                    } else if (message instanceof WorkerRequestMessage){
                        // TODO
                    } else if (message instanceof OtherRequestMessage){
                        switch (((OtherRequestMessage) message).getType()){
                            case WORKER:
                                server.addWorker(this);
                                break;
                            case HISTORY:
                                sendServerMessage(server.getClientHistory((OtherRequestMessage) message));
                                break;
                            case CLEANHISTORY:
                                server.cleanClientHistory(((OtherRequestMessage) message).getUsername());
                                break;
                            case CLOSE:
                                socket.close();
                                if (socket.isClosed()) {
                                    System.out.println("Server: Client disconnected from server.");
                                    server.minusClient();
                                } else
                                    System.out.println("Unable to close client socket in the server.");
                                return;
                            default:
                                System.out.println("Unknow MessageType received by the server.");
                        }
                    } else
                        System.out.println("Unknow message received by the server.");
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Client not properly disconnected from server.");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServerMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
