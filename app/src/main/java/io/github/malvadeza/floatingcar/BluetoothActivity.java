package io.github.malvadeza.floatingcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = BluetoothActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 300;
    public static final int REQUEST_CONNECT_DEVICE = 301;

    public static final String BLUETOOTH_DEVICE_ADDRESS =
            "io.github.malvadeza.floatingcar.bluetooth_device_address";

    private BroadcastReceiver mBcReceiver;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mAdapter;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Log.d(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ListView listView = (ListView) findViewById(R.id.bluetoothList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBtAdapter.isDiscovering())
                    mBtAdapter.cancelDiscovery();

                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Intent intent = new Intent();
                intent.putExtra(BLUETOOTH_DEVICE_ADDRESS, address);

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mBcReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            mAdapter.add(device.getName() + " (Paired)" + "\n" + device.getAddress());
                        } else {
                            mAdapter.add(device.getName() + "\n" + device.getAddress());
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        setProgressBarVisible(true);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        setProgressBarVisible(false);
                        break;
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBtAdapter = bluetoothManager.getAdapter();
            }
        } else {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        doDiscovery();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBcReceiver, intentFilter);

        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBcReceiver, intentFilter);

        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mBcReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        unregisterReceiver(mBcReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, getString(R.string.bluetooth_not_enabled), Toast.LENGTH_LONG).show();
                } else if ( resultCode == RESULT_OK) {
                    doDiscovery();
                }
                break;
        }
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery");

        if (!mBtAdapter.isEnabled()) {
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btIntent, REQUEST_ENABLE_BT);
        } else {
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
            mAdapter.clear();

            mBtAdapter.startDiscovery();
        }
    }

    private void setProgressBarVisible(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }
}
