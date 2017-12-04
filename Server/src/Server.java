import lib.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {

    private SearchEngine searchEngine;
    private ServerGUI gui;
    private static final int PORT = 8080;

    private ArrayList<ServerStreamer> workers = new ArrayList<>();
    private ArrayList<ServerStreamer> clients = new ArrayList<>();

    public Server() {
        gui = new ServerGUI(this);
        searchEngine = new SearchEngine();
        try {
            startServing();
        } catch (IOException e) {
            System.out.println("Constructor: Could not start the server.");
        }
    }

    public int getWorkersConnected() {
        return workers.size();
    }

    public int getClientsConnected() {
        return clients.size();
    }

    public WorkerManager addWorker(ServerStreamer worker) {
        workers.add(worker);
        gui.addLogToGUI(getDateStamp()+" - Worker connected.");
        gui.updateLabel();
        return searchEngine.startWorkerManager(worker);
    }

    public void disconnectWorker(ServerStreamer worker) {
        if (!workers.isEmpty()) {
            worker.getWorkerManager().setOffline();
            workers.remove(worker);
            gui.addLogToGUI(getDateStamp() + " - Worker disconnected.");
            if (workers.isEmpty())
                for (ServerStreamer client : clients)
                    client.sendServerMessage(new ServerUnavailableMessage());
        } else
            System.out.println("Error: Already with 0 workers online.");
        gui.updateLabel();
    }

    public void addClient(ServerStreamer client) {
        clients.add(client);
        gui.addLogToGUI(getDateStamp()+" - Client "+client.getUsername()+" connected.");
        gui.updateLabel();
    }

    public void disconnectClient(ServerStreamer client) {
        if (!clients.isEmpty()) {
            clients.remove(client);
            gui.addLogToGUI(getDateStamp()+" - Client "+client.getUsername()+" disconnected.");
        } else
            System.out.println("Error: Already with 0 clients online.");
        gui.updateLabel();
    }

    private void startServing() throws IOException {
        ServerSocket s = null;
        Socket socket = null;
        try {
            s = new ServerSocket(PORT);
            while (true) {
                socket = s.accept();
                ServerStreamer serverStreamer = new ServerStreamer(socket, this);
                serverStreamer.start();
            }
        } finally { // executa quer existe exception ou nao
            if (socket != null) socket.close();
            if (s != null) s.close();
        }
    }

    public String getDateStamp(){
        return (new SimpleDateFormat("yyyy/MM/dd " + "HH:mm:ss")).format(new Date());
    }

    public void doSearch(SearchRequestMessage searchRequestMessage, ServerStreamer client) {
        String findStr = searchRequestMessage.getFindStr();
        String searchHist = searchRequestMessage.getUsername()+"|"+findStr+"|"+getDateStamp();
        Thread writeToFile = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File("src/history.txt");
                synchronized (file) {
                    file.getParentFile().mkdirs();
                    try {
                        PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true));
                        printWriter.println(searchHist);
                        printWriter.close();
                        System.out.println("write to history complete");
                    } catch (FileNotFoundException e) {
                        System.out.println("History file not found.");
                    }
                }
            }
        });
        writeToFile.start();
        if (workers.size() == 0) {
            client.sendServerMessage(new ServerUnavailableMessage());
            return;
        }
        System.out.println("Server: doSearch");
        searchEngine.search(findStr, searchHist.split("\\|"), client);
    }


    public ClientHistoryMessage getClientHistory(OtherRequestMessage request) {
        File file = new File("src/history.txt");
        file.getParentFile().mkdirs();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            ArrayList<String[]> history = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] lineSplitted = line.split("\\|");
                if (lineSplitted[0].equals(request.getUsername())){
                    history.add(lineSplitted);
                }

            }
            reader.close();
            return new ClientHistoryMessage(history);
        } catch (FileNotFoundException e) {
            System.out.println("History file not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cleanClientHistory(String username) {
        File historyFile = new File("src/history.txt");
        File tempFile = new File("src/tempHistory.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(historyFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.split("\\|")[0].equals(username)) {
                    writer.write(line + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();
            if (!tempFile.renameTo(historyFile))
                System.out.println("Could not rename file");
        } catch (FileNotFoundException e) {
            System.out.println("History file not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object getArticleBody(int id) {
        return searchEngine.getArticleBody(id);
    }

    public synchronized void addResultFromWorker(WorkerResultMessage workerResultMessage){
        searchEngine.addResultFromWorker(workerResultMessage);
    }
}

