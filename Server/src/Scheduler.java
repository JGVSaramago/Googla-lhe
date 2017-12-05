import java.util.ArrayList;

public class Scheduler {

    private volatile ArrayList<SearchActivity> searchActivities;
    private volatile SearchActivity searchActivity;

    public Scheduler(ArrayList<SearchActivity> searchActivities) {
        this.searchActivities = searchActivities;
    }

    //returns actual searchActivity or if there are no searches returns null
    public SearchActivity actual() {
        return searchActivity;
    }

    public SearchActivity next() {
        if (searchActivities.isEmpty())
            searchActivity = null;
        else if (searchActivity == null)
            searchActivity = searchActivities.get(0);
        else {
            int actualID = searchActivities.indexOf(searchActivity);
            if (actualID+1 >= searchActivities.size())
                searchActivity = searchActivities.get(0);
            else
                searchActivity = searchActivities.get(actualID+1);
        }
        return searchActivity;
    }

}
