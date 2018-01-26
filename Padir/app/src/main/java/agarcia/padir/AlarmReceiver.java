package agarcia.padir;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 03/11/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static String finalUrl = "";

    private static String result = "";
    private static String forecast = "";

    private static final String AEMET_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4LmdhcmNpYS5mYXJyZW55QGdtYWlsLmNvbSIsImp0aSI6IjI3Yjg0ZDI5LWIyNTItNDczZS1hZmFiLTNmMWZjNTM2NjAzNCIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNTA5MjEzMzcxLCJ1c2VySWQiOiIyN2I4NGQyOS1iMjUyLTQ3M2UtYWZhYi0zZjFmYzUzNjYwMzQiLCJyb2xlIjoiIn0.eih25hJWWt7cVIgFN-HuNv1KP8rbDMrWYGjvczJREoE";



    NotificationCompat.Builder builderRainyTomorrow;
    NotificationCompat.Builder builderRainyToday;
    NotificationCompat.Builder builderNoConnection;
    NotificationCompat.BigTextStyle styleRainyTomorrow;
    NotificationCompat.BigTextStyle styleRainyToday;
    Notification notificationRainyTomorrow;
    Notification notificationRainyToday;
    Notification notificationNoConnection;
    NotificationManagerCompat notificationManagerCompatRainyTomorrow;
    NotificationManagerCompat notificationManagerCompatRainyToday;
    NotificationManagerCompat notificationManagerCompatNoConnection;

    Intent intentNC;
    PendingIntent alarmIntentNC;
    AlarmManager alarmManagerNC;

    dbHelper database = new dbHelper(MainActivity.getInstance());

    String locationCode = "";

    int alarmID;
    String location = "";
    String time = "";
    String forecastType = "";

    long[] vibrationPattern = {500, 1000};

    @Override
    public void onReceive(Context context, Intent intent){

        alarmID = intent.getIntExtra("ID", 0);
        location = intent.getStringExtra("location");
        time = intent.getStringExtra("time");
        forecastType = intent.getStringExtra("forecastType");

        locationCode = database.getCode(location);

        //Rainy tomorrow notification
        builderRainyTomorrow = new NotificationCompat.Builder(context);
        styleRainyTomorrow = new NotificationCompat.BigTextStyle(builderRainyTomorrow);
        notificationManagerCompatRainyTomorrow = NotificationManagerCompat.from(context);
        styleRainyTomorrow.setBigContentTitle(context.getResources().getString(R.string.expectedTomorrow) + " " + location);
        styleRainyTomorrow.setSummaryText(context.getResources().getString(R.string.app_name));
        notificationRainyTomorrow = builderRainyTomorrow.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.tomorrowForecast))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.rainy_icon))
                .setSmallIcon(R.drawable.notification_cloud)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT))
                .setLights(Color.BLUE, 1000, 4000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(vibrationPattern)
                .setAutoCancel(true)
                .build();

        //Rainy today notification
        builderRainyToday = new NotificationCompat.Builder(context);
        styleRainyToday = new NotificationCompat.BigTextStyle(builderRainyToday);
        notificationManagerCompatRainyToday = NotificationManagerCompat.from(context);
        styleRainyToday.setBigContentTitle(context.getResources().getString(R.string.expectedToday) + " " + location);
        styleRainyToday.setSummaryText(context.getResources().getString(R.string.app_name));
        notificationRainyToday = builderRainyToday.setContentTitle(context.getResources()
                .getString(R.string.app_name))
                .setContentText(location + " " + context.getResources().getString(R.string.todayForecast))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.rainy_icon))
                .setSmallIcon(R.drawable.notification_cloud)
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
                .setContentIntent(PendingIntent.getActivity(context, 0,
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
                        getUrlString("https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/horaria/" +
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
                notificationManagerCompatNoConnection.notify("No connection",
                        (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE),
                        notificationNoConnection);
                alarmManagerNC.setExact(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 60 * 1000, alarmIntentNC);
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
            if(forecastType.equals("Same Day")){
                // actual day forecast required
                forecast = isTomorrowRaining(result, false);
                if (!forecast.equals("")){
                    styleRainyToday.bigText(forecast);
                    notificationRainyToday = builderRainyToday.build();
                    notificationManagerCompatRainyToday.notify(alarmID, notificationRainyToday);
                }
                else{
                    styleRainyToday.bigText(c.getResources().getString(R.string.noRainToday));
                    notificationRainyToday = builderRainyToday.build();
                    notificationManagerCompatRainyToday.notify(alarmID, notificationRainyToday);
                }
            }
            else if (forecastType.equals("Next Day")) {
                // tomorrow's forecast required
                forecast = isTomorrowRaining(result, true);
                if (!forecast.equals("")){
                    styleRainyTomorrow.bigText(forecast);
                    notificationRainyTomorrow = builderRainyTomorrow.build();
                    notificationManagerCompatRainyTomorrow.notify(alarmID, notificationRainyTomorrow);
                }
                else {
                    styleRainyTomorrow.bigText(c.getResources().getString(R.string.noRainTomorrow));
                    notificationRainyTomorrow = builderRainyTomorrow.build();
                    notificationManagerCompatRainyTomorrow.notify(alarmID, notificationRainyTomorrow);
                }
            }

            Intent intent = new Intent(MainActivity.getInstance(), AlarmReceiver.class);
            intent.putExtra("ID", alarmID);
            intent.putExtra("location", location);
            intent.putExtra("time", time);
            intent.putExtra("forecastType", forecastType);
            intent.setAction("dummy_unique_action_identifyer" + alarmID);

            final PendingIntent alarmIntent = PendingIntent.getBroadcast(MainActivity.getInstance(),
                    alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            final AlarmManager alarmManager = (AlarmManager) MainActivity.getInstance()
                    .getSystemService(MainActivity.getInstance().ALARM_SERVICE);

            String[] time_list = time.split(":");
            Calendar calendar_alarm = Calendar.getInstance();
            calendar_alarm.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_list[0]));
            // Is interesting to ensure that time is computed
            long calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            calendar_alarm.set(Calendar.MINUTE, Integer.valueOf(time_list[1]));
            calendar_alarm_millis = calendar_alarm.getTimeInMillis();
            calendar_alarm.add(Calendar.DAY_OF_YEAR, 1);
            calendar_alarm_millis = calendar_alarm.getTimeInMillis();

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar_alarm_millis,
                    alarmIntent);
        }
    }
    //function to extract the final url to get weather forecast from first json response
    private String finalUrlGetter(String response){
        /*try {
            JSONArray reader = new JSONArray(response);
            JSONObject totalData = reader.getJSONObject(0);
            String finalUrl = totalData.getString("datos");
        }
        catch (JSONException e){

        }
        return finalUrl;*/
        String[] responseList = response.split("\"");
        String finalUrl = responseList[responseList.length - 6];
        return finalUrl;
    }


    //function to get raining data for tomorrow (output) from api json feed(input)
    private String isTomorrowRaining(String feed, boolean isForTomorrow){
        String[] time = new String[24];
        String[] rain = new String[24];
        String[] odds = new String[24];
        String returnString = "";
        try{
            JSONArray reader = new JSONArray(feed);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            JSONObject tomorrowForecast;
            if (isForTomorrow){
                tomorrowForecast = forecastArray.getJSONObject(1);
            }
            else {
                tomorrowForecast = forecastArray.getJSONObject(0);
            }
            JSONArray rainOddsTomorrow = tomorrowForecast.getJSONArray("probPrecipitacion");
            JSONArray rainTomorrow = tomorrowForecast.getJSONArray("precipitacion");
            JSONObject periodOdds;
            for(int i = 0; i < 24; i++){
                if(i < 7){
                    periodOdds = rainOddsTomorrow.getJSONObject(0);
                }
                else if (i >= 7 && i < 13){
                    periodOdds = rainOddsTomorrow.getJSONObject(1);
                }
                else if (i >= 13 && i < 19){
                    periodOdds = rainOddsTomorrow.getJSONObject(2);
                }
                else {
                    periodOdds = rainOddsTomorrow.getJSONObject(3);
                }
                time[i] = String.valueOf(i);
                rain[i] = rainTomorrow.getJSONObject(i).getString("value");
                odds[i] = periodOdds.getString("value");
                if (!rain[i].equals("0") && !rain[i].equals("")){
                    returnString = returnString + "Time: " + time[i] + ", Rain: " + rain[i] +
                            ", Odds: " + odds[i] + "\n";
                }
            }
        }
        catch (JSONException e){
            //oops
        }
        return returnString;
    }
}
