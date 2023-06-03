package com.app.plantmonitoring.offline.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.plantmonitoring.R;
import com.app.plantmonitoring.database.DatabaseHelper;
import com.app.plantmonitoring.database.devices.Delay;
import com.app.plantmonitoring.database.devices.Notification;
import com.app.plantmonitoring.database.devices.Settings;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment {

    private View view;
    private TextInputLayout delayInput;
    private AutoCompleteTextView delayUnitInput;
    private TextInputLayout pumpRunTime;
    private TextInputLayout soilThreshold;
    private AppCompatButton saveBtn;

    private String delay;
    private String delayUnit;
    private String pumpTime;
    private String threshold;

    private boolean[] validData = new boolean[] {true, true, true, true};

    private ProgressDialog progressDialog;

    private final List<String> timeUnits = Arrays.asList("seconds", "minutes", "hours", "days");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_bluetooth_settings, container, false);

        setConnections();
        initProgressDialog();

        LocalBroadcastManager.getInstance(view.getContext()).registerReceiver(receiver, new IntentFilter("DataIn2"));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, timeUnits);
        delayUnitInput.setAdapter(adapter);
        // the minimum number of characters the user has to type in the edit box before the drop down list is shown
        delayUnitInput.setThreshold(0);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(validData[0] && validData[1] && validData[2] && validData[3])){
                    return;
                }
                progressDialog.show();
                delayUnit = delayUnitInput.getText().toString();
                saveData();
            }
        });

        return view;
    }

    private void setConnections(){
        delayInput = view.findViewById(R.id.settings_delay_bluetooth);
        delayUnitInput = view.findViewById(R.id.settings_delay_unit_bluetooth);
        pumpRunTime = view.findViewById(R.id.settings_pump_run_bluetooth);
        soilThreshold = view.findViewById(R.id.settings_soil_threshold_bluetooth);
        saveBtn = view.findViewById(R.id.settings_save_btn_bluetooth);
    }

    // Initialize loading animation
    private void initProgressDialog(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Saving...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
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
    }

    private void saveData(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Toast.makeText(getContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
        String dataToSave = delay + "," + delayUnit + "," + pumpTime + "," + threshold;
        Intent intent = new Intent("SaveData");
        intent.putExtra("data", dataToSave);
        LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] dataIn = intent.getStringExtra("input").split(",");

            delay = dataIn[0];
            delayInput.getEditText().setText(delay);

            delayUnit = dataIn[1];
            delayUnitInput.setText(delayUnit);

            pumpTime = dataIn[2];
            pumpRunTime.getEditText().setText(pumpTime);

            threshold = dataIn[3];
            soilThreshold.getEditText().setText(threshold);

            validations();

            progressDialog.dismiss();
        }
    };
}