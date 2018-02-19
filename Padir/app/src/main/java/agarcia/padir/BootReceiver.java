package agarcia.padir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 19/02/2018.
 */

public class BootReceiver extends BroadcastReceiver {

    private List<weatherAlarm> alarms;
    private dbHelper alarmDBHelper;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            alarmDBHelper = new dbHelper(context);
            alarms = alarmDBHelper.getAlarms();
            for (int i = 0; i < alarms.size(); i++) {
                if (alarms.get(i).getIsOn() == 1){
                    activateAlarmAfterBoot(alarms.get(i), context);
                }
            }
        }
    }

    public void activateAlarmAfterBoot(weatherAlarm alarm, Context context){
        Intent intent = new Intent(context, AlarmReceiver.class);
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
        final PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarm.getID(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) context.
                getSystemService(context.ALARM_SERVICE);
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

}
