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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.bluetooth.BluetoothConnection;
import io.github.malvadeza.floatingcar.data.TripLoader;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<TripAdapter.TripHolder>> {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_LOCATION = 100;

    private static final int LOADER_ID = 1;

    private BroadcastReceiver mBroadcastReceiver;

    private SharedPreferences mSharedPreferences;
    private BluetoothAdapter mBtAdapter;

    private ProgressBar mProgressBar;

    private TripAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        if (LoggingService.isRunning()) {
            Intent intent = new Intent(this, LoggingDetailsActivity.class);
            startActivity(intent);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBtAdapter = bluetoothManager.getAdapter();
            }
        } else {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        mAdapter = new TripAdapter(this);

        ListView listView = (ListView) findViewById(R.id.trip_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TripAdapter.TripHolder trip = mAdapter.getItem(position);

                Intent intent = new Intent(MainActivity.this, TripDetailsActivity.class);
                intent.putExtra(TripDetailsActivity.TRIP_SHA, trip.getSha());
                startActivity(intent);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.start_trip);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Should ask permissions */
                if (mBtAdapter == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_not_found_error), Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                if (requestLocationPermissions()) {
                    return;
                }

                Log.d(TAG, "Start tracking trip");

                if (!LoggingService.isRunning()) {
                    String bluetoothDeviceAddress = mSharedPreferences.getString(getString(R.string.bluetooth_device_key), "");

                    if (bluetoothDeviceAddress.isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                        startActivityForResult(intent, BluetoothActivity.REQUEST_CONNECT_DEVICE);
                    } else if (!mBtAdapter.isEnabled()){
                        Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(btIntent, BluetoothActivity.REQUEST_ENABLE_BT);
                    } else {
                        startBluetoothService(bluetoothDeviceAddress);
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, LoggingDetailsActivity.class);
                    startActivity(intent);
                }
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast from Service");

                if (intent.getAction().equals(LoggingService.SERVICE_BROADCAST_MESSAGE)) {
                    switch (intent.getStringExtra(LoggingService.SERVICE_MESSAGE)) {
                        case LoggingService.SERVICE_BLUETOOTH_CONNECTING: {
                            Log.d(TAG, "Service connecting");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        }
                        case LoggingService.SERVICE_BLUETOOTH_CONNECTED: {
                            Log.d(TAG, "Service connected");
                            final String name = intent.getStringExtra(BluetoothConnection.BLUETOOTH_TARGET_DEVICE_NAME);
                            final String address = intent.getStringExtra(BluetoothConnection.BLUETOOTH_TARGET_DEVICE_ADDRESS);

                            mSharedPreferences.edit().putString(getString(R.string.bluetooth_device_key), address).apply();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),
                                            String.format(getString(R.string.bluetooth_connected_starting_logging),
                                                    name, address),
                                            Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(MainActivity.this, LoggingDetailsActivity.class);
                                    startActivity(intent);
                                }
                            });
                            break;
                        }
                        case LoggingService.SERVICE_BLUETOOTH_ERROR: {
                            // TODO: Should open BluetoothActivity to possibly select new device
                            Log.d(TAG, "Service bluetooth error");
                            final String name = intent.getStringExtra(BluetoothConnection.BLUETOOTH_TARGET_DEVICE_NAME);
                            final String address = intent.getStringExtra(BluetoothConnection.BLUETOOTH_TARGET_DEVICE_ADDRESS);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),
                                            String.format(getString(R.string.bluetooth_connecting_errror),
                                                    name, address),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        }

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
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        switch (requestCode) {
            case RESULT_OK: {
                String address = data.getStringExtra(BluetoothActivity.BLUETOOTH_DEVICE_ADDRESS);

                Log.d(TAG, "Device address -> " + address);

                startBluetoothService(address);

                break;
            }
            case BluetoothActivity.REQUEST_ENABLE_BT: {
                Log.d(TAG, "Bluetooth enabled");

                Toast.makeText(this, R.string.bluetooth_enabled_message, Toast.LENGTH_SHORT).show();

                break;
            }
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
                && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public Loader<List<TripAdapter.TripHolder>> onCreateLoader(int id, Bundle args) {
        return new TripLoader(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<List<TripAdapter.TripHolder>> loader, List<TripAdapter.TripHolder> data) {
        // Set adapter data
        mAdapter.clear();

        if (data != null) {
            // Add data
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<TripAdapter.TripHolder>> loader) {
        mAdapter.clear();
    }
}
