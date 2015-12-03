package nodomain.yeh.newshortcut;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Adapter used by the gridView created by {@link SelectIconDialog}
 * Created by YEH on 11/22/2015.
 */
public class SelectIconAdapter extends BaseAdapter {
    private Context mContext;

    public SelectIconAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);

            //sets the width, height of each item in the gridView
            imageView.setLayoutParams(new GridView.LayoutParams(100, 139));

            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            //for padding each item
            imageView.setPadding(0, 5, 0, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    // should be in sync with getIconTagFromPosition
    private Integer[] mThumbIds = {
            R.drawable.archive_icon,
            R.drawable.audio_icon,
            R.drawable.book_icon,
            R.drawable.default_icon,
            R.drawable.document_icon,
            R.drawable.document_with_image_icon,
            R.drawable.image_icon,
            R.drawable.presentation_icon,
            R.drawable.spreadsheet_icon,
            R.drawable.video_icon,
            R.drawable.web_icon
    };

    /**
     * Helper class to retrieve icon image specific to view position
     */
    public static String getIconTagFromPosition(int position) {

        switch (position) {
            case 0:
                return "archive_icon";
            case 1:
                return "audio_icon";
            case 2:
                return "book_icon";
            case 3:
                return "default_icon";
            case 4:
                return "document_icon";
            case 5:
                return "document_with_image_icon";
            case 6:
                return "image_icon";
            case 7:
                return "presentation_icon";
            case 8:
                return "spreadsheet_icon";
            case 9:
                return "video_icon";
            case 10:
                return "web_icon";
        }
        return "default_icon";
    }
}