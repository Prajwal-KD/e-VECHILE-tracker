package com.v2v.smartsystem.safety;

import com.v2v.smartsystem.hardware.HardwareManager;
import com.v2v.smartsystem.hardware.HardwareStatusListener;
import com.v2v.smartsystem.model.HardwarePacket;
import com.v2v.smartsystem.model.MessageType;
import com.v2v.smartsystem.model.Vehicle;
import com.v2v.smartsystem.vehicle.VehicleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for vehicular safety actions: emergency braking, turn signals, and hazard flasher states.
 * Connects both software UI button events and physical hardware switches/sensors.
 */
public class SafetyActionController implements HardwareStatusListener {

    public interface SafetyActionListener {
        void onEmergencyBrakeStateChanged(boolean isActive, String source);
        void onTurnSignalStateChanged(String turnState); // "NONE", "LEFT", "RIGHT", "HAZARD"
    }

    private static SafetyActionController instance;

    private boolean emergencyBrakeActive = false;
    private String turnSignalState = "NONE";
    private final List<SafetyActionListener> listeners = new ArrayList<>();

    private Runnable onBrakeBroadcastTrigger;
    private Runnable onTurnBroadcastTrigger;

    private SafetyActionController() {
        HardwareManager.getInstance().addListener(this);
    }

    public static synchronized SafetyActionController getInstance() {
        if (instance == null) {
            instance = new SafetyActionController();
        }
        return instance;
    }

    public void addListener(SafetyActionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SafetyActionListener listener) {
        listeners.remove(listener);
    }

    public void setBroadcastTriggers(Runnable brakeTrigger, Runnable turnTrigger) {
        this.onBrakeBroadcastTrigger = brakeTrigger;
        this.onTurnBroadcastTrigger = turnTrigger;
    }

    public boolean isEmergencyBrakeActive() {
        return emergencyBrakeActive;
    }

    public String getTurnSignalState() {
        return turnSignalState;
    }

    /**
     * Toggles emergency brake state from software UI button.
     */
    public void toggleEmergencyBrake() {
        setEmergencyBrake(!emergencyBrakeActive, "SOFTWARE_UI");
    }

    /**
     * Explicitly sets emergency brake state and triggers high-priority V2V broadcast if active.
     */
    public void setEmergencyBrake(boolean active, String source) {
        this.emergencyBrakeActive = active;
        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host != null) {
            host.setEmergencyBrakeActive(active);
        }

        // Notify listeners
        for (SafetyActionListener l : listeners) {
            l.onEmergencyBrakeStateChanged(active, source);
        }

        // Trigger immediate high-priority V2V broadcast
        if (active && onBrakeBroadcastTrigger != null) {
            onBrakeBroadcastTrigger.run();
        }
    }

    /**
     * Sets turn indicator state ("NONE", "LEFT", "RIGHT", "HAZARD").
     */
    public void setTurnSignalState(String state) {
        if (state == null) state = "NONE";
        if (state.equals(this.turnSignalState)) {
            this.turnSignalState = "NONE"; // Toggle off if clicked again
        } else {
            this.turnSignalState = state;
        }

        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host != null) {
            host.setTurnSignal(this.turnSignalState);
        }

        for (SafetyActionListener l : listeners) {
            l.onTurnSignalStateChanged(this.turnSignalState);
        }

        if (onTurnBroadcastTrigger != null) {
            onTurnBroadcastTrigger.run();
        }
    }

    // HardwareStatusListener callbacks (Physical Hardware sensor events)
    @Override
    public void onBrakeSensorTriggered(boolean isPressed) {
        setEmergencyBrake(isPressed, "PHYSICAL_HARDWARE_PIN");
    }

    @Override
    public void onTurnSwitchChanged(String switchState) {
        setTurnSignalState(switchState);
    }

    @Override public void onHardwareConnected(String deviceName, String address) {}
    @Override public void onHardwareDisconnected(String reason) {}
    @Override public void onHardwareError(String errorMessage) {}
    @Override public void onTelemetryPacketReceived(HardwarePacket packet) {}
}
