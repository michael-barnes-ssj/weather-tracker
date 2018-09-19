package bit.mike.googlemaps;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;

/**
 * Created by Mike on 13/06/2018.
 * Create alert dialog informing user about connection issues. Ends activity.
 * This handles app from crashing if they have no internet connection
 */

public class ExitAlert {
    public static void alert(MapsActivity mapsActivity)
    {

        final MapsActivity activity = mapsActivity;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        Resources r = activity.getResources();
        // set title
        alertDialogBuilder.setTitle(r.getString(R.string.no_internet));

        // set dialog message
        alertDialogBuilder
                .setMessage(r.getString(R.string.no_internet_instructions))
                .setCancelable(false)
                .setPositiveButton(r.getString(R.string.exit),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close activity
                        activity.finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
