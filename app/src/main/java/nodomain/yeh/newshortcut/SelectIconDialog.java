package nodomain.yeh.newshortcut;

import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;

/**
 * dialog to show a gallery grid of prepared file icons
 * Created by YEH on 11/22/2015.
 */
public class SelectIconDialog extends DialogFragment {
    int correspondingWidgetId;
    int correspondingPosition;

    public SelectIconDialog() {
    }

    public interface SelectIconDialogListener {
        void onFinishSelectIconDialog(String selectedIconTag, int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //String oldIconTag;

        correspondingWidgetId = getArguments().getInt("WIDGET_ID");
        correspondingPosition = getArguments().getInt("POSITION");

        View view = inflater.inflate(R.layout.fragment_select_icon, container);

        GridView gridview = (GridView) view.findViewById(R.id.selectIcon_layout);
        gridview.setAdapter(new SelectIconAdapter(view.getContext()));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Context context = v.getContext();

                // use helper to know which icon
                String newIconTag = SelectIconAdapter.getIconTagFromPosition(position);

                //delete thumbnail if it has one
                String matchingCriteria = MainActivity.THUMBNAIL_Prefix + correspondingWidgetId + MainActivity.THUMBNAIL_Postfix;
                String iconTag = TheWidgetConfigureActivity.loadIconTagPref(context, correspondingWidgetId);
                if (iconTag.contains(matchingCriteria)) {
                    File imageFile = new File(iconTag);
                    if (imageFile.exists()) {
                        //delete return true if succeeded so if it failed log a debug message
                        if (!imageFile.delete()) {
                            Log.d("@thumbDeletion", "the file exists but wasn\'t deleted, maybe permission problem");
                        }
                    }
                }
                // To update the app widget in home screen
                TheWidgetConfigureActivity.saveIconTagPref(context, correspondingWidgetId, newIconTag);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                TheWidget.updateAppWidget(context, appWidgetManager, correspondingWidgetId);

                // Return selected to activity
                SelectIconDialogListener activity = (SelectIconDialogListener) getActivity();
                activity.onFinishSelectIconDialog(newIconTag, correspondingPosition);

                // dismiss the dialog
                dismiss();
            }
        });

        getDialog().setTitle(R.string.selectIconDialogTitle);

        return view;
    }


}
