package agarcia.padir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 06/01/2018.
 */

public class addFragment extends Fragment {

    static final String ALARM_ID = "ID_ALARM_TO_EDIT";

    int id;
    String location = "";
    String time = "";
    String forecast_window = "";

    TextView windowTextView;
    TextView timeTextView;
    AutoCompleteTextView locationAutoCompleteTextView;

    weatherAlarm editedAlarm;

    private dbHelper alarmDBHelper;

    OnAddOrEditRequested mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnAddOrEditRequested) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmDBHelper = new dbHelper(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        final View v = inflater.inflate(R.layout.add_fragment, container, false);

        final LinearLayout forecastWindowLinearLayout = v.findViewById(R.id.windowLayout);
        windowTextView= v.findViewById(R.id.windowTextViewSubtitle);

        registerForContextMenu(forecastWindowLinearLayout);

        forecastWindowLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().openContextMenu(forecastWindowLinearLayout);
            }
        });

        final LinearLayout timeLinearLayout = v.findViewById(R.id.timeLayout);
        timeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newTimeFragment = new TimePickerFragment();
                newTimeFragment.show(getFragmentManager(), "TimePicker");
            }
        });

        timeTextView = v.findViewById(R.id.timeTextViewSubtitle);

        locationAutoCompleteTextView = v.findViewById(R.id.locationAutoCompleteTextView);
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.municipios, android.R.layout.simple_dropdown_item_1line);
        locationAutoCompleteTextView.setAdapter(locationAdapter);

        Bundle args = getArguments();
        id = args.getInt(ALARM_ID);
        if (id != 0){
            Log.i("debug", "addFragment --> id!=0 --> edit");
            editedAlarm = alarmDBHelper.getSpecificAlarm(id);
            if (editedAlarm.getIsOn()==1){
                Log.i("debug","addFragment --> id!=0 --> isOn=1");
                activateAlarm(editedAlarm, false);
            }
            location = editedAlarm.getLocation();
            locationAutoCompleteTextView.setText(location);
            time = editedAlarm.getTimeOfDay();
            timeTextView.setText(time);
            forecast_window = editedAlarm.getForecastType();
            if (forecast_window.equals("Rest of the Day")){
                windowTextView.setText(R.string.restOfDayType);
            }
            else if (forecast_window.equals("Next 24h")){
                windowTextView.setText(R.string.next24HoursType);
            }
            else if (forecast_window.equals("Next Day")){
                windowTextView.setText(R.string.nextDayType);
            }
            else if (forecast_window.equals("Next 6 Days")){
                windowTextView.setText(R.string.next6DaysType);
            }
            else if (forecast_window.equals("Next Weekend")){
                windowTextView.setText(R.string.nextWeekendType);
            }
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
                mCallback.mainFragmentRequested();
                return true;
            case R.id.save_action:
                String set_location = locationAutoCompleteTextView.getText().toString();
                if (set_location.equals("") || set_location == null){
                    Toast.makeText(MainActivity.getInstance(),R.string.locationInvalidToast,
                            Toast.LENGTH_LONG).show();
                }
                else {
                    String[] municipios_list = MainActivity.getInstance().getResources()
                            .getStringArray(R.array.municipios);
                    if(Arrays.asList(municipios_list).contains(set_location)){
                        String hourString = timeTextView.getText().toString().split(":")[0];
                        int hourTimeInt;
                        if(Character.toString(hourString.charAt(0)).equals("0")){
                            hourTimeInt = Integer.valueOf(Character.toString(hourString.charAt(1)));
                        }
                        else{
                            hourTimeInt = Integer.valueOf(hourString);
                        }
                        if ((windowTextView.getText().equals("Rest of the Day") ||
                                windowTextView.getText().equals("Resto del Día") ||
                                windowTextView.getText().equals("Resta del Dia")) &&
                                hourTimeInt>22){
                            Toast.makeText(getContext(),
                                    R.string.toastTooLateRestOfDay,
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            String forecastTypeString = "";
                            if (windowTextView.getText().equals("Rest of the Day") ||
                                    windowTextView.getText().equals("Resto del Día") ||
                                    windowTextView.getText().equals("Resta del Dia")){
                                forecastTypeString = "Rest of the Day";
                            }
                            else if(windowTextView.getText().equals("Next 24h") ||
                                    windowTextView.getText().equals("Próximas 24h") ||
                                    windowTextView.getText().equals("Pròximes 24h")){
                                forecastTypeString = "Next 24h";
                            }
                            else if(windowTextView.getText().equals("Next Day") ||
                                    windowTextView.getText().equals("Día Siguiente") ||
                                    windowTextView.getText().equals("Dia Següent")){
                                forecastTypeString = "Next Day";
                            }
                            else if(windowTextView.getText().equals("Next 6 Days") ||
                                    windowTextView.getText().equals("Próximos 6 Días") ||
                                    windowTextView.getText().equals("Pròxims 6 Dies")){
                                forecastTypeString = "Next 6 Days";
                            }
                            else if(windowTextView.getText().equals("Next Weekend") ||
                                    windowTextView.getText().equals("Próximo Fin de Semana") ||
                                    windowTextView.getText().equals("Pròxim Cap de Setmana")){
                                forecastTypeString = "Next Weekend";
                            }
                            if (id!=0){
                                Log.i("debug","Edit saved");
                                editedAlarm.setLocation(set_location);
                                editedAlarm.setTimeOfDay(timeTextView.getText().toString());
                                editedAlarm.setForecastType(forecastTypeString);
                                if(editedAlarm.getIsOn() == 1){
                                    activateAlarm(editedAlarm, true);
                                }
                                alarmDBHelper.editAlarm(editedAlarm);
                            }
                            else{
                                Log.i("debug","New alarm stored");
                                weatherAlarm newAlarm = new weatherAlarm();
                                newAlarm.setDefaultID();
                                newAlarm.setLocation(set_location);
                                newAlarm.setTimeOfDay(timeTextView.getText().toString());
                                newAlarm.setForecastType(forecastTypeString);
                                newAlarm.setIsOn(0);
                                alarmDBHelper.addAlarm(newAlarm);
                            }
                            mCallback.mainFragmentRequested();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.getInstance(),
                                R.string.locationInvalidToast,
                                Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_windows, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //return super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.rest_of_day:
                windowTextView.setText(R.string.restOfDayType);
                break;
            case R.id.next_24_hours:
                windowTextView.setText(R.string.next24HoursType);
                break;
            case R.id.next_day:
                windowTextView.setText(R.string.nextDayType);
                break;
            case R.id.next_6_days:
                windowTextView.setText(R.string.next6DaysType);
                break;
            case R.id.next_weekend:
                windowTextView.setText(R.string.nextWeekendType);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    // Method to either activate or deactivate an alarm (depending on the activate argument)
    private void activateAlarm(weatherAlarm alarm, boolean activate) {
        Intent intent = new Intent(MainActivity.getInstance(), AlarmReceiver.class);
        intent.putExtra("ID", alarm.getID());
        intent.putExtra("location", alarm.getLocation());
        String alarm_time = alarm.getTimeOfDay();
        String[] time_list = alarm_time.split(":");
        if (time_list[0].startsWith("0")){
            time_list[0] = time_list[0].substring(1);
        }
        if(time_list[1].startsWith("0")){
            time_list[1] = time_list[1].substring(1);
        }
        intent.putExtra("time", time_list[0] + ":" + time_list[1]);
        intent.putExtra("forecastType", alarm.getForecastType());
        intent.setAction("dummy_unique_action_identifyer" + String.valueOf(alarm.getID()));
        final PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), alarm.getID(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) getActivity().
                getSystemService(getActivity().ALARM_SERVICE);
        if (activate) {
            long calendar_now_millis = Calendar.getInstance().getTimeInMillis();
            long calendar_alarm_millis;
            Calendar calendar_alarm = Calendar.getInstance();
            calendar_alarm.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_list[0]));
            // Is interesting to ensure that time is computed
            calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            calendar_alarm.set(Calendar.MINUTE, Integer.valueOf(time_list[1]));
            calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            if (calendar_now_millis>calendar_alarm_millis){
                calendar_alarm.add(Calendar.DAY_OF_MONTH, 1);
                calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            }
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        calendar_alarm_millis, alarmIntent);
            }
            else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar_alarm_millis, alarmIntent);
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar_alarm_millis, alarmIntent);
            }
        }
        else {
            alarmManager.cancel(alarmIntent);
        }
    }
}
