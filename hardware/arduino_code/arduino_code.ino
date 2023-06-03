#include <Wire.h>
#include <dht.h>

dht DHT;

// Analog pins
int light_pin = A0;     // photoresistor
int moisture_pin = A1;  // soil moisture sensor

// Digital pins
int dht_pin = 13;       // temperature and humidity sensor

// Trigger pins
int moisture_trigger_pin = 8; // trigger soil moisture sensor
int pump_pin = 4;             // trigger pump

// Data read from sensors
int temperature;
int humidity;
int light;
int moisture;

int pumpRunTime = 1000;
int threashold = 40;

void setup() {
  // Initialize I2C communication
  Wire.begin(1);
  Wire.onRequest(read_data_from_sensors);
  Wire.onReceive(set_values);
  
  pinMode(dht_pin, INPUT);
  pinMode(light_pin, INPUT);
  pinMode(moisture_pin, INPUT);
  pinMode(water_pin, INPUT);
  
  pinMode(moisture_trigger_pin, OUTPUT);
  digitalWrite(moisture_trigger_pin, LOW);

  pinMode(pump_pin, OUTPUT);
  digitalWrite(pump_pin, LOW);
}

void loop() {
  delay(100);
}

void read_data_from_sensors(){
  read_dht_sensor();
  read_light_sensor();
  read_soil_moisture_sensor();
  if(moisture < threashold){
    trigger_pump();
  }
  values = String(temperature) + "," + String(humidity) + "," + String(light) + "," + String(moisture);
  Wire.write(values);
}

void set_values(int howMany){
  String dataIn;
  char c;
  while(Wire.available()){
    c = Wire.read();
    if(c == '/n'){
      pumpRunTime = dataIn.toInt() * 1000;
      break;
    }
    else if(c == ','){
      threashold = dataIn.toInt();
      dataIn = "";
    }
    else{
      dataIn += c;
    }
  }
}

void read_dht_sensor(){
  DHT.read11(dht_pin);

  // Get values from DHT sensor
  humidity = DHT.humidity;
  temperature = DHT.temperature;

  delay(1000); // wait 1 s
}

void read_light_sensor(){
  // Get value from photoresistor (between 0 and 1023)
  light = analogRead(light_pin);
  light = map(light, 0, 1023, 100, 0);  // 1023 -> 0%, 0 -> 100%

  delay(1000); // wait 1 s
}

void read_soil_moisture_sensor(){
  // Activate sensor
  digitalWrite(moisture_trigger_pin, HIGH);
  delay(100);

  // Get value from the soil moisture sensor
  moisture = analogRead(moisture_pin);
  // Set values to be between 400 and 1023
  moisture = constrain(moisture, 400, 1023);
  moisture = map(moisture, 400, 1023, 100, 0);  // 400 -> 100%, 1023 -> 0%

  // Deactivate sensor
  digitalWrite(moisture_trigger_pin, LOW);
  delay(100);

  delay(1000); // wait 1 s
}

void trigger_pump(){
  digitalWrite(pump_pin, HIGH);   // activate relay
  delay(pumpRunTime);             // run pump for some time
  digitalWrite(pump_pin, LOW);    // deactivate relay
  
  delay(1000); //wait 1 s
}
