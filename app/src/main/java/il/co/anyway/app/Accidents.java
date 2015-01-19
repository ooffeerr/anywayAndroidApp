package il.co.anyway.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Accidents {

    private List<Accident> accidents;
    private final String LOG_TAG = Accident.class.getSimpleName();

    // index of parameters in the parameters array for
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

    public Accidents(LatLngBounds mapBounds, int zoomLevel) {
        accidents = new ArrayList<Accident>();

        getAccidentsForMapChange(mapBounds,zoomLevel);
    }

    public void getAccidentsForMapChange(LatLngBounds mapBounds, int zoomLevel) {
        FetchAccidents accidentTask = new FetchAccidents();

        String[] params = new String[JSON_STRING_PARAMETERS_COUNT];
        params[JSON_STRING_NE_LAT] = Double.toString(mapBounds.northeast.latitude);
        params[JSON_STRING_NE_LNG] = Double.toString(mapBounds.northeast.longitude);
        params[JSON_STRING_SW_LAT] = Double.toString(mapBounds.southwest.latitude);
        params[JSON_STRING_SW_LNG] = Double.toString(mapBounds.southwest.longitude);
        params[JSON_STRING_ZOOM_LEVEL] = Integer.toString(zoomLevel);

        // TODO all this setting needs to come form user preferences
        params[JSON_STRING_START_DATE] = Long.toString(new Date(2013, 01, 01).getTime());
        params[JSON_STRING_END_DATE] = Long.toString(new Date(2014, 01, 01).getTime());
        params[JSON_STRING_SHOW_FATAL] = "1";
        params[JSON_STRING_SHOW_SEVERE] = "1";
        params[JSON_STRING_SHOW_LIGHT] = "1";
        params[JSON_STRING_SHOW_INACCURATE] = "0";
        params[JSON_STRING_FORMAT] = "json";

        accidentTask.execute(params);
    }

    public void addAccidentToList(Accident acc) {
        accidents.add(acc);
    }

    public void addAccidentsToList(ArrayList<Accident> accs) {
        accidents.addAll(accs);
    }

    public void clearAccidentsList() {
        accidents.removeAll(accidents);
    }

    public List<Accident> getAccidents() {
        return accidents;
    }

    // fetching accidents from Anyway servers
    public class FetchAccidents extends AsyncTask<String, Void, ArrayList<Accident>> {

        private final String LOG_TAG = FetchAccidents.class.getSimpleName();

        @Override
        protected ArrayList<Accident> doInBackground(String... params) {

            // If there's no parameters, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String accidentJsonStr = null;

            try {
                // Construct the URL for the Anyway accidents query
                final String FORECAST_BASE_URL = "http://www.anyway.co.il/markers?";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter("ne_lat", params[Accidents.JSON_STRING_NE_LAT])
                        .appendQueryParameter("ne_lng", params[Accidents.JSON_STRING_NE_LNG])
                        .appendQueryParameter("sw_lat", params[Accidents.JSON_STRING_SW_LAT])
                        .appendQueryParameter("sw_lng", params[Accidents.JSON_STRING_SW_LNG])
                        .appendQueryParameter("zoom", params[Accidents.JSON_STRING_ZOOM_LEVEL])
                        // TODO
                        //.appendQueryParameter("start_date", params[Accidents.JSON_STRING_START_DATE])
                        //.appendQueryParameter("end_date", params[Accidents.JSON_STRING_END_DATE])
                        .appendQueryParameter("start_date", "1356991200")
                        .appendQueryParameter("end_date", "1388527200")
                        .appendQueryParameter("show_fatal", params[Accidents.JSON_STRING_SHOW_FATAL])
                        .appendQueryParameter("show_severe", params[Accidents.JSON_STRING_SHOW_SEVERE])
                        .appendQueryParameter("show_light", params[Accidents.JSON_STRING_SHOW_LIGHT])
                        .appendQueryParameter("show_inaccurate", params[Accidents.JSON_STRING_SHOW_INACCURATE])
                        .appendQueryParameter("format", params[Accidents.JSON_STRING_FORMAT])
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                accidentJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getAccidentDataFromJson(accidentJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;

        }

        /**
         * Take the String representing the complete accidents in JSON Format and
         * pull out the data we need to construct the markers on the map.
         */
        private ArrayList<Accident> getAccidentDataFromJson(String accidentJsonStr)
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

                Accident acc = new Accident(id, user, title, desc, type, subtype, severity, createdDate, location, address, accuracy);
                resultList.add(acc);
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<Accident> accidents) {
            super.onPostExecute(accidents);
            if(accidents != null) {
                clearAccidentsList();

                // TODO - call a function to update map instead of accessing the map directly
                //addAccidentsToList(accidents);

                MainActivity.map.clear();
                for(Accident a : accidents) {
                    MainActivity.map.addMarker(new MarkerOptions()
                            .title(a.getTitle())
                            .snippet(a.getDescription() + "\n" + a.getAddress())
                            .position(a.getLocation()));
                }
            }
        }
    }
}
