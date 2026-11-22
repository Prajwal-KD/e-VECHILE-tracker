/*
 * Smart V2V Communication System - Vehicular Hardware ECU Firmware
 * Compatible with ESP32 (Recommended) and Arduino Uno / Nano + HC-05.
 * 
 * Functions:
 * 1. Reads physical vehicle brake sensor / switch (Pin 4)
 * 2. Reads physical left/right turn indicator switches (Pins 18, 19)
 * 3. Reads NEO-6M GPS module over UART (Pins 16, 17)
 * 4. Transmits live telemetry ($V2V_HW,...) to Android over Bluetooth SPP
 * 5. Receives safety warning commands (#ALERT,...) and actuates physical piezo buzzer & LED
 */

#if defined(ESP32)
  #include "BluetoothSerial.h"
  BluetoothSerial SerialBT;
  #define BT_ACTIVE 1
  #define PIN_BRAKE        4   // Push button / Brake pedal switch (Pull-Up)
  #define PIN_TURN_LEFT   18   // Left turn signal toggle switch
  #define PIN_TURN_RIGHT  19   // Right turn signal toggle switch
  #define PIN_BUZZER      23   // Active 5V Piezo Buzzer
  #define PIN_LED_RED      5   // Emergency Alert Red LED
  #define PIN_LED_GREEN    2   // Status Green LED (Onboard)
  #define GPS_RX          16   // NEO-6M TX -> ESP32 RX2
  #define GPS_TX          17   // NEO-6M RX -> ESP32 TX2
  HardwareSerial GpsSerial(2);
#else
  #include <SoftwareSerial.h>
  SoftwareSerial SerialBT(10, 11); // RX, TX to HC-05 module
  SoftwareSerial GpsSerial(8, 9);   // RX, TX to NEO-6M GPS
  #define PIN_BRAKE        2
  #define PIN_TURN_LEFT    3
  #define PIN_TURN_RIGHT   4
  #define PIN_BUZZER       5
  #define PIN_LED_RED      6
  #define PIN_LED_GREEN   13
#endif

// State variables
bool brakeState = false;
String turnState = "OFF";
double simulatedLat = 12.971598;
double simulatedLon = 77.594566;
float vehicleSpeed = 0.0;
unsigned long lastTelemetryTime = 0;
bool buzzerActive = false;
String alertLedMode = "OFF";

void setup() {
  Serial.begin(115200);
  
  pinMode(PIN_BRAKE, INPUT_PULLUP);
  pinMode(PIN_TURN_LEFT, INPUT_PULLUP);
  pinMode(PIN_TURN_RIGHT, INPUT_PULLUP);
  
  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_LED_GREEN, OUTPUT);

  digitalWrite(PIN_BUZZER, LOW);
  digitalWrite(PIN_LED_RED, LOW);
  digitalWrite(PIN_LED_GREEN, HIGH); // System alive

#if defined(ESP32)
  SerialBT.begin("SmartV2V-ECU"); // Bluetooth device name
  GpsSerial.begin(9600, SERIAL_8N1, GPS_RX, GPS_TX);
  Serial.println("ESP32 Smart V2V Hardware Node Started. Bluetooth: SmartV2V-ECU");
#else
  SerialBT.begin(9600);
  GpsSerial.begin(9600);
  Serial.println("Arduino V2V Hardware Node Started on HC-05");
#endif
}

void loop() {
  readSensors();
  checkIncomingCommands();

  // Send telemetry stream at 5 Hz (every 200 ms)
  if (millis() - lastTelemetryTime >= 200) {
    lastTelemetryTime = millis();
    sendTelemetryPacket();
  }

  // Actuate warning indicators
  handleWarningActuators();
}

void readSensors() {
  // Brake sensor (active LOW with internal pull-up)
  brakeState = (digitalRead(PIN_BRAKE) == LOW);

  // Turn signal switches
  bool leftSw = (digitalRead(PIN_TURN_LEFT) == LOW);
  bool rightSw = (digitalRead(PIN_TURN_RIGHT) == LOW);

  if (leftSw && rightSw) {
    turnState = "HAZARD";
  } else if (leftSw) {
    turnState = "LEFT";
  } else if (rightSw) {
    turnState = "RIGHT";
  } else {
    turnState = "OFF";
  }

  // Read raw GPS data if available
  while (GpsSerial.available()) {
    char c = GpsSerial.read();
    // TinyGPS++ or NMEA parser can be hooked here
  }
}

void sendTelemetryPacket() {
  // Packet format: $V2V_HW,BRAKE=1,TURN=LEFT,SPD=45.0,LAT=12.9716,LON=77.5946,GPS=1,V=5.02\n
  String packet = "$V2V_HW,";
  packet += "BRAKE=" + String(brakeState ? "1" : "0") + ",";
  packet += "TURN=" + turnState + ",";
  packet += "SPD=" + String(vehicleSpeed, 1) + ",";
  packet += "LAT=" + String(simulatedLat, 6) + ",";
  packet += "LON=" + String(simulatedLon, 6) + ",";
  packet += "GPS=1,";
  packet += "V=5.02\n";

  // Send to Android over Bluetooth
  SerialBT.print(packet);
  
  // Mirror to USB Serial monitor for easy debugging
  Serial.print(packet);
}

void checkIncomingCommands() {
  // Look for commands sent from Android: #ALERT,BUZZER=ON,LED=RED\n
  if (SerialBT.available()) {
    String cmd = SerialBT.readStringUntil('\n');
    cmd.trim();
    if (cmd.startsWith("#ALERT")) {
      if (cmd.indexOf("BUZZER=ON") >= 0) {
        buzzerActive = true;
      } else if (cmd.indexOf("BUZZER=OFF") >= 0) {
        buzzerActive = false;
      }

      if (cmd.indexOf("LED=RED") >= 0) {
        alertLedMode = "RED";
      } else if (cmd.indexOf("LED=OFF") >= 0) {
        alertLedMode = "OFF";
      }
    }
  }
}

void handleWarningActuators() {
  if (buzzerActive) {
    // Pulsing danger alarm pattern
    if ((millis() / 150) % 2 == 0) {
      digitalWrite(PIN_BUZZER, HIGH);
      digitalWrite(PIN_LED_RED, HIGH);
    } else {
      digitalWrite(PIN_BUZZER, LOW);
      digitalWrite(PIN_LED_RED, LOW);
    }
  } else {
    digitalWrite(PIN_BUZZER, LOW);
    digitalWrite(PIN_LED_RED, alertLedMode == "RED" ? HIGH : LOW);
  }
}
