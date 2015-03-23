package il.co.anyway.app;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AccidentsManager {

    public static final boolean DO_RESET = true;
    public static final boolean DO_NOT_RESET = false;
    private final String LOG_TAG = AccidentsManager.class.getSimpleName();

    private List<Accident> accidentsList;

    public AccidentsManager() {
        accidentsList = new ArrayList<>();
    }

    private boolean isAccidentExist(Accident toCheck) {

        for(Accident a : accidentsList)
            if(a.getId() == toCheck.getId()) {

                return true;
            }

        return false;
    }

    public boolean addAccident(Accident toAdd) {

        if(!isAccidentExist(toAdd)) {
            accidentsList.add(toAdd);
            //TODO updateMap();
        }
        else
            return false;

        return true;
    }

    public int addAllAccidents(List<Accident> toAddList, boolean reset) {

        if(reset == DO_RESET)
            accidentsList.clear();

        int counter = 0;

        for(Accident a : toAddList) {
            if(!isAccidentExist(a)) {
                accidentsList.add(a);

                counter++;
            }
        }

        //TODO updateMap();
        Log.i(LOG_TAG, counter + " accidents of " + toAddList.size() + " added");
        return counter;
    }

    public Accident getAccidentByMarkerID(String markerID) {

        for(Accident a : accidentsList) {
            if (a.getMarkerID().equals(markerID)) {
                return a;
            }
        }

        return null;
    }

    public List<Accident> getAllAccidents() {
        return accidentsList;
    }

    public List<Accident> getAllNewAccidents() {

        List<Accident> newAccidents = new ArrayList<>();

        for(Accident a : accidentsList) {
            if (a.getMarkerID() == null) {
                newAccidents.add(a);
            }
        }

        return newAccidents;
    }
}
