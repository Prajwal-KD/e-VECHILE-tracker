package com.v2v.smartsystem.gps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Real Android OS GPS provider using the onboard smartphone satellite receiver.
 */
public class AndroidLocationProvider implements IGpsProvider, LocationListener {

    private final Context context;
    private final LocationManager locationManager;
    private OnLocationUpdateListener listener;
    private GeoLocation lastLocation;
    private boolean isRunning = false;

    public AndroidLocationProvider(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start() {
        if (locationManager == null) return;
        isRunning = true;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0f, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 5.0f, this);
            }
            if (listener != null) {
                listener.onGpsStatusChanged(true, getProviderName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
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
        return "Android Satellite GPS";
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (loc == null) return;
        float speedKmh = loc.hasSpeed() ? (loc.getSpeed() * 3.6f) : 0.0f;
        float bearing = loc.hasBearing() ? loc.getBearing() : 0.0f;
        float accuracy = loc.hasAccuracy() ? loc.getAccuracy() : 5.0f;

        lastLocation = new GeoLocation(loc.getLatitude(), loc.getLongitude(), speedKmh, bearing, accuracy);
        if (listener != null) {
            listener.onLocationUpdated(lastLocation);
        }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}
}
