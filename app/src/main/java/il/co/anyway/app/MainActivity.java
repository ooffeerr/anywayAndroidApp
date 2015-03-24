package il.co.anyway.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.GoogleMap.CancelableCallback;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMapLongClickListener;

import com.google.android.gms.maps.CameraUpdateFactory;


import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainActivity extends ActionBarActivity implements OnInfoWindowClickListener,
        OnMapLongClickListener, OnCameraChangeListener, LocationListener {

    @SuppressWarnings("unused")
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final LatLng AZZA_METUDELA_LOCATION = new LatLng(31.772126, 35.213678);
    private static final int MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS = 16;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private AccidentsManager accidents;
    private LocationManager locationManager;
    private String provider;
    private Location location;
    private int nextMarkerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accidents = new AccidentsManager();

        // first run set to true only when this is the first time onCreate called
        // used to handle the case of screen rotation
        boolean firstRun = false;
        if (savedInstanceState == null)
            firstRun = true;

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(provider);

        // check if gps enabled, if not - offer the user to turn it on
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled && firstRun)
            new EnableGpsDialogFragment().show(getSupportFragmentManager(), "");

        setUpMapIfNeeded(firstRun);

        /*
         the real marker id is set by google maps API, i'm saving the marker id in order
         to find accident by a marker
          */
        nextMarkerID = 0;
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
            setMapToMyLocationAndAddMarkers();
            return true;
        }
        if (id == R.id.action_fetch_markers) {
            getAccidentsFromServer();
            return true;
        }
        if (id == R.id.action_search) {
            showSearchDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * when marker is clicked, find and show the accident details of this marker
     *
     * @param marker marker clicked
     */
    @Override
    public void onInfoWindowClick(Marker marker) {

        if (marker.isCluster())
            return;

        // If the marker is just the search address marker, do nothing
        if (marker.getTitle().equals(getString(R.string.search_result)))
            return;

        // findAccidentByMarkerID
        String markerID = marker.getId();

        Bundle args = new Bundle();

        Accident a = accidents.getAccidentByMarkerID(markerID);
        if (a != null) {

            args.putString("description", a.getDescription());
            args.putString("titleBySubType", Utility.getAccidentTypeByIndex(a.getSubType(), getApplicationContext()));
            args.putLong("id", a.getId());
            args.putString("address", a.getAddress());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String strCreated = dateFormat.format(a.getCreated());
            args.putString("created", strCreated);
        }

        AccidentDetailsDialogFragment accidentDetailsDialog =
                new AccidentDetailsDialogFragment();
        accidentDetailsDialog.setArguments(args);
        accidentDetailsDialog.show(getSupportFragmentManager(), "accidentDetails");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO open location based discussion
        Toast.makeText(this, "Long pressed: " + latLng, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        getAccidentsFromServer();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the location listener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    // action handler for address search
    public void showSearchDialog() {
/*
            // hide the keyboard
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View addressDialogView = inflater.inflate(R.layout.address_search_dialog, null);
        builder.setView(addressDialogView)
                // Add action buttons
                .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        TextView searchTextView = (TextView) addressDialogView.findViewById(R.id.address_search);
                        searchAddress(searchTextView.getText().toString());

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    /**
     * Search for an address, show a dialog and move the map to the searched location
     *
     * @param addressToSearch The address to search, in free speech
     */
    private void searchAddress(String addressToSearch) {

        Geocoder geoCoder = new Geocoder(this);
        final int MAX_RESULTS = 7;

        try {
            // Search for the address
            final List<Address> addresses = geoCoder.getFromLocationName(addressToSearch, MAX_RESULTS);

            if (addresses.size() > 0) {
                // re-arrange all the address in String array for the AlertDialog
                final String[] addressList = new String[addresses.size()];
                for (int i = 0; i < addresses.size(); i++) {

                    // Address received as an address lines, join them all to one line
                    String tempAddress = "";
                    for (int j = 0; j <= addresses.get(i).getMaxAddressLineIndex(); j++)
                        tempAddress += addresses.get(i).getAddressLine(j) + ", ";

                    // remove the last ", " from the address
                    tempAddress = tempAddress.substring(0, tempAddress.length() - 2);
                    // add it to the array, the index match to the address checked
                    addressList[i] = tempAddress;
                }

                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.address_result_title))
                        .setItems(addressList, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                LatLng p = new LatLng(addresses.get(which).getLatitude(), addresses.get(which).getLongitude());
                                setMapToLocationAndAddMarkers(p);

                                // TODO - when markers behavior set, check this marker,
                                // now the marker disappear when getting new markers
                                map.addMarker(new MarkerOptions().position(p).title(getString(R.string.search_result)).snippet(addressList[which]));
                                nextMarkerID++;
                            }
                        });
                adb.show();
            } else {
                // address not found, prompt user
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.address_not_found_title));
                adb.setMessage(getString(R.string.address_not_found_details));
                adb.setPositiveButton(getString(R.string.address_not_found_close), null);
                adb.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpMapIfNeeded(boolean firstRun) {

        FragmentManager fm = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();;
            FragmentTransaction tx = fm.beginTransaction();
            tx.add(R.id.map_container, mapFragment);
            tx.commit();
        }

        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = mapFragment.getExtendedMap();
            if (map != null) {
                setUpMap(firstRun);
            }
        }
    }

    private void setUpMap(boolean firstRun) {

        ClusteringSettings settings = new ClusteringSettings();
        //CLUSTERING_ENABLED_DYNAMIC
        settings.clusterOptionsProvider(new AnywayClusterOptionsProvider(getResources())).addMarkersDynamically(true);
        map.setClustering(settings);

        // Enable location buttons
        map.setMyLocationEnabled(true);

        // Hide My Location button
        // this because it implemented is the action bar
        map.getUiSettings().setMyLocationButtonEnabled(false);

        // Disable toolbar on the right bottom corner(taking user to google maps app)
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnCameraChangeListener(this);

        if (firstRun) {

            // try to move map to user location, if not succeed go to default
            if (!setMapToMyLocationAndAddMarkers())
                setMapToLocationAndAddMarkers(AZZA_METUDELA_LOCATION, 17);

        } else {
            // this happening only on screen rotation, markers have been delete so re-fetch them but do not move map
            // calling only getAccidentsFromASyncTask is not working because it happening too fast and map is not initialized yet
            LatLng currentLocation = map.getCameraPosition().target;
            int currentZoomLevel = (int) map.getCameraPosition().zoom;
            setMapToLocationAndAddMarkers(currentLocation, currentZoomLevel);
        }
    }

    /**
     * move the camera to specific location, should be called on after checking map!=null
     * when camera finish moving - fetching accidents of current location
     *
     * @param location  location to move to
     * @param zoomLevel move camera to this specific
     */
    private void setMapToLocationAndAddMarkers(LatLng location, int zoomLevel) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.latitude, location.longitude), zoomLevel),
                new CancelableCallback() {

                    @Override
                    public void onFinish() {
                        //getAccidentsFromServer();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    /**
     * same as setMapToLocationAndAddMarkers(LatLng location, int zoomLevel)
     * only zoom level is set to current value
     *
     * @param location location to move to
     */
    private void setMapToLocationAndAddMarkers(LatLng location) {
        setMapToLocationAndAddMarkers(location, (int) map.getCameraPosition().zoom);
    }

    /**
     * Move the camera to current user location(received from gps sensors)
     *
     * @return true if location is found and set, false otherwise
     */
    private boolean setMapToMyLocationAndAddMarkers() {

        if (location != null) {
            setMapToLocationAndAddMarkers(new LatLng(location.getLatitude(), location.getLongitude()));
            return true;
        } else {
            return false;
        }

    }

    private void getAccidentsFromServer() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        int zoomLevel = (int) map.getCameraPosition().zoom;

        if (zoomLevel < MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS) {
            // If zoom level too high, move the camera to minimum zoom level required
            Toast.makeText(getBaseContext(), getString(R.string.zoom_in_to_display), Toast.LENGTH_LONG).show();

            LatLng currentLocation = map.getCameraPosition().target;
            setMapToLocationAndAddMarkers(currentLocation, MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS);

            // setMapToLocationAndAddMarkers calls this method again when it finish moving the camera, so no need to keep going
            return;
        }

        Utility.getAccidentsFromASyncTask(bounds, zoomLevel, this);
    }

    // add accidents from array list to map
    private void addAccidentsToMap() {

        for (Accident a : accidents.getAllNewAccidents()) {

            map.addMarker(new MarkerOptions()
                    .title(Utility.getAccidentTypeByIndex(a.getSubType(), getApplicationContext()))
                    .snippet(getString(R.string.marker_default_desc))
                    .icon(BitmapDescriptorFactory.fromResource(Utility.getIconForMarker(a.getSeverity(), a.getSubType())))
                    .position(a.getLocation()));

            a.setMarkerID("m" + nextMarkerID);
            nextMarkerID++;
        }
    }

    /**
     * add all the accidents from the list to the map
     *
     * @param accidentsToAddList
     */
    public void setAccidentsListAndUpdateMap(List<Accident> accidentsToAddList) {
        int accidentsAddedCounter = accidents.addAllAccidents(accidentsToAddList, AccidentsManager.DO_NOT_RESET);
        if (accidentsAddedCounter > 0) {
            addAccidentsToMap();
            Log.i(LOG_TAG, accidentsAddedCounter + " Added to map");
        }
    }

}
