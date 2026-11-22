package com.v2v.smartsystem.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Universal packet exchanged over the V2V ad-hoc wireless network.
 * Compatible with JSON serialization for easy network transmission and debugging.
 */
public class V2VMessage {
    private String messageId;
    private String senderVehicleId;
    private VehicleType vehicleType;
    private MessageType messageType;
    private long timestamp;
    private double latitude;
    private double longitude;
    private float speedKmh;
    private float headingDegrees;
    private boolean emergencyBrakeActive;
    private String turnSignal; // "NONE", "LEFT", "RIGHT", "HAZARD"
    private String payloadText;
    private int priority;

    public V2VMessage() {
        this.messageId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = System.currentTimeMillis();
        this.turnSignal = "NONE";
        this.payloadText = "";
    }

    public V2VMessage(String senderVehicleId, VehicleType vehicleType, MessageType messageType,
                      double latitude, double longitude, float speedKmh, float headingDegrees) {
        this();
        this.senderVehicleId = senderVehicleId;
        this.vehicleType = vehicleType;
        this.messageType = messageType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.headingDegrees = headingDegrees;
        this.priority = messageType.getPriority();
    }

    /**
     * Serializes this message to a compact JSON string for UDP broadcast.
     */
    public String toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("mid", messageId);
            obj.put("vid", senderVehicleId);
            obj.put("vtype", vehicleType != null ? vehicleType.name() : VehicleType.CAR.name());
            obj.put("mtype", messageType != null ? messageType.name() : MessageType.BEACON_HEARTBEAT.name());
            obj.put("ts", timestamp);
            obj.put("lat", latitude);
            obj.put("lon", longitude);
            obj.put("spd", (double) speedKmh);
            obj.put("hdg", (double) headingDegrees);
            obj.put("brake", emergencyBrakeActive);
            obj.put("turn", turnSignal);
            obj.put("text", payloadText != null ? payloadText : "");
            obj.put("prio", priority);
            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Deserializes a JSON string received over the network into a V2VMessage object.
     */
    public static V2VMessage fromJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return null;
        try {
            JSONObject obj = new JSONObject(jsonStr);
            V2VMessage msg = new V2VMessage();
            msg.setMessageId(obj.optString("mid", UUID.randomUUID().toString().substring(0, 8)));
            msg.setSenderVehicleId(obj.optString("vid", "UNKNOWN"));
            msg.setVehicleType(VehicleType.fromString(obj.optString("vtype", "CAR")));
            msg.setMessageType(MessageType.fromString(obj.optString("mtype", "BEACON_HEARTBEAT")));
            msg.setTimestamp(obj.optLong("ts", System.currentTimeMillis()));
            msg.setLatitude(obj.optDouble("lat", 0.0));
            msg.setLongitude(obj.optDouble("lon", 0.0));
            msg.setSpeedKmh((float) obj.optDouble("spd", 0.0));
            msg.setHeadingDegrees((float) obj.optDouble("hdg", 0.0));
            msg.setEmergencyBrakeActive(obj.optBoolean("brake", false));
            msg.setTurnSignal(obj.optString("turn", "NONE"));
            msg.setPayloadText(obj.optString("text", ""));
            msg.setPriority(obj.optInt("prio", 1));
            return msg;
        } catch (JSONException e) {
            return null;
        }
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderVehicleId() { return senderVehicleId; }
    public void setSenderVehicleId(String senderVehicleId) { this.senderVehicleId = senderVehicleId; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

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

    public String getPayloadText() { return payloadText; }
    public void setPayloadText(String payloadText) { this.payloadText = payloadText; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
