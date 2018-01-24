package agarcia.padir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 17/01/2018.
 */

public class loadingDialogFragment extends DialogFragment {

    ProgressBar bar;
    TextView counterTextView;
    BufferedReader reader;
    dbHelper database = new dbHelper(MainActivity.getInstance());
    int progress = 0;
    private Handler handler = new Handler();

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.loading_municipios, null);
        builder.setView(v);

        bar = v.findViewById(R.id.loadingProgressBar);
        counterTextView = v.findViewById(R.id.progressBarCounterTextView);


        int totalMunicipios = 8124;
        bar.setMax(totalMunicipios);


        database.createMunicipioCodeTable();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    reader = new BufferedReader(
                            new InputStreamReader(MainActivity.getInstance().getAssets().open("municipio_code_db.txt")));
                    String mLine;
                    Log.i("DEBUGGING", "Start of database traspass...");
                    while ((mLine = reader.readLine()) != null) {
                        String[] list = mLine.split(",");
                        String municipio = list[0];
                        String code = list[1];
                        database.addMunicioCodePair(municipio, code);
                        progress++;
                        handler.post(new Runnable() {
                            public void run() {
                                bar.setProgress(progress);
                                counterTextView.setText(progress + "/" + bar.getMax());
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(progress == bar.getMax()){
                            reader.close();
                            SharedPreferences sharedPreferences = MainActivity.getInstance()
                                    .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME,
                                            Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("firstTime", false);
                            editor.apply();
                            getDialog().dismiss();
                        }
                    }
                    Log.i("DEBUGGING", "End of database traspass...");
                }
                catch (IOException e) {
                    Log.i("DEBUGGING", e.toString());
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException e) {
                            Log.i("DEBUGGING", e.toString());
                        }
                    }
                }
            }
        }).start();

        return builder.create();
    }



}
