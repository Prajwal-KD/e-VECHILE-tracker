package com.v2v.smartsystem.vehicle;

import android.content.Context;
import android.content.SharedPreferences;

import com.v2v.smartsystem.gps.GeoLocation;
import com.v2v.smartsystem.gps.IGpsProvider;
import com.v2v.smartsystem.gps.MockGpsProvider;
import com.v2v.smartsystem.gps.OnLocationUpdateListener;
import com.v2v.smartsystem.model.Vehicle;
import com.v2v.smartsystem.model.VehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the host vehicle's identity, telemetry profile, and GPS binding.
 */
public class VehicleManager implements OnLocationUpdateListener {

    private static final String PREF_NAME = "v2v_vehicle_prefs";
    private static final String KEY_VEHICLE_ID = "key_vehicle_id";
    private static final String KEY_VEHICLE_TYPE = "key_vehicle_type";

    private static VehicleManager instance;

    private final SharedPreferences prefs;
    private final Vehicle hostVehicle;
    private IGpsProvider gpsProvider;
    private final List<VehicleStatusListener> listeners = new ArrayList<>();

    private VehicleManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String savedId = prefs.getString(KEY_VEHICLE_ID, null);
        String savedType = prefs.getString(KEY_VEHICLE_TYPE, VehicleType.CAR.name());

        VehicleType type = VehicleType.fromString(savedType);
        if (savedId == null) {
            savedId = generateUniqueVehicleId(type);
            prefs.edit().putString(KEY_VEHICLE_ID, savedId).apply();
        }

        this.hostVehicle = new Vehicle(savedId, type);

        // Default to MockGpsProvider for seamless indoor and bench testing
        this.gpsProvider = new MockGpsProvider();
        this.gpsProvider.setListener(this);
        this.gpsProvider.start();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new VehicleManager(context);
        }
    }

    public static synchronized VehicleManager getInstance() {
        return instance;
    }

    public Vehicle getHostVehicle() {
        return hostVehicle;
    }

    public void addListener(VehicleStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(VehicleStatusListener listener) {
        listeners.remove(listener);
    }

    public void setVehicleType(VehicleType type) {
        if (type == null) return;
        hostVehicle.setVehicleType(type);
        prefs.edit().putString(KEY_VEHICLE_TYPE, type.name()).apply();
        notifyProfileChanged();
    }

    public void regenerateVehicleId() {
        String newId = generateUniqueVehicleId(hostVehicle.getVehicleType());
        hostVehicle.setVehicleId(newId);
        prefs.edit().putString(KEY_VEHICLE_ID, newId).apply();
        notifyProfileChanged();
    }

    public void setSpeed(float speedKmh) {
        hostVehicle.setSpeedKmh(speedKmh);
        if (gpsProvider instanceof MockGpsProvider) {
            ((MockGpsProvider) gpsProvider).setSpeedKmh(speedKmh);
        }
        for (VehicleStatusListener l : listeners) {
            l.onSpeedChanged(speedKmh);
        }
        notifyProfileChanged();
    }

    public void setHeading(float headingDegrees) {
        hostVehicle.setHeadingDegrees(headingDegrees);
        if (gpsProvider instanceof MockGpsProvider) {
            ((MockGpsProvider) gpsProvider).setHeading(headingDegrees);
        }
        for (VehicleStatusListener l : listeners) {
            l.onHeadingChanged(headingDegrees);
        }
        notifyProfileChanged();
    }

    public void setGpsProvider(IGpsProvider newProvider) {
        if (this.gpsProvider != null) {
            this.gpsProvider.stop();
        }
        this.gpsProvider = newProvider;
        if (this.gpsProvider != null) {
            this.gpsProvider.setListener(this);
            this.gpsProvider.start();
        }
    }

    public IGpsProvider getGpsProvider() {
        return gpsProvider;
    }

    private String generateUniqueVehicleId(VehicleType type) {
        int randomNum = 1000 + new Random().nextInt(9000);
        return "V2V-" + (type != null ? type.name() : "CAR") + "-" + randomNum;
    }

    private void notifyProfileChanged() {
        for (VehicleStatusListener l : listeners) {
            l.onVehicleProfileChanged(hostVehicle);
        }
    }

    @Override
    public void onLocationUpdated(GeoLocation location) {
        if (location == null) return;
        hostVehicle.setLatitude(location.getLatitude());
        hostVehicle.setLongitude(location.getLongitude());
        if (location.getSpeedKmh() > 0.1f) {
            hostVehicle.setSpeedKmh(location.getSpeedKmh());
        }
        if (location.getHeadingDegrees() > 0.0f) {
            hostVehicle.setHeadingDegrees(location.getHeadingDegrees());
        }
        notifyProfileChanged();
    }

    @Override
    public void onGpsStatusChanged(boolean isLocked, String providerName) {
        // Can be used to notify status
    }
}
