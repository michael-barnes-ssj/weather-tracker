package bit.mike.googlemaps;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by Mike on 20/05/2018.
 * Checks GPS permissions and asks user for permission if permission isn't set.
 * Gets current location.
 */

public class GPSLocation {
    private static final int locationPermissionRequestsCode = 42;
    private Location location;
    private MapsActivity activity;

    public GPSLocation(MapsActivity activity)
    {
        this.activity = activity;
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        if (checkLocationPermission())
        {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new GPSListener());
        }
        else
        {
            requestLocationPermission();
        }
    }

    /**
     * Listens for location, when found it calls method to load map
     */

    public class GPSListener implements OnSuccessListener<Location>
    {
        @Override
        public void onSuccess(Location loc)
        {
            if (loc != null)
            {
                location = loc;
                activity.loadMap();
            }
        }
    }

    /**
     * Checks for GPS permissions
     */

    public boolean checkLocationPermission() {
        int fineLocationOk = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        int coarseLocationOk = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if ((fineLocationOk != PackageManager.PERMISSION_GRANTED) || (coarseLocationOk != PackageManager.PERMISSION_GRANTED))
            return false;
        else {
            return true;
        }
    }


    public void requestLocationPermission() {
        String[] permissionsIWant = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissionsIWant, locationPermissionRequestsCode);
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        }
    }

    public Location getLocation() {
        return location;
    }
}