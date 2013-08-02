/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 * Not commented!
 */
 
 
#include <Servo.h> 
#include <RemoteReceiver.h>
#include <OneWire.h>
#include <RemoteTransmitter.h>
#include <avr/eeprom.h>
#include <IRremote.h>
#include <String.h>
//#include <DallasTemperature.h>






struct settings_t
{
  int OpenValueLeft;
  int OpenValueRight;
  int CloseValueLeft;
  int CloseValueRight;
  int MidValueLeft;
  int MidValueRight;
} settings;





class Blind {
  public:
  
    Blind() {}
  
    Blind(int openV, int closeV, int midV, int servoP, int potP, byte tolerance, int openVal, int closeVal, int stopVal, char serialIndicator) {
      ovp = openV;
      cvp = closeV;
      mvp = midV;
      ov = openVal;
      cv = closeVal;
      svo = stopVal;
      servoPin = servoP;
      ptp = potP;
      curPos = 1;
      tol = tolerance;
      sri = serialIndicator;
    }
    /*
    void open() {
      srv.attach(servoPin);
      srv.write(ov);                  
      delay(od);
      srv.write(svo);
      srv.detach();
    }
    *//*
    void start(boolean left) {
      srv.attach(servoPin);
      if (left)
        srv.write(ov);
      else
        srv.write(cv);
    }
    *//*
    void stop() {
      srv.attach(servoPin);
      srv.write(svo);
      srv.detach();
    }*/
    /*
    void close() {
      srv.attach(servoPin);
      srv.write(cv);                  
      delay(cd);
      srv.write(svo);
      srv.detach();
    }
    */
    void manOpen() {
      srv.attach(servoPin);
      srv.write(ov);
      delay(200);
      srv.write(svo);
      srv.detach();
    }
    
    void manClose() {
      srv.attach(servoPin);
      srv.write(cv);
      delay(200);
      srv.write(svo);
      srv.detach();
    }
    
    void moveTo (byte pos) {
      if (pos == 1 && curPos == 0) {
        srv.attach(servoPin);
        while (abs(analogRead(ptp)-mvp) > tol) {
          srv.write(ov);                  
          delay(3); //5
        }
        srv.write(svo);
        srv.detach();
        curPos = 1;
      }
      else if (pos == 0 && curPos == 1) {
        srv.attach(servoPin);
        while (abs(analogRead(ptp)-cvp) > tol) {
          srv.write(cv);                  
          delay(3);
        }
        srv.write(svo);
        srv.detach();
        curPos = 0;
      }
      else if (pos == 2 && curPos == 1) {
        srv.attach(servoPin);
        while (abs(analogRead(ptp)-ovp) > tol) {
          srv.write(ov);                  
          delay(3);
        }
        srv.write(svo);
        srv.detach();
        curPos = 2;
      }
      else if (pos == 1 && curPos == 2) {
        srv.attach(servoPin);
        while (abs(analogRead(ptp)-mvp) > tol) {
          srv.write(cv);                  
          delay(3);
        }
        srv.write(svo);
        srv.detach();
        curPos = 1;
      }
      else if (pos == 2 && curPos == 0) {
        moveTo(1);
        moveTo(2);
      }
      else if (pos == 0 && curPos == 2) {
        moveTo(1);
        moveTo(0);
      }
    }
    
    byte getPos() {
      return curPos;
    }
    
    void statusDetect() {
      
      if (abs(analogRead(ptp)-ovp) < 30) {
        curPos = 2;
        moveTo(1);
      }
      else if (abs(analogRead(ptp)-mvp) < 40) {
        curPos = 1;
      }
      else if (abs(analogRead(ptp)-cvp) < 40) {
        curPos = 0;
        moveTo(1);
      }
      
    }
    
    void printStatus() {
      Serial.print("ZA=");
      Serial.print(sri);
      Serial.print((int)curPos);
      Serial.print('\0');      
    }
    
  private:
  
    int ovp;
    int cvp;
    int mvp;
    
    int ov;
    int cv;
    int svo;
    Servo srv;
    int servoPin;
    
    int ptp;
    int tol;
    
    byte curPos;
    
    char sri;
};
 
#define zvocnik 5
#define RECV_PIN 11


Blind rightBlind;
Blind leftBlind;

char buff[20];
byte buff_pointer = 0;

char miniBuff[4];
byte miniBuffByte[3];

int sel = 1;
volatile int menuSel = -1;


//Temperature chip i/o
int DS18S20_Pin = 12; //DS18S20 temp pin
OneWire ds(DS18S20_Pin);
//DallasTemperature tempsensors(&ds);

int primaryLight_on = 0;
int primaryLight_off = 0;

int secondaryLight_on = 26;
int secondaryLight_off = 24;

long timer = 0;


IRrecv irrecv(RECV_PIN);
IRsend irsend;

decode_results results;

KaKuTransmitter kaKuTransmitter(8);

void setup() 
{ 
  Serial.begin(9600);
  
  eeprom_read_block((void*)&settings, (void*)0, sizeof(settings));

  //rightBlind = Blind(790, 700, 217, 10, 0, 5, 70, 110, 90);
  //leftBlind = Blind(525, 422, 473, 9, 1, 1, 110, 70, 90);
  
  rightBlind = Blind(settings.OpenValueRight, settings.CloseValueRight, settings.MidValueRight, 10, 0, 5, 70, 110, 90, 'R');
  leftBlind = Blind(settings.OpenValueLeft, settings.CloseValueLeft, settings.MidValueLeft, 9, 1, 1, 110, 70, 90, 'L');
  
  
  RemoteReceiver::init(0, 3, remoteEvent);
  
  pinMode(5, OUTPUT);
  /*
  pinMode(13, OUTPUT);
  pinMode(4, INPUT);
  digitalWrite(4, HIGH);
  pinMode(5, INPUT);
  digitalWrite(5, HIGH);
  pinMode(6, INPUT);
  digitalWrite(6, HIGH);
  pinMode(7, INPUT);
  digitalWrite(7, HIGH);
  pinMode(8, INPUT);
  digitalWrite(8, HIGH);
  */
  /*
  //Zapisimo zacetne nastavitve v eeprom
  settings.OpenValueLeft=525;
  settings.OpenValueRight=770;
  settings.CloseValueLeft=422;
  settings.CloseValueRight=700;
  settings.MidValueLeft=473;
  settings.MidValueRight=217;
  
  eeprom_write_block((const void*)&settings, (void*)0, sizeof(settings));
  Serial.println("Write complete");
  */
  
  /*
  leftBlind.statusDetect();
  rightBlind.statusDetect();
  
  rightBlind.printStatus();
  leftBlind.printStatus();
  */
  irrecv.enableIRIn();
  //tempsensors.begin();
  
} 



void noise(boolean norm=true)
{
//   analogWrite(zvocnik, 200); 
//   delay(700);
   
   if (norm) {
   for (int i = 50; i > 0; i--)
   {
      analogWrite(zvocnik,i); 

      delay(10); 
   }
   }
   else {
        for (int i = 15; i > 0; i--)
   {
      analogWrite(zvocnik,i); 

      delay(10); 
   }
   }
   
   analogWrite(zvocnik, 0); 
}



void remoteEvent(unsigned long receivedCode, unsigned int period) {
  menuSel = receivedCode;
}


void messageReceived() {
  if (buff_pointer >= 3) {
    
    if (buff[0] == 'L' && buff[1] == 'U') {
      
      miniBuff[0] = buff[3];
      miniBuff[1] = '\0';
      
      miniBuffByte[1] = atoi(miniBuff);
      
      miniBuff[0] = buff[4];
      miniBuff[1] = '\0';
      
      miniBuffByte[0] = switchLight(miniBuffByte[1], atoi(miniBuff));
      
      Serial.print("LU=");
      Serial.print((int)miniBuffByte[1]);
      Serial.print((int)miniBuffByte[0]);
      Serial.print('\0');
    }
    else if (buff[0] == 'Z' && buff[1] == 'A') {
      
      miniBuff[0] = buff[4];
      miniBuff[1] = '\0';
      
      boolean manual = false;
      
      if (buff[5] == 'M')
        manual = true;
        
      setBlinds(buff[3], atoi(miniBuff), manual);
      
    }
    else if (buff[0] == 'I' && buff[1] == 'R') {
      unsigned long ircode = strtoul(buff+3, NULL, 16);
      //Serial.println(ircode, 16);
      irsend.sendNEC(ircode, 32);
      irrecv.resume();
      irrecv.enableIRIn();
    }
    else if (buff[0] == 'L' && buff[1] == 'O') {
      if (buff[3] == '1')
        digitalWrite(13, HIGH);
      else
        digitalWrite(13, LOW);
    }
    else if (buff[0] == 'B' && buff[1] == 'E') {
      noise();
    }
  }
  else {
    //error handling
  }
}


int switchLight(byte light, byte stat) {

  boolean dimmer = (stat == 3) ? true:false;
  boolean ws = (stat == 1) ? true:false;
    
  if (light == 1) {
    noInterrupts();
    kaKuTransmitter.sendSignal('A',3 , ws);
    interrupts();
    return (int)ws;
  }
  else if (light == 2) {
    
    if (dimmer) {/*
      noInterrupts();
      kaKuTransmitter.sendSignal('A', 1, true);
      interrupts();
      delay(300);
      noInterrupts();
      kaKuTransmitter.sendSignal('A', 1, true);
      interrupts();
      return 1;
      */
    }
    else {
      noInterrupts();
      kaKuTransmitter.sendSignal('A', 1 , ws);
      interrupts();
      return (int)ws;
    }
  }
}


void setBlinds(char blind, byte position, boolean manual) {
  
  if (!manual) {
    switch (blind) {
      case 'L': leftBlind.moveTo(position); leftBlind.printStatus(); break;
      case 'R': rightBlind.moveTo(position); rightBlind.printStatus(); break;
    }
  }
  else {
    switch (blind) {
      case 'L': (position == 0) ? leftBlind.manClose() : leftBlind.manOpen(); break;
      case 'R': (position == 0) ? rightBlind.manClose() : rightBlind.manOpen(); break;
    }
  }
  
} 



float getTemp(){
//returns the temperature from one DS18S20 in DEG Celsius

byte data[12];
byte addr[8];

if ( !ds.search(addr)) {
//no more sensors on chain, reset search
ds.reset_search();
return -1000;
}

if ( OneWire::crc8( addr, 7) != addr[7]) {
Serial.println("Cnv"); //CRC is not valid
return -1000;
}

if ( addr[0] != 0x10 && addr[0] != 0x28) {
Serial.print("Dnr"); //device not recognized
return -1000;
}

ds.reset();
ds.select(addr);
ds.write(0x44,1); // start conversion, with parasite power on at the end

byte present = ds.reset();
ds.select(addr);
ds.write(0xBE); // Read Scratchpad


for (int i = 0; i < 9; i++) { // we need 9 bytes
data[i] = ds.read();
}

ds.reset_search();

byte MSB = data[1];
byte LSB = data[0];

float tempRead = ((MSB << 8) | LSB); //using two's compliment
float TemperatureSum = tempRead / 16;

return TemperatureSum;

}

 
void loop() 
{ /*
    if (digitalRead(4) == LOW) {
      if (opening == false) {
        time = millis();
        opening = true;
      rightBlind.start(true);
    }
    else {
      Serial.print("pot-v: ");
      Serial.println(analogRead(0));
      opening = false;
      rightBlind.stop();
    }
    delay(250);
  }
  
  if (digitalRead(5) == LOW) {
      if (opening == false) {
        time = millis();
        opening = true;
      rightBlind.start(false);
    }
    else {
      Serial.print("pot-v: ");
      Serial.println(analogRead(0));
      opening = false;
      rightBlind.stop();
  }
  delay(250);
  }
  
    if (digitalRead(6) == LOW) {
      Serial.print("Value = ");
      Serial.println(analogRead(0));
      delay(500);
  }*/
  
  if (menuSel != -1) {
    //Serial.println(menuSel);

    switch (menuSel) {
      case 4400: sel++;  
               if (sel > 2)
                  sel = 2;
               setBlinds('R', sel, false);
               setBlinds('L', sel, false);
               break;
      case 4398: sel--;  
               if (sel < 0)
                  sel = 0;
               setBlinds('R', sel, false);
               setBlinds('L', sel, false);
               break;
      default: Serial.print("RE=");
               Serial.print(menuSel);
               Serial.print('\0');
               break;
    }
    noise(false);
    menuSel = -1;
  }
  
    if (irrecv.decode(&results)) {
      if (results.value != 0 && results.value != 0xFFFFFFFF) {
        Serial.print("IR=");
        Serial.print(results.value, HEX);
        Serial.print("\0");
        delay(200);
      }
      irrecv.resume(); // Receive the next value
  }

  
  
  while (Serial.available() > 0) {
    buff[buff_pointer] = Serial.read();
    
    if (buff[buff_pointer] == '\0') {
      messageReceived();
      buff_pointer=0;
    }
    else {
      buff_pointer++;
    }
  }
  
  
  
  if (millis() > timer+10000) {
    
    noInterrupts();
    float tmp = getTemp();
    interrupts();
    Serial.print("TM=");
    Serial.print(tmp);
    Serial.print('\0');
    delay(30);
    /*
    //Stanje luci
    int svetlost = analogRead(2);
    Serial.print("LL=");
    Serial.print(svetlost);
    Serial.print("\0");
    */
    timer = millis();
  }
  
} 
