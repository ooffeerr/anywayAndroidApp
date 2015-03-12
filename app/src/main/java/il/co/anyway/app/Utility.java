package il.co.anyway.app;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final int SEVERITY_FATAL = 1;
    public static final int SEVERITY_SEVERE = 2;
    public static final int SEVERITY_LIGHT = 3;
    public static final int SEVERITY_VARIOUS = 4;

    public static final int ACCIDENT_MULTIPLE = -10;

    /**
     * Parse JSON string to accidents list
     * @param accidentJsonStr JSON string to parse
     * @return all accidents from json string as ArrayList<Accident>
     * @throws JSONException
     */
    public static ArrayList<Accident> getAccidentDataFromJson(String accidentJsonStr)
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
        final String ACCIDENT_TYPE= "type";
        final String ACCIDENT_SUBTYPE = "subtype";
        final String ACCIDENT_TITLE = "title";

        // user, following, followers - not implemented right now at anyway
        // TODO - implement users interface
        long user = 0;

        JSONObject accidentJson = new JSONObject(accidentJsonStr);
        JSONArray accidentsArray = accidentJson.getJSONArray(ACCIDENT_LIST);

        ArrayList<Accident> resultList = new ArrayList<>();
        for(int i = 0; i < accidentsArray.length(); i++) {

            // Get the JSON object representing the day
            JSONObject accidentDetails = accidentsArray.getJSONObject(i);

            // Date comes as 2013-12-30T21:00:00, needs to be converted
            String created = accidentDetails.getString(ACCIDENT_CREATED);
            Date createdDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                createdDate = sdf.parse(created);
            } catch (ParseException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

            String address = accidentDetails.getString(ACCIDENT_ADDRESS);
            String desc = accidentDetails.getString(ACCIDENT_DESC);
            String title = accidentDetails.getString(ACCIDENT_TITLE);
            Integer id = accidentDetails.getInt(ACCIDENT_ID);

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
                    .setLocationAccuracy(accuracy);

            resultList.add(acc);
        }
        return resultList;
    }

    /**
     * Covert date object to timestamp
     * @param date java.util.Date object
     * @return TimeStamp, formatted to Anyway API requirements
     */
    public static String getTimeStamp(Date date) {
        Long ts = new Timestamp(date.getTime()).getTime()/1000;
        return Long.toString(ts);
    }

    /**
     * Covert date(saved as string) to timestamp
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

    public static String getAccidentTypeByIndex(int accidentSubType) {
        String str = "";

        switch (accidentSubType) {
            case 1:
                str = "פגיעה בהולך רגל";
                break;
            case 2:
                str = "התנגשות חזית אל צד";
                break;
            case 3:
                str = "התנגשות חזית באחור";
                break;
            case 4:
                str = "התנגשות צד בצד";
                break;
            case 5:
                str = "התנגשות חזית אל חזית";
                break;
            case 6:
                str = "התנגשות עם רכב שנעצר ללא חניה";
                break;
            case 7:
                str = "התנגשות עם רכב חונה";
                break;
            case 8:
                str = "התנגשות עם עצם דומם";
                break;
            case 9:
                str = "ירידה מהכביש או עלייה למדרכה";
                break;
            case 10:
                str = "התהפכות";
                break;
            case 11:
                str = "החלקה";
                break;
            case 12:
                str = "פגיעה בנוסע בתוך כלי רכב";
                break;
            case 13:
                str = "נפילה ברכב נע";
                break;
            case 14:
                str = "שריפה";
                break;
            case 15:
                str = "אחר";
                break;
            case 17:
                str = "התנגשות אחור אל חזית";
                break;
            case 18:
                str = "התנגשות אחור אל צד";
                break;
            case 19:
                str = "התנגשות עם בעל חיים";
                break;
            case 20:
                str = "פגיעה ממטען של רכב";
                break;
        }

        return str;
    }

    public static String getAccidentSeverityByIndex(int severity) {
        String str = "";

/*
"HUMRAT_TEUNA": {
1: "קטלנית",
2: "קשה",
3: "קלה",
}
*/

        return str;
    }

    public static int getIconForMarker(int severity, int accidentSubType) {

        int innerType = 0;

        final int ACCIDENT_TYPE_CAR_TO_CAR =-1; // Synthetic type
        final int ACCIDENT_TYPE_CAR_TO_OBJECT = -2; // Synthetic type
        final int ACCIDENT_TYPE_CAR_TO_PEDESTRIAN = 1;
        final int ACCIDENT_TYPE_FRONT_TO_SIDE = 2;
        final int ACCIDENT_TYPE_FRONT_TO_REAR = 3;
        final int ACCIDENT_TYPE_SIDE_TO_SIDE = 4;
        final int ACCIDENT_TYPE_FRONT_TO_FRONT = 5;
        final int ACCIDENT_TYPE_WITH_STOPPED_CAR_NO_PARKING = 6;
        final int ACCIDENT_TYPE_WITH_STOPPED_CAR_PARKING = 7;
        final int ACCIDENT_TYPE_WITH_STILL_OBJECT = 8;
        final int ACCIDENT_TYPE_OFF_ROAD_OR_SIDEWALK = 9;
        final int ACCIDENT_TYPE_ROLLOVER = 10;
        final int ACCIDENT_TYPE_SKID = 11;
        final int ACCIDENT_TYPE_HIT_PASSSENGER_IN_CAR = 12;
        final int ACCIDENT_TYPE_FALLING_OFF_MOVING_VEHICLE = 13;
        final int ACCIDENT_TYPE_FIRE = 14;
        final int ACCIDENT_TYPE_OTHER = 15;
        final int ACCIDENT_TYPE_BACK_TO_FRONT = 17;
        final int ACCIDENT_TYPE_BACK_TO_SIDE = 18;
        final int ACCIDENT_TYPE_WITH_ANIMAL = 19;
        final int ACCIDENT_TYPE_WITH_VEHICLE_LOAD = 20;

        switch (accidentSubType) {

            case ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                innerType = ACCIDENT_TYPE_CAR_TO_PEDESTRIAN;
                break;
            case ACCIDENT_TYPE_FRONT_TO_SIDE:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_FRONT_TO_REAR:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_SIDE_TO_SIDE:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_FRONT_TO_FRONT:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_WITH_STOPPED_CAR_NO_PARKING:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_WITH_STOPPED_CAR_PARKING:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_WITH_STILL_OBJECT:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_OFF_ROAD_OR_SIDEWALK:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_ROLLOVER:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_SKID:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_HIT_PASSSENGER_IN_CAR:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_FALLING_OFF_MOVING_VEHICLE:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_FIRE:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_OTHER:
                innerType = ACCIDENT_TYPE_CAR_TO_OBJECT;
                break;
            case ACCIDENT_TYPE_BACK_TO_FRONT:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_BACK_TO_SIDE:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
            case ACCIDENT_TYPE_WITH_ANIMAL:
                innerType = ACCIDENT_TYPE_CAR_TO_PEDESTRIAN;
                break;
            case ACCIDENT_TYPE_WITH_VEHICLE_LOAD:
                innerType = ACCIDENT_TYPE_CAR_TO_CAR;
                break;
        }

        int icon = 0;

        if(accidentSubType == ACCIDENT_MULTIPLE) {
            switch(severity) {
                case SEVERITY_FATAL:
                    icon = R.drawable.multiple_lethal;
                    break;
                case SEVERITY_SEVERE:
                    icon = R.drawable.multiple_severe;
                    break;
                case SEVERITY_LIGHT:
                    icon = R.drawable.multiple_medium;
                    break;
                case SEVERITY_VARIOUS:
                    icon = R.drawable.multiple_various;
                    break;
            }
        }
        else if(severity == SEVERITY_FATAL) {
            switch(innerType) {
                case ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                    icon = R.drawable.vehicle_person_lethal;
                    break;
                case ACCIDENT_TYPE_CAR_TO_CAR:
                    icon = R.drawable.vehicle_person_lethal;
                    break;
                case ACCIDENT_TYPE_CAR_TO_OBJECT:
                    icon = R.drawable.vehicle_object_lethal;
                    break;
            }
        }
        else if(severity == SEVERITY_SEVERE) {
            switch(innerType) {
                case ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                    icon = R.drawable.vehicle_person_severe;
                    break;
                case ACCIDENT_TYPE_CAR_TO_CAR:
                    icon = R.drawable.vehicle_vehicle_severe;
                    break;
                case ACCIDENT_TYPE_CAR_TO_OBJECT:
                    icon = R.drawable.vehicle_object_severe;
                    break;
            }
        }
        else if(severity == SEVERITY_LIGHT) {
            switch(innerType) {
                case ACCIDENT_TYPE_CAR_TO_PEDESTRIAN:
                    icon = R.drawable.vehicle_person_medium;
                    break;
                case ACCIDENT_TYPE_CAR_TO_CAR:
                    icon = R.drawable.vehicle_vehicle_medium;
                    break;
                case ACCIDENT_TYPE_CAR_TO_OBJECT:
                    icon = R.drawable.vehicle_object_medium;
                    break;
            }
        }

        return icon;
    }
}

