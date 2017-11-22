import Project.*;

import java.io.*;
import java.util.ArrayList;

public class SearchEngine {
    private ArrayList<Article> articles;
    private ArrayList<SearchActivity> searchActivities;

    public SearchEngine() {
        articles = new ArrayList<>();
        searchActivities = new ArrayList<>();
        txtToObject(); // Os ficheiros são todos transformados em objetos assim sempre que for feita uma pesquisa não é preciso ir ler tudo outra vez
        Cleaner cleaner = new Cleaner(searchActivities);
        cleaner.start();
    }

    public ArticleBody getArticleBody(int id){
        return new ArticleBody(id, articles.get(id).getBody());
    }

    private void txtToObject() {
        File dir = new File("news29out");
        File[] filesList = dir.listFiles();
        if (filesList != null) {
            for (File file : filesList) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(file.getPath()));
                } catch (FileNotFoundException e) {
                    System.out.println("txtToObject(): Unable to open the file "+file.getPath());
                }
                try {
                    String line;
                    String title = null;
                    String history = null;
                    for (int l=0; (line=reader.readLine())!=null; l++){
                        if (l==0)
                            title = line;
                        else {
                            if (history == null)
                                history = line;
                            else
                                history.concat(line);
                        }
                    }
                    Article article = new Article(articles.size(), title, history);
                    articles.add(article);
                } catch (IOException e) {
                    System.out.println("txtToObject(): Unable to read the file "+file.getPath());
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.out.println("txtToObject(): Unable to close the file "+file.getPath());
                    }
                }
            }
        } else {
            System.out.println("No files in the directory.");
        }
    }

    public void search(String findStr, String[] searchHist, ServerStreamer client) {
        searchActivities.add(new SearchActivity(articles.size()-1, client, findStr, searchHist));
        System.out.println("added search to arraylist");
    }

    public void addResultFromWorker(WorkerResultMessage workerResultMessage) {
        for (SearchActivity s: searchActivities){
            if (s.equals(workerResultMessage.getSearchActivity()))
                s.searchDone(workerResultMessage.getSearchedArticle());
        }
    }

    public void startWorkerManager(ServerStreamer worker) {
        new WorkerManager(worker,searchActivities,articles).start();
    }

}
