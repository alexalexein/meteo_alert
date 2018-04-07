package agarcia.padir;

import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
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

/**
 * Created by agarcia on 06/04/2018.
 */

public class forecastFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.forecast_fragment, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Initialization of options menu
        inflater.inflate(R.menu.menu_forecast_window, menu);
        MenuItem searchViewItem = menu.findItem(R.id.search_item);
        final SearchView searchViewActionBar = (SearchView) searchViewItem.getActionView();

        // Set suggestions
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchViewActionBar.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.municipios, android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setAdapter(locationAdapter);

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
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
