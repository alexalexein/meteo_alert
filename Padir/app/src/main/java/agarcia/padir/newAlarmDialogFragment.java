package agarcia.padir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import java.util.UUID;

/**
 * Created by agarcia on 11/11/2017.
 */

public class newAlarmDialogFragment extends DialogFragment {

    String id = "";
    String positiveButtonText = "";

    static newAlarmDialogFragment newInstance(UUID id,
                                              String weatherEvent,
                                              String location,
                                              String time,
                                              String type){
        newAlarmDialogFragment f = new newAlarmDialogFragment();
        Bundle args = new Bundle();
        args.putString("ID", id.toString());
        args.putString("Weather Event", weatherEvent);
        args.putString("Location", location);
        args.putString("Time", time);
        args.putString("Type", type);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(positiveButtonText.equals(getContext().getResources().getString(R.string.positiveButtonEditDialogText))){
            String[] nullAnswer = null;
            sendResult(Activity.RESULT_CANCELED, nullAnswer);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.add_dialog, null);

        final Spinner weatherEventSpinner = v.findViewById(R.id.weatherEventSpinner);
        ArrayAdapter<CharSequence> weatherEventAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.weatherEventsArray, android.R.layout.simple_spinner_item);
        weatherEventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weatherEventSpinner.setAdapter(weatherEventAdapter);

        final AutoCompleteTextView locationAutoCompleteTextView = v.findViewById(R.id.locationAutoCompleteTextView);
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.municipios, android.R.layout.simple_dropdown_item_1line);
        locationAutoCompleteTextView.setAdapter(locationAdapter);


        final Spinner timeSpinner = v.findViewById(R.id.alarmTimeSpinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.alarmTimeArray, android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        final Spinner forecastDateSpinner = v.findViewById(R.id.forecastDateSpinner);
        ArrayAdapter<CharSequence> forecastDateAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.forecastDateArray, android.R.layout.simple_spinner_item);
        forecastDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        forecastDateSpinner.setAdapter(forecastDateAdapter);

        try{
            id = getArguments().getString("ID");
            String weatherEvent = getArguments().getString("Weather Event");
            String location = getArguments().getString("Location");
            String alarmTime = getArguments().getString("Time");
            String forecastType = getArguments().getString("Type");
            weatherEventSpinner.setSelection(weatherEventAdapter.getPosition(weatherEvent));
            locationAutoCompleteTextView.setText(location);
            //locationSpinner.setSelection(locationAdapter.getPosition(location));
            timeSpinner.setSelection(timeAdapter.getPosition(alarmTime));
            forecastDateSpinner.setSelection(forecastDateAdapter.getPosition(forecastType));
        }
        catch (NullPointerException e){
            Log.i("DEBUGGING", "Entered NullPointerException");
        }

        if(!id.equals("")){
            positiveButtonText = getContext().getResources().getString(R.string.positiveButtonEditDialogText);
        }
        else {
            positiveButtonText = getContext().getResources().getString(R.string.positiveButtonAddDialogText);
        }
        builder.setView(v)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] results = new String[5];
                        results[0] = weatherEventSpinner.getSelectedItem().toString();
                        results[1] = locationAutoCompleteTextView.getText().toString();
                        results[2] = timeSpinner.getSelectedItem().toString();
                        results[3] = forecastDateSpinner.getSelectedItem().toString();
                        results[4] = id;
                        sendResult(Activity.RESULT_OK, results);
                    }
                })
                .setNegativeButton(getContext().getResources().getString(R.string.cancelAddDialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    private void sendResult(int resultCode, String[] results){
        if(getTargetFragment() == null){
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("agarcia.padir.dialogResults", results);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
