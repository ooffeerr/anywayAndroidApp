package il.co.anyway.app;


import java.util.ArrayList;
import java.util.List;

public class AccidentsListSameLatLng {

    private List<Accident> accidentList;
    private double mLatitude;
    private double mLongitude;

    public AccidentsListSameLatLng(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
        accidentList = new ArrayList<>();
    }

    public boolean addAccidentToList(Accident toAdd) {

        if (toAdd == null)
            return false;

        if (toAdd.getLocation().latitude == mLatitude &&
                toAdd.getLocation().longitude == mLongitude) {

            accidentList.add(toAdd);
            return true;
        }

        return false;
    }

    public List<Accident> getAccidentList() {
        return accidentList;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

}
