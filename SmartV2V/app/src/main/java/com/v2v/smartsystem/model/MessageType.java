package com.v2v.smartsystem.model;

/**
 * Categorization of V2V messages exchanged over the vehicular ad-hoc network.
 */
public enum MessageType {
    BEACON_HEARTBEAT("BEACON", 1),
    TURN_INDICATOR("TURN", 2),
    HAZARD_ALERT("HAZARD", 3),
    EMERGENCY_BRAKE("EMERGENCY_BRAKE", 4),
    CUSTOM_ALERT("ALERT", 2);

    private final String code;
    private final int priority; // Higher means more critical

    MessageType(String code, int priority) {
        this.code = code;
        this.priority = priority;
    }

    public String getCode() {
        return code;
    }

    public int getPriority() {
        return priority;
    }

    public static MessageType fromString(String code) {
        if (code == null) return BEACON_HEARTBEAT;
        for (MessageType mt : values()) {
            if (mt.name().equalsIgnoreCase(code) || mt.code.equalsIgnoreCase(code)) {
                return mt;
            }
        }
        return BEACON_HEARTBEAT;
    }
}
