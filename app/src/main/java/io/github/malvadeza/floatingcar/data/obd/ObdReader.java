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
    private final BluetoothSocket mBtSocket;
    private List<ObdCommand> obdCommands = new ArrayList<>();
    private List<ObdCommand> setupCommands = new ArrayList<>();

    public ObdReader(BluetoothSocket btSocket) {
        mBtSocket = btSocket;

        // Will get from preferences, but will default to Speed and RPM
        obdCommands.add(new SpeedCommand());
        obdCommands.add(new RPMCommand());

        // Set Defaults and Reset all
        setupCommands.add(new ObdRawCommand("AT D"));
        setupCommands.add(new ObdRawCommand("AT Z"));

        setupCommands.add(new EchoOffCommand());
        setupCommands.add(new LineFeedOffCommand());
        setupCommands.add(new SpacesOffCommand());
        setupCommands.add(new HeadersOffCommand());
    }

    public void setupObd() {
        try {
            for (ObdCommand command: setupCommands) {
                command.run(mBtSocket.getInputStream(), mBtSocket.getOutputStream());
            }
        } catch (IOException | InterruptedException  e) {
            e.printStackTrace();
        }
    }

    public List<ObdValue> readValues() {
        List<ObdValue> obdValues = new ArrayList<>();
        for (ObdCommand command: obdCommands) {
            try {
                command.run(mBtSocket.getInputStream(), mBtSocket.getOutputStream());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            obdValues.add(new ObdValue(command.getCommandPID(), command.getCalculatedResult()));
        }

        return obdValues;
    }
}
