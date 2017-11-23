package lib;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchResultMessage implements Serializable{

    private ArrayList<SearchedArticle> searchResults;
    private int occurrencesFound;
    private int filesWithOccurrences;
    private String[] searchHist;

    public SearchResultMessage(ArrayList<SearchedArticle> searchResults, int occurrencesFound, int filesWithOccurrences, String[] searchHist) {
        this.searchResults = searchResults;
        this.occurrencesFound = occurrencesFound;
        this.filesWithOccurrences = filesWithOccurrences;
        this.searchHist = searchHist;
    }

    public ArrayList<SearchedArticle> getSearchResults() {
        return searchResults;
    }

    public int getOccurrencesFound() {
        return occurrencesFound;
    }

    public int getFilesWithOccurrences() {
        return filesWithOccurrences;
    }

    public String[] getSearchHist() {
        return searchHist;
    }
}
