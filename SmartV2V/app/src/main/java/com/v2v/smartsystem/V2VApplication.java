package com.v2v.smartsystem;

import android.app.Application;

import com.v2v.smartsystem.communication.V2VMessageDispatcher;
import com.v2v.smartsystem.hardware.HardwareManager;
import com.v2v.smartsystem.safety.SafetyActionController;
import com.v2v.smartsystem.safety.WarningSystem;
import com.v2v.smartsystem.vehicle.VehicleManager;

/**
 * Application entry point initializing all modular subsystems on startup.
 */
public class V2VApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Initialize Vehicle State & Profile
        VehicleManager.init(this);

        // 2. Initialize Hardware Abstraction Layer
        HardwareManager.getInstance();

        // 3. Initialize Safety & Warning Engine
        WarningSystem.init(this);

        // 4. Initialize Safety Action Controller (Brakes & Turn Signals)
        SafetyActionController.getInstance();

        // 5. Initialize V2V Communication Network Dispatcher
        V2VMessageDispatcher.init(this);
    }
}
