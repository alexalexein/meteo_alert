package agarcia.padir.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import agarcia.padir.MainActivity;
import agarcia.padir.weatherAlarm;

/**
 * Created by agarcia on 03/12/2017.
 */

public class dbHelper extends SQLiteOpenHelper {

    private static final int VERSION = 4;
    private static final String DATABASE_NAME = "alarmDB.db";

    public dbHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + dbSchema.dbTable.NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                dbSchema.dbTable.Cols.UUID + " TEXT, " +
                dbSchema.dbTable.Cols.LOCATION + " TEXT, " +
                dbSchema.dbTable.Cols.TIME_OF_DAY + " TEXT, " +
                dbSchema.dbTable.Cols.FORECAST_TYPE + " TEXT" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + dbSchema.dbTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS" + dbSchemaMunicipioCode.dbTable.NAME);
        SharedPreferences sharedPreferences = MainActivity.getInstance().getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firstTime", true);
        editor.apply();
        onCreate(db);
    }

    // Method to add a new alarm to the database
    public void addAlarm(weatherAlarm alarm){
        ContentValues values = new ContentValues();
        values.put(dbSchema.dbTable.Cols.UUID, alarm.getID().toString());
        values.put(dbSchema.dbTable.Cols.LOCATION, alarm.getLocation());
        values.put(dbSchema.dbTable.Cols.TIME_OF_DAY, alarm.getTimeOfDay());
        values.put(dbSchema.dbTable.Cols.FORECAST_TYPE, alarm.getForecastType());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(dbSchema.dbTable.NAME, null, values);
        db.close();
    }

    // Method to delete an alarm from the database
    public void deleteAlarm(weatherAlarm alarm){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + dbSchema.dbTable.NAME + " WHERE " + dbSchema.dbTable.Cols.UUID + "=\"" + alarm.getID().toString() + "\"");
    }

    // Method to update a specific alarm of the database
    public void editAlarm(weatherAlarm alarm){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbSchema.dbTable.Cols.UUID, alarm.getID().toString());
        values.put(dbSchema.dbTable.Cols.LOCATION, alarm.getLocation());
        values.put(dbSchema.dbTable.Cols.TIME_OF_DAY, alarm.getTimeOfDay());
        values.put(dbSchema.dbTable.Cols.FORECAST_TYPE, alarm.getForecastType());
        db.update(dbSchema.dbTable.NAME, values, dbSchema.dbTable.Cols.UUID + " = ?", new String[] {alarm.getID().toString()});
    }

    // Method to get an alarm when in a specific position of the cursor
    public weatherAlarm getAlarm(Cursor cursor){
        String uuidString = cursor.getString(cursor.getColumnIndex(dbSchema.dbTable.Cols.UUID));
        String location = cursor.getString(cursor.getColumnIndex(dbSchema.dbTable.Cols.LOCATION));
        String timeOfDay = cursor.getString(cursor.getColumnIndex(dbSchema.dbTable.Cols.TIME_OF_DAY));
        String forecastType = cursor.getString(cursor.getColumnIndex(dbSchema.dbTable.Cols.FORECAST_TYPE));
        weatherAlarm alarm = new weatherAlarm();
        alarm.setID(UUID.fromString(uuidString));
        alarm.setLocation(location);
        alarm.setTimeOfDay(timeOfDay);
        alarm.setForecastType(forecastType);
        return alarm;
    }

    // Method to get all the alarms stored in the database
    public List<weatherAlarm> getAlarms() {
        ArrayList<weatherAlarm> alarmsList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                dbSchema.dbTable.NAME,
                null, null, null, null, null, null);
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                weatherAlarm alarm = getAlarm(cursor);
                alarmsList.add(alarm);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return alarmsList;
    }

    // Method to get a specific alarm from its UUID
    public weatherAlarm getSpecificAlarm(UUID ID){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(dbSchema.dbTable.NAME,
                null,
                dbSchema.dbTable.Cols.UUID + " = ? ", new String[] {ID.toString()},
                null, null, null);
        try{
            if(cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return getAlarm(cursor);
        }finally {
            cursor.close();
        }

    }

    public void createMunicipioCodeTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE " + dbSchemaMunicipioCode.dbTable.NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                dbSchemaMunicipioCode.dbTable.Cols.MUNICIPIO + " TEXT, " +
                dbSchemaMunicipioCode.dbTable.Cols.CODE + " TEXT" + ");");
    }

    public void addMunicioCodePair(String municipio, String code){
        ContentValues values = new ContentValues();
        values.put(dbSchemaMunicipioCode.dbTable.Cols.MUNICIPIO, municipio);
        values.put(dbSchemaMunicipioCode.dbTable.Cols.CODE, code);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(dbSchemaMunicipioCode.dbTable.NAME, null, values);
        db.close();
    }

    public String getCode(String municipio){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(dbSchemaMunicipioCode.dbTable.NAME,
                null,
                dbSchemaMunicipioCode.dbTable.Cols.MUNICIPIO + " = ? ", new String[] {municipio},
                null, null, null);
        try{
            if(cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            String code = cursor.getString(cursor.getColumnIndex(dbSchemaMunicipioCode.dbTable.Cols.CODE));
            return code;
        }finally {
            cursor.close();
        }
    }

}
