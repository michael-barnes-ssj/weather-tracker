package bit.mike.googlemaps;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by Mike on 29/05/2018.
 * Creates Date and Number pickers, and a seek bar.
 * Uses these values to create settings.
 */

public class SettingsFragment extends DialogFragment
{
    /*
    *   Any class creating SettingsFragment should include this interface
    */
    public interface GetData
    {
        public void getData(int seekNumber, Calendar c);
    }

    private static final int FIRST_CHILD = 0;
    private static final int MIN = 0;
    private static final int MINUTES = 0;
    private static final int SECONDS = 0;
    private static final int MAX = 23;
    private static final int OFFSET = 2;
    private NumberPicker numberPicker;
    private DecimalFormat decimalFormat;
    private TextView seekNumber;
    private DatePicker datePicker;
    private SeekBar seekBar;
    private int startSpeed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.set_time_fragment, container, false);
        setCancelable(false);
        startSpeed = getArguments().getInt(getResources().getString(R.string.travel_speed_key));
        Calendar calendar = (Calendar) getArguments().getSerializable(getResources().getString(R.string.calendar));

        seekNumber = v.findViewById(R.id.seekNumber);
        seekBar = v.findViewById(R.id.seekBar);
        datePicker = v.findViewById(R.id.datePicker);
        datePicker.updateDate(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));

        setUpNumberPicker(v, calendar);
        setUpSeekBar();
        return v;
    }

    public void setUpNumberPicker(View v, Calendar calendar)
    {
        decimalFormat = new DecimalFormat(getResources().getString(R.string.number_format));
        NumberPicker.Formatter formatter = new NumberPicker.Formatter()
        {
            @Override
            public String format(int i)
            { return decimalFormat.format(i)+getResources().getString(R.string.minutes);}
        };
        numberPicker = v.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(MAX);
        numberPicker.setMinValue(MIN);
        numberPicker.setFormatter(formatter);
        numberPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));
        Button b = v.findViewById(R.id.confirmTimeButton);
        b.setOnClickListener(new SetTimeHandler());

        // Fixing number picker bug where first value doesn't show up
        View firstItem = numberPicker.getChildAt(FIRST_CHILD);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }
    }

    public void setUpSeekBar()
    {
        // Have to use offset on seek bar, first value can't be set
        seekBar.setProgress(startSpeed-OFFSET);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                String seekBarValue = String.valueOf(i+OFFSET);
                seekNumber.setText(getResources().getString(R.string.km, seekBarValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekNumber.setText(getResources().getString(R.string.km_int, (seekBar.getProgress()+OFFSET)));
    }



    public class SetTimeHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            MapsActivity ma = (MapsActivity) getActivity();
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year =  datePicker.getYear();
            Calendar calender = Calendar.getInstance();
            calender.set(year, month, day, numberPicker.getValue(), MINUTES, SECONDS);
            ma.getData(seekBar.getProgress()+OFFSET, calender);
        }
    }


}