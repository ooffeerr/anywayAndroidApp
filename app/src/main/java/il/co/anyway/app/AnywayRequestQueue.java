package il.co.anyway.app;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

// fetching accidents from Anyway servers
public class AnywayRequestQueue {

    public final static String ANYWAY_BASE_URL = "http://www.anyway.co.il/";
    public final static String ANYWAY_MARKERS_BASE_URL = ANYWAY_BASE_URL + "markers?";
    private final static String LOG_TAG = AnywayRequestQueue.class.getSimpleName();
    private static AnywayRequestQueue instance = null;
    RequestQueue mRequestQueue;
    private MainActivity callingActivity;

    private AnywayRequestQueue(MainActivity mainActivity) {

        callingActivity = mainActivity;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(callingActivity.getCacheDir(), 1024 * 1024 * 2); // 2MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();
    }

    public static AnywayRequestQueue getInstance(MainActivity activity) {
        if (instance == null)
            instance = new AnywayRequestQueue(activity);
        return instance;
    }

    public void addRequest(double ne_lat, double ne_lng, double sw_lat, double sw_lng, int zoom,
                           String start_date, String end_date,
                           boolean show_fatal, boolean show_severe, boolean show_light, boolean show_inaccurate) {

        final String DEFAULT_REQUEST_FORMAT = "json";

        // Construct the URL for the Anyway accidents query
        Uri builtUri = Uri.parse(ANYWAY_MARKERS_BASE_URL).buildUpon()
                .appendQueryParameter("ne_lat", Double.toString(ne_lat))
                .appendQueryParameter("ne_lng", Double.toString(ne_lng))
                .appendQueryParameter("sw_lat", Double.toString(sw_lat))
                .appendQueryParameter("sw_lng", Double.toString(sw_lng))
                .appendQueryParameter("zoom", Integer.toString(zoom))
                .appendQueryParameter("start_date", start_date)
                .appendQueryParameter("end_date", end_date)
                .appendQueryParameter("show_fatal", show_fatal ? "1" : "0")
                .appendQueryParameter("show_severe", show_severe ? "1" : "0")
                .appendQueryParameter("show_light", show_light ? "1" : "0")
                .appendQueryParameter("show_inaccurate", show_inaccurate ? "1" : "0")
                .appendQueryParameter("format", DEFAULT_REQUEST_FORMAT)
                .build();

        try {
            URL url = new URL(builtUri.toString());
            addRequest(url.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error building the URL: " + e.getMessage());
        }
    }

    private void addRequest(String url) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        List<Accident> fetchedAccidents = Utility.getAccidentDataFromJson(response);
                        if (fetchedAccidents != null)
                            AccidentsManager.getInstance().addAllAccidents(fetchedAccidents, AccidentsManager.DO_NOT_RESET);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        // Add the request to the RequestQueue.
        mRequestQueue.add(jsObjRequest);

    }
}