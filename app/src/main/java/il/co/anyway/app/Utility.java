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
}
