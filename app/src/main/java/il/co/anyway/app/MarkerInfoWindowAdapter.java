package il.co.anyway.app;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;

class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    @SuppressWarnings("unused")
    private final String LOG_TAG = MarkerInfoWindowAdapter.class.getSimpleName();

    private View mInfoWindows = null;
    private LayoutInflater mInflater = null;

    MarkerInfoWindowAdapter(LayoutInflater inflater) {
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

        TextView tv;

        tv = (TextView) mInfoWindows.findViewById(R.id.title);
        if (tv != null)
            tv.setText(marker.getTitle());

        tv = (TextView) mInfoWindows.findViewById(R.id.snippet);
        if (tv != null)
            tv.setText(marker.getSnippet());

        return (mInfoWindows);
    }
}