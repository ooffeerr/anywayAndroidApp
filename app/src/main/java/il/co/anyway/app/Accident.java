package il.co.anyway.app;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Accident {

    @SuppressWarnings("unused")
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

    public Accident() {}

    public long getId() {
        return id;
    }

    public Accident setId(long id) {
        this.id = id;
        return this;
    }

    public long getUser() {
        return user;
    }

    public Accident setUser(long user) {
        this.user = user;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Accident setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Accident setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getType() {
        return type;
    }

    public Accident setType(int type) {
        this.type = type;
        return this;
    }

    public int getSubType() {
        return subType;
    }

    public Accident setSubType(int subType) {
        this.subType = subType;
        return this;
    }

    public int getSeverity() {
        return severity;
    }

    public Accident setSeverity(int severity) {
        this.severity = severity;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Accident setCreated(Date created) {
        this.created = created;
        return this;
    }

    public LatLng getLocation() {
        return location;
    }

    public Accident setLocation(LatLng location) {
        this.location = location;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Accident setAddress(String address) {
        this.address = address;
        return this;
    }

    public int getLocationAccuracy() {
        return locationAccuracy;
    }

    public Accident setLocationAccuracy(int locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
        return this;
    }
}
