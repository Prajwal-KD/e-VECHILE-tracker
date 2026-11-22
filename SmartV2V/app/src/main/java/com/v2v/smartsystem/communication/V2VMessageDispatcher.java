package com.v2v.smartsystem.communication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.v2v.smartsystem.model.MessageType;
import com.v2v.smartsystem.model.V2VMessage;
import com.v2v.smartsystem.model.Vehicle;
import com.v2v.smartsystem.model.VehicleType;
import com.v2v.smartsystem.safety.SafetyActionController;
import com.v2v.smartsystem.safety.WarningSystem;
import com.v2v.smartsystem.utils.DistanceCalculator;
import com.v2v.smartsystem.vehicle.VehicleManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central orchestrator for all V2V message broadcasting, 1 Hz beacon scheduling,
 * message deduplication, and nearby vehicle tracking.
 */
public class V2VMessageDispatcher implements OnMessageReceivedListener {

    public interface DispatcherEventListener {
        void onPacketDispatched(V2VMessage message, boolean isOutgoing);
        void onNearbyVehiclesUpdated(List<Vehicle> vehicles);
    }

    private static V2VMessageDispatcher instance;

    private final Context context;
    private final UdpBroadcastCommunicator udpCommunicator;
    private final LoopbackCommunicator loopbackCommunicator;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Map<String, Vehicle> nearbyVehicles = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<DispatcherEventListener> listeners = new ArrayList<>();

    private boolean isBroadcasting = false;

    private final Runnable beaconRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBroadcasting) return;

            // Broadcast 1 Hz Cooperative Awareness Beacon
            broadcastCurrentState(MessageType.BEACON_HEARTBEAT, "");

            // Prune vehicles that haven't been heard from in 6 seconds
            pruneStaleVehicles();

            handler.postDelayed(this, 1000);
        }
    };

    private V2VMessageDispatcher(Context context) {
        this.context = context.getApplicationContext();
        this.udpCommunicator = new UdpBroadcastCommunicator(this.context);
        this.loopbackCommunicator = new LoopbackCommunicator();

        this.udpCommunicator.setOnMessageReceivedListener(this);
        this.loopbackCommunicator.setOnMessageReceivedListener(this);

        // Bind SafetyActionController triggers so emergency braking immediately broadcasts
        SafetyActionController.getInstance().setBroadcastTriggers(
                () -> broadcastCurrentState(MessageType.EMERGENCY_BRAKE, "Emergency braking applied!"),
                () -> broadcastCurrentState(MessageType.TURN_INDICATOR, "Turn signal changed")
        );
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new V2VMessageDispatcher(context);
        }
    }

    public static synchronized V2VMessageDispatcher getInstance() {
        return instance;
    }

    public void addListener(DispatcherEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DispatcherEventListener listener) {
        listeners.remove(listener);
    }

    public void start() {
        if (isBroadcasting) return;
        isBroadcasting = true;
        udpCommunicator.start();
        loopbackCommunicator.start();
        handler.post(beaconRunnable);
    }

    public void stop() {
        isBroadcasting = false;
        handler.removeCallbacks(beaconRunnable);
        udpCommunicator.stop();
        loopbackCommunicator.stop();
    }

    public boolean isBroadcasting() {
        return isBroadcasting;
    }

    /**
     * Broadcasts the current host vehicle telemetry with specified message type.
     */
    public void broadcastCurrentState(MessageType type, String payloadText) {
        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host == null) return;

        V2VMessage msg = new V2VMessage(
                host.getVehicleId(),
                host.getVehicleType(),
                type,
                host.getLatitude(),
                host.getLongitude(),
                host.getSpeedKmh(),
                host.getHeadingDegrees()
        );
        msg.setEmergencyBrakeActive(SafetyActionController.getInstance().isEmergencyBrakeActive());
        msg.setTurnSignal(SafetyActionController.getInstance().getTurnSignalState());
        msg.setPayloadText(payloadText);

        // Transmit via UDP
        udpCommunicator.broadcastMessage(msg);

        // Notify UI of outgoing packet
        for (DispatcherEventListener l : listeners) {
            l.onPacketDispatched(msg, true);
        }
    }

    /**
     * Injects a synthetic remote vehicle for test demonstrations.
     */
    public void simulateNearbyHazardVehicle(String type, double distanceMeters, boolean slamBrakes) {
        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host == null) return;

        // Position synthetic vehicle slightly ahead of host vehicle
        double rEarth = 6371000.0;
        double headingRad = Math.toRadians(host.getHeadingDegrees());
        double deltaLat = (distanceMeters * Math.cos(headingRad)) / rEarth;
        double deltaLon = (distanceMeters * Math.sin(headingRad)) / (rEarth * Math.cos(Math.toRadians(host.getLatitude())));

        V2VMessage simMsg = new V2VMessage(
                "V2V-" + type.toUpperCase() + "-7129",
                VehicleType.fromString(type),
                slamBrakes ? MessageType.EMERGENCY_BRAKE : MessageType.BEACON_HEARTBEAT,
                host.getLatitude() + Math.toDegrees(deltaLat),
                host.getLongitude() + Math.toDegrees(deltaLon),
                slamBrakes ? 0.0f : 55.0f,
                host.getHeadingDegrees()
        );
        simMsg.setEmergencyBrakeActive(slamBrakes);
        simMsg.setPayloadText(slamBrakes ? "Sudden stop hazard" : "Routine beacon");

        loopbackCommunicator.injectSimulatedMessage(simMsg);
    }

    @Override
    public void onMessageReceived(V2VMessage message, String sourceIp) {
        if (message == null) return;
        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host != null && message.getSenderVehicleId().equals(host.getVehicleId())) {
            return; // Ignore own echoes
        }

        // Calculate relative geometry
        if (host != null) {
            double distance = DistanceCalculator.calculateDistanceMeters(
                    host.getLatitude(), host.getLongitude(),
                    message.getLatitude(), message.getLongitude()
            );
            double bearing = DistanceCalculator.calculateBearingDegrees(
                    host.getLatitude(), host.getLongitude(),
                    message.getLatitude(), message.getLongitude()
            );
            double relBearing = DistanceCalculator.calculateRelativeBearing(host.getHeadingDegrees(), bearing);

            // Update or register vehicle
            Vehicle remote = nearbyVehicles.get(message.getSenderVehicleId());
            if (remote == null) {
                remote = new Vehicle(message.getSenderVehicleId(), message.getVehicleType());
                nearbyVehicles.put(message.getSenderVehicleId(), remote);
            }
            remote.updateFromMessage(message);
            remote.setDistanceMeters(distance);
            remote.setRelativeBearing(relBearing);
        }

        // Forward to WarningSystem evaluation
        WarningSystem.getInstance().evaluateMessage(message);

        // Notify UI
        notifyVehiclesUpdated();
        for (DispatcherEventListener l : listeners) {
            l.onPacketDispatched(message, false);
        }
    }

    private void pruneStaleVehicles() {
        long now = System.currentTimeMillis();
        boolean changed = nearbyVehicles.entrySet().removeIf(entry -> (now - entry.getValue().getLastSeenTimestamp()) > 6000);
        if (changed) {
            notifyVehiclesUpdated();
        }
    }

    private void notifyVehiclesUpdated() {
        List<Vehicle> list = new ArrayList<>(nearbyVehicles.values());
        for (DispatcherEventListener l : listeners) {
            l.onNearbyVehiclesUpdated(list);
        }
    }

    public List<Vehicle> getNearbyVehiclesList() {
        return new ArrayList<>(nearbyVehicles.values());
    }
}
