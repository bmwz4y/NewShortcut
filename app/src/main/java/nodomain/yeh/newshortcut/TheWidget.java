package nodomain.yeh.newshortcut;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.io.File;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TheWidgetConfigureActivity}
 */
public class TheWidget extends AppWidgetProvider {

    /**
     * Implements {@link widgetBroadcastReceiver#onReceive} to dispatch calls to the various
     * other methods on AppWidgetProvider.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < N; i++) {
            //deletes the entry from main app file
            MainActivity.deleteFromCurrentShortcutsFile(context, appWidgetIds[i]);

            //delete thumbnail if it has one
            String matchingCriteria = MainActivity.THUMBNAIL_Prefix + appWidgetIds[i] + MainActivity.THUMBNAIL_Postfix;
            String iconTag = TheWidgetConfigureActivity.loadIconTagPref(context, appWidgetIds[i]);
            if (iconTag.contains(matchingCriteria)) {
                File imageFile = new File(iconTag);
                if (imageFile.exists()) {
                    //delete return true if succeeded so if it failed log a debug message
                    if (!imageFile.delete()) {
                        Log.d("@thumbDeletion", "the file exists but wasn\'t deleted, maybe permission problem");
                    }
                }
            }

            //delete preferences
            deleteAllPref(context, appWidgetIds[i]);
        }
    }

    //
    public static void deleteAllPref(Context context, int appWidgetId) {
        TheWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        TheWidgetConfigureActivity.deleteUriStringPref(context, appWidgetId);
        TheWidgetConfigureActivity.deleteMimeTypePref(context, appWidgetId);
        TheWidgetConfigureActivity.deleteIconTagPref(context, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        String TAG = "@Wid.updAppWid";
        Log.i(TAG, "updating widget #" + appWidgetId);
        try {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.the_widget);

            // to change icon image
            String iconTag = TheWidgetConfigureActivity.loadIconTagPref(context, appWidgetId);
            // test iconTag source and adjust widget icon accordingly
            if (iconTag.startsWith("/")) {
                File imageFile = new File(iconTag);
                if (imageFile.exists()) {
                    Log.d("@widgetImage", iconTag);
                    // to force image cache reset
                    views.setImageViewUri(R.id.iconImageView, Uri.parse(""));

                    //load the actual image
                    views.setImageViewUri(R.id.iconImageView, Uri.fromFile(imageFile));
                } else {
                    views.setImageViewResource(R.id.iconImageView, R.drawable.image_not_found);
                }
            } else {
                int iconId = iconIdOf(iconTag);
                // if it's not one of the select icon tags then it's custom and not local
                if (iconId == R.drawable.image_not_found) {
                    // non local uri, for now display not found instead
                    views.setImageViewResource(R.id.iconImageView, iconId);
                } else {
                    views.setImageViewResource(R.id.iconImageView, iconId);
                }
            }

            //change the text under the icon
            String title = TheWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
            views.setTextViewText(R.id.theWidget_name_textView, title);

            // for changing the applications settings
            views.setTextViewTextSize(R.id.theWidget_name_textView, TypedValue.COMPLEX_UNIT_SP
                    , SettingsActivity.getFontSize(context));
            views.setTextColor(R.id.theWidget_name_textView, SettingsActivity.getFontColor(context));

            Intent open = new Intent(widgetBroadcastReceiver.ACTION_OPEN);
            open.putExtra("widgetNumber", appWidgetId);


            // Should i change the last param to PendingIntent.FLAG_UPDATE_CURRENT instead of 0
            PendingIntent pending = PendingIntent.getBroadcast(context, appWidgetId, open, 0);
            views.setOnClickPendingIntent(R.id.the_widget, pending);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Pending?: " + Log.getStackTraceString(e), e);
        }
    }

    /**
     * Helper used by {@link TheWidget .updateAppWidget}
     *
     * @param iconTag The tag to map the icon
     * @return Id of the icon
     */
    public static int iconIdOf(String iconTag) {
        switch (iconTag) {
            case "default_icon":
                return R.drawable.default_icon;
            case "image_icon":
                return R.drawable.image_icon;
            case "audio_icon":
                return R.drawable.audio_icon;
            case "video_icon":
                return R.drawable.video_icon;
            case "web_icon":
                return R.drawable.web_icon;
            case "document_icon":
                return R.drawable.document_icon;
            case "document_with_image_icon":
                return R.drawable.document_with_image_icon;
            case "book_icon":
                return R.drawable.book_icon;
            case "spreadsheet_icon":
                return R.drawable.spreadsheet_icon;
            case "presentation_icon":
                return R.drawable.presentation_icon;
            case "archive_icon":
                return R.drawable.archive_icon;
        }
        return R.drawable.image_not_found;
    }

    /**
     * Helper used by {@link TheWidgetConfigureActivity} or {@link widgetBroadcastReceiver}
     *
     * @param context     The context of the callee
     * @param appWidgetId The number of the widget being served
     * @return Intent to open
     */
    static Intent createOpen(Context context, int appWidgetId) {

        String uriString = TheWidgetConfigureActivity.loadUriStringPref(context, appWidgetId);
        Uri uri = Uri.parse(uriString);

        String mimeType = TheWidgetConfigureActivity.loadMimeTypePref(context, appWidgetId);

        Intent openWith = new Intent();
        openWith.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openWith.setAction(Intent.ACTION_VIEW);
        openWith.addCategory(Intent.CATEGORY_DEFAULT);
        openWith.setDataAndType(uri, mimeType);

        return openWith;
    }
}