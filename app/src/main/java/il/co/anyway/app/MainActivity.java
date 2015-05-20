package il.co.anyway.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMapLoadedCallback;
import com.androidmapsextensions.GoogleMap.OnMapLongClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.GoogleMap.OnMyLocationButtonClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.anyway.app.models.Accident;
import il.co.anyway.app.models.AccidentCluster;
import il.co.anyway.app.models.Discussion;

public class MainActivity extends ActionBarActivity
        implements
        OnInfoWindowClickListener,
        OnMapLongClickListener,
        OnCameraChangeListener,
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        OnMapLoadedCallback,
        OnMarkerClickListener {

    private static final LatLng START_LOCATION = new LatLng(31.774511, 35.011642);
    private static final int START_ZOOM_LEVEL = 7;
    private static final int MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS = 16;
    private static final Locale APP_DEFAULT_LOCALE = new Locale("he_IL");

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;
    private FragmentManager fm;
    private SupportMapFragment mapFragment;
    private LocationManager mLocationManager;
    private boolean mFirstRun;
    private boolean mMapIsInClusterMode;
    private boolean mDoubleBackToExitPressedOnce;

    private List<AccidentCluster> mLastAccidentsClusters = null;

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // force double pressing on back key to leave discussion
        mDoubleBackToExitPressedOnce = false;

        // map start in START_ZOOM_LEVEL
        mMapIsInClusterMode = true;

        // find out if this is the first instance of the activity
        mFirstRun = (savedInstanceState == null);

        // get location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // check if gps enabled, if not - offer the user to turn it on
        boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled && mFirstRun)
            new EnableGpsDialogFragment().show(getSupportFragmentManager(), "");

        // add Preference changed listener int order to update map data after preference changed
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                        // reset accident manager
                        MarkersManager.getInstance().addAllAccidents(null, MarkersManager.DO_RESET);
                        mMap.clear();
                        getMarkersFromServer();

                    }
                };
        prefs.registerOnSharedPreferenceChangeListener(mSharedPrefListener);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(mSharedPrefListener);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // set the map fragment and call the map settings
        setUpMapIfNeeded();

        // check if app opened by a link to specific location, if so - move to that location
        getDataFromSharedURL();

        MarkersManager.getInstance().registerListenerActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        MarkersManager.getInstance().unregisterListenerActivity();
    }

    private void setUpMapIfNeeded() {
        fm = getSupportFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();

            FragmentTransaction tx = fm.beginTransaction();
            tx.add(R.id.map_container, mapFragment);
            tx.commit();
        }
        mapFragment.getExtendedMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // set map to start location if previous instance exist
        if (mFirstRun)
            setMapToLocation(START_LOCATION, START_ZOOM_LEVEL, false);

        // Set Clustering
        ClusteringSettings settings = new ClusteringSettings();
        settings.clusterOptionsProvider(new AnywayClusterOptionsProvider(getResources())).addMarkersDynamically(true);
        mMap.setClustering(settings);

        // Enable location buttons
        mMap.setMyLocationEnabled(true);

        // Disable toolbar on the right bottom corner(taking user to google maps app)
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Enable zoom +/- controls and compass
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // set listener and layout to markers
        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);

        // set listener to open Disqus on long click
        mMap.setOnMapLongClickListener(this);

        // set listener to fetch accidents from server when map change
        mMap.setOnCameraChangeListener(this);

        // set listener to force minimum zoom level when user click on MyLocation button
        mMap.setOnMyLocationButtonClickListener(this);

        // when map finish rendering - set camera to user location
        mMap.setOnMapLoadedCallback(this);

        // set listener for marker click, enable to zoom in automatically when cluster marker is clicked
        mMap.setOnMarkerClickListener(this);

        // accident manager is static, so me need to make sure the markers of accident re-added to the map
        MarkersManager.getInstance().setAllMarkersAsNotShownOnTheMap();
        mMap.clear();

        // call both markers and clusters, only one of them will run, the other return
        addMarkersToMap();
        addClustersToMap(mLastAccidentsClusters);
    }

    @Override
    public void onMapLoaded() {

        // if the user didn't opened the map before try to set the map to user location
        if (mFirstRun) {

            Location myLocation = mMap.getMyLocation();
            if (myLocation == null)
                myLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(new Criteria(), false));

            if (myLocation != null)
                setMapToLocation(myLocation, MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS, true);

            mFirstRun = false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_search) {
            showSearchDialog();
            return true;
        }
        if (id == R.id.action_share) {
            String currentStringUri = Utility.getCurrentPositionStringURI(mMap.getCameraPosition().target,
                    (int) mMap.getCameraPosition().zoom, this);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            i.putExtra(Intent.EXTRA_TEXT, currentStringUri);
            startActivity(Intent.createChooser(i, getString(R.string.share_title)));
            return true;
        }
        if (id == R.id.action_report_bug) {

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "samuel.regev@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "בעייה באפליקציית Anyway");
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(emailIntent, getString(R.string.action_report_bug)));

        }
        if (id == R.id.action_about) {
            showAboutInfoDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        int currentZoomLevel = (int) mMap.getCameraPosition().zoom;

        if (marker.getData() instanceof Discussion) {
            Intent disqusIntent = new Intent(this, DisqusActivity.class);
            disqusIntent.putExtra(DisqusActivity.DISQUS_TALK_IDENTIFIER, ((Discussion) marker.getData()).getIdentifier());
            startActivity(disqusIntent);
            return true;
        }

        // zoom in one level when clicking on AccidentCluster marker
        if (marker.getData() instanceof AccidentCluster) {

            int requiredZoomLevel = currentZoomLevel + 2;

            AccidentCluster accidentCluster = marker.getData();
            switch(accidentCluster.getCount()) {
                case 1:
                    requiredZoomLevel = MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS + 2;
                    break;
                case 2:
                    requiredZoomLevel = MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS + 1;
                    break;
                case 3:
                    requiredZoomLevel = MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS;
            }

            setMapToLocation(marker.getPosition(), requiredZoomLevel, true);

            return true;
        }

        /*
        if the marker is cluster zoom-in automatically to MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS + 1
        returning 'true' means no need to further handling, 'false' will cause InfoWindow to appear
        */
        if (marker.isCluster()) {
            if (currentZoomLevel < MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS + 1) {
                setMapToLocation(marker.getPosition(), MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS + 1, true);
                return true;
            } else
                return false;
        }

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (marker.getData() instanceof Accident)
            showAccidentDetailsInDialog((Accident) marker.getData());

        else if (marker.isCluster())
            showAccidentsClusterAsListDialog(marker);

    }

    /**
     * Show list dialog of accidents inside marker cluster
     *
     * @param marker Marker cluster
     */
    private void showAccidentsClusterAsListDialog(Marker marker) {
        if (!marker.isCluster())
            return;

        // re-arrange all the accident titles in String array for the AlertDialog
        final List<Marker> markersInCluster = marker.getMarkers();
        final String[] accidentsList = new String[markersInCluster.size()];
        for (int i = 0; i < markersInCluster.size(); i++) {

            Object markerData = markersInCluster.get(i).getData();
            if (markerData instanceof Accident) {

                Accident accident = (Accident) markerData;
                accidentsList[i] =
                        Utility.getAccidentTypeByIndex(accident.getSubType(), this) +
                                " - " +
                                accident.getCreatedDateAsString();
            }
        }

        // show list dialog with all the accidents in the cluster
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(accidentsList.length + " " + getString(R.string.accidents));
        adb.setItems(accidentsList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (markersInCluster.get(which).getData() instanceof Accident) {
                    Accident a = markersInCluster.get(which).getData();
                    showAccidentDetailsInDialog(a);
                }
            }
        });
        adb.show();
    }

    /**
     * Show accident details in dialog
     *
     * @param a Accident to show
     */
    private void showAccidentDetailsInDialog(Accident a) {
        Bundle args = new Bundle();
        args.putString("description", a.getDescription());
        args.putString("titleBySubType", Utility.getAccidentTypeByIndex(a.getSubType(), this));
        args.putLong("id", a.getId());
        args.putString("address", a.getAddress());
        args.putString("created", a.getCreatedDateAsString());

        AccidentDetailsDialogFragment accidentDetailsDialog =
                new AccidentDetailsDialogFragment();
        accidentDetailsDialog.setArguments(args);
        accidentDetailsDialog.show(getSupportFragmentManager(), "accidentDetails");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO show some kind of dialog to confirm opening new discussion

        /*
        Intent disqusIntent = new Intent(this, DisqusActivity.class);
        disqusIntent.putExtra(DisqusActivity.DISQUS_LOCATION_ID, latLng);
        startActivity(disqusIntent);
        */
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        int zoomLevel = (int) mMap.getCameraPosition().zoom;

        if (zoomLevel < MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS && !mMapIsInClusterMode) {

            mMapIsInClusterMode = true;
            mMap.clear();
            MarkersManager.getInstance().setAllMarkersAsNotShownOnTheMap();

        } else if (zoomLevel >= MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS && mMapIsInClusterMode) {

            mMapIsInClusterMode = false;
            mLastAccidentsClusters = null;
            mMap.clear();
            addMarkersToMap();

        }

        getMarkersFromServer();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        setMapToLocation(mMap.getMyLocation(), MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS, true);
        return true;
    }

    /**
     * check if user opened the intent from a link shared with him, like: http://www.anyway.co.il/...parameters
     *
     * @return true if user open the app from the link and all parameters are correct, false otherwise
     */
    private boolean getDataFromSharedURL() {

        // url parameters: start_date, end_date, show_fatal, show_severe, show_light, show_inaccurate, zoom, lat, lon

        Uri data = getIntent().getData();
        if (data == null)
            return false;
        else {

            Log.i(LOG_TAG + "Url", "query: " + data.getQuery());

            String start_date_str = data.getQueryParameter("start_date");
            String end_date_str = data.getQueryParameter("end_date");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date start_date;
            Date end_date;

            int show_fatal, show_severe, show_light, show_inaccurate, zoom;
            double latitude, longitude;
            try {
                show_fatal = Integer.parseInt(data.getQueryParameter("show_fatal"));
                show_severe = Integer.parseInt(data.getQueryParameter("show_severe"));
                show_light = Integer.parseInt(data.getQueryParameter("show_light"));
                show_inaccurate = Integer.parseInt(data.getQueryParameter("show_inaccurate"));
                zoom = Integer.parseInt(data.getQueryParameter("zoom"));
                latitude = Double.parseDouble(data.getQueryParameter("lat"));
                longitude = Double.parseDouble(data.getQueryParameter("lon"));

                start_date = dateFormat.parse(start_date_str);
                end_date = dateFormat.parse(end_date_str);

                Log.i(LOG_TAG + "Url", "All args from url parsed successfully");

                setSettingsAndMoveToLocation(start_date, end_date, show_fatal, show_severe, show_light, show_inaccurate, zoom, latitude, longitude);

                return true;
            } catch (Exception e) {
                // NumberFormatException || ParseException
                Log.e(LOG_TAG + "_URL", e.getMessage());
                return false;
            }
        }
    }

    /**
     * Save all parameters to SharedPreferences and move to lat,lng specified
     *
     * @param start_date      start date
     * @param end_date        end date
     * @param show_fatal      show fatal accidents
     * @param show_severe     show severe accidents
     * @param show_light      show light accidents
     * @param show_inaccurate show inaccurate location accidents
     * @param zoom            zoom level
     * @param latitude        latitude to move to
     * @param longitude       longitude to move to
     */
    private void setSettingsAndMoveToLocation(Date start_date, Date end_date,
                                              int show_fatal, int show_severe, int show_light, int show_inaccurate,
                                              int zoom, double latitude, double longitude) {

        // TODO create a special view for links result instead of replacing use settings

        // Get preferences form SharedPreferncses
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        sharedPrefs.edit().putBoolean(getString(R.string.pref_accidents_fatal_key), show_fatal == 1)
                .putBoolean(getString(R.string.pref_accidents_severe_key), show_severe == 1)
                .putBoolean(getString(R.string.pref_accidents_light_key), show_light == 1)
                .putBoolean(getString(R.string.pref_accidents_inaccurate_key), show_inaccurate == 1)
                .putString(getString(R.string.pref_from_date_key), df.format(start_date))
                .putString(getString(R.string.pref_to_date_key), df.format(end_date))
                .apply();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(latitude, longitude), zoom)));
    }

    // action handler for address search
    private void showSearchDialog() {

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
                            } else {
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
                                setMapToLocation(p, MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS, true);

                                mMap.addMarker(new MarkerOptions().position(p).title(getString(R.string.search_result)).snippet(addressList[which]).clusterGroup(ClusterGroup.NOT_CLUSTERED));
                            }
                        });
                adb.show();
            } else {
                // address not found, prompt user
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(getString(R.string.address_not_found_title));
                adb.setMessage(getString(R.string.address_not_found_details));
                adb.setPositiveButton(getString(R.string.close), null);
                adb.show();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }
    }

    /**
     * move the camera to specific location, if the map is ready
     *
     * @param location  location to move to
     * @param zoomLevel zoom level of the map after new location set
     * @param animate   animate the map movement if true
     */
    private boolean setMapToLocation(Location location, int zoomLevel, boolean animate) {
        if (location == null)
            return false;

        if (animate)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel), null);
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));

        return true;
    }

    /**
     * move the camera to specific location, if the map is ready
     *
     * @param latLng    location to move to
     * @param zoomLevel zoom level of the map after new location set
     * @param animate   animate the map movement if true
     */
    private boolean setMapToLocation(LatLng latLng, int zoomLevel, boolean animate) {
        Location l = new Location("");
        l.setLongitude(latLng.longitude);
        l.setLatitude(latLng.latitude);

        return setMapToLocation(l, zoomLevel, animate);
    }

    /**
     * get parameters and start fetching accidents for current view and settings
     */
    private void getMarkersFromServer() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        int zoomLevel = (int) mMap.getCameraPosition().zoom;

        // fetch accidents from anyway or fetch from cluster decided by zoom level
        if (zoomLevel >= MINIMUM_ZOOM_LEVEL_TO_SHOW_ACCIDENTS)
            Utility.getMarkersByParameters(bounds, zoomLevel, this);
        else
            new FetchClusteredAccidents(bounds, zoomLevel, this);
    }

    public void addClustersToMap(List<AccidentCluster> accidentClusterList) {

        if (accidentClusterList == null)
            return;

        if (!mMapIsInClusterMode)
            return;

        mMap.clear();
        for (AccidentCluster ac : accidentClusterList) {

            // add cluster marker to map
            Marker m = mMap.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .position(ac.getLocation())
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                            Utility.drawTextToBitmap(this,
                                                    Utility.getClusterImageByCountOfAccidents(ac.getCount()),
                                                    Integer.toString(ac.getCount())
                                            )
                                    )
                            )
            );

            m.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
            m.setData(ac);

        }

        mLastAccidentsClusters = accidentClusterList;
    }

    /**
     * get all accidents and discussions that is not on the map from MarkersManager and add them to map
     */
    public void addMarkersToMap() {

        if (mMapIsInClusterMode)
            return;

        // add new accidents
        List<Accident> newAccidents = MarkersManager.getInstance().getAllNewAccidents();
        for (Accident a : newAccidents)
            addAccidentToMap(a);

        // add new discussions
        List<Discussion> newDiscussions = MarkersManager.getInstance().getAllNewDiscussions();
        for (Discussion d : newDiscussions)
            addDiscussionToMap(d);
    }

    /**
     * Add accident marker to map
     *
     * @param a Accident to add
     */
    public void addAccidentToMap(Accident a) {

        // if map is in cluster mode, do not add accident marker
        if (mMapIsInClusterMode)
            return;

        mMap.addMarker(new MarkerOptions()
                .title(Utility.getAccidentTypeByIndex(a.getSubType(), this))
                .snippet(getString(R.string.marker_default_desc))
                .icon(BitmapDescriptorFactory.fromResource(Utility.getIconForMarker(a.getSeverity(), a.getSubType())))
                .position(a.getLocation()))
                .setData(a);

        a.setMarkerAddedToMap(true);
    }

    /**
     * Add Discussion marker to map
     *
     * @param d Discussion to add
     */
    public void addDiscussionToMap(Discussion d) {

        // if map is in cluster mode, do not add discussion marker
        if (mMapIsInClusterMode)
            return;

        // TODO decide cluster group

        Marker m = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.discussion))
                .position(d.getLocation()));

        m.setData(d);
        m.setClusterGroup(ClusterGroup.NOT_CLUSTERED);

        d.setMarkerAddedToMap(true);
    }

    private void showAboutInfoDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setView(getLayoutInflater().inflate(R.layout.info_dialog, null));
        adb.setPositiveButton(getString(R.string.close), null);
        adb.show();
    }

    @Override
    public void onBackPressed() {

        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }
}
