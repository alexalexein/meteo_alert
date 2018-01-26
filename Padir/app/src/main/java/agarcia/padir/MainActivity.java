package agarcia.padir;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class MainActivity extends AppCompatActivity implements OnAddOrEditRequested {

    Fragment MainFragment;
    public static final String SHARED_PREFERENCES_NAME = "mySharedPreferences";
    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
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
    }

    public static MainActivity getInstance(){
        return instance;
    }

    public void addFragmentRequested(String request, String[] parameters){
        Bundle args_add = new Bundle();
        args_add.putString(addFragment.METHOD_KEY, request);
        args_add.putString(addFragment.ARGUMENTS_ID_KEY, parameters[0]);
        args_add.putString(addFragment.ARGUMENTS_LOCATION_KEY, parameters[1]);
        args_add.putString(addFragment.ARGUMENTS_TIME_KEY, parameters[2]);
        args_add.putString(addFragment.ARGUMENTS_FORECAST_WINDOW_KEY, parameters[3]);
        addFragment AddFragment = new addFragment();
        AddFragment.setArguments(args_add);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, AddFragment, "addFragment")
                .addToBackStack(null)
                .commit();
    }

    public void mainFragmentRequested(String request, String[] parameters){
        Bundle args_main = new Bundle();
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        MainFragment = fm.findFragmentByTag("mainFragment");
        if(!request.equals("cancel")){
            args_main.putString(mainFragment.ARGUMENTS_METHOD_MAIN_KEY, request);
            args_main.putString(mainFragment.ARGUMENTS_ID_MAIN_KEY, parameters[0]);
            args_main.putString(mainFragment.ARGUMENTS_LOCATION_MAIN_KEY, parameters[1]);
            args_main.putString(mainFragment.ARGUMENTS_TIME_MAIN_KEY, parameters[2]);
            args_main.putString(mainFragment.ARGUMENTS_WINDOW_MAIN_KEY, parameters[3]);
        }
        else{
            if(MainFragment.getArguments() != null){
                MainFragment.getArguments().clear();
            }
            args_main.putString(mainFragment.ARGUMENTS_METHOD_MAIN_KEY, request);
        }
        MainFragment.setArguments(args_main);
        fm.popBackStack();
        fm.beginTransaction()
                .replace(R.id.fragment_container, MainFragment)
                .commit();
    }

    private void readMunicipioCodeDB(){
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction();
        loadingDialogFragment loadFragment = new loadingDialogFragment();
        loadFragment.setCancelable(false);
        loadFragment.show(fm, "loadFragment");
    }
}
