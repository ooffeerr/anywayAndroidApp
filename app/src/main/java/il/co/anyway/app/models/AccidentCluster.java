package il.co.anyway.app.models;

import com.google.android.gms.maps.model.LatLng;

public class AccidentCluster {

    private LatLng location;
    private int count;

    public AccidentCluster(int count, LatLng location) {
        this.count = count;
        this.location = location;
    }

    public int getCount() {
        return count;
    }

    public LatLng getLocation() {
        return location;
    }
}
