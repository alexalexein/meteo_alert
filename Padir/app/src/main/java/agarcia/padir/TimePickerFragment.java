package agarcia.padir;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by agarcia on 09/01/2018.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TextView timeTextView = getActivity().findViewById(R.id.timeTextViewSubtitle);
        int hour;
        int minute;
        // If we are adding a new alarm, just show the current time
        if (timeTextView.getText().equals("00:00")){
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // If we are editting an existing one, show the last set time for that alarm
        else{
            String[] time_list = timeTextView.getText().toString().split(":");
            if (time_list[0].startsWith("0")){
                time_list[0] = time_list[0].substring(1);
            }
            if(time_list[1].startsWith("0")){
                time_list[1] = time_list[1].substring(1);
            }
            hour = Integer.valueOf(time_list[0]);
            minute = Integer.valueOf(time_list[1]);
        }

        return new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                this, hour, minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        TextView timeTextView = getActivity().findViewById(R.id.timeTextViewSubtitle);
        if(hourOfDay < 10){
            if(minute < 10){
                timeTextView.setText("0" + String.valueOf(hourOfDay) + ":" +  "0" + String.valueOf(minute));
            }
            else {
                timeTextView.setText("0" + String.valueOf(hourOfDay) + ":" +  String.valueOf(minute));
            }
        }
        else {
            if (minute < 10){
                timeTextView.setText(String.valueOf(hourOfDay) + ":" +  "0" + String.valueOf(minute));
            }
            else {
                timeTextView.setText(String.valueOf(hourOfDay) + ":" +  String.valueOf(minute));
            }
        }
    }
}
