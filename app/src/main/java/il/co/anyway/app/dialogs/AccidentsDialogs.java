package il.co.anyway.app.dialogs;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.androidmapsextensions.Marker;

import java.util.List;

import il.co.anyway.app.R;
import il.co.anyway.app.Utility;
import il.co.anyway.app.models.Accident;

public class AccidentsDialogs {

    /**
     * Show list dialog of accidents inside marker cluster
     *
     * @param marker Marker cluster
     */
    public static void showAccidentsClusterAsListDialog(Marker marker, final AppCompatActivity activity) {
        if (!marker.isCluster())
            return;

        // re-arrange all the accident titles in String array for the AlertDialog
        final List<Marker> markersInCluster = marker.getMarkers();
        final String[] accidentsList = new String[markersInCluster.size()];
        for (int i = 0; i < markersInCluster.size(); i++) {

            Object markerData = markersInCluster.get(i).getData();
            if (markerData instanceof Accident) {

                Accident accident = (Accident) markerData;
                accidentsList[i] =
                        Utility.getAccidentTypeByIndex(accident.getSubType(), activity) +
                                " - " +
                                accident.getCreatedDateAsString();
            }
        }

        // show list dialog with all the accidents in the cluster
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
        adb.setTitle(accidentsList.length + " " + activity.getString(R.string.accidents));
        adb.setItems(accidentsList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (markersInCluster.get(which).getData() instanceof Accident) {
                    Accident a = markersInCluster.get(which).getData();
                    showAccidentDetailsInDialog(a, activity);
                }
            }
        });
        adb.show();
    }

    /**
     * Show accident details in dialog
     *
     * @param a Accident to show
     */
    public static void showAccidentDetailsInDialog(Accident a, AppCompatActivity activity) {
        Bundle args = new Bundle();
        args.putString("description", a.getDescription());
        args.putString("titleBySubType", Utility.getAccidentTypeByIndex(a.getSubType(), activity));
        args.putLong("id", a.getId());
        args.putString("address", a.getAddress());
        args.putString("created", a.getCreatedDateAsString());

        AccidentDetailsDialogFragment accidentDetailsDialog =
                new AccidentDetailsDialogFragment();
        accidentDetailsDialog.setArguments(args);
        accidentDetailsDialog.show(activity.getSupportFragmentManager(), "accidentDetails");
    }
}
