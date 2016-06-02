package il.co.anyway.app;

import android.location.Location;
import android.location.LocationListener;

/**
 * Contract for location related data.
 */
public interface ILocationManager extends com.google.android.gms.location.LocationListener {
    /**
     * @return the last known location, or null if not available.
     */
    Location getLocation();

    void updateLocation(MainActivity mainActivity);
}
