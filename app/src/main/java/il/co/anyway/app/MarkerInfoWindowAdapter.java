package il.co.anyway.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.Marker;

import il.co.anyway.app.models.Accident;

class MarkerInfoWindowAdapter implements InfoWindowAdapter {

    @SuppressWarnings("unused")
    private final String LOG_TAG = MarkerInfoWindowAdapter.class.getSimpleName();

    private View mInfoWindows = null;
    private LayoutInflater mInflater = null;

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

        Context context = mInflater.getContext();

        if (marker.isCluster()) {

            String title = marker.getMarkers().size() + " " +
                    context.getString(R.string.accidents);

            mainTitle.setText(title);
            mainSnippet.setText(context.getString(R.string.marker_default_desc));

            return mInfoWindows;

        } else {

            if (marker.getData() instanceof Accident) {

                mainTitle.setText(marker.getTitle());
                mainSnippet.setText(marker.getSnippet());

                return mInfoWindows;
            } else if (marker.getTitle().equals("תוצאת חיפוש")) {

                LinearLayout searchLayout = new LinearLayout(mInflater.getContext());
                TextView searchAddress = new TextView(mInflater.getContext());
                searchAddress.setText(marker.getSnippet());
                searchAddress.setTypeface(null, Typeface.BOLD);
                searchAddress.setTextSize(20);

                searchLayout.addView(searchAddress);

                return searchLayout;
            }
        }

        return null;
    }
}