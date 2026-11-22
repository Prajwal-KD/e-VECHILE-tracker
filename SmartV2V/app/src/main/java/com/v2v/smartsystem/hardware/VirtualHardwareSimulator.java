package com.v2v.smartsystem.hardware;

import android.os.Handler;
import android.os.Looper;

import com.v2v.smartsystem.model.HardwarePacket;

/**
 * Virtual Hardware Simulator acting as an in-memory test bench.
 * Allows full functional verification of hardware triggers without physical boards attached.
 */
public class VirtualHardwareSimulator implements IHardwareBridge {

    private HardwareStatusListener listener;
    private final Handler mainHandler;
    private boolean isConnected = false;
    private boolean brakePressed = false;
    private String turnSwitchState = "OFF";
    private boolean buzzerActive = false;

    public VirtualHardwareSimulator() {
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void connect(String targetAddressOrId) {
        isConnected = true;
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onHardwareConnected("Virtual ECU Test Bench", "SIM-HW-001");
            }
        });
    }

    @Override
    public void disconnect() {
        isConnected = false;
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onHardwareDisconnected("Test bench offline");
            }
        });
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean sendCommand(String command) {
        if (command != null && command.contains("BUZZER=ON")) {
            buzzerActive = true;
        } else if (command != null && command.contains("BUZZER=OFF")) {
            buzzerActive = false;
        }
        return true;
    }

    @Override
    public void setListener(HardwareStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public String getTransportName() {
        return "Virtual Hardware Test Bench";
    }

    /**
     * Manually simulates tripping the physical brake pedal sensor.
     */
    public void triggerBrakeSensor(boolean pressed) {
        this.brakePressed = pressed;
        emitPacket();
        if (listener != null) {
            listener.onBrakeSensorTriggered(pressed);
        }
    }

    /**
     * Manually simulates toggling the physical turn switch.
     */
    public void setTurnSwitch(String state) {
        this.turnSwitchState = state;
        emitPacket();
        if (listener != null) {
            listener.onTurnSwitchChanged(state);
        }
    }

    public boolean isBuzzerActive() {
        return buzzerActive;
    }

    private void emitPacket() {
        if (!isConnected || listener == null) return;
        HardwarePacket packet = new HardwarePacket();
        packet.setBrakePressed(brakePressed);
        packet.setTurnSwitch(turnSwitchState);
        packet.setBoardVoltage(5.02f);
        packet.setGpsFix(true);
        listener.onTelemetryPacketReceived(packet);
    }
}
