package io.github.malvadeza.floatingcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.github.malvadeza.floatingcar.bluetooth.BluetoothConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_LOCATION = 100;

    private BroadcastReceiver mBroadcastReceiver;

    private SharedPreferences mSharedPreferences;
    private BluetoothAdapter mBtAdapter;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBtAdapter = bluetoothManager.getAdapter();
            }
        } else {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.start_trip);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Should ask permissions */
                Log.d(TAG, "Start tracking trip");

                if (LoggingService.isRunning()) return;

                if (mBtAdapter == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getString(R.string.bluetooth_not_found_error), Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                String bluetoothDeviceAddress = mSharedPreferences.getString(getString(R.string.bluetooth_device_key), "");

                if (bluetoothDeviceAddress.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                    startActivityForResult(intent, BluetoothActivity.REQUEST_CONNECT_DEVICE);
                } else {
                    /**
                     * Here I send the BluetoothDevice to the Service
                     * The service then tries to connect to the device,
                     * if it is unable to connect, it should return to
                     * broadcast receiver with an error code informing
                     * what was the error
                     */

                    startBluetoothService(bluetoothDeviceAddress);
                }
            }


        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast from service");

                if (intent.getAction().equals(LoggingService.SERVICE_BROADCAST_MESSAGE)) {
                    switch (intent.getStringExtra(LoggingService.SERVICE_MESSAGE)) {
                        case LoggingService.SERVICE_CONNECTING:
                            Log.d(TAG, "Service connecting");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        case LoggingService.SERVICE_CONNECTED:
                            Log.d(TAG, "Service connecting");
                            String address = intent.getStringExtra(BluetoothConnection.BLUETOOTH_TARGET_DEVICE);

                            mSharedPreferences.edit().putString(getString(R.string.bluetooth_device_key), address);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Starting logging", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case LoggingService.SERVICE_BLUETOOTH_ERROR:
                            Log.d(TAG, "Service bluetooth error");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this,
                                            "Error connecting to bluetooth device",
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        IntentFilter filter = new IntentFilter(LoggingService.SERVICE_BROADCAST_MESSAGE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        if (resultCode == RESULT_OK) {
            String address = data.getStringExtra(BluetoothActivity.BLUETOOTH_DEVICE_ADDRESS);

            Log.d(TAG, "Device address -> " + address);

            startBluetoothService(address);
        }
    }

    private void startBluetoothService(String bluetoothDeviceAddress) {
        BluetoothDevice bluetoothDevice = mBtAdapter.getRemoteDevice(bluetoothDeviceAddress);
        Intent intent = new Intent(MainActivity.this, LoggingService.class);
        intent.setAction(LoggingService.SERVICE_START);
        intent.putExtra("bluetoothDevice", bluetoothDevice);

        startService(intent);
    }

    private boolean requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_LOCATION);

            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted!");
            } else {
                Log.d(TAG, "Permissions denied!");
            }
        }
    }
}
