package il.co.anyway.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

// fetching accidents from Anyway servers
public class FetchAccidents extends AsyncTask<String, Void, ArrayList<Accident>> {

    @SuppressWarnings("unused")
    private final String LOG_TAG = FetchAccidents.class.getSimpleName();

    private MainActivity callingActivity;

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
                    .appendQueryParameter("ne_lat", params[Utility.JSON_STRING_NE_LAT])
                    .appendQueryParameter("ne_lng", params[Utility.JSON_STRING_NE_LNG])
                    .appendQueryParameter("sw_lat", params[Utility.JSON_STRING_SW_LAT])
                    .appendQueryParameter("sw_lng", params[Utility.JSON_STRING_SW_LNG])
                    .appendQueryParameter("zoom", params[Utility.JSON_STRING_ZOOM_LEVEL])
                    .appendQueryParameter("start_date", params[Utility.JSON_STRING_START_DATE])
                    .appendQueryParameter("end_date", params[Utility.JSON_STRING_END_DATE])
                    .appendQueryParameter("show_fatal", params[Utility.JSON_STRING_SHOW_FATAL])
                    .appendQueryParameter("show_severe", params[Utility.JSON_STRING_SHOW_SEVERE])
                    .appendQueryParameter("show_light", params[Utility.JSON_STRING_SHOW_LIGHT])
                    .appendQueryParameter("show_inaccurate", params[Utility.JSON_STRING_SHOW_INACCURATE])
                    .appendQueryParameter("format", params[Utility.JSON_STRING_FORMAT])
                    .build();

            URL url = new URL(builtUri.toString());
            //Log.i(LOG_TAG, "URL: " + url);

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
            try {
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Could not load accidents from server");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            accidentJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping to parse it.
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
            return Utility.getAccidentDataFromJson(accidentJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            //e.printStackTrace();
        }

        return null;

    }


    @Override
    protected void onPostExecute(ArrayList<Accident> accidents) {
        super.onPostExecute(accidents);

        // remove previous markers add accidents to the map
        if(accidents != null) {
            callingActivity.setAccidentsListAndUpdateMap(accidents);
        }
    }

    public void setCallingActivity(MainActivity callingActivity) {
        this.callingActivity = callingActivity;
    }
}