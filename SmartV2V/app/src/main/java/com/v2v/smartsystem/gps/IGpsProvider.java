package com.v2v.smartsystem.gps;

/**
 * Universal abstraction for vehicle GPS and motion tracking (Android GPS, Hardware NEO-6M, or Mock Simulator).
 */
public interface IGpsProvider {
    void start();
    void stop();
    boolean isRunning();
    GeoLocation getLastKnownLocation();
    void setListener(OnLocationUpdateListener listener);
    String getProviderName();
}
