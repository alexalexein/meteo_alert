package agarcia.padir;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import agarcia.padir.database.dbHelper;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        municipios_list = MainActivity.getInstance().getResources()
                .getStringArray(R.array.municipios);
        database = new dbHelper(getContext());
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        selectedMunicipio = sharedPreferences.getString("lastMunicipio", "Barcelona");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.forecast_fragment, container, false);
        locationTextView = v.findViewById(R.id.locationForecastTextView);
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

        public MyPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            c = context;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Hourly forecast
                    return hourlyForecastFragment.newInstance(0, c.getResources().getString(R.string.hourlyText));
                case 1: // Daily forecast
                    return dailyForecastFragment.newInstance(1, c.getResources().getString(R.string.dailyText));
                default:
                    return null;
            }
        }
    }
}
