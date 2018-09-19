package bit.mike.googlemaps;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Mike on 6/06/2018.
 * Inflates Display Weather fragment into page view for each forecast in list.*
 */

public class PageAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Forecast> forecasts;
    private Resources resources;

    public PageAdapter(FragmentManager fm, ArrayList<Forecast> forecasts, Resources resources) {
        super(fm);
        this.forecasts = forecasts;
        this.resources = resources;
    }

    @Override
    public Fragment getItem(int position) {
        /** Show a Fragment based on the position of the current screen */

        DisplayWeatherFragment bf = new DisplayWeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(resources.getString(R.string.forecast), forecasts.get(position));
        bf.setArguments(bundle);
        return bf;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return forecasts.size();
    }
}