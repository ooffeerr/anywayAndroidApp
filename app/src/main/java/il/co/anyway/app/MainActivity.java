package il.co.anyway.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements OnInfoWindowClickListener,
        OnMapLongClickListener, OnCameraChangeListener, LocationListener {

    @SuppressWarnings("unused")
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final LatLng AZZA_METUDELA_LOCATION = new LatLng(31.772126, 35.213678);

    private static final int MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS = 16;
    private static final Locale APP_DEFAULT_LOCALE = new Locale("he_IL");

    private static boolean ANIMATE_ON = true;
    private static boolean ANIMATE_OFF = false;

    private GoogleMap mMap;
    private AccidentsManager mAccidentsManager;
    private LocationManager mLocationManager;
    private String mProvider;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccidentsManager = AccidentsManager.getInstance();

        Boolean firstRun = savedInstanceState==null ? true:false;

        // Get the location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use default
        Criteria criteria = new Criteria();
        mProvider = mLocationManager.getBestProvider(criteria, false);
        mLocation = mLocationManager.getLastKnownLocation(mProvider);

        // check if gps enabled, if not - offer the user to turn it on
        boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled && firstRun)
            new EnableGpsDialogFragment().show(getSupportFragmentManager(), "");

        setUpMapIfNeeded(firstRun);
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
        if (id == R.id.action_search) {
            showSearchDialog();
            return true;
        }
        if (id == R.id.action_share) {

            String currentStringUri = getCurrentPositionStringURI();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            i.putExtra(Intent.EXTRA_TEXT, currentStringUri);
            startActivity(Intent.createChooser(i, getString(R.string.share_title)));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Build URL to anyway map from current view for sharing
     * @return the URL as String
     */
    private String getCurrentPositionStringURI() {

        LatLng location = mMap.getCameraPosition().target;
        int zoomLevel = (int) mMap.getCameraPosition().zoom;
        String[] params = Utility.getMarkersUriParams(mMap.getProjection().getVisibleRegion().latLngBounds, zoomLevel, this);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String fromDate = sharedPrefs.getString(getString(R.string.pref_from_date_key), getString(R.string.pref_default_from_date));
        String toDate = sharedPrefs.getString(getString(R.string.pref_to_date_key), getString(R.string.pref_default_to_date));

        // re format the dates to yyyy-MM-dd for the url sharing
        fromDate = DatePreference.getYear(fromDate) + "-" + DatePreference.getMonth(fromDate) + "-" + DatePreference.getDate(fromDate);
        toDate = DatePreference.getYear(toDate) + "-" + DatePreference.getMonth(toDate) + "-" + DatePreference.getDate(toDate);

        Uri builtUri = Uri.parse(FetchAccidents.ANYWAY_BASE_URL).buildUpon()
                .appendQueryParameter("start_date", fromDate)
                .appendQueryParameter("end_date", toDate)
                .appendQueryParameter("show_fatal", params[Utility.JSON_STRING_SHOW_FATAL])
                .appendQueryParameter("show_severe", params[Utility.JSON_STRING_SHOW_SEVERE])
                .appendQueryParameter("show_light", params[Utility.JSON_STRING_SHOW_LIGHT])
                .appendQueryParameter("show_inaccurate", params[Utility.JSON_STRING_SHOW_INACCURATE])
                .appendQueryParameter("zoom", params[Utility.JSON_STRING_ZOOM_LEVEL])
                .appendQueryParameter("lat", Double.toString(location.latitude))
                .appendQueryParameter("lon", Double.toString(location.longitude))
                .build();

        return builtUri.toString();
    }

    /**
     * when marker is clicked, find and show the accident details of this marker
     *
     * @param marker marker clicked
     */
    @Override
    public void onInfoWindowClick(Marker marker) {

        // If the marker is just the search address marker, do nothing
        if (marker.getTitle().equals(getString(R.string.search_result)))
            return;

        // findAccidentByMarkerID
        String markerID = marker.getId();

        Bundle args = new Bundle();

        Accident a = mAccidentsManager.getAccidentByMarkerID(markerID);
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

        Intent disqusIntent = new Intent(this, DisqusActivity.class);
        disqusIntent.putExtra(DisqusActivity.DISQUS_LOCATION_ID, latLng);
        startActivity(disqusIntent);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        // TODO - currently if zoom level is too high -> just do nothing. needed server side clustering
        int zoomLevel = (int) mMap.getCameraPosition().zoom;
        if (zoomLevel < MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS)
            Toast.makeText(getBaseContext(), getString(R.string.zoom_in_to_display), Toast.LENGTH_LONG).show();
        else
            getAccidentsFromServer();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLocation = location;
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
        mLocationManager.requestLocationUpdates(mProvider, 400, 1, this);

        // force APP_DEFAULT_LOCALE on the app
        Locale.setDefault(APP_DEFAULT_LOCALE);
        Configuration config = new Configuration();
        config.locale = APP_DEFAULT_LOCALE;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    /* Remove the location listener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    // action handler for address search
    public void showSearchDialog() {
/*
            // hide the keyboard
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
*/

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View addressDialogView = getLayoutInflater().inflate(R.layout.address_search_dialog, null);

        final AlertDialog searchDialog = new AlertDialog.Builder(this)
                .setView(addressDialogView)
                .setPositiveButton(R.string.search, null) //Set to null. We override the onclick
                .setNegativeButton(R.string.cancel, null)
                .create();

        searchDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = searchDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        TextView searchTextView = (TextView) addressDialogView.findViewById(R.id.address_search);

                        if (searchTextView != null) {
                            if (searchTextView.getText().toString().equals("")) {
                                Toast t = Toast.makeText(getBaseContext(), getString(R.string.address_empty), Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.CENTER, 0, 0);
                                t.show();
                            }
                            else {
                                searchAddress(searchTextView.getText().toString());
                                searchDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        searchDialog.show();
    }

    /**
     * Search for an address, show a dialog and move the map to the searched location
     *
     * @param addressToSearch The address to search, in free speech
     */
    private void searchAddress(String addressToSearch) {

        Geocoder geoCoder = new Geocoder(this, APP_DEFAULT_LOCALE);
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
                                setMapToLocation(p, MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS, ANIMATE_ON);

                                mMap.addMarker(new MarkerOptions().position(p).title(getString(R.string.search_result)).snippet(addressList[which]));
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
           Log.e(LOG_TAG, e.getLocalizedMessage());
        }
    }

    private void setUpMapIfNeeded(boolean firstRun) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(firstRun);
            }
        }
    }

    private void setUpMap(boolean firstRun) {

        // Enable location buttons
        mMap.setMyLocationEnabled(true);

        // Disable toolbar on the right bottom corner(taking user to google maps app)
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);

        if (firstRun) {
            // try to move map to user location, if not user location found go to default
            if (!setMapToLocation(mLocation, MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS, ANIMATE_ON))
                setMapToLocation(AZZA_METUDELA_LOCATION, MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS, ANIMATE_ON);
        } else {
            /*
            this happening only on screen rotation, markers have been delete from the map
            because the map is re-render, clear marker's ids mark the accident as not on the map
             */
            mAccidentsManager.clearMarkersIDs();
            addAccidentsToMap();
        }
    }

    /**
     * move the camera to specific location, should be called on after checking map!=null
     * when camera finish moving - fetching accidents of current location
     *
     * @param location  location to move to
     * @param zoomLevel move camera to this specific
     */
    private boolean setMapToLocation(Location location, int zoomLevel, boolean animate) {
        if (location == null)
            return false;

        if (animate)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel),
                new CancelableCallback() {

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onCancel() {

                    }
                });
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));

        return true;
    }

    private boolean setMapToLocation(LatLng location, int zoomLevel, boolean animate) {
        Location l = new Location("");
        l.setLongitude(location.longitude);
        l.setLatitude(location.latitude);

        return setMapToLocation(l, zoomLevel, animate);
    }

    private void getAccidentsFromServer() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        int zoomLevel = (int) mMap.getCameraPosition().zoom;
        Utility.getAccidentsFromASyncTask(bounds, zoomLevel, this);
    }

    // add accidents from array list to map
    private void addAccidentsToMap() {

        for (Accident a : mAccidentsManager.getAllNewAccidents()) {

            Marker m = mMap.addMarker(new MarkerOptions()
                    .title(Utility.getAccidentTypeByIndex(a.getSubType(), getApplicationContext()))
                    .snippet(getString(R.string.marker_default_desc))
                    .icon(BitmapDescriptorFactory.fromResource(Utility.getIconForMarker(a.getSeverity(), a.getSubType())))
                    .position(a.getLocation()));

            a.setMarkerID(m.getId());
        }
    }

    /**
     * add all the accidents from the list to the map
     *
     * @param accidentsToAddList List of Accidents objects
     */
    public void setAccidentsListAndUpdateMap(List<Accident> accidentsToAddList) {
        int accidentsAddedCounter = mAccidentsManager.addAllAccidents(accidentsToAddList, AccidentsManager.DO_NOT_RESET);
        if (accidentsAddedCounter > 0) {
            addAccidentsToMap();
            Log.i(LOG_TAG, accidentsAddedCounter + " Added to map");
        }
    }
}
