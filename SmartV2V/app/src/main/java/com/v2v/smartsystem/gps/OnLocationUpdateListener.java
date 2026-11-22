package com.v2v.smartsystem.gps;

/**
 * Callback interface for receiving vehicle location and motion updates.
 */
public interface OnLocationUpdateListener {
    void onLocationUpdated(GeoLocation location);
    void onGpsStatusChanged(boolean isLocked, String providerName);
}
