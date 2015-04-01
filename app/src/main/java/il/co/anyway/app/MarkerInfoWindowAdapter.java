package il.co.anyway.app;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.Marker;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class MarkerInfoWindowAdapter implements InfoWindowAdapter {

    @SuppressWarnings("unused")
    private final String LOG_TAG = MarkerInfoWindowAdapter.class.getSimpleName();

    private View mInfoWindows = null;
    private LayoutInflater mInflater = null;

    private Collator collator = Collator.getInstance();
    private Comparator<Marker> comparator = new Comparator<Marker>() {
        public int compare(Marker lhs, Marker rhs) {
            String leftTitle = lhs.getTitle();
            String rightTitle = rhs.getTitle();
            if (leftTitle == null && rightTitle == null) {
                return 0;
            }
            if (leftTitle == null) {
                return 1;
            }
            if (rightTitle == null) {
                return -1;
            }
            return collator.compare(leftTitle, rightTitle);
        }
    };

    public MarkerInfoWindowAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return (null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {

        if (mInfoWindows == null)
            mInfoWindows = mInflater.inflate(R.layout.marker_info_window, null);

        TextView mainTitle, mainSnippet;
        mainTitle = (TextView) mInfoWindows.findViewById(R.id.title);
        mainSnippet = (TextView) mInfoWindows.findViewById(R.id.snippet);

        if (mainTitle == null || mainSnippet == null)
            return null;

        // TODO move strings to strings.xml
        if (marker.isCluster()) {
            List<Marker> markers = marker.getMarkers();
            int i = 0;
            String text = "";
            while (i < 3 && markers.size() > 0) {
                Marker m = Collections.min(markers, comparator);
                String title = m.getTitle();
                if (title == null) {
                    break;
                }
                text += title + "\n";
                markers.remove(m);
                i++;
            }
            if  (markers.size() > 0) {
                text += "ועוד " + markers.size() + " נוספות";
            } else {
                text = text.substring(0, text.length() - 1);
            }

            mainTitle.setText("ריבוי תאונות");
            mainSnippet.setText(text);

            return mInfoWindows;

        } else {

            if (marker.getData() instanceof Accident) {

                mainTitle.setText(marker.getTitle());
                mainSnippet.setText(marker.getSnippet());

                return mInfoWindows;
            }
        }

        return null;
    }
}