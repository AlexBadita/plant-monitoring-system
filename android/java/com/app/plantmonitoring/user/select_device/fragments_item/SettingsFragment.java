package com.app.plantmonitoring.user.select_device.fragments_item;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.devices.Delay;
import com.app.plantmonitoring.database.devices.Notification;
import com.app.plantmonitoring.database.devices.Settings;
import com.app.plantmonitoring.database.plants.Plant;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment {

    private final DatabaseHelper databaseHelper = DatabaseHelper.getInstance();

    private View view;
    private TextInputLayout delayInput;
    private AutoCompleteTextView delayUnitInput;
    private TextInputLayout pumpRunTime;
    private TextInputLayout soilThreshold;
    private TextInputLayout minTemperature;
    private TextInputLayout maxTemperature;
    private SwitchMaterial switchTemperature;
    private TextInputLayout minHumidity;
    private TextInputLayout maxHumidity;
    private SwitchMaterial switchHumidity;
    private TextInputLayout minLight;
    private TextInputLayout maxLight;
    private SwitchMaterial switchLight;
    private AppCompatButton saveBtn;
    private AutoCompleteTextView suggestedSettings;

    private String delay;
    private String delayUnit;
    private String pumpTime;
    private String threshold;
    private String temperatureMin;
    private String temperatureMax;
    private boolean temperatureEnabled;
    private String humidityMin;
    private String humidityMax;
    private boolean humidityEnabled;
    private String lightMin;
    private String lightMax;
    private boolean lightEnabled;

    private String id;

    private boolean[] validData = new boolean[] {true, true, true, true, true, true, true, true, true};

    private ProgressDialog progressDialog;

    private final List<String> timeUnits = Arrays.asList("seconds", "minutes", "hours", "days");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_settings, container, false);

        setConnections();
        initProgressDialog();

        id = databaseHelper.getCurrentDeviceId();
        getData();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, timeUnits);
        delayUnitInput.setAdapter(adapter);
        // the minimum number of characters the user has to type in the edit box before the drop down list is shown
        delayUnitInput.setThreshold(0);

        List<String> plantNames = new ArrayList<>();
        for(Plant plant : databaseHelper.getAllPlants()){
            plantNames.add(plant.getName());
        }
        ArrayAdapter<String> plantsAdapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, plantNames);
        suggestedSettings.setAdapter(plantsAdapter);
        suggestedSettings.setThreshold(0);
        suggestedSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Plant plant = databaseHelper.getAllPlants().get(position);

                threshold = plant.getMoisture();
                soilThreshold.getEditText().setText(threshold);

                temperatureMin = plant.getTemperatureMin();
                minTemperature.getEditText().setText(temperatureMin);

                temperatureMax = plant.getTemperatureMax();
                maxTemperature.getEditText().setText(temperatureMax);

                humidityMin = plant.getHumidityMin();
                minHumidity.getEditText().setText(humidityMin);

                humidityMax = plant.getHumidityMax();
                maxHumidity.getEditText().setText(humidityMax);

                lightMin = plant.getLightMin();
                minLight.getEditText().setText(lightMin);

                lightMax = plant.getLightMax();
                maxLight.getEditText().setText(lightMax);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(validData[0] && validData[1] && validData[2] && validData[3] && validData[4] &&
                        validData[5] && validData[6] && validData[7] && validData[8])){
                    return;
                }
                progressDialog.show();
                delayUnit = delayUnitInput.getText().toString();
                temperatureEnabled = switchTemperature.isChecked();
                humidityEnabled = switchHumidity.isChecked();
                lightEnabled = switchLight.isChecked();
                saveData();
            }
        });

        return view;
    }

    private void setConnections(){
        delayInput = view.findViewById(R.id.settings_delay);
        delayUnitInput = view.findViewById(R.id.settings_delay_unit);
        pumpRunTime = view.findViewById(R.id.settings_pump_run);
        soilThreshold = view.findViewById(R.id.settings_soil_threshold);
        maxTemperature = view.findViewById(R.id.settings_max_temperature);
        minTemperature = view.findViewById(R.id.settings_min_temperature);
        switchTemperature = view.findViewById(R.id.settings_switch_temperature);
        maxHumidity = view.findViewById(R.id.settings_max_humidity);
        minHumidity = view.findViewById(R.id.settings_min_humidity);
        switchHumidity = view.findViewById(R.id.settings_switch_humidity);
        maxLight = view.findViewById(R.id.settings_max_light);
        minLight = view.findViewById(R.id.settings_min_light);
        switchLight = view.findViewById(R.id.settings_switch_light);
        saveBtn = view.findViewById(R.id.settings_save_btn);
        suggestedSettings = view.findViewById(R.id.settings_search);
    }

    // Initialize loading animation
    private void initProgressDialog(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Saving...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void getData(){
        databaseHelper.getDeviceRef().child(id).child("Settings").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String unit = snapshot.child("Delay").child("unit").getValue().toString();
                        String delay = snapshot.child("Delay").child("value").getValue().toString();
                        String minTemperature = snapshot.child("Temperature").child("min").getValue().toString();
                        String maxTemperature = snapshot.child("Temperature").child("max").getValue().toString();
                        boolean enabledTemperature = Boolean.parseBoolean(snapshot.child("Temperature").child("enabled").getValue().toString());
                        String minHumidity = snapshot.child("Humidity").child("min").getValue().toString();
                        String maxHumidity = snapshot.child("Humidity").child("max").getValue().toString();
                        boolean enabledHumidity = Boolean.parseBoolean(snapshot.child("Humidity").child("enabled").getValue().toString());
                        String minLight = snapshot.child("Light").child("min").getValue().toString();
                        String maxLight = snapshot.child("Light").child("max").getValue().toString();
                        boolean enabledLight = Boolean.parseBoolean(snapshot.child("Light").child("enabled").getValue().toString());
                        String threshold = snapshot.child("moistureThreshold").getValue().toString();
                        String pump = snapshot.child("pumpRunTime").getValue().toString();
                        Settings settings = new Settings(new Delay(delay, unit),
                                new Notification(enabledTemperature, minTemperature, maxTemperature),
                                new Notification(enabledHumidity, minHumidity, maxHumidity),
                                new Notification(enabledLight, minLight, maxLight),
                                threshold, pump);
                        setValues(settings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                }
        );
    }

    private void setValues(Settings settings){
        delay = settings.getDelay().getValue();
        delayInput.getEditText().setText(delay);

        delayUnit = settings.getDelay().getUnit();
        delayUnitInput.setText(delayUnit);

        pumpTime = settings.getPumpRunTime();
        pumpRunTime.getEditText().setText(pumpTime);

        threshold = settings.getMoistureThreshold();
        soilThreshold.getEditText().setText(threshold);

        temperatureMin = settings.getTemperatureNotification().getMin();
        minTemperature.getEditText().setText(temperatureMin);

        temperatureMax = settings.getTemperatureNotification().getMax();
        maxTemperature.getEditText().setText(temperatureMax);

        temperatureEnabled = settings.getTemperatureNotification().isEnabled();
        switchTemperature.setChecked(temperatureEnabled);

        humidityMin = settings.getHumidityNotification().getMin();
        minHumidity.getEditText().setText(humidityMin);

        humidityMax = settings.getHumidityNotification().getMax();
        maxHumidity.getEditText().setText(humidityMax);

        humidityEnabled = settings.getHumidityNotification().isEnabled();
        switchHumidity.setChecked(humidityEnabled);

        lightMin = settings.getLightNotification().getMin();
        minLight.getEditText().setText(lightMin);

        lightMax = settings.getLightNotification().getMax();
        maxLight.getEditText().setText(lightMax);

        lightEnabled = settings.getLightNotification().isEnabled();
        switchLight.setChecked(lightEnabled);

        validations();
    }

    private void validations(){
        delayInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    delayInput.setError("This field cannot be empty!");
                    validData[0] = false;
                }
                else if((delayUnit.equals("days") && Integer.parseInt(s.toString()) > 7) ||
                        (delayUnit.equals("hours") && Integer.parseInt(s.toString()) > 168) ||
                        (delayUnit.equals("minutes") && Integer.parseInt(s.toString()) > 10080) ||
                        (delayUnit.equals("seconds") && Integer.parseInt(s.toString()) > 604800)){
                    delayInput.setError("Please select a delay value of maximum 7 days");
                    validData[0] = false;
                }
                else{
                    delayInput.setError(null);
                    validData[0] = true;
                }
                delay = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        pumpRunTime.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    pumpRunTime.setError("This field cannot be empty!");
                    validData[1] = false;
                }
                else if(Integer.parseInt(s.toString()) < 1){
                    pumpRunTime.setError("Please select a value of minimum 1 second");
                    validData[1] = false;
                }
                else if(Integer.parseInt(s.toString()) > 10){
                    pumpRunTime.setError("Please select a value of maximum 10 seconds");
                    validData[1] = false;
                }
                else {
                    pumpRunTime.setError(null);
                    validData[1] = true;
                }
                pumpTime = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        soilThreshold.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    soilThreshold.setError("This field cannot be empty!");
                    validData[2] = false;
                }
                else if(Integer.parseInt(s.toString()) < 0){
                    soilThreshold.setError("This field cannot be negative!");
                    validData[2] = false;
                }
                else if(Integer.parseInt(s.toString()) > 100){
                    soilThreshold.setError("This field cannot be greater than 100!");
                    validData[2] = false;
                }
                else {
                    soilThreshold.setError(null);
                    validData[2] = true;
                }
                threshold = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        minTemperature.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    minTemperature.setError("This field cannot be empty!");
                    validData[3] = false;
                }
                else if(Integer.parseInt(s.toString()) > Integer.parseInt(temperatureMax)){
                    minTemperature.setError("This field must be less than Max Value!");
                    validData[3] = false;
                }
                else {
                    minTemperature.setError(null);
                    validData[3] = true;
                }
                temperatureMin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        maxTemperature.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    maxTemperature.setError("This field cannot be empty!");
                    validData[4] = false;
                }
                else if(Integer.parseInt(s.toString()) < Integer.parseInt(temperatureMin)){
                    maxTemperature.setError("This field must be more than Min Value!");
                    validData[4] = false;
                }
                else {
                    maxTemperature.setError(null);
                    validData[4] = true;
                }
                temperatureMax = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        minHumidity.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    minHumidity.setError("This field cannot be empty!");
                    validData[5] = false;
                }
                else if(Integer.parseInt(s.toString()) > Integer.parseInt(humidityMax)){
                    minHumidity.setError("This field must be less than Max Value!");
                    validData[5] = false;
                }
                else {
                    minHumidity.setError(null);
                    validData[5] = true;
                }
                humidityMin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        maxHumidity.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    maxHumidity.setError("This field cannot be empty!");
                    validData[6] = false;
                }
                else if(Integer.parseInt(s.toString()) < Integer.parseInt(humidityMin)){
                    maxHumidity.setError("This field must be more than Min Value!");
                    validData[6] = false;
                }
                else {
                    maxHumidity.setError(null);
                    validData[6] = true;
                }
                humidityMax = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        minLight.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    minLight.setError("This field cannot be empty!");
                    validData[7] = false;
                }
                else if(Integer.parseInt(s.toString()) > Integer.parseInt(lightMax)){
                    minLight.setError("This field must be less than Max Value!");
                    validData[7] = false;
                }
                else {
                    minLight.setError(null);
                    validData[7] = true;
                }
                lightMin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        maxLight.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)){
                    maxLight.setError("This field cannot be empty!");
                    validData[8] = false;
                }
                else if(Integer.parseInt(s.toString()) < Integer.parseInt(lightMin)){
                    maxLight.setError("This field must be more than Min Value!");
                    validData[8] = false;
                }
                else {
                    maxLight.setError(null);
                    validData[8] = true;
                }
                lightMax = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void saveData(){
        databaseHelper.getDeviceRef().child(id).child("Delay").child("unit").setValue(delayUnit);
        databaseHelper.getDeviceRef().child(id).child("Delay").child("value").setValue(delay);
        databaseHelper.getDeviceRef().child(id).child("Temperature").child("min").setValue(temperatureMin);
        databaseHelper.getDeviceRef().child(id).child("Temperature").child("max").setValue(temperatureMax);
        databaseHelper.getDeviceRef().child(id).child("Temperature").child("enabled").setValue(temperatureEnabled);
        databaseHelper.getDeviceRef().child(id).child("Humidity").child("min").setValue(humidityMin);
        databaseHelper.getDeviceRef().child(id).child("Humidity").child("max").setValue(humidityMax);
        databaseHelper.getDeviceRef().child(id).child("Humidity").child("enabled").setValue(humidityEnabled);
        databaseHelper.getDeviceRef().child(id).child("Light").child("min").setValue(lightMin);
        databaseHelper.getDeviceRef().child(id).child("Light").child("max").setValue(lightMax);
        databaseHelper.getDeviceRef().child(id).child("Light").child("enabled").setValue(lightEnabled);
        databaseHelper.getDeviceRef().child(id).child("changes").setValue(true);
        databaseHelper.getDeviceRef().child(id).child("moistureThreshold").setValue(threshold);
        databaseHelper.getDeviceRef().child(id).child("pumpRunTime").setValue(pumpTime);
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Toast.makeText(view.getContext(), "Dara saved!", Toast.LENGTH_SHORT).show();
    }
}