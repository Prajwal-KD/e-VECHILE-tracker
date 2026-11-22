package com.v2v.smartsystem.model;

/**
 * Encapsulates telemetry streamed from the physical vehicle hardware (ESP32/Arduino).
 */
public class HardwarePacket {
    private boolean brakePressed;
    private String turnSwitch; // "OFF", "LEFT", "RIGHT", "HAZARD"
    private boolean gpsFix;
    private double latitude;
    private double longitude;
    private float speedKmh;
    private float boardVoltage;
    private long timestamp;
    private String rawData;

    public HardwarePacket() {
        this.timestamp = System.currentTimeMillis();
        this.turnSwitch = "OFF";
        this.boardVoltage = 5.0f;
    }

    public boolean isBrakePressed() { return brakePressed; }
    public void setBrakePressed(boolean brakePressed) { this.brakePressed = brakePressed; }

    public String getTurnSwitch() { return turnSwitch; }
    public void setTurnSwitch(String turnSwitch) { this.turnSwitch = turnSwitch; }

    public boolean hasGpsFix() { return gpsFix; }
    public void setGpsFix(boolean gpsFix) { this.gpsFix = gpsFix; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(float speedKmh) { this.speedKmh = speedKmh; }

    public float getBoardVoltage() { return boardVoltage; }
    public void setBoardVoltage(float boardVoltage) { this.boardVoltage = boardVoltage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
}
