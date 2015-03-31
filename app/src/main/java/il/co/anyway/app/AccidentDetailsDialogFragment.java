package il.co.anyway.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AccidentDetailsDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get saved accident data
        Bundle mArgs = getArguments();
        Long id = mArgs.getLong("id");
        String description = mArgs.getString("description");
        String titleBySubType = mArgs.getString("titleBySubType");
        String address = mArgs.getString("address");
        String created = mArgs.getString("created");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.accident_details, null);
        builder.setView(v);

        // Add action buttons
        builder.setNeutralButton(getString(R.string.address_not_found_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // update dialog TextViews with the accident details
        TextView tv;
        tv = (TextView) v.findViewById(R.id.accident_details_desc);
        if (tv != null)
            tv.setText(created + "\n" + address + "\n" + description);

        tv = (TextView) v.findViewById(R.id.accident_details_title);
        if (tv != null)
            tv.setText(titleBySubType);

        return builder.create();
    }


}
