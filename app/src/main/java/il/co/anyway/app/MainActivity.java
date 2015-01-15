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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MainActivity extends ActionBarActivity implements OnInfoWindowClickListener {

    private final LatLng AZZA_METUDELA = new LatLng(31.772126, 35.213678);

    private GoogleMap map;
    private boolean needsInit=false;
    private static boolean debug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map  = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

        if (savedInstanceState == null) {
            needsInit=true;
        }

        if(needsInit) {
            // set map location to current location
            if(debug)
                setMapToLocation(AZZA_METUDELA, 17);
            else
                centerMapOnMyLocation();
        }
        addAccidentsMarkers();

        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);
    }

    // move map to specific location
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
            setMapToLocation(AZZA_METUDELA, 17);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void centerMapOnMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

    }

    private void addAccidentsMarkers() {
        List<Accident> accidents = new Accidents().getAccidents();
        for(Accident a : accidents) {
            map.addMarker(new MarkerOptions()
                    .title(a.getTitle())
                    .snippet(a.getDescription() + "\n" + a.getAddress())
                    .position(a.getLocation()));
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }
}
