package il.co.anyway.app.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Discussion {

    private LatLng location;
    private int type;
    private long id;
    private Date created;
    private String title;
    private String identifier;

    private boolean markerAddedToMap;

    public Discussion() {
        markerAddedToMap = false;
    }

    public boolean isMarkerAddedToMap() {
        return markerAddedToMap;
    }

    public Discussion setMarkerAddedToMap(boolean markerAddedToMap) {
        this.markerAddedToMap = markerAddedToMap;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Discussion setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Discussion setCreated(Date created) {
        this.created = created;
        return this;
    }

    public long getId() {
        return id;
    }

    public Discussion setId(long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Discussion setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getType() {
        return type;
    }

    public Discussion setType(int type) {
        this.type = type;
        return this;
    }

    public LatLng getLocation() {
        return location;
    }

    public Discussion setLocation(LatLng location) {
        this.location = location;
        return this;
    }
}
