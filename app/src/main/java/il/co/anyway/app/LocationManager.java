package il.co.anyway.app;

import android.location.Location;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.MarkerOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import il.co.anyway.app.singletons.AnywayRequestQueue;

public class LocationManager implements LocationListener, ILocationManager {
    private final MainActivity mainActivity;
    private final GoogleApiClient mGoogleApiClient;
    private Location mLocation;


    public LocationManager(MainActivity mainActiviity, GoogleApiClient apiClient) {
        this.mainActivity = mainActivity;
        this.mGoogleApiClient = apiClient;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;

        if (location == null)
            return;

        if (!mainActivity.ismSentUserLocation()) {
            AnywayRequestQueue.getInstance(mainActivity)
                    .sendUserAndSearchedLocation(
                            location.getLatitude(), location.getLongitude(),
                            AnywayRequestQueue.HIGHLIGHT_TYPE_USER_GPS
                    );
            mainActivity.setmSentUserLocation(true);
        }

        if (mainActivity.getmPositionMarker() == null) {

            mainActivity.setmPositionMarker(mainActivity.getmMap().addMarker(new MarkerOptions()
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.position_indicator))
                    .title(MarkerInfoWindowAdapter.FORCE_SIMPLE_SNIPPET_SHOW)
                    .snippet(mainActivity.getString(R.string.my_location))
                    .position(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()))));
            mainActivity.getmPositionMarker().setClusterGroup(ClusterGroup.NOT_CLUSTERED);

        } else {

            mainActivity.getmPositionMarker().setPosition(new LatLng(location.getLatitude(), location
                    .getLongitude()));
        }

    }

    public Location getLocation() {
        return mLocation;
    }

    @Override
    public void updateLocation(MainActivity mainActivity) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it.
        if (mLocation == null)
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }
}