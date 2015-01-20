package il.co.anyway.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity implements OnInfoWindowClickListener,
        OnMapLongClickListener, OnCameraChangeListener {

    private static final LatLng AZZA_METUDELA_LOCATION = new LatLng(31.772126, 35.213678);
    private static boolean CLEAR_MAP_AFTER_EACH_FETCH = true;
    public static final int MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS = 15;


    private final int MAXIMUM_CHARACTERS_IN_INFO_WINDOWS = 100;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    // index of parameters in the parameters array for
    public static final int JSON_STRING_PARAMETERS_COUNT = 12;
    public static final int JSON_STRING_NE_LAT = 0;
    public static final int JSON_STRING_NE_LNG = 1;
    public static final int JSON_STRING_SW_LAT = 2;
    public static final int JSON_STRING_SW_LNG = 3;
    public static final int JSON_STRING_ZOOM_LEVEL = 4;
    public static final int JSON_STRING_START_DATE = 5;
    public static final int JSON_STRING_END_DATE = 6;
    public static final int JSON_STRING_SHOW_FATAL = 7;
    public static final int JSON_STRING_SHOW_SEVERE = 8;
    public static final int JSON_STRING_SHOW_LIGHT = 9;
    public static final int JSON_STRING_SHOW_INACCURATE = 10;
    public static final int JSON_STRING_FORMAT = 11;

    private GoogleMap map;
    private List<Accident> accidentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_back_to_start_location) {
            centerMapOnMyLocation();
            return true;
        }
        if (id == R.id.action_fetch_markers) {
            getAccidentsFromASyncTask();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        // TODO when this enabled, updating happing too much, not allowing to focus on marker
        //getAccidentsFromASyncTask();
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

        // try to move map to user location, if not succeed go to default
        if(!centerMapOnMyLocation())
            setMapToLocation(AZZA_METUDELA_LOCATION, 17);


        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnCameraChangeListener(this);

        getAccidentsFromASyncTask();
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

    /**
     * Move the camera to cuurent user location(recevied from gps sensors)
     */
    private boolean centerMapOnMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            setMapToLocation(new LatLng(location.getLatitude(), location.getLongitude()), 16);
            return true;
        }
        else {
            return false;
        }

    }


    private void getAccidentsFromASyncTask() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        int zoomLevel = (int) map.getCameraPosition().zoom;

        if(zoomLevel < MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS) {
            Toast.makeText(this, getString(R.string.zoom_in_to_display), Toast.LENGTH_LONG).show();
            return;
        }

        FetchAccidents accidentTask = new FetchAccidents();

        String[] params = new String[JSON_STRING_PARAMETERS_COUNT];
        params[JSON_STRING_NE_LAT] = Double.toString(bounds.northeast.latitude);
        params[JSON_STRING_NE_LNG] = Double.toString(bounds.northeast.longitude);
        params[JSON_STRING_SW_LAT] = Double.toString(bounds.southwest.latitude);
        params[JSON_STRING_SW_LNG] = Double.toString(bounds.southwest.longitude);
        params[JSON_STRING_ZOOM_LEVEL] = Integer.toString(zoomLevel);

        // TODO all this setting needs to come form user preferences
        params[JSON_STRING_START_DATE] = Long.toString(new Date(2013, 01, 01).getTime());
        params[JSON_STRING_END_DATE] = Long.toString(new Date(2014, 01, 01).getTime());

        // Get preferences form SharedPreferncses
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean show_fatal = sharedPrefs.getBoolean(getString(R.string.pref_accidents_fatal_key), true);
        Boolean show_severe = sharedPrefs.getBoolean(getString(R.string.pref_accidents_severe_key), true);
        Boolean show_light = sharedPrefs.getBoolean(getString(R.string.pref_accidents_light_key), true);
        Boolean show_inaccurate = sharedPrefs.getBoolean(getString(R.string.pref_accidents_inaccurate_key), false);

        params[JSON_STRING_SHOW_FATAL] = show_fatal ? "1" : "0";
        params[JSON_STRING_SHOW_SEVERE] = show_severe ? "1" : "0";
        params[JSON_STRING_SHOW_LIGHT] = show_light ? "1" : "0";
        params[JSON_STRING_SHOW_INACCURATE] = show_inaccurate ? "1" : "0";
        params[JSON_STRING_FORMAT] = "json";

        accidentTask.setCallingActivity(this);
        accidentTask.execute(params);
    }

    // add accidents from array list to map
    private void addAccidentsToMap(boolean clearMap) {

        if(clearMap)
            map.clear();

        for(Accident a : accidentsList) {

            // make sure info windows don't get to messy
            String desc = a.getDescription();
            if(desc.length() > MAXIMUM_CHARACTERS_IN_INFO_WINDOWS)
                desc = desc.substring(0, 30).concat("...");

            map.addMarker(new MarkerOptions()
                    .title(a.getTitle())
                    .snippet(desc)
                    .position(a.getLocation()));
        }
    }

    public void setAccidentsListAndUpdateMap(List<Accident> accidentsList) {
        this.accidentsList = accidentsList;
        addAccidentsToMap(CLEAR_MAP_AFTER_EACH_FETCH);
    }
}
