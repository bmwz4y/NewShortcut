package nodomain.yeh.newshortcut;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements EditNameDialog.EditNameDialogListener, SelectIconDialog.SelectIconDialogListener {

    final public static String COMMUNICATOR_FileName = "currentShortcuts";
    final public static String THUMBNAIL_Prefix = "thumbnail_";
    final public static String THUMBNAIL_Postfix = ".jpg";

    CustomAdapter adapter;
    public MainActivity CustomListView = null;
    public ArrayList<ListModel> CustomListViewValuesArr = new ArrayList<>();
    public static final int CUSTOM_IMAGE_REQUEST_CODE = 16385;

    private static boolean onResumeEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.help_text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        CustomListView = this;

        /******** Take some data in Arraylist ( CustomListViewValuesArr ) ***********/
        //setting the list and if there's none then show alert dialog how to add widget
        if (setListData() == 0) {
            NoShortcutsYetDialogFragment noShortcutsYet = new NoShortcutsYetDialogFragment();
            noShortcutsYet.show(getSupportFragmentManager(), "NoShortcutsYet");
        } else {
            Resources res = getResources();
            ListView list = (ListView) findViewById(R.id.currentShortcutsListView);  // List defined in XML ( See Below )

            /**************** Create Custom Adapter *********/
            adapter = new CustomAdapter(CustomListView, CustomListViewValuesArr, res);
            list.setAdapter(adapter);
        }
    }

    /**
     * Function to set data in ArrayList
     *
     * @return 0 if no items, 1 if one or more items
     */
    public int setListData() {
        String TAG = "@setLisDat";

        String inputString = "";
        try {

            FileInputStream inputStream = openFileInput(MainActivity.COMMUNICATOR_FileName);

            int c;
            while ((c = inputStream.read()) != -1) {
                inputString = inputString + Character.toString((char) c);
            }
            inputStream.close();
        } catch (FileNotFoundException ex) {
            Log.i(TAG, "FileNotFoundException '" + MainActivity.COMMUNICATOR_FileName + "'");

        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e), e);
        }
        // initializing for the loop
        int currentShortcutsCount = inputString.length() - inputString.replace(",", "").length();
        String tempShortcutIdString;
        int tempShortcutId;

        if (currentShortcutsCount < 1)
            return 0;
        else {
            for (int i = 0; i < currentShortcutsCount; i++) {
                final ListModel model = new ListModel();

                //get the number
                tempShortcutIdString = inputString.substring(0, inputString.indexOf(','));
                tempShortcutId = Integer.valueOf(tempShortcutIdString);
                //cut the number out
                inputString = inputString.substring(inputString.indexOf(',') + 1);

                /******* Firstly take data in model object ******/
                model.setWidgetId(tempShortcutId);
                model.setIconTag(TheWidgetConfigureActivity.loadIconTagPref(this, tempShortcutId));
                model.setTitle(TheWidgetConfigureActivity.loadTitlePref(this, tempShortcutId));

                /******** Take Model Object in ArrayList **********/
                CustomListViewValuesArr.add(model);
            }
            return 1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                // go to settings
                Intent invokeSettings = new Intent(this, SettingsActivity.class);
                startActivity(invokeSettings);

                //return consumed
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void deleteFromCurrentShortcutsFile(Context context, int appWidgetId) {
        String TAG = "@delFroCurShoFil";

        // deletes corresponding widget Id number from the file
        String inputString = "";
        try {

            FileInputStream inputStream = context.openFileInput(MainActivity.COMMUNICATOR_FileName);

            int c;
            while ((c = inputStream.read()) != -1) {
                inputString = inputString + Character.toString((char) c);
            }
            inputStream.close();
        } catch (FileNotFoundException ex) {
            Log.i(TAG, "File was not found someone deleted it or hasn't been created yet");
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e), e);
        }
        String toRemove = appWidgetId + ",";
        inputString = inputString.replaceFirst(toRemove, "");

        try {
            FileOutputStream outputStream = context.openFileOutput(MainActivity.COMMUNICATOR_FileName, Context.MODE_PRIVATE);
            outputStream.write(inputString.getBytes());
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e), e);
        }
    }

    @Override
    public void onFinishEditNameDialog(String inputName, int position) {
        // refresh corresponding name textView
        CustomListViewValuesArr.get(position).setTitle(inputName);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFinishSelectIconDialog(String selectedIconTag, int position) {
        // refresh corresponding icon imageView
        CustomListViewValuesArr.get(position).setIconTag(selectedIconTag);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        String TAG = "@MaiAct.onActRes";

        // returns from select custom image and checks If the file selection was successful
        if (requestCode == CUSTOM_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && returnIntent != null) {
            try {
                final Context context = this;
                final int widgetId = CustomAdapter.customIconWidgetId;
                final int position = CustomAdapter.customIconViewPosition;

                // use uri to get the path of user selected image
                final String path = FileUtils.getPath(context, returnIntent.getData());

                //make sure not null and local file
                if (path != null && path.startsWith("/")) {
                    // To update the app widget in home screen
                    TheWidgetConfigureActivity.saveIconTagPref(context, widgetId, createThumbnail(path, widgetId));
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    TheWidget.updateAppWidget(context, appWidgetManager, widgetId);

                    // To update the list view
                    CustomListViewValuesArr.get(position).setIconTag(path);
                    adapter.notifyDataSetChanged();
                } else {
                    // not local, for now show toast message
                    Toast.makeText(this, R.string.image_not_local_message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + Log.getStackTraceString(e), e);
            }
        }

    }

    private String createThumbnail(String inputImagePath, int widgetId) {


        byte[] imageData = null;

        try {

            final int thumbnail_size = SettingsActivity.getThumbnailSize(this);

            FileInputStream fis = new FileInputStream(inputImagePath);
            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);

            float width = imageBitmap.getWidth();
            float height = imageBitmap.getHeight();
            float ratio = width / height;
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (thumbnail_size * ratio), thumbnail_size, false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageData = baos.toByteArray();

        } catch (Exception e) {
            Log.e("@creating thumbnail", Log.getStackTraceString(e), e);
        }

        // use the widget id inside thumbnail name
        String thumbFileName = MainActivity.THUMBNAIL_Prefix + widgetId + MainActivity.THUMBNAIL_Postfix;

        try {

            //save thumbnail image ,must be deleted on widget deletion
            //noinspection deprecation for home launcher usage it must be public
            @SuppressLint("WorldReadableFiles") FileOutputStream outputStream = openFileOutput(thumbFileName, Context.MODE_WORLD_READABLE);
            if (imageData != null)
                outputStream.write(imageData);
            outputStream.close();
        } catch (IOException e) {
            Log.e("@saving thumbnail", Log.getStackTraceString(e), e);
        }

        return getApplicationContext().getFilesDir().getAbsolutePath() + "/" + thumbFileName;
    }

    /**
     * Dispatch onResume() to fragments.
     */
    @Override
    protected void onResume() {
        super.onResume();

        //checking if first run then no need for it
        if (onResumeEnabled) {

            //clear and reset the listView items
            CustomListViewValuesArr.clear();
            if (setListData() == 1) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        onResumeEnabled = true;
    }
}