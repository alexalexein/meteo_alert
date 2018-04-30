package agarcia.padir;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import static agarcia.padir.R.id.alarmRecyclerView;

/**
 * Created by agarcia on 13/04/2018.
 */

public class dailyForecastFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private dailyAdapter recyclerViewAdapter;

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

    // newInstance constructor for creating fragment with arguments
    public static dailyForecastFragment newInstance(int page, String title) {
        dailyForecastFragment dailyFragment = new dailyForecastFragment();
        Bundle args = new Bundle();
        args.putInt("FORECAST_PAGE", page);
        args.putString("FORECAST_TITLE", title);
        dailyFragment.setArguments(args);
        return dailyFragment;
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
        View view = inflater.inflate(R.layout.daily_forecast, container, false);
        progressBar = view.findViewById(R.id.dailyIndeterminateBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        recyclerView = view.findViewById(R.id.dailyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setVisibility(RecyclerView.INVISIBLE);
        return view;
    }

    public void updateUI(String dailyForecastString){
        if (dailyForecastString.equals("loading")){
            recyclerView.setVisibility(RecyclerView.INVISIBLE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        else {
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            recyclerViewAdapter = new dailyAdapter(dailyForecastString);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    private class dailyHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private ImageView skyStateImageView;
        private TextView oddsTextView;
        private TextView maxTempTextView;
        private TextView minTempTextView;
        private TextView windDirTextView;
        private TextView windSpeedTextView;

        public dailyHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dailyDayTextView);
            skyStateImageView = itemView.findViewById(R.id.dailyForecastImageView);
            oddsTextView = itemView.findViewById(R.id.dailyPrecipitationTextView);
            maxTempTextView = itemView.findViewById(R.id.dailyMaxTempTextView);
            minTempTextView = itemView.findViewById(R.id.dailyMinTempTextView);
            windDirTextView = itemView.findViewById(R.id.dailyWindDirectionTextView);
            windSpeedTextView = itemView.findViewById(R.id.dailyWindSpeedTextView);
        }
    }

    private class dailyAdapter extends RecyclerView.Adapter<dailyForecastFragment.dailyHolder> {

        private String adapterForecastString;
        private String[] skyStateString;
        private String[] oddsString;
        private String[] maxTempString;
        private String[] minTempString;
        private String[] windDirString;
        private String[] windSpeedString;

        public dailyAdapter(String forecast) {
            adapterForecastString = forecast;
            String[] forecast_list = adapterForecastString.split(";");
            skyStateString = forecast_list[0].split(",");
            oddsString = forecast_list[1].split(",");
            maxTempString = forecast_list[2].split(",");
            minTempString = forecast_list[3].split(",");
            windDirString = forecast_list[4].split(",");
            windSpeedString = forecast_list[5].split(",");
        }

        @Override
        public dailyForecastFragment.dailyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.daily_forecast_item, parent, false);
            return new dailyForecastFragment.dailyHolder(view);
        }

        @Override
        public void onBindViewHolder(final dailyForecastFragment.dailyHolder holder, final int position) {
            holder.dateTextView.setText(getDateText(position));
            holder.skyStateImageView.setImageResource(getIcon(skyStateString[position]));
            holder.oddsTextView.setText(oddsString[position] + " %");
            holder.maxTempTextView.setText(maxTempString[position]);
            holder.minTempTextView.setText(minTempString[position]);
            holder.windDirTextView.setText(getWindDir(position));
            holder.windSpeedTextView.setText(windSpeedString[position] + " km/h");
        }

        @Override
        public int getItemCount() {
            return skyStateString.length;
        }

        private String getWindDir(int position){
            String result;
            switch (windDirString[position]){
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

        private String getDateText(int position){
            Calendar currentCalendarInstance = Calendar.getInstance();
            currentCalendarInstance.add(Calendar.DAY_OF_YEAR, position + 1);
            String result;
            switch (currentCalendarInstance.get(Calendar.DAY_OF_WEEK)){
                case 1:
                    result = getResources().getString(R.string.sundayName);
                    break;
                case 2:
                    result= getResources().getString(R.string.mondayName);
                    break;
                case 3:
                    result = getResources().getString(R.string.tuesdayName);
                    break;
                case 4:
                    result = getResources().getString(R.string.wednesdayName);
                    break;
                case 5:
                    result = getResources().getString(R.string.thursdayName);
                    break;
                case 6:
                    result = getResources().getString(R.string.fridayName);
                    break;
                case 7:
                    result = getResources().getString(R.string.saturdayName);
                    break;
                default:
                    result = "unknown";
                    break;
            }
            result = result + ", " +
                    String.valueOf(currentCalendarInstance.get(Calendar.DAY_OF_MONTH)) + "/" +
                    String.valueOf(currentCalendarInstance.get(Calendar.MONTH) + 1);
            return result;
        }
    }
}
