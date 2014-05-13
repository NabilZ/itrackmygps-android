package com.twormobile.itrackmygps;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import com.twormobile.itrackmygps.android.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.android.gms.maps.GoogleMap;

public class SettingsActivity extends Activity {
    private static final String TAG = "MainActivity";

    public static final String PREF_MAP_LAYER_INDEX = "PREF_MAP_LAYER_INDEX";
    public static final String PREF_ZOOM_BASED_ON_SPEED = "PREF_ZOOM_BASED_ON_SPEED";
    public static final String PREF_TIME_INTERVAL_IN_SECONDS = "PREF_TIME_INTERVAL_IN_SECONDS";
    public static final String PREF_TIME_INTERVAL_IN_METERS = "PREF_TIME_INTERVAL_IN_METERS";

    public static final int DEFAULT_TIME_INTERVAL_IN_SECONDS = 300;  //5 minutes
    public static final int DEFAULT_DISTANCE_INTERVAL_IN_METERS = 0;

    private Spinner mapLayerSpinner;
    private CheckBox chkDynamicZoom;
    private EditText etxtTimeInterval;
    private EditText etxtDistanceInterval;

    SharedPreferences prefs;

    private GpsLoggerApplication gpsApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.gpsApp = (GpsLoggerApplication)getApplication();

        mapLayerSpinner = (Spinner)findViewById(R.id.spinner_map_layers);
        chkDynamicZoom = (CheckBox)findViewById(R.id.chk_dynamic_zoom_on_speed);
        etxtTimeInterval = (EditText)findViewById(R.id.etxt_time_interval);
        etxtDistanceInterval = (EditText)findViewById(R.id.etxt_distance_interval);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        populate();

        Context context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateUIFromPreferences();
    }

    private void updateUIFromPreferences() {
        int mapLayerIndex = prefs.getInt(PREF_MAP_LAYER_INDEX, GoogleMap.MAP_TYPE_NORMAL);
        int timeIntervalInSecs = prefs.getInt(PREF_TIME_INTERVAL_IN_SECONDS, DEFAULT_TIME_INTERVAL_IN_SECONDS);
        int distanceIntervalInMeters = prefs.getInt(PREF_TIME_INTERVAL_IN_METERS, DEFAULT_DISTANCE_INTERVAL_IN_METERS);
        boolean isZoomBasedOnSpeed = prefs.getBoolean(PREF_ZOOM_BASED_ON_SPEED, true);

        mapLayerSpinner.setSelection(mapLayerIndex);
        chkDynamicZoom.setChecked(isZoomBasedOnSpeed);
        etxtTimeInterval.setText(Integer.toString(timeIntervalInSecs));
        etxtDistanceInterval.setText(Integer.toString(distanceIntervalInMeters));
    }

    private void populate(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.map_layers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapLayerSpinner.setAdapter(adapter);
    }

    public void buttonSavePressed(View view){
        Log.d(TAG, "buttonSavePressed");
        if(isTimeDistanceIntervalValid()){
            savePreferences();
            SettingsActivity.this.setResult(RESULT_OK);
            finish();
        }
    }

    private boolean isTimeDistanceIntervalValid() {
        boolean valid = true;
        try {
            int timeIntervalInSecs = Integer.parseInt(String.valueOf(etxtTimeInterval.getText()));
            int distanceIntervalInMeters = Integer.parseInt(String.valueOf(etxtDistanceInterval.getText()));

            if(timeIntervalInSecs < 300 && timeIntervalInSecs > 3600){
                valid = false;
                gpsApp.showDialog("Warning", "Time interval should be between 300 seconds (5 minutes) to 3600 seconds (1 hour)", this);
            }

            if(distanceIntervalInMeters < 0) {
                valid = false;
                gpsApp.showDialog("Warning", "Distance interval should be at least 0", this);
            }
        }
        catch (Exception ex) {
            valid = false;
            gpsApp.showDialog("Warning", "Time and Distance intervals should be a valid number", this);
        }

        return valid;
    }

    private void savePreferences() {
        int mapLayerIndex = mapLayerSpinner.getSelectedItemPosition();
        int timeIntervalInSecs = Integer.parseInt(String.valueOf(etxtTimeInterval.getText()));
        int distanceIntervalInMeters = Integer.parseInt(String.valueOf(etxtDistanceInterval.getText()));
        boolean isZoomBasedOnSpeed = chkDynamicZoom.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_MAP_LAYER_INDEX, mapLayerIndex);
        editor.putBoolean(PREF_ZOOM_BASED_ON_SPEED, isZoomBasedOnSpeed);
        editor.putInt(PREF_TIME_INTERVAL_IN_SECONDS, timeIntervalInSecs);
        editor.putInt(PREF_TIME_INTERVAL_IN_METERS, distanceIntervalInMeters);
        editor.commit();
    }

    public void buttonCancelPressed(View view){
        Log.d(TAG, "buttonCancelPressed");
        SettingsActivity.this.setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        SettingsActivity.this.setResult(RESULT_OK);
        finish();
    }

}