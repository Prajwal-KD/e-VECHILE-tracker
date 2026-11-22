# Smart V2V Communication System - Android Project Setup Guide

**Project Name:** Smart V2V Communication System  
**Language:** Java  
**Platform:** Android (Min SDK 24, Target SDK 34)  
**Location:** `c:\Users\prajw\OneDrive\Desktop\my works\SmartV2V`  

---

## 1. How to Open the Project in Android Studio

1. Launch **Android Studio**.
2. Click **Open** (or `File` > `Open`).
3. Browse to `c:\Users\prajw\OneDrive\Desktop\my works\SmartV2V` and click **OK**.
4. Android Studio will automatically recognize the Gradle build scripts and sync the dependencies:
   - `androidx.appcompat`
   - `com.google.android.material`
   - `androidx.constraintlayout`
   - `androidx.recyclerview`
5. Connect your Android phone via USB with **USB Debugging** enabled, or start an Android Emulator.
6. Click the green **Run** button (or press `Shift + F10`) to build and install the APK on your device!

---

## 2. Multi-Device V2V Testing (Over Wi-Fi Hotspot)

To test communication between **two or more physical Android phones**:

1. On **Phone 1**, turn on **Personal Hotspot** (Portable Wi-Fi Hotspot).
2. On **Phone 2**, turn on **Wi-Fi** and connect to Phone 1's hotspot.
3. Install and open the **Smart V2V System** app on both phones.
4. On Phone 1, select Vehicle Type: **Car** (e.g. `V2V-CAR-8419`).
5. On Phone 2, select Vehicle Type: **Truck** (e.g. `V2V-TRUCK-3122`).
6. Both phones will immediately discover each other on port `8888` via UDP broadcast:
   - Phone 1's radar will show Phone 2.
   - The **Nearby Detected Vehicles** list will show the other vehicle with distance and speed.
7. Tap **Emergency Brake** on Phone 2:
   - Phone 1 will immediately trigger a flashing **CRITICAL ALERT BANNER**: *"Vehicle Ahead (V2V-TRUCK-3122) applied emergency brakes!"*
   - Phone 1 will play cabin acoustic warning beeps and trigger the hardware piezo buzzer!

---

## 3. Single-Device Test Bench Mode (Demo Without 2 Phones)

If you only have one phone or an emulator:
1. Open the app.
2. Tap the **"Simulate Vehicle Ahead Braking (Demo Test)"** button.
3. The in-memory loopback injector immediately simulates a nearby heavy vehicle slamming brakes 38 meters ahead.
4. You can observe the radar showing the vehicle turning red, the warning banner firing, and the hardware buzzer activating!

---

## 4. Software Architecture Breakdown

The project follows strict modular programming:

- `com.v2v.smartsystem.hardware`: Physical hardware transport (Bluetooth SPP, HC-05/ESP32, protocol parser, virtual test bench).
- `com.v2v.smartsystem.gps`: Abstraction for GPS positioning (Mock GPS, Android satellite GPS, and external NEO-6M hardware GPS).
- `com.v2v.smartsystem.vehicle`: Host vehicle profile, persistent unique ID generator, and telemetry state.
- `com.v2v.smartsystem.safety`: Forward collision detection, Time-To-Collision (TTC) algorithm, emergency brake controller, and turn indicators.
- `com.v2v.smartsystem.communication`: Peer-to-peer UDP broadcast engine over Wi-Fi/Hotspot LAN, 1 Hz heartbeat beacon, and deduplicator.
- `com.v2v.smartsystem.ui`: Real-time cockpit dashboard, dynamic circular Radar canvas view, nearby vehicle recycler, and packet stream logs.
- `arduino_esp32_hardware_code`: Complete firmware for ESP32 and Arduino Uno microcontrollers.
