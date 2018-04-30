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
import android.support.annotation.NonNull;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import agarcia.padir.database.dbHelper;

/**
 * Created by agarcia on 17/01/2018.
 */

public class loadingDialogFragment extends DialogFragment {

    private static String DB_NAME = "alarmDB.db";
    private static String DB_PATH = "";

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.loading_municipios, null);
        builder.setView(v);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= 17)
                    DB_PATH = MainActivity.getInstance().getApplicationInfo().dataDir + "/databases/";
                else
                    DB_PATH = "/data/data/" + MainActivity.getInstance().getPackageName() + "/databases/";

                File dbFile = new File(DB_PATH + DB_NAME);
                if(dbFile.exists()){
                    dbFile.delete();
                }
                try{
                    InputStream mInput = MainActivity.getInstance().getAssets().open(DB_NAME);
                    OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
                    byte[] mBuffer = new byte[1024];
                    int mLength;
                    while ((mLength = mInput.read(mBuffer)) > 0){
                        mOutput.write(mBuffer, 0, mLength);
                    }
                    mOutput.flush();
                    mOutput.close();
                    mInput.close();
                    SharedPreferences sharedPreferences = MainActivity.getInstance()
                            .getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME,
                                    Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("firstTime", false);
                    editor.apply();
                    getDialog().dismiss();
                }
                catch (IOException mIOException){
                    Log.i("DEBUGGING", mIOException.toString());
                }
                catch (Exception e){
                    Log.i("DEBUGGING", e.toString());
                }
            }
        }).start();

        return builder.create();
    }
}
