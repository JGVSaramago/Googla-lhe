import lib.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

import static lib.ThemeEngine.setDarkTheme;
import static lib.ThemeEngine.setLightTheme;

public class ClientGUI {

    private Client client;
    private JFrame frame;
    private JList<SearchedArticle> newsList;
    private JTextPane newsViewer;
    private JLabel occurrencesFound;
    private JLabel filesWithOccurrences;
    private ListSelectionListener listSelectionListener;
    private JTextField searchInput;
    private volatile ArticleBody articleBody;

    private Color highlighterColor = new Color(0xFFB300);
    private JMenu historyMenu;
    private ArrayList<String[]> clientHistory;
    private JButton searchButton;
    private String findStr;

    public ClientGUI(Client client) {
        new ThemeEngine();
        this.client = client;
        frame = new JFrame("Googla-lhe");
        frame.setLayout(new BorderLayout());
        buildGUI();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                client.disconnect();
                disposeGUI();
            }
        });
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.pack();
        searchButton.requestFocusInWindow();
        frame.setVisible(true);
    }

    private void setOccurrences(int occurrencesFound, int filesWithOccurrences) {
        this.filesWithOccurrences.setText("Files with occurrences: " + filesWithOccurrences);
        this.occurrencesFound.setText("   Occurrences found: " + occurrencesFound);
    }

    public void setArticleBody(ArticleBody articleBody) {
        this.articleBody = articleBody;
    }

    public void disposeGUI() {
        frame.dispose();
    }

    private void buildGUI() {
        buildMenu();
        buildSearchBar();
        buildCenter();
        buildBottomBar();
    }

    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Googla-lhe");
        mainMenu.setFont(mainMenu.getFont().deriveFont(Font.BOLD));
        JMenu menuItem = new JMenu("Order by");
        JMenuItem occurrencesNo = new JMenuItem("Occurrences number");
        JMenuItem name = new JMenuItem("Name");
        JMenuItem date = new JMenuItem("Date");
        menuItem.add(occurrencesNo);
        menuItem.add(name);
        menuItem.add(date);
        mainMenu.add(menuItem);

        JMenu appearanceMenu = new JMenu("Appearance");
        JMenuItem highlighterColorItem = new JMenuItem("Highlighter Color");
        highlighterColorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color backup = highlighterColor;
                if ( (highlighterColor = JColorChooser.showDialog(null, "Highlighter Color Chooser", highlighterColor)) == null)
                    highlighterColor = backup;
                SearchedArticle s = newsList.getSelectedValue();
                if (s != null) highlightArticle(s, articleBody);
            }
        });
        JCheckBoxMenuItem darkModeItem = new JCheckBoxMenuItem("Dark Mode");
        darkModeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchedArticle s = newsList.getSelectedValue();
                if (!darkModeItem.getState()) {
                    setLightTheme();
                    SwingUtilities.updateComponentTreeUI(frame);
                    if (s != null) highlightArticle(s, articleBody);
                } else {
                    setDarkTheme();
                    SwingUtilities.updateComponentTreeUI(frame);
                    if (s != null) highlightArticle(s, articleBody);
                }
            }
        });
        appearanceMenu.add(highlighterColorItem);
        appearanceMenu.add(darkModeItem);

        historyMenu = new JMenu("History");
        JLabel label = new JLabel("Recent History");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.BLACK);
        label.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 0));
        historyMenu.add(label);

        menuBar.add(mainMenu);
        menuBar.add(appearanceMenu);
        menuBar.add(historyMenu);
        frame.setJMenuBar(menuBar);
    }

    private void buildCenter() {
        JPanel panel = new JPanel(new GridLayout(1,0));
        builListViewer(panel);
        buildNewsViewer(panel);
        frame.add(panel, BorderLayout.CENTER);
    }

    private void buildSearchBar() {
        JPanel panel = new JPanel();
        searchInput = new JTextField();
        searchInput.setText("Insert you search here...");
        searchInput.setMargin(new Insets(2,5,2,5));
        searchInput.setColumns(30);
        searchInput.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchInput.getText().equals("Insert you search here...")) {
                    searchInput.setText("");
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        panel.add(searchInput);
        panel.add(createSearchButton(searchInput));
        frame.add(panel, BorderLayout.NORTH);
    }

    private JButton createSearchButton(JTextField searchInput) {
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findStr = searchInput.getText();
                client.sendSearchRequest(findStr);
            }
        });
        frame.getRootPane().setDefaultButton(searchButton);
        return searchButton;
    }

    private void builListViewer(JPanel panel) {
        newsList = new JList<>();
        newsList.setLayoutOrientation(JList.VERTICAL);
        ListCellRenderer newsArticleRenderer = new NewsArticleRenderer();
        newsList.setCellRenderer(newsArticleRenderer);
        addListSelectionListener();
        JScrollPane scrollPane = new JScrollPane(newsList);
        scrollPane.setPreferredSize(new Dimension(600, 600));
        panel.add(scrollPane);
    }

    private void buildNewsViewer(JPanel panel) {
        newsViewer = new JTextPane();
        newsViewer.setMargin(new Insets(10,7,10,7));
        JScrollPane scrollPane = new JScrollPane(newsViewer);
        scrollPane.setPreferredSize(new Dimension(600, 600));
        newsViewer.setEditable(false);
        panel.add(scrollPane);
    }

    private void buildBottomBar() {
        JPanel panel = new JPanel();
        filesWithOccurrences = new JLabel("Files with occurrences: ");
        occurrencesFound = new JLabel("   Occurrences found: ");
        panel.add(filesWithOccurrences);
        panel.add(occurrencesFound);
        frame.add(panel, BorderLayout.SOUTH);
    }

    public void showSearchResults(SearchResultMessage searchResultMessage) {
        newsViewer.setText("");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultListModel<SearchedArticle> list = new DefaultListModel<>();
                ArrayList<SearchedArticle> results = searchResultMessage.getSearchResults();
                results.sort(new Comparator<SearchedArticle>() {
                    @Override
                    public int compare(SearchedArticle o1, SearchedArticle o2) {
                        return o1.getOccurrencesCount()-o2.getOccurrencesCount();
                    }
                });
                for (SearchedArticle searchedArticle : results)
                    list.addElement(searchedArticle);
                newsList.removeListSelectionListener(listSelectionListener);
                newsList.setModel(list);
                addListSelectionListener();
                setOccurrences(searchResultMessage.getOccurrencesFound(), searchResultMessage.getFilesWithOccurrences());
            }
        });
        updateClientHistory(searchResultMessage.getSearchHist());
    }

    private void highlighter(int start, int end) {
        DefaultHighlighter highlighter = (DefaultHighlighter) newsViewer.getHighlighter();
        DefaultHighlighter.DefaultHighlightPainter hPainter = new DefaultHighlighter.DefaultHighlightPainter(highlighterColor);
        try {
            highlighter.addHighlight(start, end, hPainter);
        } catch (BadLocationException e) {
            System.out.println("highlighter(): Unable to highlighter location.");
        }
    }

    private void highlightArticle(SearchedArticle selectedArticle, ArticleBody articleBody){
        newsViewer.setText(selectedArticle.getTitle()+"\n\n"+articleBody.getBody());
        for (Integer i : selectedArticle.getOccurrences()) {
            highlighter(i, i + findStr.length());
        }
    }

    public void addListSelectionListener() {
        newsList.addListSelectionListener(listSelectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    SearchedArticle selectedArticle = newsList.getSelectedValue();
                    articleBody = null;
                    client.requestArticleBody(selectedArticle.getID());
                    while (articleBody == null);
                    highlightArticle( selectedArticle, articleBody);
                }
            }
        });
    }

    public void createClientHistory(ArrayList<String[]> clientHist) {
        clientHistory = clientHist;
        if (!clientHist.isEmpty()) {
            int arraysize = clientHistory.size();
            int count = 0;
            for (int i = arraysize - 1; i>=0 && count < 8; i--) {
                String[] line = clientHistory.get(i);
                JMenuItem item = new JMenuItem(line[2] + ":  \"" + line[1] + "\"");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        findStr = line[1];
                        client.sendSearchRequest(findStr);
                        System.out.println("Search called");
                    }
                });
                historyMenu.add(item);
                count++;
            }
        }
        JMenuItem item = new JMenuItem("Show full history...");
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        item.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 0));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFullHistory();
            }
        });
        historyMenu.add(item);
    }

    private void updateClientHistory(String[] searchHist){
        JMenuItem item = new JMenuItem(searchHist[2]+":  \""+searchHist[1]+"\"");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendSearchRequest(searchHist[1]);
                System.out.println("Search called");
            }
        });
        historyMenu.add(item, 1);
        clientHistory.add(searchHist);
        if (clientHistory.size()>8)
            historyMenu.remove(9);
    }

    private void showFullHistory() {
        JFrame historyFrame = new JFrame();
        historyFrame.setLayout(new BorderLayout());
        JLabel label = new JLabel("Full history:");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(15, 10, 7, 0));
        JList<String> list = new JList<>();
        list.setLayoutOrientation(JList.VERTICAL);

        DefaultListModel<String> model = new DefaultListModel<>();
        int arraysize = clientHistory.size();
        for (int i = arraysize-1; i >= 0; i--) {
            String[] line = clientHistory.get(i);
            model.addElement(line[2]+":  \""+line[1]+"\"");
        }
        list.setModel(model);

        list.addListSelectionListener(listSelectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String findStr = list.getSelectedValue().split("[\"']")[1];
                    client.sendSearchRequest(findStr);
                    searchInput.setText(findStr);
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(400, 500));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton cleanHist = new JButton("Clean History");
        cleanHist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanHistory();
                list.setModel(new DefaultListModel<>());
            }
        });

        historyFrame.add(label, BorderLayout.NORTH);
        historyFrame.add(scrollPane, BorderLayout.CENTER);
        historyFrame.add(cleanHist, BorderLayout.SOUTH);
        historyFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        historyFrame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        historyFrame.setLocation(dim.width/2-historyFrame.getSize().width/2, dim.height/2-historyFrame.getSize().height/2);
        historyFrame.setVisible(true);
    }

    private void cleanHistory() {
        client.cleanClientHistory();
        clientHistory = new ArrayList<>();
        historyMenu.removeAll();
        JLabel label = new JLabel("Recent History");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.BLACK);
        label.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 0));
        historyMenu.add(label);
        JMenuItem item = new JMenuItem("Show full history...");
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        item.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 0));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFullHistory();
            }
        });
        historyMenu.add(item);
        System.out.println("History clean request sent");

    }
}
