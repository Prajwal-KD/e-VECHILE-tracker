package com.v2v.smartsystem.hardware;

import com.v2v.smartsystem.model.HardwarePacket;

import org.json.JSONObject;

/**
 * Protocol parser for microcontroller serial streams (supports key-value CSV and JSON formats).
 */
public class HardwareProtocolParser {

    /**
     * Parses a raw line of text from the serial link into a HardwarePacket.
     */
    public static HardwarePacket parseIncomingLine(String line) {
        if (line == null) return null;
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return null;

        HardwarePacket packet = new HardwarePacket();
        packet.setRawData(trimmed);

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            // JSON format
            try {
                JSONObject obj = new JSONObject(trimmed);
                packet.setBrakePressed(obj.optInt("brake", 0) == 1 || obj.optBoolean("brake", false));
                packet.setTurnSwitch(obj.optString("turn", "OFF").toUpperCase());
                packet.setGpsFix(obj.optBoolean("gps_fix", false));
                packet.setLatitude(obj.optDouble("lat", 0.0));
                packet.setLongitude(obj.optDouble("lon", 0.0));
                packet.setSpeedKmh((float) obj.optDouble("spd", 0.0));
                packet.setBoardVoltage((float) obj.optDouble("v", 5.0));
                return packet;
            } catch (Exception e) {
                // Fallback to text parsing
            }
        }

        // CSV Key-Value format: $V2V_HW,BRAKE=1,TURN=L,SPD=45.0,LAT=12.97,LON=77.59,V=5.0
        if (trimmed.startsWith("$V2V_HW") || trimmed.startsWith("$V2V")) {
            String[] tokens = trimmed.split(",");
            for (String token : tokens) {
                String[] kv = token.split("=");
                if (kv.length == 2) {
                    String key = kv[0].trim().toUpperCase();
                    String val = kv[1].trim().toUpperCase();
                    switch (key) {
                        case "BRAKE":
                            packet.setBrakePressed(val.equals("1") || val.equals("TRUE") || val.equals("ON"));
                            break;
                        case "TURN":
                            if (val.equals("L") || val.equals("LEFT")) packet.setTurnSwitch("LEFT");
                            else if (val.equals("R") || val.equals("RIGHT")) packet.setTurnSwitch("RIGHT");
                            else if (val.equals("H") || val.equals("HAZARD")) packet.setTurnSwitch("HAZARD");
                            else packet.setTurnSwitch("OFF");
                            break;
                        case "SPD":
                        case "SPEED":
                            try { packet.setSpeedKmh(Float.parseFloat(val)); } catch (Exception ignored) {}
                            break;
                        case "LAT":
                            try { packet.setLatitude(Double.parseDouble(val)); } catch (Exception ignored) {}
                            break;
                        case "LON":
                            try { packet.setLongitude(Double.parseDouble(val)); } catch (Exception ignored) {}
                            break;
                        case "GPS":
                            packet.setGpsFix(val.equals("1") || val.equals("FIX"));
                            break;
                        case "V":
                            try { packet.setBoardVoltage(Float.parseFloat(val)); } catch (Exception ignored) {}
                            break;
                    }
                }
            }
            return packet;
        }

        return null;
    }

    /**
     * Builds an alert actuation command sent from Android back to the Arduino/ESP32.
     * @param buzzerOn Turn physical piezo buzzer on or off.
     * @param ledState "RED", "YELLOW", "OFF".
     */
    public static String buildActuatorCommand(boolean buzzerOn, String ledState) {
        return "#ALERT,BUZZER=" + (buzzerOn ? "ON" : "OFF") + ",LED=" + (ledState != null ? ledState : "OFF") + "\n";
    }
}
