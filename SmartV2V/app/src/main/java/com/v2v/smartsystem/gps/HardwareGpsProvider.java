package com.v2v.smartsystem.gps;

import com.v2v.smartsystem.hardware.HardwareManager;
import com.v2v.smartsystem.hardware.HardwareStatusListener;
import com.v2v.smartsystem.model.HardwarePacket;

/**
 * Pipes external GPS telemetry from the physical vehicle hardware unit (NEO-6M / NEO-8M module
 * attached to the ESP32/Arduino) directly into the V2V positioning engine.
 */
public class HardwareGpsProvider implements IGpsProvider, HardwareStatusListener {

    private OnLocationUpdateListener listener;
    private GeoLocation lastLocation;
    private boolean isRunning = false;

    public HardwareGpsProvider() {
        HardwareManager.getInstance().addListener(this);
    }

    @Override
    public void start() {
        isRunning = true;
        if (listener != null) {
            listener.onGpsStatusChanged(true, getProviderName());
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        if (listener != null) {
            listener.onGpsStatusChanged(false, getProviderName());
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public GeoLocation getLastKnownLocation() {
        return lastLocation;
    }

    @Override
    public void setListener(OnLocationUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public String getProviderName() {
        return "External NEO-6M Hardware GPS";
    }

    @Override
    public void onTelemetryPacketReceived(HardwarePacket packet) {
        if (!isRunning || packet == null) return;
        if (packet.hasGpsFix() || (packet.getLatitude() != 0.0 && packet.getLongitude() != 0.0)) {
            lastLocation = new GeoLocation(
                    packet.getLatitude(),
                    packet.getLongitude(),
                    packet.getSpeedKmh(),
                    0.0f,
                    3.0f
            );
            if (listener != null) {
                listener.onLocationUpdated(lastLocation);
            }
        }
    }

    @Override public void onHardwareConnected(String deviceName, String address) {}
    @Override public void onHardwareDisconnected(String reason) {}
    @Override public void onHardwareError(String errorMessage) {}
    @Override public void onBrakeSensorTriggered(boolean isPressed) {}
    @Override public void onTurnSwitchChanged(String switchState) {}
}
