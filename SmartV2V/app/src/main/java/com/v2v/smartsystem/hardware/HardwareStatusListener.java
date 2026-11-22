package com.v2v.smartsystem.hardware;

import com.v2v.smartsystem.model.HardwarePacket;

/**
 * Callbacks for hardware connection events and incoming physical telemetry.
 */
public interface HardwareStatusListener {
    void onHardwareConnected(String deviceName, String address);
    void onHardwareDisconnected(String reason);
    void onHardwareError(String errorMessage);
    void onTelemetryPacketReceived(HardwarePacket packet);
    void onBrakeSensorTriggered(boolean isPressed);
    void onTurnSwitchChanged(String switchState); // "OFF", "LEFT", "RIGHT", "HAZARD"
}
