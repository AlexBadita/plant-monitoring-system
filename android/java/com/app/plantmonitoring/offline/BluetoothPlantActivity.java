package com.app.plantmonitoring.offline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.app.plantmonitoring.OpeningScreenActivity;
import com.app.plantmonitoring.R;
import com.app.plantmonitoring.offline.fragments.SettingsFragment;
import com.app.plantmonitoring.offline.fragments.ValuesFragment;
import com.app.plantmonitoring.offline.fragments.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothPlantActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket = null;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");;

    private ConnectThread connectThread;
    private DataTransferThread dataTransferThread;

    private ProgressDialog progressDialog;

    private String address;
    private boolean isConnected = false;
    private final String communicationStartFlag = "start";
    private final String communicationStopFlag = "stop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_plant);

        setConnections();
        statusBarColor();
        initProgressDialog();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Plant");

        tabLayout.setupWithViewPager(viewPager);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        registerReceiver(receiver, filter);

        initAdapter();

        Intent intent = getIntent();
        address = intent.getStringExtra("Address");

        if(address != null){
            progressDialog.show();
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            connectThread = new ConnectThread();
            connectThread.start();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                Toast.makeText(BluetoothPlantActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                Toast.makeText(BluetoothPlantActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(BluetoothPlantActivity.this, OpeningScreenActivity.class));
                finish();
            }
        }
    };

    private final BroadcastReceiver settingsChangesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String set = intent.getStringExtra("data");
            dataTransferThread.write(set);
        }
    };

    private void setConnections() {
        toolbar = findViewById(R.id.custom_toolbar);
        tabLayout = findViewById(R.id.tab_menu_offline);
        viewPager = findViewById(R.id.tab_content_offline);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(BluetoothPlantActivity.this);
        progressDialog.setMessage("Connecting...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void initAdapter(){
        ValuesFragment valuesFragment = new ValuesFragment();
        SettingsFragment settingsFragment = new SettingsFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(valuesFragment, "Values");
        adapter.addFragment(settingsFragment, "Settings");
        viewPager.setAdapter(adapter);
    }

    private class ConnectThread extends Thread{
        public ConnectThread(){
            progressDialog.show();
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            try {
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            progressDialog.dismiss();
            dataTransferThread = new DataTransferThread();
            dataTransferThread.run();
        }

        public void cancel(){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class DataTransferThread extends Thread{
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public DataTransferThread(){
            InputStream tempInput = null;
            OutputStream tempOutput = null;
            try {
                tempInput = socket.getInputStream();
                tempOutput = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempInput;
            outputStream = tempOutput;
            write(communicationStartFlag);
            LocalBroadcastManager.getInstance(BluetoothPlantActivity.this).registerReceiver(settingsChangesReceiver, new IntentFilter("SaveData"));
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    buffer[bytes] = (byte) inputStream.read();
                    String dataIn;
                    if(buffer[bytes] == '\n'){
                        dataIn = new String(buffer, 0, bytes);
                        String[] data = dataIn.split("=");
                        if(data[0].equals("values")){
                            Intent intent = new Intent("DataIn1");
                            intent.putExtra("input", data[1]);
                            LocalBroadcastManager.getInstance(BluetoothPlantActivity.this).sendBroadcast(intent);
                        }
                        else if(data[0].equals("settings")){
                            Intent intent = new Intent("DataIn2");
                            intent.putExtra("input", data[1]);
                            LocalBroadcastManager.getInstance(BluetoothPlantActivity.this).sendBroadcast(intent);
                        }
                        bytes = 0;
                    }
                    else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            write(communicationStopFlag);
            startActivity(new Intent(BluetoothPlantActivity.this, OpeningScreenActivity.class));
            finish();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write(String input){
            byte[] bytes = input.getBytes();
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }
}