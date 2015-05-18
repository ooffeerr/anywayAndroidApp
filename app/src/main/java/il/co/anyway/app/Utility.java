package il.co.anyway.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
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
import java.util.Date;
import java.util.List;

import il.co.anyway.app.models.Accident;
import il.co.anyway.app.models.Discussion;

public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static int getClusterImageByCountOfAccidents(int count) {

        final int[] res = {R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4};

        final int[] forCounts = {10, 100, 1000, Integer.MAX_VALUE};

        int markerIcon;
        int i = 0;
        do {
            markerIcon = res[i];
        } while (count >= forCounts[i++]);

        return markerIcon;
    }

    public static Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {

        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        // set default bitmap config if none
        if (bitmapConfig == null)
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;

        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize((int) (13 * scale)); // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    /**
     * Parse JSON string to accidents list
     *
     * @param accidentJson       JSON object received from Anyway
     * @param fetchedAccidents   empty List<Accident> that all accidents will be added to
     * @param fetchedDiscussions empty List<Discussion> that all discussion will be added to
     * @return 0 for ok status, -1 for error in list, -2 for error in JSON
     */
    public static int getAccidentDataFromJson(JSONObject accidentJson,
                                              List<Accident> fetchedAccidents,
                                              List<Discussion> fetchedDiscussions) {

        if (fetchedAccidents == null || fetchedDiscussions == null)
            return -1;

        // These are the names of the JSON objects that need to be extracted.
        final String ACCIDENT_LIST = "markers";

        final String ACCIDENT_ADDRESS = "address";
        final String ACCIDENT_CREATED = "created";
        final String ACCIDENT_DESC = "description";
        final String ACCIDENT_ID = "id";
        final String ACCIDENT_LATITUDE = "latitude";
        final String ACCIDENT_LONGITUDE = "longitude";
        final String ACCIDENT_LOCATION_ACCURACY = "locationAccuracy";
        final String ACCIDENT_SEVERITY = "severity";
        final String ACCIDENT_TYPE = "type";
        final String ACCIDENT_SUBTYPE = "subtype";
        final String ACCIDENT_TITLE = "title";

        JSONArray accidentsArray;
        try {
            accidentsArray = accidentJson.getJSONArray(ACCIDENT_LIST);
        } catch (JSONException e) {
            return -2;
        }

        for (int i = 0; i < accidentsArray.length(); i++) {

            try {
                // Get the JSON object representing accident/discussion
                JSONObject accidentDetails = accidentsArray.getJSONObject(i);

                // check if array object is Accident or Discussion
                int type;
                try {
                    type = accidentDetails.getInt(ACCIDENT_TYPE);
                } catch (JSONException e) {
                    // TODO due to a temporary error Accident type is 'null' instead of 1
                    type = 1;
                }

                // Date comes as 2013-12-30T21:00:00, needs to be converted
                // 'created' also exist for Accident and Discussion
                String created = accidentDetails.getString(ACCIDENT_CREATED);
                Date createdDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                try {
                    createdDate = sdf.parse(created);
                } catch (ParseException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                // Accident
                if (type == 1) {

                    // get accident details from json
                    String address = accidentDetails.getString(ACCIDENT_ADDRESS);
                    String desc = accidentDetails.getString(ACCIDENT_DESC);
                    String title = accidentDetails.getString(ACCIDENT_TITLE);
                    Long id = accidentDetails.getLong(ACCIDENT_ID);
                    Double lat = accidentDetails.getDouble(ACCIDENT_LATITUDE);
                    Double lng = accidentDetails.getDouble(ACCIDENT_LONGITUDE);
                    Integer accuracy = accidentDetails.getInt(ACCIDENT_LOCATION_ACCURACY);
                    Integer severity = accidentDetails.getInt(ACCIDENT_SEVERITY);
                    Integer subtype = accidentDetails.getInt(ACCIDENT_SUBTYPE);

                    // create new Accident object and set parameters
                    Accident acc = new Accident()
                            .setId(id)
                            .setTitle(title)
                            .setDescription(desc)
                            .setType(type)
                            .setSubType(subtype)
                            .setSeverity(severity)
                            .setCreatedDate(createdDate)
                            .setLocation(new LatLng(lat, lng))
                            .setAddress(address)
                            .setLocationAccuracy(accuracy);

                    fetchedAccidents.add(acc);
                }
                // Discussion
                else if (type == 2) {

                    // get discussion details from json
                    Double lat = accidentDetails.getDouble(ACCIDENT_LATITUDE);
                    Double lng = accidentDetails.getDouble(ACCIDENT_LONGITUDE);
                    Long id = accidentDetails.getLong(ACCIDENT_ID);
                    String title = accidentDetails.getString(ACCIDENT_TITLE);

                    // create new Discussion object and set parameters
                    Discussion discussion = new Discussion()
                            .setId(id)
                            .setTitle(title)
                            .setLocation(new LatLng(lat, lng))
                            .setCreated(createdDate)
                            .setType(type);

                    fetchedDiscussions.add(discussion);

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getLocalizedMessage());
            }

        }

        return 0;
    }

    /**
     * set all the variables needed for pulling accident data from Anyway API
     *
     * @param bounds    map bounds
     * @param zoomLevel map zoom level
     * @param context   activity context
     */
    public static void getMarkersByParameters(LatLngBounds bounds, int zoomLevel, Context context) {

        if (bounds == null || context == null)
            return;

        AnywayRequestQueue requestQueue = AnywayRequestQueue.getInstance(context);

        // Get preferences form SharedPreferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean show_fatal = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_fatal_key), true);
        Boolean show_severe = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_severe_key), true);
        Boolean show_light = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_light_key), true);
        Boolean show_inaccurate = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_inaccurate_key), false);
        String fromDate = sharedPrefs.getString(context.getString(R.string.pref_from_date_key), context.getString(R.string.pref_default_from_date));
        String toDate = sharedPrefs.getString(context.getString(R.string.pref_to_date_key), context.getString(R.string.pref_default_to_date));

        requestQueue.addRequest(
                bounds.northeast.latitude,
                bounds.northeast.longitude,
                bounds.southwest.latitude,
                bounds.southwest.longitude,
                zoomLevel,
                getTimeStamp(fromDate),
                getTimeStamp(toDate),
                show_fatal,
                show_severe,
                show_light,
                show_inaccurate
        );
    }

    /**
     * Covert date object to timestamp
     *
     * @param date java.util.Date object
     * @return TimeStamp, formatted to Anyway API requirements or empty String if date is null
     */
    private static String getTimeStamp(Date date) {
        if (date == null)
            return "";

        Long ts = new Timestamp(date.getTime()).getTime() / 1000;
        return Long.toString(ts);
    }

    /**
     * Covert date(saved as string) to timestamp
     *
     * @param dateStr Date as String, in dd/MM/yyyy format
     * @return TimeStamp, formatted to Anyway API requirements or empty String if date is in the wrong format
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
                    icon = R.drawable.vehicle_vehicle_lethal;
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

    /**
     * Build URL to anyway map from current view for sharing
     *
     * @return the URL as String
     */
    public static String getCurrentPositionStringURI(LatLng location, int zoomLevel, Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean show_fatal = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_fatal_key), true);
        Boolean show_severe = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_severe_key), true);
        Boolean show_light = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_light_key), true);
        Boolean show_inaccurate = sharedPrefs.getBoolean(context.getString(R.string.pref_accidents_inaccurate_key), false);
        String fromDate = sharedPrefs.getString(context.getString(R.string.pref_from_date_key), context.getString(R.string.pref_default_from_date));
        String toDate = sharedPrefs.getString(context.getString(R.string.pref_to_date_key), context.getString(R.string.pref_default_to_date));

        // re format the dates to yyyy-MM-dd for the url sharing
        fromDate = DatePreference.getYear(fromDate) + "-" + DatePreference.getMonth(fromDate) + "-" + DatePreference.getDate(fromDate);
        toDate = DatePreference.getYear(toDate) + "-" + DatePreference.getMonth(toDate) + "-" + DatePreference.getDate(toDate);

        Uri builtUri = Uri.parse(AnywayRequestQueue.ANYWAY_BASE_URL).buildUpon()
                .appendQueryParameter("start_date", fromDate)
                .appendQueryParameter("end_date", toDate)
                .appendQueryParameter("show_fatal", show_fatal ? "1" : "0")
                .appendQueryParameter("show_severe", show_severe ? "1" : "0")
                .appendQueryParameter("show_light", show_light ? "1" : "0")
                .appendQueryParameter("show_inaccurate", show_inaccurate ? "1" : "0")
                .appendQueryParameter("zoom", Integer.toString(zoomLevel))
                .appendQueryParameter("lat", Double.toString(location.latitude))
                .appendQueryParameter("lon", Double.toString(location.longitude))
                .build();


        return builtUri.toString();
    }
}

