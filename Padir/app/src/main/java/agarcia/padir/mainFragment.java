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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 19/11/2017.
 */

public class mainFragment extends Fragment {

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

    String id = "";

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
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("debug", "Inside onCreateView mainFragment");

        // Inflate Fragment View
        final View v = inflater.inflate(R.layout.alarm_list_fragment, container, false);
        addButton = v.findViewById(R.id.fab_button);
        alarmRecyclerView = v.findViewById(R.id.alarmRecyclerView);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());
        alarmRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.addFragmentRequested(0);
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
                    alarms = getOrderedAlarmList();
                    deletedPosition = viewHolder.getAdapterPosition();
                    deletedAlarm = alarms.get(deletedPosition);
                    removeAlarm(deletedAlarm, deletedPosition);
                    deleteSnackbar = Snackbar.make(v, R.string.alarmRemovedMessage, Snackbar.LENGTH_LONG);
                    deleteSnackbar.setAction(R.string.undoMessage, new undoRemoveListener());
                    deleteSnackbar.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                        }

                        @Override
                        public void onShown(Snackbar sb) {
                            super.onShown(sb);
                        }
                    });
                    deleteSnackbar.show();
                }

                // If swipe right, then edit alarm (but only if it is not active)
                else {
                    alarms = getOrderedAlarmList();
                    editedPosition = viewHolder.getAdapterPosition();
                    editedAlarm = alarms.get(editedPosition);
                    mCallback.addFragmentRequested(editedAlarm.getID());
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

    private List<weatherAlarm> getOrderedAlarmList(){
        alarms = alarmDBHelper.getAlarms();
        ArrayList<weatherAlarm> alarms_ordered = new ArrayList<>();
        int minIDvalue;
        int minIDindex;
        int sizeOfAlarmsList = alarms.size();
        for (int j=0; j<sizeOfAlarmsList; j++){
            minIDvalue = alarms.get(0).getID();
            minIDindex = 0;
            if(alarms.size()>1){
                for (int i=1; i<alarms.size(); i++){
                    if(alarms.get(i).getID()<minIDvalue){
                        minIDindex = i;
                        minIDvalue = alarms.get(i).getID();
                    }
                }
            }
            alarms_ordered.add(alarms.get(minIDindex));
            alarms.remove(minIDindex);
        }
        return alarms_ordered;
    }

    private int getPositionAlarm(List<weatherAlarm> alarms, weatherAlarm alarm){
        for (int i=0; i<alarms.size(); i++){
            if(alarms.get(i).getID()==alarm.getID()){
                return i;
            }
        }
        return -1;
    }

    // Method to remove an alarm both from the recyclerview and from the database
    private void removeAlarm(weatherAlarm alarm, int position){
        alarmDBHelper.deleteAlarm(alarm);
        alarms = getOrderedAlarmList();
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemRemoved(position);
        if (alarm.getIsOn() == 1){
            activateAlarm(alarm, false);
        }
    }

    // Method to populate the recyclerview with the stored alarms of the database
    private void showAlarmList() {
        alarms = getOrderedAlarmList();
        mAlarmAdapter = new alarmAdapter(alarms);
        alarmRecyclerView.setAdapter(mAlarmAdapter);
    }

    // Method to add new alarm to database and notify addItemAlarmList method
    private void addNewAlarm(int ID, String newLocation, String newTime, String newForecastType, int isOn) {
        weatherAlarm newAlarm = new weatherAlarm();
        if (ID == 0){
            newAlarm.setDefaultID();
        }
        else{
            newAlarm.setID(ID);
        }
        newAlarm.setLocation(newLocation);
        newAlarm.setTimeOfDay(newTime);
        newAlarm.setForecastType(newForecastType);
        newAlarm.setIsOn(isOn);
        if(isOn == 1){
            activateAlarm(newAlarm, true);
        }
        alarmDBHelper.addAlarm(newAlarm);
        addItemAlarmList(newAlarm);
    }

    // Method to add a new alarm in the recyclerview
    private void addItemAlarmList(weatherAlarm addedAlarm) {
        alarms = getOrderedAlarmList();
        int position = getPositionAlarm(alarms, addedAlarm);
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemInserted(position);
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
            addNewAlarm(deletedAlarm.getID(), deletedAlarm.getLocation(),
                    deletedAlarm.getTimeOfDay(), deletedAlarm.getForecastType(),
                    deletedAlarm.getIsOn());
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
                else if(alarm.getForecastType().equals("Next 6 Days")){
                    holder.forecastTypeTextView.setText(R.string.next6DaysType);
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
                    mCallback.addFragmentRequested(alarm.getID());
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
