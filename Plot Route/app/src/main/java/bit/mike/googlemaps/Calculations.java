package bit.mike.googlemaps;

import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Mike on 13/06/2018.
 */

public class Calculations
{


    public static int getDistance(int markerIndex, ArrayList<Marker> markers)
    {
        final int KMS = 1000;
        final int NEAREST_HUNDRED = 100;
        double total = 0;

        for (int i = 0; i < markerIndex; i++)
        {
            total += SphericalUtil.computeDistanceBetween(markers.get(i).getPosition(), markers.get(i+1).getPosition());
        }

        return (int) Math.round(total/KMS * NEAREST_HUNDRED) / NEAREST_HUNDRED;
    }

     static Date getArrivalTime(int distance, int travelSpeed, Calendar startDate)
    {
        final int MINUTES = 6;
        //Get hours a minutes of distance
        int h = distance / travelSpeed;
        // Multiply by 6 to get minutes rather than decimal
        int m = distance % travelSpeed*MINUTES;
        // Create temp calendar to change time
        Calendar c = Calendar.getInstance();
        c.setTime(startDate.getTime());
        // Added hours and minutes to time
        c.set(Calendar.HOUR_OF_DAY, (c.get(Calendar.HOUR_OF_DAY)+h));
        c.set(Calendar.MINUTE, (c.get(Calendar.MINUTE)+m));
        // return formatted date time
        return c.getTime();
    }
}
