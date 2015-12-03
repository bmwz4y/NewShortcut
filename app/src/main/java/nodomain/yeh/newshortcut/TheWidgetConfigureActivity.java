package nodomain.yeh.newshortcut;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 * The configuration logic for the {@link TheWidget}
 */
public class TheWidgetConfigureActivity extends Activity {

    //static fields
    private static final String PREFS_NAME = "nodomain.yeh.newshortcut.TheWidget";
    private static final String TITLE_PREFIX_KEY = "widgetTitle_";
    private static final String URI_STRING_PREFIX_KEY = "widgetUriString_";
    private static final String MIME_TYPE_PREFIX_KEY = "widgetMimeType_";
    private static final String ICON_TAG_PREFIX_KEY = "widgetIconTag_";
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 16386;
    private static final int TheWidgetConfigureActivity_REQUEST_CODE = 16387;
    private static Handler mHandler = new Handler();

    //non static fields
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean canceled = true;
    private int noHandlerToastTime = 0;//default to zero milliseconds


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String TAG = "@WidCon.onCre";
        //Loading screen
        setContentView(R.layout.loading_activity);
        ProgressBar spinner;
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        //For android 6.0+ Requesting Permissions at Run Time
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(getBaseContext(), R.string.blame_message, Toast.LENGTH_LONG).show();

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_REQUEST_CODE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }

        try {
            // Set the result to CANCELED.  This will cause the widget host to cancel
            // out of the widget placement if the user presses the back button.
            setResult(RESULT_CANCELED);

            //To let the user choose file
            showFileChooser();


            // Find the widget id from the intent.
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            // If this activity was started with an intent without an app widget ID, finish with an error.
            if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, " launcher? " + Log.getStackTraceString(e), e);
        }
    }

    @Override
    protected void onDestroy() {
        widgetFinish();
        super.onDestroy();
    }

    private void showFileChooser() {
        String TAG = "@WidCon.shoFilCho";
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, TheWidgetConfigureActivity_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "the createChooser didn't run correctly " + Log.getStackTraceString(e), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        String TAG = "@WidCon.onActRes";

        // If the file selection was successful
        if (requestCode == TheWidgetConfigureActivity_REQUEST_CODE && resultCode == RESULT_OK && returnIntent != null) {
            try {
                Uri uri = returnIntent.getData();
                createTheWidget(uri);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + Log.getStackTraceString(e), e);
            }
        }
        finish();
    }

    private void widgetFinish() {
        // Finishing state and Don't allow ghost widget creation
        if (canceled) {
            AppWidgetHost host = new AppWidgetHost(TheWidgetConfigureActivity.this, 1);
            TheWidget.deleteAllPref(getBaseContext(), mAppWidgetId);

            //to delay this toast message if file can't be resolved toast has been reached
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(), R.string.cancel_creation_toast, Toast.LENGTH_LONG).show();
                }
            }, noHandlerToastTime);

            host.deleteAppWidgetId(mAppWidgetId);
            finish();
        } else {
            saveToCurrentShortcutsFile();
            Toast.makeText(this, R.string.affirm_creation_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToCurrentShortcutsFile() {
        String TAG = "@savToCurShoFil";

        try {
            FileOutputStream outputStream = openFileOutput(MainActivity.COMMUNICATOR_FileName, Context.MODE_APPEND);
            String toWrite = mAppWidgetId + ",";
            outputStream.write(toWrite.getBytes());
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e), e);
        }
    }

    private void createTheWidget(Uri uri) {
        final Context context = getBaseContext();
        String TAG = "@creTheWid";

        // Getting the real path out
        String path = FileUtils.getPath(context, uri);

        String mimeType;
        // new processing the mime type out of Uri which may return null in some cases
        mimeType = getContentResolver().getType(uri);
        // old processing the mime type out of path using the extension part if new way returned null
        if (mimeType == null) {
            mimeType = URLConnection.guessContentTypeFromName(path);
        }

        //renaming the widget
        assert path != null;
        String widgetName = path.substring(path.lastIndexOf('/') + 1);

        try {
            //get the real name out
            widgetName = widgetName.substring(0, widgetName.lastIndexOf('.'));

            //normalize file path
            path = "file://" + path;

            // get the specific icon tag
            String iconTag = getIconTagFromMime(mimeType);

            //Saving preferences
            Log.i(TAG, widgetName);
            saveTitlePref(context, mAppWidgetId, widgetName);
            Log.i(TAG, path);
            saveUriStringPref(context, mAppWidgetId, path);
            Log.i(TAG, mimeType);
            saveMimeTypePref(context, mAppWidgetId, mimeType);
            Log.i(TAG, iconTag);
            saveIconTagPref(context, mAppWidgetId, iconTag);


            // If file can't be resolved then don't allow shortcut creation
            PackageManager packageManager = getPackageManager();
            if (TheWidget.createOpen(context, mAppWidgetId).resolveActivity(packageManager) == null) {

                //to make delay between this and the cancel toast inside widgetFinish()
                noHandlerToastTime = Toast.LENGTH_SHORT;
                Toast.makeText(this, R.string.no_handler_toast, noHandlerToastTime).show();

            } else {//allow creation

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                TheWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

                // To make sure only when all previous code met that a widget gets created
                setResult(RESULT_OK, resultValue);
                canceled = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Uri or Mime Type error? " + Log.getStackTraceString(e), e);
        }
        finish();
    }

    /**
     * Helper class to retrieve icon image specific to common mime types
     */
    private String getIconTagFromMime(String mimeType) {

        // should be changed to hashmap or hashset or whatever
        if (mimeType.startsWith("image")) {
            return "image_icon";
        } else {
            if (mimeType.startsWith("audio")) {
                return "audio_icon";
            } else {
                if (mimeType.startsWith("video")) {
                    return "video_icon";
                } else {
                    if (mimeType.startsWith("text/html") ||
                            mimeType.startsWith("application/xhtml") ||
                            mimeType.startsWith("text/php")) {
                        return "web_icon";
                    } else {
                        if (mimeType.startsWith("text")) {
                            if (mimeType.startsWith("text/plain") ||
                                    mimeType.startsWith("text/rtf") ||
                                    mimeType.startsWith("text/richtext") ||
                                    mimeType.startsWith("text/csv") ||
                                    mimeType.startsWith("text/markdown") ||
                                    mimeType.startsWith("text/xml")) {
                                return "document_icon";
                            } else {
                                return "default_icon";
                            }
                        } else {
                            if (mimeType.startsWith("application")) {
                                if (mimeType.startsWith("application/msword") ||
                                        mimeType.startsWith("application/pdf") ||
                                        mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                                    return "document_with_image_icon";
                                } else {
                                    if (mimeType.startsWith("application/epub")) {
                                        return "book_icon";
                                    } else {
                                        if (mimeType.startsWith("application/vnd.ms-excel") ||
                                                mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                                            return "spreadsheet_icon";
                                        } else {
                                            if (mimeType.startsWith("application/powerpoint") ||
                                                    mimeType.startsWith("application/vnd.ms-powerpoint") ||
                                                    mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.slideshow") ||
                                                    mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                                                return "presentation_icon";
                                            } else {
                                                if (mimeType.startsWith("application/gzip") ||
                                                        mimeType.startsWith("application/x-gzip") ||
                                                        mimeType.startsWith("application/x-tar") ||
                                                        mimeType.startsWith("application/zip") ||
                                                        mimeType.startsWith("application/x-compressed") ||
                                                        mimeType.startsWith("application/x-gtar")) {
                                                    return "archive_icon";
                                                } else {
                                                    return "default_icon";
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                return "default_icon";
                            }
                        }
                    }
                }
            }
        }
    }


    // Write the preference to the SharedPreferences object for this widget
    @SuppressLint("CommitPrefEdits")
    public static void saveTitlePref(Context context, int appWidgetId, String title) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(TITLE_PREFIX_KEY + appWidgetId, title);
        prefs.commit();
    }

    // Read the preference from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(TITLE_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.theWidget_DefaultName);
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(TITLE_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    // Write the preference to the SharedPreferences object for this widget
    @SuppressLint("CommitPrefEdits")
    private static void saveUriStringPref(Context context, int appWidgetId, String uriString) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(URI_STRING_PREFIX_KEY + appWidgetId, uriString);
        prefs.commit();
    }

    // Read the preference from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadUriStringPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(URI_STRING_PREFIX_KEY + appWidgetId, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void deleteUriStringPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(URI_STRING_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    // Write the preference to the SharedPreferences object for this widget
    @SuppressLint("CommitPrefEdits")
    private static void saveMimeTypePref(Context context, int appWidgetId, String mimeType) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(MIME_TYPE_PREFIX_KEY + appWidgetId, mimeType);
        prefs.commit();
    }

    // Read the preference from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadMimeTypePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(MIME_TYPE_PREFIX_KEY + appWidgetId, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void deleteMimeTypePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(MIME_TYPE_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    // Write the preference to the SharedPreferences object for this widget
    @SuppressLint("CommitPrefEdits")
    public static void saveIconTagPref(Context context, int appWidgetId, String iconTag) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(ICON_TAG_PREFIX_KEY + appWidgetId, iconTag);
        prefs.commit();
    }

    // Read the preference from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadIconTagPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String tagValue = prefs.getString(ICON_TAG_PREFIX_KEY + appWidgetId, null);
        if (tagValue != null) {
            return tagValue;
        } else {
            return "default_icon";
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void deleteIconTagPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(ICON_TAG_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    //For android 6.0+ Requesting Permissions at Run Time
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permission granted
                    Toast.makeText(this, R.string.retryMessage, Toast.LENGTH_LONG).show();
                } else {

                    // permission denied, boo!
                    Toast.makeText(this, R.string.blame_message, Toast.LENGTH_LONG).show();
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}