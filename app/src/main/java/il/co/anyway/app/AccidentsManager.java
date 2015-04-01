package il.co.anyway.app;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AccidentsManager {

    public static final boolean DO_RESET = true;
    public static final boolean DO_NOT_RESET = false;

    private final String LOG_TAG = AccidentsManager.class.getSimpleName();

    private static AccidentsManager instance = null;
    private List<Accident> accidentsList;

    // making the default constructor private make sure there will be only one instance of the accidents manager
    private AccidentsManager() {
        accidentsList = new ArrayList<>();
    }

    public static AccidentsManager getInstance() {
        if (instance == null)
            instance = new AccidentsManager();
        return instance;
    }

    /**
     * check if an accident exist in the accident manager
     *
     * @param toCheck the Accident object to check
     * @return true if exist, false if not or toCheck is null
     */
    private boolean isAccidentExist(Accident toCheck) {

        if (toCheck == null)
            return false;

        for (Accident a : accidentsList)
            if (a.getId() == toCheck.getId()) {
                return true;
            }

        return false;
    }

    /**
     * Add accident to the list
     *
     * @param toAdd the Accident object to add
     * @return true if accident added, false if accident already exist, or toAdd is null
     */
    public boolean addAccident(Accident toAdd) {

        if (toAdd == null)
            return false;

        if (!isAccidentExist(toAdd)) {
            accidentsList.add(toAdd);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Add a list of accidents to the list
     *
     * @param toAddList the list of Accident objects
     * @param reset     use to select if you want to reset the list before adding the new list
     * @return How many accidents from the list actually taken(duplicate accident will ignore)
     */
    public int addAllAccidents(List<Accident> toAddList, boolean reset) {

        if (reset == DO_RESET)
            accidentsList.clear();

        if (toAddList == null)
            return 0;

        int counter = 0;

        for (Accident a : toAddList) {
            if (addAccident(a)) {
                counter++;
            }
        }


        Log.i(LOG_TAG, counter + " accidents of " + toAddList.size() + " added");
        return counter;
    }

    /**
     * @return the list of all accidents in the list
     */
    public List<Accident> getAllAccidents() {
        return accidentsList;
    }

    /**
     * Get all the accidents that not on the map
     *
     * @return a list of accidents that not on the map
     */
    public List<Accident> getAllNewAccidents() {

        List<Accident> newAccidents = new ArrayList<>();

        for (Accident a : accidentsList) {
            if (!a.isMarkerAddedToMap()) {
                newAccidents.add(a);
            }
        }

        return newAccidents;
    }

    /**
     * set all accident's marker id to null
     */
    public void setAllAccidentAsNotShownOnTheMap() {
        for (Accident a : accidentsList)
            a.setMarkerAddedToMap(false);

    }
}
