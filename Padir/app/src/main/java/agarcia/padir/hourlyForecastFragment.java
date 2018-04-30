package agarcia.padir;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by agarcia on 13/04/2018.
 */

public class hourlyForecastFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private hourlyForecastFragment.hourlyAdapter recyclerViewAdapter;

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

    // Days of week
    private final int SUNDAY = 1;
    private final int MONDAY = 2;
    private final int TUESDAY = 3;
    private final int WEDNESDAY = 4;
    private final int THURSDAY = 5;
    private final int FRIDAY = 6;
    private final int SATURDAY = 7;

    // newInstance constructor for creating fragment with arguments
    public static hourlyForecastFragment newInstance(int page, String title) {
        hourlyForecastFragment hourlyFragment = new hourlyForecastFragment();
        Bundle args = new Bundle();
        args.putInt("FORECAST_PAGE", page);
        args.putString("FORECAST_TITLE", title);
        hourlyFragment.setArguments(args);
        return hourlyFragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("FORECAST_PAGE", 0);
        title = getArguments().getString("FORECAST_TITLE");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hourly_forecast, container, false);
        progressBar = view.findViewById(R.id.hourlyIndeterminateBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        recyclerView = view.findViewById(R.id.hourlyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setVisibility(RecyclerView.INVISIBLE);
        return view;
    }

    public void updateUI(String hourlyForecastString){
        if (hourlyForecastString.equals("loading")){
            recyclerView.setVisibility(RecyclerView.INVISIBLE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        else {
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            recyclerViewAdapter = new hourlyForecastFragment.hourlyAdapter(hourlyForecastString);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    private class hourlyHolder extends RecyclerView.ViewHolder {
        private TextView dayTextView;
        private TextView hourTextView;
        private ImageView skyStateImageView;
        private TextView rainTextView;
        private TextView tempTextView;
        private TextView windDirTextView;
        private TextView windSpeedTextView;

        public hourlyHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.hourlyDayTextView);
            hourTextView = itemView.findViewById(R.id.hourlyTimeTextView);
            skyStateImageView = itemView.findViewById(R.id.hourlyForecastImageView);
            rainTextView = itemView.findViewById(R.id.hourlyPrecipitationTextView);
            tempTextView = itemView.findViewById(R.id.hourlyTempTextView);
            windDirTextView = itemView.findViewById(R.id.hourlyWindDirectionTextView);
            windSpeedTextView = itemView.findViewById(R.id.hourlyWindSpeedTextView);
        }
    }

    private class hourlyAdapter extends RecyclerView.Adapter<hourlyForecastFragment.hourlyHolder> {

        private String adapterForecastString;
        private String[] daysStringList;
        private String[] hoursStringList;
        private String[] skyStateStringList;
        private String[] rainStringList;
        private String[] tempStringList;
        private String[] windDirStringList;
        private String[] windSpeedStringList;

        public hourlyAdapter(String forecast) {
            adapterForecastString = forecast;
            String[] forecast_list = adapterForecastString.split(";");
            daysStringList = forecast_list[0].split(",");
            hoursStringList = forecast_list[1].split(",");
            skyStateStringList = forecast_list[2].split(",");
            rainStringList = forecast_list[3].split(",");
            tempStringList = forecast_list[4].split(",");
            windDirStringList = forecast_list[5].split(",");
            windSpeedStringList = forecast_list[6].split(",");
            Log.i("debug", "days: " + String.valueOf(daysStringList.length));
            Log.i("debug", "hours: " + String.valueOf(hoursStringList.length));
            Log.i("debug", "skystate: " + String.valueOf(skyStateStringList.length));
            Log.i("debug", "rain: " + String.valueOf(rainStringList.length));
            Log.i("debug", "temp: " + String.valueOf(tempStringList.length));
            Log.i("debug", "dir: " + String.valueOf(windDirStringList.length));
            Log.i("debug", "speed: " + String.valueOf(windSpeedStringList.length));
        }

        @Override
        public hourlyForecastFragment.hourlyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.hourly_forecast_item, parent, false);
            return new hourlyForecastFragment.hourlyHolder(view);
        }

        @Override
        public void onBindViewHolder(final hourlyForecastFragment.hourlyHolder holder, final int position) {
            holder.dayTextView.setText(getDayText(position));
            holder.hourTextView.setText(hoursStringList[position] + ":00");
            holder.skyStateImageView.setImageResource(getIcon(skyStateStringList[position]));
            holder.rainTextView.setText(rainStringList[position] + " mm");
            holder.tempTextView.setText(tempStringList[position] + " ºC");
            holder.windDirTextView.setText(getWindDir(position));
            holder.windSpeedTextView.setText(windSpeedStringList[position] + " km/h");
        }

        @Override
        public int getItemCount() {
            return daysStringList.length;
        }

        private String getDayText(int position){
            String result = "";
            switch (Integer.valueOf(daysStringList[position])){
                case SUNDAY:
                    result = getResources().getString(R.string.sundayName);
                    break;
                case MONDAY:
                    result = getResources().getString(R.string.mondayName);
                    break;
                case TUESDAY:
                    result = getResources().getString(R.string.tuesdayName);
                    break;
                case WEDNESDAY:
                    result = getResources().getString(R.string.wednesdayName);
                    break;
                case THURSDAY:
                    result = getResources().getString(R.string.thursdayName);
                    break;
                case FRIDAY:
                    result = getResources().getString(R.string.fridayName);
                    break;
                case SATURDAY:
                    result = getResources().getString(R.string.saturdayName);
                    break;
            }
            return result;
        }

        private String getWindDir(int position){
            String result;
            switch (windDirStringList[position]){
                case "S":
                    result = getResources().getString(R.string.southAbbr);
                    break;
                case "E":
                    result = getResources().getString(R.string.eastAbbr);
                    break;
                case "O":
                    result = getResources().getString(R.string.westAbbr);
                    break;
                case "N":
                    result = getResources().getString(R.string.northAbbr);
                    break;
                case "SE":
                    result = getResources().getString(R.string.southEastAbbr);
                    break;
                case "SO":
                    result = getResources().getString(R.string.southWestAbbr);
                    break;
                case "NE":
                    result = getResources().getString(R.string.northEastAbbr);
                    break;
                case "NO":
                    result = getResources().getString(R.string.northWestAbbr);
                    break;
                case "C":
                    result = "C";
                    break;
                default:
                    result = "unknown";
            }
            return result;
        }

        private int getIcon(String skyStateCode){
            String skyStateCode_0;
            String skyStateCode_1;
            int icon;
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
                            icon = R.mipmap.clear_icon;
                            break;
                        case SLIGTHLY_CLOUDY:
                            icon = R.mipmap.slightly_partly_cloudy_icon;
                            break;
                        case PARTLY_CLOUDY:
                            icon = R.mipmap.slightly_partly_cloudy_icon;
                            break;
                        case MOSTLY_CLOUDY:
                            icon = R.mipmap.mostly_overcast_cloudy_icon;
                            break;
                        case CLOUDY:
                            icon = R.mipmap.mostly_overcast_cloudy_icon;
                            break;
                        case OVERCAST_SKY:
                            icon = R.mipmap.mostly_overcast_cloudy_icon;
                            break;
                        case HIGH_CLOUDS:
                            icon = R.mipmap.high_clouds_icon;
                            break;
                        default:
                            icon = R.mipmap.weather_icon;
                            break;
                    }
                    break;
                case RAIN:
                    icon = R.mipmap.rain_icon;
                    break;
                case SNOW:
                    icon = R.mipmap.snow_icon;
                    break;
                case SCARCE_RAIN:
                    icon = R.mipmap.scarce_rain_icon;
                    break;
                case SCARCE_SNOW:
                    icon = R.mipmap.scarce_snow_icon;
                    break;
                default:
                    icon = R.mipmap.weather_icon;
                    break;

            }
            return icon;
        }
    }
}
