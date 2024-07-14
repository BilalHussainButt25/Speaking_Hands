#include "Wire.h"
#include "I2Cdev.h"
#include "MPU6050.h"
#include <math.h>

MPU6050 mpu;
int16_t ax, ay, az;
int16_t gx, gy, gz;

struct MyData {
  byte X;
  byte Y;
  byte Z;
  float pitch;
  float roll;
  float yaw;
  float accX_body;
  float accY_body;
  float accZ_body;
  float accX_earth;
  float accY_earth;
  float accZ_earth;
  float gyroX;
  float gyroY;
  float gyroZ;
};

MyData data;

const int flexpin1 = A0;
const int flexpin2 = A1;
const int flexpin3 = A2;
const int flexpin4 = A3;
const int flexpin5 = A4;

#define RAD_TO_DEG 57.2957795131
#define DEG_TO_RAD 0.01745329252

unsigned long startTime; 
bool headersPrinted = false;
int readingCount = 0; 
bool breakTime = false; 

const int ledPin = 13;  // Define pin for LED
const int ledPin53 = 53;  // Define pin for LED at port 53

void setup() {
  Serial.begin(9600);
  Wire.begin();
  mpu.initialize();
  startTime = millis();
  pinMode(ledPin, OUTPUT);  // Initialize LED pin as an output
  pinMode(ledPin53, OUTPUT);  // Initialize LED pin 53 as an output
}

void loop() {
  if (readingCount >= 30 || millis() - startTime >= 5000) {
    if (breakTime) {
      delay(5000);  // Take a break for 5 seconds
      digitalWrite(ledPin, LOW);  // Turn off the LED after break time
      digitalWrite(ledPin53, LOW);  // Turn off the LED at port 53 after break time
    } else {
      digitalWrite(ledPin, HIGH);  // Turn on the LED when 30 readings are taken
      digitalWrite(ledPin53, LOW);  // Ensure LED at pin 53 is off during the break period
    }
    startTime = millis();
    breakTime = !breakTime;
    readingCount = 0;
    headersPrinted = false;
  }

  if (!breakTime) {
    digitalWrite(ledPin53, HIGH);  // Turn on the LED at port 53 when receiving values

    mpu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);

    data.X = map(ax, -17000, 17000, 0, 255);  // X axis data
    data.Y = map(ay, -17000, 17000, 0, 255);
    data.Z = map(az, -17000, 17000, 0, 255);  // Y axis data

    data.pitch = atan2(-data.X, sqrt(data.Y * data.Y + data.Z * data.Z)) * RAD_TO_DEG;
    data.roll = atan2(data.Y, data.Z) * RAD_TO_DEG;
    data.yaw = atan2(sqrt(data.X * data.X + data.Z * data.Z), data.Y) * RAD_TO_DEG;

    // Convert accelerometer data from body to earth coordinates
    float accX_body = ax / 16384.0; 
    float accY_body = ay / 16384.0;
    float accZ_body = az / 16384.0;

    // Rotation matrix for pitch, roll, and yaw
    data.accX_earth = accX_body * cos(data.pitch * DEG_TO_RAD) + 
                      accY_body * sin(data.roll * DEG_TO_RAD) * sin(data.pitch * DEG_TO_RAD) +
                      accZ_body * cos(data.roll * DEG_TO_RAD) * sin(data.pitch * DEG_TO_RAD);

    data.accY_earth = accY_body * cos(data.roll * DEG_TO_RAD) - 
                      accZ_body * sin(data.roll * DEG_TO_RAD);

    data.accZ_earth = accX_body * sin(data.pitch * DEG_TO_RAD) + 
                      accY_body * cos(data.roll * DEG_TO_RAD) * cos(data.pitch * DEG_TO_RAD) +
                      accZ_body * cos(data.roll * DEG_TO_RAD) * cos(data.pitch * DEG_TO_RAD);

    data.accX_body = accX_body;
    data.accY_body = accY_body;
    data.accZ_body = accZ_body;

    data.gyroX = gx / 131.0; 
    data.gyroY = gy / 131.0;
    data.gyroZ = gz / 131.0;

    int flexVal1 = analogRead(flexpin1);
    int flexVal2 = analogRead(flexpin2);
    int flexVal3 = analogRead(flexpin3);
    int flexVal4 = analogRead(flexpin4);
    int flexVal5 = analogRead(flexpin5);

    Serial.print(data.X);
    Serial.print(",");
    Serial.print(data.Y);
    Serial.print(",");
    Serial.print(data.Z);
    Serial.print(",");
    Serial.print(data.pitch);
    Serial.print(",");
    Serial.print(data.roll);
    Serial.print(",");
    Serial.print(data.yaw);
    Serial.print(",");
    Serial.print(data.accX_body);
    Serial.print(",");
    Serial.print(data.accY_body);
    Serial.print(",");
    Serial.print(data.accZ_body);
    Serial.print(",");
    Serial.print(data.accX_earth);
    Serial.print(",");
    Serial.print(data.accY_earth);
    Serial.print(",");
    Serial.print(data.accZ_earth);
    Serial.print(",");
    Serial.print(data.gyroX);
    Serial.print(",");
    Serial.print(data.gyroY);
    Serial.print(",");
    Serial.print(data.gyroZ);
    Serial.print(",");
    Serial.print(flexVal1);
    Serial.print(",");
    Serial.print(flexVal2);
    Serial.print(",");
    Serial.print(flexVal3);
    Serial.print(",");
    Serial.print(flexVal4);
    Serial.print(",");
    Serial.print(flexVal5);
    
    if (readingCount < 29) {
      Serial.println(";");
    } else {
      Serial.println();
    }
    
    readingCount++;

    delay(100);

    if (readingCount >= 30) {
      digitalWrite(ledPin53, LOW);
    }
  }
}
