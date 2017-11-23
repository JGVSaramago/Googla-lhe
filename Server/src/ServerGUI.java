import lib.*;

import javax.swing.*;
import java.awt.*;

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

    public void addLogToGUI(String log) {
        textPane.setText(textPane.getText().concat("\n"+log));
    }

    private void buildGUI() {
        //buildMenu();
        buildCenter();
        buildBottomBar();
    }

    private void buildCenter(){
        JLabel logLabel = new JLabel("Logs:");
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setMargin(new Insets(10,3,10,3));
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        frame.add(logLabel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void buildBottomBar(){
        label = new JLabel("Clients connected: "+server.getClientsConnected());
        frame.add(label, BorderLayout.SOUTH);
    }

    public void updateLabel(){
        label.setText("Clients connected: "+server.getClientsConnected());
    }

}
