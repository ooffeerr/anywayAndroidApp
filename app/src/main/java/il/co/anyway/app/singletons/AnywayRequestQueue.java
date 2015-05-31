package il.co.anyway.app.singletons;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.anyway.app.DisqusActivity;
import il.co.anyway.app.PriorityJsonObjectRequest;
import il.co.anyway.app.R;
import il.co.anyway.app.Utility;
import il.co.anyway.app.models.Accident;
import il.co.anyway.app.models.Discussion;

// fetching accidents from Anyway servers
public class AnywayRequestQueue {

    public final static String ANYWAY_BASE_URL = "http://www.anyway.co.il/";
    public final static String ANYWAY_MARKERS_BASE_URL = ANYWAY_BASE_URL + "markers?";
    public final static String ANYWAY_DISCUSSION_POST_URL = ANYWAY_BASE_URL + "discussion";
    public final static String ANYWAY_HIGHLIGHT_POINTS = ANYWAY_BASE_URL + "highlightpoints";

    public final static int HIGHLIGHT_TYPE_USER_SEARCH = 1;
    public final static int HIGHLIGHT_TYPE_USER_GPS = 2;

    private final static String LOG_TAG = AnywayRequestQueue.class.getSimpleName();
    private static AnywayRequestQueue instance = null;
    RequestQueue mRequestQueue;

    private AnywayRequestQueue(Context context) {
        // Instantiate the RequestQueue - Volley will choose network and cache automatically.
        // This also start the queue
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static AnywayRequestQueue getInstance(Context context) {
        if (instance == null)
            instance = new AnywayRequestQueue(context);
        return instance;
    }

    public void addMarkersRequest(double ne_lat, double ne_lng, double sw_lat, double sw_lng, int zoom,
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
            addMarkersRequest(url.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error building the URL: " + e.getMessage());
        }
    }

    private void addMarkersRequest(String url) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        List<Accident> fetchedAccidents = new ArrayList<>();
                        List<Discussion> fetchedDiscussion = new ArrayList<>();

                        int fetchStatus = Utility.getMarkersDataFromJson(response, fetchedAccidents, fetchedDiscussion);

                        if (fetchStatus == 0) {
                            MarkersManager.getInstance().addAllAccidents(fetchedAccidents, MarkersManager.DO_NOT_RESET);
                            MarkersManager.getInstance().addAllDiscussions(fetchedDiscussion, MarkersManager.DO_NOT_RESET);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, error.toString());
                    }
                });

        // Add the request to the RequestQueue.
        mRequestQueue.add(jsObjRequest);
    }

    public void createNewDisqus(final double lat, final double lng, final Context context) {

        if (context == null)
            return;

        // Show ProgressDialog telling user new discussion is created
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.creating_new_discussion));
        dialog.setCancelable(false);
        dialog.show();

        String identifier = "(" + lat + ", " + lng + ")";
        JSONObject jsonObjectData = new JSONObject();
        try {
            jsonObjectData.put("latitude", Double.toString((lat)));
            jsonObjectData.put("longitude", Double.toString(lng));
            jsonObjectData.put("title", identifier);
            jsonObjectData.put("identifier", identifier);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error creating json for new discussion request");
            dialog.dismiss();
            return;
        }

        PriorityJsonObjectRequest newDisqusRequest = new PriorityJsonObjectRequest
                (Request.Method.POST, ANYWAY_DISCUSSION_POST_URL, jsonObjectData, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        dialog.dismiss();

                        Discussion discussion = Utility.parseJsonToDiscussion(response);
                        if (discussion != null) {
                            MarkersManager.getInstance().addDiscussion(discussion);
                            Intent disqusIntent = new Intent(context, DisqusActivity.class);
                            disqusIntent.putExtra(DisqusActivity.DISQUS_TALK_IDENTIFIER, discussion.getIdentifier());
                            context.startActivity(disqusIntent);
                        } else
                            Toast.makeText(context, R.string.error_creating_discussion, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(context, R.string.error_creating_discussion, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        newDisqusRequest.setShouldCache(false);
        newDisqusRequest.setPriority(Request.Priority.HIGH);
        mRequestQueue.add(newDisqusRequest);
    }

    public void sendUserAndSearchedLocation(double lat, double lng, int type) {

        JSONObject jsonObjectData = new JSONObject();
        try {
            jsonObjectData.put("latitude", Double.toString((lat)));
            jsonObjectData.put("longitude", Double.toString(lng));
            jsonObjectData.put("type", Integer.toString(type));
        } catch (JSONException e) {
            //Log.e(LOG_TAG, "Error parsing statistics");
            return;
        }

        PriorityJsonObjectRequest newLocationRequest = new PriorityJsonObjectRequest
                (Request.Method.POST, ANYWAY_HIGHLIGHT_POINTS, jsonObjectData, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.i(LOG_TAG, "Statistics sent");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(LOG_TAG, "Error sending statistics");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        newLocationRequest.setShouldCache(false);
        newLocationRequest.setPriority(Request.Priority.LOW);
        mRequestQueue.add(newLocationRequest);

    }
}