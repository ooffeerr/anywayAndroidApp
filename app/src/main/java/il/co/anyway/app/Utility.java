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

    public static ArrayList<Accident> getAccidentDataFromJson(String accidentJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MARKERS_LIST = "markers";
        final String MARKER_ADDRESS = "address";
        final String MARKER_CREATED = "created";
        final String MARKER_DESC = "description";
        final String MARKER_ID = "id";
        final String MARKER_LATITUDE = "latitude";
        final String MARKER_LONGITUDE = "longitude";
        final String MARKER_LOCATOIN_ACCURACY = "locationAccuracy";
        final String MARKER_SEVERITY = "severity";
        final String MARKER_TYPE = "type";
        final String MARKER_SUBTYPE = "subtype";
        final String MARKER_TITLE = "title";

        // user, following, followers - not implemented right now at anyway
        // TODO - implement users interface
        Long user = new Long(0);

        JSONObject accidentJson = new JSONObject(accidentJsonStr);
        JSONArray accidentsArray = accidentJson.getJSONArray(MARKERS_LIST);

        ArrayList<Accident> resultList = new ArrayList<>();
        for(int i = 0; i < accidentsArray.length(); i++) {

            // Get the JSON object representing the day
            JSONObject accidentDetails = accidentsArray.getJSONObject(i);

            // Date comes as 2013-12-30T21:00:00, needs to be converted
            String created = accidentDetails.getString(MARKER_CREATED);
            Date createdDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                createdDate = sdf.parse(created);
            } catch (ParseException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

            String address = accidentDetails.getString(MARKER_ADDRESS);
            String desc = accidentDetails.getString(MARKER_DESC);
            String title = accidentDetails.getString(MARKER_TITLE);
            Integer id = accidentDetails.getInt(MARKER_ID);

            Double lat = accidentDetails.getDouble(MARKER_LATITUDE);
            Double lng = accidentDetails.getDouble(MARKER_LONGITUDE);
            LatLng location = new LatLng(lat, lng);

            Integer accuracy = accidentDetails.getInt(MARKER_LOCATOIN_ACCURACY);
            Integer severity = accidentDetails.getInt(MARKER_SEVERITY);
            Integer type = accidentDetails.getInt(MARKER_TYPE);
            Integer subtype = accidentDetails.getInt(MARKER_SUBTYPE);

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


    public static String getTimeStamp(Date date) {
        Long ts = new Timestamp(date.getTime()).getTime()/1000;
        return Long.toString(ts);
    }

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
