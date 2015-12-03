package nodomain.yeh.newshortcut;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * dialog to show if bo active shortcuts
 * Created by YEH on 11/21/2015.
 */
public class NoShortcutsYetDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true)
                .setTitle(R.string.noShortcutsYet_title)
                .setMessage(getString(R.string.noShortcutsYet_message) + " [" + getString(R.string.widget_name) + "]");
        // Create the AlertDialog object and return it
        return builder.create();
    }
}