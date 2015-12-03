package nodomain.yeh.newshortcut;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "nodomain.yeh.newshortcut.settings";
    private static final String FONT_SIZE_KEY = "fontSize";
    private static final String FONT_COLOR_KEY = "fontColor";
    private static final String THUMBNAIL_SIZE_KEY = "thumbnailSize";
    final private static int Shortcuts_defaultFontSize = 16;//sp
    final private static int Shortcuts_defaultFontColor = Color.argb(255, 250, 250, 250);//same as primary_text_dark
    final private static int Thumbnail_defaultSize = 256;//px used for custom images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //supposedly solves issues on some older phones
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final EditText fontSizeEditText = (EditText) findViewById(R.id.shortcutFontSizing_editText);

        //start focus on the font size edit text
        fontSizeEditText.requestFocus();

        // Hide soft keyboard automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        final TextView previewText = (TextView) findViewById(R.id.theWidget_name_textView_settingsPreview);
        final EditText thumbnailSizeEditText = (EditText) findViewById(R.id.thumbnailSizing_editText);

        int size = getFontSize(this);
        fontSizeEditText.setText(String.valueOf(size));

        //this will move edit cursor to the end of the respective EditText
        fontSizeEditText.setSelection(fontSizeEditText.length());

        previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

        final int color = getFontColor(this);
        previewText.setTextColor(color);

        int thumbSize = getThumbnailSize(this);
        thumbnailSizeEditText.setText(String.valueOf(thumbSize));

        Button defaultFontSize = (Button) findViewById(R.id.shortcutFontSizingDefault_button);
        defaultFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset to defaults
                fontSizeEditText.setText(String.valueOf(Shortcuts_defaultFontSize));
                previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, Shortcuts_defaultFontSize);

                //this will move edit cursor to the end of the respective EditText
                fontSizeEditText.setSelection(fontSizeEditText.length());
            }
        });

        Button chooseFontColor = (Button) findViewById(R.id.shortcutFontColoringChoose_button);
        chooseFontColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // color picker dialog
                ColorPickerDialogBuilder
                        .with(view.getContext())
                        .setTitle("Choose color")
                        .initialColor(previewText.getCurrentTextColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)//this changes the selectable color gamut
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                Log.i("color: ", "0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                previewText.setTextColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });

        Button defaultFontColor = (Button) findViewById(R.id.shortcutFontColoringDefault_button);
        defaultFontColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset to defaults
                previewText.setTextColor(Shortcuts_defaultFontColor);
            }
        });

        Button defaultThumbnailSize = (Button) findViewById(R.id.thumbnailSizingDefault_button);
        defaultThumbnailSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset to defaults
                thumbnailSizeEditText.setText(String.valueOf(Thumbnail_defaultSize));

                //this will move edit cursor to the end of the respective EditText
                thumbnailSizeEditText.setSelection(thumbnailSizeEditText.length());
            }
        });

        Button applyFontChanges = (Button) findViewById(R.id.shortcutFontApply_button);
        applyFontChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int inputSize = Integer.valueOf(fontSizeEditText.getText().toString());
                    if (inputSize > 0) {
                        // commit changes to preferences
                        setFontSize(view.getContext(), inputSize);
                        setFontColor(view.getContext(), previewText.getCurrentTextColor());

                        // reload all of this app widgets
                        Intent refreshAppWidgets = new Intent(view.getContext(), TheWidget.class);
                        refreshAppWidgets.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

                        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                        // since it seems the onUpdate() is only fired on that
                        int[] ids = AppWidgetManager.getInstance(getApplication())
                                .getAppWidgetIds(new ComponentName(getApplication(), TheWidget.class));
                        refreshAppWidgets.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                        sendBroadcast(refreshAppWidgets);

                        //displays done message
                        Snackbar.make(view, R.string.applyFontChanges_done, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (NumberFormatException e) {
                    Log.i("@applyFont", "NumberFormatException");
                }
            }
        });

        Button applyThumbnailChanges = (Button) findViewById(R.id.thumbnailSizingApply_button);
        applyThumbnailChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int inputSize = Integer.valueOf(thumbnailSizeEditText.getText().toString());
                    if (inputSize > 0) {
                        // commit changes to preferences
                        setThumbnailSize(view.getContext(), inputSize);

                        //displays done message
                        Snackbar.make(view, R.string.applyThumbnailChanges_done, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (NumberFormatException e) {
                    Log.i("@applyThumbnail", "NumberFormatException");
                }
            }
        });

        fontSizeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                try {
                    int input = Integer.valueOf(charSequence.toString());
                    if (input > 0) {
                        previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, input);
                    }
                } catch (NumberFormatException e) {
                    Log.i("@FSETw", "NumberFormatException");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // for increasing and decreasing the values of font size edit text
        Button increaseFontSize = (Button) findViewById(R.id.fontSizePlus_button);
        increaseFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if empty then set editText to 1
                String input = fontSizeEditText.getText().toString();
                if (input.isEmpty()) {
                    fontSizeEditText.setText("1");
                } else {
                    int number = Integer.valueOf(input) + 1;
                    fontSizeEditText.setText(String.valueOf(number));
                }

                //this will move edit cursor to the end of the respective EditText
                fontSizeEditText.setSelection(fontSizeEditText.length());
            }
        });
        Button decreaseFontSize = (Button) findViewById(R.id.fontSizeMinus_button);
        decreaseFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if empty then set editText to 0
                String input = fontSizeEditText.getText().toString();
                if (input.isEmpty()) {
                    fontSizeEditText.setText("0");
                } else {
                    int number = Integer.valueOf(input) - 1;

                    //no negatives
                    if (number < 0) {
                        number = 0;
                    }
                    fontSizeEditText.setText(String.valueOf(number));
                }

                //this will move edit cursor to the end of the respective EditText
                fontSizeEditText.setSelection(fontSizeEditText.length());
            }
        });

        // for increasing and decreasing the values of thumbnail size edit text
        Button increaseThumbnailSize = (Button) findViewById(R.id.thumbnailSizePlus_button);
        increaseThumbnailSize.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                //check if empty then set editText to 32
                String input = thumbnailSizeEditText.getText().toString();
                if (input.isEmpty()) {
                    thumbnailSizeEditText.setText("32");
                } else {
                    int number = Integer.valueOf(input) + 32;
                    thumbnailSizeEditText.setText(String.valueOf(number));
                }

                //this will move edit cursor to the end of the respective EditText
                thumbnailSizeEditText.setSelection(thumbnailSizeEditText.length());
            }
        });
        Button decreaseThumbnailSize = (Button) findViewById(R.id.thumbnailSizeMinus_button);
        decreaseThumbnailSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if empty then set editText to 0
                String input = thumbnailSizeEditText.getText().toString();
                if (input.isEmpty()) {
                    thumbnailSizeEditText.setText("0");
                } else {
                    int number = Integer.valueOf(input) - 32;

                    //no negatives
                    if (number < 0) {
                        number = 0;
                    }
                    thumbnailSizeEditText.setText(String.valueOf(number));
                }

                //this will move edit cursor to the end of the respective EditText
                thumbnailSizeEditText.setSelection(thumbnailSizeEditText.length());
            }
        });
    }


    public static int getFontSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int value = prefs.getInt(FONT_SIZE_KEY, 0);
        if (value != 0) {
            return value;
        } else {
            return Shortcuts_defaultFontSize;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void setFontSize(Context context, int fontSize) {
        if (fontSize > 0 && fontSize < Integer.MAX_VALUE) {
            SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
            prefs.putInt(FONT_SIZE_KEY, fontSize);
            prefs.commit();
        }
    }

    public static int getFontColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int value = prefs.getInt(FONT_COLOR_KEY, -1);
        if (value != -1) {
            return value;
        } else {
            return Shortcuts_defaultFontColor;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void setFontColor(Context context, int fontColor) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(FONT_COLOR_KEY, fontColor);
        prefs.commit();
    }

    public static int getThumbnailSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int value = prefs.getInt(THUMBNAIL_SIZE_KEY, -1);
        if (value != -1) {
            return value;
        } else {
            return Thumbnail_defaultSize;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void setThumbnailSize(Context context, int thumbnailSize) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(THUMBNAIL_SIZE_KEY, thumbnailSize);
        prefs.commit();
    }
}
