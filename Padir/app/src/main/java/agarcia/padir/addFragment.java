package agarcia.padir;

import android.content.Context;
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

/**
 * Created by agarcia on 06/01/2018.
 */

public class addFragment extends Fragment {

    static final String METHOD_KEY = "ADD OR EDIT";
    static final String ARGUMENTS_ID_KEY = "ARGUMENTS ID";
    static final String ARGUMENTS_LOCATION_KEY = "ARGUMENTS LOCATION";
    static final String ARGUMENTS_TIME_KEY = "ARGUMENTS TIME";
    static final String ARGUMENTS_FORECAST_WINDOW_KEY = "ARGUMENTS FORECAST WINDOW";

    String method = "";
    String id = "";
    String location = "";
    String time = "";
    String forecast_window = "";

    TextView windowTextView;
    TextView timeTextView;
    AutoCompleteTextView locationAutoCompleteTextView;

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
        method = args.getString(METHOD_KEY);
        if(method.equals("add")){
        }
        else if (method.equals("edit")){
            id = args.getString(ARGUMENTS_ID_KEY);
            location = args.getString(ARGUMENTS_LOCATION_KEY);
            locationAutoCompleteTextView.setText(location);
            time = args.getString(ARGUMENTS_TIME_KEY);
            timeTextView.setText(time);
            forecast_window = args.getString(ARGUMENTS_FORECAST_WINDOW_KEY);
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
        else {
            Log.i("DEBUGGING", "Method Add or Edit not passed correctly to addFragment...");
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
        String[] arguments_main = new String[4];
        switch (item.getItemId()) {
            case R.id.cancel:
                mCallback.mainFragmentRequested("cancel", arguments_main);
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
                        arguments_main[0] = id;
                        arguments_main[1] = set_location;
                        arguments_main[2] = timeTextView.getText().toString();
                        String hourString = arguments_main[2].split(":")[0];
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
                        else{
                            if (windowTextView.getText().equals("Rest of the Day") ||
                                    windowTextView.getText().equals("Resto del Día") ||
                                    windowTextView.getText().equals("Resta del Dia")){
                                arguments_main[3] = "Rest of the Day";
                            }
                            else if(windowTextView.getText().equals("Next 24h") ||
                                    windowTextView.getText().equals("Próximas 24h") ||
                                    windowTextView.getText().equals("Pròximes 24h")){
                                arguments_main[3] = "Next 24h";
                            }
                            else if(windowTextView.getText().equals("Next Day") ||
                                    windowTextView.getText().equals("Día Siguiente") ||
                                    windowTextView.getText().equals("Dia Següent")){
                                arguments_main[3] = "Next Day";
                            }
                            else if(windowTextView.getText().equals("Next 6 Days") ||
                                    windowTextView.getText().equals("Próximos 6 Días") ||
                                    windowTextView.getText().equals("Pròxims 6 Dies")){
                                arguments_main[3] = "Next 6 Days";
                            }
                            else if(windowTextView.getText().equals("Next Weekend") ||
                                    windowTextView.getText().equals("Próximo Fin de Semana") ||
                                    windowTextView.getText().equals("Pròxim Cap de Setmana")){
                                arguments_main[3] = "Next Weekend";
                            }

                            mCallback.mainFragmentRequested(method, arguments_main);
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
}
