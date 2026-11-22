# Smart V2V Communication System - Hardware Instruction Manual

**Project Title:** Smart V2V Communication System  
**Hardware Interface:** Vehicular Electronic Control Unit (ECU) Breadboard Prototype  
**Platforms Supported:** ESP32 (Recommended) or Arduino Uno / Nano + HC-05  

---

## 1. Complete Bill of Materials (Components Needed)

Here is the exact shopping / lab inventory list for building the physical hardware node:

| # | Component Name | Recommended Model / Specification | Purpose in Project | Approx Quantity |
|---|----------------|-----------------------------------|--------------------|-----------------|
| **1** | **Microcontroller Board** | **ESP32 NodeMCU (30-pin or 38-pin)** *(or Arduino Uno R3)* | Primary on-board computer (ECU). Reads vehicle switches, formats telemetry, and links with Android. *(ESP32 has built-in Bluetooth Classic SPP!)* | 1 |
| **2** | **Bluetooth Module** *(If using Arduino Uno)* | **HC-05 or HC-06** Transceiver Module | Wireless Serial SPP bridge between Arduino and Android phone. *(Not required if using ESP32)* | 1 |
| **3** | **GPS Module** | **u-blox NEO-6M** (with ceramic patch antenna) | Real-world satellite coordinates, ground speed, and heading telemetry over UART. | 1 |
| **4** | **Brake Pedal Sensor** | **Large Momentary Push Button** (Tactile switch) | Acts as physical vehicle brake pedal sensor switch. Tripping triggers emergency brake V2V broadcast. | 1 |
| **5** | **Turn Signal Switches** | **SPDT Miniature Toggle Switches** (or 2x push buttons) | Acts as left and right turn signal indicators on the steering column. | 2 |
| **6** | **Acoustic Warning Actuator** | **Active 5V Piezo Buzzer** | Emits loud cabin warning beeps when an incoming hazard/brake threat is received from a nearby vehicle. | 1 |
| **7** | **Visual Warning Actuator** | **5mm High-Intensity Red LED** | Flashes danger strobe when emergency brake threat ahead is detected. | 1 |
| **8** | **System Status LED** | **5mm Green LED** | Indicates on-board ECU power and operational status. | 1 |
| **9** | **Resistors** | **220 Ω (x2)** and **10 kΩ (x1)** | Current limiting for LEDs and pull-up/pull-down safety. | 3 |
| **10**| **Prototyping Solderless Breadboard** | Standard 830-tie point MB-102 | For clean, solder-free wiring of all switches and modules. | 1 |
| **11**| **Jumper Wires** | Male-to-Male and Male-to-Female | Connecting pins between board, sensors, and breadboard rails. | 1 bundle (20 pcs) |
| **12**| **Power Supply** | USB Cable (Micro-USB or Type-C) OR 9V Battery with DC barrel plug / 18650 battery holder | Powers the microcontroller and sensors. | 1 |

---

## 2. Pin-to-Pin Connection Guide

### Option A: Wiring with ESP32 (Recommended - Fastest & Cleanest)

The ESP32 features **built-in Bluetooth**, meaning you do not need an external HC-05 module!

| Component | Component Pin | Connect to ESP32 Pin | Notes |
|-----------|---------------|----------------------|-------|
| **Brake Button** | Leg 1 | **GPIO 4** | Configured as `INPUT_PULLUP`. |
| | Leg 2 | **GND** | Pressing button connects GPIO 4 to GND (Active LOW). |
| **Left Turn Switch** | Pin 1 | **GPIO 18** | Configured as `INPUT_PULLUP`. |
| | Pin 2 | **GND** | Toggling switch connects to GND. |
| **Right Turn Switch**| Pin 1 | **GPIO 19** | Configured as `INPUT_PULLUP`. |
| | Pin 2 | **GND** | Toggling switch connects to GND. |
| **Piezo Buzzer** | Positive (+) [Long leg] | **GPIO 23** | Actuated when danger warning is received. |
| | Negative (-) [Short leg] | **GND** | Direct ground. |
| **Red Warning LED** | Anode (+) [Long leg] | **GPIO 5** (via 220 Ω resistor) | Warning strobe indicator. |
| | Cathode (-) [Short leg]| **GND** | Direct ground. |
| **Green Status LED** | Anode (+) | **GPIO 2** (or Built-in LED) | System active indicator. |
| | Cathode (-) | **GND** | Ground. |
| **NEO-6M GPS** | VCC | **3.3V or 5V** | Power rail. |
| | GND | **GND** | Ground rail. |
| | TX | **GPIO 16 (RX2)** | Hardware Serial 2 receiver on ESP32. |
| | RX | **GPIO 17 (TX2)** | Hardware Serial 2 transmitter on ESP32. |

---

### Option B: Wiring with Arduino Uno + HC-05 Module

| Component | Component Pin | Connect to Arduino Uno Pin | Notes |
|-----------|---------------|----------------------------|-------|
| **HC-05 Bluetooth** | VCC | **5V** | Power. |
| | GND | **GND** | Ground. |
| | TXD | **Pin 10** | SoftwareSerial RX on Arduino. |
| | RXD | **Pin 11** | SoftwareSerial TX on Arduino (via 1k/2k voltage divider). |
| **Brake Button** | Leg 1 | **Pin 2** | `INPUT_PULLUP` to GND. |
| **Left Turn Switch** | Pin 1 | **Pin 3** | `INPUT_PULLUP` to GND. |
| **Right Turn Switch**| Pin 1 | **Pin 4** | `INPUT_PULLUP` to GND. |
| **Piezo Buzzer** | Positive (+) | **Pin 5** | Active buzzer output. |
| **Red Warning LED** | Anode (+) | **Pin 6** (via 220 Ω resistor) | Emergency strobe. |

---

## 3. Circuit Schematic Diagram (Breadboard Layout)

```
        +-------------------------------------------------------+
        |                 BREADBOARD POWER RAILS                |
        |  (+) 5V / 3.3V   ===================================  |
        |  (-) GND         ===================================  |
        +-------------------------------------------------------+

        +-----------------------+
        |      ESP32 BOARD      |
        |                       |
        | [GPIO 4]  ------------+-----> [BRAKE PUSH BUTTON] ----> GND
        |                       |
        | [GPIO 18] ------------+-----> [LEFT TURN SWITCH]  ----> GND
        | [GPIO 19] ------------+-----> [RIGHT TURN SWITCH] ----> GND
        |                       |
        | [GPIO 23] ------------+-----> (+) [5V PIEZO BUZZER] (-) -> GND
        |                       |
        | [GPIO 5]  ---[220Ω]---+-----> (+) [RED WARNING LED] (-) -> GND
        |                       |
        | [RX2/16] <------------+------ [NEO-6M GPS TX]
        | [TX2/17] ------------>+------ [NEO-6M GPS RX]
        |                       |
        | [GND]     ------------+-----> Common GND Rail
        | [5V/VIN]  ------------+-----> Common 5V Rail
        +-----------------------+
```

---

## 4. How to Upload Code to the Hardware (Step-by-Step)

### For ESP32:
1. Open the **Arduino IDE** on your PC.
2. If you don't have ESP32 installed:
   - Go to `File` > `Preferences`.
   - In "Additional Boards Manager URLs", add:
     `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
   - Go to `Tools` > `Board` > `Boards Manager`, search for **esp32** by Espressif Systems and click **Install**.
3. Open the firmware sketch file:
   `SmartV2V/arduino_esp32_hardware_code/v2v_hardware_node.ino`
4. Select `Tools` > `Board` > `ESP32 Arduino` > **ESP32 Dev Module**.
5. Connect your ESP32 board to your PC using a micro-USB / USB-C data cable.
6. Select your COM port under `Tools` > `Port`.
7. Click the **Upload** button (Arrow icon).
8. Once uploaded, open `Tools` > `Serial Monitor` at **115200 baud**. You will see:
   `ESP32 Smart V2V Hardware Node Started. Bluetooth: SmartV2V-ECU`

### For Arduino Uno:
1. Open `SmartV2V/arduino_esp32_hardware_code/arduino_uno_hc05_node.ino`.
2. Select `Tools` > `Board` > **Arduino Uno**.
3. Select your COM port.
4. Click **Upload**.

---

## 5. Pairing Android Phone with the Hardware

1. On your Android smartphone, turn ON **Bluetooth**.
2. Go to **Bluetooth Settings** > **Pair New Device**.
3. Scan for devices:
   - If using ESP32: Select **SmartV2V-ECU** (no PIN required or standard 1234/0000).
   - If using HC-05: Select **HC-05** (Enter PIN: `1234` or `0000`).
4. Once paired, launch the **Smart V2V System** app on your phone.
5. In the app top bar, tap the **HW Offline** badge or the **HW Setup** button.
6. Select your paired device (`SmartV2V-ECU` or `HC-05`) from the dropdown list.
7. Tap **Connect Device**.
8. The badge will instantly turn **Green: SmartV2V-ECU (Connected)**!

---

## 6. End-to-End System Testing & Demonstration

Follow these steps to demonstrate the hardware integration:

### Test 1: Physical Brake Trigger to V2V Broadcast
1. Press the physical push-button on your breadboard.
2. On your phone screen, watch the **HW BRAKE PIN** indicator instantly turn **TRIPPED (RED)**!
3. The app automatically compiles and transmits an `EMERGENCY_BRAKE` packet over UDP.
4. Any other phone running the app nearby will immediately flash the **Red Emergency Brake Alert Banner** and play an alarm!

### Test 2: Physical Turn Switch to Telemetry
1. Flip the Left Turn toggle switch on your breadboard.
2. In the app, watch the **HW TURN SW** indicator display **LEFT (Cyan)**.
3. The spatial radar on other vehicles displays your turn intent.

### Test 3: Receiving a Danger Alert (Actuator Test)
1. On Phone 2 (or by clicking the **"Simulate Vehicle Ahead Braking"** button):
2. As soon as the collision engine identifies a forward collision threat:
3. Phone 1 sends `#ALERT,BUZZER=ON,LED=RED\n` to the microcontroller.
4. The **physical piezo buzzer on your breadboard will buzz loudly**, and the **Red Warning LED will flash**!
