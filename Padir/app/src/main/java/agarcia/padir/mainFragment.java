package agarcia.padir;

import android.app.Activity;
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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private int deletedPosition;
    private weatherAlarm deletedAlarm;

    private boolean snackbarShown = false;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.alarm_list_fragment, container, false);
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
                weatherAlarm currentAlarm = alarmDBHelper.getSpecificAlarm(UUID.fromString(id));
                activateAlarm(currentAlarm, false);
                for (int i = 0; i < alarms.size(); i++) {
                    if (alarms.get(i).getID().equals(currentAlarm.getID())) {
                        currentPosition = i;
                        break;
                    }
                }
                currentAlarm.setLocation(location);
                currentAlarm.setTimeOfDay(time);
                currentAlarm.setForecastType(forecast_window);
                alarmDBHelper.editAlarm(currentAlarm);
                editItemAlarmList(currentPosition);
                activateAlarm(currentAlarm, true);
            }
        }
        addButton = v.findViewById(R.id.fab_button);
        alarmRecyclerView = v.findViewById(R.id.alarmRecyclerView);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());
        alarmRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] arguments_add = new String[5];
                mCallback.addFragmentRequested("add", arguments_add);
            }
        });

        showAlarmList();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (snackbarShown){
                    deleteSnackbar.dismiss();
                    activateAlarm(deletedAlarm, false);
                    alarmDBHelper.deleteAlarm(deletedAlarm);
                }
                deletedPosition = viewHolder.getAdapterPosition();
                deletedAlarm = alarms.get(deletedPosition);
                if (direction == ItemTouchHelper.LEFT) {
                    alarms.remove(deletedAlarm);
                    mAlarmAdapter.updateList(alarms);
                    mAlarmAdapter.notifyItemRemoved(deletedPosition);
                    deleteSnackbar = Snackbar.make(v, R.string.alarmRemovedMessage, Snackbar.LENGTH_LONG);
                    deleteSnackbar.setAction(R.string.undoMessage, new undoRemoveListener());
                    deleteSnackbar.addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar deleteSnackbar, int event) {
                            snackbarShown = false;
                            Log.i("DEBUGGING", "OnDismiss Event: " + String.valueOf(event));
                            if (event != DISMISS_EVENT_ACTION){
                                activateAlarm(deletedAlarm, false);
                                alarmDBHelper.deleteAlarm(deletedAlarm);
                            }
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {
                            snackbarShown = true;
                        }
                    });
                    deleteSnackbar.show();
                } else {
                    String[] arguments_edit = new String[4];
                    arguments_edit[0] = deletedAlarm.getID().toString();
                    arguments_edit[1] = deletedAlarm.getLocation();
                    arguments_edit[2] = deletedAlarm.getTimeOfDay();
                    arguments_edit[3] = deletedAlarm.getForecastType();
                    mCallback.addFragmentRequested("edit", arguments_edit);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#81d4fa"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.edit_icon);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor("#F78877"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.delete_icon);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
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

    private void showAlarmList() {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter = new alarmAdapter(alarms);
        alarmRecyclerView.setAdapter(mAlarmAdapter);
    }

    private void addNewAlarm(String newLocation, String newTime, String newForecastType) {
        weatherAlarm newAlarm = new weatherAlarm();
        newAlarm.setLocation(newLocation);
        newAlarm.setTimeOfDay(newTime);
        newAlarm.setForecastType(newForecastType);
        alarmDBHelper.addAlarm(newAlarm);
        addItemAlarmList();
        activateAlarm(newAlarm, true);
    }

    private void editItemAlarmList(int currentPosition) {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemChanged(currentPosition);
    }

    private void addItemAlarmList() {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemInserted(alarms.size() - 1);
    }

    /*private void removeItemAlarmList(int position) {
        alarms = alarmDBHelper.getAlarms();
        mAlarmAdapter.updateList(alarms);
        mAlarmAdapter.notifyItemRemoved(position);
    }*/

    private void activateAlarm(weatherAlarm alarm, boolean activate) {
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("ID", alarm.getID().toString());
        intent.putExtra("location", alarm.getLocation());
        String alarm_time = alarm.getTimeOfDay();
        String[] time_list = alarm_time.split(":");
        if (time_list[0].startsWith("0")){
            time_list[0] = time_list[0].substring(1);
            Log.i("DEBUGGING", "Hour: " + time_list[0]);
        }
        if(time_list[1].startsWith("0")){
            time_list[1] = time_list[1].substring(1);
            Log.i("DEBUGGING", "Minute: " + time_list[1]);
        }
        intent.putExtra("time", time_list[0] + ":" + time_list[1]);
        intent.putExtra("forecastType", alarm.getForecastType());
        intent.setAction("dummy_unique_action_identifyer" + alarm.getID());
        final PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        if (activate) {
            Calendar calendar_now = Calendar.getInstance();
            calendar_now.add(Calendar.MINUTE, 1);
            Calendar calendar_alarm = Calendar.getInstance();
            calendar_alarm.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_list[0]));
            calendar_alarm.set(Calendar.MINUTE, Integer.valueOf(time_list[1]));
            if (calendar_alarm.before(calendar_now)){
                calendar_alarm.add(Calendar.DAY_OF_MONTH, 1);
            }
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmManager.INTERVAL_DAY, alarmIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 1000, 60 * 1000, alarmIntent);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar_alarm.getTimeInMillis(), alarmIntent);
        } else {
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
        private TextView mainWeatherEventTextView;
        private TextView mainLocationTextView;
        private TextView mainTimeTextView;
        private TextView mainForecastDateTextView;

        public alarmHolder(View itemView) {
            super(itemView);
            mainWeatherEventTextView = itemView.findViewById(R.id.mainWeatherEventTextView);
            mainLocationTextView = itemView.findViewById(R.id.mainLocationTextView);
            mainTimeTextView = itemView.findViewById(R.id.mainTimeTextView);
            mainForecastDateTextView = itemView.findViewById(R.id.mainForecastDateTextView);
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
            holder.mainLocationTextView.setText(alarm.getLocation());
            holder.mainTimeTextView.setText(alarm.getTimeOfDay() + " h");
            holder.mainForecastDateTextView.setText(alarm.getForecastType());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] arguments_edit = new String[4];
                    arguments_edit[0] = alarm.getID().toString();
                    arguments_edit[1] = alarm.getLocation();
                    arguments_edit[2] = alarm.getTimeOfDay();
                    arguments_edit[3] = alarm.getForecastType();
                    mCallback.addFragmentRequested("edit", arguments_edit);
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
