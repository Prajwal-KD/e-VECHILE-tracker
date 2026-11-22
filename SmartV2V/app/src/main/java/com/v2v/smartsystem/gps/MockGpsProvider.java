package com.v2v.smartsystem.gps;

import android.os.Handler;
import android.os.Looper;

/**
 * High-fidelity simulated GPS provider for lab testing, indoors development, and demonstration.
 */
public class MockGpsProvider implements IGpsProvider {

    private OnLocationUpdateListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private double currentLat = 12.971598;
    private double currentLon = 77.594566;
    private float currentSpeedKmh = 0.0f;
    private float currentHeading = 0.0f; // North
    private boolean isRunning = false;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            // Simulate forward movement based on speed and heading
            if (currentSpeedKmh > 0.1f) {
                double speedMps = currentSpeedKmh / 3.6;
                double distanceMovedMeters = speedMps * 1.0; // 1-second step

                // Earth radius in meters
                double rEarth = 6371000.0;
                double headingRad = Math.toRadians(currentHeading);

                double deltaLat = (distanceMovedMeters * Math.cos(headingRad)) / rEarth;
                double deltaLon = (distanceMovedMeters * Math.sin(headingRad)) / (rEarth * Math.cos(Math.toRadians(currentLat)));

                currentLat += Math.toDegrees(deltaLat);
                currentLon += Math.toDegrees(deltaLon);
            }

            GeoLocation location = new GeoLocation(currentLat, currentLon, currentSpeedKmh, currentHeading, 2.5f);
            if (listener != null) {
                listener.onLocationUpdated(location);
            }

            handler.postDelayed(this, 1000); // 1 Hz GPS tick
        }
    };

    @Override
    public void start() {
        if (isRunning) return;
        isRunning = true;
        if (listener != null) {
            listener.onGpsStatusChanged(true, getProviderName());
        }
        handler.post(tickRunnable);
    }

    @Override
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
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
        return new GeoLocation(currentLat, currentLon, currentSpeedKmh, currentHeading, 2.5f);
    }

    @Override
    public void setListener(OnLocationUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public String getProviderName() {
        return "Simulated GPS Engine";
    }

    public void setSpeedKmh(float speedKmh) {
        this.currentSpeedKmh = Math.max(0, speedKmh);
    }

    public void setHeading(float headingDegrees) {
        this.currentHeading = (headingDegrees + 360.0f) % 360.0f;
    }

    public void setCoordinates(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
    }
}
