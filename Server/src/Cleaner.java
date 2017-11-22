import Project.SearchResultMessage;

import java.util.ArrayList;

public class Cleaner extends Thread {

    private ArrayList<SearchActivity> searchActivities;

    public Cleaner(ArrayList<SearchActivity> searchActivities) {
        this.searchActivities = searchActivities;
    }

    @Override
        public void run() {
            while (true){
                for (SearchActivity searchActivity: searchActivities)
                    if (searchActivity.getArticlesLeft() == 0) {
                        Thread sender = new Thread(new Runnable() { // Thread para enviar ao cliente o resultado
                            @Override
                            public void run() {
                                System.out.println("sending result");
                                SearchActivity s = searchActivity;
                                searchActivities.remove(s);
                                s.getClient().sendServerMessage(new SearchResultMessage(searchActivity.getResults(), searchActivity.getOccurrencesFound(), searchActivity.getFilesWithOccurrences(), searchActivity.getSearchHist()));

                            }
                        });
                        sender.start();
                    }

        }
    }



}
