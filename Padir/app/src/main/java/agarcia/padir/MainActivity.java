package agarcia.padir;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


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

    public void addFragmentRequested(int ID){
        Bundle args_add = new Bundle();
        args_add.putInt(addFragment.ALARM_ID, ID);
        addFragment AddFragment = new addFragment();
        AddFragment.setArguments(args_add);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, AddFragment, "addFragment")
                .addToBackStack(null)
                .commit();
    }

    public void mainFragmentRequested(){
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        MainFragment = fm.findFragmentByTag("mainFragment");
        if(MainFragment.getArguments() != null){
            MainFragment.getArguments().clear();
        }
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
