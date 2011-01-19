/*
 * Blinky read
 * Copyright (C) 2010 Sarah Sharp
 *
 * Read the analog to digital converter every second,
 * output the results, and turn on an LED if
 * the voltage drops below a specific threshold.
 */

const int ledPin =  13;
const int analogInPin = 0;
const int pumpVccPin = 3;
/*
 * Max voltage measured is 5V, or 1023 from ADC.
 * Pick the voltage threshold to be half that.
 */
const int voltageThreshold = 512;

/* Globals */
unsigned long previousMillis = 0;  // will store last time LED was updated

void setup() {
  // set the digital pin as output:
  pinMode(ledPin, OUTPUT);
  pinMode(pumpVccPin, OUTPUT);
  Serial.begin(9600);
}

void loop()
{
  /* 0 to 1023, representing 0 to 5V */
  int voltage;
  unsigned long currentMillis;

  currentMillis = millis();  
  if (currentMillis - previousMillis <= 1000)
    return;
  previousMillis = currentMillis;
  
  voltage = analogRead(analogInPin);
  Serial.println(voltage);
  if (voltage < voltageThreshold) {
    digitalWrite(ledPin, HIGH);
    digitalWrite(pumpVccPin, HIGH);
  } else {
    digitalWrite(pumpVccPin, LOW);
    digitalWrite(ledPin, LOW);
  }
}
