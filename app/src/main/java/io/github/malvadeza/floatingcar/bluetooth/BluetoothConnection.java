package io.github.malvadeza.floatingcar.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnection {
    private static final String TAG = BluetoothConnection.class.getSimpleName();

    public static final int BLUETOOTH_CONNECTING_DEVICE = 1;
    public static final int BLUETOOTH_CONNECTED_DEVICE = 2;
    public static final int BLUETOOTH_STATE_CHANGE = 3;
    public static final int BLUETOOTH_CONNECTION_ERROR = 4;

    public static final String BLUETOOTH_TARGET_DEVICE_NAME =
            "io.github.malvadeza.floatingcar.bluetooth.target_device_name";
    public static final String BLUETOOTH_TARGET_DEVICE_ADDRESS =
            "io.github.malvadeza.floatingcar.bluetooth.target_device_address";

    public static final int BLUETOOTH_STATE_NOT_CONNECTED = 0;
    public static final int BLUETOOTH_STATE_CONNECTING = 1;
    public static final int BLUETOOTH_STATE_CONNECTED = 2;

    private Handler mHandler;

    private int state = BLUETOOTH_STATE_NOT_CONNECTED;

    private BluetoothAdapter mBtAdapter;

    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectThread connectThread;

    private BluetoothSocket btSocket;

    public BluetoothConnection(Handler handler, BluetoothAdapter btAdapter) {
        mHandler = handler;
        mBtAdapter = btAdapter;
    }

    public void connect(BluetoothDevice btDevice) {
        Bundle bundle = new Bundle();
        bundle.putString(BLUETOOTH_TARGET_DEVICE_NAME, btDevice.getName());
        bundle.putString(BLUETOOTH_TARGET_DEVICE_ADDRESS, btDevice.getAddress());

        Message msg = mHandler.obtainMessage(BLUETOOTH_CONNECTING_DEVICE);
        msg.setData(bundle);

        mHandler.sendMessage(msg);

        setState(BLUETOOTH_STATE_CONNECTING);

        mBtAdapter.cancelDiscovery();

        connectThread = new ConnectThread(btDevice);
        connectThread.start();
    }

    private void connected(BluetoothSocket btSocket, BluetoothDevice btDevice) {
        disconnect();

        this.btSocket = btSocket;

        Bundle bundle = new Bundle();
        bundle.putString(BLUETOOTH_TARGET_DEVICE_NAME, btDevice.getName());
        bundle.putString(BLUETOOTH_TARGET_DEVICE_ADDRESS, btDevice.getAddress());

        Message msg = mHandler.obtainMessage(BLUETOOTH_CONNECTED_DEVICE);
        msg.setData(bundle);

        mHandler.sendMessage(msg);

        setState(BLUETOOTH_STATE_CONNECTED);
    }

    public BluetoothSocket getSocket() {
        return btSocket;
    }

    public void disconnect() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        setState(BLUETOOTH_STATE_NOT_CONNECTED);
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "BluetoothConnection.setState() -> " + this.state + " -> " + state);
        this.state = state;

        mHandler.obtainMessage(BLUETOOTH_STATE_CHANGE, state, -1).sendToTarget();
    }

    private class ConnectThread extends Thread {
        private BluetoothDevice mBtDevice;
        private BluetoothSocket mBtSocket;

        public ConnectThread(BluetoothDevice btDevice) {
            mBtDevice = btDevice;

            try {
                mBtSocket = btDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread() -> Socket creation failed", e);
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "Beginning ConnectThread");

            try {
                mBtSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "ConnectTread.run() -> Socket connection failed", e);

                Bundle bundle = new Bundle();
                bundle.putString(BLUETOOTH_TARGET_DEVICE_NAME, mBtDevice.getName());
                bundle.putString(BLUETOOTH_TARGET_DEVICE_ADDRESS, mBtDevice.getAddress());


                Message msg = mHandler.obtainMessage(BLUETOOTH_CONNECTION_ERROR);
                msg.setData(bundle);

                mHandler.sendMessage(msg);

                try {
                    mBtSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "ConnectTread.run() -> Unable to close socket", e1);
                }

                return;
            }

            synchronized (BluetoothConnection.this) {
                connectThread = null;
            }

            connected(mBtSocket, mBtDevice);
        }

        public void cancel() {
            try {
                mBtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread.cancel() -> Unable to close socket");
            }
        }

    }
}

