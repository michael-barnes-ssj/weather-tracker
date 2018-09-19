package bit.mike.googlemaps;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Creates fragment containing weather data
 */
public class DisplayWeatherFragment extends Fragment {

    /**
     * Inflates layout with components and fills them with forecast data
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_display_weather, container,false);
        //Get access to resources
        Resources r = getResources();
        //Get passed in forecast object
        Forecast forecast = (Forecast) getArguments().getSerializable(r.getString(R.string.forecast));

        //Get reference to components and pass forecast attributes to them
        TextView temp =  rootView.findViewById(R.id.temp);
        TextView windSpeed =  rootView.findViewById(R.id.windspeed);
        TextView  chanceOfRain =  rootView.findViewById(R.id.chance_of_rain);
        TextView humidity =  rootView.findViewById(R.id.humidity);
        TextView desc =  rootView.findViewById(R.id.description);
        ImageView image = rootView.findViewById(R.id.weatherImage);
        TextView dateTime = rootView.findViewById(R.id.dateTime);
        temp.setText( r.getString(R.string.celcius, forecast.getTemp()));
        windSpeed.setText(r.getString(R.string.km, forecast.getWindSpeed()));
        chanceOfRain.setText(r.getString(R.string.percent, forecast.getChanceOfRain()));
        humidity.setText(r.getString(R.string.percent, forecast.getHumidity()));
        desc.setText(forecast.getDesc());
        dateTime.setText(forecast.getDateTime());
        //Get image
        new DownloadImageTask(image).execute(forecast.getImage());
        return rootView;
    }
}
