/*
 * Blinky read
 * Copyright (C) 2010 Sarah Sharp
 *
 * Read the analog to digital converter every second,
 * output the results, and turn on an LED if
 * the voltage drops below a specific threshold.
 */

/* Limitations on the amount of time to water,
 * and the time between waterings.
 */
/* Water for 3 seconds */
const int wateringMicrosecs = 5 * 1000;
/* Only water every 30 seconds */
const int delayMicrosecs = 30 * 1000;

const int ledPin =  13;
const int analogInPin = 0;
const int pumpVccPin = 3;
/*
 * Max voltage measured is 5V, or 1023 from ADC.
 * Pick the voltage threshold to be half that.
 */
const int voltageThreshold = 795;

/* Globals */
unsigned long previousMillis = 0;  // will store last time LED was updated

void setup() {
  // set the digital pin as output:
  pinMode(ledPin, OUTPUT);
  pinMode(pumpVccPin, OUTPUT);
  Serial.begin(9600);
}

/* Turn on the pump for wateringMicrosecs.
 * Refuse to water more often than delayMicrosecs.
 */
void waterPlant()
{
  unsigned long currentMillis;
  
  currentMillis = millis();  
  if (currentMillis - previousMillis <= delayMicrosecs)
    return;
  
  digitalWrite(ledPin, HIGH);
  digitalWrite(pumpVccPin, HIGH);
  
  delay(wateringMicrosecs);
  
  digitalWrite(pumpVccPin, LOW);
  digitalWrite(ledPin, LOW);
  previousMillis = currentMillis;
}

void loop()
{
  /* 0 to 1023, representing 0 to 5V */
  int voltage;
  
  voltage = analogRead(analogInPin);
  Serial.println(voltage);
  if (voltage < voltageThreshold)
    waterPlant();
  delay(100);
}
