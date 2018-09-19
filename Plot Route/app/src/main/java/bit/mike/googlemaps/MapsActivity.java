package bit.mike.googlemaps;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, WeatherRequest.AsyncResponse, SettingsFragment.GetData {

    private static final int START_SPEED = 5;
    private static final int X_OFFSET = 0;
    private static final int Y_OFFSET = 250;
    private static final int CURRENT_WEATHER = 2;
    private static final int SNAP_DISTANCE = 1000;
    private static final float ZOOM_AMOUNT = 8.0f;
    private static final float MARKER_LIMIT = 10;

    private GoogleMap map;
    private LatLng currentLocation;
    private ArrayList<Marker> markers;
    private ArrayList<Polyline> polylines;
    private TextView totalDistanceText;
    private TextView startTimeText;
    private TextView arrivalTimeText;
    private TextView travelSpeedText;
    private Button cleanScreenButton;
    private Marker clickedMarker;
    private SettingsFragment settingsFragment;
    private ConstraintLayout infoText;
    private Calendar startDate;
    private DateFormat dateFormatter;
    private ViewPager viewPager;
    private int travelSpeed;
    private GPSLocation gps;
    private Boolean markPlaced;
    private ProgressBar progressBar;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkForConnection();
    }

    /**
     * Load map and move screen to current location.
     * Set up listeners from map interaction.
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        MarkerHandler m = new MarkerHandler();
        map.setOnMarkerDragListener(m);
        map.setOnMarkerClickListener(m);
        map.setOnMapClickListener(new MapClickListener());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, ZOOM_AMOUNT));
    }

    /**
     * Will be called by Weather request when getting data from weather api is completed.
     * Create fragment to display weather data, or display error message
     * @param forecasts - Holds weather data
     * @param error - Error message, can be null if weather is data is present
     */
    @Override
    public void processFinish(ArrayList<Forecast> forecasts, String error)
    {
        progressBar.setVisibility(View.INVISIBLE);
        infoText.setVisibility(View.VISIBLE);

        if (forecasts != null)
        {
            CreateFragment(forecasts);
        }
        else
        {
            Toast toast = Toast.makeText(this, error, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, X_OFFSET, Y_OFFSET);
            toast.show();
        }
    }

    /**
     * Called by SettingsFragment.
     * Gets data from start time fragment as sets date, time and speed for calculations
     * @param travelSpeed
     * @param startDate
     */
    @Override
    public void getData(int travelSpeed, Calendar startDate) {

        this.startDate = startDate;
        this.travelSpeed = travelSpeed;
        startTimeText.setText(dateFormatter.format(startDate.getTime()));
        travelSpeedText.setText(resources.getString(R.string.km_int, travelSpeed));
        updateTotalDistance();
        settingsFragment.dismiss();
    }

    /**
     * Lets the user remove markers and polylines when back button it clicked.*
     */
    @Override
    public void onBackPressed()
    {
        CleanScreen();

        if (markers.size() > 0)
        {
            markers.get(markers.size()-1).remove();
            markers.remove(markers.size()-1);

            if (polylines.size() > 0)
            {
                map.animateCamera(CameraUpdateFactory.newLatLng(markers.get(markers.size()-1).getPosition()));
                polylines.get(polylines.size()-1).remove();
                polylines.remove(polylines.size()-1);
            }
        }
        else
        {
            finish();
        }

        updateTotalDistance();
    }

    /**
     * Checks user has internet connection when activity is created.*
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * If user has no connectivity, show them message and close application.
     */
    public void checkForConnection()
    {
        if (isOnline())
        {
            setUp();
        }
        else
        {
            ExitAlert.alert(this);
        }
    }

    /**
     * Starts the settings fragment and intialises components and class variables
     */
    public void setUp()
    {
        resources = getResources();
        startSettingsFragment(Calendar.getInstance(), START_SPEED);
        gps = new GPSLocation(MapsActivity.this);
        markers = new ArrayList<>();
        polylines = new ArrayList<>();
        infoText = findViewById(R.id.infoText);
        totalDistanceText = findViewById(R.id.distanceValue);
        cleanScreenButton = findViewById(R.id.cleanScreen);
        startTimeText = findViewById(R.id.startTimeValue);
        arrivalTimeText = findViewById(R.id.endTimeValue);
        travelSpeedText = findViewById(R.id.speedValue);
        cleanScreenButton.setOnClickListener(new CleanScreenButton());
        dateFormatter = new SimpleDateFormat(resources.getString(R.string.format_string));
        markPlaced = false;
        Button setTimeButton = findViewById(R.id.setTimeButton);
        setTimeButton.setOnClickListener(new SetSettingsFragmentHandler());
    }

    public void loadMap()
    {
        currentLocation = new LatLng(gps.getLocation().getLatitude(), gps.getLocation().getLongitude());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void CreateFragment(ArrayList<Forecast> forecasts)
    {
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new PageAdapter(
                getSupportFragmentManager(), forecasts, resources));
        viewPager.setCurrentItem(CURRENT_WEATHER);
    }

    /**
     * Moves map location to users current location
     * Snaps marker to current location if map press is within 1km of location. Only happens first time.
     * @param latLng
     */
    public void addFirstMarker(LatLng latLng)
    {
        double distance = SphericalUtil.computeDistanceBetween(latLng, currentLocation);
        LatLng startPosition = latLng;

        if ((distance < SNAP_DISTANCE) && (!markPlaced))
        {
            startPosition = currentLocation;
        }

        map.animateCamera(CameraUpdateFactory.newLatLng(startPosition));
        Marker marker = map.addMarker(new MarkerOptions()
                .position(startPosition)
                .draggable(true));
        markers.add(marker);
        markPlaced = true;
    }

    public void addMarkerAndPolyline(LatLng latlng)
    {
        CleanScreen();
        // Limit the amount of markers
        if (markers.size() < MARKER_LIMIT)
        {
            map.animateCamera(CameraUpdateFactory.newLatLng(latlng));
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latlng)
                    .draggable(true));
            markers.add(marker);

            Polyline polyline = map.addPolyline(new PolylineOptions().add(
                    markers.get(markers.size() - 2).getPosition(),
                    markers.get(markers.size() - 1).getPosition()));
            polylines.add(polyline);

        }
        // Reached limit, inform user
        else
        {
            Toast toast = Toast.makeText(this, resources.getString(R.string.marker_limit_reached), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, X_OFFSET, Y_OFFSET);
            toast.show();
        }

        updateTotalDistance();
    }

    public void updateTotalDistance()
    {
        int totalDistance = Calculations.getDistance(markers.size()-1, markers);
        totalDistanceText.setText(resources.getString(R.string.kms_int, totalDistance));
        arrivalTimeText.setText(dateFormatter.format(Calculations.getArrivalTime(totalDistance, travelSpeed, startDate)));
    }

    public void CleanScreen()
    {
        if (clickedMarker != null)
        {
            try
            {
                clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            catch (IllegalArgumentException e)
            {
                // Handle user spamming buttons and throwing error
                Log.d(resources.getString(R.string.icon_error) , e.getMessage());
            }
        }

        if (viewPager != null)
        {
            viewPager.removeAllViews();
        }
    }

    public void updatePolylines(Marker marker)
    {
        // Find index of dragged marker
        Integer markerIndex = markers.indexOf(marker);

        // If there is more than one marker
        if (markers.size() > 1)
        {
            // If the marker isn't the last marker
            if (markerIndex < markers.size() - 1) {

                // Set polyline from current marker to next marker
                polylines.get(markerIndex).setPoints(getPoints(markerIndex, markerIndex+1));

                // If not the first marker - Set polyline from previous marker to current
                if (markerIndex > 0)
                {
                    polylines.get(markerIndex - 1).setPoints(getPoints(markerIndex-1, markerIndex));
                }
            }
            // If the last marker 0 - Set polyline from previous marker to current
            else
            {
                polylines.get(markerIndex - 1).setPoints(getPoints(markerIndex-1, markerIndex));
            }
        }
    }

    public ArrayList<LatLng> getPoints(int marker1, int marker2)
    {
        ArrayList<LatLng> points = new ArrayList<>();
        points.add(markers.get(marker1).getPosition());
        points.add(markers.get(marker2).getPosition());

        return points;
    }

    public void startSettingsFragment(Calendar c, int travelSpeed)
    {
        if (viewPager != null)
        {
            CleanScreen();
        }

        settingsFragment = new SettingsFragment();
        Bundle b = new Bundle();
        b.putSerializable(resources.getString(R.string.calendar), c);
        b.putInt(resources.getString(R.string.travel_speed_key), travelSpeed);
        settingsFragment.setArguments(b);
        FragmentManager fm = getSupportFragmentManager();
        settingsFragment.show(fm, resources.getString(R.string.start_time_tag));
    }

    public class SetSettingsFragmentHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startSettingsFragment(startDate, travelSpeed);
        }
    }

    public class MapClickListener implements GoogleMap.OnMapClickListener
    {
        @Override
        public void onMapClick(LatLng latLng)
        {
            if (markers.size() == 0)
            {
                TextView start_message = findViewById(R.id.startLocation);
                start_message.setVisibility(View.INVISIBLE);
                addFirstMarker(latLng);
            }

            else
            {
                addMarkerAndPolyline(latLng);
            }
        }
    }

    public class MarkerHandler implements GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener
    {
        @Override
        public void onMarkerDragStart(Marker marker) {
            updatePolylines(marker);
            CleanScreen();
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            updatePolylines(marker);
        }

        @Override
        public void onMarkerDragEnd(Marker marker)
        {
            updateTotalDistance();
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }

        /**
         * When marker is clicked, make a request to weather api based on markers location.
         * Animate map and update screen to make it clear which marker weather is for.
         * @param marker
         * @return
         */
        @Override
        public boolean onMarkerClick(Marker marker)
        {
            CleanScreen();
            int markerIndex = markers.indexOf(marker);
            int distance = Calculations.getDistance(markerIndex, markers);

            clickedMarker = marker;
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            infoText.setVisibility(View.INVISIBLE);
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            new WeatherRequest(MapsActivity.this,
                    Calculations.getArrivalTime(distance, travelSpeed, startDate),
                    dateFormatter,
                    resources).execute(
                    String.valueOf(marker.getPosition().latitude),
                    String.valueOf(marker.getPosition().longitude));

            cleanScreenButton.setVisibility(View.VISIBLE);
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

            return true;
        }
    }

    public class CleanScreenButton implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            CleanScreen();
        }
    }
}



