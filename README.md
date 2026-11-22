# Smart V2V Communication System

[![Platform: Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-brightgreen.svg)](https://developer.android.com/)
[![Language: Java](https://img.shields.io/badge/Language-Java%208%2F17-orange.svg)](https://www.java.com/)
[![Hardware: ESP32 / Arduino](https://img.shields.io/badge/Hardware-ESP32%20%7C%20Arduino%20Uno-blue.svg)](https://espressif.com/)
[![Protocol: UDP Broadcast](https://img.shields.io/badge/V2V%20Network-UDP%20Broadcast%20(Port%208888)-blueviolet.svg)](#)
[![Connection: Bluetooth SPP](https://img.shields.io/badge/ECU%20Link-Bluetooth%20RFCOMM%20SPP-cyan.svg)](#)

A Cyber-Physical **Vehicle-to-Vehicle (V2V) Communication & Road Safety System** that bridges physical on-board automotive hardware (sensors, switches, GPS, and acoustic/visual warning actuators) with an Android-based vehicular cockpit dashboard.

Vehicles autonomously exchange real-time **Cooperative Awareness Messages (CAM / BSM)** over peer-to-peer Wi-Fi / Hotspot UDP broadcast on port `8888`, detecting forward collision threats, emergency braking ahead, turn conflicts, and road hazards without relying on cellular networks or central cloud servers.

---

## 1. System Blueprint & Architecture

The system operates across three tightly integrated layers:

```
+-----------------------------------------------------------------------------------+
|                        PHYSICAL VEHICULAR HARDWARE (ECU)                          |
|                                                                                   |
|  [Brake Pedal Button]   [Turn Switches (L/R)]   [NEO-6M GPS]   [5V Piezo Buzzer]  |
|       (GPIO 4)             (GPIO 18 / 19)        (RX2 / TX2)        (GPIO 23)     |
|          |                        |                   |                 ^         |
|          +------------------------+-------------------+                 |         |
|                                   |                                     |         |
|                         [ESP32 Microcontroller]                         |         |
|                                   |                                     |         |
|                      Bluetooth Classic SPP (RFCOMM)                     |         |
+-----------------------------------|-------------------------------------|---------+
                                    | ($V2V_HW Telemetry)                 | (#ALERT)
                                    v                                     |
+-------------------------------------------------------------------------|---------+
|                         ANDROID SMARTPHONE / TABLET                     |         |
|                                                                         |         |
|  +---------------------------+       +-----------------------------+    |         |
|  |     Hardware Manager      | ----> |   SafetyActionController    |    |         |
|  | (BluetoothHardwareBridge) |       |  (Emergency Brake / Turn)   |    |         |
|  +---------------------------+       +-----------------------------+    |         |
|               |                                     |                   |         |
|               v                                     v                   |         |
|  +---------------------------+       +-----------------------------+    |         |
|  |    Location / GPS Layer   |       |    V2V Message Dispatcher   |    |         |
|  |  (Hardware/Android/Mock)  | ----> | (1Hz BSM Beacon Scheduler)  |    |         |
|  +---------------------------+       +-----------------------------+    |         |
|               |                                     |                   |         |
|               v                                     v                   |         |
|  +---------------------------+       +-----------------------------+    |         |
|  |       Warning System      |       |  UdpBroadcastCommunicator   |    |         |
|  | (TTC / Haversine Engine)  | <---- |   (Wi-Fi / Hotspot Socket)  |    |         |
|  +---------------------------+       +-----------------------------+    |         |
|         |             |                             |                             |
|         v             v                             v                             |
|  [Cockpit Radar] [Audio Alarms]             [Peer-to-Peer Wi-Fi LAN]              |
|   (RadarView)    (Tone/Vibrate)                    (Port 8888)                    |
+-----------------------------------------------------|-----------------------------+
                                                      |
                                                      | 802.11 UDP Broadcast
                                                      v
                                        +---------------------------+
                                        |  NEARBY VEHICLE B (CAR)   |
                                        |                           |
                                        | - Displays Vehicle on Map |
                                        | - Receives Safety Alert   |
                                        | - Sounds Cabin Alarm      |
                                        +---------------------------+
```

---

## 2. Hardware Bill of Materials (Components Needed)

| Component | Model / Specification | Purpose | Quantity |
|---|---|---|---|
| **Microcontroller (ECU)** | **ESP32 NodeMCU** (30/38-pin) *(or Arduino Uno)* | Reads sensors, processes GPS UART, and connects via Bluetooth SPP. | 1 |
| **Bluetooth Interface** | **Built-in ESP32 Bluetooth** *(or HC-05 for Arduino)* | Wireless RFCOMM Serial Port Profile link to smartphone. | 1 |
| **GPS Receiver** | **u-blox NEO-6M** (with ceramic antenna) | Provides latitude, longitude, and ground speed telemetry. | 1 |
| **Brake Pedal Sensor** | **Momentary Push Button** (Tactile switch) | Physical brake pedal simulator; tripping triggers emergency V2V broadcast. | 1 |
| **Turn Signal Switches** | **SPDT Miniature Toggle Switches** (x2) | Left and right turn signals on the steering column. | 2 |
| **Acoustic Warning Actuator** | **Active 5V Piezo Buzzer** | High-decibel alarm sounding inside the vehicle when danger is detected ahead. | 1 |
| **Visual Warning Strobe** | **5mm Red LED** (+ 220 Ω resistor) | Flashes red emergency strobe on collision alerts. | 1 |
| **System Status Indicator** | **5mm Green LED** (+ 220 Ω resistor) | Indicates ECU power and loop status. | 1 |
| **Breadboard & Wires** | 830-point MB-102 + Jumper wires | Solder-free circuit prototyping. | 1 set |
| **Power Supply** | USB Cable or 9V Battery | Powers the microcontroller and connected sensors. | 1 |

---

## 3. Hardware Circuit Wiring Guide

### ESP32 Pinout (Recommended):
- **Brake Push Button**: Leg 1 $\rightarrow$ **GPIO 4**, Leg 2 $\rightarrow$ **GND** (Configured as `INPUT_PULLUP`).
- **Left Turn Switch**: Pin 1 $\rightarrow$ **GPIO 18**, Pin 2 $\rightarrow$ **GND** (`INPUT_PULLUP`).
- **Right Turn Switch**: Pin 1 $\rightarrow$ **GPIO 19**, Pin 2 $\rightarrow$ **GND** (`INPUT_PULLUP`).
- **Active Piezo Buzzer**: Positive (+) $\rightarrow$ **GPIO 23**, Negative (-) $\rightarrow$ **GND**.
- **Red Warning LED**: Anode (+) $\rightarrow$ **GPIO 5** (via 220 Ω resistor), Cathode (-) $\rightarrow$ **GND**.
- **Green Status LED**: Anode (+) $\rightarrow$ **GPIO 2**, Cathode (-) $\rightarrow$ **GND**.
- **NEO-6M GPS**: VCC $\rightarrow$ **3.3V/5V**, GND $\rightarrow$ **GND**, TX $\rightarrow$ **GPIO 16 (RX2)**, RX $\rightarrow$ **GPIO 17 (TX2)**.

### Arduino Uno + HC-05 Pinout:
- **HC-05 Bluetooth**: VCC $\rightarrow$ 5V, GND $\rightarrow$ GND, TXD $\rightarrow$ Pin 10, RXD $\rightarrow$ Pin 11 (via voltage divider).
- **Brake Button**: Pin 2 $\rightarrow$ GND (`INPUT_PULLUP`).
- **Turn Switches**: Left $\rightarrow$ Pin 3, Right $\rightarrow$ Pin 4 (`INPUT_PULLUP`).
- **Buzzer & Red LED**: Buzzer $\rightarrow$ Pin 5, Red LED $\rightarrow$ Pin 6.

---

## 4. Key Software Features

1. **Unique Vehicle Identity & Profile Selection**:
   - Generates persistent unique IDs (e.g. `V2V-CAR-8419`, `V2V-TRUCK-3122`).
   - Supports 4 vehicle categories with realistic braking dynamics: **Car**, **Bike**, **Truck**, **Bus**.
2. **Dynamic 360° V2V Spatial Radar**:
   - Custom 2D canvas view plotting nearby vehicles in real-time according to relative bearing and distance (50m, 100m, 150m range rings).
   - Vehicles with emergency brakes active are highlighted with pulsating red danger rings.
3. **Multi-Source GPS Integration**:
   - Seamlessly switches between **External Hardware GPS (NEO-6M)**, **Android Satellite GPS**, and an internal **Mock GPS Simulator** for indoor bench testing.
4. **Collision & Hazard Warning Engine**:
   - **Forward Collision Warning (FCW)**: Calculates Time-To-Collision (TTC) using closing speeds.
   - **Emergency Brake Ahead (EBA)**: Warns within 120m if a lead vehicle slams brakes.
   - **Turn Indicator Conflict**: Alerts when a nearby vehicle is indicating a lane change into your trajectory.
5. **Two-Way Hardware Actuation**:
   - Physical brake press on hardware $\rightarrow$ instantly broadcasts V2V warning.
   - Danger received from another vehicle $\rightarrow$ app triggers physical piezo buzzer and red warning strobe on the microcontroller!

---

## 5. Repository Structure

```
SmartV2V/
├── app/
│   ├── build.gradle                              # Android build script (SDK 34, Material 3)
│   ├── src/main/
│   │   ├── AndroidManifest.xml                   # Permissions: UDP, Bluetooth, Location, Vibration
│   │   ├── java/com/v2v/smartsystem/
│   │   │   ├── V2VApplication.java               # App initialization entry point
│   │   │   ├── model/
│   │   │   │   ├── Vehicle.java                  # Vehicle state and kinematic model
│   │   │   │   ├── VehicleType.java              # Car, Bike, Truck, Bus definitions
│   │   │   │   ├── V2VMessage.java               # JSON-serializable BSM packet
│   │   │   │   ├── MessageType.java              # BEACON, EMERGENCY_BRAKE, HAZARD, TURN
│   │   │   │   └── HardwarePacket.java           # Microcontroller sensor packet
│   │   │   ├── hardware/
│   │   │   │   ├── IHardwareBridge.java          # Transport abstraction interface
│   │   │   │   ├── BluetoothHardwareBridge.java  # Android Bluetooth SPP RFCOMM thread
│   │   │   │   ├── HardwareProtocolParser.java   # Serial string parser ($V2V_HW and #ALERT)
│   │   │   │   ├── VirtualHardwareSimulator.java # In-memory hardware test bench
│   │   │   │   ├── HardwareStatusListener.java   # Hardware callback interface
│   │   │   │   └── HardwareManager.java          # Hardware coordinator singleton
│   │   │   ├── gps/
│   │   │   │   ├── IGpsProvider.java             # GPS provider abstraction
│   │   │   │   ├── GeoLocation.java              # Coordinates and speed container
│   │   │   │   ├── MockGpsProvider.java          # Simulated movement engine
│   │   │   │   ├── AndroidLocationProvider.java  # Android LocationManager GPS
│   │   │   │   └── HardwareGpsProvider.java      # NEO-6M hardware GPS bridge
│   │   │   ├── safety/
│   │   │   │   ├── WarningSystem.java            # Haversine distance & TTC warning engine
│   │   │   │   ├── WarningEvent.java             # Warning data model with severity levels
│   │   │   │   ├── WarningListener.java          # Warning event callback
│   │   │   │   └── SafetyActionController.java   # Brake and turn signal coordinator
│   │   │   ├── communication/
│   │   │   │   ├── IV2VCommunicator.java         # Network transceiver interface
│   │   │   │   ├── UdpBroadcastCommunicator.java # Peer-to-peer UDP broadcast (Port 8888)
│   │   │   │   ├── LoopbackCommunicator.java     # Single-device simulation channel
│   │   │   │   └── V2VMessageDispatcher.java     # 1 Hz BSM beacon scheduler & deduplicator
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.java             # Interactive cockpit dashboard
│   │   │   │   ├── view/RadarView.java           # Custom circular 360° radar canvas
│   │   │   │   └── adapter/                      # Nearby vehicles & message log adapters
│   │   │   └── utils/
│   │   │       ├── DistanceCalculator.java       # Haversine & bearing calculation
│   │   │       ├── NetworkUtils.java             # IP & Wi-Fi MulticastLock utility
│   │   │       └── AudioAlertManager.java        # Cabin tone generator & haptics
│   │   └── res/
│   │       ├── layout/activity_main.xml          # Cockpit layout
│   │       ├── values/colors.xml                 # Automotive dark cockpit palette
│   │       └── drawable/                         # Vehicle, brake, radar, and alert vectors
├── arduino_esp32_hardware_code/
│   ├── v2v_hardware_node.ino                     # Ready-to-flash ESP32/Arduino firmware
│   └── arduino_uno_hc05_node.ino                 # Specialized Arduino Uno + HC-05 firmware
├── HARDWARE_INSTRUCTION_MANUAL.md                # Comprehensive wiring & flashing guide
├── PROJECT_SETUP_GUIDE.md                        # Android Studio setup & multi-device test guide
├── build.gradle                                  # Root Gradle configuration
├── settings.gradle                               # Project settings
└── gradlew / gradlew.bat                         # Gradle wrappers
```

---

## 6. How to Build & Run

### Step 1: Upload Firmware to Hardware
1. Connect your **ESP32** (or Arduino Uno) to your PC with a USB data cable.
2. Open `arduino_esp32_hardware_code/v2v_hardware_node.ino` in the **Arduino IDE**.
3. Select your board (`ESP32 Dev Module`) and COM port.
4. Click **Upload**.
5. Once complete, open Serial Monitor at **115200 baud** to verify:
   `ESP32 Smart V2V Hardware Node Started. Bluetooth: SmartV2V-ECU`

### Step 2: Open and Run Android App
1. Open **Android Studio** and select `File` > `Open`.
2. Browse to `SmartV2V/` and click **OK**.
3. Let Gradle sync dependencies.
4. Connect your Android device via USB (with Developer Options / USB Debugging enabled).
5. Click the green **Run** button (or press `Shift + F10`).

---

## 7. End-to-End Demonstration Guide

### Testing with Real Hardware:
1. On your phone, go to **Settings** > **Bluetooth** and pair with **SmartV2V-ECU** (or HC-05).
2. In the app, tap the **HW Setup** button at the top right.
3. Select `SmartV2V-ECU` from the dropdown and tap **Connect Device**.
4. The status badge will turn **Green (Connected)**.
5. **Physical Brake Test**: Press the push-button on your breadboard $\rightarrow$ **HW BRAKE PIN: TRIPPED** turns red, and an immediate `EMERGENCY_BRAKE` packet is transmitted!
6. **Physical Turn Signal Test**: Flip the Left or Right toggle switch $\rightarrow$ **HW TURN SW: LEFT/RIGHT** updates on screen.

### Testing Between Two Android Devices (Multi-Vehicle V2V):
1. Turn on **Personal Hotspot** on Phone 1.
2. Connect Phone 2 to Phone 1's Wi-Fi hotspot.
3. Open the Smart V2V app on both phones.
4. Both devices will immediately detect each other on the **Spatial Radar** and in the **Nearby Vehicles** list.
5. Press **Emergency Brake** on Phone 2:
   - Phone 1 immediately flashes the **Red Emergency Alert Banner**: *"Vehicle Ahead (V2V-TRUCK-3122) applied emergency brakes!"*
   - Phone 1 plays high-urgency cabin beeps and vibrates.
   - Phone 1 commands the connected hardware to **sound the physical piezo buzzer and flash the red LED**!

### Single-Device Demo (No Hardware / Single Phone):
- Don't have a second phone or physical hardware right now? Click **"Simulate Vehicle Ahead Braking (Demo Test)"** on the dashboard. The built-in simulator will inject a simulated heavy truck slamming brakes 38m ahead, demonstrating all radar changes, warning banners, and alert sounds instantly.
