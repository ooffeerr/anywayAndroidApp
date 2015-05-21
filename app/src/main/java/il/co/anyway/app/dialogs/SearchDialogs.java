package il.co.anyway.app.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import il.co.anyway.app.MainActivity;
import il.co.anyway.app.R;

public class SearchDialogs {

    private static final String LOG_TAG = SearchDialogs.class.getSimpleName();
    private static final Locale APP_DEFAULT_LOCALE = new Locale("he_IL");

    // action handler for address search
    public static void showSearchDialog(final Activity activity) {

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
                                searchAddress(searchTextView.getText().toString(), activity);
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
     * Search for an address, show a dialog and move the map to the searched location
     *
     * @param addressToSearch The address to search, in free speech
     */
    private static void searchAddress(String addressToSearch, final Activity activity) {

        Geocoder geoCoder = new Geocoder(activity, APP_DEFAULT_LOCALE);
        final int MAX_RESULTS = 7;

        try {

            // Search for the address
            final List<Address> addresses = geoCoder.getFromLocationName(addressToSearch, MAX_RESULTS);

            if (addresses.size() > 0) {

                // re-arrange all the address in String array for the AlertDialog
                final String[] addressList = new String[addresses.size()];
                for (int i = 0; i < addresses.size(); i++) {

                    // Address received as an address lines, join them all to one line
                    String tempAddress = "";
                    for (int j = 0; j <= addresses.get(i).getMaxAddressLineIndex(); j++)
                        tempAddress += addresses.get(i).getAddressLine(j) + ", ";

                    // remove the last ", " from the address
                    tempAddress = tempAddress.substring(0, tempAddress.length() - 2);
                    // add it to the array, the index match to the address checked
                    addressList[i] = tempAddress;

                }

                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle(activity.getString(R.string.address_result_title))
                        .setItems(addressList, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {

                                        LatLng resultLocation = new LatLng(addresses.get(which).getLatitude(), addresses.get(which).getLongitude());
                                        ((MainActivity) activity).updateMapFromSearchResult(resultLocation, addressList[which]);

                                    }
                                }
                        );
                adb.show();

            } else {

                // address not found, prompt user
                AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                adb.setTitle(activity.getString(R.string.address_not_found_title));
                adb.setMessage(activity.getString(R.string.address_not_found_details));
                adb.setPositiveButton(activity.getString(R.string.close), null);
                adb.show();

            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }
    }
}
