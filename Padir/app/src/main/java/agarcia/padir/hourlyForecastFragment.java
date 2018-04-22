package agarcia.padir;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by agarcia on 13/04/2018.
 */

public class hourlyForecastFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private TextView debugTextView;
    private ProgressBar progressBar;

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
        debugTextView = view.findViewById(R.id.hourlyDebugTextView);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        return view;
    }

    public void updateUI(String hourlyForecastString){
        if (hourlyForecastString.equals("loading")){
            debugTextView.setText("");
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        else {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            debugTextView.setText(hourlyForecastString);
        }
    }
}
