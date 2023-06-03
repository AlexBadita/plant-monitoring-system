#include <SoftwareSerial.h>
#include <FirebaseArduino.h>
#include <ESP8266WiFi.h>
#include <Wire.h>

// Network credentials
#define WIFI_SSID "Tenda_gog"
#define WIFI_PASSWORD "cicatrice"

// Firebase connection
#define FIREBASE_AUTH "WdZWT9ZOQ2POSWXV1vKQs9wWUfvCpZgJFn7XKoGV"
#define FIREBASE_HOST "plant-watering-system-aaeb9-default-rtdb.europe-west1.firebasedatabase.app"

int temperature;
int humidity;
int light;
int moisture;

// The id for this device
String id = "1";

boolean start = true
boolean changes;
boolean bluetoothOn = false;

long delayValue = 5000;

String pumpRunTime = "1";
String moistureThreshold = "40";

void setup() {
  Wire.begin(D1, D2);
  bluetoothConnection.begin(9600);
  Serial.begin(9600);
  // Connect to WIFI
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.println("Connecting");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();  
  Serial.print("Connected to ");  
  Serial.println(WiFi.localIP());
  // Connect to Firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

void loop() {
  Wire.beginTransmission(1);
  
  if(start || changes){
    String delayUnit = getFromFirebase("Delay/unit");
    String delayValue = getFromFirebase("Delay/value");
    pumpRunTime = getFromFirebase("pumpRunTime");
    moistureThreshold = getFromFirebase("moistureThreshold");
    String dataToSlave = moistureThreshold + "," + pumpRunTime;
    Wire.write(dataToSlave);
    if(start){
      start = false;
    }
    if(changes){
      changes = false;
    }
    String path = "Devices/" + id + "/Settings/changes";
    Firebase.setBool(path, false); 
  }
  Wire.endTransmission();

  delay(1000);

  Wire.requestFrom(1, 20)); // request and read data of size 20 from slave

  if(bluetoothConnection.readString().equals("start")){
    bluetoothOn = true;
  }
  else if(bluetoothConnection.readString().equals("stop"){
    bluetoothOn = false;
  }

  if(bluetoothOn){
    temperature = Wire.parseInt();
    humidity = Wire.parseInt();
    light = Wire.parseInt();
    moisture = Wire.parseInt();
    String dataOut =  "values=" + String(temperature) + "," + String(humidity) + "," + String(light) + "," + String(moisture);
    bluetoothConnection.print(dataOut); 
    
    dataOut =  "settings=" + pumpRunTime + "," + moistureThreshold;
    bluetoothConnection.print(dataOut); 
  }
  
  if(Wire.available()){
    temperature = Wire.parseInt();
    humidity = Wire.parseInt();
    light = Wire.parseInt();
    moisture = Wire.parseInt();
    sendToFirebase("temperature", String(temperature));
    sendToFirebase("humidity", String(humidity));
    sendToFirebase("light", String(light));
    sendToFirebase("moisture", String(moisture));
    if(Wire.available()){
      Wire.readString();
    }
  }

  myDelay();
}

void myDelay(){
  unsigned long myPrevMillis = millis();
  unsigned long myCurrentMillis = myPrevMillis;
  while (myCurrentMillis - myPrevMillis <= delayValue){
    myCurrentMillis = millis();

    if(bluetoothConnection.available()){
       bluetoothConnection.read();
       int pumpRunTime = bluetoothConnection.parseInt();
       int moistureThreshold = bluetoothConnection.parseInt();

       int value = bluetoothConnection.parseInt();
       bluetoothConnection.read();
       String unit = bluetoothConnection.read();

       calculateDelay(unit, String(value));
       
       String dataToSlave = String(moistureThreshold) + "," + String(pumpRunTime);

       Wire.beginTransmission(1);
       Wire.write(dataToSlave);
       Wire.endTransmission();

       break;
    }

    changes = getFromFirebase("changes");

    if(changes){
      break;
    }
  }
}

String getFromFirebase(String ref){
  String path = "Devices/" + id + "/Settings/" + ref;
  FirebaseObject object = Firebase.get(path);
  if (Firebase.failed()) {  
      Serial.print("Setting " + ref + " failed!");  
      Serial.println(Firebase.error());    
      return;  
  }
  return object.getString(ref);
}

void sendToFirebase(String ref, String value){
  String path = "Devices/" + id + "/Measurements/" + ref;
  Firebase.setString(path, value); 
  if (Firebase.failed()) {  
      Serial.print("Setting " + ref + " failed!");  
      Serial.println(Firebase.error());    
      return;  
  }
  delay(1000);
}

void calculateDelay(String unit, String value) {
  if(unit.equals("seconds")){
    delayValue = Integer.parseInt(value) * 1000;
  }
  else if(unit.equals("minutes")){
    delayValue = Integer.parseInt(value) * 60 * 1000;
  }
  else if(unit.equals("hours")){
    delayValue = Integer.parseInt(value) * 60 * 60 * 1000;
  }
  else if(unit.equals("days")){
    delayValue = Integer.parseInt(value) * 60 * 60 * 24 * 1000;
  }
}
