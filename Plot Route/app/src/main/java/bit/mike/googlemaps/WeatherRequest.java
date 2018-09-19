package bit.mike.googlemaps;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mike on 21/05/2018.
 */


// Inner class that extends async so we can tdo http requests in background
public class WeatherRequest extends AsyncTask<String, Void, String>
{
    /**
     * Any class creating an instance of this class must implement AsyncResponse
     */
    public interface AsyncResponse {
        void processFinish(ArrayList<Forecast> forecasts, String error);
    }

    private static final int MAX_NUMBER_OF_DAYS = 8;
    private static final int LAT_INDEX = 0;
    private static final int LONG_INDEX = 1;
    private static final int OFFSET = 2;
    private static final int HOUR_COUNT = 5;

    private AsyncResponse delegate;
    private Calendar arrivalDate;
    private DateFormat dateFormatter;
    private Resources resources;
    public WeatherRequest(AsyncResponse delegate, Date arrivalDate, DateFormat dateFormatter, Resources resources){
        this.delegate = delegate;
        this.arrivalDate = Calendar.getInstance();
        this.arrivalDate.setTime(arrivalDate);
        this.dateFormatter = dateFormatter;
        this.resources = resources;
    }

    //Needs to implement do in background, returns a string
    @Override
    protected String doInBackground(String... locationInfo)
    {
        String JSONString;
        String lat = locationInfo[LAT_INDEX];
        String lng = locationInfo[LONG_INDEX];
        String url = resources.getString(R.string.url, lat, lng);
        JSONString = HTTPRequest.getJSONString(url, resources);

        return JSONString;
    }

    /**
     * When async call is complete
     * Use json data to create forecasts based on arrival time
     * @param s
     */
    @Override
    protected void onPostExecute(String s)
    {
        int arrivalDaysFromNow = getArrivalDaysFromNow();
        ArrayList<Forecast> forecasts = null;
        String error = null;

        if (arrivalDaysFromNow < MAX_NUMBER_OF_DAYS)
        {
            try
            {
                JSONArray ja = new JSONObject(s).
                        getJSONObject(resources.getString(R.string.data)).
                        getJSONArray(resources.getString(R.string.weather)).
                        getJSONObject(arrivalDaysFromNow).
                        getJSONArray(resources.getString(R.string.hourly));

                Calendar[] calendars = changeCalendarHours();

                JSONObject[] weatherArray = {
                        ja.getJSONObject(calendars[0].get(Calendar.HOUR_OF_DAY)),
                        ja.getJSONObject(calendars[1].get(Calendar.HOUR_OF_DAY)),
                        ja.getJSONObject(calendars[2].get(Calendar.HOUR_OF_DAY)),
                        ja.getJSONObject(calendars[3].get(Calendar.HOUR_OF_DAY)),
                        ja.getJSONObject(calendars[4].get(Calendar.HOUR_OF_DAY)),
                };

                forecasts = new ArrayList<>();

                for (int i = 0; i < weatherArray.length; i++)
                {
                    forecasts.add(new Forecast(weatherArray[i], dateFormatter.format(calendars[i].getTime()), resources));
                }

            }
            catch (JSONException e)
            {
                Log.d(resources.getString(R.string.log_error), e.getMessage());
                error = resources.getString(R.string.weather_error);
            }
            catch (NullPointerException e)
            {
                Log.d(resources.getString(R.string.log_error), e.getMessage());
                error = resources.getString(R.string.network_error);
            }
        }
        else
        {
            error = resources.getString(R.string.trip_error);
        }

        delegate.processFinish(forecasts, error);

    }

    // Creates a calendar array holding different hours to get weather data
    private Calendar[] changeCalendarHours()
    {
        Calendar[] calendars = new Calendar[HOUR_COUNT];
        // Creates calendars from 2 before arrival time to 2 after
        for (int i = 0; i < calendars.length; i++)
        {
            calendars[i] = Calendar.getInstance();
            calendars[i].setTime(arrivalDate.getTime());
            calendars[i].set(Calendar.HOUR_OF_DAY, arrivalDate.get(Calendar.HOUR_OF_DAY)+(i-OFFSET));
        }
        return calendars;
    }

    private int getArrivalDaysFromNow()
    {
        // Create calendar for today
        Calendar currentDate = Calendar.getInstance();
        currentDate.get(Calendar.DAY_OF_MONTH);

        //Convert to millis
        long end = currentDate.getTimeInMillis();
        long start = arrivalDate.getTimeInMillis();

        // Get days between both calendars

        return (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }
    
}

