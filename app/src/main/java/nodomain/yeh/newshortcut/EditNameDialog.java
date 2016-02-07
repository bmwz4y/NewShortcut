package nodomain.yeh.newshortcut;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * dialog fragment to change name of source
 * Created by YEH on 11/22/2015.
 */
public class EditNameDialog extends DialogFragment implements TextView.OnEditorActionListener {

    EditText newName;
    int correspondingWidgetId;
    int correspondingPosition;

    public EditNameDialog() {
    }

    public interface EditNameDialogListener {
        void onFinishEditNameDialog(String inputName, int position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Button ok;
        Button cancel;

        correspondingWidgetId = getArguments().getInt("WIDGET_ID");
        correspondingPosition = getArguments().getInt("POSITION");

        View view = inflater.inflate(R.layout.fragment_edit_name, container);

        newName = (EditText) view.findViewById(R.id.rename_editText);
        newName.setText(TheWidgetConfigureActivity.loadTitlePref(view.getContext(), correspondingWidgetId));

        // Show soft keyboard automatically
        newName.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        newName.setOnEditorActionListener(this);

        ok = (Button) view.findViewById(R.id.ok_button);
        cancel = (Button) view.findViewById(R.id.cancel_button);

        getDialog().setTitle(R.string.nameDialogTitle);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneEditing(view);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // dismiss the dialog
                dismiss();
            }
        });

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            doneEditing(v);
            return true;
        }
        return false;
    }

    private void doneEditing(View view) {
        Context context = view.getContext();
        //react to input
        // check if newName not empty and update both widget and row
        String newNameInput = newName.getText().toString();
        if (newNameInput.isEmpty()) {
            Toast.makeText(context, R.string.nameDialogEmptyMessage, Toast.LENGTH_SHORT).show();
        } else {
            // To update the app widget in home screen
            TheWidgetConfigureActivity.saveTitlePref(context, correspondingWidgetId, newNameInput);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TheWidget.updateAppWidget(context, appWidgetManager, correspondingWidgetId);

            // Return input text to activity
            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
            activity.onFinishEditNameDialog(newNameInput, correspondingPosition);

            // dismiss the dialog
            dismiss();
        }
    }
}