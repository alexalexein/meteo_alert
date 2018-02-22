package agarcia.padir;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 03/11/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static String finalUrl = "";

    private static String result = "";
    private static String forecast = "";

    private static String requestType;

    private static final String AEMET_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4LmdhcmNpYS5mYXJyZW55QGdtYWlsLmNvbSIsImp0aSI6IjI3Yjg0ZDI5LWIyNTItNDczZS1hZmFiLTNmMWZjNTM2NjAzNCIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNTA5MjEzMzcxLCJ1c2VySWQiOiIyN2I4NGQyOS1iMjUyLTQ3M2UtYWZhYi0zZjFmYzUzNjYwMzQiLCJyb2xlIjoiIn0.eih25hJWWt7cVIgFN-HuNv1KP8rbDMrWYGjvczJREoE";

    NotificationCompat.Builder builderTomorrow;
    NotificationCompat.Builder builderRestOfDay;
    NotificationCompat.Builder builderNext24h;
    NotificationCompat.Builder builderNext6Days;
    NotificationCompat.Builder builderNextWeekend;
    NotificationCompat.Builder builderNoConnection;
    NotificationCompat.BigTextStyle styleTomorrow;
    NotificationCompat.BigTextStyle styleRestOfDay;
    NotificationCompat.BigTextStyle styleNext24h;
    NotificationCompat.BigTextStyle styleNext6Days;
    NotificationCompat.BigTextStyle styleNextWeekend;
    Notification notificationTomorrow;
    Notification notificationRestOfDay;
    Notification notificationNext24h;
    Notification notificationNext6Days;
    Notification notificationNextWeekend;
    Notification notificationNoConnection;
    NotificationManagerCompat notificationManagerCompatTomorrow;
    NotificationManagerCompat notificationManagerCompatRestOfDay;
    NotificationManagerCompat notificationManagerCompatNext24h;
    NotificationManagerCompat notificationManagerCompatNext6Days;
    NotificationManagerCompat notificationManagerCompatNextWeekend;
    NotificationManagerCompat notificationManagerCompatNoConnection;

    Intent intentNC;
    PendingIntent alarmIntentNC;
    AlarmManager alarmManagerNC;



    String locationCode = "";

    int alarmID;
    String location = "";
    String time = "";
    String forecastType = "";

    long[] vibrationPattern = {500, 1000};

    // SKY STATES PRECIPITATION
    private final int NO_PRECIPITATION = 1;
    private final int RAIN = 2;
    private final int SNOW = 3;
    private final int SCARCE_RAIN = 4;
    private final int SCARCE_SNOW = 7;

    // SKY STATE CLOUDS
    private final int CLEAR = 1;
    private final int SLIGTHLY_CLOUDY = 2;
    private final int PARTLY_CLOUDY = 3;
    private final int MOSTLY_CLOUDY = 4;
    private final int CLOUDY = 5;
    private final int OVERCAST_SKY = 6;
    private final int HIGH_CLOUDS = 7;

    // Weekend numbers
    private final int SATURDAY = 7;
    private final int SUNDAY = 1;


    @Override
    public void onReceive(Context context, Intent intent){

        dbHelper database = new dbHelper(context);

        alarmID = intent.getIntExtra("ID", 0);
        location = intent.getStringExtra("location");
        time = intent.getStringExtra("time");
        forecastType = intent.getStringExtra("forecastType");

        locationCode = database.getCode(location);

        if (forecastType.equals("Next 6 Days") || forecastType.equals("Next Weekend")){
            requestType = "diaria";
        }
        else {
            requestType = "horaria";
        }

        Log.i("DEBUGGING", "ID: " + alarmID);
        Log.i("DEBUGGING", "Location: " + location);
        Log.i("DEBUGGING", "Time: " + time);
        Log.i("DEBUGGING", "Forecast Type: " + forecastType);
        Log.i("DEBUGGING", "Location Code: " + locationCode);

        // Tomorrow notification
        builderTomorrow = new NotificationCompat.Builder(context);
        styleTomorrow = new NotificationCompat.BigTextStyle(builderTomorrow);
        notificationManagerCompatTomorrow = NotificationManagerCompat.from(context);
        styleTomorrow.setBigContentTitle(context.getResources().getString(R.string.expectedTomorrow) + " " + location);
        styleTomorrow.setSummaryText(context.getResources().getString(R.string.dataProviderThanks));
        notificationTomorrow = builderTomorrow.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.tomorrowForecast))
                .setSmallIcon(R.drawable.notification_cloud)
                .setColor(context.getResources().getColor(R.color.light_blue_notification))
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrationPattern)
                .setAutoCancel(true)
                .build();

        // Rest of day notification
        builderRestOfDay = new NotificationCompat.Builder(context);
        styleRestOfDay = new NotificationCompat.BigTextStyle(builderRestOfDay);
        notificationManagerCompatRestOfDay = NotificationManagerCompat.from(context);
        styleRestOfDay.setBigContentTitle(context.getResources().getString(R.string.expectedRestOfDay) + " " + location);
        styleRestOfDay.setSummaryText(context.getResources().getString(R.string.dataProviderThanks));
        notificationRestOfDay = builderRestOfDay.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.restOfDayForecast))
                .setSmallIcon(R.drawable.notification_cloud)
                .setColor(context.getResources().getColor(R.color.light_blue_notification))
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrationPattern)
                .setAutoCancel(true)
                .build();

        // Next 24h notification
        builderNext24h = new NotificationCompat.Builder(context);
        styleNext24h = new NotificationCompat.BigTextStyle(builderNext24h);
        notificationManagerCompatNext24h = NotificationManagerCompat.from(context);
        styleNext24h.setBigContentTitle(context.getResources().getString(R.string.expectedNext24h) + " " + location);
        styleNext24h.setSummaryText(context.getResources().getString(R.string.dataProviderThanks));
        notificationNext24h = builderNext24h.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.next24hForecast))
                .setSmallIcon(R.drawable.notification_cloud)
                .setColor(context.getResources().getColor(R.color.light_blue_notification))
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrationPattern)
                .setAutoCancel(true)
                .build();

        // Next 6 days notification
        builderNext6Days = new NotificationCompat.Builder(context);
        styleNext6Days = new NotificationCompat.BigTextStyle(builderNext6Days);
        notificationManagerCompatNext6Days = NotificationManagerCompat.from(context);
        styleNext6Days.setBigContentTitle(context.getResources().getString(R.string.expectedNext6Days) + " " + location);
        styleNext6Days.setSummaryText(context.getResources().getString(R.string.dataProviderThanks));
        notificationNext6Days = builderNext6Days.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.next6DaysForecast))
                .setSmallIcon(R.drawable.notification_cloud)
                .setColor(context.getResources().getColor(R.color.light_blue_notification))
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrationPattern)
                .setAutoCancel(true)
                .build();

        // Next weekend notification
        builderNextWeekend = new NotificationCompat.Builder(context);
        styleNextWeekend = new NotificationCompat.BigTextStyle(builderNextWeekend);
        notificationManagerCompatNextWeekend = NotificationManagerCompat.from(context);
        styleNextWeekend.setBigContentTitle(context.getResources().getString(R.string.expectedNextWeekend) + " " + location);
        styleNextWeekend.setSummaryText(context.getResources().getString(R.string.dataProviderThanks));
        notificationNextWeekend = builderNextWeekend.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.nextWeekendForecast))
                .setSmallIcon(R.drawable.notification_cloud)
                .setColor(context.getResources().getColor(R.color.light_blue_notification))
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrationPattern)
                .setAutoCancel(true)
                .build();

        //No connection notification
        builderNoConnection = new NotificationCompat.Builder(context);
        notificationManagerCompatNoConnection = NotificationManagerCompat.from(context);
        notificationNoConnection = builderNoConnection
                .setContentTitle(context.getResources().getString(R.string.unableProvider))
                .setContentText(context.getResources().getString(R.string.noInternet))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.no_internet_icon))
                .setSmallIcon(R.drawable.notification_cloud)
                .setColor(context.getResources().getColor(R.color.light_blue_notification))
                .setContentIntent(PendingIntent.getActivity(context, alarmID,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setAutoCancel(true)
                .build();

        //No connection alarm repetition scheduler
        intentNC = new Intent(context, AlarmReceiver.class);
        intentNC.putExtra("ID", alarmID);
        intentNC.putExtra("location", location);
        intentNC.putExtra("time", time);
        intentNC.putExtra("forecastType", forecastType);
        alarmIntentNC = PendingIntent.getBroadcast(context, 0, intentNC, 0);
        alarmManagerNC = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        AlarmReceiver.mainUrlFetcher mainForecastFetcher = new AlarmReceiver.mainUrlFetcher(context);
        mainForecastFetcher.execute();
    }


    /* from first url it gets (using finalUrlGetter method) the second and final url.
    It then executes finalUrlFetcher.*/
    private class mainUrlFetcher extends AsyncTask<Void,Void,Void> {

        Context c;

        public mainUrlFetcher(Context c){
            this.c = c;
        }

        @Override
        protected Void doInBackground(Void... params){

            try {
                result = new urlFetcher().
                        getUrlString("https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/"
                                + requestType + "/" +
                                locationCode + "/?api_key=" + AEMET_API_KEY);
                finalUrl = finalUrlGetter(result);
            }

            catch (IOException ioe){
                finalUrl = "nc";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (finalUrl.startsWith("https")){
                new AlarmReceiver.finalUrlFetcher(c).execute();
            }
            else{
                notificationManagerCompatNoConnection.notify(alarmID, notificationNoConnection);
                if (Build.VERSION.SDK_INT >= 23) {
                    alarmManagerNC.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60 * 1000, alarmIntentNC);
                }
                else if (Build.VERSION.SDK_INT >= 19) {
                    alarmManagerNC.setExact(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60 * 1000, alarmIntentNC);
                }
                else {
                    alarmManagerNC.set(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60 * 1000, alarmIntentNC);
                }
            }
        }
    }

    //it reads and analyses weather forecast (isTomorrowRaining) to output results in textView
    private class finalUrlFetcher extends AsyncTask<Void, Void, Void>{

        Context c;

        public finalUrlFetcher(Context c) {
            this.c = c;
        }

        @Override
        protected Void doInBackground(Void... params){
            try {
                result = new urlFetcher().getUrlString(finalUrl);
            }
            catch (IOException ioe){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            if(forecastType.equals("Rest of the Day")){
                // rest of day forecast required
                forecast = getRestOfDayForecast(result, c);
                styleRestOfDay.bigText(forecast);
                notificationRestOfDay = builderRestOfDay.build();
                notificationManagerCompatRestOfDay.notify(alarmID, notificationRestOfDay);
            }
            else if (forecastType.equals("Next 24h")){
                int currentHourNumber = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if(currentHourNumber<=20){
                    // next 24h forecast required
                    forecast = getNext24HourForecast(result, c);
                    styleNext24h.bigText(forecast);
                    notificationNext24h = builderNext24h.build();
                    notificationManagerCompatNext24h.notify(alarmID, notificationNext24h);
                }
                else {
                    // next 24h forecast required
                    forecast = getNextDayForecast(result, c);
                    styleNext24h.bigText(forecast);
                    notificationNext24h = builderNext24h.build();
                    notificationManagerCompatNext24h.notify(alarmID, notificationNext24h);
                }
            }
            else if (forecastType.equals("Next Day")) {
                // next day forecast required
                forecast = getNextDayForecast(result, c);
                styleTomorrow.bigText(forecast);
                notificationTomorrow = builderTomorrow.build();
                notificationManagerCompatTomorrow.notify(alarmID, notificationTomorrow);
            }
            else if (forecastType.equals("Next 6 Days")){
                // next 6 days forecast required
                forecast = getNext6DaysForecast(result, c);
                styleNext6Days.bigText(forecast);
                notificationNext6Days = builderNext6Days.build();
                notificationManagerCompatNext6Days.notify(alarmID, notificationNext6Days);
            }
            else if (forecastType.equals("Next Weekend")){
                // next weekend forecast required
                forecast = getNextWeekendForecast(result, c);
                if(!forecast.equals("") && forecast != "" && forecast != null){
                    styleNextWeekend.bigText(forecast);
                    notificationNextWeekend = builderNextWeekend.build();
                    notificationManagerCompatNextWeekend.notify(alarmID, notificationNextWeekend);
                }
            }

            Intent intent = new Intent(c, AlarmReceiver.class);
            intent.putExtra("ID", alarmID);
            intent.putExtra("location", location);
            intent.putExtra("time", time);
            intent.putExtra("forecastType", forecastType);
            intent.setAction("dummy_unique_action_identifyer" + alarmID);

            final PendingIntent alarmIntent = PendingIntent.getBroadcast(c, alarmID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final AlarmManager alarmManager = (AlarmManager) c.getSystemService(c.ALARM_SERVICE);

            String[] time_list = time.split(":");
            Calendar calendar_alarm = Calendar.getInstance();
            calendar_alarm.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_list[0]));
            // Is interesting to ensure that time is computed
            long calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            calendar_alarm.set(Calendar.MINUTE, Integer.valueOf(time_list[1]));
            calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            calendar_alarm.add(Calendar.DAY_OF_YEAR, 1);
            calendar_alarm_millis = calendar_alarm.getTimeInMillis();

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
    //function to extract the final url to get weather forecast from first json response
    private String finalUrlGetter(String response){
        String[] responseList = response.split("\"");
        String finalUrl = responseList[responseList.length - 6];
        return finalUrl;
    }


    ///////////////////////////////////REST OF DAY//////////////////////////////////////////////////

    // Method to get the weather forecast for the rest of the day
    private String getRestOfDayForecast(String feed, Context context){

        String[] timeList;
        String[] temperatureList;
        String[] skyStateList;
        String resultForecast = "";

        int currentDayNumber = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHourNumber = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Get today's forecast in JSONObject format
        JSONObject dayForecast = getForecastTodayRaw(currentDayNumber, feed);

        // Generation of time list
        timeList = getTimeListRestOfDay(currentHourNumber);

        // Generation of temperature list
        temperatureList = getTemperatureListRestOfDay(dayForecast, timeList);

        // Generation of sky state list
        skyStateList = getSkyStateListRestOfDay(dayForecast, timeList, context);

        // Generation of forecast to show in notification
        for(int i=0; i<timeList.length-1; i++){
            resultForecast = resultForecast + timeList[i] + "h, " + temperatureList[i] + "ºC, "
                    + skyStateList[i] + "\n";
        }
        resultForecast = resultForecast + timeList[timeList.length-1] + "h, "
                + temperatureList[timeList.length-1] + "ºC, "
                + skyStateList[timeList.length-1];
        return resultForecast;
    }

    private JSONObject getForecastTodayRaw(int currentDayOfMonth, String feed){
        try{
            JSONArray reader = new JSONArray(feed);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            JSONObject dayForecast;
            String forecastDate;
            String[] forecastDateList;

            // For loop to find which JSON Object is for today
            for(int i = 0; i < forecastArray.length(); i++){
                dayForecast = forecastArray.getJSONObject(i);
                forecastDate = dayForecast.getString("fecha");
                forecastDateList = forecastDate.split("-");
                if(Integer.valueOf(forecastDateList[2]) == currentDayOfMonth){
                    return dayForecast;
                }
            }
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getForecastTodayRaw: " + e.toString());
        }
        return new JSONObject();
    }

    private String[] getTimeListRestOfDay(int currentHourNumber){
        int timeListIndex = 0;
        int timeListSize = (24-currentHourNumber)/2;
        String[] timeList = new String[timeListSize];
        for(int i=currentHourNumber+1; i<24; i+=2){
            if(i<10){
                timeList[timeListIndex] = "0" + String.valueOf(i);
            }else{
                timeList[timeListIndex] = String.valueOf(i);
            }
            timeListIndex++;
        }
        return timeList;
    }

    private String[] getTemperatureListRestOfDay(JSONObject dayForecast, String[] timeList){
        try{
            JSONArray temperatureArray = dayForecast.getJSONArray("temperatura");
            String[] temperatureList = new String[timeList.length];
            for(int i=0; i<temperatureList.length; i++){
                for(int j=0; j<temperatureArray.length(); j++){
                    if(temperatureArray.getJSONObject(j).getString("periodo").equals(timeList[i])){
                        temperatureList[i] = temperatureArray.getJSONObject(j).getString("value");
                        break;
                    }
                }
            }
            return temperatureList;
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getTemperatureListRestOfDay: " + e.toString());
        }
        return new String[timeList.length];
    }

    private String[] getSkyStateListRestOfDay(JSONObject dayForecast, String[] timeList, Context context){
        try{
            JSONArray skyStateArray = dayForecast.getJSONArray("estadoCielo");
            JSONArray precipitationArray = dayForecast.getJSONArray("precipitacion");
            String[] skyStateList = new String[timeList.length];
            String skyStateCode;
            String precipitationValue;
            for(int i=0; i<skyStateList.length; i++){
                for(int j=0; j<skyStateArray.length(); j++){
                    if(skyStateArray.getJSONObject(j).getString("periodo").equals(timeList[i])){
                        skyStateCode = skyStateArray.getJSONObject(j).getString("value");
                        precipitationValue = precipitationArray.getJSONObject(j).getString("value");
                        skyStateList[i] = getSkyState(skyStateCode, precipitationValue, "", context);
                        break;
                    }
                }
            }
            return skyStateList;
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getSkyStateListRestOfDay: " + e.toString());
        }
        return new String[timeList.length];
    }

    private String getSkyState(String skyStateCode, String infoInBrackets, String inBracketsUnits, Context context){
        String skyStateCode_0;
        String skyStateCode_1;
        String result;
        if (skyStateCode != "" && !skyStateCode.equals("") && skyStateCode != null){
            skyStateCode_0 = Character.toString(skyStateCode.charAt(0));
            skyStateCode_1 = Character.toString(skyStateCode.charAt(1));
        }
        else {
            skyStateCode_0 = "0";
            skyStateCode_1 = "0";
        }
        switch (Integer.valueOf(skyStateCode_0)){
            case NO_PRECIPITATION:
                switch (Integer.valueOf(skyStateCode_1)){
                    case CLEAR:
                        result = context.getResources()
                                .getString(R.string.clearSkyState);
                        break;
                    case SLIGTHLY_CLOUDY:
                        result = context.getResources()
                                .getString(R.string.slightlyCloudySkyState);
                        break;
                    case PARTLY_CLOUDY:
                        result = context.getResources()
                                .getString(R.string.partlyCloudySkyState);
                        break;
                    case MOSTLY_CLOUDY:
                        result = context.getResources()
                                .getString(R.string.mostlyCloudySkyState);
                        break;
                    case CLOUDY:
                        result = context.getResources()
                                .getString(R.string.cloudySkyState);
                        break;
                    case OVERCAST_SKY:
                        result = context.getResources()
                                .getString(R.string.overcastSkyState);
                        break;
                    case HIGH_CLOUDS:
                        result = context.getResources()
                                .getString(R.string.highCloudsSkyState);
                        break;
                    default:
                        result = "Unknown: " + skyStateCode;
                        break;
                }
                break;
            case RAIN:
                result = context.getResources().getString(R.string.rainySkyState)
                        + " (" + infoInBrackets + " " + inBracketsUnits + ")";
                break;
            case SNOW:
                result = context.getResources().getString(R.string.snowySkyState)
                        + " (" + infoInBrackets + " " + inBracketsUnits + ")";
                break;
            case SCARCE_RAIN:
                result = context.getResources().getString(R.string.scarcelyRainySkyState)
                        + " (" + infoInBrackets + " " + inBracketsUnits + ")";
                break;
            case SCARCE_SNOW:
                result = context.getResources().getString(R.string.scarcelySnowySkyState)
                        + " (" + infoInBrackets + " " + inBracketsUnits + ")";
                break;
            default:
                result = "Unknown: " + skyStateCode;
                break;

        }
        return result;
    }

    /////////////////////////////////////END OF REST OF DAY/////////////////////////////////////////

    /////////////////////////////////////NEXT 24 HOURS//////////////////////////////////////////////

    // Method to get the weather forecast for the next 24h
    private String getNext24HourForecast(String feed, Context context){
        String[] timeListTomorrow;
        String[] temperatureListTomorrow;
        String[] skyStateListTomorrow;
        String resultForecastRestOfDay = getRestOfDayForecast(feed, context);
        String resultForecastToday = resultForecastRestOfDay.substring(resultForecastRestOfDay.indexOf("\n")+1);
        String resultForecastTomorrow = "";

        int currentDayNumber = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentHourNumber = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Get tomorrow's forecast in JSONObject format
        JSONObject tomorrowForecast = getForecastTomorrowRaw(currentDayNumber, feed);

        // Generation of time list for tomorrow in next 24 hours forecast
        timeListTomorrow = getTimeListTomorrow24h(currentHourNumber);

        // Generation of temperature list for tomorrow in next 24 hours forecast
        temperatureListTomorrow = getTemperatureListRestOfDay(tomorrowForecast, timeListTomorrow);

        // Generation of sky state list
        skyStateListTomorrow = getSkyStateListRestOfDay(tomorrowForecast, timeListTomorrow, context);

        // Generation of forecast to show in notification
        for(int i=0; i<timeListTomorrow.length-1; i++){
            resultForecastTomorrow = resultForecastTomorrow + timeListTomorrow[i]
                    + "h, " + temperatureListTomorrow[i] + "ºC, " + skyStateListTomorrow[i] + "\n";
        }
        resultForecastTomorrow = resultForecastTomorrow + timeListTomorrow[timeListTomorrow.length-1]
                + "h, " + temperatureListTomorrow[timeListTomorrow.length-1] + "ºC, "
                + skyStateListTomorrow[timeListTomorrow.length-1];

        return resultForecastToday + "\n" + resultForecastTomorrow;
    }

    private JSONObject getForecastTomorrowRaw(int currentDayNumber, String feed){
        try{
            JSONArray reader = new JSONArray(feed);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            JSONObject tomorrowForecast;
            String forecastDate;
            String[] forecastDateList;

            // For loop to find which JSON Object is for today
            for(int i = 0; i < forecastArray.length(); i++){
                tomorrowForecast = forecastArray.getJSONObject(i);
                forecastDate = tomorrowForecast.getString("fecha");
                forecastDateList = forecastDate.split("-");
                if(Integer.valueOf(forecastDateList[2]) == (currentDayNumber+1)){
                    return tomorrowForecast;
                }
            }
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getForecastTomorrowRaw: " + e.toString());
        }
        return new JSONObject();
    }

    private String[] getTimeListTomorrow24h(int currentHourNumber){
        int lengthOfTodayTimeList = (22 - currentHourNumber)/2;
        int lengthOfTomorrowTimeList = 10 - lengthOfTodayTimeList;
        String[] timeList = new String[lengthOfTomorrowTimeList];

        int timeListIndex = 0;

        int initialHour;
        if (currentHourNumber%2 == 0){
            initialHour = 1;
        }
        else {
            initialHour = 0;
        }

        for(int i=initialHour; i<=(initialHour + 2*(lengthOfTomorrowTimeList-1)); i+=2){
            if(i<10){
                timeList[timeListIndex] = "0" + String.valueOf(i);
            }else{
                timeList[timeListIndex] = String.valueOf(i);
            }
            timeListIndex++;
        }
        return timeList;
    }

    ///////////////////////////////////END OF NEXT 24 HOURS/////////////////////////////////////////

    ///////////////////////////////////////NEXT DAY/////////////////////////////////////////////////

    // Method to get the weather forecast for the next day
    private String getNextDayForecast(String feed, Context context){
        String[] timeList;
        String[] temperatureList;
        String[] skyStateList;
        String resultForecast = "";

        int currentDayNumber = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        // Get today's forecast in JSONObject format
        JSONObject tomorrowForecast = getForecastTomorrowRaw(currentDayNumber, feed);

        // Generation of time list
        timeList = getTimeListTomorrow();

        // Generation of temperature list
        temperatureList = getTemperatureListRestOfDay(tomorrowForecast, timeList);

        // Generation of sky state list
        skyStateList = getSkyStateListRestOfDay(tomorrowForecast, timeList, context);

        // Generation of forecast to show in notification
        for(int i=0; i<timeList.length-1; i++){
            resultForecast = resultForecast + timeList[i] + "h, " + temperatureList[i] + "ºC, "
                    + skyStateList[i] + "\n";
        }
        resultForecast = resultForecast + timeList[timeList.length-1] + "h, "
                + temperatureList[timeList.length-1] + "ºC, "
                + skyStateList[timeList.length-1];

        return resultForecast;
    }

    private String[] getTimeListTomorrow(){
        String[] timeList = new String[10];

        int timeListIndex = 0;

        for(int i=3; i<22; i+=2){
            if(i<10){
                timeList[timeListIndex] = "0" + String.valueOf(i);
            }else{
                timeList[timeListIndex] = String.valueOf(i);
            }
            timeListIndex++;
        }
        return timeList;
    }

    ////////////////////////////////////END OF NEXT DAY/////////////////////////////////////////////

    //////////////////////////////////////NEXT 6 DAYS///////////////////////////////////////////////

    // Method to get the weather forecast for the next days (plural)
    private String getNext6DaysForecast(String feed, Context context){
        String resultForecast = "";
        String[] dayList = getDaysList(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), feed, context);
        String[] temperatures = getTemperatures6DaysList(feed);
        String[] skyStateList = getSkyState6Days(feed, context);
        for (int i=0; i<5; i++){
            resultForecast = resultForecast + dayList[i] + ", " + temperatures[i] + ", " + skyStateList[i] + "\n";
        }
        resultForecast = resultForecast + dayList[5] + ", " + temperatures[5] + ", " + skyStateList[5];
        return resultForecast;
    }

    private String[] getDaysList(int DAY_OF_WEEK, String feed, Context context){
        Map<String, String> days_dict = new HashMap<>();
        days_dict.put("1", context.getResources().getString(R.string.sundayAbbrev));
        days_dict.put("2", context.getResources().getString(R.string.mondayAbbrev));
        days_dict.put("3", context.getResources().getString(R.string.tuesdayAbbrev));
        days_dict.put("4", context.getResources().getString(R.string.wednesdayAbbrev));
        days_dict.put("5", context.getResources().getString(R.string.thursdayAbbrev));
        days_dict.put("6", context.getResources().getString(R.string.fridayAbbrev));
        days_dict.put("7", context.getResources().getString(R.string.saturdayAbbrev));
        String[] result = new String[6];
        int currentDayNumber = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        try{
            JSONArray reader = new JSONArray(feed);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            String firstDate = forecastArray.getJSONObject(0).getString("fecha");
            int firstDayNumber = Integer.valueOf(firstDate.split("-")[2]);
            if(firstDayNumber == currentDayNumber){
                if(DAY_OF_WEEK==7){
                    DAY_OF_WEEK = 1;
                }
                else {
                    DAY_OF_WEEK++;
                }
            }
            for (int i=0; i<6; i++){
                result[i] = days_dict.get(String.valueOf(DAY_OF_WEEK));
                if(DAY_OF_WEEK == 7){
                    DAY_OF_WEEK = 1;
                }
                else {
                    DAY_OF_WEEK++;
                }
            }
            return result;
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Caught JSONException in getDaysList method.");
        }
        return result;
    }

    private String[] getTemperatures6DaysList(String feed){
        try{
            String maxTemp;
            String minTemp;
            JSONArray reader = new JSONArray(feed);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            String[] result = new String[forecastArray.length()-1];
            for(int i=1; i<forecastArray.length(); i++){
                JSONObject forecastDayObject = forecastArray.getJSONObject(i);
                JSONObject temperatureObject = forecastDayObject.getJSONObject("temperatura");
                maxTemp = temperatureObject.getString("maxima");
                minTemp = temperatureObject.getString("minima");
                result[i-1] = maxTemp + " ºC, " + minTemp + " ºC";
            }
            return result;
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getTemperatureListRestOfDay: " + e.toString());
        }
        return new String[6];
    }

    private String[] getSkyState6Days(String feed, Context context){
        try {
            String[] skyStateList = new String[6];
            String[] oddsList = new String[6];
            JSONArray reader = new JSONArray(feed);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            JSONObject dayForecast;
            JSONArray probPrecipitacion;
            JSONArray estadoCielo;
            String skyState;
            String odds;
            for (int i=1; i<forecastArray.length(); i++) {
                dayForecast = forecastArray.getJSONObject(i);
                probPrecipitacion = dayForecast.getJSONArray("probPrecipitacion");
                odds = probPrecipitacion.getJSONObject(0).getString("value");
                oddsList[i - 1] = odds;
                estadoCielo = dayForecast.getJSONArray("estadoCielo");
                skyState = estadoCielo.getJSONObject(0).getString("value");
                skyStateList[i - 1] = getSkyState(skyState, odds, "%", context);
            }
            return skyStateList;
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getSkyStateListNext6Days: " + e.toString());
        }
        return new String[6];
    }

    ///////////////////////////////////END OF NEXT 6 DAYS///////////////////////////////////////////

    /////////////////////////////////////NEXT WEEKEND///////////////////////////////////////////////

    // Method to get the weather forecast for the following weekend
    private String getNextWeekendForecast(String feed, Context context){
        String resultForecast = "";
        int currentDayNumber = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if(currentDayNumber != SATURDAY || currentDayNumber != SUNDAY){
            int currentDayWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int daysToSaturday = 7 - currentDayWeek;
            Calendar saturdayCalendar = Calendar.getInstance();
            saturdayCalendar.add(Calendar.DAY_OF_MONTH, daysToSaturday);
            int saturdayDayNumber = saturdayCalendar.get(Calendar.DAY_OF_MONTH);
            Calendar sundayCalendar = saturdayCalendar;
            sundayCalendar.add(Calendar.DAY_OF_MONTH, 1);
            int sundayDayNumber = sundayCalendar.get(Calendar.DAY_OF_MONTH);
            JSONObject saturdayForecastRaw = getForecastTodayRaw(saturdayDayNumber, feed);
            JSONObject sundayForecastRaw = getForecastTodayRaw(sundayDayNumber, feed);
            String saturdayForecast = context.getResources().getString(R.string.saturdayName) + ":\n";
            saturdayForecast = saturdayForecast +
                    getWeekendDayForecast(saturdayForecastRaw, context) + "\n\n";
            String sundayForecast = context.getResources().getString(R.string.sundayName) + ":\n";
            sundayForecast = sundayForecast + getWeekendDayForecast(sundayForecastRaw, context);
            resultForecast = saturdayForecast + sundayForecast;
        }
        return resultForecast;
    }

    private String getWeekendDayForecast(JSONObject dayForecastRaw, Context context){
        try{
            String skyState;
            String maxTemp = dayForecastRaw.getJSONObject("temperatura").getString("maxima");
            String minTemp = dayForecastRaw.getJSONObject("temperatura").getString("minima");
            JSONArray odds = dayForecastRaw.getJSONArray("probPrecipitacion");
            JSONArray skyStateArray = dayForecastRaw.getJSONArray("estadoCielo");
            if(odds.length()>1){
                String skyState_1 = getSkyState(skyStateArray.getJSONObject(1).getString("value"),
                        odds.getJSONObject(1).getString("value"), "%", context);
                String skyState_2 = getSkyState(skyStateArray.getJSONObject(2).getString("value"),
                        odds.getJSONObject(2).getString("value"), "%", context);
                skyState = "     " + "T. Max: " + maxTemp + " ºC, T. Min: " + minTemp + " ºC" +
                        "\n     " + skyState_1 + "\n     " + skyState_2;
            }
            else {
                String skyState_1 = getSkyState(skyStateArray.getJSONObject(0).getString("value"),
                        odds.getJSONObject(0).getString("value"), "%", context);
                skyState = "     " + "T. Max: " + maxTemp + " ºC, T. Min: " + minTemp + " ºC" +
                        "\n     " + skyState_1;
            }
            return skyState;
        }
        catch (JSONException e){
            Log.i("DEBUGGING", "Exception caught in method getWeekendDayForecast: " + e.toString());
        }
        return "";
    }

    ///////////////////////////////////END OF NEXT WEEKEND//////////////////////////////////////////
}
