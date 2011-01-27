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
const unsigned long wateringMicrosecs = 5 * 1000;
/* Only water every 30 seconds */
const unsigned long delaySecs = 30;

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
  previousMillis = 0;
}

/* Turn on the pump for wateringMicrosecs.
 * Refuse to water more often than delayMicrosecs.
 */
void waterPlant()
{
  unsigned long currentMillis;
  unsigned long secondsLeft;
  
  currentMillis = millis();
  if ((currentMillis - previousMillis) / 1000 <= delaySecs) {
    secondsLeft = delaySecs - ((currentMillis - previousMillis) / 1000);
    Serial.print("    count down:  ");
    Serial.print(secondsLeft);
    Serial.print(" sec");
    if (secondsLeft > 1)
      Serial.print("s");
    return;
  }
  
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
  Serial.print(voltage);
  if (voltage < voltageThreshold)
    waterPlant();
  Serial.println("");
  delay(100);
}
