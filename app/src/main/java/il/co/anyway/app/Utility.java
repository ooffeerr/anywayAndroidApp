package il.co.anyway.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    // index of parameters in the parameters array for fetching accidents from server task
    public static final int JSON_STRING_PARAMETERS_COUNT = 12;
    public static final int JSON_STRING_NE_LAT = 0;
    public static final int JSON_STRING_NE_LNG = 1;
    public static final int JSON_STRING_SW_LAT = 2;
    public static final int JSON_STRING_SW_LNG = 3;
    public static final int JSON_STRING_ZOOM_LEVEL = 4;
    public static final int JSON_STRING_START_DATE = 5;
    public static final int JSON_STRING_END_DATE = 6;
    public static final int JSON_STRING_SHOW_FATAL = 7;
    public static final int JSON_STRING_SHOW_SEVERE = 8;
    public static final int JSON_STRING_SHOW_LIGHT = 9;
    public static final int JSON_STRING_SHOW_INACCURATE = 10;
    public static final int JSON_STRING_FORMAT = 11;

    /**
     * Parse JSON string to accidents list
     *
     * @param accidentJsonStr JSON string to parse
     * @return all accidents from json string as List<Accident>
     * @throws JSONException
     */
    public static List<Accident> getAccidentDataFromJson(String accidentJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String ACCIDENT_LIST = "markers";
        final String ACCIDENT_ADDRESS = "address";
        final String ACCIDENT_CREATED = "created";
        final String ACCIDENT_DESC = "description";
        final String ACCIDENT_ID = "id";
        final String ACCIDENT_LATITUDE = "latitude";
        final String ACCIDENT_LONGITUDE = "longitude";
        final String ACCIDENT_LOCATOIN_ACCURACY = "locationAccuracy";
        final String ACCIDENT_SEVERITY = "severity";
        final String ACCIDENT_TYPE = "type";
        final String ACCIDENT_SUBTYPE = "subtype";
        final String ACCIDENT_TITLE = "title";

        // user, following, followers - not implemented right now at anyway
        // TODO - implement users interface
        long user = 0;

        JSONObject accidentJson = new JSONObject(accidentJsonStr);
        JSONArray accidentsArray = accidentJson.getJSONArray(ACCIDENT_LIST);

        List<Accident> resultList = new ArrayList<>();
        for (int i = 0; i < accidentsArray.length(); i++) {

            // Get the JSON object representing the day
            JSONObject accidentDetails = accidentsArray.getJSONObject(i);

            // Date comes as 2013-12-30T21:00:00, needs to be converted
            String created = accidentDetails.getString(ACCIDENT_CREATED);
            Date createdDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                createdDate = sdf.parse(created);
            } catch (ParseException e) {
                Log.e(LOG_TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }

            try {

                String address = accidentDetails.getString(ACCIDENT_ADDRESS);
                String desc = accidentDetails.getString(ACCIDENT_DESC);
                String title = accidentDetails.getString(ACCIDENT_TITLE);
                Long id = accidentDetails.getLong(ACCIDENT_ID);

                // Log.d(LOG_TAG, "Accident id:" + id);

                Double lat = accidentDetails.getDouble(ACCIDENT_LATITUDE);
                Double lng = accidentDetails.getDouble(ACCIDENT_LONGITUDE);
                LatLng location = new LatLng(lat, lng);

                Integer accuracy = accidentDetails.getInt(ACCIDENT_LOCATOIN_ACCURACY);
                Integer severity = accidentDetails.getInt(ACCIDENT_SEVERITY);
                Integer type = accidentDetails.getInt(ACCIDENT_TYPE);
                Integer subtype = accidentDetails.getInt(ACCIDENT_SUBTYPE);

                Accident acc = new Accident()
                        .setId(id)
                        .setUser(user)
                        .setTitle(title)
                        .setDescription(desc)
                        .setType(type)
                        .setSubType(subtype)
                        .setSeverity(severity)
                        .setCreated(createdDate)
                        .setLocation(location)
                        .setAddress(address)
                        .setLocationAccuracy(accuracy)
                        .setMarkerID(null);

                resultList.add(acc);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getLocalizedMessage());
            }

        }
        return resultList;
    }

    /**
     * set all the variables needed for pulling accident data from Anyway API
     *
     * @param bounds          map bounds
     * @param zoomLevel       map zoom level
     * @param callingActivity the calling activity(will get updates when pulling data from server end)
     */
    public static void getAccidentsFromASyncTask(LatLngBounds bounds, int zoomLevel, MainActivity callingActivity) {

        FetchAccidents accidentTask = new FetchAccidents();

        String[] params = new String[JSON_STRING_PARAMETERS_COUNT];
        params[JSON_STRING_NE_LAT] = Double.toString(bounds.northeast.latitude);
        params[JSON_STRING_NE_LNG] = Double.toString(bounds.northeast.longitude);
        params[JSON_STRING_SW_LAT] = Double.toString(bounds.southwest.latitude);
        params[JSON_STRING_SW_LNG] = Double.toString(bounds.southwest.longitude);
        params[JSON_STRING_ZOOM_LEVEL] = Integer.toString(zoomLevel);

        // Get preferences form SharedPreferncses
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(callingActivity);

        Boolean show_fatal = sharedPrefs.getBoolean(callingActivity.getString(R.string.pref_accidents_fatal_key), true);
        Boolean show_severe = sharedPrefs.getBoolean(callingActivity.getString(R.string.pref_accidents_severe_key), true);
        Boolean show_light = sharedPrefs.getBoolean(callingActivity.getString(R.string.pref_accidents_light_key), true);
        Boolean show_inaccurate = sharedPrefs.getBoolean(callingActivity.getString(R.string.pref_accidents_inaccurate_key), false);

        String fromDate = sharedPrefs.getString(callingActivity.getString(R.string.pref_from_date_key), callingActivity.getString(R.string.pref_default_from_date));
        String toDate = sharedPrefs.getString(callingActivity.getString(R.string.pref_to_date_key), callingActivity.getString(R.string.pref_default_to_date));

        // getting timestamp for Anyway API
        params[JSON_STRING_START_DATE] = Utility.getTimeStamp(fromDate);
        params[JSON_STRING_END_DATE] = Utility.getTimeStamp(toDate);

        params[JSON_STRING_SHOW_FATAL] = show_fatal ? "1" : "0";
        params[JSON_STRING_SHOW_SEVERE] = show_severe ? "1" : "0";
        params[JSON_STRING_SHOW_LIGHT] = show_light ? "1" : "0";
        params[JSON_STRING_SHOW_INACCURATE] = show_inaccurate ? "1" : "0";
        params[JSON_STRING_FORMAT] = "json";

        accidentTask.setCallingActivity(callingActivity);
        accidentTask.execute(params);
    }

    /**
     * Covert date object to timestamp
     *
     * @param date java.util.Date object
     * @return TimeStamp, formatted to Anyway API requirements
     */
    public static String getTimeStamp(Date date) {
        Long ts = new Timestamp(date.getTime()).getTime() / 1000;
        return Long.toString(ts);
    }

    /**
     * Covert date(saved as string) to timestamp
     *
     * @param dateStr Date as String, in dd/MM/yyyy format
     * @return TimeStamp, formatted to Anyway API requirements
     */
    public static String getTimeStamp(String dateStr) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;

        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.e(LOG_TAG + "_TIME_STAMP", e.getMessage());
            e.printStackTrace();
        }

        return getTimeStamp(date);
    }

    /**
     * @param accidentSubType The type of the accident
     * @param appContext      Application Context
     * @return A string describing the accident type
     */
    public static String getAccidentTypeByIndex(int accidentSubType, Context appContext) {
        String str = "";

        switch (accidentSubType) {
            case Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_CAR_TO_PEDESTRIAN);
                break;
            case Accident.ACCIDENT_TYPE_FRONT_TO_SIDE:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_FRONT_TO_SIDE);
                break;
            case Accident.ACCIDENT_TYPE_FRONT_TO_REAR:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_FRONT_TO_REAR);
                break;
            case Accident.ACCIDENT_TYPE_SIDE_TO_SIDE:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_SIDE_TO_SIDE);
                break;
            case Accident.ACCIDENT_TYPE_FRONT_TO_FRONT:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_FRONT_TO_FRONT);
                break;
            case Accident.ACCIDENT_TYPE_WITH_STOPPED_CAR_NO_PARKING:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_WITH_STOPPED_CAR_NO_PARKING);
                break;
            case Accident.ACCIDENT_TYPE_WITH_STOPPED_CAR_PARKING:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_WITH_STOPPED_CAR_PARKING);
                break;
            case Accident.ACCIDENT_TYPE_WITH_STILL_OBJECT:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_WITH_STILL_OBJECT);
                break;
            case Accident.ACCIDENT_TYPE_OFF_ROAD_OR_SIDEWALK:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_OFF_ROAD_OR_SIDEWALK);
                break;
            case Accident.ACCIDENT_TYPE_ROLLOVER:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_ROLLOVER);
                break;
            case Accident.ACCIDENT_TYPE_SKID:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_SKID);
                break;
            case Accident.ACCIDENT_TYPE_HIT_PASSSENGER_IN_CAR:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_HIT_PASSSENGER_IN_CAR);
                break;
            case Accident.ACCIDENT_TYPE_FALLING_OFF_MOVING_VEHICLE:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_FALLING_OFF_MOVING_VEHICLE);
                break;
            case Accident.ACCIDENT_TYPE_FIRE:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_FIRE);
                break;
            case Accident.ACCIDENT_TYPE_OTHER:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_OTHER);
                break;
            case Accident.ACCIDENT_TYPE_BACK_TO_FRONT:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_BACK_TO_FRONT);
                break;
            case Accident.ACCIDENT_TYPE_BACK_TO_SIDE:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_BACK_TO_SIDE);
                break;
            case Accident.ACCIDENT_TYPE_WITH_ANIMAL:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_WITH_ANIMAL);
                break;
            case Accident.ACCIDENT_TYPE_WITH_VEHICLE_LOAD:
                str = appContext.getString(R.string.str_ACCIDENT_TYPE_WITH_VEHICLE_LOAD);
                break;
        }

        return str;
    }

    /*public static String getAccidentSeverityByIndex(int severity) {
        String str = "";

        //TODO decide if we need this method

        return str;
    }*/

    /**
     * Choose which icon to show on the map(for the marker)
     *
     * @param severity        The severity of the accident
     * @param accidentSubType the type of the accident
     * @return ID of a drawable icon
     */
    public static int getIconForMarker(int severity, int accidentSubType) {

        int innerType = 0;

        // we only have 3 different icons so we need to choose which one to use for each type of accident
        switch (accidentSubType) {

            case Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN;
                break;
            case Accident.ACCIDENT_TYPE_FRONT_TO_SIDE:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_FRONT_TO_REAR:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_SIDE_TO_SIDE:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_FRONT_TO_FRONT:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_WITH_STOPPED_CAR_NO_PARKING:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_WITH_STOPPED_CAR_PARKING:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_WITH_STILL_OBJECT:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_OFF_ROAD_OR_SIDEWALK:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_ROLLOVER:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_SKID:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_HIT_PASSSENGER_IN_CAR:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_FALLING_OFF_MOVING_VEHICLE:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_FIRE:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_OTHER:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case Accident.ACCIDENT_TYPE_BACK_TO_FRONT:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_BACK_TO_SIDE:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case Accident.ACCIDENT_TYPE_WITH_ANIMAL:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN;
                break;
            case Accident.ACCIDENT_TYPE_WITH_VEHICLE_LOAD:
                innerType = Accident.ACCIDENT_TYPE_CAR_TO_CAR;
                break;
        }

        // choose the icon to show by the icon type and the severity
        int icon = 0;
        if (accidentSubType == Accident.ACCIDENT_MULTIPLE) {
            switch (severity) {
                case Accident.SEVERITY_FATAL:
                    icon = R.drawable.multiple_lethal;
                    break;
                case Accident.SEVERITY_SEVERE:
                    icon = R.drawable.multiple_severe;
                    break;
                case Accident.SEVERITY_LIGHT:
                    icon = R.drawable.multiple_medium;
                    break;
                case Accident.SEVERITY_VARIOUS:
                    icon = R.drawable.multiple_various;
                    break;
            }
        } else if (severity == Accident.SEVERITY_FATAL) {
            switch (innerType) {
                case Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                    icon = R.drawable.vehicle_person_lethal;
                    break;
                case Accident.ACCIDENT_TYPE_CAR_TO_CAR:
                    icon = R.drawable.vehicle_person_lethal;
                    break;
                case Accident.ACCIDENT_TYPE_CAR_TO_OBJECT:
                    icon = R.drawable.vehicle_object_lethal;
                    break;
            }
        } else if (severity == Accident.SEVERITY_SEVERE) {
            switch (innerType) {
                case Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                    icon = R.drawable.vehicle_person_severe;
                    break;
                case Accident.ACCIDENT_TYPE_CAR_TO_CAR:
                    icon = R.drawable.vehicle_vehicle_severe;
                    break;
                case Accident.ACCIDENT_TYPE_CAR_TO_OBJECT:
                    icon = R.drawable.vehicle_object_severe;
                    break;
            }
        } else if (severity == Accident.SEVERITY_LIGHT) {
            switch (innerType) {
                case Accident.ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                    icon = R.drawable.vehicle_person_medium;
                    break;
                case Accident.ACCIDENT_TYPE_CAR_TO_CAR:
                    icon = R.drawable.vehicle_vehicle_medium;
                    break;
                case Accident.ACCIDENT_TYPE_CAR_TO_OBJECT:
                    icon = R.drawable.vehicle_object_medium;
                    break;
            }
        }

        return icon;
    }
}

