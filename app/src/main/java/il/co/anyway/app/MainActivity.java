package il.co.anyway.app;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.List;


public class MainActivity extends ActionBarActivity implements OnInfoWindowClickListener,
        OnMapLongClickListener, OnCameraChangeListener {

    public final LatLng AZZA_METUDELA_LOCATION = new LatLng(31.772126, 35.213678);
    public final int MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS = 15;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    public static GoogleMap map;
    private static boolean debug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {

            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // Enable location buttons
        map.setMyLocationEnabled(true);

        // go to test area in map if we are still debugging
        if(debug)
            setMapToLocation(AZZA_METUDELA_LOCATION, 17);
        else
            centerMapOnMyLocation();

        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnCameraChangeListener(this);
    }

    /**
     * move the camera to specific location, should be called on after checking map!=null
     * @param location location to move to
     * @param zoomLevel move camera to this specific
     */
    private void setMapToLocation(LatLng location, int zoomLevel) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.latitude, location.longitude), zoomLevel));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)           // Sets the center of the map to location user
                .zoom(zoomLevel)            // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "עוד אין הגדרות", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_back_to_start_location) {
            setMapToLocation(AZZA_METUDELA_LOCATION, 17);
            return true;
        }
        if (id == R.id.action_fetch_markers) {
            addAccidentsMarkers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Move the camera to cuurent user location(recevied from gps sensors)
     */
    private void centerMapOnMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
            setMapToLocation(new LatLng(location.getLatitude(), location.getLongitude()), 16);

    }

    private void addAccidentsMarkers() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        int zoomLevel = (int)map.getCameraPosition().zoom;
        List<Accident> accidents = new Accidents(bounds, zoomLevel).getAccidents();

        /*
        TODO make the map not static and private again, find a way to add markers
        for(Accident a : accidents) {
            map.addMarker(new MarkerOptions()
                    .title(a.getTitle())
                    .snippet(a.getDescription() + "\n" + a.getAddress())
                    .position(a.getLocation()));
        }
        */
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // TODO
        // Show accident specific details
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO
        // Add new accident,
        Toast.makeText(this, "Long pressed: " + latLng, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //TODO get accidents for current location
        //Toast.makeText(this, cameraPosition.target.toString(), Toast.LENGTH_LONG).show();
        //LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

    }
}
