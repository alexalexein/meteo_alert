package agarcia.padir;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import agarcia.padir.database.dbHelper;

import static agarcia.padir.AlarmReceiver.AEMET_API_KEY;
import static agarcia.padir.MainActivity.SHARED_PREFERENCES_NAME;

/**
 * Created by agarcia on 06/04/2018.
 */

public class forecastFragment extends Fragment {

    private SimpleCursorAdapter mAdapter;
    private String[] municipios_list;
    private String selectedItem = "";

    private TabLayout tabLayout;
    private TabLayout.Tab hourlyTab;
    private TabLayout.Tab dailyTab;
    private TextView locationTextView;
    private FragmentPagerAdapter adapterViewPager;

    dbHelper database;
    String selectedMunicipio;

    // Connection
    private String mainUrlResultDaily;
    private String finalUrlResultDaily;
    private String finalUrlDaily;
    private String mainUrlResultHourly;
    private String finalUrlResultHoury;
    private String finalUrlHourly;
    private String HORARIA = "horaria";
    private String DIARIA = "diaria";
    public String dailyForecastString;
    public String hourlyForecastString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        municipios_list = MainActivity.getInstance().getResources()
                .getStringArray(R.array.municipios);
        database = new dbHelper(getContext());
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        selectedMunicipio = sharedPreferences.getString("lastMunicipio", "Barcelona");
        mainUrlFetcher hourlyForecastFetcher = new mainUrlFetcher(HORARIA, getContext());
        mainUrlFetcher dailyForecastFetcher = new mainUrlFetcher(DIARIA, getContext());
        hourlyForecastFetcher.execute();
        dailyForecastFetcher.execute();
        dailyForecastString = "loading";
        hourlyForecastString = "loading";
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.forecast_fragment, container, false);
        locationTextView = v.findViewById(R.id.locationForecastTextView);
        locationTextView.setText(selectedMunicipio);
        tabLayout = (TabLayout) v.findViewById(R.id.forecastTabLayout);
        hourlyTab = tabLayout.newTab();
        hourlyTab.setText(R.string.hourlyText);
        dailyTab = tabLayout.newTab();
        dailyTab.setText(R.string.dailyText);
        tabLayout.addTab(hourlyTab);
        tabLayout.addTab(dailyTab);
        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), getContext());
        viewPager.setAdapter(adapterViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Initialization of options menu
        inflater.inflate(R.menu.menu_forecast_window, menu);
        final MenuItem searchViewItem = menu.findItem(R.id.search_item);
        final SearchView searchViewActionBar = (SearchView) searchViewItem.getActionView();

        // Set cursor adapter for suggestions
        final String[] from = new String[] {"municipios"};
        final int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        searchViewActionBar.setSuggestionsAdapter(mAdapter);


        // Set hint text
        searchViewActionBar.setQueryHint(getResources().getString(R.string.locationAutoCompleTextViewHint));

        // Change color of search icon
        ImageView searchButton = (ImageView) searchViewActionBar.findViewById (android.support.v7.appcompat.R.id.search_button);
        searchButton.setColorFilter (Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        // Change color of both hint and text of search field
        EditText searchEditText = (EditText) searchViewActionBar.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(Color.parseColor("#FFFFFF"));
        searchEditText.setTextColor(Color.parseColor("#FFFFFF"));

        // Change color of close icon of search
        ImageView searchCloseButton = (ImageView) searchViewActionBar.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseButton.setColorFilter(Color.parseColor("#FFFFFF"));

        // Set maximum width of search EditText


        // Onquerytextlistener
        searchViewActionBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchViewActionBar.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                populateAdapter(newText);
                return false;
            }
        });

        searchViewActionBar.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor searchCursor = mAdapter.getCursor();
                if(searchCursor.moveToPosition(position)){
                    selectedItem = searchCursor.getString(1);
                }
                searchViewActionBar.onActionViewCollapsed();
                locationTextView.setText(selectedItem);
                if (!selectedItem.equals(selectedMunicipio)){
                    selectedMunicipio = selectedItem;
                    ((hourlyForecastFragment) adapterViewPager.getItem(0)).updateUI("loading");
                    ((dailyForecastFragment) adapterViewPager.getItem(1)).updateUI("loading");
                    mainUrlFetcher hourlyForecastFetcher = new mainUrlFetcher(HORARIA, getContext());
                    mainUrlFetcher dailyForecastFetcher = new mainUrlFetcher(DIARIA, getContext());
                    hourlyForecastFetcher.execute();
                    dailyForecastFetcher.execute();
                    SharedPreferences sharedPreferences = MainActivity.getInstance()
                            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME,
                                    Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lastMunicipio", selectedItem);
                    editor.apply();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void populateAdapter(String query){
        final MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, "municipios"});
        for (int i=0; i<municipios_list.length; i++){
            if (municipios_list[i].toLowerCase().startsWith(query.toLowerCase())){
                c.addRow(new Object[] {i, municipios_list[i]});
            }
            mAdapter.changeCursor(c);
        }
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;
        private static Context c;
        private FragmentManager mFragmentManager;

        public MyPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            c = context;
            mFragmentManager = fragmentManager;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            String name = makeFragmentName(R.id.viewPager, position);
            Fragment f = mFragmentManager.findFragmentByTag(name);
            if (f == null){
                switch (position) {
                    case 0: // Hourly forecast
                        return hourlyForecastFragment.newInstance(0, c.getResources().getString(R.string.hourlyText));
                    case 1: // Daily forecast
                        return dailyForecastFragment.newInstance(1, c.getResources().getString(R.string.dailyText));
                    default:
                        return null;
                }
            }
            return f;
        }

        private static String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }
    }

    // Method to get the data to get the final URL where to find the forecast
    private class mainUrlFetcher extends AsyncTask<Void,Void,Void> {

        Context c;
        String requestType;

        public mainUrlFetcher(String requestType,Context c){
            this.c = c;
            this.requestType = requestType;
        }

        @Override
        protected Void doInBackground(Void... params){

            try {
                if (requestType.equals(HORARIA)){
                    mainUrlResultHourly = new urlFetcher().
                            getUrlString("https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/"
                                    + requestType + "/" +
                                    database.getCode(selectedMunicipio) + "/?api_key=" + AEMET_API_KEY);
                    finalUrlHourly = finalUrlGetter(mainUrlResultHourly);
                }
                else {
                    mainUrlResultDaily = new urlFetcher().
                            getUrlString("https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/"
                                    + requestType + "/" +
                                    database.getCode(selectedMunicipio) + "/?api_key=" + AEMET_API_KEY);
                    finalUrlDaily = finalUrlGetter(mainUrlResultDaily);
                }
            }

            catch (IOException ioe){
                if (requestType.equals(HORARIA)){
                    finalUrlHourly = "nc";
                }
                else {
                    finalUrlDaily = "nc";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (requestType.equals(HORARIA)){
                if (finalUrlHourly.startsWith("https")){
                    new finalUrlFetcher(requestType, c).execute();
                }
                else {
                    // Populate fragment with no connection layout
                }
            }
            else {
                if (finalUrlDaily.startsWith("https")){
                    new finalUrlFetcher(requestType, c).execute();
                }
                else {
                    // Populate fragment with no connection layout
                }
            }
        }
    }

    //it reads and analyses weather forecast (isTomorrowRaining) to output results in textView
    private class finalUrlFetcher extends AsyncTask<Void, Void, Void>{

        Context c;
        String requestType;

        public finalUrlFetcher(String requestType, Context c) {
            this.c = c;
            this.requestType = requestType;
        }

        @Override
        protected Void doInBackground(Void... params){
            try {
                if (requestType.equals(HORARIA)){
                    finalUrlResultHoury = new urlFetcher().getUrlString(finalUrlHourly);
                }
                else {
                    finalUrlResultDaily = new urlFetcher().getUrlString(finalUrlDaily);
                }
            }
            catch (IOException ioe){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            if (requestType.equals(HORARIA)){
                populateForecastString(finalUrlResultHoury, requestType);
            }
            else {
                populateForecastString(finalUrlResultDaily, requestType);
            }
        }
    }

    //function to extract the final url to get weather forecast from first json response
    private String finalUrlGetter(String response){
        String[] responseList = response.split("\"");
        String finalUrl = responseList[responseList.length - 6];
        return finalUrl;
    }

    private void populateForecastString(String urlResult, String dailyOrHourly){
        boolean firstDayToday = isFirstDayToday(urlResult);
        JSONArray forecastJSONArray = getForecastJSONArray(urlResult);
        if (dailyOrHourly.equals(DIARIA)){
            int init;
            if (firstDayToday){
                init = 1;
            }
            else {
                init = 2;
            }
            dailyForecastString = getDailySkyStateString(forecastJSONArray, init) + ";" +
                    getDailyRainProbString(forecastJSONArray, init) + ";" +
                    getDailyMaxTempString(forecastJSONArray, init) + ";" +
                    getDailyMinTempString(forecastJSONArray, init) + ";" +
                    getDailyWindDirString(forecastJSONArray, init) + ";" +
                    getDailyWindSpeedString(forecastJSONArray, init);
            ((dailyForecastFragment) adapterViewPager.getItem(1)).updateUI(dailyForecastString);
        }
        else {
            int firstDay;
            if (firstDayToday){
                firstDay = 0;
            }
            else{
                firstDay = 1;
            }
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String currentHourString;
            if (currentHour == 23){
                currentHourString = "00";
                firstDay++;
            }
            else{
                if (currentHour < 9){
                    currentHourString = "0" + String.valueOf(currentHour + 1);
                }
                else{
                    currentHourString = String.valueOf(currentHour + 1);
                }
            }
            int firstIndex = getHourlyFirstIndex(forecastJSONArray, firstDay, currentHourString);
            Log.i("debug", "Hourly first index: " + String.valueOf(firstIndex));
            Log.i("debug", "Hourly first day: " + String.valueOf(firstDay));
            Log.i("debug", "Hourly first time string: " + currentHourString);
            Log.i("debug", "Hourly time string: " + getHourlyTimeString(forecastJSONArray, firstDay, firstIndex));
            Log.i("debug", "Hourly sky state string: " + getHourlySkyStateString(forecastJSONArray, firstDay, firstIndex));
            Log.i("debug", "Hourly rain string: " + getHourlyRainString(forecastJSONArray, firstDay, firstIndex));
            hourlyForecastString = urlResult;
            /*hourlyForecastString = getHourlyTimeString(forecastJSONArray, firstDay, currentHourString) + ";" +
                    getHourlySkyStateString(forecastJSONArray, firstDay, currentHourString) + ";" +
                    getHourlyRainString(forecastJSONArray, firstDay, currentHourString) + ";" +
                    getHourlyTempString(forecastJSONArray, firstDay, currentHourString) + ";" +
                    getHourlyWindDirString(forecastJSONArray, firstDay, currentHourString) + ";" +
                    getHourlyWindSpeedString(forecastJSONArray, firstDay, currentHourString);*/
            ((hourlyForecastFragment) adapterViewPager.getItem(0)).updateUI(hourlyForecastString);
        }
    }

    // Method to check if the data acquired from weather provider is updated

    private boolean isFirstDayToday(String urlResult){
        try {
            JSONArray reader = new JSONArray(urlResult);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            String firstDay = forecastArray.getJSONObject(0).getString("fecha");
            int dayOfMonth = Integer.valueOf(firstDay.split("-")[2]);
            int currentDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth == currentDayOfMonth){
                return true;
            }
            else {
                return false;
            }
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method isFirstDayToday of forecastFragment: " + e.toString());
        }
        return true;
    }

    // Method to get only the important data from the forecast json data
    private JSONArray getForecastJSONArray(String urlResult){
        try {
            JSONArray reader = new JSONArray(urlResult);
            JSONObject totalData = reader.getJSONObject(0);
            JSONObject dia = totalData.getJSONObject("prediccion");
            JSONArray forecastArray = dia.getJSONArray("dia");
            return forecastArray;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailySkyStateString of forecastFragment: " + e.toString());
        }
        return new JSONArray();
    }

    // Daily forecast methods

    private String getDailySkyStateString(JSONArray forecastJSONArray, int init){
        try {
            JSONObject dayForecast;
            JSONArray estadoCielo;
            String skyState = "";
            for (int i=init; i<forecastJSONArray.length(); i++) {
                dayForecast = forecastJSONArray.getJSONObject(i);
                estadoCielo = dayForecast.getJSONArray("estadoCielo");
                if (i != init){
                    skyState = skyState + ",";
                }
                skyState = skyState + estadoCielo.getJSONObject(0).getString("value");
            }
            return skyState;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailySkyStateString of forecastFragment: " + e.toString());
        }
        return new String();
    }

    private String getDailyRainProbString(JSONArray forecastJSONArray, int init){
        try {
            JSONObject dayForecast;
            JSONArray probPrecipitacion;
            String precOdds = "";
            for (int i=init; i<forecastJSONArray.length(); i++) {
                dayForecast = forecastJSONArray.getJSONObject(i);
                probPrecipitacion = dayForecast.getJSONArray("probPrecipitacion");
                if (i != init){
                    precOdds = precOdds + ",";
                }
                precOdds = precOdds + probPrecipitacion.getJSONObject(0).getString("value");
            }
            return precOdds;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailyRainProbString of forecastFragment: " + e.toString());
        }
        return new String();
    }

    private String getDailyMaxTempString(JSONArray forecastJSONArray, int init){
        try {
            JSONObject dayForecast;
            JSONObject temperatura;
            String maxTemp = "";
            for (int i=init; i<forecastJSONArray.length(); i++) {
                dayForecast = forecastJSONArray.getJSONObject(i);
                temperatura = dayForecast.getJSONObject("temperatura");
                if (i != init){
                    maxTemp = maxTemp + ",";
                }
                maxTemp = maxTemp + temperatura.getString("maxima");
            }
            return maxTemp;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailyMaxTempString of forecastFragment: " + e.toString());
        }
        return new String();
    }

    private String getDailyMinTempString(JSONArray forecastJSONArray, int init){
        try {
            JSONObject dayForecast;
            JSONObject temperatura;
            String minTemp = "";
            for (int i=init; i<forecastJSONArray.length(); i++) {
                dayForecast = forecastJSONArray.getJSONObject(i);
                temperatura = dayForecast.getJSONObject("temperatura");
                if (i != init){
                    minTemp = minTemp + ",";
                }
                minTemp = minTemp + temperatura.getString("minima");
            }
            return minTemp;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailyMinTempString of forecastFragment: " + e.toString());
        }
        return new String();
    }

    private String getDailyWindDirString(JSONArray forecastJSONArray, int init){
        try {
            JSONObject dayForecast;
            JSONArray viento;
            String windDir = "";
            for (int i=init; i<forecastJSONArray.length(); i++) {
                dayForecast = forecastJSONArray.getJSONObject(i);
                viento = dayForecast.getJSONArray("viento");
                if (i != init){
                    windDir = windDir + ",";
                }
                windDir = windDir + viento.getJSONObject(0).getString("direccion");
            }
            return windDir;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailyWindDirString of forecastFragment: " + e.toString());
        }
        return new String();
    }

    private String getDailyWindSpeedString(JSONArray forecastJSONArray, int init){
        try {
            JSONObject dayForecast;
            JSONArray viento;
            String windSpeed = "";
            for (int i=init; i<forecastJSONArray.length(); i++) {
                dayForecast = forecastJSONArray.getJSONObject(i);
                viento = dayForecast.getJSONArray("viento");
                if (i != init){
                    windSpeed = windSpeed + ",";
                }
                windSpeed = windSpeed + viento.getJSONObject(0).getString("velocidad");
            }
            return windSpeed;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getDailyWindSpeedString of forecastFragment: " + e.toString());
        }
        return new String();
    }

    // Hourly forecast methods

    private int getHourlyFirstIndex(JSONArray forecastJSONArray, int firstDay, String firstHour){
        try {
            JSONObject dayForecast = forecastJSONArray.getJSONObject(firstDay);
            JSONArray skyStateArray = dayForecast.getJSONArray("estadoCielo");
            for (int i = 0; i < skyStateArray.length(); i++){
                if (skyStateArray.getJSONObject(i).getString("periodo").equals(firstHour)){
                    return i;
                }
            }
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getHourlyFirstIndex of forecastFragment: " + e.toString());
        }
        return 0;
    }
    private String getHourlyTimeString(JSONArray forecastJSONArray, int firstDay, int firstIndex){
        String result = "";
        try {
            JSONObject dayForecast = forecastJSONArray.getJSONObject(firstDay);
            JSONArray skyStateArray = dayForecast.getJSONArray("estadoCielo");
            // Only future hours of first day
            for (int i = firstIndex; i < skyStateArray.length(); i++){
                result = result + skyStateArray.getJSONObject(i).getString("periodo") + ",";
            }
            // Other days
            for (int i = (firstDay + 1); i < forecastJSONArray.length(); i++){
                dayForecast = forecastJSONArray.getJSONObject(i);
                skyStateArray = dayForecast.getJSONArray("estadoCielo");
                for (int j = 0; j < skyStateArray.length(); j++){
                    result = result + skyStateArray.getJSONObject(j).getString("periodo");
                    if (i != (forecastJSONArray.length() - 1) || j != (skyStateArray.length() - 1)){
                        result = result + ",";
                    }
                }
            }
            return result;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getHourlyTimeString of forecastFragment: " + e.toString());
        }
        return "";
    }

    private String getHourlySkyStateString(JSONArray forecastJSONArray, int firstDay, int firstIndex){
        String result = "";
        try {
            JSONObject dayForecast = forecastJSONArray.getJSONObject(firstDay);
            JSONArray skyStateArray = dayForecast.getJSONArray("estadoCielo");
            // Only future sky states of first day
            for (int i = firstIndex; i < skyStateArray.length(); i++){
                result = result + skyStateArray.getJSONObject(i).getString("value") + ",";
            }
            // Other days
            for (int i = (firstDay + 1); i < forecastJSONArray.length(); i++){
                dayForecast = forecastJSONArray.getJSONObject(i);
                skyStateArray = dayForecast.getJSONArray("estadoCielo");
                for (int j = 0; j < skyStateArray.length(); j++){
                    result = result + skyStateArray.getJSONObject(j).getString("value");
                    if (i != (forecastJSONArray.length() - 1) || j != (skyStateArray.length() - 1)){
                        result = result + ",";
                    }
                }
            }
            return result;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getHourlySkyStateString of forecastFragment: " + e.toString());
        }
        return "";
    }

    private String getHourlyRainString(JSONArray forecastJSONArray, int firstDay, int firstIndex){
        String result = "";
        try {
            JSONObject dayForecast = forecastJSONArray.getJSONObject(firstDay);
            JSONArray rainArray = dayForecast.getJSONArray("precipitacion");
            // Only future sky states of first day
            for (int i = firstIndex; i < rainArray.length(); i++){
                result = result + rainArray.getJSONObject(i).getString("value") + ",";
            }
            // Other days
            for (int i = (firstDay + 1); i < forecastJSONArray.length(); i++){
                dayForecast = forecastJSONArray.getJSONObject(i);
                rainArray = dayForecast.getJSONArray("precipitacion");
                for (int j = 0; j < rainArray.length(); j++){
                    result = result + rainArray.getJSONObject(j).getString("value");
                    if (i != (forecastJSONArray.length() - 1) || j != (rainArray.length() - 1)){
                        result = result + ",";
                    }
                }
            }
            return result;
        }
        catch (JSONException e){
            Log.i("debug", "Exception caught in method getHourlySkyStateString of forecastFragment: " + e.toString());
        }
        return "";
    }

    private String getHourlyTempString(JSONArray forecastJSONArray, int firstDay, String firstHour){
        return "";
    }

    private String getHourlyWindDirString(JSONArray forecastJSONArray, int firstDay, String firstHour){
        return "";
    }

    private String getHourlyWindSpeedString(JSONArray forecastJSONArray, int firstDay, String firstHour){
        return "";
    }
}
