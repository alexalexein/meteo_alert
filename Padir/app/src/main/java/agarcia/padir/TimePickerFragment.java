package agarcia.padir;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, this, hour, minute, true);
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
