package com.v2v.smartsystem.model;

/**
 * Represents a vehicle entity in the V2V network (either the host vehicle or a nearby remote vehicle).
 */
public class Vehicle {
    private String vehicleId;
    private VehicleType vehicleType;
    private double latitude;
    private double longitude;
    private float speedKmh;
    private float headingDegrees;
    private boolean emergencyBrakeActive;
    private String turnSignal; // "NONE", "LEFT", "RIGHT", "HAZARD"
    private boolean hazardActive;
    private long lastSeenTimestamp;
    private double distanceMeters;
    private double relativeBearing; // angle relative to host heading (-180 to +180)

    public Vehicle(String vehicleId, VehicleType vehicleType) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType != null ? vehicleType : VehicleType.CAR;
        this.turnSignal = "NONE";
        this.lastSeenTimestamp = System.currentTimeMillis();
    }

    /**
     * Updates telemetry from a received V2VMessage packet.
     */
    public void updateFromMessage(V2VMessage message) {
        if (message == null) return;
        this.vehicleType = message.getVehicleType();
        this.latitude = message.getLatitude();
        this.longitude = message.getLongitude();
        this.speedKmh = message.getSpeedKmh();
        this.headingDegrees = message.getHeadingDegrees();
        this.emergencyBrakeActive = message.isEmergencyBrakeActive();
        this.turnSignal = message.getTurnSignal();
        if (message.getMessageType() == MessageType.HAZARD_ALERT) {
            this.hazardActive = true;
        } else if (message.getMessageType() == MessageType.BEACON_HEARTBEAT && !message.isEmergencyBrakeActive()) {
            this.hazardActive = false;
        }
        this.lastSeenTimestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(float speedKmh) { this.speedKmh = speedKmh; }

    public float getHeadingDegrees() { return headingDegrees; }
    public void setHeadingDegrees(float headingDegrees) { this.headingDegrees = headingDegrees; }

    public boolean isEmergencyBrakeActive() { return emergencyBrakeActive; }
    public void setEmergencyBrakeActive(boolean emergencyBrakeActive) { this.emergencyBrakeActive = emergencyBrakeActive; }

    public String getTurnSignal() { return turnSignal; }
    public void setTurnSignal(String turnSignal) { this.turnSignal = turnSignal; }

    public boolean isHazardActive() { return hazardActive; }
    public void setHazardActive(boolean hazardActive) { this.hazardActive = hazardActive; }

    public long getLastSeenTimestamp() { return lastSeenTimestamp; }
    public void setLastSeenTimestamp(long lastSeenTimestamp) { this.lastSeenTimestamp = lastSeenTimestamp; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public double getRelativeBearing() { return relativeBearing; }
    public void setRelativeBearing(double relativeBearing) { this.relativeBearing = relativeBearing; }
}
