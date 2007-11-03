/*
 * Ardrumo sketch
 *
 * Use with the Ardumo software here:
 * http://code.google.com/p/ardrumo/
 * This is designed to let an Arduino act as a drum machine
 * in GarageBand (sorry, MacOS X only).
 */

// define the pins we use
#define ledPin     13  // for serial out status LED

// analog threshold for piezo sensing
#define PIEZOTHRESHOLD 5
#define PADNUM 6

int val;

void setup() {
  pinMode(ledPin, OUTPUT);
  Serial.begin(57600);   // set serial output rate
}

void loop() {

  // Loop through each piezo and send data
  // on the serial output if the force exceeds
  // the piezo threshold
  for(int i = 0; i < PADNUM; i++) {
    val = analogRead(i);
    if( val >= PIEZOTHRESHOLD ) {
      digitalWrite(ledPin,HIGH);  // indicate we're sending MIDI data
      Serial.print(i);
      Serial.print(",");
      Serial.print(val);
      Serial.println();
      digitalWrite(ledPin,LOW);
    }
  }
}
