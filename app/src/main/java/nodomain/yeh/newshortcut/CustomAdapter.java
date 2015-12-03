package nodomain.yeh.newshortcut;

/**
 * custom adapter for listView allShortcutsListView
 * Created by someone else edited by YEH.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/*********
 * Adapter class extends with BaseAdapter and implements with OnClickListener
 ************/
public class CustomAdapter extends BaseAdapter {
    public static int customIconWidgetId;
    public static int customIconViewPosition;

    /***********
     * Declare Used Variables
     *********/
    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater = null;
    public Resources res;
    ListModel tempModelValues = null;

    /*************
     * CustomAdapter Constructor
     *****************/
    public CustomAdapter(Activity a, ArrayList d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data = d;
        res = resLocal;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /********
     * What is the size of Passed Arraylist Size
     ************/
    public int getCount() {

        if (data.size() <= 0) {
            return 0;
        }
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /*********
     * Create a holder Class to contain inflated xml file elements
     *********/
    public static class ViewHolder {

        public ImageView iconImage;
        public TextView title;
        public Button editName;
        public Button selectIcon;
        public Button customIcon;
    }

    /******
     * Depends upon data size called for each row , Create each ListView row
     *****/
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            convertView = inflater.inflate(R.layout.tab_item, parent, false);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.iconImage = (ImageView) convertView.findViewById(R.id.inAppIconImageView);
            holder.title = (TextView) convertView.findViewById(R.id.inAppWidget_name_textView);
            holder.editName = (Button) convertView.findViewById(R.id.editNameField_button);
            holder.selectIcon = (Button) convertView.findViewById(R.id.selectIcon_button);
            holder.customIcon = (Button) convertView.findViewById(R.id.customIcon_button);

            /************  Set holder with LayoutInflater ************/
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        if (data.size() > 0) {
            /***** Get each Model object from Arraylist ********/
            tempModelValues = null;
            tempModelValues = (ListModel) data.get(position);

            // to save the widget id corresponding to the specific row
            final int widgetId = tempModelValues.getWidgetId();

            /************  Set Model values in Holder elements ***********/

            String iconTag = tempModelValues.getIconTag();
            // test if the iconTag is a file path
            if (iconTag.contains("/")) {
                // to force image cache reset
                holder.iconImage.setImageURI(Uri.parse(""));
                // view thumbnail
                holder.iconImage.setImageURI(Uri.parse(iconTag));
            } else {
                holder.iconImage.setImageResource(TheWidget.iconIdOf(iconTag));
            }

            holder.title.setText(tempModelValues.getTitle());

            holder.editName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // shows a dialog fragment that edits the name of the corresponding widget
                    FragmentManager fm = activity.getFragmentManager();
                    EditNameDialog editNameDialog = new EditNameDialog();
                    Bundle bundle = new Bundle();
                    bundle.putInt("WIDGET_ID", widgetId);
                    bundle.putInt("POSITION", position);
                    editNameDialog.setArguments(bundle);
                    editNameDialog.show(fm, "fragment_edit_name");
                }
            });

            holder.selectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // shows a dialog fragment to select the icon of the corresponding widget
                    FragmentManager fm = activity.getFragmentManager();
                    SelectIconDialog selectIconDialog = new SelectIconDialog();
                    Bundle bundle = new Bundle();
                    bundle.putInt("WIDGET_ID", widgetId);
                    bundle.putInt("POSITION", position);
                    selectIconDialog.setArguments(bundle);
                    selectIconDialog.show(fm, "fragment_select_icon");
                }
            });

            holder.customIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //using static members as passers for data for now
                    customIconWidgetId = widgetId;
                    customIconViewPosition = position;

                    // shows a dialog fragment to select an image for the icon of the corresponding widget
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, res.getString(R.string.customIcon_chooserTitle));

                    activity.startActivityForResult(chooserIntent, MainActivity.CUSTOM_IMAGE_REQUEST_CODE);
                }
            });

            //when list item (row) clicked show corresponding widget file uriString
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, TheWidgetConfigureActivity.loadUriStringPref(activity, widgetId), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
        return convertView;
    }
}