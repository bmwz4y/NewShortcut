package nodomain.yeh.newshortcut;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class widgetBroadcastReceiver extends BroadcastReceiver {
    final public static String ACTION_OPEN = "nodomain.yeh.newshortcut.intent.action.open";

    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = "@WidBroRec.onRec";

        if (intent.getAction().equals(ACTION_OPEN)) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                int appWidgetId = bundle.getInt("widgetNumber");
                Log.i(TAG, "Serving widget #" + appWidgetId);
                try {
                    String uriString = TheWidgetConfigureActivity.loadUriStringPref(context, appWidgetId);

                    //cutting file://
                    File toOpen = new File(uriString.substring(7));

                    // check if it's available then try to open it
                    if (toOpen.exists()) {
                        //create the view intent
                        Intent openWith = TheWidget.createOpen(context, appWidgetId);

                        //start the view intent
                        context.startActivity(openWith);
                    } else {
                        Toast.makeText(context, R.string.file_not_found_toast, Toast.LENGTH_LONG).show();
                    }
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.no_handler_toast, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Resolving?: " + Log.getStackTraceString(e), e);
                }
            }
        }
    }
}