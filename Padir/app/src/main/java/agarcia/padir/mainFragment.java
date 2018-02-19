package agarcia.padir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 19/11/2017.
 */

public class mainFragment extends Fragment {

    final static String ARGUMENTS_METHOD_MAIN_KEY = "ARGUMENTS METHOD MAIN KEY";
    final static String ARGUMENTS_ID_MAIN_KEY = "ARGUMENTS ID MAIN KEY";
    final static String ARGUMENTS_LOCATION_MAIN_KEY = "ARGUMENTS LOCATION MAIN KEY";
    final static String ARGUMENTS_TIME_MAIN_KEY = "ARGUMENTS TIME MAIN KEY";
    final static String ARGUMENTS_WINDOW_MAIN_KEY = "ARGUMENTS WINDOW MAIN KEY";

    private FloatingActionButton addButton;
    private RecyclerView alarmRecyclerView;
    private alarmAdapter mAlarmAdapter;
    private dbHelper alarmDBHelper;
    private List<weatherAlarm> alarms;
    private Paint p = new Paint();
    private int deletedPosition = -1;
    private weatherAlarm deletedAlarm;
    private int editedPosition;
    private weatherAlarm editedAlarm;

    private Snackbar deleteSnackbar;

    String method = "";
    String id = "";
    String location = "";
    String time = "";
    String forecast_window = "";

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmDBHelper = new dbHelper(getActivity());
        setHasOptionsMenu(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        checkSnackBarOn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate Fragment View
        final View v = inflater.inflate(R.layout.alarm_list_fragment, container, false);

        // Check if an alarm has been edited or added
        Bundle args = getArguments();
        if (args != null) {
            int currentPosition = -1;
            alarms = alarmDBHelper.getAlarms();
            method = args.getString(ARGUMENTS_METHOD_MAIN_KEY);
            location = args.getString(ARGUMENTS_LOCATION_MAIN_KEY);
            time = args.getString(ARGUMENTS_TIME_MAIN_KEY);
            forecast_window = args.getString(ARGUMENTS_WINDOW_MAIN_KEY);
            if (method.equals("add")) {
                addNewAlarm(location, time, forecast_window);
            }
            else if (method.equals("edit")){
                id = args.getString(ARGUMENTS_ID_MAIN_KEY);
                weatherAlarm currentAlarm = alarmDBHelper.getSpecificAlarm(Integer.valueOf(id));
                if(currentAlarm.getIsOn() == 1){
                    activateAlarm(currentAlarm, false);
                }
                for (int i = 0; i < alarms.size(); i++) {
                    if (alarms.get(i).getID() == currentAlarm.getID()) {
                        currentPosition = i;
                        break;
                    }
                }
                currentAlarm.setLocation(location);
                currentAlarm.setTimeOfDay(time);
                currentAlarm.setForecastType(forecast_window);
                alarmDBHelper.editAlarm(currentAlarm);
                editItemAlarmList(currentPosition);
                if(currentAlarm.getIsOn() == 1){
                    activateAlarm(currentAlarm, true);
                }
            }
        }
        addButton = v.findViewById(R.id.fab_button);
        alarmRecyclerView = v.findViewById(R.id.alarmRecyclerView);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());
        alarmRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSnackBarOn();
                String[] arguments_add = new String[5];
                mCallback.addFragmentRequested("add", arguments_add);
            }
        });

        showAlarmList();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            // Method to decide the reaction to a swipe
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                // If swipe left --> remove alarm
                if (direction == ItemTouchHelper.LEFT) {
                    checkSnackBarOn();
                    deletedPosition = viewHolder.getAdapterPosition();
                    deletedAlarm = alarms.get(deletedPosition);
                    alarms.remove(deletedAlarm);
                    mAlarmAdapter.updateList(alarms);
                    mAlarmAdapter.notifyItemRemoved(deletedPosition);
                    deleteSnackbar = Snackbar.make(v, R.string.alarmRemovedMessage, Snackbar.LENGTH_LONG);
                    deleteSnackbar.setAction(R.string.undoMessage, new undoRemoveListener());
                    deleteSnackbar.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar deleteSnackbar, int event) {
                            if (event == DISMISS_EVENT_TIMEOUT){
                                activateAlarm(deletedAlarm, false);
                                alarmDBHelper.deleteAlarm(deletedAlarm);
                            }
                            deletedPosition = -1;
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {
                        }
                    });
                    deleteSnackbar.show();
                }

                // If swipe right, then edit alarm (but only if it is not active)
                else {
                    editedPosition = viewHolder.getAdapterPosition();
                    editedAlarm = alarms.get(editedPosition);
                    checkSnackBarOn();
                    String[] arguments_edit = new String[4];
                    arguments_edit[0] = String.valueOf(editedAlarm.getID());
                    arguments_edit[1] = editedAlarm.getLocation();
                    arguments_edit[2] = editedAlarm.getTimeOfDay();
                    arguments_edit[3] = editedAlarm.getForecastType();
                    mCallback.addFragmentRequested("edit", arguments_edit);
                }
            }

            // Method to draw the animation of swiping
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#81d4fa"));
                        RectF background = new RectF((float) itemView.getLeft(),
                                (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.edit_icon);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width,
                                (float) itemView.getTop() + width,
                                (float) itemView.getLeft() + 2 * width,
                                (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor("#F78877"));
                        RectF background = new RectF((float) itemView.getRight() + dX,
                                (float) itemView.getTop(), (float) itemView.getRight(),
                                (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.delete_icon);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width,
                                (float) itemView.getTop() + width,
                                (float) itemView.getRight() - width,
                                (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(alarmRecyclerView);

        return v;
    }

    private void checkSnackBarOn(){
        if (deletedPosition != -1){
            activateAlarm(deletedAlarm, false);
            alarmDBHelper.deleteAlarm(deletedAlarm);
            deletedPosition = -1;
        }
    }

    // Method to populate the recyclerview with the stored alarms of the database
    private void showAlarmList() {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter = new alarmAdapter(alarms);
        alarmRecyclerView.setAdapter(mAlarmAdapter);
    }

    // Method to add new alarm to database and notify addItemAlarmList method
    private void addNewAlarm(String newLocation, String newTime, String newForecastType) {
        weatherAlarm newAlarm = new weatherAlarm();
        newAlarm.setDefaultID();
        newAlarm.setLocation(newLocation);
        newAlarm.setTimeOfDay(newTime);
        newAlarm.setForecastType(newForecastType);
        newAlarm.setIsOn(0);
        alarmDBHelper.addAlarm(newAlarm);
        addItemAlarmList();
    }

    // Method to edit an item from the recyclerview
    private void editItemAlarmList(int currentPosition) {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemChanged(currentPosition);
    }

    // Method to add a new alarm in the recyclerview
    private void addItemAlarmList() {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemInserted(alarms.size() - 1);
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

    private class undoRemoveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            alarms = alarmDBHelper.getAlarms();
            mAlarmAdapter.updateList(alarms);
            mAlarmAdapter.notifyItemInserted(deletedPosition);
        }
    }



    private class alarmHolder extends RecyclerView.ViewHolder {
        private TextView locationTimeTextView;
        private TextView forecastTypeTextView;
        private Switch activateSwitch;

        public alarmHolder(View itemView) {
            super(itemView);
            locationTimeTextView = itemView.findViewById(R.id.locationTimeTextView);
            forecastTypeTextView = itemView.findViewById(R.id.forecastTypeTextView);
            activateSwitch = itemView.findViewById(R.id.activateSwitch);
        }

    }

    private class alarmAdapter extends RecyclerView.Adapter<alarmHolder> {

        private List<weatherAlarm> weatherAlarmList;

        public alarmAdapter(List<weatherAlarm> list) {
            weatherAlarmList = list;
        }

        @Override
        public alarmHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.alarm_list_item, parent, false);
            return new alarmHolder(view);
        }

        @Override
        public void onBindViewHolder(final alarmHolder holder, final int position) {
            final weatherAlarm alarm = weatherAlarmList.get(position);
            holder.locationTimeTextView.
                    setText(alarm.getLocation() + ", " + alarm.getTimeOfDay() + "h");

            if(alarm.getForecastType() != null){
                if (alarm.getForecastType().equals("Rest of the Day")){
                    holder.forecastTypeTextView.setText(R.string.restOfDayType);
                }
                else if(alarm.getForecastType().equals("Next 24h")){
                    holder.forecastTypeTextView.setText(R.string.next24HoursType);
                }
                else if(alarm.getForecastType().equals("Next Day")){
                    holder.forecastTypeTextView.setText(R.string.nextDayType);
                }
                else if(alarm.getForecastType().equals("Next 7 Days")){
                    holder.forecastTypeTextView.setText(R.string.next7DaysType);
                }
                else if(alarm.getForecastType().equals("Next Weekend")){
                    holder.forecastTypeTextView.setText(R.string.nextWeekendType);
                }
            }

            if(alarm.getIsOn()==1){
                holder.activateSwitch.setChecked(true);
            }
            else {
                holder.activateSwitch.setChecked(false);
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(MainActivity.getInstance(),
                            R.string.longPressToast,
                            Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkSnackBarOn();
                    String[] arguments_edit = new String[4];
                    arguments_edit[0] = String.valueOf(alarm.getID());
                    arguments_edit[1] = alarm.getLocation();
                    arguments_edit[2] = alarm.getTimeOfDay();
                    arguments_edit[3] = alarm.getForecastType();
                    mCallback.addFragmentRequested("edit", arguments_edit);
                }
            });

            holder.activateSwitch.
                    setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        alarm.setIsOn(1);
                    }
                    else {
                        alarm.setIsOn(0);
                    }
                    activateAlarm(alarm, isChecked);
                    alarmDBHelper.editAlarm(alarm);
                }
            });
        }

        @Override
        public int getItemCount() {
            return weatherAlarmList.size();
        }

        public void updateList(List<weatherAlarm> list) {
            weatherAlarmList = list;
        }
    }
}