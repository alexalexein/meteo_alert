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
    private TextView debugTextView;
    private RecyclerView recyclerView;
    private dailyAdapter recyclerViewAdapter;

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
        debugTextView = view.findViewById(R.id.dailyDebugTextView);
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
            debugTextView.setText("");
            recyclerView.setVisibility(RecyclerView.INVISIBLE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        else {
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            // debugTextView.setText(dailyForecastString);
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
        }

        @Override
        public dailyForecastFragment.dailyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.daily_forecast_item, parent, false);
            String[] forecast_list = adapterForecastString.split(";");
            skyStateString = forecast_list[0].split(",");
            oddsString = forecast_list[1].split(",");
            maxTempString = forecast_list[2].split(",");
            minTempString = forecast_list[3].split(",");
            windDirString = forecast_list[4].split(",");
            windSpeedString = forecast_list[5].split(",");
            return new dailyForecastFragment.dailyHolder(view);
        }

        @Override
        public void onBindViewHolder(final dailyForecastFragment.dailyHolder holder, final int position) {
            holder.dateTextView.setText(getDateText(position));
            holder.skyStateImageView.setImageResource(getIcon(Integer.valueOf(skyStateString[position])));
            holder.oddsTextView.setText(oddsString[position] + " %");
            holder.maxTempTextView.setText(maxTempString[position]);
            holder.minTempTextView.setText(minTempString[position]);
            holder.windDirTextView.setText(windDirString[position]);
            holder.windSpeedTextView.setText(windSpeedString[position] + " km/h");
        }

        @Override
        public int getItemCount() {
            return 6;
        }

        private int getIcon(int skyStateCode){
            return R.mipmap.clear_icon;
        }

        private String getDateText(int position){
            return "Monday 12/04";
        }
    }
}
