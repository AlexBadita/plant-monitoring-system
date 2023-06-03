package com.app.plantmonitoring;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.app.plantmonitoring.offline.BluetoothPlantActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpeningScreenActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION_REQ_CODE = 1;
    private static final int BLUETOOTH_ENABLE_REQ_CODE = 2;
    private static final int BLUETOOTH_SCAN_REQ_CODE = 3;
    private static final int LOCATION_REQ_CODE = 4;
    private static final int LOCATION_ENABLE_REQ_CODE = 5;

    // Layout elements
    private AppCompatButton loginBtn;
    private AppCompatButton signupBtn;
    private AppCompatButton offlineBtn;

    // Bluetooth element
    private BluetoothAdapter bluetoothAdapter;

    private List<String> allDevicesName = new ArrayList<>();
    private List<String> allDevicesAddress = new ArrayList<>();
    private ArrayAdapter<String> devicesAdapter;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private final String bluetoothDeviceIdentifier = "HC-05";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening_screen);

        statusBarColor();
        setConnections();
        initProgressDialog();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OpeningScreenActivity.this, LoginActivity.class));
                finish();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OpeningScreenActivity.this, SignupActivity.class));
                finish();
            }
        });

        offlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allDevicesName.clear();
                allDevicesAddress.clear();
                // Check if bluetooth is supported on this device
                if (bluetoothAdapter == null) {
                    Toast.makeText(OpeningScreenActivity.this, "This device does not support bluetooth",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Check for bluetooth permission
                    if (!(ContextCompat.checkSelfPermission(OpeningScreenActivity.this,
                            Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(OpeningScreenActivity.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQ_CODE);
                    } else if (ContextCompat.checkSelfPermission(OpeningScreenActivity.this,
                            Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        activateBluetooth();
                    }
                }
            }
        });
    }

    // Initialize layout connexions
    private void setConnections() {
        loginBtn = findViewById(R.id.login_btn);
        signupBtn = findViewById(R.id.signup_btn);
        offlineBtn = findViewById(R.id.offline_btn);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(OpeningScreenActivity.this);
        progressDialog.setMessage("Scanning...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void statusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
    }

    private void activateBluetooth() {
        // Check if bluetooth is not enabled
        if (!bluetoothAdapter.isEnabled()) {
            // Enable bluetooth
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, BLUETOOTH_ENABLE_REQ_CODE);
        } else {
            permissionToScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateBluetooth();
            } else {
                Toast.makeText(OpeningScreenActivity.this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == BLUETOOTH_SCAN_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermission();
            } else {
                Toast.makeText(OpeningScreenActivity.this, "Scanning for devices denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == LOCATION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanForDevices();
            } else {
                Toast.makeText(OpeningScreenActivity.this, "Accessing location denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == BLUETOOTH_ENABLE_REQ_CODE) {
                permissionToScan();
                Toast.makeText(OpeningScreenActivity.this, "Bluetooth activated", Toast.LENGTH_SHORT).show();
            }
            if (requestCode == LOCATION_ENABLE_REQ_CODE) {
                scanForDevices();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(OpeningScreenActivity.this, "Bluetooth activation refused", Toast.LENGTH_SHORT).show();
        }
    }

    private void permissionToScan() {
        // Check if scanning for devices is allowed
        if (!(ContextCompat.checkSelfPermission(OpeningScreenActivity.this,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(OpeningScreenActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH_SCAN_REQ_CODE);
        } else if (ContextCompat.checkSelfPermission(OpeningScreenActivity.this,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            locationPermission();
        }
    }

    private void locationPermission() {
        // Check permission for location
        if (!(ContextCompat.checkSelfPermission(OpeningScreenActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(OpeningScreenActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQ_CODE);
        } else if (ContextCompat.checkSelfPermission(OpeningScreenActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            scanForDevices();
        }
    }

    private void scanForDevices() {
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                allDevicesName.add(device.getName());
//                allDevicesAddress.add(device.getAddress()); // MAC address
//            }
//        }
//        if (bluetoothAdapter.isDiscovering()) {
//            bluetoothAdapter.cancelDiscovery();
//        }
        buildDialog();
        startScanning();
    }

    private void startScanning() {
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        buildDialog();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                progressDialog.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressDialog.dismiss();
                if (allDevicesName.isEmpty()) {
                    Toast.makeText(OpeningScreenActivity.this, "No device found", Toast.LENGTH_LONG).show();
                } else {
                    builder.show();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                allDevicesName.add(device.getName());
                allDevicesAddress.add(device.getName());
            }
        }
    };

    private void buildDialog() {
        devicesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, allDevicesName);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Nearby devices")
                .setAdapter(devicesAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (allDevicesName.get(which).contains(bluetoothDeviceIdentifier)) {
                            Intent intent = new Intent(OpeningScreenActivity.this, BluetoothPlantActivity.class);
                            intent.putExtra("Address", allDevicesAddress.get(which));
                            startActivity(intent);
                        } else {
                            Toast.makeText(OpeningScreenActivity.this, "This is not a valid device!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create().show();
    }
}