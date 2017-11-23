import lib.*;

import javax.swing.*;
import java.awt.*;

/***
 * Used only to display the Project.SearchedArticle title in the JList<Project.SearchedArticle>, otherwise the object reference would be showing on the JList
 */

public class NewsArticleRenderer extends DefaultListCellRenderer {

    public NewsArticleRenderer() {   }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        SearchedArticle l = (SearchedArticle) value;
        setText(l.getOccurrencesCount()+" - "+l.getTitle());
        return this;
    }

}


