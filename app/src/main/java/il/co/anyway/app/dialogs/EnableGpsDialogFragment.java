package il.co.anyway.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import il.co.anyway.app.R;

public class EnableGpsDialogFragment extends DialogFragment {

    public static final String DONT_SHOW_GPS_DIALOG_KEY = "il.co.anyway.app.DONT_SHOW_GPS_DIALOG";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.gps_is_off)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Start location settings activity
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                })
                .setNeutralButton(R.string.dont_as_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences.Editor sharedPrefsEditor =
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

                        sharedPrefsEditor.putBoolean(DONT_SHOW_GPS_DIALOG_KEY, true);
                        sharedPrefsEditor.apply();

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
