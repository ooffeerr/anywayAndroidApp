package il.co.anyway.app.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import il.co.anyway.app.MainActivity;
import il.co.anyway.app.R;
import il.co.anyway.app.singletons.AnywayRequestQueue;

public class SearchAddress {

    private static final String LOG_TAG = SearchAddress.class.getSimpleName();
    private static final Locale APP_DEFAULT_LOCALE = new Locale("he_IL");

    private Activity activity;

    public SearchAddress(Activity activity) {
        this.activity = activity;
        showSearchDialog();
    }

    // action handler for address search
    private void showSearchDialog() {

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View addressDialogView = activity.getLayoutInflater().inflate(R.layout.address_search_dialog, null);

        final AlertDialog searchDialog = new AlertDialog.Builder(activity)
                .setView(addressDialogView)
                .setPositiveButton(R.string.search, null) //Set to null. We override the onclick
                .setNegativeButton(R.string.cancel, null)
                .create();

        searchDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = searchDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        TextView searchTextView = (TextView) addressDialogView.findViewById(R.id.address_search);

                        if (searchTextView != null) {
                            if (searchTextView.getText().toString().equals("")) {
                                Toast t = Toast.makeText(activity.getBaseContext(), activity.getString(R.string.address_empty), Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.CENTER, 0, 0);
                                t.show();
                            } else {
                                String addressToSearch = searchTextView.getText().toString();
                                SearchAddressAsync searchAddressAsync = new SearchAddressAsync(addressToSearch);
                                searchAddressAsync.execute();
                                searchDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        searchDialog.show();
    }

    /**
     * Show search result as dialog list and allow user to choose the right address
     * or show dialog with 'address not found' message
     *
     * @param resultList search result list
     */
    private void showSearchResult(final List<Address> resultList) {
        if (resultList == null)
            return;

        if (resultList.size() == 0) {
            // address not found, prompt user
            AlertDialog.Builder adb = new AlertDialog.Builder(activity);
            adb.setTitle(activity.getString(R.string.address_not_found_title));
            adb.setMessage(activity.getString(R.string.address_not_found_details));
            adb.setPositiveButton(activity.getString(R.string.close), null);
            adb.show();
            return;
        }

        // re-arrange all the address in String array for the AlertDialog
        final String[] addressList = new String[resultList.size()];
        for (int i = 0; i < resultList.size(); i++)
            addressList[i] = resultList.get(i).getAddressLine(0);

        // show dialog with all search result
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
        adb.setTitle(activity.getString(R.string.address_result_title))
                .setItems(addressList, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                LatLng resultLocation = new LatLng(resultList.get(which).getLatitude(), resultList.get(which).getLongitude());
                                ((MainActivity) activity).updateMapFromSearchResult(resultLocation, addressList[which]);

                                AnywayRequestQueue.getInstance(activity)
                                        .sendUserAndSearchedLocation(
                                                resultLocation.latitude,
                                                resultLocation.longitude,
                                                AnywayRequestQueue.HIGHLIGHT_TYPE_USER_SEARCH
                                        );

                            }
                        }
                );
        adb.show();

    }

    // use google geo-coder api too get location from search string
    // use AsyncTask to not overload the UI thread
    public class SearchAddressAsync extends AsyncTask<Void, Void, List<Address>> {

        private String mAddressToSearch;
        private ProgressDialog dialog;

        public SearchAddressAsync(String address) {
            mAddressToSearch = address;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show ProgressDialog telling user new discussion is created
            dialog = new ProgressDialog(activity);
            dialog.setMessage(activity.getString(R.string.address_searching));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected List<Address> doInBackground(Void... params) {

            final String GOOGLE_GEOCODER_APIKEY = "AIzaSyB3vqN-tZV2DTMw75B6okJLDptNOil1xhY";

            final String GOOGLE_GEOCODER_URL = "https://maps.google.com/maps/api/geocode/json" +
                    "?language=iw" +
                    "&components=country:IL" +
                    "&key=" + GOOGLE_GEOCODER_APIKEY;

            List<Address> results = new ArrayList<>();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Construct the URL for the Anyway cluster query
                Uri builtUri = Uri.parse(GOOGLE_GEOCODER_URL).buildUpon()
                        .appendQueryParameter("address", mAddressToSearch) //TODO
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to Google, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    stringBuilder.append(line);

                String JSONResp = stringBuilder.toString();

                JSONObject jsonObject = new JSONObject(JSONResp);

                // check status, continue only if found results
                String resultStatus = jsonObject.getString("status");
                if (resultStatus.equals("OK")) {

                    JSONArray arr = jsonObject.getJSONArray("results");
                    for (int i = 0; i < arr.length(); i++) {

                        JSONObject currentResult = arr.getJSONObject(i);
                        String strAddress = currentResult.getString("formatted_address");

                        // if no result is found for this address google geo-coder return one result
                        // which is israel country, this is the only case the formatted address length
                        // will be 5(the length of the word 'Israel' in hebrew
                        if (strAddress.length() == 5)
                            continue;

                        double lng = currentResult
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .getDouble("lng");

                        double lat = currentResult
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .getDouble("lat");

                        Address address = new Address(APP_DEFAULT_LOCALE);
                        address.setAddressLine(0, strAddress);
                        address.setLatitude(lat);
                        address.setLongitude(lng);

                        results.add(address);

                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error " + e.getMessage());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error " + e.getMessage());
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<Address> results) {
            super.onPostExecute(results);

            dialog.dismiss();
            showSearchResult(results);
        }
    }
}
