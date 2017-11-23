import lib.SearchResultMessage;

public class ResultSender extends Thread {

    private SearchActivity searchActivity;

    public ResultSender(SearchActivity searchActivity) {
        this.searchActivity = searchActivity;
    }

    private synchronized void sendMsg(){
        System.out.println("ResultSender: sending result");
        SearchResultMessage s = new SearchResultMessage(searchActivity.getResults(), searchActivity.getOccurrencesFound(), searchActivity.getFilesWithOccurrences(), searchActivity.getSearchHist());
        searchActivity.getClient().sendServerMessage(s);
        System.out.println("ResultSender: result sent");
    }

    @Override
    public void run() {
        sendMsg();
    }
}
