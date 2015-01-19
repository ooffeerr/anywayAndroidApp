package il.co.anyway.app;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Accident {

    private final String LOG_TAG = Accident.class.getSimpleName();
    private long id;
    private long user;
    private String title;
    private String description;
    private int type;
    private int subType;
    private int severity;
    private Date created;
    private LatLng location;
    private String address;
    private int locationAccuracy;

    public Accident(long id,
                    long user,
                    String title,
                    String description,
                    int type,
                    int subType,
                    int severity,
                    Date created,
                    LatLng location,
                    String address,
                    int locationAccuracy) {

        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.type = type;
        this.subType = subType;
        this.severity = severity;
        this.created = created;
        this.location = location;
        this.address = address;
        this.locationAccuracy = locationAccuracy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLocationAccuracy() {
        return locationAccuracy;
    }

    public void setLocationAccuracy(int locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
    }
}
