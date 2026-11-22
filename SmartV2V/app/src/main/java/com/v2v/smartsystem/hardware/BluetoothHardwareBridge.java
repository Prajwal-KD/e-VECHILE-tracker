package com.v2v.smartsystem.hardware;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import com.v2v.smartsystem.model.HardwarePacket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Real Bluetooth SPP (Serial Port Profile) RFCOMM bridge for communicating with external
 * vehicle hardware microcontrollers (HC-05 / HC-06 Bluetooth modules or ESP32 Bluetooth).
 */
public class BluetoothHardwareBridge implements IHardwareBridge {

    // Standard SPP UUID for Bluetooth Serial connections
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private HardwareStatusListener listener;
    private final Handler mainHandler;

    private Thread workerThread;
    private volatile boolean isRunning = false;
    private boolean lastBrakeState = false;
    private String lastTurnState = "OFF";

    public BluetoothHardwareBridge() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setListener(HardwareStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public String getTransportName() {
        return "Bluetooth SPP (HC-05 / ESP32)";
    }

    @SuppressLint("MissingPermission")
    @Override
    public void connect(String targetAddressOrId) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            notifyError("Bluetooth is not available or disabled on this device.");
            return;
        }

        disconnect(); // Disconnect existing connection if any

        new Thread(() -> {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(targetAddressOrId);
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();

                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                isRunning = true;

                String deviceName = device.getName() != null ? device.getName() : "Hardware Unit";
                mainHandler.post(() -> {
                    if (listener != null) listener.onHardwareConnected(deviceName, device.getAddress());
                });

                startReaderLoop();

            } catch (Exception e) {
                notifyError("Connection failed: " + e.getMessage());
                disconnect();
            }
        }).start();
    }

    private void startReaderLoop() {
        workerThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while (isRunning && (line = reader.readLine()) != null) {
                    final HardwarePacket packet = HardwareProtocolParser.parseIncomingLine(line);
                    if (packet != null) {
                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onTelemetryPacketReceived(packet);

                                // Check for state changes
                                if (packet.isBrakePressed() != lastBrakeState) {
                                    lastBrakeState = packet.isBrakePressed();
                                    listener.onBrakeSensorTriggered(lastBrakeState);
                                }

                                if (!packet.getTurnSwitch().equals(lastTurnState)) {
                                    lastTurnState = packet.getTurnSwitch();
                                    listener.onTurnSwitchChanged(lastTurnState);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                if (isRunning) {
                    mainHandler.post(() -> {
                        if (listener != null) listener.onHardwareDisconnected("Connection lost: " + e.getMessage());
                    });
                }
            } finally {
                disconnect();
            }
        });
        workerThread.start();
    }

    @Override
    public synchronized void disconnect() {
        isRunning = false;
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception ignored) {}

        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }

        mainHandler.post(() -> {
            if (listener != null) listener.onHardwareDisconnected("Disconnected by user");
        });
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && isRunning;
    }

    @Override
    public boolean sendCommand(String command) {
        if (!isConnected() || outputStream == null) return false;
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void notifyError(String error) {
        mainHandler.post(() -> {
            if (listener != null) listener.onHardwareError(error);
        });
    }
}
