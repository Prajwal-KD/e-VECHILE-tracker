package com.v2v.smartsystem.vehicle;

import com.v2v.smartsystem.model.Vehicle;

/**
 * Listener for host vehicle profile and telemetry updates.
 */
public interface VehicleStatusListener {
    void onVehicleProfileChanged(Vehicle vehicle);
    void onSpeedChanged(float newSpeedKmh);
    void onHeadingChanged(float newHeading);
}
