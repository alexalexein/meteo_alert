package agarcia.padir;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends AppCompatActivity implements OnAddOrEditRequested {

    Fragment MainFragment;
    public static final String SHARED_PREFERENCES_NAME = "mySharedPreferences";
    private static MainActivity instance;
    private DrawerLayout mDrawerLayout;
    ActionBar actionbar;
    private boolean isDrawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.nav_drawer_menu_icon);
        FragmentManager fm = getSupportFragmentManager();
        MainFragment = fm.findFragmentByTag("mainFragment");
        if(MainFragment == null){
            MainFragment = new mainFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, MainFragment, "mainFragment")
                    .commit();
        }
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        instance = this;
        boolean firstTime = sharedPreferences.getBoolean("firstTime", true);
        if (firstTime){
            readMunicipioCodeDB();
        }

        // Code for navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // Set item as selected to persist highlight
                        item.setChecked(true);

                        // Close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        //Add code here to update the UI based on the item selected
                        int ID = item.getItemId();
                        if (ID == R.id.forecast_display_item){
                            forecastFragmentRequested();
                        }
                        else if (ID == R.id.alarms_display_item){
                            mainFragmentRequested();
                        }

                        return true;
                    }
                });
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                isDrawerOpen = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawerOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (isDrawerOpen){
                    mDrawerLayout.closeDrawers();
                }
                else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static MainActivity getInstance(){
        return instance;
    }

    public void addFragmentRequested(int ID){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Bundle args_add = new Bundle();
        args_add.putInt(addFragment.ALARM_ID, ID);
        addFragment AddFragment = new addFragment();
        AddFragment.setArguments(args_add);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, AddFragment, "addFragment")
                .addToBackStack(null)
                .commit();
        actionbar.setDisplayHomeAsUpEnabled(false);
    }

    public void mainFragmentRequested(){
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.nav_drawer_menu_icon);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        FragmentManager fm = getSupportFragmentManager();
        MainFragment = new mainFragment();
        fm.popBackStack();
        fm.beginTransaction().replace(R.id.fragment_container, MainFragment).commit();
    }

    public void forecastFragmentRequested(){
        FragmentManager fm = getSupportFragmentManager();
        forecastFragment ForecastFragment = new forecastFragment();
        fm.popBackStack();
        fm.beginTransaction().replace(R.id.fragment_container, ForecastFragment).commit();
    }

    private void readMunicipioCodeDB(){
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction();
        loadingDialogFragment loadFragment = new loadingDialogFragment();
        loadFragment.setCancelable(false);
        loadFragment.show(fm, "loadFragment");
    }
}
