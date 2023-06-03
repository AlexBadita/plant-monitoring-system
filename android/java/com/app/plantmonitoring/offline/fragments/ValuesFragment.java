package com.app.plantmonitoring.offline.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.plantmonitoring.R;

public class ValuesFragment extends Fragment {

    private View view;

    private ImageView plantImg;
    private TextView temperature;
    private TextView humidity;
    private TextView light;
    private TextView moisture;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_bluetooth_values, container, false);

        setConnections();

        LocalBroadcastManager.getInstance(view.getContext()).registerReceiver(receiver, new IntentFilter("DataIn1"));

        return view;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] dataIn = intent.getStringExtra("input").split(",");
            temperature.setText(dataIn[0]);
            humidity.setText(dataIn[1]);
            light.setText(dataIn[2]);
            moisture.setText(dataIn[3]);
        }
    };

    private void setConnections() {
        plantImg = view.findViewById(R.id.selected_plant_img);
        temperature = view.findViewById(R.id.temperature_bluetooth);
        humidity = view.findViewById(R.id.humidity_bluetooth);
        light = view.findViewById(R.id.light_bluetooth);
        moisture = view.findViewById(R.id.soil_moisture_bluetooth);
    }
}