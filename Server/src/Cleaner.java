import lib.*;

import java.util.ArrayList;

public class Cleaner extends Thread {

    private SearchEngine searchEngine;
    private ArrayList<SearchActivity> searchesCompleted;
    private ArrayList<SearchActivity> toRemove;

    public Cleaner(SearchEngine searchEngine, ArrayList<SearchActivity> searchesCompleted) {
        this.searchesCompleted = searchesCompleted;
        this.searchEngine = searchEngine;
    }

    private synchronized void waitingMode() {
        synchronized (searchEngine){
            try {
                System.out.println("Cleaner going to sleep...");
                searchEngine.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true){

            if (searchesCompleted.isEmpty())
                waitingMode();
            else{
                System.out.println("Ha limpeza a fazer");
                ResultSender sender = new ResultSender(searchesCompleted.get(0));
                sender.start();
                searchesCompleted.remove(searchesCompleted.get(0));
                //toRemove.add(searchActivity);
                System.out.println(searchesCompleted.size());
            }
            //for (SearchActivity s : toRemove)
            //  searchActivities.remove(s);
        }
    }

}