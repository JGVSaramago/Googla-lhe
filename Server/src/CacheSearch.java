import lib.SearchedArticle;

import java.util.ArrayList;

public class CacheSearch {

    private final ArrayList<SearchedArticle> results;
    private final int occurrencesFound;
    private final int filesWithOccurrences;

    public CacheSearch(ArrayList<SearchedArticle> results, int occurrencesFound, int filesWithOccurrences) {
        this.results = results;
        this.occurrencesFound = occurrencesFound;
        this.filesWithOccurrences = filesWithOccurrences;
    }

    public ArrayList<SearchedArticle> getResults() {
        return results;
    }

    public int getOccurrencesFound() {
        return occurrencesFound;
    }

    public int getFilesWithOccurrences() {
        return filesWithOccurrences;
    }
}
