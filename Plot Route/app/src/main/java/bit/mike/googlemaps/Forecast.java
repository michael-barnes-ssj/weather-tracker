package bit.mike.googlemaps;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Mike on 24/05/2018.
 * Takes json data and turns it into a class.
 * Once data is in a class there will be less exception handling
 */

public class Forecast implements Serializable {
    private static final int WEATHER_INDEX = 0;

    private String temp;
    private String windSpeed;
    private String humidity;
    private String chanceOfRain;
    private String desc;
    private String imageUrl;
    private String dateTime;


    public Forecast(JSONObject weather, String dateTime, Resources resources) {
        this.dateTime = dateTime;
        GetWeatherData(weather, resources);
    }

    /**
     * Takes json object and extracts data into class variables
     * @param weather
     */

    private void GetWeatherData(JSONObject weather, Resources resources)
    {
        try
        {
            temp = weather.getString(resources.getString(R.string.tempC));
            windSpeed = weather.getString(resources.getString(R.string.windspeedKmph));
            humidity = weather.getString(resources.getString(R.string.get_humidity));
            chanceOfRain = weather.getString(resources.getString(R.string.chanceofrain));
            desc = weather.getJSONArray(resources.getString(R.string.weatherDesc))
                    .getJSONObject(WEATHER_INDEX).getString( resources.getString(R.string.value));
            imageUrl = weather.getJSONArray( resources.getString(R.string.weatherIconUrl))
                    .getJSONObject(WEATHER_INDEX).getString(resources.getString(R.string.value));
        }
        catch (JSONException e)
        {
            Log.d(resources.getString(R.string.json_error), e.getMessage());
        }
}

    public String getTemp() {
        return temp;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getChanceOfRain() {
        return chanceOfRain;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return imageUrl;
    }

    public String getDateTime() {
        return dateTime;
    }
}
