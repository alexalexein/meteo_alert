package agarcia.padir;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by agarcia on 13/04/2018.
 */

public class dailyForecastFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;

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
        return view;
    }
}
