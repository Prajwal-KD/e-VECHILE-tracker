/*
 * Smart V2V Communication System - Arduino Uno / Nano + HC-05 Firmware
 * 
 * Hardware Wiring:
 * HC-05 Bluetooth:
 *   - VCC -> 5V
 *   - GND -> GND
 *   - TXD -> Arduino Pin 10 (Soft RX)
 *   - RXD -> Arduino Pin 11 (Soft TX via voltage divider: 1k + 2k ohm)
 * 
 * Sensors & Indicators:
 *   - Push Button (Brake Sensor): Pin 2 (to GND, internal pull-up)
 *   - Toggle Switch (Left Turn):  Pin 3 (to GND, internal pull-up)
 *   - Toggle Switch (Right Turn): Pin 4 (to GND, internal pull-up)
 *   - Active 5V Piezo Buzzer:     Pin 5 (+ to Pin 5, - to GND)
 *   - Red Emergency LED:          Pin 6 (with 220 ohm resistor to GND)
 *   - NEO-6M GPS (Optional):      Pin 8 (RX), Pin 9 (TX)
 */

#include <SoftwareSerial.h>

SoftwareSerial hc05(10, 11); // RX, TX

const int PIN_BRAKE = 2;
const int PIN_TURN_L = 3;
const int PIN_TURN_R = 4;
const int PIN_BUZZER = 5;
const int PIN_LED_RED = 6;

bool brakeActive = false;
String turnSignal = "OFF";
bool buzzerOn = false;
unsigned long lastSend = 0;

void setup() {
  Serial.begin(9600);
  hc05.begin(9600); // Standard HC-05 default baud rate

  pinMode(PIN_BRAKE, INPUT_PULLUP);
  pinMode(PIN_TURN_L, INPUT_PULLUP);
  pinMode(PIN_TURN_R, INPUT_PULLUP);

  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_LED_RED, OUTPUT);

  digitalWrite(PIN_BUZZER, LOW);
  digitalWrite(PIN_LED_RED, LOW);

  Serial.println(F("Arduino Uno V2V Hardware Ready. Pair HC-05 (PIN 1234)."));
}

void loop() {
  // Read inputs
  brakeActive = (digitalRead(PIN_BRAKE) == LOW);
  bool lSw = (digitalRead(PIN_TURN_L) == LOW);
  bool rSw = (digitalRead(PIN_TURN_R) == LOW);

  if (lSw && rSw) turnSignal = "HAZARD";
  else if (lSw) turnSignal = "LEFT";
  else if (rSw) turnSignal = "RIGHT";
  else turnSignal = "OFF";

  // Check incoming commands from Android (#ALERT,BUZZER=ON...)
  if (hc05.available()) {
    String cmd = hc05.readStringUntil('\n');
    cmd.trim();
    if (cmd.startsWith("#ALERT")) {
      buzzerOn = (cmd.indexOf("BUZZER=ON") >= 0);
    }
  }

  // Actuate buzzer and red alert LED
  if (buzzerOn) {
    if ((millis() / 150) % 2 == 0) {
      digitalWrite(PIN_BUZZER, HIGH);
      digitalWrite(PIN_LED_RED, HIGH);
    } else {
      digitalWrite(PIN_BUZZER, LOW);
      digitalWrite(PIN_LED_RED, LOW);
    }
  } else {
    digitalWrite(PIN_BUZZER, LOW);
    digitalWrite(PIN_LED_RED, LOW);
  }

  // Send telemetry packet every 200ms
  if (millis() - lastSend >= 200) {
    lastSend = millis();
    String pkt = "$V2V_HW,BRAKE=" + String(brakeActive ? "1" : "0") +
                 ",TURN=" + turnSignal +
                 ",SPD=0.0,LAT=12.971598,LON=77.594566,GPS=1,V=5.0\n";
    hc05.print(pkt);
    Serial.print(pkt);
  }
}
