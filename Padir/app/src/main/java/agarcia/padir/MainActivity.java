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

    private Fragment MainFragment;
    private Fragment AddFragment;
    private Fragment ForecastFragment;
    public static final String SHARED_PREFERENCES_NAME = "mySharedPreferences";
    private static MainActivity instance;
    private DrawerLayout mDrawerLayout;
    ActionBar actionbar;
    private boolean isDrawerOpen = false;
    private FragmentManager fm;
    private int currentFragment;
    private final int FORECAST_FRAGMENT = 0;
    private final int MAIN_FRAGMENT = 1;
    private final int ADD_FRAGMENT = 2;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.nav_drawer_menu_icon);
        fm = getSupportFragmentManager();
        ForecastFragment = new forecastFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container, ForecastFragment, "forecastFragment")
                .commit();
        currentFragment = FORECAST_FRAGMENT;
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        instance = this;
        boolean firstTime = sharedPreferences.getBoolean("firstTime", true);
        if (firstTime){
            readMunicipioCodeDB();
        }

        // Code for navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
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

    @Override
    public void onBackPressed() {
        if (currentFragment == MAIN_FRAGMENT){
            forecastFragmentRequested();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
        else {
            super.onBackPressed();
        }
    }

    public static MainActivity getInstance(){
        return instance;
    }

    public void addFragmentRequested(int ID){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Bundle args_add = new Bundle();
        args_add.putInt(addFragment.ALARM_ID, ID);
        AddFragment = new addFragment();
        AddFragment.setArguments(args_add);
        fm.beginTransaction().replace(R.id.fragment_container, AddFragment, "addFragment")
                .addToBackStack(null)
                .commit();
        actionbar.setDisplayHomeAsUpEnabled(false);
        currentFragment = ADD_FRAGMENT;
    }

    public void mainFragmentRequested(){
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.nav_drawer_menu_icon);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        MainFragment = new mainFragment();
        fm.beginTransaction().add(R.id.fragment_container, MainFragment, "mainFragment").commit();
        currentFragment = MAIN_FRAGMENT;
    }

    public void forecastFragmentRequested(){
        ForecastFragment = fm.findFragmentByTag("forecastFragment");
        fm.popBackStack();
        fm.beginTransaction().replace(R.id.fragment_container, ForecastFragment).commit();
        currentFragment = FORECAST_FRAGMENT;
    }

    private void readMunicipioCodeDB(){
        fm.beginTransaction();
        loadingDialogFragment loadFragment = new loadingDialogFragment();
        loadFragment.setCancelable(false);
        loadFragment.show(fm, "loadFragment");
    }
}
