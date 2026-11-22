# Smart V2V Communication System - Project Checkpoint & Roadmap

**Date & Time Logged:** 2026-11-21  
**Project Location:** `c:\Users\prajw\OneDrive\Desktop\my works\SmartV2V`  
**Status:** Saved & Checkpointed for Next Session  

---

## 1. Project Goal & Architecture
A cyber-physical Vehicle-to-Vehicle (V2V) Communication System on Android (Java) integrated with external vehicular hardware (ESP32 / Arduino + HC-05 + NEO-6M GPS + Brake switch + Turn indicator switches + Warning Buzzer/LED).

Nearby vehicles exchange real-time Cooperative Awareness Messages (CAM/BSM) over UDP broadcast / Wi-Fi / Hotspot on port 8888, detecting forward collision risks, emergency braking, turn maneuvers, and road hazards.

---

## 2. Completed Files & Infrastructure (Ready & Saved on Disk)

### A. Android Build Configuration
- `settings.gradle` - Project definitions & repository settings
- `build.gradle` (Root) - Android Gradle Plugin 8.2.2
- `gradle.properties` - JVM args & AndroidX configurations
- `gradle/wrapper/gradle-wrapper.properties` - Gradle 8.2 wrapper
- `app/build.gradle` - CompileSdk 34, MinSdk 24, Material3, ViewBinding, CardView, RecyclerView
- `app/proguard-rules.pro` - Optimization and release rules
- `app/src/main/AndroidManifest.xml` - UDP network, Bluetooth SPP (HC-05/ESP32), Location, and Vibration permissions
- `app/src/main/res/xml/backup_rules.xml` & `data_extraction_rules.xml`

### B. User Interface & Assets
- `res/values/colors.xml` - Automotive dark cockpit palette (Obsidian, Cyan, Warning Amber, Critical Red, Safe Green)
- `res/values/strings.xml` - All UI text, alerts, vehicle types, and hardware labels
- `res/values/themes.xml` - Material3 Dark NoActionBar theme
- `res/drawable/` - Vector drawables for:
  - Vehicles: `ic_car.xml`, `ic_bike.xml`, `ic_truck.xml`, `ic_bus.xml`
  - Safety & Controls: `ic_brake.xml`, `ic_turn_left.xml`, `ic_turn_right.xml`, `ic_hazard.xml`, `ic_warning.xml`
  - Telemetry: `ic_bluetooth.xml`, `ic_gps.xml`, `ic_radar.xml`
  - Backgrounds: `bg_card.xml`, `bg_pill.xml`, `bg_alert_banner.xml`
- `res/mipmap-anydpi-v26/` - App launcher icons (`ic_launcher.xml`, `ic_launcher_round.xml`)
- `res/layout/activity_main.xml` - Full dashboard:
  - Host vehicle profile (ID, Type, Speed slider, Heading, GPS coords)
  - Hardware ECU interface card (Live indicators: HW Brake Pin, HW Turn Switch, HW Buzzer/LED)
  - Active safety warning banner (flashing red alert with sound)
  - Safety action controls (Emergency Brake toggle, Left/Hazard/Right turn signals)
  - V2V Spatial Radar display
  - Nearby detected vehicles list
  - Real-time V2V message log stream
- `res/layout/item_nearby_vehicle.xml` - Recycler item for detected vehicles
- `res/layout/item_message_log.xml` - Recycler item for live V2V packet stream
- `res/layout/dialog_hardware_config.xml` - Dialog for Bluetooth hardware pairing and test bench
- `res/layout/dialog_vehicle_type.xml` - Dialog for selecting vehicle type and regenerating vehicle ID

### C. Java Models Started
- `model/VehicleType.java` - Enum for Car, Bike, Truck, Bus with deceleration dynamics and icons
- `model/MessageType.java` - Enum for BEACON, EMERGENCY_BRAKE, HAZARD, TURN, CUSTOM_ALERT

---

## 3. What to Do Tomorrow (Exact Step-by-Step Execution Plan)

### Step 1: Complete Remaining Data Models
- `model/V2VMessage.java`: Universal JSON-serialized packet (sender ID, vehicle type, message type, lat/lon, speed, heading, flags, timestamp).
- `model/Vehicle.java`: Host & remote vehicle state object.
- `model/HardwarePacket.java`: Telemetry received from microcontroller.

### Step 2: Implement Hardware Interface Module (`hardware/`)
- `IHardwareBridge.java`: Interface for hardware transports.
- `BluetoothHardwareBridge.java`: Real Android Bluetooth RFCOMM/SPP thread for HC-05 / ESP32.
- `HardwareProtocolParser.java`: Parses serial telemetry (`$V2V_HW,BRAKE=1,TURN=L,SPD=...`) and commands (`#ALERT,BUZZER=ON...`).
- `VirtualHardwareSimulator.java`: Testbench to simulate hardware inputs when physical board is disconnected.
- `HardwareManager.java`: Coordinates hardware lifecycle, triggers V2V alerts on hardware brake press, and drives physical buzzer on received danger.

### Step 3: Implement GPS & Telemetry Module (`gps/`)
- `IGpsProvider.java` & `GeoLocation.java`
- `MockGpsProvider.java`: Realistic vehicle movement simulator.
- `AndroidLocationProvider.java`: Native device GPS.
- `HardwareGpsProvider.java`: Pipes external NEO-6M GPS from microcontroller into the app.

### Step 4: Implement Vehicle State & Safety Engine (`vehicle/` & `safety/`)
- `VehicleManager.java`: Persistent ID generation (`V2V-XXX-####`), vehicle type updates.
- `WarningSystem.java`: Collision detection, Haversine distance, relative bearing, Time-To-Collision (TTC) calculations.
- `SafetyActionController.java`: Emergency brake & turn signal state coordination.

### Step 5: Implement Communication Layer (`communication/`)
- `UdpBroadcastCommunicator.java`: Peer-to-peer UDP broadcast over Wi-Fi / Hotspot LAN with MulticastLock on port 8888.
- `V2VMessageDispatcher.java`: 1 Hz heartbeat scheduler, priority broadcast trigger, message deduplicator.

### Step 6: Implement UI Adapters & Radar View (`ui/`)
- `RadarView.java`: Custom canvas drawing circular radar sweeps and nearby vehicles with danger coloring.
- `NearbyVehiclesAdapter.java` & `MessageLogAdapter.java`.
- `MainActivity.java`: Connects all modules into the interactive cockpit UI.
- `V2VApplication.java`: Application bootstrap.

### Step 7: Write Microcontroller Firmware (`arduino_esp32_hardware_code/`)
- `v2v_hardware_node.ino`: Dual-mode sketch for ESP32 (built-in Bluetooth) and Arduino Uno + HC-05.
- Interfaces: Brake push button / switch, Turn switch, NEO-6M GPS, Piezo buzzer, and warning LEDs.

### Step 8: Create the Comprehensive Hardware Instruction Manual
- `HARDWARE_INSTRUCTION_MANUAL.md`:
  - Complete Bill of Materials (components, pins, ratings).
  - Pin-to-pin wiring diagram and color-coded table.
  - Arduino IDE setup and flashing instructions.
  - Bluetooth pairing and system testing guide.
