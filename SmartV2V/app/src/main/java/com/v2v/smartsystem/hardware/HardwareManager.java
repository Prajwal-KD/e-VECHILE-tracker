package com.v2v.smartsystem.hardware;

import com.v2v.smartsystem.model.HardwarePacket;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level manager orchestrating physical vehicle hardware (sensors, switches, actuators).
 * Bridges external physical events into the V2V software core and sends actuation commands back to hardware.
 */
public class HardwareManager implements HardwareStatusListener {

    private static HardwareManager instance;

    private IHardwareBridge activeBridge;
    private final BluetoothHardwareBridge bluetoothBridge;
    private final VirtualHardwareSimulator simulatorBridge;
    private final List<HardwareStatusListener> listeners = new ArrayList<>();

    private HardwarePacket latestPacket;
    private boolean isPhysicalHardware = false;

    private HardwareManager() {
        this.bluetoothBridge = new BluetoothHardwareBridge();
        this.simulatorBridge = new VirtualHardwareSimulator();
        this.bluetoothBridge.setListener(this);
        this.simulatorBridge.setListener(this);

        // Default to virtual simulator until user connects physical hardware
        this.activeBridge = simulatorBridge;
        this.simulatorBridge.connect("DEFAULT_BENCH");
    }

    public static synchronized HardwareManager getInstance() {
        if (instance == null) {
            instance = new HardwareManager();
        }
        return instance;
    }

    public void addListener(HardwareStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(HardwareStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * Connects to a physical Bluetooth microcontroller (e.g. HC-05 / ESP32).
     */
    public void connectPhysicalBluetooth(String macAddress) {
        if (activeBridge == simulatorBridge) {
            simulatorBridge.disconnect();
        }
        activeBridge = bluetoothBridge;
        isPhysicalHardware = true;
        bluetoothBridge.connect(macAddress);
    }

    /**
     * Switches to the in-memory hardware test bench.
     */
    public void switchToTestBench() {
        if (bluetoothBridge.isConnected()) {
            bluetoothBridge.disconnect();
        }
        activeBridge = simulatorBridge;
        isPhysicalHardware = false;
        simulatorBridge.connect("BENCH");
    }

    public void disconnect() {
        if (activeBridge != null) {
            activeBridge.disconnect();
        }
    }

    public boolean isConnected() {
        return activeBridge != null && activeBridge.isConnected();
    }

    public boolean isPhysicalHardware() {
        return isPhysicalHardware;
    }

    public VirtualHardwareSimulator getSimulator() {
        return simulatorBridge;
    }

    public HardwarePacket getLatestPacket() {
        return latestPacket;
    }

    /**
     * Commands the physical hardware buzzer and alert LED to activate.
     */
    public void triggerHardwareAlert(boolean buzzerOn, String ledColor) {
        if (activeBridge != null && activeBridge.isConnected()) {
            String cmd = HardwareProtocolParser.buildActuatorCommand(buzzerOn, ledColor);
            activeBridge.sendCommand(cmd);
        }
    }

    // HardwareStatusListener callbacks forwarded to subscribers
    @Override
    public void onHardwareConnected(String deviceName, String address) {
        for (HardwareStatusListener l : listeners) {
            l.onHardwareConnected(deviceName, address);
        }
    }

    @Override
    public void onHardwareDisconnected(String reason) {
        for (HardwareStatusListener l : listeners) {
            l.onHardwareDisconnected(reason);
        }
    }

    @Override
    public void onHardwareError(String errorMessage) {
        for (HardwareStatusListener l : listeners) {
            l.onHardwareError(errorMessage);
        }
    }

    @Override
    public void onTelemetryPacketReceived(HardwarePacket packet) {
        this.latestPacket = packet;
        for (HardwareStatusListener l : listeners) {
            l.onTelemetryPacketReceived(packet);
        }
    }

    @Override
    public void onBrakeSensorTriggered(boolean isPressed) {
        for (HardwareStatusListener l : listeners) {
            l.onBrakeSensorTriggered(isPressed);
        }
    }

    @Override
    public void onTurnSwitchChanged(String switchState) {
        for (HardwareStatusListener l : listeners) {
            l.onTurnSwitchChanged(switchState);
        }
    }
}
