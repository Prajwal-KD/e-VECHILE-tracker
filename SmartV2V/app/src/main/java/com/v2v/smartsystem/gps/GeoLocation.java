package com.v2v.smartsystem.gps;

/**
 * Immutable geographic location and motion state.
 */
public class GeoLocation {
    private final double latitude;
    private final double longitude;
    private final float speedKmh;
    private final float headingDegrees;
    private final float accuracyMeters;
    private final long timestamp;

    public GeoLocation(double latitude, double longitude, float speedKmh, float headingDegrees, float accuracyMeters) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.headingDegrees = headingDegrees;
        this.accuracyMeters = accuracyMeters;
        this.timestamp = System.currentTimeMillis();
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getSpeedKmh() { return speedKmh; }
    public float getHeadingDegrees() { return headingDegrees; }
    public float getAccuracyMeters() { return accuracyMeters; }
    public long getTimestamp() { return timestamp; }
}
