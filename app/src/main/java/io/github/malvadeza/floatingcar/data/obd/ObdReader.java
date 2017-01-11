package io.github.malvadeza.floatingcar.data.obd;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.HeadersOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.github.pires.obd.commands.protocol.SpacesOffCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.malvadeza.floatingcar.data.ObdValue;

public class ObdReader {
    private static final String TAG = ObdReader.class.getSimpleName();
    private final BluetoothSocket mBtSocket;
    private List<ObdCommand> mObdCommands = new ArrayList<>();
    private List<ObdCommand> mSetupCommands = new ArrayList<>();

    public ObdReader(BluetoothSocket btSocket) {
        mBtSocket = btSocket;

        // TODO: Will get from preferences, but will default to Speed and RPM
        mObdCommands.add(new SpeedCommand());
        mObdCommands.add(new RPMCommand());

        // Set Defaults and Reset all
        mSetupCommands.add(new ObdRawCommand("AT D"));
        mSetupCommands.add(new ObdRawCommand("AT Z"));

        mSetupCommands.add(new EchoOffCommand());
        mSetupCommands.add(new LineFeedOffCommand());
        mSetupCommands.add(new SpacesOffCommand());
        mSetupCommands.add(new HeadersOffCommand());
    }

    public void setupObd() {
        Log.d(TAG, "Setting up OBD connection");
        try {
            for (ObdCommand command: mSetupCommands) {
                command.run(mBtSocket.getInputStream(), mBtSocket.getOutputStream());
            }
        } catch (IOException | InterruptedException  e) {
            e.printStackTrace();
        }
    }

    public List<ObdValue> readValues() {
        Log.d(TAG, "Reading values");

        List<ObdValue> obdValues = new ArrayList<>();
        for (ObdCommand command: mObdCommands) {
            try {
                command.run(mBtSocket.getInputStream(), mBtSocket.getOutputStream());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            obdValues.add(new ObdValue(command.getCommandPID(), command.getCalculatedResult()));
        }

        return obdValues;
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting bluetooth");

        try {
            mBtSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
