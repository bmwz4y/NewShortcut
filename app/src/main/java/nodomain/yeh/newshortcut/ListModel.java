package nodomain.yeh.newshortcut;

/**
 * Model for each item in the listView allShortcutsListView
 * Created by YEH on 11/19/2015.
 */
public class ListModel {

    private int widgetId = 0;
    private String iconTag = "";
    private String title = "";

    public int getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    public String getIconTag() {
        return iconTag;
    }

    public void setIconTag(String iconTag) {
        this.iconTag = iconTag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}