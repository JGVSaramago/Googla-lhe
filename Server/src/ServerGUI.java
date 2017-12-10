import lib.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ServerGUI {

    private Server server;
    private JFrame frame;
    private JTextPane textPane;
    private JLabel label;

    public ServerGUI(Server server) {
        this.server = server;
        new ThemeEngine();
        frame = new JFrame("Server");
        frame.setLayout(new BorderLayout());
        buildGUI();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.pack();
        frame.setVisible(true);
    }

    public synchronized void addLogToGUI(String log) {
        textPane.setText(textPane.getText().concat("\n"+log));
    }

    private void buildGUI() {
        buildCenter();
        buildBottomBar();
    }

    private void buildCenter() {
        JPanel panel = new JPanel();
        buildLogDisplay(panel);
        buildOptions(panel);
        frame.add(panel, BorderLayout.CENTER);
    }

    private void buildOptions(JPanel panel) {
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 1));
        JButton cacheBtn = new JButton("Deactivate cache");
        cacheBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (server.isUsingCache()) {
                    server.setUseCache(false);
                    cacheBtn.setText("Activate cache");
                    frame.pack();
                } else {
                    server.setUseCache(true);
                    cacheBtn.setText("Deactivate cache");
                    frame.pack();
                }
            }
        });
        JButton clearCacheBtn = new JButton("Clear cache");
        clearCacheBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.clearServerCache();
                addLogToGUI(server.getDateStamp()+" - Cache cleared.");
            }
        });
        JButton closeWorkersBtn = new JButton("Close all workers");
        closeWorkersBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<ServerStreamer> workers = (ArrayList<ServerStreamer>) server.getWorkersArray().clone();
                for (ServerStreamer worker: workers)
                    worker.closeObject();
            }
        });
        JButton closeClientsBtn = new JButton("Close all clients");
        closeClientsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<ServerStreamer> clients = (ArrayList<ServerStreamer>) server.getClientsArray().clone();
                for (ServerStreamer client: clients)
                    client.closeObject();
            }
        });
        buttonsPanel.add(cacheBtn);
        buttonsPanel.add(clearCacheBtn);
        buttonsPanel.add(closeWorkersBtn);
        buttonsPanel.add(closeClientsBtn);
        panel.add(buttonsPanel);
    }

    private void buildLogDisplay(JPanel panel){
        JLabel logLabel = new JLabel("Logs:");
        logLabel.setBorder(new EmptyBorder(5,5,5,5));
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setMargin(new Insets(3,3,3,3));
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        frame.add(logLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
    }

    private void buildBottomBar(){
        label = new JLabel("Clients connected: "+server.getClientsConnected()+"     Workers connected: "+server.getWorkersConnected());
        label.setBorder(new EmptyBorder(5,5,5,5));
        frame.add(label, BorderLayout.SOUTH);
    }

    public void updateLabel(){
        label.setText("Clients connected: "+server.getClientsConnected()+"     Workers connected: "+server.getWorkersConnected());
    }

}
