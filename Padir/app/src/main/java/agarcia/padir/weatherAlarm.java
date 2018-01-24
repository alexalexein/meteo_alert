package agarcia.padir;

import java.util.UUID;

/**
 * Created by agarcia on 12/11/2017.
 */

public class weatherAlarm {

    //instance identification is missing, either with an ID or in a list

    private UUID ID;
    private String location;
    private String timeOfDay;
    private String forecastType;

    public weatherAlarm(){
        ID = UUID.randomUUID();
    }

    public UUID getID(){
        return ID;
    }

    public void setID(UUID uuid){
        ID = uuid;
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

    public void setForecastType(String forecastTypeString){
        forecastType = forecastTypeString;
    }

}
