package com.v2v.smartsystem.safety;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.v2v.smartsystem.hardware.HardwareManager;
import com.v2v.smartsystem.model.MessageType;
import com.v2v.smartsystem.model.V2VMessage;
import com.v2v.smartsystem.model.Vehicle;
import com.v2v.smartsystem.utils.AudioAlertManager;
import com.v2v.smartsystem.utils.DistanceCalculator;
import com.v2v.smartsystem.vehicle.VehicleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent V2V Collision & Hazard Warning System.
 * Evaluates spatial geometry, closing speeds, and alert messages to warn drivers in real time.
 */
public class WarningSystem {

    private static WarningSystem instance;

    private final Context context;
    private final AudioAlertManager audioAlertManager;
    private final List<WarningListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WarningEvent currentActiveWarning;

    private WarningSystem(Context context) {
        this.context = context.getApplicationContext();
        this.audioAlertManager = new AudioAlertManager(this.context);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new WarningSystem(context);
        }
    }

    public static synchronized WarningSystem getInstance() {
        return instance;
    }

    public void addListener(WarningListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(WarningListener listener) {
        listeners.remove(listener);
    }

    public WarningEvent getCurrentActiveWarning() {
        return currentActiveWarning;
    }

    /**
     * Evaluates an incoming V2V message against the host vehicle's kinematics.
     */
    public void evaluateMessage(V2VMessage message) {
        if (message == null) return;
        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host == null || message.getSenderVehicleId().equals(host.getVehicleId())) {
            return; // Ignore own echoes
        }

        // Calculate spatial metrics
        double distance = DistanceCalculator.calculateDistanceMeters(
                host.getLatitude(), host.getLongitude(),
                message.getLatitude(), message.getLongitude()
        );

        double absBearing = DistanceCalculator.calculateBearingDegrees(
                host.getLatitude(), host.getLongitude(),
                message.getLatitude(), message.getLongitude()
        );

        double relBearing = DistanceCalculator.calculateRelativeBearing(host.getHeadingDegrees(), absBearing);
        boolean isInFront = Math.abs(relBearing) <= 65.0; // Within front 130° cone

        // 1. Emergency Brake Warning
        if (message.isEmergencyBrakeActive() || message.getMessageType() == MessageType.EMERGENCY_BRAKE) {
            if (distance < 120.0) {
                WarningEvent.Severity severity = (distance < 50.0) ? WarningEvent.Severity.CRITICAL : WarningEvent.Severity.WARNING;
                String desc = String.format("Vehicle (%s, %s) at %.0fm slammed brakes!",
                        message.getVehicleType().getDisplayName(), message.getSenderVehicleId(), distance);

                triggerWarning(new WarningEvent(
                        "EB_" + message.getSenderVehicleId(),
                        "CRITICAL: Emergency Brake Ahead!",
                        desc,
                        severity,
                        message.getSenderVehicleId(),
                        distance
                ));
                return;
            }
        }

        // 2. Road Hazard Alert
        if (message.getMessageType() == MessageType.HAZARD_ALERT) {
            if (distance < 150.0) {
                String desc = String.format("Road hazard reported by %s at %.0fm: %s",
                        message.getSenderVehicleId(), distance,
                        message.getPayloadText().isEmpty() ? "Obstruction in lane" : message.getPayloadText());

                triggerWarning(new WarningEvent(
                        "HZ_" + message.getSenderVehicleId(),
                        "WARNING: Road Hazard Ahead",
                        desc,
                        WarningEvent.Severity.WARNING,
                        message.getSenderVehicleId(),
                        distance
                ));
                return;
            }
        }

        // 3. Lane Merge / Turn Conflict
        if (!"NONE".equals(message.getTurnSignal()) && !"OFF".equals(message.getTurnSignal())) {
            if (distance < 45.0) {
                String desc = String.format("Nearby %s (%s) indicating %s turn!",
                        message.getVehicleType().getDisplayName(), message.getSenderVehicleId(), message.getTurnSignal());

                triggerWarning(new WarningEvent(
                        "TURN_" + message.getSenderVehicleId(),
                        "CAUTION: Turning Vehicle Ahead",
                        desc,
                        WarningEvent.Severity.INFO,
                        message.getSenderVehicleId(),
                        distance
                ));
                return;
            }
        }

        // 4. Forward Collision Warning (FCW) based on Time-To-Collision
        if (isInFront && distance < 80.0 && host.getSpeedKmh() > 20.0f) {
            float relSpeedKmh = host.getSpeedKmh() - message.getSpeedKmh();
            if (relSpeedKmh > 15.0f) { // Closing in faster
                double ttcSeconds = DistanceCalculator.calculateTimeToCollision(distance, (relSpeedKmh / 3.6));
                if (ttcSeconds <= 3.5) {
                    triggerWarning(new WarningEvent(
                            "FCW_" + message.getSenderVehicleId(),
                            "CRITICAL: Forward Collision Risk!",
                            String.format("TTC: %.1fs to vehicle ahead at %.0fm!", ttcSeconds, distance),
                            WarningEvent.Severity.CRITICAL,
                            message.getSenderVehicleId(),
                            distance
                    ));
                }
            }
        }
    }

    private void triggerWarning(final WarningEvent event) {
        this.currentActiveWarning = event;

        mainHandler.post(() -> {
            // Cabin audio & vibration
            if (event.getSeverity() == WarningEvent.Severity.CRITICAL) {
                audioAlertManager.playCriticalAlert();
                // Send alert to external physical hardware (piezo buzzer & red strobe LED)
                HardwareManager.getInstance().triggerHardwareAlert(true, "RED");
            } else {
                audioAlertManager.playWarningAlert();
                HardwareManager.getInstance().triggerHardwareAlert(true, "YELLOW");
            }

            for (WarningListener l : listeners) {
                l.onWarningTriggered(event);
            }
        });
    }

    public void clearActiveWarning() {
        if (currentActiveWarning != null) {
            final String id = currentActiveWarning.getWarningId();
            currentActiveWarning = null;
            // Silence hardware buzzer
            HardwareManager.getInstance().triggerHardwareAlert(false, "OFF");
            mainHandler.post(() -> {
                for (WarningListener l : listeners) {
                    l.onWarningCleared(id);
                }
            });
        }
    }
}
