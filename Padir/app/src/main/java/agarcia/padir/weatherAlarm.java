package agarcia.padir;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

/**
 * Created by agarcia on 12/11/2017.
 */

public class weatherAlarm {

    //instance identification is missing, either with an ID or in a list

    private int ID;
    private String location;
    private String timeOfDay;
    private String forecastType;
    private int isOn; // if 1 --> active, if 0 --> inactive

    public weatherAlarm(){

    }

    // Method to set the defautl identifier of the alarm
    public void setDefaultID(){
        SharedPreferences sharedPreferences = MainActivity.getInstance()
                .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE);
        int alarm_counter = sharedPreferences.getInt("alarm_counter", 0);
        alarm_counter++;
        ID = alarm_counter;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("alarm_counter", alarm_counter);
        editor.apply();
    }

    // Method to get the identifier of the alarm
    public int getID(){
        return ID;
    }

    // Method to set the identifier of the alarm
    public void setID(int id){
        ID = id;
    }

    /* Method to get the location's name. Currently: Barcelona, Alp, El Port de la Selva, Alcaufar, ...*/
    public String getLocation(){
        return location;
    }

    public void setLocation(String locationString){
        location = locationString;
    }

    /* Method to get the time of day to notify the user. Any number between 0 and 23.*/
    public String getTimeOfDay(){
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDayString){
        timeOfDay = timeOfDayString;
    }

    // Method to get the type of forecast. Currently: same day, following day, ...
    public String getForecastType(){
        return forecastType;
    }

    // Method to set the type of forecast of the alarm
    public void setForecastType(String forecastTypeString){
        forecastType = forecastTypeString;
    }

    // Method to check whether the alarm is active or not
    public int getIsOn(){
        return isOn;
    }

    // Method to activate or deactivate the alarm
    public void setIsOn(int truefalse){
        isOn = truefalse;
    }

}
